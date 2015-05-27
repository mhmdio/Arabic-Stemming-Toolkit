/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.ac.uk/
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is WARC018Collection.java
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.indexing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
import org.terrier.utility.FixedSizeInputStream;

/**
 * This object is used to parse WARC format web crawls, 0.18. 
 * The precise {@link Document} class to be used can be specified with the
 * <tt>trec.document.class</tt> property.
 * 
 * <p>
 * <b>Properties</b>
 * <ul>
 * <li><tt>trec.document.class</tt> the {@link Document} class to parse individual documents (defaults to {@link TaggedDocument}).</li>
 * <li><tt>warc018collection.force.utf8</tt> - should UTF8 encoding be assumed throughout. Defaults to false.</li>
 * <li><tt>warc018collection.header.docno</tt> - what header has the thing to be used as docno? Defaults to warc-trec-id.</li>
 * <li><tt>warc018collection.header.url</tt> - what header has the thing to be used as url? Defaults to warc-target-url.</li>
 * </ul>
 * @author Craig Macdonald
 */
public class WARC018Collection implements Collection
{
	/** logger for this class */
	protected static final Logger logger = Logger.getLogger(WARC018Collection.class);
	/** Counts the number of documents that have been found in this file. */
	protected int documentsInThisFile = 0;
	/** are we at the end of the collection? */
	protected boolean eoc = false;
	/** has the end of the current input file been reached? */
	protected boolean eof = false;
	/** the input stream of the current input file */
	protected InputStream is = null;
	/** the length of the blob containing the document data */
	protected long currentDocumentBlobLength = 0;
	/** properties for the current document */
	protected Map<String,String> DocProperties = null;
	/** The list of files to process. */
	protected ArrayList<String> FilesToProcess = new ArrayList<String>();
	/** The index in the FilesToProcess of the currently processed file.*/
	protected int FileNumber = 0;
	/** should UTF8 encoding be assumed? */
	protected final boolean forceUTF8 = Boolean.parseBoolean(ApplicationSetup.getProperty("warc018collection.force.utf8", "false"));
	/** what header for the docno document metadata */
	protected final String warc_docno_header = ApplicationSetup.getProperty("warc018collection.header.docno","warc-trec-id").toLowerCase();
	/** what header for the url document metadata */
	protected final String warc_url_header = ApplicationSetup.getProperty("warc018collection.header.url", "warc-target-uri").toLowerCase();
	/** what header for the crawldate document metadata */
	protected final String warc_crawldate_header = ApplicationSetup.getProperty("warc018collection.header.crawldate", "date").toLowerCase();
	/** how to parse WARC date formats */
	final static SimpleDateFormat dateWARC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
	/** Encoding to be used to open all files. */	
	protected String desiredEncoding = ApplicationSetup.getProperty("trec.encoding", Charset.defaultCharset().name());
	/** Class to use for all documents parsed by this class */
	protected Class<? extends Document> documentClass;
	/** Tokeniser to use for all documents parsed by this class */
	protected Tokeniser tokeniser = Tokeniser.getTokeniser();	
	
	/** default constructor for this collection object. Reads files from the system
	  * default collection.spec file */
	public WARC018Collection()
	{
		this(ApplicationSetup.COLLECTION_SPEC);
	}

	/** construct a collection from the denoted collection.spec file */
	public WARC018Collection(final String CollectionSpecFilename)
	{
		readCollectionSpec(CollectionSpecFilename);
		loadDocumentClass();
		try{
			openNextFile();
		} catch (IOException ioe) {
			logger.error("Problem opening first file ", ioe);
		}
	}

	/**
     * A constructor that reads only the specificed InputStream.*/
    public WARC018Collection(InputStream input)
    {
        is = input;
        loadDocumentClass();
    }
    /**
     * Check whether it is the last document in the collection
     * @return boolean
     */
	public boolean hasNext() {
		return ! endOfCollection();
	}
	/**
	 * Return the next document
	 * @return next document
	 */
	public Document next()
	{
		nextDocument();
		return getDocument();
	}
	
	/** 
	 * This is unsupported by this Collection implementation, and
	 * any calls will throw UnsupportedOperationException
	 * @throws UnsupportedOperationException on all invocations */
//	@Override 
//	public void remove()
//	{
//		throw new UnsupportedOperationException("Iterator.remove() not supported");
//	}
	
	
	/** Closes the collection, any files that may be open. */
	public void close()
	{
		try{
			is.close();
		} catch (IOException ioe) { 
			logger.warn("Problem closing collection",ioe);
		}
	}

