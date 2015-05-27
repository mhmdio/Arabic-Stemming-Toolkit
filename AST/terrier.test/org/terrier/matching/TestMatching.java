/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - Department of Computing Science
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
 * The Original Code is TestMatching.java.
 *
 * The Original Code is Copyright (C) 2004-2010 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gnu.trove.TIntHashSet;

import org.junit.Before;
import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.matching.models.DLH13;
import org.terrier.structures.Index;
import org.terrier.structures.LexiconEntry;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public abstract class TestMatching extends ApplicationSetupBasedTest {

	public static class TestTAATFullMatching extends TestMatching
	{
		@Override
		protected Matching makeMatching(Index i)
		{
			return new org.terrier.matching.taat.Full(i);
		}
	}
	
	public static class TestDAATFullMatching extends TestMatching
	{
		@Override
		protected Matching makeMatching(Index i)
		{
			return new org.terrier.matching.daat.Full(i);
		}
	}
	
	
	public static class TestTAATFullNoPLMMatching extends TestMatching
	{
		@Override
		protected Matching makeMatching(Index i)
		{
			return new org.terrier.matching.taat.FullNoPLM(i);
		}
		
		@Override
		public void testThreeDocumentsSynonymIndexMatching() throws Exception {}
	}
	
	public static class TestDAATFullNoPLMMatching extends TestMatching
	{
		@Override
		protected Matching makeMatching(Index i)
		{
			return new org.terrier.matching.daat.FullNoPLM(i);
		}

		@Override
		public void testThreeDocumentsSynonymIndexMatching() throws Exception {}
	}
	
	
	@Before public void setIndexerProperties()
	{
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename");
		ApplicationSetup.setProperty("indexer.meta.reverse.keys", "");
		ApplicationSetup.setProperty("termpipelines", "");
	}
	
	protected abstract Matching makeMatching(Index i);
	
	@Test public void testSingleDocumentIndexMatching() throws Exception
	{
		Index index = IndexTestUtils.makeIndex(
				new String[]{"doc1"}, 
				new String[]{"The quick brown fox jumps over the lazy dog"});
		System.err.println("testSingleDocumentIndexMatching: " + index.toString());
		assertNotNull(index);
		assertEquals(1, index.getCollectionStatistics().getNumberOfDocuments());
		Matching matching = makeMatching(index);
		assertNotNull(matching);
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.setTermProperty("quick", 1);
		mqt.setDefaultTermWeightingModel(new DLH13());
		ResultSet rs = matching.match("query1", mqt);
		assertNotNull(rs);
		assertEquals(1, rs.getResultSize());
		assertEquals(0, rs.getDocids()[0]);
		assertTrue(rs.getScores()[0] > 0);
	}
	
	@Test public void testTwoDocumentsIndexMatching() throws Exception
	{
		Index index = IndexTestUtils.makeIndex(
				new String[]{"doc1", "doc2"}, 
				new String[]{
						"The quick brown fox jumps over the lazy dog",
						"how much is that dog in the window"});
		System.err.println("testTwoDocumentsIndexMatching: " + index.toString());
		assertNotNull(index);
		assertEquals(2, index.getCollectionStatistics().getNumberOfDocuments());
		Matching matching = makeMatching(index);
		assertNotNull(matching);
		MatchingQueryTerms mqt;
		ResultSet rs;
		
		mqt = new MatchingQueryTerms();
		mqt.setTermProperty("quick", 1);
		mqt.setDefaultTermWeightingModel(new DLH13());
		rs = matching.match("query1", mqt);
		assertNotNull(rs);
		assertEquals(1, rs.getResultSize());
		assertEquals(0, rs.getDocids()[0]);
		assertTrue(rs.getScores()[0] > 0);
		
		mqt = new MatchingQueryTerms();
		mqt.setTermProperty("dog", 1);
		mqt.setDefaultTermWeightingModel(new DLH13());
		rs = matching.match("query2", mqt);
		assertNotNull(rs);
		assertEquals(2, rs.getResultSize());		
		assertTrue(rs.getScores()[0] > 0);
		assertTrue(rs.getScores()[1] > 0);
	}
	
	@Test public void testThreeDocumentsSynonymIndexMatching() throws Exception
	{
		Index index = IndexTestUtils.makeIndex(
				new String[]{"doc1", "doc2", "doc3"}, 
				new String[]{
						"The quick brown fox jumps over the lazy dog",
						"how much is that dog in the window",
						"the one with the waggily tail"});
		System.err.println("testThreeDocumentsSynonymIndexMatching: " + index.toString());
		assertNotNull(index);
		assertEquals(3, index.getCollectionStatistics().getNumberOfDocuments());
		Matching matching = makeMatching(index);
		assertNotNull(matching);
		MatchingQueryTerms mqt;
		ResultSet rs;
		
		mqt = new MatchingQueryTerms();
		mqt.setTermProperty("quick|waggily");
		mqt.setDefaultTermWeightingModel(new DLH13());
		rs = matching.match("query1", mqt);
		assertNotNull(rs);
		assertEquals(2, rs.getResultSize());
		TIntHashSet docids = new TIntHashSet(rs.getDocids());
		System.err.println("" + rs.getDocids()[0] + " "+ rs.getScores()[0]);
		System.err.println("" + rs.getDocids()[1] + " "+ rs.getScores()[1]);
		assertTrue(docids.contains(0));
		assertTrue(docids.contains(2));
		assertEquals(2, rs.getDocids()[0]);
		assertEquals(0, rs.getDocids()[1]);
		assertTrue(rs.getScores()[0] > 0);
		assertTrue(rs.getScores()[1] > 0);
		
	}
	
	
	
	@Test public void testMatchingNonStatisticsOverwrite() throws Exception
	{
		Index index = IndexTestUtils.makeIndex(
				new String[]{"doc1"}, 
				new String[]{"The quick brown fox jumps over the lazy dog"});
		assertNotNull(index);
		System.err.println("testMatchingNonStatisticsOverwrite: " + index.toString());
		assertEquals(1, index.getCollectionStatistics().getNumberOfDocuments());
		Matching matching = makeMatching(index);
		assertNotNull(matching);
		
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.setDefaultTermWeightingModel(new DLH13());
		LexiconEntry le = index.getLexicon().getLexiconEntry("quick");
		assertNotNull(le);
		le.setStatistics(1, 40);
		mqt.setTermProperty("quick", le);
		ResultSet rs = matching.match("query1", mqt);
		assertNotNull(rs);
		assertEquals(1, rs.getResultSize());
		assertEquals(0, rs.getDocids()[0]);
		
		//check that statistics havent been overwritten
		assertEquals(40, mqt.getStatistics("quick").getFrequency());
	}
	
}
