/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is SimpleJettyHTTPServer.java
 *
 * The Original Code is Copyright (C) 2004-2010 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 *   Ben He <ben{a.}dcs.gla.ac.uk>
 */
package org.terrier.querying;

import gnu.trove.TObjectIntHashMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.terrier.matching.ResultSet;
import org.terrier.structures.MetaIndex;
import org.terrier.structures.collections.LRUMap;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.StringTools;
import org.terrier.utility.StringTools.ESCAPE;

/** This class decorates a result set with metadata. This metadata can be highlighted, 
 * can have a query biased summary created, and
 * also be escaped for display in another format.
 * <b>Controls:</b>
 * &lt;ul&gt;
 * <li><tt>summaries</tt> - comma or semicolon delimited list of the key names for which a query biased summary should be created. e.g. summaries:snippet</li>
 * <li><tt>emphasis</tt> - comma or semicolon delimited list of they key names that should have boldened for occurrences of the query terms. e.g. emphasis:title;snippet</li>
 * <li><tt>earlyDecorate</tt> - comma or semicolon delimited list of the key names that should be decorated early, e.g. to support another PostProcess using them.</li>
 * <li><tt>escape</tt> - comma or semicolon delimited list of the key names that should be escaped e.g. escape:title;snippet;url</li>. Currently, per-key type escaping 
 * is not supported. The default escape type is defined using the property decorate.escape.
 * &lt;/li&gt;
 * <b>Properties:</b>
 * <ul>
 * <li><tt>decorate.escape</tt> - default escape type for metadata. Default is html. See StringTools.ESCAPE for more information.
 * </ul>
 * 
 * @author Craig Macdonald, Vassilis Plachouras, Ben He
 * @since 3.0
 */
public class Decorate implements PostProcess, PostFilter {

	/** delimiters for breaking down the values of controls further */
	protected static final String[] CONTROL_VALUE_DELIMS = new String[]{";", ","};

	/** Logging error messages */
	private static Logger logger = Logger.getLogger(Decorate.class);
		
	/** The cache used for the meta data. Implements a 
	 * Least-Recently-Used policy for retaining the most 
	 * recently accessed metadata. */ 
	protected LRUMap<Integer,String[]> metaCache = null;
	
	/** The meta index server. It is provided by the manager. */
	protected MetaIndex metaIndex = null;
	
	//a regular expression that detects the existence of a 
	//control character or a non-visible character in a string
	protected static final Pattern controlNonVisibleCharacters = Pattern.compile("[\\p{Cntrl}\uFFFD]|[^\\p{Graph}\\p{Space}]");
	
	/** what is the default escape sequence */
	protected static final ESCAPE defaultEscape = ESCAPE.parse(ApplicationSetup.getProperty("decorate.escape", "html"));
	
	//the matcher that corresponds to the above regular expression, initialised
	//for an empty string. This variable is defined in order to avoid creating
	//a new object every time it is required to check for and remove control characters, or non-visible characters.
	protected Matcher controlNonVisibleCharactersMatcher = controlNonVisibleCharacters.matcher("");
	
	/** highlighting pattern for the current query */
	protected Pattern highlight;
	
	/** query terms of the current query */
	protected String[] qTerms;
	
