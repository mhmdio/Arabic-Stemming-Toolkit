/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
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
 * The Original Code is MSWordDocument.java.
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.indexing;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import org.apache.log4j.Logger;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.textmining.text.extraction.WordExtractor;
/** This class is used for indexing MS Word document files (ie files ending .doc).
 * 	It does this by using the <a href="http://www.textmining.org">textmining.org</a>
 *  MSWord conversion library (tm-extractors), which in turn uses the Jakarta-POI
 *  libraries. So to compile or use this object, you'll need to ensure poi-?.?.?-final-*.jar
 *  and tm-extractors.jar are part of you classpath.
 *  @author Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
*/
public class MSWordDocument extends FileDocument
{
	protected static final Logger logger = Logger.getLogger(MSWordDocument.class);
	/** Constructs a new MSWordDocument object for the file represented by
	 * 	docStream.
	 */
	public MSWordDocument(String filename, InputStream docStream, Tokeniser tokeniser)
	{
		super(filename, docStream, tokeniser);
	}
	/** 
	 * Constructs a new MSWordDocument object for the file represented by
	 * 	docStream.
	 * @param docStream
	 * @param docProperties
	 * @param tok
	 */
	public MSWordDocument(InputStream docStream,
			Map<String, String> docProperties, Tokeniser tok) {
		super(docStream, docProperties, tok);
	}
	/** 
	 * Constructs a new MSWordDocument object for the file represented by
	 * 	docReader.
	 * @param docReader
	 * @param docProperties
	 * @param tok
	 */
	public MSWordDocument(Reader docReader, Map<String, String> docProperties,
			Tokeniser tok) {
		super(docReader, docProperties, tok);
	}
	/** 
	 * Constructs a new MSWordDocument object for the file 
	 * @param filename
	 * @param docReader
	 * @param tok
	 */
	public MSWordDocument(String filename, Reader docReader, Tokeniser tok) {
		super(filename, docReader, tok);
	}
	/** Converts the docStream InputStream parameter into a Reader which contains
	 *  plain text, and from which terms can be obtained. 
	 *  On failure, returns null and sets EOD to true, so no terms can be read from
	 *  this object.
	 */
	protected Reader getReader(InputStream docStream)
	{
		try{
			WordExtractor  extractor = new WordExtractor();
			String text = extractor.extractText(docStream);
			return new StringReader(text);
		} catch (Exception e) {
			logger.warn("WARNING: Problem converting MS Winword doc: ",e);
			EOD = true;
			return null;
		}
	}
}
