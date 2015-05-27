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
 * The Original Code is FieldORIterablePosting.java.
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures.postings;

import java.io.IOException;

/** ORIterablePosting implementation that implements FieldPosting
 * @since 3.5
 * @author Craig Macdonald
 */
public class FieldORIterablePosting extends ORIterablePosting implements
		FieldPosting {
	
	final int[] fieldFreqs;
	final int fieldCount;
//	int[] fieldLens;
	FieldPosting current;
	/**
	 * Constructs an instance of FieldORIterablePosting.
	 * @param ips
	 * @throws IOException
	 */
	public FieldORIterablePosting(IterablePosting[] ips) throws IOException {
		super(ips);
		fieldCount = ((FieldPosting)ips[0]).getFieldFrequencies().length;
		fieldFreqs = new int[fieldCount];
	}

	@Override
	public int[] getFieldFrequencies() {
		return fieldFreqs;
	}

	@Override
	public int[] getFieldLengths() {
		return current.getFieldLengths();
		//return fieldLens;
	}

	@Override
	protected void addPosting(Posting _p) {
		super.addPosting(_p);
		FieldPosting p = (FieldPosting)_p;
		final int[] thisPostingFieldFreqs = p.getFieldFrequencies();
		for(int fi=0;fi<fieldCount;fi++)
			fieldFreqs[fi] += thisPostingFieldFreqs[fi];
	}

	@Override
	protected void firstPosting(Posting _p) {
		super.firstPosting(_p);
		FieldPosting p = current = (FieldPosting)_p;
		//fieldLens = p.getFieldLengths();
		final int[] thisPostingFieldFreqs = p.getFieldFrequencies();
		for(int fi=0;fi<fieldCount;fi++)
			fieldFreqs[fi] = thisPostingFieldFreqs[fi];
	}

	@Override
	public WritablePosting asWritablePosting() {
		return new FieldPostingImpl(this.getId(), this.getFrequency(), this.getFieldFrequencies());
	}


}
