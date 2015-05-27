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
 * The Original Code is BitFileBuffered.java
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.compression;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.terrier.utility.Files;
import org.terrier.utility.io.RandomDataInput;


/** Implementation of BitInSeekable/BitIn interfaces similar to BitFile. However this
  * class buffers only a small area of the posting list, to minimise large memory 
  * allocations during retrieval. In contrast to BitFile, this class is read-only.
  * @author Patrice Lacour, Craig Macdonald
  */
public class BitFileBuffered implements BitInSeekable { 
	/** how much of a file to buffer by default */	
	protected static final int DEFAULT_BUFFER_LENGTH = 8*1024;
	/** The logger used */
	protected static final Logger logger = Logger.getLogger(BitFileBuffered.class);
	/** The underlying file */
	protected RandomDataInput file;
	/** how much of this file we will buffer */
	protected final int buffer_size;
	/** how big the file is, so we know when to stop reading */
	protected long fileSize;

	/** Constructor for a RandomDataInput object */
	public BitFileBuffered(RandomDataInput f)
	{
		file = f;
		buffer_size = DEFAULT_BUFFER_LENGTH;
		try{
			fileSize = f.length();
		} catch (IOException ioe) {
			logger.error("Input/Output exception getting file length in BitFileBuffered object.", ioe);
		}
	}
	
    /** 
     * Constructs an instance of the class for a given filename, using the default buffer size
     * @param _file the underlying file
     */	
	public BitFileBuffered(File _file) {
		this(_file, DEFAULT_BUFFER_LENGTH);
	}

    /** 
     * Constructs an instance of the class for a given filename. Default buffer size
     * @param filename java.lang.String the name of the underlying file
     */
	public BitFileBuffered(String filename) {
		this(filename, DEFAULT_BUFFER_LENGTH);
	}
	
    /** 
     * Constructs an instance of the class for a given filename 
     * @param _file the underlying file
     * @param bufSize how much of the file to buffer
     */
	public BitFileBuffered(File _file, int bufSize) {
		buffer_size = bufSize;
		fileSize = _file.length();
		try {
			this.file = Files.openFileRandom(_file);
		} catch (IOException ioe) {
			logger.error("Input/Output exception while creating BitFileBuffered object.", ioe);
		}	
	}
	
	/** 
	 * Constructs an instance of the class for a given filename
	 * @param filename java.lang.String the name of the underlying file
	 * @param bufSize how much of the file to buffer
	 */
	public BitFileBuffered(String filename, int bufSize) {
		buffer_size = bufSize;
		try {
			fileSize = Files.length(filename);
			file =  Files.openFileRandom(filename);
		} catch (IOException ioe) {
			logger.error("Input/Output exception while creating BitFileBuffered object.", ioe);
		}	
	}
	
	
	/**
	 * Reads from the file a specific number of bytes and after this
	 * call, a sequence of read calls may follow. The offsets given 
	 * as arguments are inclusive. For example, if we call this method
	 * with arguments 0, 2, 1, 7, it will read in a buffer the contents 
	 * of the underlying file from the third bit of the first byte to the 
	 * last bit of the second byte.
	 * @param startByteOffset the starting byte to read from
	 * @param startBitOffset the bit offset in the starting byte
	 * @param endByteOffset the ending byte 
	 * @param endBitOffset the bit offset in the ending byte. 
	 *        This bit is the last bit of this entry.
	 * @return Returns the BitIn object to use to read that data
	 */	
	public BitIn readReset(long startByteOffset, byte startBitOffset, long endByteOffset, byte endBitOffset) {
		final long range = endByteOffset - startByteOffset + (long)1;
		return new BitInBuffered(file,startByteOffset,startBitOffset, range < buffer_size ? (int)range : buffer_size);
	}
	
	
	/**
	 * Reads from the file from a specific offset. After this
	 * call, a sequence of read calls may follow.
	 * @param startByteOffset the starting byte to read from
	 * @param startBitOffset the bit offset in the starting byte
	 */
	public BitIn readReset(long startByteOffset, byte startBitOffset) 
	{
		final long actualBufferSize = (startByteOffset + buffer_size) > fileSize 
			? (fileSize - startByteOffset) 
			: buffer_size;
		return new BitInBuffered(file,startByteOffset,startBitOffset, (int)actualBufferSize);
	}
	
