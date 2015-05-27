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
 * The Original Code is PhraseScoreModifier.java.
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching.dsms;

import gnu.trove.TIntIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.ResultSet;
import org.terrier.querying.parser.Query;
import org.terrier.querying.parser.SingleTermQuery;
import org.terrier.structures.Index;
import org.terrier.structures.InvertedIndex;
import org.terrier.structures.LexiconEntry;
import org.terrier.utility.ApplicationSetup;

/**
 * Modifies the scores of the documents which contain, or do not contain a given
 * phrase.
 * 
 * @author Vassilis Plachouras, Craig Macdonald
  */
public class PhraseScoreModifier implements DocumentScoreModifier {
	/** the logger for this class */
	protected static final Logger logger = Logger.getLogger(PhraseScoreModifier.class);
	/**
	 * The maximum distance, in blocks, that is allowed between the phrase
	 * terms. The default value of one corresponds to phrase search, while any
	 * higher value enables proximity search.
	 */
	protected int blockDistance = 1;

	/** A list of the strings of the phrase terms. */
	protected List<Query> phraseTerms;

	/**
	 * Indicates whether the phrase should appear in the retrieved documents, or
	 * not. The default value is true.
	 */
	protected boolean required = true;

	/**
     * Number of tokens in one block. As defined in ApplicationSetup.BLOCK_SIZE
	 */
	protected static final int BLOCK_SIZE = ApplicationSetup.BLOCK_SIZE;

	/**
	 * Constructs a phrase score modifier for a given set of query terms.
	 * 
	 * @param pTerms
	 *            ArrayList the terms that make up the query.
	 */
	public PhraseScoreModifier(List<Query> pTerms) {
		phraseTerms = pTerms;
	}

	/**
	 * Constructs a phrase score modifier for a given set of query terms and the
	 * allowed distance between them.
	 * 
	 * @param pTerms
	 *            ArrayList the terms that make up the query.
	 * @param bDist
	 *            int the allowed distance between phrase terms.
	 */
	public PhraseScoreModifier(List<Query> pTerms, int bDist) {
		phraseTerms = pTerms;
		blockDistance = bDist;
	}

	/**
	 * Constructs a phrase score modifier for a given set of query terms.
	 * 
	 * @param pTerms
	 *            ArrayList the terms that make up the query.
	 * @param r
	 *            boolean indicates whether the phrase is required.
	 */
	public PhraseScoreModifier(List<Query> pTerms, boolean r) {
		this(pTerms);
		required = r;
	}

	/**
	 * Constructs a phrase score modifier for a given set of query terms,
	 * whether they are required to appear in a document, and the allowed
	 * distance between the phrase terms.
	 * 
	 * @param pTerms
	 *            ArrayList the terms that make up the query.
	 * @param r
	 *            boolean indicates whether the phrase is required.
	 * @param bDist
	 *            int the allowed distance between the phrase terms.
	 */
	public PhraseScoreModifier(List<Query> pTerms, boolean r, int bDist) {
		this(pTerms, bDist);
		required = r;
	}

	/**
	 * Returns the name of the modifier.
	 * 
	 * @return String the name of the modifier.
	 */
	public String getName() {
		return "PhraseScoreModifier";
	}

	/** Clones this DSM. Note that phraseTerms is shallow copied, because Strings are immutable */
	public Object clone() {
		return (Object)new PhraseScoreModifier(new ArrayList<Query>(phraseTerms), required, blockDistance);
	}

