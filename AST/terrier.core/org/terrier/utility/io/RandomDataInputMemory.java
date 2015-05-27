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
 * The Original Code is RandomDataInputMemory.java
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.utility.io;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.terrier.utility.Files;

/** Implements a RandomDataInput backed by a byte[] rather than a file.
 * @since 3.0
 * @author Craig Macdonald 
 */
public class RandomDataInputMemory extends DataInputStream implements RandomDataInput, Cloneable  {
	
	static int MAX_INDIVIDUAL_BUFFER_SIZE = Integer.MAX_VALUE;
	// 1024 * 1024; //
	static interface Seekable extends Closeable
	{
		void seek(long _pos);		
		long getFilePointer();				
		long length();
	}
	
	/** seekable implementation which uses multiple byte arrays */
	private static class MultiSeeakableByteArrayInputStream extends InputStream implements Seekable
	{				
		long pos = 0;
		long[] endOffsets;
		byte[][] data;
		
		public MultiSeeakableByteArrayInputStream(byte[][] _data) 
		{
			super();
			data = _data;
			int parts = _data.length;
			endOffsets = new long[parts];
			long offset = 0;
			for(int i=0;i<parts;i++)
			{
				offset += data[i].length;
				endOffsets[i] = offset;
			}
		}
		
		protected MultiSeeakableByteArrayInputStream(byte[][] _data, long[] _endOffsets)
		{
			pos = 0;
			data = _data;
			endOffsets = _endOffsets;
		}
		
		public MultiSeeakableByteArrayInputStream(DataInputStream in, long length) throws IOException {
			super();
			int parts = (int)(length / (long)MAX_INDIVIDUAL_BUFFER_SIZE);
			if (length % (long)MAX_INDIVIDUAL_BUFFER_SIZE != 0)
				parts++;
			data = new byte[parts][];
			endOffsets = new long[parts];
			long tmpLength = length;
			long read = 0;
			for(int i=0;i<parts;i++)
			{
				int bytes = (int)Math.min((long)MAX_INDIVIDUAL_BUFFER_SIZE, tmpLength);
				System.err.println("Processing " + bytes + " bytes");
				
				data[i] = new byte[bytes];
				in.readFully(data[i]);
				//System.err.println(ArrayUtils.join(data[i], ","));
				tmpLength -= (long) bytes;
				read += (long) bytes;
				endOffsets[i] = read-1;
				System.err.println("array " + i + " length = " + bytes + " endOffset=" + endOffsets[i]);
			}
			
		}

		public void seek(long _pos)
		{
			this.pos = _pos;
		}
		
		public long getFilePointer()
		{
			return this.pos;
		}
		
		public long length() {
			return endOffsets[endOffsets.length-1];
		}
		
		@Override
		public int read() throws IOException 
		{
			//System.err.println("looking for "+ pos);
			int rangeId = Arrays.binarySearch(endOffsets, pos);
			int offset;
			if (rangeId < 0)
			{
				rangeId = (-(rangeId)-1);				
			}
			if (rangeId == 0)
			{
				offset = (int)pos;
			}
			else if (rangeId > endOffsets.length)
			{
				return -1;
			}
			else
			{
				offset = -1+ (int)(pos - endOffsets[rangeId-1]);
			}
			//System.err.print("pos="+pos+" rangeId="+ rangeId + " offset=" + offset + " byte="+data[rangeId][offset]+"\n");
			int rtr = data[rangeId][offset] & 0xff;   //(int)(data[rangeId][offset]);
			pos++;
			return rtr;
		}

		@Override
		public long skip(long n) throws IOException {
			pos += n;
			return n;
		}

		@Override
		public void close() throws IOException {
			super.close();
			data = null;
			endOffsets = null;
		}
		
	}
	
	/** class which allows seeking over a ByteArrayInputStream */
	private static class SeeakableByteArrayInputStream extends ByteArrayInputStream implements Seekable
	{
		public SeeakableByteArrayInputStream(byte[] buf) 
		{
			super(buf);
		}
		
		public void seek(long _pos)
		{
			super.pos = (int)_pos;
		}
		
		public long getFilePointer()
		{
			return (long)super.pos;
		}
		
		public long length() {
			return (long)count;
		}
		
		public byte[] getBuffer()
		{
			return super.buf;
		}
	}
	
	/** input stream to use */
	protected Seekable buf;
	
	/** decide which seekable implementatino to use.
	 * @param in a DataInputStream to read
	 * @param length how many bytes to expect from in
	 * @return an InputStream which also implements Seekable
	 * @throws IOException if an IO problem occurs
	 */
	private static InputStream getSeekable(DataInputStream in, long length) throws IOException
	{
		if (length < MAX_INDIVIDUAL_BUFFER_SIZE)
		{
			byte[] buf = new byte[(int)length];
			in.readFully(buf);
			return new SeeakableByteArrayInputStream(buf);
		} 
		return new MultiSeeakableByteArrayInputStream(in, length);
	}
	
	protected RandomDataInputMemory(InputStream seekable) throws IOException
	{
		super(seekable);
		buf = (Seekable)super.in;
	}
	
	/** Construct a new RandomDataInputMemory object, backed by the specified file */
	public RandomDataInputMemory(String filename) throws IOException {
		this(new DataInputStream(Files.openFileStream(filename)), Files.length(filename));
	}
	
	/** Construct a new RandomDataInputMemory object, backed by the specified buffer */
	public RandomDataInputMemory(DataInputStream in, long length) throws IOException {
		super(getSeekable(in, length));
		buf = (Seekable)super.in;
	}
	
	/** Construct a new RandomDataInputMemory object, backed by the specified buffer */
	public RandomDataInputMemory(byte[] b) {
		super(new SeeakableByteArrayInputStream(b));
		buf = (Seekable)super.in;
	}
	
	/** {@inheritDoc} */
	public long getFilePointer() throws IOException {
		return buf.getFilePointer();
	}

	/** {@inheritDoc} */
	public long length() throws IOException {
		return buf.length();
	}

	/** {@inheritDoc} */
	public void seek(long _pos) throws IOException {
		buf.seek(_pos);
	}

	/** {@inheritDoc} */
	public void close() throws IOException {
		buf.close();
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		InputStream in = null;
		if (buf instanceof MultiSeeakableByteArrayInputStream)
		{
			in = new MultiSeeakableByteArrayInputStream( 
					((MultiSeeakableByteArrayInputStream)buf).data, 
					((MultiSeeakableByteArrayInputStream)buf).endOffsets);
		}
		else if (buf instanceof SeeakableByteArrayInputStream) 
		{
			in = new SeeakableByteArrayInputStream(((SeeakableByteArrayInputStream)buf).getBuffer());
		}			
		try{
			return new RandomDataInputMemory(in);
		} catch (IOException ioe) {
			throw new CloneNotSupportedException(ioe.getMessage());
		}
	}

}
