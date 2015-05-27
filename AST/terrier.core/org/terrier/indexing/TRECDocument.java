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
 * The Original Code is TRECDocument.java.
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.indexing;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

import org.terrier.indexing.tokenisation.Tokeniser;

/** TRECDocument is a placeholder class - all functionality is implemented
 * in {@link TaggedDocument}. This class will be removed in a future
 * release of Terrier. 
 */
@Deprecated
public class TRECDocument extends TaggedDocument {
	/** 
	 * Construct TRECDocument object from input stream.
	 * @param docStream
	 * @param docProperties
	 * @param _tokeniser
	 */
	public TRECDocument(InputStream docStream,
			Map<String, String> docProperties, Tokeniser _tokeniser) {
		super(docStream, docProperties, _tokeniser);
		logger.warn("TRECDocument is deprecated. Use TaggedDocument instead");
	}
	/** 
	 * Construct TRECDocument object from reader.
	 * @param docReader
	 * @param docProperties
	 * @param _tokeniser
	 */
	public TRECDocument(Reader docReader, Map<String, String> docProperties,
			Tokeniser _tokeniser) {
		super(docReader, docProperties, _tokeniser);
		logger.warn("TRECDocument is deprecated. Use TaggedDocument instead");
	}
	/** 
	 * Construct TRECDocument object from reader.
	 * @param docReader
	 * @param docProperties
	 */
	public TRECDocument(Reader docReader, Map<String, String> docProperties) {
		super(docReader, docProperties, Tokeniser.getTokeniser());
		logger.warn("TRECDocument is deprecated. Use TaggedDocument instead");
	}

	
}
