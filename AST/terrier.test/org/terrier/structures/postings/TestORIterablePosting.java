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
 * The Original Code is TestORIterablePosting.java.
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures.postings;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestORIterablePosting
{
	protected IterablePosting joinPostings(IterablePosting[] ips) throws Exception
	{
		return new ORIterablePosting(ips);
	}
	
	@Test public void testSingleNoOverlap() throws Exception
	{
		IterablePosting[] ips = new IterablePosting[]{
				new ArrayOfBasicIterablePosting(new int[]{0}, new int[]{1}),
				new ArrayOfBasicIterablePosting(new int[]{1}, new int[]{2}),
		};
		//for(IterablePosting ip : ips)
		//	ip.next();
		IterablePosting joined = joinPostings(ips);
		assertEquals(0, joined.next());
		assertEquals(0, joined.getId());
		assertEquals(1, joined.getFrequency());
		assertEquals(1, joined.next());
		assertEquals(1, joined.getId());
		assertEquals(2, joined.getFrequency());
		assertEquals(IterablePosting.EOL, joined.next());			
	}
	
	@Test public void testTwoWithOverlap() throws Exception
	{
		IterablePosting[] ips = new IterablePosting[]{
				new ArrayOfBasicIterablePosting(new int[]{0,1}, new int[]{1,1}),
				new ArrayOfBasicIterablePosting(new int[]{0,1}, new int[]{2,2}),
		};
//		for(IterablePosting ip : ips)
//			ip.next();
		IterablePosting joined = joinPostings(ips);
		assertEquals(0, joined.next());
		assertEquals(0, joined.getId());
		assertEquals(3, joined.getFrequency());
		assertEquals(1, joined.next());
		assertEquals(1, joined.getId());
		assertEquals(3, joined.getFrequency());
		assertEquals(IterablePosting.EOL, joined.next());			
	}
	
	@Test public void testManySomeOverlap() throws Exception
	{
		IterablePosting[] ips = new IterablePosting[]{
				new ArrayOfBasicIterablePosting(new int[]{0,1}, new int[]{1,1}),
				new ArrayOfBasicIterablePosting(new int[]{1,2}, new int[]{2,2}),
		};
//		for(IterablePosting ip : ips)
//			ip.next();
		IterablePosting joined = joinPostings(ips);
		assertEquals(0, joined.next());
		assertEquals(0, joined.getId());
		assertEquals(1, joined.getFrequency());
		assertEquals(1, joined.next());
		assertEquals(1, joined.getId());
		assertEquals(3, joined.getFrequency());
		assertEquals(2, joined.next());
		assertEquals(2, joined.getId());
		assertEquals(2, joined.getFrequency());
		assertEquals(IterablePosting.EOL, joined.next());			
	}
	
}
