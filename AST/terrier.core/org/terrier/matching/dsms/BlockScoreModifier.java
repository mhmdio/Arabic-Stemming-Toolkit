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
 * The Original Code is BlockScoreModifier.java.
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Douglas Johnson <johnsoda{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> 
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching.dsms;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.ResultSet;
import org.terrier.structures.BlockInvertedIndex;
import org.terrier.structures.Index;
import org.terrier.structures.InvertedIndex;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.utility.ApplicationSetup;
/**
 * This class modifers the scores of documents based on
 * the position of the query terms in the document.
 * This class implements the DocumentScoreModifier interface. 
 * @author Douglas Johnson, Vassilis Plachouras
  */
public class BlockScoreModifier implements DocumentScoreModifier {
	protected static final Logger logger = Logger.getLogger(BlockScoreModifier.class);
	/** {@inheritDoc}*/ @Override 
	public String getName() {
		return "BlockScoreModifier";
	}
	
	/**
	 * Modifies scores by applying proximity weighting.
	 */
	public boolean modifyScores(Index index, MatchingQueryTerms query, ResultSet resultSet) {
		// The rest of the method applies proximity weighting as outlined
		// by Yves Rasolofo for queries of 1 < length < 5.
		//TODO replace ApplicationSetup.BLOCK_QUERYING

		InvertedIndex invertedIndex = index.getInvertedIndex();
		if (invertedIndex instanceof BlockInvertedIndex && 
				query.length() > 1 && query.length() < 5) {
			
			Lexicon<String> lexicon = index.getLexicon();
			
			int[] docids = resultSet.getDocids();
			double[] scores = resultSet.getScores();
			
			
			//check when the application of proximity started.
			long proximityStart = System.currentTimeMillis();
			
			
			// the constants used by the algorithm
			double N = index.getCollectionStatistics().getNumberOfDocuments();
			int blockSize = ApplicationSetup.BLOCK_SIZE;
			//The okapi constants for use with the proximity algorithm
			double k = 2.0d;
			double k1 = 1.2d;
			double k3 = 1000d;
			double b = 0.9d;
			int topDocs = 100;
			double avdl =
				1.0D * index.getCollectionStatistics().getAverageDocumentLength();
			double K = k * ((1 - b) + (b * (1 / avdl)));
			// an array holding the proximity weight for each docid
			// corresponds to the scores array
			double[] TPRSV = new double[scores.length];
			//arrays to reference the first terms block information
			int[][] term1Pointers;
			int[] term1blockfreqs;
			int[] term1blockids;
			int[] term1docids;
			//int[] term1freqs;
			//term2Pointers holds the information for the second term of each pair
			//each of the other arrays are used to reduce the number of references
			int[][] term2Pointers;
			int[] term2docids;
			//int[] term2termfreqs;
			int[] term2blockfreqs;
			int[] term2blockids;
			// calculate all the possible combinations of query term pairs
			ArrayList<String[]> queryTermPairs = generateQueryTermPairs(query);
			//Iterator termPairIterator<ArrayList<String>> = queryTermPairs.iterator();
			// for all term pairs
			for (String[] queryTermPair : queryTermPairs)
			{
				final String term1 = queryTermPair[0];
				final String term2 = queryTermPair[1];
				
				//we seek the query term in the lexicon
				LexiconEntry tEntry1 = lexicon.getLexiconEntry(term1);
				if (tEntry1 == null)//and if it is not found, we continue with the next term pair
					continue;
				//double term1KeyFrequency = query.getTermWeight(term1);
				
				double term1DocumentFrequency = (double)tEntry1.getDocumentFrequency();
				
				//we seek the 2nd query term in the lexicon
				LexiconEntry tEntry2 = lexicon.getLexiconEntry(term2);
				//and if it is not found, we continue with the next term pair
				if (tEntry1 == null)
					continue;
				//double term2KeyFrequency = query.getTermWeight(term2);
				double term2DocumentFrequency = (double)tEntry2.getDocumentFrequency();
				term1Pointers = invertedIndex.getDocuments(tEntry1);
				
				term1docids = term1Pointers[0];
				term1blockfreqs = term1Pointers[2];
				term1blockids = term1Pointers[3];
				term2Pointers = invertedIndex.getDocuments(tEntry2);
				term2docids = term2Pointers[0];
				term2blockfreqs = term2Pointers[2];
				term2blockids = term2Pointers[3];
				int length1 = term1docids.length;
				int length2 = term2docids.length;
				// generate a set of docids containing only those which
				// are in the top scores, and contain both term1 and term2
				TreeSet<Integer>topdocidSet = new TreeSet<Integer>();
				for (int n = docids.length-1; n > 0 && n > topDocs; n--) {
					topdocidSet.add(docids[n]);
				}
				TreeSet<Integer> term1docidSet = new TreeSet<Integer>();
				for (int n = 0; n < term1docids.length; n++) {
					if (topdocidSet.contains(term1docids[n])) {
						term1docidSet.add(term1docids[n]);
					}
				}
				TreeSet<Integer> matchingSet = new TreeSet<Integer>();
				for (int n = 0; n < term2docids.length; n++) {
					if (term1docidSet.contains(term2docids[n])) {
						matchingSet.add(term2docids[n]);
					}
				}
				Iterator<Integer> docidIterator = matchingSet.iterator();
				int term1index = 0;
				int term2index = 0;
				int term1blockindex = 0;
				int term2blockindex = 0;
				// for all docids in the matching set
				while (docidIterator.hasNext()) {
					int docid = ((Integer) docidIterator.next()).intValue();
					int distance;
					double tpi = 0;
					// find the position of this docid
					while (term1index < length1
						&& docid != term1docids[term1index]) {
						term1blockindex += term1blockfreqs[term1index];
						term1index++;
					}
					// find the position of this docid
					while (term2index < length2
						&& docid != term2docids[term2index]) {
						term2blockindex += term2blockfreqs[term2index];
						term2index++;
					}
					//int term1freq = term1freqs[term1index];
					//int term2freq = term2termfreqs[term2index];
					int term1blockfreq = term1blockfreqs[term1index];
					int term2blockfreq = term2blockfreqs[term2index];
					// for each block containing term1 find the distance to each block
					// containing term2 and if the distance is within maximal distance of 5
					// add this to the tpi score for this document
					for (int blockidIndex = term1blockindex; blockidIndex < term1blockindex + term1blockfreq; blockidIndex++) {
						for (int blockidIndex2 = term2blockindex; blockidIndex2 < term2blockindex + term2blockfreq; blockidIndex2++) {
							distance =
								(blockSize
									* Math.abs(term1blockids[blockidIndex] - term2blockids[blockidIndex2]))
									+ 1;
							if (distance < 6) {
								tpi += 1 / Math.pow(distance, 2.0);
							}
						}
					}
					double w = (k1 + 1) * (tpi / (K + tpi));
					// the weight of this term pair
					// the equations for the weight of term i and j in the query are slightly different 
					// to those in the paper as we know the query has no duplicate terms so qtf = 1
					double qwi =
						Math.log(
							(N - term1DocumentFrequency)
								/ term1DocumentFrequency)
							/ k3;
					double qwj =
						Math.log(
							(N - term2DocumentFrequency)
								/ term2DocumentFrequency)
							/ k3;
					TPRSV[docid] += (w * Math.min(qwi, qwj));
					// add the value to the proximity score for this docid
				}
			}
			// for the top documents update their score
			for (int docidIndex = docids.length - 1;
				docidIndex > 0 && docidIndex > docids.length - topDocs;
				docidIndex--) {
				scores[docidIndex] += TPRSV[docids[docidIndex]];
			}
			long proximityEnd = System.currentTimeMillis();
			if(logger.isDebugEnabled()){
			logger.debug(
				"time to apply proximity and resort: "
					+ ((proximityEnd - proximityStart) / 1000.0D));
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Generates all possible query term pairs
	 * @param query the query to generate term pairs from
	 */
	protected ArrayList<String[]> generateQueryTermPairs(MatchingQueryTerms query) {
		ArrayList<String[]> queryTermPairs = new ArrayList<String[]>();
		String[] terms = query.getTerms();
		for (int i = 0; i < query.length() - 1; i++) {
			for (int j = 1; j < query.length(); j++) {
				queryTermPairs.add(new String[]{terms[i], terms[j]});
			}
		}
		return queryTermPairs;
	}
	/** {@inheritDoc}*/ @Override 
	public Object clone()
	{
			return this;
	}
}
