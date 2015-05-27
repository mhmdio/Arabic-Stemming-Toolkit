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
 * The Original Code is OldBasicMatching.java.
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.terrier.matching.dsms.DocumentScoreModifier;
import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.HeapSort;
/**
 * <i>This is the original matching implementation of Terrier</i>.
 * Performs the matching of documents with a query, by
 * first assigning scores to documents for each query term
 * and modifying these scores with the appropriate modifiers.
 * Then, a series of document score modifiers are applied
 * if necessary.
 * 
 * @author Vassilis Plachouras, Craig Macdonald
  */
public class OldBasicMatching implements Matching {

     /** the logger for this class */
	protected static final Logger logger = Logger.getLogger(Matching.class);

	/**
	 * The default namespace for the document score modifiers that are
	 * specified in the properties file.
	 */
	protected static final String dsmNamespace = "org.terrier.matching.dsms.";
	
	/** 
	 * The maximum number of documents in the final retrieved set.
	 * It corresponds to the property <tt>matching.retrieved_set_size</tt>.
	 * The default value is 1000, however, setting the property to 0
	 * will return all matched documents.
	 */
	protected static int RETRIEVED_SET_SIZE;
	
	
	/**
	 * A property that enables to ignore the terms with a low
	 * IDF. In the match method, we check whether the frequency
	 * of a term in the collection is higher than the number of
	 * documents. If this is true, then by default we don't assign
	 * scores to documents that contain this term. We can change
	 * this default behaviour by altering the corresponding property
	 * <tt>ignore.low.idf.terms</tt>, the default value of which is
	 * true.
	 */
	protected static boolean IGNORE_LOW_IDF_TERMS;
	
	/**
	 * A property that when it is true, it allows matching all documents
	 * to an empty query. In this case the ordering of documents is 
	 * random. More specifically, it is the ordering of documents in 
	 * the document index. The corresponding property is 
	 * <tt>match.empty.query</tt> and the default value is <tt>false</tt>.
	 */
	protected static boolean MATCH_EMPTY_QUERY;
	
	/** The number of retrieved documents for a query.*/
	protected int numberOfRetrievedDocuments;
		
	/**
	 * The index used for retrieval. 
	 */ 
	protected Index index;
	
	/** The lexicon used.*/
	protected Lexicon<String> lexicon;
	/** The inverted file.*/
	protected PostingIndex<BitIndexPointer> invertedIndex;
	/** The collection statistics */
	protected CollectionStatistics collectionStatistics;
	/** The result set.*/
	protected ResultSet resultSet;
	
	
	/**
	 * Contains the document score modifiers
	 * to be applied for a query.
	 */
	protected ArrayList<DocumentScoreModifier> documentModifiers;


	protected OldBasicMatching() { /* do nothing constructor */
		documentModifiers = new ArrayList<DocumentScoreModifier>();
	}
	
	
	protected void initialiseDSMs()
	{
		
	}
	
	/** 
	 * A default constructor that creates 
	 * the CollectionResultSet and initialises
	 * the document and term modifier containers.
	 * @param _index the object that encapsulates the basic
	 *        data structures used for retrieval.
	 */
	public OldBasicMatching(Index _index) {
		documentModifiers = new ArrayList<DocumentScoreModifier>();
		this.index = _index;
		this.lexicon = _index.getLexicon();

		
		this.invertedIndex = _index.getInvertedIndex();
		this.collectionStatistics = _index.getCollectionStatistics();
		resultSet = new CollectionResultSet(collectionStatistics.getNumberOfDocuments());		
		//adding document and term score modifiers that will be 
		//used for all queries, independently of the query operators
		//only modifiers with default constructors can be used in this way.
		//String defaultTSMS = ApplicationSetup.getProperty("matching.tsms","");
		String defaultDSMS = ApplicationSetup.getProperty("matching.dsms","");
		
		try {
//			for(String modifierName : defaultTSMS.split("\\s*,\\s*"))
//			{
//				if (modifierName.length() == 0)
//					continue;
//				if (modifierName.indexOf('.') == -1) 
//					modifierName = tsmNamespace + modifierName;
//				addTermScoreModifier((TermScoreModifier)Class.forName(modifierName).newInstance());
//			}

			for(String modifierName : defaultDSMS.split("\\s*,\\s*"))
			{
				if (modifierName.length() == 0)
                    continue;
				if (modifierName.indexOf('.') == -1)
					modifierName = dsmNamespace + modifierName;
				else if (modifierName.startsWith("uk.ac.gla.terrier"))
					modifierName = modifierName.replaceAll("uk.ac.gla.terrier", "org.terrier");
				
				addDocumentScoreModifier((DocumentScoreModifier)Class.forName(modifierName).newInstance());
			}

		} catch(Exception e) {
			logger.error("Exception while initialising default modifiers. Please check the name of the modifiers in the configuration file.", e);
		}
	}
	