	/**
	 * Modifies the scores of documents, in which there exist, or there does not
	 * exist a given phrase.
	 * 
	 * @param index
	 *            Index the data structures to use.
	 * @param terms
	 *            MatchingQueryTerms the terms to be matched for the query. This
	 *            does not correspond to the phrase terms necessarily, but to
	 *            all the terms of the query.
	 * @param set
	 *            ResultSet the result set for the query.
	 * @return true if any scores have been altered
	 */
	public boolean modifyScores(Index index, MatchingQueryTerms terms, ResultSet set) {

		//the number of documents, the scores of which were modified.
		int numOfModifiedDocumentScores = 0;

		//the number of terms in the phrase
		int phraseLength = phraseTerms.size();

		//get local references for the document ids and the
		//scores of documents from the result set.
		double[] scores = set.getScores();
		int[] docids = set.getDocids();

		//create a hashset with the document identifiers 
		//and their index for phrase each term. For example, 
		//if docidsMap[2].
		final TIntIntHashMap[] docidsMap = new TIntIntHashMap[phraseLength];
		
		final InvertedIndex invIndex = index.getInvertedIndex();
		
		int blockLengthIndex = -1;
		//BlockInvertedIndex invIndex = (BlockInvertedIndex)inv;
		int[][][] postings = new int[phraseLength][][];
		for (int i = 0; i < phraseLength; i++) {
			docidsMap[i] = new TIntIntHashMap();
			String t = ((SingleTermQuery) phraseTerms.get(i)).getTerm();
			if (terms.getStatistics(t) == null)
			{
				LexiconEntry le = index.getLexicon().getLexiconEntry(t);
				if (le == null)
					continue;
				terms.setTermProperty(t, le);
			}

			
			//for each phrase term, we store the identifiers of
			//documents that contain that term in a hashmap
			//we also convert the block frequencies into
			//indexes for the block ids array, so that we
			//can obtain easily the block ids of a phrase
			//term for each document.
			//
			//For j-th document in the postings lists postings[i]
			//the positions start at postings[i][4][postings[i][3][j-1]]
			//and end at postings[i][4][postings[i][3][j]-1]
			postings[i] = invIndex.getDocuments((LexiconEntry)terms.getStatistics(t));
			blockLengthIndex = postings[i].length - 2;
			
			for (int j = 0; j < postings[i][0].length; j++) {
				//note that the entries in the docidsMap hash sets have
				//been increased by one
				docidsMap[i].put(postings[i][0][j] + 1, j + 1);
				if (j > 0)
					postings[i][blockLengthIndex][j] += postings[i][blockLengthIndex][j - 1];
			}
		}
		try {
			int resultSetSize = set.getResultSize();
			boolean containsAllTerms = true;
			for (int i = 0; i < resultSetSize; i++) { //foreach document
				//check whether i-th document
				//contains all the query terms
				containsAllTerms = true;
				for (int j = 0; j < phraseLength; j++) {
					if (docidsMap[j].get(docids[i] + 1) == 0) {
						containsAllTerms = false;
						break;
					}
				}

				//if i-th document contains all query terms,
				//then check for whether the terms appear as a phrase
				if (containsAllTerms) {
					//the minimum number of blocks that a phrase term
					//appears in the document.
					int minBlocks = Integer.MAX_VALUE;

					//the index of the phrase term with the minimum
					//number of positions, or blocks.
					int indexMinBlocks = 0;

					//get the position arrays for all query terms
					//and find the shortest one.
					//P[][0] beginning of positions
					//P[][1] end of positions
					//P[][2] number of positions
					int[][] P = new int[phraseLength][3];

					//the positions is just a copy of the positions array
					int[][] positions = new int[phraseLength][];
					for (int j = 0; j < phraseLength; j++) {
						int postingIndex = Arrays.binarySearch(postings[j][0],docids[i]);//docidsMap[j].get(docids[i]+1)-1;

						if (postingIndex == 0)
							P[j][0] = 0;
						else
							P[j][0] = postings[j][blockLengthIndex][postingIndex - 1];
						
						P[j][1] = postings[j][blockLengthIndex][postingIndex] - 1;
						P[j][2] = P[j][1] - P[j][0] + 1;
						
						positions[j] = new int[P[j][2]];
						System.arraycopy(postings[j][blockLengthIndex+1], P[j][0], positions[j], 0, P[j][2]);

						if (Math.min(minBlocks, P[j][2]) != minBlocks) {
							minBlocks = P[j][2];
							indexMinBlocks = j;
						}
					}

					//now we check whether the positions correspond to a phrase
					int s = indexMinBlocks;
					boolean foundPhrase = true;
					//TIntArrayList matchingPositions = new TIntArrayList();
					for (int p = 0; p < positions[s].length; p++) {
						boolean foundPhraseForThisPosition = true; //int foundPhraseAt = -1;
						for (int j = 0; j < phraseLength; j++) {
							if (j == s) 	//we don't check for 
								continue;	//the phrase term with index s
		
							//PROXIMITY search notes:
							//if binarySearchResult is positive, this means
							//that the expected block id was found. If the
							//binarySearchResult is negative, then the 
							//expected block id was not found and the 
							//binarySearchResult is equal to (quoting
							// from Java's API documentation for class 
							//Arrays): (-(insertion point) - 1). The 
							//insertion point is defined as the point at
							//which the key would be inserted into the 
							//list: the index of the first element greater 
							//than the key, or list.size(), if all elements
							//in the list are less than the specified key.
							//
							//if binarySearchResult is negative, we check
							//other blocks if proximity is enabled, or
							//we check whether the terms were found in
							//the same block, if blockDistance is equal
							//to 1, but block.size is greater than 1.

							if (BLOCK_SIZE == 1 && blockDistance == 1) {
								int binarySearchResult = Arrays.binarySearch(positions[j],(positions[s][p] - s + j));
								if (binarySearchResult < 0) {
									foundPhraseForThisPosition = false;
									break;
								}
								//if (binarySearchResult > -1)
									//foundPhraseAt = positions[j][p];
							} else {
								int distance = Math.max(blockDistance,phraseTerms.size()/BLOCK_SIZE);

								if (positions[j].length == 1) {
									if ( (positions[j][0] > (positions[s][p] - s + distance + 1)) 
									   ||(positions[j][0] < (positions[s][p] - s - distance - 1))) {
										foundPhraseForThisPosition = false;
										break;
									}
								} else {
									int[] binarySearchResults = range(
											positions[j], positions[s][p] - s
													- distance, positions[s][p]
													- s + distance);

									if (binarySearchResults[0] == -1
											&& binarySearchResults[1] == positions[j].length) {
										foundPhraseForThisPosition = false;
										break;
									}
								}
							}
						}
						if (foundPhraseForThisPosition) {
							/*
							if (foundPhraseAt != -1)
							{
								matchingPositions.add(foundPhraseAt);
								//System.setProperty("phrasefound.docid."+docids[i]+".at",""+foundPhraseAt);
								//System.err.println("phrasefound.docid."+docids[i]+".at"+foundPhraseAt);
							}*/
							foundPhrase = true;
							break;
						} else
							foundPhrase = false;
					}
					if (foundPhrase) {
						if (!required) {
							if (scores[i] > Double.NEGATIVE_INFINITY)
								numOfModifiedDocumentScores++;
							scores[i] = Double.NEGATIVE_INFINITY;
						}
						/*
						else
						{
							//System.err.println("phrasefound.docid."+docids[i]+".count"+matchingPositions.size());
							System.setProperty("phrasefound.docid."+docids[i]+".count",""+matchingPositions.size());
							final int[] termids = matchingPositions.toNativeArray();
							StringBuffer a = new StringBuffer();
							for(int z=0;z<termids.length;z++)
							{
								a.append(termids[z]);
								if (z<termids.length-1) a.append(',');
							}
							System.setProperty("phrasefound.docid."+docids[i]+".at", a.toString());
						}*/
					} else {
						if (required) {
							if (scores[i] > Double.NEGATIVE_INFINITY)
								numOfModifiedDocumentScores++;
							scores[i] = Double.NEGATIVE_INFINITY;
						}
					}
				} else { //document does not contain all query terms
					if (required) {
						if (scores[i] > Double.NEGATIVE_INFINITY)
							numOfModifiedDocumentScores++;
						scores[i] = Double.NEGATIVE_INFINITY;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		if (numOfModifiedDocumentScores == 0)
			return false;
		set.setResultSize(set.getResultSize() - numOfModifiedDocumentScores);
		return true;
	}

	/**
	 * Performs a binary search in an array and returns the indices of the array
	 * for which the elements of the array are higher and lower than the given
	 * floor and ceiling. This method is based on code from
	 * http://www.tbray.org/ongoing/org/tbray/ongoing/BinarySearch.java.
	 * Corrected for binary search bug: http://googleresearch.blogspot.com/2006/06/extra-extra-read-all-about-it-nearly.html 
	 * @param array
	 *            the array to search in
	 * @param floor
	 *            the lower limit of the range we want to check for.
	 * @param ceiling
	 *            the upper limit of the range we want to check for.
	 * @return int[] an array of two integers. The first integer corresponds to
	 *         the index of the element of the array, which is lower than the
	 *         floor, and the second integer corresponds to index of the element
	 *         of the array, which is higher than the ceiling.
	 */
	protected int[] range(int[] array, int floor, int ceiling) {
		int[] answer = new int[2];
		int high, low, probe;

		// work on floor
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = low + ((high - low) / 2);// (high + low) / 2;
			if (array[probe] < floor)
				low = probe;
			else
				high = probe;
		}
		answer[0] = low;

		// work on ceiling
		high = array.length;
		low = -1;
		while (high - low > 1) {
			probe = low + ((high - low) / 2); //(high + low) / 2;
			if (array[probe] > ceiling)
				high = probe;
			else
				low = probe;
		}
		answer[1] = high;
		return answer;
	}
}
