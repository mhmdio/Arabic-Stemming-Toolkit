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
 * The Original Code is WARC09Collection.java
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.indexing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
import org.terrier.utility.FixedSizeInputStream;
import org.terrier.utility.ProcessInputStream;

/**
 * This object is used to parse WARC format web crawls, version 0.9. 
 * The precise {@link Document} class to be used can be specified with the
 * <tt>trec.document.class</tt> property. The following links denote the pages
 * that were used to construct the format of this object:
 * 
 * http://www.yr-bcn.es/webspam/datasets/uk2006-pages/excerpt.txt
 * http://archive-access.sourceforge.net/warc/warc_file_format.html
 * http://crawler.archive.org/apidocs/index.html?org/archive/io/arc/ARCWriter.html
 * http://crawler.archive.org/apidocs/org/archive/io/GzippedInputStream.html
 * 
 * <p>
 * <b>Properties</b>
 * <ul>
 * <li><tt>trec.document.class</tt> the {@link Document} class to parse individual documents (defaults to {@link TaggedDocument}).</li>
 * </ul>
 * </p>
 * 
 * @author Craig Macdonald
 */
public class WARC09Collection implements Collection
{
	/** logger for this class */
	protected static final Logger logger = Logger.getRootLogger();
	/** Counts the number of documents that have been found in this file. */
	protected int documentsInThisFile = 0;
	/** are we at the end of the collection? */
	protected boolean eoc = false;
	/** has the end of the current input file been reached? */
	protected boolean eof = false;
	/** the document number of the current document */
	protected String currentDocno = null;
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
	
	/** Class to use for all documents parsed by this class */
	protected Class<? extends Document> documentClass;
	/** Tokeniser to use for all documents parsed by this class */
	protected Tokeniser tokeniser = Tokeniser.getTokeniser();

	
	final static SimpleDateFormat dateWARC = new SimpleDateFormat("yyyyMMddHHmmss");
	static{
		dateWARC.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	/** default constructor for this collection object. Reads files from the system
	  * default collection.spec file */
	public WARC09Collection()
	{
		this(ApplicationSetup.COLLECTION_SPEC);
	}

	
	/**
     * A constructor that reads only the specificed InputStream.*/
    public WARC09Collection(InputStream input)
    {
        is = input;
        FilesToProcess.clear();
    }
	
	/** construct a collection from the denoted collection.spec file */
	public WARC09Collection(final String CollectionSpecFilename)
	{
		readCollectionSpec(CollectionSpecFilename);
		try{
			openNextFile();
		} catch (IOException ioe) {
			logger.error("Problem opening first file ", ioe);
		}
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
	
	/** {@inheritDoc}
	 * This is unsupported by this Collection implementation, and
	 * any calls will throw UnsupportedOperationException
	 * @throws UnsupportedOperationException on all invocations */
//	@Override 
//	public void remove()
//	{
//		throw new UnsupportedOperationException("Iterator.remove() not supported");
//	}
	

	
	/** Closes the collection, any files that may be open. 
	 * */
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
		return currentDocno;
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
		//return new TaggedDocument(new InputStreamReader(fsis), DocProperties, tokeniser);
	}

	/** Move the collection to the start of the next document. */
	public boolean nextDocument()
	{
		DocProperties = new HashMap<String,String>(15);
		try{
		warcrecord: while(true)
		{
			String line = readLine();
			//look for a warc line
			if (line.toLowerCase().startsWith("warc/"))
			{
				final String[] parts = line.split("\\s+");
				final long length = Long.parseLong(parts[1]);
				int headerSize = line.length() + 2;	
				if (! parts[2].equals("response"))
				{
					//it's not a downloaded document
					is.skip(length - headerSize);
					continue warcrecord;
				}
				/* now let's parse the rest of the WARC header 
				 * format: warc-id data-length record-type subject-uri		   creation-date record-id	content-type 
				 * example: warc/0.9 10757 response http://www.mattenltd.co.uk/ 20060920234350 message/http uuid:c6f7927d-aaea-4e53-b121-c4a594218d8a
				 */
				DocProperties.put("warc-id", parts[0]);
				DocProperties.put("url", parts[3]);
				DocProperties.put("creationdate", parts[4]);
				DocProperties.put("crawldate", parseDate(parts[4]));
				DocProperties.put("uuid", parts[6].replaceAll("^uuid:", ""));
				//System.out.println("parts array="+java.util.Arrays.toString(parts));	
				int blankCount = 0;
				do {
					final String followLine = readLine();
					final int len = followLine.length();
					if (len == 0)
					{
						headerSize+=2;
						blankCount++;
					}
					else
					{
						headerSize+= len +2;
						final int colonIndex = followLine.indexOf(':');
						if (colonIndex < 0)
							continue;
						DocProperties.put(followLine.substring(colonIndex-1).trim(), followLine.substring(colonIndex, len ).trim());
					}
				}while(blankCount != 2);

				//obtain the character set of the document and put in the charset property
				String cType = DocProperties.get("contenttype");
        		if (cType != null)
				{
        			cType = cType.toLowerCase();
        			if (cType.contains("charset"))
       				{
            			final Matcher m = charsetMatchPattern.matcher(cType);
            			if (m.find() && m.groupCount() > 0)
	                		DocProperties.put("charset", m.group(1));
        			}
				}

				//TODO: check for empty documents, redirects?
				documentsInThisFile++;
				currentDocno = FileNumber + "-" + documentsInThisFile;
				DocProperties.put("docno", currentDocno);	
				currentDocumentBlobLength = length - headerSize;
				//logger.debug("Document "+ currentDocno + " blobsize="+currentDocumentBlobLength);
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

	static final Pattern charsetMatchPattern = Pattern.compile("charset=(\\S+)");

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
				//logger.debug("readLine setting eof @1");
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
					//logger.debug("readLine setting eof @2");
					eof = true;
					break;
				}
				ch2 = (char)c;
				if (ch2 == '\n')
					break;
				s.append(ch); s.append(ch2);
			}
			else
			{
				s.append(ch);
			}
		}
		//logger.debug("readLine: "+ s.toString());
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
				String filename = FilesToProcess.get(FileNumber);
				FileNumber++;
				//check the filename is sane
				File f = new File(filename);
				if (! f.exists())
				{
					logger.warn("Could not open "+filename+" : File Not Found");
				}
				else if (! f.canRead())
				{
					logger.warn("Could not open "+filename+" : Cannot read");
				}
				else
				{//filename seems ok, open it
					if (filename.toLowerCase().endsWith(".gz"))
					{
						/* WARC format files have multiple compressed records. JDK one can't deal with this
						 * See: http://crawler.archive.org/apidocs/index.html?org/archive/io/arc/ARCWriter.html
						 * We get around this by using an external zcat process
						 */
						is = new java.io.BufferedInputStream(new ProcessInputStream("/usr/bin/gzip -dc ", filename));
					}
					else
						is = Files.openFileStream(filename); //throws an IOException, throw upwards
					logger.info("WARCCollection processing "+filename);
					//no need to loop again
					tryFile = false;
					//return success
					rtr = true;
					//accurately record file offset
					documentsInThisFile = 0;
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
			logger.info("read collection specification");
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
