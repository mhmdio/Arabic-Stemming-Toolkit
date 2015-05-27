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
 * The Original Code is BlockFieldPostingImpl.java
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.postings;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableUtils;
import org.terrier.utility.ArrayUtils;
/** 
 * A writable block field posting list 
 */
public class BlockFieldPostingImpl extends BlockPostingImpl implements FieldPosting {

	int[] fieldFrequencies;
	
	/**
	 * default constructor
	 */
	public BlockFieldPostingImpl() {}

	/**
	 * contructure
	 * @param docid
	 * @param frequency
	 * @param _positions
	 * @param _fieldFrequencies
	 */
	public BlockFieldPostingImpl(int docid, int frequency, int[] _positions, int[] _fieldFrequencies) {
		super(docid, frequency, _positions);
		fieldFrequencies = new int[_fieldFrequencies.length];
		System.arraycopy(_fieldFrequencies, 0, fieldFrequencies, 0, _fieldFrequencies.length);
	}
	/**
	 * constructor
	 * @param docid
	 * @param frequency
	 * @param _positions
	 * @param fieldCount
	 */
	public BlockFieldPostingImpl(int docid, int frequency, int[] _positions, int fieldCount) {
		super(docid, frequency, _positions);
		fieldFrequencies = new int[fieldCount]; 
	}

	/** {@inheritDoc} */
	public int[] getFieldFrequencies() {
		return fieldFrequencies;
	}

	/** {@inheritDoc} */
	public int[] getFieldLengths() {
		return null;
	}
	
	@Override
	public void readFields(DataInput in) throws IOException {
		super.readFields(in);
		final int l = WritableUtils.readVInt(in);
		fieldFrequencies = new int[l];
		for(int i=0;i<l;i++)
			fieldFrequencies[i] = WritableUtils.readVInt(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		super.write(out);
		WritableUtils.writeVInt(out, fieldFrequencies.length);
		for(int field_f : fieldFrequencies)
			WritableUtils.writeVInt(out, field_f);
	}

	@Override
	public WritablePosting asWritablePosting()
	{	
		int[] newPos = new int[positions.length];
		System.arraycopy(positions, 0, newPos, 0, positions.length);
		BlockFieldPostingImpl fbp = new BlockFieldPostingImpl(id, tf, newPos, fieldFrequencies.length);
		fbp.id = id;
		fbp.tf = tf;
		System.arraycopy(fieldFrequencies, 0, fbp.fieldFrequencies, 0, fieldFrequencies.length);
		return fbp;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public String toString()
	{
		return "(" + id + "," + tf + ",F[" + ArrayUtils.join(fieldFrequencies, ",")
			+ ",B[" + ArrayUtils.join(positions, ",") + "])";
	}
	
}