	/**
	 * Returns the result set.
	 * @deprecated match() now returns the ResultSet
	 */
	public ResultSet getResultSet() {
		return resultSet;
	}
	/**
	 * Initialises the arrays prior of retrieval. Only the first time it is called,
	 * it will allocate memory for the arrays.
	 */
	protected void initialise() {
		resultSet.initialise();
		RETRIEVED_SET_SIZE = Integer.parseInt(ApplicationSetup.getProperty("matching.retrieved_set_size", "1000"));
		//FREQUENCY_UPPER_THRESHOLD = Integer.parseInt(ApplicationSetup.getProperty("frequency.upper.threshold", "0"));
		IGNORE_LOW_IDF_TERMS = Boolean.parseBoolean(ApplicationSetup.getProperty("ignore.low.idf.terms","true"));
		MATCH_EMPTY_QUERY = Boolean.parseBoolean(ApplicationSetup.getProperty("match.empty.query","false"));
	}
	/**
	 * Initialises the arrays prior of retrieval, with 
	 * the given scores. Only the first time it is called,
	 * it will allocate memory for the arrays.
	 * @param scs double[] the scores to initialise the result set with.
	 */
	protected void initialise(double[] scs) {
		resultSet.initialise(scs);
		RETRIEVED_SET_SIZE = Integer.parseInt(ApplicationSetup.getProperty("matching.retrieved_set_size", "1000"));
		//FREQUENCY_UPPER_THRESHOLD = Integer.parseInt(ApplicationSetup.getProperty("frequency.upper.threshold", "0"));
		IGNORE_LOW_IDF_TERMS = Boolean.parseBoolean(ApplicationSetup.getProperty("ignore.low.idf.terms","true"));
		MATCH_EMPTY_QUERY = Boolean.parseBoolean(ApplicationSetup.getProperty("match.empty.query","false"));
	}
	
	/**
	 * Registers a document score modifier. If more than one modifiers
	 * are registered, then they applied in the order they were registered.
	 * @param documentScoreModifier DocumentScoreModifier the score modifier to be
	 *        applied. 
	 */
	public void addDocumentScoreModifier(DocumentScoreModifier documentScoreModifier) {
		documentModifiers.add(documentScoreModifier);
	}
	
	/**
	 * Returns the i-th registered document score modifier.
	 * @return the i-th registered document score modifier.
	 */
	public DocumentScoreModifier getDocumentScoreModifier(int i) {
		return (DocumentScoreModifier)documentModifiers.get(i);
	}
	/**
	 * Sets the weighting model used for retrieval.
	 * @param model the weighting model used for retrieval
	 * @deprecated
	 */
	public void setModel(Model model) {
		//wmodel = (WeightingModel)model;
	}
	/**
	 * Set the collection statistics.
	 */
	public void setCollectionStatistics(CollectionStatistics cs)
	{
		collectionStatistics = cs;
	}
	
	/**
	 * Returns a descriptive string for the retrieval process performed.
	 */
	public String getInfo() {
		return "TODO";
		//return wmodel.getInfo();
	}
	