	//the metadata keys
	private TObjectIntHashMap<String> keys = new TObjectIntHashMap<String>();
	//the keys which should be summarised
	private Set<String> summaryKeys = new HashSet<String>();
	//keys which should be emphasised
	private Set<String> emphasisKeys = new HashSet<String>();
	//keys which should be escaped
	private Map<String, ESCAPE> escapeKeys = new HashMap<String,ESCAPE>();
	//keys which should be decorated at PostProcess rather than filter
	private Set<String> earlyKeys = new HashSet<String>();
	/** 
	 * {@inheritDoc} 
	 */
	@SuppressWarnings("unchecked")
	public void new_query(Manager m, SearchRequest q, ResultSet rs)
	{
		metaIndex = m.getIndex().getMetaIndex();
		int i=0;
		for(String k : metaIndex.getKeys())
		{
			keys.put(k,i++);
		}
		
		for(String summarykey : ArrayUtils.parseDelimitedString(q.getControl("summaries"),CONTROL_VALUE_DELIMS))
		{
			summaryKeys.add(summarykey);
		}
		for(String emphKey : ArrayUtils.parseDelimitedString(q.getControl("emphasis"),CONTROL_VALUE_DELIMS))
		{
			emphasisKeys.add(emphKey);
		}
		for(String earlyKey : ArrayUtils.parseDelimitedString(q.getControl("earlyDecorate"),CONTROL_VALUE_DELIMS))
		{
			earlyKeys.add(earlyKey);
		}
		for(String escapeKey : ArrayUtils.parseDelimitedString(q.getControl("escape"),CONTROL_VALUE_DELIMS))
		{
			escapeKeys.put(escapeKey, defaultEscape);
		}
		
		if (m.getIndex().hasIndexStructure("metacache"))
			metaCache = (LRUMap<Integer,String[]>) m.getIndex().getIndexStructure("metacache");
		else
			metaCache = new LRUMap<Integer,String[]>(1000);

		//preparing the query terms for highlighting
		String original_q = q.getOriginalQuery();
		if (original_q == null)
		{
			return;
		}
		highlight = generateEmphasisPattern(original_q.trim().toLowerCase().split("\\s+"));
	}
	
	/** 
	 * {@inheritDoc} 
	 */
	//decoration at the postfilter stage
	public byte filter(Manager m, SearchRequest q, ResultSet rs, int rank, int docid)
	{		
		String[] _qTerms = q.getOriginalQuery().replaceAll(" \\w+\\p{Punct}\\w+ "," ").toLowerCase().split(" ");
		String[] metaKeys = keys.keys(new String[keys.size()]);
		String[] metadata = getMetadata(metaKeys, docid);
		
		int keyID = 0;
		for(String key : metaKeys)
		{
			//get the desired metdata value
			String value = metadata[keyID];
			//is it a snippet? if so, do create query biassed summary
			if (summaryKeys.contains(key))
			{
				value = generateQueryBiasedSummary(value, _qTerms);
			}
			//do some cleaning of the snippet
			controlNonVisibleCharactersMatcher.reset(value);
			value = controlNonVisibleCharactersMatcher.replaceAll("");
			//is escaping needed?
			StringTools.ESCAPE e = escapeKeys.get(key);
			if (e != null)
			{
				value = StringTools.escape(e, value);
			}
			//add to the result set
			rs.addMetaItem(key, rank, value);
			
			//should it be highlighted?
			if (emphasisKeys.contains(key))
			{
				String value_highlight = highlight.matcher(value).replaceAll("$1<b>$2</b>$3");
				rs.addMetaItem(key+ "_emph", rank, value_highlight);
			}
			keyID++;
		}
		return FILTER_OK;
	}

	/** decoration at the postprocess stage. only decorate if required for future postfilter or postprocesses.
	  * @param manager The manager instance handling this search session.
	  * @param q the current query being processed
	  */
	public void process(Manager manager, SearchRequest q)
	{
		ResultSet rs = q.getResultSet();
		new_query(manager, q, rs);
		if (earlyKeys.size() == 0)
			return;
		
		
		int docids[] = rs.getDocids();
		int resultsetsize = docids.length;
		logger.info("Early decorating resultset with metadata for " + resultsetsize + " documents");
		
		String[] earlykeys = earlyKeys.toArray(new String[earlyKeys.size()]);
		String[] allKeys = keys.keys(new String[keys.size()]);
		String[][] metadata = getMetadata(allKeys, docids);
		for(String k : earlykeys)
		{
			rs.addMetaItems(k, metadata[keys.get(k)]);
		}
	}
	
	protected String[] getMetadata(String[] metaKeys, int docid)
	{
		String[] metadata = null;
		synchronized(metaCache) {
			try {
				Integer docidObject = Integer.valueOf(docid);
				if (metaCache.containsKey(docidObject))
						metadata = metaCache.get(docidObject);
				else {
					metadata = metaIndex.getItems(metaKeys, docid);
					metaCache.put(docidObject,metadata);
				}
			} catch(IOException ioe) {
				logger.error("Problem getting metadata for docid " + docid);
			} 
		}
		return metadata;
	}
	
