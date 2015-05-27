package test;
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
 * The Original Code is TerrierDefaultTestSuite.java
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.terrier.compression.TestCompressedBitFiles;
import org.terrier.evaluation.TestAdhocEvaluation;
import org.terrier.indexing.TestCollections;
import org.terrier.indexing.TestCrawlDate;
import org.terrier.indexing.TestIndexers;
import org.terrier.indexing.TestSimpleFileCollection;
import org.terrier.indexing.TestSimpleXMLCollection;
import org.terrier.indexing.TestTRECCollection;
import org.terrier.indexing.TestTaggedDocument;
import org.terrier.indexing.tokenisation.TestEnglishTokeniser;
import org.terrier.indexing.tokenisation.TestUTFTokeniser;
import org.terrier.matching.TestMatching.TestDAATFullMatching;
import org.terrier.matching.TestMatching.TestDAATFullNoPLMMatching;
import org.terrier.matching.TestMatching.TestTAATFullMatching;
import org.terrier.matching.TestMatching.TestTAATFullNoPLMMatching;
import org.terrier.matching.TestMatchingQueryTerms;
import org.terrier.matching.TestResultSets;
import org.terrier.matching.TestTRECResultsMatching;
import org.terrier.matching.models.TestWeightingModelFactory;
import org.terrier.querying.TestManager;
import org.terrier.querying.parser.TestQueryParser;
import org.terrier.statistics.TestGammaFunction.TestWikipediaLanczosGammaFunction;
import org.terrier.structures.TestBasicLexiconEntry;
import org.terrier.structures.TestBitIndexPointer;
import org.terrier.structures.TestBitPostingIndex;
import org.terrier.structures.TestBitPostingIndexInputStream;
import org.terrier.structures.TestCompressingMetaIndex;
import org.terrier.structures.TestPostingStructures;
import org.terrier.structures.TestTRECQuery;
import org.terrier.structures.collections.TestFSArrayFile;
import org.terrier.structures.collections.TestFSOrderedMapFile;
import org.terrier.structures.indexing.TestCompressingMetaIndexBuilderPartitioner;
import org.terrier.structures.indexing.singlepass.hadoop.TestBitPostingIndexInputFormat;
import org.terrier.structures.indexing.singlepass.hadoop.TestPositingAwareSplit;
import org.terrier.structures.indexing.singlepass.hadoop.TestSplitEmittedTerm;
import org.terrier.structures.postings.TestORIterablePosting;
import org.terrier.structures.serialization.TestFixedSizeTextFactory;
import org.terrier.terms.TestPorterStemmer;
import org.terrier.terms.TestTermPipelineAccessor;
import org.terrier.tests.ShakespeareEndToEndTestSuite;
import org.terrier.utility.TestArrayUtils;
import org.terrier.utility.TestDistance;
import org.terrier.utility.TestHeapSort;
import org.terrier.utility.TestRounding;
import org.terrier.utility.TestStaTools;
import org.terrier.utility.TestTagSet;
import org.terrier.utility.TestTermCodes;
import org.terrier.utility.io.TestCountingInputStream;
import org.terrier.utility.io.TestHadoopPlugin;
import org.terrier.utility.io.TestRandomDataInputMemory;


/** This class defines the active JUnit test classes for Terrier
 * @since 3.0
 * @author Craig Macdonald */
@RunWith(Suite.class)
@SuiteClasses({
	//.tests
	ShakespeareEndToEndTestSuite.class,
	
	//.compression
	TestCompressedBitFiles.class,
	
	//.evaluation
	TestAdhocEvaluation.class,
	
	//.indexing
	TestCollections.class,
	TestCrawlDate.class,
	TestIndexers.class,
	TestSimpleFileCollection.class,
	TestTaggedDocument.class,
	TestSimpleXMLCollection.class,
	TestTRECCollection.class,
	
	//.indexing.tokenisation
	TestEnglishTokeniser.class,
	TestUTFTokeniser.class,
	
	//.matching
	TestMatchingQueryTerms.class,
	TestDAATFullMatching.class,
	TestDAATFullNoPLMMatching.class,
	TestTAATFullMatching.class,
	TestTAATFullNoPLMMatching.class,
	TestTRECResultsMatching.class,
	TestResultSets.class,
	
	//matching.models
	TestWeightingModelFactory.class,
	
	//querying
	TestManager.class,
	
	//querying.parser
	TestQueryParser.class,
	
	//.statistics
	TestWikipediaLanczosGammaFunction.class,
	
	//.structures
	TestBasicLexiconEntry.class,
	TestBitIndexPointer.class,
	TestBitPostingIndex.class,
	TestBitPostingIndexInputStream.class,
	TestCompressingMetaIndex.class,
	TestPostingStructures.class,
	TestTRECQuery.class,
	
	//.structures.collections
	TestFSOrderedMapFile.class,
	TestFSArrayFile.class,
	
	//.structures.indexing
	TestCompressingMetaIndexBuilderPartitioner.class,
	
	//.structures.indexing.sp.hadoop
	TestBitPostingIndexInputFormat.class,
	TestSplitEmittedTerm.class,
	TestPositingAwareSplit.class,
	
	//structures.postings
	TestORIterablePosting.class,
	
	//.structures.serialization
	TestFixedSizeTextFactory.class,
	
	//.terms
	TestTermPipelineAccessor.class,
	TestPorterStemmer.class,
	
	//.utility
	TestArrayUtils.class,
	TestDistance.class,
	TestHeapSort.class,
	TestRounding.class,
	TestTagSet.class,
	TestStaTools.class,
	TestTermCodes.class,
	
	//utility.io
	TestRandomDataInputMemory.class,
	TestHadoopPlugin.class,
	TestCountingInputStream.class
	
	
	
})
public class TerrierDefaultTestSuite {}
