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
 * The Original Code is IndexTestUtils.java.
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.indexing;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.terrier.indexing.tokenisation.EnglishTokeniser;
import org.terrier.structures.Index;
import org.terrier.utility.ApplicationSetup;

public class IndexTestUtils {

	public static Index makeIndex(String[] docnos, String[] documents) throws Exception
	{
		return makeIndex(docnos, documents, new BasicIndexer(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX));
	}
	
	public static Index makeIndex(String[] docnos, String[] documents, Indexer indexer) throws Exception
	{
		assertEquals(docnos.length, documents.length);
		Document[] sourceDocs = new Document[docnos.length];
		for(int i=0;i<docnos.length;i++)
		{
			Map<String,String> docProperties = new HashMap<String,String>();
			docProperties.put("filename", docnos[i]);
			sourceDocs[i] = new FileDocument(new ByteArrayInputStream(documents[i].getBytes()), docProperties, new EnglishTokeniser());
		}
		Collection col = new CollectionDocumentList(sourceDocs, "filename");
		indexer.index(new Collection[]{col});		
		Index index = Index.createIndex();
		assertEquals(sourceDocs.length, index.getCollectionStatistics().getNumberOfDocuments());
		return index;
	}
	
}
