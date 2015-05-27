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
 * The Original Code is TestResultSets.java.
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.matching;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestResultSets {

	
	@Test public void testNormalResultSetExactSize()
	{
		ResultSet r = new QueryResultSet(10);
		r.setExactResultSize(1000);
		ResultSet r2 = r.getResultSet(0, 5);
		assertEquals(1000, r2.getExactResultSize());
	}
	
	@Test public void testAccumulatorResultSetExactSize()
	{
		AccumulatorResultSet r = new AccumulatorResultSet(10);
		r.scoresMap.adjustOrPutValue(5, 1.0d, 1.0d);
		r.occurrencesMap.adjustOrPutValue(5, (short)1, (short)1);
		r.initialise();
		r.setExactResultSize(1000);
		ResultSet r2 = r.getResultSet(0, 5);
		assertEquals(1000, r2.getExactResultSize());
	}
	
	@Test public void testCollectionResultSetExactSize()
	{
		CollectionResultSet r = new CollectionResultSet(10);
		r.initialise();
		r.setExactResultSize(1000);
		ResultSet r2 = r.getResultSet(0, 5);
		assertEquals(1000, r2.getExactResultSize());
	}
	
}