	/** {@inheritDoc} */
	public void close()
	{
		try {
			file.close();	
		} catch(IOException ioe) {
			logger.error("Input/Output exception while reading from a random access file. Stack trace follows", ioe);
		}
	
	}

	/** Implements a BitIn around a RandomDataInput */
	protected static class BitInBuffered implements BitIn
	{
		/** parent file */
		protected RandomDataInput parentFile;
		/** ofset in the larger file */
		protected long offset;
		/** buffer for the slice of the file being read */
		protected byte[] inBuffer;
		/** size of the buffer */
		protected int size;
		/** byte offset within the buffer */
		protected int readByteOffset;
		/** bit offset in this byte, and in the larger file */
		protected int bitOffset;
	
		/** empty constructor for child classes */
		protected BitInBuffered(){}
		
		/** Construct a new BitInBuffered on the specified file, starting at the given offset
		 * and with the specified buffer length.
		 * @param file File to seek on
		 * @param startByteOffset Start byte offset
		 * @param _bitOffset Start bit offset
		 * @param _bufLength Number of bytes to buffer
		 */
		public BitInBuffered(RandomDataInput file, long startByteOffset, byte _bitOffset, int _bufLength)
		{
			this.offset = startByteOffset;
			this.bitOffset= _bitOffset;
			this.parentFile = file;
			this.size = _bufLength;
			try{				
				parentFile.seek(startByteOffset);
				inBuffer = new byte[size];
				parentFile.readFully(inBuffer);
				readByteOffset = 0;
			}catch(IOException ioe){
				logger.error("Input/Output exception while reading from a random access file. Stack trace follows", ioe);
			}
		}
		
		
		/* algorithm in this class: 
			for every byte read	
				if we exceed current buffer
					seek parentFile if needed
					read (size) more from parentFile
				end if
			for a skip
				if skip exceed current buffer
					seek parent file to end of skip
					read (size) more from parentFile
				end if
		*/
		
		
		/** Move forward one byte */
		protected void incrByte()
		{
			try{		
				readByteOffset++;
				offset++;
				if(readByteOffset == size)
				{					
					readByteOffset=0;
					//Arrays.fill(inBuffer, (byte)0);
					parentFile.seek(offset);
					//logger.info("Reading 1024 bytes. pos="+parentFile.getFilePointer());
					try{
						parentFile.readFully(inBuffer);
					} catch (EOFException eofe) { /* ignore this */}
				}
			}catch(IOException ioe){
				logger.error("Input/Output exception while reading from a random access file. Stack trace follows", ioe);
			}
		}	
	
		/** Move forward i bytes */
		protected void incrByte(int i)
		{
			try{
				//System.out.println("skypping");
				offset += i;
				readByteOffset+=i;
				if( readByteOffset >= size ) // we go to the next block  -- we skip only the begin of the block
				{
					parentFile.seek(offset); // we skip the first bytes of the next block
					//Arrays.fill(inBuffer, (byte)0);
					readByteOffset = 0;
					//logger.info("Reading 1024 bytes. pos="+parentFile.getFilePointer());
					try{
						parentFile.readFully(inBuffer);
					} catch (EOFException eofe) { /* ignore this */}
				}
				
			}catch(IOException ioe){
				logger.error("Input/Output exception while reading from a random access file. Stack trace follows", ioe);
			}
		}
		
		/** {@inheritDoc} **/
		public int readDelta() throws IOException {		
			final int msb = readGamma();
			return ( ( 1 << msb ) | readBinary( msb ) ) - 1;
		}
		
		/** {@inheritDoc} **/
		public int readGamma()  {
			final int u = readUnary() - 1;		
			return (1 << u) + readBinary(u) ;
		}
		
		/** {@inheritDoc} **/
		public int readSkewedGolomb( final int b ) throws IOException {
			
			final int M = ( ( 1 << readUnary()  ) - 1 ) * b;
			final int m = ( M / ( 2 * b ) ) * b;
			return m + readMinimalBinary( M - m ) ;
		}
		
		/** {@inheritDoc} **/
		public void readInterpolativeCoding( int data[], int localoffset, int len, int lo, int hi ) throws IOException {
			final int h, m;
			
			if ( len == 0 ) return;
			if ( len == 1 ) {
				data[ localoffset ] = readMinimalBinaryZero( hi - lo  ) + lo  ;		
				return;
			}
			
			h = len / 2;
			m = readMinimalBinaryZero( hi - len + h  - ( lo + h ) + 1 ) + lo + h ;
			data[ localoffset + h ] = m ;
			
			readInterpolativeCoding(  data, localoffset, h, lo, m - 1 );
			readInterpolativeCoding(  data, localoffset + h + 1, len - h - 1, m + 1, hi );
		}

