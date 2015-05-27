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
 * The Original Code is TestFSOrderedMapFile.java
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.collections;

import gnu.trove.TObjectIntHashMap;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
//import static org.junit.Assert.*;
import org.terrier.structures.Skipable;
import org.terrier.structures.collections.FSOrderedMapFile.MapFileWriter;
import org.terrier.structures.seralization.FixedSizeIntWritableFactory;
import org.terrier.structures.seralization.FixedSizeTextFactory;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.utility.io.RandomDataInputMemory;

/** Make some tests on FSOrderedMapFile */
public class TestFSOrderedMapFile extends TestCase {
	@Rule String file = new TemporaryFolder().newFile("testFSOMapfile" + FSOrderedMapFile.USUAL_EXTENSION).toString();
	String[] testKeys = new String[]{"00", "0000", "1", "a", "z", new String(new byte[]{(byte)0xbb,(byte)0xB6,(byte)0xD3,(byte)0xAD}, "gb2312")};
	String[] testNotInKeys = new String[]{"0", "000", "zz"};
	protected TObjectIntHashMap<String> key2id = new TObjectIntHashMap<String>();
	
	
	public TestFSOrderedMapFile() throws Exception{}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		MapFileWriter w = FSOrderedMapFile.mapFileWrite(file);
		FixedSizeWriteableFactory<Text> keyFactory = new FixedSizeTextFactory(20);
		int offset = 0;
		for(String key : testKeys)
		{
			Text wkey = keyFactory.newInstance();
			IntWritable wvalue = new IntWritable();
			wkey.set(key);
			wvalue.set(offset);

			w.write(wkey, wvalue);
			key2id.put(key, offset);
			offset++;
		}
		w.close();			
	}
	
	protected void checkKeys(FixedSizeTextFactory keyFactory, Map<Text,IntWritable> mapToCheck) throws Exception
	{
		for(String key : testKeys)
		{
			Text testKey = keyFactory.newInstance();
			testKey.set(key);
			IntWritable rtr = mapToCheck.get(testKey);
			assertNotNull("Got null response for key " + key, rtr);
			assertEquals("Response for key " + key + " was wrong", key2id.get(key), rtr.get());
		}
		String[] randomOrderedTestKeys = new String[testKeys.length];
		System.arraycopy(testKeys, 0, randomOrderedTestKeys, 0, testKeys.length);
		Collections.shuffle(Arrays.asList(randomOrderedTestKeys));
		for(String key : randomOrderedTestKeys)
		{
			Text testKey = keyFactory.newInstance();
			testKey.set(key);
			IntWritable rtr = mapToCheck.get(testKey);
			assertNotNull("Got null response for key " + key, rtr);
			assertEquals("Response for key " + key + " was wrong", key2id.get(key), rtr.get());
		}

		for(String key : testNotInKeys)
		{
			Text testKey = keyFactory.newInstance();
			testKey.set(key);
			IntWritable rtr = mapToCheck.get(testKey);
			assertNull("Got non null response for key " + key, rtr);
		}
	}
	
	protected void readStream(Iterator<Map.Entry<Text, IntWritable>> iterator) throws Exception
	{
		TObjectIntHashMap<String> copyKey2Id = key2id.clone();
		while(iterator.hasNext())
		{
			Map.Entry<Text, IntWritable> e = iterator.next();
			assertNotNull(e);
			assertNotNull(e.getKey());
			if (copyKey2Id.containsKey(e.getKey().toString()))
			{
				assertEquals(copyKey2Id.get(e.getKey().toString()), e.getValue().get());
				copyKey2Id.remove(e.getKey().toString());
			}				
		}
		assertTrue(copyKey2Id.size() == 0);
	}
	
	protected void readStreamSkip(Iterator<Map.Entry<Text, IntWritable>> iterator, int totalNumEntries) throws Exception
	{
		int skip = 3;
		int entryIndex = 0;
		((Skipable)iterator).skip(skip);
		entryIndex += skip;
		while(iterator.hasNext())
		{
			Map.Entry<Text, IntWritable> e = iterator.next();
			assertNotNull(e);
			assertNotNull(e.getKey());
			assertEquals(testKeys[entryIndex], e.getKey().toString());
			entryIndex++;
		}
		assertEquals(testKeys.length, entryIndex);
	}
	
	@Test public void testStream() throws Exception
	{
		FixedSizeTextFactory keyFactory = new FixedSizeTextFactory(20);
		FSOrderedMapFile.EntryIterator<Text, IntWritable> inputStream = new FSOrderedMapFile.EntryIterator<Text, IntWritable>(
				file, keyFactory, new FixedSizeIntWritableFactory());
		readStream(inputStream);
		inputStream.close();
	}
	
	@Test public void testStreamSkip() throws Exception
	{
		FixedSizeTextFactory keyFactory = new FixedSizeTextFactory(20);
		FSOrderedMapFile.EntryIterator<Text, IntWritable> inputStream = new FSOrderedMapFile.EntryIterator<Text, IntWritable>(
				file, keyFactory, new FixedSizeIntWritableFactory());
		readStreamSkip(inputStream, testKeys.length);
		inputStream.close();
	}
	
	@Test public void testOnDisk() throws Exception
	{
		FixedSizeTextFactory keyFactory = new FixedSizeTextFactory(20);
		FSOrderedMapFile<Text, IntWritable> mapfile = new FSOrderedMapFile<Text, IntWritable>(file, false, keyFactory, new FixedSizeIntWritableFactory());
		checkKeys(keyFactory, mapfile);
	}
	
	@Test public void testInMemory() throws Exception
	{
		FixedSizeTextFactory keyFactory = new FixedSizeTextFactory(20);
		Map<Text, IntWritable> mapfileInMem = new FSOrderedMapFile.MapFileInMemory<Text, IntWritable>(file, keyFactory, new FixedSizeIntWritableFactory());
		checkKeys(keyFactory, mapfileInMem);
	}
	
	@Test public void testInMemoryRandomDataInputMemory() throws Exception
	{
		FixedSizeTextFactory keyFactory = new FixedSizeTextFactory(20);
		Map<Text, IntWritable> mapfileInMem = new FSOrderedMapFile<Text, IntWritable>(
				new RandomDataInputMemory(file), file,
				keyFactory, new FixedSizeIntWritableFactory());
		checkKeys(keyFactory, mapfileInMem);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (! new File(file).delete())
			System.err.println("Could not delete file " + file);
	}
}