	/** Returns true if the end of the collection has been reached */	
	public boolean endOfCollection()
	{
		return eoc;
	}

	/** Get the String document identifier of the current document. */
	public String getDocid()
	{
		return DocProperties.get("docno");
	}
	
	/** Loads the class that will supply all documents for this Collection.
	 * Set by property <tt>trec.document.class</tt>
	 */
	protected void loadDocumentClass() {
		try{
			documentClass = Class.forName(ApplicationSetup.getProperty("trec.document.class", TaggedDocument.class.getName())).asSubclass(Document.class);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
	

	/** Get the document object representing the current document. */
	public Document getDocument()
	{
		FixedSizeInputStream fsis = new FixedSizeInputStream(is, currentDocumentBlobLength);
		fsis.suppressClose();
		Document rtr; 
		try {
			rtr = documentClass.getConstructor(InputStream.class, Map.class, Tokeniser.class).newInstance(fsis, DocProperties, tokeniser);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return rtr;
//		String charset = DocProperties.get("charset");
//		Reader r;
//		if (charset == null)
//		{
//			r = new InputStreamReader(fsis);
//		}
//		else
//		{
//			try{
//				charset = StringTools.normaliseEncoding(charset);
//				logger.debug("Using "+ charset + " to decode "+ DocProperties.get("docno"));
//				r = new InputStreamReader(fsis, charset);
//			} catch (java.io.UnsupportedEncodingException uee) {
//				logger.warn("Encoding "+charset+ " is unrecognised, resorting to system default");
//                r = new InputStreamReader(fsis);
//			} catch (Exception e) {
//				logger.warn("Problem reading documents, perhaps encoding "+charset+ " is unrecognised, trying to read with system default encoding", e);
//				r = new InputStreamReader(fsis);
//			}
//		}	
//		return new TaggedDocument(r, DocProperties, tokeniser);
	}

	protected int parseHeaders(final boolean requireContentLength) throws IOException
	{
		int headerSize = 0;
		boolean foundContentLength = false;
		while(true)
		{
			final String followLine = readLine();
			final int len = followLine.length();
			headerSize += len +1;
			if (len == 0)
			{
				if ( (! requireContentLength) || (requireContentLength && foundContentLength))
					break;
			}
			final int colonIndex = followLine.indexOf(':');
			if (colonIndex < 0)
			{
				continue;
			}
			final String key = followLine.substring(0,colonIndex).trim().toLowerCase();
			final String value = followLine.substring(colonIndex+1, len).trim();
			DocProperties.put(key, value);
			if (key.equals("content-length"))
				foundContentLength = true;
		}
		return headerSize;
	}

	/** Move the collection to the start of the next document. */
	public boolean nextDocument()
	{
		DocProperties = new HashMap<String,String>(15);
		try{
		warcrecord: while(true)
		{
			String line = readLine();
			//logger.debug("Checking "+line + " for the magic warc header");
			//look for a warc line
			if (line.startsWith("WARC/0.18"))
			{
				//logger.debug("Found warc header");
				int headerSize = parseHeaders(true);
				//logger.debug("Parsed WARC headers in "+ headerSize + " bytes");
				final long warc_response_length = Long.parseLong(DocProperties.get("content-length"));
				//logger.debug("length of http message is "+warc_response_length);
				if (! DocProperties.get("warc-type").equals("response"))
				{
					is.skip(warc_response_length);
					//-49
					continue warcrecord;
				}
				headerSize = parseHeaders(false);
				//logger.debug("Parsed HTTP headers in "+ headerSize + " bytes");
				DocProperties.put("docno", DocProperties.get(warc_docno_header));
				DocProperties.put("url", DocProperties.get(warc_url_header));
				DocProperties.put("crawldate", parseDate(DocProperties.get(warc_crawldate_header)));
				if (logger.isDebugEnabled())
					logger.debug("Now working on document "+ DocProperties.get("docno"));

				DocProperties.put("charset", desiredEncoding);
				//obtain the character set of the document and put in the charset property
				String cType = DocProperties.get("content-type");
				//force UTF-8 for english documents - webpage isnt clear:
				//http://boston.lti.cs.cmu.edu/Data/clueweb09/dataset.html#encodings
				if (cType != null)
				{
					cType = cType.toLowerCase();
					if (cType.contains("charset"))
	   				{
						final Matcher m = charsetMatchPattern.matcher(cType);
						if (m.find() && m.groupCount() > 0) {
							DocProperties.put("charset", m.group(1));
						}
					}
				}
				if (forceUTF8)
					DocProperties.put("charset", "utf-8");
				//TODO: check for empty documents, redirects?
				documentsInThisFile++;
				currentDocumentBlobLength = warc_response_length - headerSize; //-16
				return true;
			}
			if (eof)
			{
				if (documentsInThisFile == 0)
				{
					logger.warn(this.getClass().getSimpleName() + " found no documents in " + FilesToProcess.get(FileNumber-1) + ". "
						+"Perhaps trec.collection.class is wrongly set.");
				}
				if (! openNextFile())
					return false;
			}
		}
		} catch (IOException ioe) {
			logger.error("IOException while reading WARC format collection file" + ioe);
		}
		return false;
	}

	static final Pattern charsetMatchPattern = Pattern.compile("charset[:=]\\s*['\"]?([0-9a-zA-Z_\\-]+)['\"]?");

	/** read a line from the currently open InputStream is */
	protected String readLine() throws IOException
	{
		final StringBuilder s = new StringBuilder();
		int c = 0;char ch; char ch2;
		while(true)
		{
			c = is.read();
			if (c == -1)
			{
				eof = true;
				break;
			}
			ch = (char)c;
			if (ch == '\r')
			{
				c = is.read();
				if (c== -1)
				{
					s.append(ch);
					eof = true;
					break;
				}
				ch2 = (char)c;
				if (ch2 == '\n')
					break;
				s.append(ch); s.append(ch2);
			}
			else if (ch == '\n')
			{
				break;
			}
			else
			{
				s.append(ch);
			}
		}
		return s.toString();
	}

	/**
	 * Opens the next document from the collection specification.
	 * @return boolean true if the file was opened successufully. If there
	 *	   are no more files to open, it returns false.
	 * @throws IOException if there is an exception while opening the
	 *	   collection files.
	 */
	protected boolean openNextFile() throws IOException {
		//try to close the currently open file
		if (is!=null)
			try{
				is.close();
			}catch (IOException ioe) {
				logger.warn("IOException while closing file being read", ioe);
			}
		//keep trying files
		boolean tryFile = true;
		//return value for this fn
		boolean rtr = false;
		while(tryFile)
		{
			if (FileNumber < FilesToProcess.size()) {
				//SkipFile = true;
				String filename = (String) FilesToProcess.get(FileNumber);
				FileNumber++;
				//check the filename is sane
				if (! Files.exists(filename))
				{
					logger.warn("Could not open "+filename+" : File Not Found");
				}
				else if (! Files.canRead(filename))
				{
					logger.warn("Could not open "+filename+" : Cannot read");
				}
				else
				{//filename seems ok, open it
					//if (filename.toLowerCase().endsWith(".gz"))
					//{
						/* WARC format files have multiple compressed records. JDK one can't deal with this
						 * See: http://crawler.archive.org/apidocs/index.html?org/archive/io/arc/ARCWriter.html
						 * We get around this by using an external zcat process
						 */
					//	is = new ProcessInputStream("/usr/bin/gzip -dc ", filename);
					//}
					//else
						is = Files.openFileStream(filename); //throws an IOException, throw upwards
					logger.info("WARC018Collection processing "+filename);
					//no need to loop again
					tryFile = false;
					//return success
					rtr = true;
					//accurately record file offset
					documentsInThisFile = 0;
					eof = false;
				}
			} else {
				//last file of the collection has been read, EOC
				eoc = true;
				rtr = false;
				tryFile = false;
			}
		}
		return rtr;
	}

	/** read in the collection.spec */
	protected void readCollectionSpec(String CollectionSpecFilename)
	{
		//reads the collection specification file
		try {
			BufferedReader br2 = Files.openFileReader(CollectionSpecFilename);
			String filename = null;
			FilesToProcess = new ArrayList<String>();
			while ((filename = br2.readLine()) != null) {
				filename = filename.trim();
				if (!filename.startsWith("#") && !filename.equals(""))
					FilesToProcess.add(filename);
			}
			br2.close();
			logger.info("WARC018Collection read collection specification");
		} catch (IOException ioe) {
			logger.error("Input output exception while loading the collection.spec file. "
							+ "("+CollectionSpecFilename+")", ioe);
		}
	}

	/** Resets the Collection iterator to the start of the collection. */
	public void reset()
	{}
	
	final static String parseDate(String date)
	{
		if (date == null)
			return "";
		try{
			return Long.toString(dateWARC.parse(date).getTime());
		} catch (ParseException pe ) {
			return "";
		}
	}

}