	protected String[][] getMetadata(String[] metaKeys, int[] docids)
	{
		String[][] metadata = null;
		try{
			metadata = metaIndex.getItems(metaKeys, docids);
			synchronized (metaCache) {
				for(int i=0;i<docids.length;i++)
				{
					metaCache.put(Integer.valueOf(docids[i]), metadata[i]);
				}
			}
		} catch (IOException ioe) {
			logger.error("Problem getting metadata for " + docids.length + " documents");
		}
		return metadata;
	}
	
	//the regular expression for splitting the text into sentences
	private static Pattern sentencePattern = Pattern.compile("\\.\\s+|!\\s+|\\|\\s+|\\?\\s+");
	
	//the regular expression for removing common endings from words - similar to very light stemming
	private static Pattern removeEndings = Pattern.compile("ing$|es$|s$");
	
	protected String generateQueryBiasedSummary(String extract, String[] _qTerms)
	{
		int tmpSentence;
		double tmpScore;
		String[] sentences = sentencePattern.split(extract, 50); //use 50 sentences at most
		double[] sentenceScores = new double[sentences.length]; 
		int frsSentence = -1;
		int sndSentence = -1;
		int top1Sentence = 0;
		int top2Sentence = 0;
		double max1Score = -1;
		double max2Score = 0;
		final int qTermsLength = _qTerms.length;
		for (int i=0; i<qTermsLength; i++) {
			_qTerms[i] = removeEndings.matcher(_qTerms[i]).replaceAll("");
		}
		String lowerCaseSentence;
		int sentenceLength;
		final int sentencesLength = sentences.length;

		for (int i=0; i<sentencesLength; i++) {
			
			lowerCaseSentence = sentences[i].toLowerCase();
			sentenceLength=sentences[i].length();
			if (sentenceLength < 20 || sentenceLength > 250) {
				for (int j=0; j<qTermsLength; j++) {
					if (lowerCaseSentence.indexOf(_qTerms[j])>=0) {
						sentenceScores[i]+=1.0d + sentenceLength / (20.0d + sentenceLength);
					}
				}

				
			} else {
				for (int j=0; j<qTermsLength; j++) {
					if (lowerCaseSentence.indexOf(_qTerms[j])>=0) {
						sentenceScores[i]+=_qTerms[j].length() + sentenceLength / (1.0d + sentenceLength);
					}
				}					
			}
							
			//do your best to get at least a second sentence for the snippet, 
			//after having found the first one
			if (frsSentence > -1 && sndSentence == -1 && sentenceLength > 5) {
				sndSentence = i;
			}

			//do your best to get at least one sentence for the snippet
			if (frsSentence == -1 && sentenceLength > 5) { 
				frsSentence = i;
			}

			if (max2Score < sentenceScores[i]) {
				max2Score = sentenceScores[i];
				top2Sentence = i;
				//logger.debug("top 2 sentence is " + i);
				if (max2Score > max1Score) {
					tmpScore = max1Score; max1Score = max2Score; max2Score = tmpScore;
					tmpSentence = top1Sentence; top1Sentence = top2Sentence; top2Sentence = tmpSentence;
				}
			}

		}
		int lastIndexOfSpace = -1;
		String sentence="";
		String secondSentence="";
		String snippet = "";
		if (max1Score == -1) {
			if (frsSentence>=0) {
				sentence = sentences[frsSentence];
				if (sentence.length() > 100) {
					lastIndexOfSpace = sentence.substring(0, 100).lastIndexOf(" ");
					sentence = sentence.substring(0, lastIndexOfSpace > 0 ? lastIndexOfSpace : 100);
				}
			}
			
			if (sndSentence>=0) {
				secondSentence = sentences[sndSentence];
				if (secondSentence.length() > 100) {
					lastIndexOfSpace = secondSentence.substring(0, 100).lastIndexOf(" ");
					secondSentence = secondSentence.substring(0, lastIndexOfSpace>0 ? lastIndexOfSpace : 100);
				}					
			}
			
			if (frsSentence >=0 && sndSentence >= 0) 
				snippet = sentence.trim() + "..." + secondSentence.trim();
			else if (frsSentence >= 0 && sndSentence<0) 
				snippet = sentence.trim();
			
		} else if (sentences[top1Sentence].length()<100 && top1Sentence!=top2Sentence) {
			sentence = sentences[top1Sentence];
			if (sentence.length() > 100) {
				lastIndexOfSpace = sentence.substring(0, 100).lastIndexOf(" ");
				sentence = sentence.substring(0, lastIndexOfSpace > 0 ? lastIndexOfSpace : 100);
			}
							
			secondSentence = sentences[top2Sentence];
			if (secondSentence.length() > 100) {
				lastIndexOfSpace = secondSentence.substring(0, 100).lastIndexOf(" ");
				secondSentence = secondSentence.substring(0, lastIndexOfSpace>0 ? lastIndexOfSpace : 100);
			}
			snippet = sentence.trim() + "..." + secondSentence.trim();
		} else {
			sentence = sentences[top1Sentence];
			if (sentence.length()>200) {
				lastIndexOfSpace = sentence.substring(0, 200).lastIndexOf(" ");
				sentence = sentence.substring(0, lastIndexOfSpace > 0 ? lastIndexOfSpace : 200);
			}
			snippet = sentence.trim();
		}
		return snippet;
	}
	
	
	/** Creates a regular expression pattern to highlight query terms metadata.
	 * @param _qTerms query terms
	 * @return Pattern to apply
	 */
	protected Pattern generateEmphasisPattern(String[] _qTerms) {
		boolean atLeastOneTermToHighlight = false;
		StringBuilder pattern = new StringBuilder();
		if (_qTerms.length>0 ) {
				pattern.append("(\\b)(");
				if (!_qTerms[0].contains(":")) {
						String qTerm = _qTerms[0].replaceAll("\\W+", "");
						pattern.append(qTerm);
						atLeastOneTermToHighlight = true;
				} else if (!(_qTerms[0].startsWith("group:") || _qTerms[0].startsWith("related:"))) {
						String qTerm = _qTerms[0].substring(_qTerms[0].indexOf(':')+1).replaceAll("\\W+","");
						pattern.append(qTerm);
						atLeastOneTermToHighlight = true;
				}
		}

		for (int i=1; i<_qTerms.length; i++) {
				if (!_qTerms[i].contains(":")) {
						String qTerm = _qTerms[i].replaceAll("\\W+","");
						if (atLeastOneTermToHighlight) {
							pattern.append('|'); 
							pattern.append(qTerm);
						} else {
							pattern.append(qTerm);
						}
						atLeastOneTermToHighlight = true;
				} else if (!(_qTerms[i].startsWith("group:") || _qTerms[0].startsWith("related:"))) {
						String qTerm = _qTerms[i].substring(_qTerms[i].indexOf(':')+1).replaceAll("\\W+","");
						if (atLeastOneTermToHighlight) {
							pattern.append('|'); 
							pattern.append(qTerm);
						} else {
							pattern.append(qTerm);
						}
						atLeastOneTermToHighlight = true;
				}
		}

		if (_qTerms.length>0)
				pattern.append(")(\\b)");
		String _pattern = pattern.toString();
		if (!atLeastOneTermToHighlight) {
			_pattern = ("(\\b)()(\\b)");
		}
		return Pattern.compile(_pattern, Pattern.CASE_INSENSITIVE);
	}
	

	protected boolean checkControl(String control_name, SearchRequest srq)
	{
		String controlValue = srq.getControl(control_name);
		if (controlValue.length() == 0)
			return false;
		if (controlValue.equals("0") || controlValue.toLowerCase().equals("off")
			|| controlValue.toLowerCase().equals("false"))
			return false;
		return true;
	}
	
	/**
	 * Returns the name of the post processor.
	 * @return String the name of the post processor.
	 */
	public String getInfo()
	{
		return "Decorate";
	}
}