		/** {@inheritDoc} **/
		public int readUnary() {
			int x;
			final int leftA = (inBuffer[readByteOffset] << bitOffset) & 0x00FF;		
			if(leftA != 0){			
				x = 8 - BitUtilities.MSB_BYTES[ leftA ];
				bitOffset += x ;
				readIn();
				return x;
			}
			x = 8 - bitOffset;
			incrByte();
			while( (inBuffer[readByteOffset]== 0 )) {
				x += 8;
				incrByte();
			}
			x += (bitOffset =  8 -  BitUtilities.MSB_BYTES[ inBuffer[readByteOffset] & 0x00FF] );
			readIn();
			return x;		
		}
	
		/** {@inheritDoc} **/
		public int readGolomb( final int b) throws IOException {		
			final int q = (readUnary() - 1 ) * b;
			return q + readMinimalBinary( b ) + 1;
		}
		
		/** {@inheritDoc} **/
		public int readMinimalBinary( final int b ) throws IOException {	
			final int log2b = BitUtilities.mostSignificantBit(b);
			final int m = ( 1 << log2b + 1 ) - b; 
			final int x = readBinary( log2b );		
			if ( x < m ) return x + 1;
			else { int temp =  ( x << 1 ) + readBinary(1)  ;
			return temp;
			}
		}
		
		/** {@inheritDoc} **/
		public int readMinimalBinaryZero(int b) throws IOException{
			if(b > 0 ) return readMinimalBinary(b);
			else return 0;
		}
		
		/**
		 * Reads a new byte from the InputStream if we have finished with the current one. 
		 */
		protected void readIn(){
			if(bitOffset == 8){					
				bitOffset = 0;
				incrByte();	 						
			}
		}


		/** {@inheritDoc} **/
	   	 public void align(){
	     	   if ( ( bitOffset & 7 ) == 0 ) return;
	     	   bitOffset = 0;
	    	    incrByte();
	   	 }
	
	   	/** {@inheritDoc} **/
		public int readBinary(int len) {
			if(8 - bitOffset > len){					
				int b = ( ((inBuffer[readByteOffset] << bitOffset) & 0x00FF)) >>> (8-len) ;
				bitOffset += len;
				return b;
			}
			
			int x = inBuffer[readByteOffset] & ( ~ (0xFF << (8-bitOffset) )) &0xFF;
			len +=  bitOffset - 8;
			int i = len >> 3;
			while(i-- != 0){			
				incrByte();
				x = x << 8 | (inBuffer[readByteOffset] & 0xFF); 
			}		
			incrByte();
			bitOffset = len & 7;	
			return (x << bitOffset) | ((inBuffer[readByteOffset] & 0xFF) >>> (8-bitOffset)) ;
		}

		/** Skip a number of bits in the current input stream
	 	* @param len The number of bits to skip
	 	*/
    	public void skipBits(int len)
    	{
			if(8 - bitOffset > len){
				bitOffset += len;	
				return;
			}
			len +=  bitOffset - 8;
			final int i = len >> 3;
			if (i > 0)
			{
			   incrByte(i);
			}
			incrByte();
			bitOffset = len & 7;
		}
    	
    	/** {@inheritDoc} */
		public void skipBytes(long len) throws IOException
		{
			if (readByteOffset + len > inBuffer.length)
			{
				offset += len;
				parentFile.seek(offset); // we skip the first bytes of the next block
				//Arrays.fill(inBuffer, (byte)0);
				readByteOffset = 0;
				//logger.info("Reading 1024 bytes. pos="+parentFile.getFilePointer());
				try{
					parentFile.readFully(inBuffer);
				} catch (EOFException eofe) { /* ignore this */}
			}
			else
			{
				offset += len;
				readByteOffset += len;
				bitOffset = 0;
			}			
		}
		
		/** {@inheritDoc} */
		public long getByteOffset(){ return offset;}
		/** {@inheritDoc} */
		public byte getBitOffset(){return (byte) bitOffset;}
		
		/** Does nothing */		
		public void close(){
			/*try{
				file.close();
			}catch(IOException ioe){
				logger.error("Input/Output exception while closing BitFile object.", ioe);
			}*/
		}
		
		
		
   }	
}
	
