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
 * The Original Code is BlockFieldLexiconMap.java
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.indexing;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntProcedure;

import java.io.IOException;
import java.util.Arrays;

import org.terrier.structures.BlockFieldLexiconEntry;
import org.terrier.structures.LexiconOutputStream;
import org.terrier.utility.TermCodes;
/** BlockFieldLexiconMap class */
public class BlockFieldLexiconMap extends BlockLexiconMap {
	protected final int fieldCount;
	protected final TObjectIntHashMap<String>field_tfs[]; 
	/**
	 * Constructs an instance of the BlockFieldLexiconMap.
	 * @param _fieldCount
	 */
	@SuppressWarnings("unchecked")
	public BlockFieldLexiconMap(int _fieldCount)
	{
		super();
		fieldCount = _fieldCount;
		field_tfs = new TObjectIntHashMap[fieldCount];
		for(int fi=0;fi<fieldCount;fi++)
			field_tfs[fi] = new TObjectIntHashMap<String>(BUNDLE_AVG_UNIQUE_TERMS);
	}
	
	protected int[] getFieldFrequency(String term)
	{
		int[] fieldFrequencies = new int[fieldCount];
		for(int fi=0;fi<fieldCount;fi++)
			fieldFrequencies[fi] = field_tfs[fi].get(term);
		return fieldFrequencies;
	}
	
	/** Inserts all the terms from a document posting
	  * into the lexicon map
	  * @param _doc The postinglist for that document. Assumed to be of type BlockFieldDocumentPostingList.
	  */
	public void insert(DocumentPostingList _doc)
	{
		super.insert(_doc);
		BlockFieldDocumentPostingList doc = (BlockFieldDocumentPostingList)_doc;
		int fi = 0;
		for(TObjectIntHashMap<String> docField : doc.field_occurrences)
		{
			final TObjectIntHashMap<String> thisField = field_tfs[fi];
			docField.forEachEntry(new TObjectIntProcedure<String>() {
				public boolean execute(String arg0, int arg1) {
					thisField.adjustOrPutValue(arg0, arg1, arg1);
					return true;
				}
			});
			fi++;
		}
	}
	
	/** Stores the lexicon tree to a lexicon stream as a sequence of entries.
	  * The binary tree is traversed in order, by called the method
	  * traverseAndStoreToStream.
	  * @param lexiconStream The lexicon output stream to store to. */
	public void storeToStream(LexiconOutputStream<String> lexiconStream) throws IOException
	{
		final String[] terms = tfs.keys(new String[0]);
		Arrays.sort(terms);
		for (String t : terms)
		{
			BlockFieldLexiconEntry fle = new BlockFieldLexiconEntry(getFieldFrequency(t), blockFreqs.get(t));
			fle.setOffset((long)0, (byte)0);
			fle.setTermId(TermCodes.getCode(t));
			fle.setStatistics(nts.get(t), tfs.get(t));	
			lexiconStream.writeNextEntry(t, fle);
		}
	}

	@Override
	public void clear() {
		super.clear();
		for(int fi=0;fi<fieldCount;fi++)
			field_tfs[fi].clear();
	}
}