	/**
	 * Implements the matching of a query with the documents.
	 * @param queryNumber the identifier of the processed query.
	 * @param queryTerms the query terms to be processed.
	 * @return Returns the resultset expressed by this query.
	 */
	public ResultSet match(String queryNumber, MatchingQueryTerms queryTerms) throws IOException {
		//the first step is to initialise the arrays of scores and document ids.
		initialise();
		
		
		
		String[] queryTermStrings = queryTerms.getTerms();
		//check whether we need to match an empty query.
		//if so, then return the existing result set.
		if (MATCH_EMPTY_QUERY && queryTermStrings.length == 0) {
			resultSet.setExactResultSize(collectionStatistics.getNumberOfDocuments());
			resultSet.setResultSize(collectionStatistics.getNumberOfDocuments());
			return resultSet;
		}
		
		//load in the dsms
		DocumentScoreModifier[] dsms; int NumberOfQueryDSMs = 0;
		dsms = queryTerms.getDocumentScoreModifiers();
		if (dsms!=null)
			NumberOfQueryDSMs = dsms.length;
		//the number of document score modifiers
		int numOfDocModifiers = documentModifiers.size();
		
		//in order to save the time from references to the arrays, we create local references
		int[] docids = resultSet.getDocids();
		double[] scores = resultSet.getScores();
		short[] occurences = resultSet.getOccurrences();
		
		
		
		//the number of documents with non-zero score.
		numberOfRetrievedDocuments = 0;
		
		//the pointers read from the inverted file
		IterablePosting postings;		
		
		//inform the weighting model of the collection statistics
		

		//for each query term in the query
		final int queryLength = queryTermStrings.length;
		
		
		for (int i = 0; i < queryLength; i++) {
			
			//get the entry statistics - perhaps this came from "far away"
			EntryStatistics entryStats = queryTerms.getStatistics(queryTermStrings[i]);
			//we seek the query term in the lexicon
			LexiconEntry lEntry = lexicon.getLexiconEntry(queryTermStrings[i]);
			if (entryStats == null)
				entryStats = lEntry;
			
			//and if it is not found, we continue with the next term
			if (lEntry==null)
			{
				logger.info("Term Not Found: "+queryTermStrings[i]);
				continue;
			}
			queryTerms.setTermProperty(queryTermStrings[i], lEntry);
			logger.debug((i + 1) + ": " + queryTermStrings[i].trim() + " with " + entryStats.getDocumentFrequency() 
					+ " documents (TF is " + entryStats.getFrequency() + ").");
			
			//check if the IDF is very low.
			if (IGNORE_LOW_IDF_TERMS && collectionStatistics.getNumberOfDocuments() < lEntry.getFrequency()) {
				logger.debug("query term " + queryTermStrings[i] + " has low idf - ignored from scoring.");
				continue;
			}
			
			//the weighting models are prepared for assigning scores to documents
			WeightingModel[] termWeightingModels = queryTerms.getTermWeightingModels(queryTermStrings[i]);
			
			if (termWeightingModels.length == 0)
			{
				logger.warn("No weighting models for term "+ queryTermStrings[i] +", skipping scoring");
				continue;
			}
			
			for (WeightingModel wmodel: termWeightingModels)
			{
				wmodel.setCollectionStatistics(collectionStatistics);
				wmodel.setRequest(queryTerms.getRequest());
				wmodel.setKeyFrequency(queryTerms.getTermWeight(queryTermStrings[i]));
				wmodel.setEntryStatistics(entryStats);
				IndexUtil.configure(index, wmodel);
				//this requests any pre-calculations to be made
				wmodel.prepare();
			}
			
			//the postings are being read from the inverted file.
			postings = invertedIndex.getPostings((BitIndexPointer)lEntry);
			
			//assign scores to documents for a term
			//assignScores(termScores, pointers);
			assignScores(i, termWeightingModels, resultSet, postings, lEntry, queryTerms.getTermWeight(queryTermStrings[i]));
		}
		logger.info("Number of docs with +ve score: "+numberOfRetrievedDocuments);
		//sort in descending score order the top RETRIEVED_SET_SIZE documents
		//long sortingStart = System.currentTimeMillis();
		//we need to sort at most RETRIEVED_SET_SIZE, or if we have retrieved
		//less documents than RETRIEVED_SET_SIZE then we need to find the top 
		//numberOfRetrievedDocuments.
		int set_size = Math.min(RETRIEVED_SET_SIZE, numberOfRetrievedDocuments);
		if (set_size == 0) 
			set_size = numberOfRetrievedDocuments;
		
		//sets the effective size of the result set.
		resultSet.setExactResultSize(numberOfRetrievedDocuments);
		
		//sets the actual size of the result set.
		resultSet.setResultSize(set_size);
		
		HeapSort.descendingHeapSort(scores, docids, occurences, set_size);
		//long sortingEnd = System.currentTimeMillis();
		
		/*we apply the query dependent document score modifiers first and then 
		we apply the application dependent ones. This is to ensure that the 
		BooleanFallback modifier is applied last. If there are more than 
		one application dependent dsms, then it's up to the application, ie YOU!
		to ensure that the BooleanFallback is applied last.*/
		
		/* dsms each require resorts of the result list. This is expensive, so should
		   be avoided if possible. Sorting is only done if the dsm actually altered any scores */

		/*query dependent modification of scores
		of documents for a query, defined by this query*/
		for (int t = NumberOfQueryDSMs-1; t >= 0; t--) {
			if (dsms[t].modifyScores(index, queryTerms, resultSet))
				HeapSort.descendingHeapSort(scores, docids, occurences, resultSet.getResultSize());
		}
		
		/*application dependent modification of scores
		of documents for a query, based on a static set by the client code
		sorting the result set after applying each DSM*/
		for (int t = 0; t < numOfDocModifiers; t++) {
			if (documentModifiers.get(t).modifyScores(
					index, 
					queryTerms, 
					resultSet))
				HeapSort.descendingHeapSort(scores, docids, occurences, resultSet.getResultSize());
		}
		logger.debug("number of retrieved documents: " + resultSet.getResultSize());
		return resultSet;
	}
	/** Assign scores method 
	 * @param i which query term is this
	 * @param wModels weighting models to use for this term
	 * @param rs Resultset to alter
	 * @param postings post list to process
	 * @param lEntry entry statistics 
	 * @param queryTermWeight weight of the query term
	 * */
	protected void assignScores(int i, final WeightingModel[] wModels, ResultSet rs, final IterablePosting postings, LexiconEntry lEntry, double queryTermWeight)
		throws IOException
	{
		
		
		//for each document that contains 
		//the query term, the score is computed.
		double score;
		double[] scores = rs.getScores();
		short[] occurences = rs.getOccurrences();
		
		//finally setting the scores of documents for a term
		//a mask for setting the occurrences
		short mask = 0;
		if (i<16)
			mask = (short)(1 << i);
		
		int docid;
		while((docid = postings.next()) != IterablePosting.EOL)
		{
			score = 0;

			for (WeightingModel wmodel: wModels)
			{
				score += wmodel.score(postings);
			}
			if ((scores[docid] == 0.0d) && (score > 0.0d)) {
				numberOfRetrievedDocuments++;
			} else if ((scores[docid] > 0.0d) && (score < 0.0d)) {
				numberOfRetrievedDocuments--;
			}
			scores[docid] += score;
			occurences[docid] |= mask;
		}
	}
}
