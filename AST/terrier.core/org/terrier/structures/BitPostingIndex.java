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
 * The Original Code is BitPostingIndex.java
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;

import java.io.IOException;
import java.lang.reflect.Constructor;

import org.terrier.compression.BitFileBuffered;
import org.terrier.compression.BitFileInMemoryLarge;
import org.terrier.compression.BitIn;
import org.terrier.compression.BitInSeekable;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.io.WrappedIOException;
/** Class for various bit compressed index implementations, including parents to current DirectIndex and InvertedIndex implementations. 
 * <b>Index properties</b>:
 * <ul>
 * <li><tt>index.STRUCTURENAME.data-files</tt> - how many files represent this structure.</li>
 * <li><tt>index.STRUCTURENAME.data-source</tt> - one of {file,fileinmem} or a class implements BitInSeekable.</li>
 * <li><tt>index.STRUCTURENAME.fields.count</tt> - how many fields are in use by this structures.</li>
 * </ul>
 * @since 3.0
 */
public class BitPostingIndex implements PostingIndex<BitIndexPointer>
{

	static class DocidSpecificDocumentIndex implements FieldDocumentIndex {
		DocumentIndexEntry die;
		DocumentIndex di;
		
		public DocidSpecificDocumentIndex(DocumentIndex _di, DocumentIndexEntry _die)
		{
			di = _di;
			die = _die;
		}
		
		public DocumentIndexEntry getDocumentEntry(int docid) throws IOException {
			return die;
		}

		public int getDocumentLength(int docid) throws IOException {
			return die.getDocumentLength();
		}

		public int getNumberOfDocuments() {
			return di.getNumberOfDocuments();
		}

		public int[] getFieldLengths(int docid) throws IOException {
			return ((FieldDocumentIndexEntry)die).getFieldLengths();
		}
	}
	
	
	protected BitInSeekable[] file;
	protected Class<? extends IterablePosting> postingImplementation;
	protected Constructor<? extends IterablePosting> postingConstructor;
	
	protected Index index = null;
	protected int fieldCount = 0;
	

	/**
	 * Constructs an instance of the BitPostingIndex.
	 * @param _index
	 * @param _structureName
	 * @param _postingImplementation
	 * @throws IOException
	 */
	public BitPostingIndex(
			Index _index, 
			String _structureName, 
			Class<? extends IterablePosting> _postingImplementation)
		throws IOException
	{
		this(
				_index.getPath() + "/" + _index.getPrefix() + "." + _structureName + BitIn.USUAL_EXTENSION, 
				Byte.parseByte(_index.getIndexProperty("index."+_structureName+".data-files", "1")),
				_postingImplementation,
				_index.getIndexProperty("index."+_structureName+".data-source", "file"), 
				_index.getIntIndexProperty("index."+_structureName+".fields.count", 0));
		index = _index;
		
	}
	
	@Deprecated
	protected BitPostingIndex(
			String filename, byte fileCount,
			Class<? extends IterablePosting> _postingImplementation,
			String _dataSource)
		throws IOException
	{
		this(filename, fileCount, _postingImplementation, _dataSource, 0);
	}
	
	protected BitPostingIndex(
			String filename, byte fileCount,
			Class<? extends IterablePosting> _postingImplementation,
			String _dataSource, int _fieldCount)
		throws IOException
	{
		file = new BitInSeekable[fileCount];
		for(int i=0;i<fileCount;i++)
		{
			String dataFilename = fileCount == 1 ? filename : filename + String.valueOf(i);
			if (_dataSource.equals("fileinmem"))
			{
				System.err.println("BitPostingIndex loading " + dataFilename + " to memory");
				this.file[i] = new BitFileInMemoryLarge(dataFilename);
			}
			else if (_dataSource.equals("file"))
			{
				this.file[i] = new BitFileBuffered(dataFilename);
			}
			else
			{
				if (_dataSource.startsWith("uk.ac.gla.terrier"))
					_dataSource = _dataSource.replaceAll("uk.ac.gla.terrier", "org.terrier");				
				try{
					this.file[i] = Class.forName(_dataSource).asSubclass(BitInSeekable.class).getConstructor(String.class).newInstance(dataFilename);
				} catch (Exception e) {
					throw new WrappedIOException(e);
				}
			}
		}
		fieldCount = _fieldCount;
		setPostingImplementation(_postingImplementation);
	}
	
	private void setPostingImplementation(Class<? extends IterablePosting> postingClass) throws IOException
	{
		postingImplementation = postingClass;
		try{
			postingConstructor = (fieldCount > 0) 
				? postingImplementation.getConstructor(BitIn.class, Integer.TYPE, DocumentIndex.class, Integer.TYPE)
				: postingImplementation.getConstructor(BitIn.class, Integer.TYPE, DocumentIndex.class);
		} catch (NoSuchMethodException e) {
			throw new WrappedIOException(e);
		}
	}
	
	/** 
	 * {@inheritDoc} 
	 */
	public IterablePosting getPostings(BitIndexPointer pointer) throws IOException
	{
		final BitIn _file = this.file[pointer.getFileNumber()].readReset(pointer.getOffset(), pointer.getOffsetBits());
		IterablePosting rtr = null;
		
		DocumentIndex fixedDi = pointer instanceof DocumentIndexEntry
			? new DocidSpecificDocumentIndex(index.getDocumentIndex(), (DocumentIndexEntry)pointer)
			: null;
		
		try{
			rtr = (fieldCount > 0) 
				? postingConstructor.newInstance(_file, pointer.getNumberOfEntries(), fixedDi, fieldCount)
				: postingConstructor.newInstance(_file, pointer.getNumberOfEntries(), fixedDi);
		} catch (Exception e) {
			throw new WrappedIOException(e);
		}
		return rtr;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void close() {
		try{
			for(java.io.Closeable c : file)
				c.close();
		} catch (IOException ioe) {}
	}

}
