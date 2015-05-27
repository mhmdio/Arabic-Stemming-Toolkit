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
 * The Original Code is PostingUtil.java
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.postings;

import gnu.trove.TIntArrayList;

import java.io.IOException;
/** Handy methods for Posting classes, such as obtaining all ids in a
 * posting list, or selecting the minimum id in an array of posting lists. 
 * @author Craig Macdonald
 * @since 3.0
 */
public class PostingUtil {

	/** Get an array of all the ids in a given IterablePosting stream */
	public static int[] getIds(final IterablePosting ip) throws IOException
	{
		final TIntArrayList ids = new TIntArrayList();
		while(ip.next() != IterablePosting.EOL)
			ids.add(ip.getId());
		return ids.toNativeArray();
	}
	
	/** Get an array of all the ids in a given IterablePosting stream, 
	 * where the length of the stream is known */
	public static int[] getIds(final IterablePosting ip, final int numPointers) throws IOException
	{
		final int[] ids = new int[numPointers];
		for(int i=0;ip.next() != IterablePosting.EOL;i++)
			ids[i] = ip.getId();
		return ids;
	}
	
	/** Returns the minimum docid of the current postings in the array of IterablePostings
	 * @return minimum docid, or -1 if all postings have ended. */
	public static int selectMinimumDocId(final IterablePosting postingListArray[])
	{
		int docid = Integer.MAX_VALUE;
		for (IterablePosting postingList: postingListArray)
			if (!postingList.endOfPostings() && docid > postingList.getId()) 
				docid = postingList.getId();
		if (docid == Integer.MAX_VALUE)
			docid = -1;
		return docid;
	}
	
}
