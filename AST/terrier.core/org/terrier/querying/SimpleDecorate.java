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
 * The Original Code is SimpleDecorate.java
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.querying;

import org.apache.log4j.Logger;
import org.terrier.matching.ResultSet;
import org.terrier.structures.MetaIndex;

/** A simple decorator, which applies all metadata in the MetaIndex to each document on the way out. */
public class SimpleDecorate implements PostFilter {

	static Logger logger = Logger.getLogger(SimpleDecorate.class);

	MetaIndex meta = null;
	String[] decorateKeys = null;
	/** 
	 * {@inheritDoc} 
	 */
	public final byte filter(
			Manager m, SearchRequest srq, ResultSet results,
			int DocAtNumber, int DocNo) 
	{
		try{
			final String[] values = meta.getItems(decorateKeys, DocNo);
			for(int j=0;j<decorateKeys.length;j++)
				results.addMetaItem(decorateKeys[j], DocAtNumber, values[j]);
			return PostFilter.FILTER_OK;
		} catch (Exception e) {
			return PostFilter.FILTER_REMOVE;
		}
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void new_query(Manager m, SearchRequest srq, ResultSet rs) 
	{
		meta = ((Request)srq).getIndex().getMetaIndex();
		decorateKeys = meta.getKeys();
	}

}
