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
 * The Original Code is Index.java.
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
/** 
 * This class encapsulates all the Indexes at retrieval time. 
 * It is loaded by giving a path and prefix. This looks for an 
 * index properties file at path/prefix.properties. Essentially, the
 * properties file then specifies which index structures the index
 * supports. The index then loads these so they can be used in retrieval.
 * <p>
 * Arbitrary properties can be defined in the index properties files, and
 * in particular, properties are used to record index statistics and 
 * the contructor type and values of various index objects.
 * <p>
 * The Index will apply methods on specially marked interfaces. Currently,
 * the only interface supported is <a href="IndexConfigurable.html">IndexConfigurable</a>. 
 * Moreover, structures implementing java.io.Closeable will have
 * their close method called when the Index is closed.
 * <p>
 * @author Craig Macdonald &amp; Vassilis Plachouras
*/
public class Index implements Closeable, Flushable
{
	protected final static int MINIMUM_INDEX_TERRIER_VERSION = 3;
	protected final static String PROPERTIES_SUFFIX = ".properties";
	protected static boolean RETRIEVAL_LOADING_PROFILE = Boolean.parseBoolean(ApplicationSetup.getProperty("terrier.index.retrievalLoadingProfile.default", "true"));
	/**
	 * Set RETRIEVAL_LOADING_PROFILE
	 * @param yes
	 */
	public static void setIndexLoadingProfileAsRetrieval(boolean yes)
	{
		RETRIEVAL_LOADING_PROFILE = yes;
	}
	/**
	 * Get RETRIEVAL_LOADING_PROFILE
	 * @return retrieval loading profile
	 */
	public static boolean getIndexLoadingProfileAsRetrieval()
	{
		return RETRIEVAL_LOADING_PROFILE;
	}
	
	
	/** This collection statistics parses the associated index properties for each call. It doesnt support fields. */
	protected class UpdatingCollectionStatistics extends CollectionStatistics
	{
		private static final long serialVersionUID = 1L;
		public UpdatingCollectionStatistics (Index index)
		{
			super(0,0,0,0, new long[0]);
		}

		@Override
		public double getAverageDocumentLength() {
			final int numDocs = getNumberOfDocuments();
			if (numDocs == 0)
				return 0.0d;
			return (double)getNumberOfTokens() / (double) numDocs;
		}

		@Override
		public int getNumberOfDocuments() {
			return Integer.parseInt(properties.getProperty("num.Documents","0"));
		}

		@Override
		public long getNumberOfPointers() {
			return Long.parseLong(properties.getProperty("num.Pointers", "0"));
		}

		@Override
		public long getNumberOfTokens() {
			return Long.parseLong(properties.getProperty("num.Pointers", "0"));
		}

		@Override
		public int getNumberOfUniqueTerms() {
			return Integer.parseInt(properties.getProperty("num.Terms", "0"));
		}
		
	}
	
	/** The logger used */
	protected static final Logger logger = Logger.getLogger(Index.class);
	protected static String lastLoadError = null;
	/** path component of this index's location */
	protected String path;
	/** prefix component of this index's location */
	protected String prefix;
	/** properties of this index */	
	protected final Properties properties = new Properties();
	/** Cache of all opened index structures, but not input streams */
	protected final HashMap<String, Object> structureCache = new HashMap<String, Object>(10);
	/** Have the properties of this index changed, suggesting a flush() is necessary when closing */
	protected boolean dirtyProperties = false;

	/** Set to true if loading an index succeeds */
	protected boolean loadSuccess = true;
	protected String loadError = null;

	
	/** A constructor for child classes that doesnt open the file */
	protected Index(long a, long b, long c) { }
	/**
	 * A default constuctor that creates an instance of the index.
	 */
	protected Index() {
		this(ApplicationSetup.TERRIER_INDEX_PATH,
			ApplicationSetup.TERRIER_INDEX_PREFIX, false);
	}
	/**
	 * Constructs a new Index object. Don't call this method,
	 * call the createIndex(String) factory method to
	 * construct an Index object.
	 * @param _path String the path in which the data structures
	 *		will be created.
	 * @param _prefix String the prefix of the files to
	 *		be created.
	 * @param isNew where a new Index should be created if there is no index
	 * 		at the specified location
	 */
	protected Index(String _path, String _prefix, boolean isNew) {
		if (!(new File(_path)).isAbsolute())
			_path = ApplicationSetup.makeAbsolute(_path, ApplicationSetup.TERRIER_VAR);
		
		this.path = _path;
		this.prefix = _prefix;
		
		boolean indexExists = loadProperties();	

		if (isNew && ! indexExists)
		{
			logger.debug("Creating new index : " + this.toString());
			setIndexProperty("index.terrier.version", ApplicationSetup.TERRIER_VERSION);
			setIndexProperty("index.created", ""+System.currentTimeMillis());
			setIndexProperty("num.Documents","0");
			setIndexProperty("num.Terms", "0");
			setIndexProperty("num.Tokens", "0");
			setIndexProperty("num.Pointers", "0");
			loadUpdatingStatistics();
			dirtyProperties = true;
			loadSuccess = true;	
		}
		else if (indexExists)
		{
			logger.debug("Loading existing index : " + this.toString());
			//note the order - some structures will require collection statistics, so load this first.
			loadStatistics();
			loadIndices();
		}
	}
	/** 
	 * Get the index properties
	 */
	public Properties getProperties(){
		return properties;
	}

	/** for an immutable index, use a normal collection statistics, never changes */	
	protected void loadStatistics()
	{
		//calculate fields
		int fieldCount = 0;
		if (this.hasIndexStructure("inverted"))
		{
			fieldCount = Integer.parseInt(properties.getProperty("index.inverted.fields.count","0"));
		} else if (this.hasIndexStructure("direct"))
		{
			fieldCount = Integer.parseInt(properties.getProperty("index.direct.fields.count","0"));
		}
		final long[] tokensF = new long[fieldCount];
		for(int fi=0;fi<fieldCount;fi++)
		{
			tokensF[fi] = Long.parseLong(properties.getProperty("num.field."+fi+".Tokens", "0"));
		}
		//create collection statistics
		structureCache.put("collectionstatistics",
			new CollectionStatistics(
				Integer.parseInt(properties.getProperty("num.Documents","0")),
				Integer.parseInt(properties.getProperty("num.Terms", "0")),
				Long.parseLong(properties.getProperty("num.Tokens", "0")),
				Long.parseLong(properties.getProperty("num.Pointers", "0")),
				tokensF
			));
	}

	/** for an index that is not yet built, use an UpdatingCollectionStatistics, which is slower
	  * but can support updates of the index statistics */	
	protected void loadUpdatingStatistics()
	{
		structureCache.put("collectionstatistics", new UpdatingCollectionStatistics(this));
	}

	/** load all index structures. Is disabled if index property <tt>index.preloadIndices.disabled</tt>
	 * is set to true. It is false by default, which means that all non-inputstream indices are loaded 
	 * on initialisation of the index. When the property is true, indices are loaded as required. */
	protected void loadIndices()
	{
		final boolean methodDisabled = Boolean.parseBoolean(properties.getProperty("index.preloadIndices.disabled", "false"));
		if (methodDisabled || ! RETRIEVAL_LOADING_PROFILE)
			return;
		
		boolean OK = true;
		//look for all index structures
		for (Object oKey: properties.keySet())
		{
			final String sKey = (String)oKey;
			if (sKey.matches("^index\\..+\\.class$")
				&& ! (sKey.matches("^index\\..+-inputstream.class$"))) //don't pre-load input streams
			{
				final String structureName = sKey.split("\\.")[1];
				Object o = getIndexStructure(structureName);
				if (o == null)
				{
					loadError = "Could not load an index structure called "+ structureName;
					OK = false;
				}
			}
		}
		if (! OK)
			this.loadSuccess = false;
	}

	/** loads in the properties file, falling back to the Terrier 1.xx log file if no properties exist. */
	protected boolean loadProperties()
	{
		try{ 
			String propertiesFilename = path + ApplicationSetup.FILE_SEPARATOR + prefix + ".properties";
			if(! allExists(propertiesFilename))
			{
				loadSuccess = false;
				loadError = "Index not found: "+propertiesFilename + " not found.";
				return false;
			}
			else
			{
				InputStream propertyStream = Files.openFileStream(propertiesFilename);
				properties.load(propertyStream);
				propertyStream.close();
			}
			
		} catch (IOException ioe) {
			loadSuccess = false;
			logger.error("Problem loading index properties", ioe);
			loadError = "Problem loading index properties: "+ ioe;
			return false;
		}
		if (properties.getProperty("index.terrier.version", null) == null)
		{
			loadSuccess = false;
			logger.error("index.terrier.version not set in index, invalid index?");
			loadError = "index.terrier.version not set in index";
			return false;
		}
		final String versionString = properties.getProperty("index.terrier.version", null);
		final String[] versionStringParts = versionString.split("\\.");
		if (Integer.parseInt(versionStringParts[0]) < MINIMUM_INDEX_TERRIER_VERSION)
		{
			loadSuccess = false;
			logger.error(loadError = "This index is too old. Need at least version "+MINIMUM_INDEX_TERRIER_VERSION +" index");
			return false;
		}
		return true;
	}

	/** Does this index have an index structure with the specified name?
	  * @param structureName name of the required structure
	  * @return true if the index has an appropriately named structure */	
	public boolean hasIndexStructure(String structureName)
	{
		return properties.containsKey("index."+structureName+".class");
	}
	
	/** Does this index have an index structure input stream with the specified name?
	 * @param structureName name of the required structure
	 * @return true if the index has an appropriately named structure */ 
	public boolean hasIndexStructureInputStream(String structureName)
	{
		return properties.containsKey("index."+structureName+"-inputstream.class");
	}

	/** Obtains the named index structure, using an already loaded one if possible.
	  * @param structureName name of the required structure
	  * @return desired object or null if not found */	
	public Object getIndexStructure(String structureName)
	{
		Object rtr = structureCache.get(structureName);
		if (rtr != null)
			return rtr;
		rtr = loadIndexStructure(structureName);
		if (rtr != null)
			structureCache.put(structureName, rtr);
		return rtr;
	}
	
	/** Load a new instance of the named index structure.
	  * @param structureName name of the required structure
	  * @return desired object or null if not found */
	protected Object loadIndexStructure(String structureName)
	{	
		logger.debug("Attempting to load structure "+structureName);
		try{
			// figure out the correct class
			String structureClassName = properties.getProperty("index."+structureName+".class");
			if (structureClassName == null)
			{
				logger.error("This index ("+this.toString()+") doesnt have an index structure called "+ structureName 
						+ ": property index."+structureName+".class not found");
				logger.error("Valid structures are: " + Arrays.deepToString( IndexUtil.getStructures(this)));
				return null;//TODO exceptions?
			}
			if (structureClassName.startsWith("uk.ac.gla.terrier"))
				structureClassName = structureClassName.replaceAll("uk.ac.gla.terrier", "org.terrier");
			//obtain the class definition for the index structure
			Class<?> indexStructureClass = null;
			try{
				indexStructureClass = Class.forName(structureClassName, false, this.getClass().getClassLoader());
			} catch (ClassNotFoundException cnfe) {
				logger.error("ClassNotFoundException: This index ("+this.toString()+") references an unknown index structure class: "+structureName+ " looking for "+ structureClassName);
				cnfe.printStackTrace();
				return null;//TODO exceptions?
			}

			//build up the constructor parameter type array
			final ArrayList<Class<?>> paramTypes = new ArrayList<Class<?>>(5);

			final String typeList = properties.getProperty("index."+structureName+".parameter_types", "java.lang.String,java.lang.String").trim();
			Object rtr = null;
			//for objects with constructor arguments
			if (typeList.length() > 0)
			{
				final String[] types = typeList.split("\\s*,\\s*");
				for (String t: types)
				{
					if (t.startsWith("uk.ac.gla.terrier"))
						t = t.replaceAll("uk.ac.gla.terrier", "org.terrier");					
					paramTypes.add(Class.forName(t));
				}
				Class<?>[] param_types = paramTypes.toArray(EMPTY_CLASS_ARRAY);
	
				//build up the constructor parameter value array
				String[] params = properties.getProperty("index."+structureName+".parameter_values", "path,prefix").split("\\s*,\\s*");
				Object[] objs = new Object[paramTypes.size()];
				int i=0;
				for (String p: params)
				{
					//System.err.println("looking for parameter value called "+ p + " with type '" + param_types[i]+ "'");
					if (p.equals("path"))
						objs[i] = path;
					else if (p.equals("prefix"))
						objs[i] = prefix;
					else if (p.equals("index"))
						objs[i] = this;
					else if (p.equals("structureName"))
					{
						final String tmp = structureName;
						objs[i] = tmp.replaceAll("-inputstream$", "");
					}
					else if (param_types[i].equals(java.lang.Class.class))
					{
						//System.err.println("loading class called "+p);
						if (p.startsWith("uk.ac.gla.terrier"))
							p = p.replaceAll("uk.ac.gla.terrier", "org.terrier");
						objs[i] = Class.forName(p);
					}
					else if (p.endsWith("-inputstream"))//no caching for input streams
						 objs[i] = loadIndexStructure(p);
					else if (p.matches("^\\$\\{.+\\}$"))
					{
						String propertyName = p.substring(2,p.length()-1);
						objs[i] = properties.getProperty(propertyName, ApplicationSetup.getProperty(propertyName, null));
						if (objs[i] == null)
							throw new IllegalArgumentException("Property "+propertyName+" not found");
					}
					else
						objs[i] = getIndexStructure(p);
					i++;
				}

				//get the index structure using the appropriate constructor with correct parameters
				rtr = indexStructureClass.getConstructor(param_types).newInstance(objs);
			}
			else
			{	//no constructor arguments
				rtr = indexStructureClass.newInstance();
			}
		
			//Special case hacks
			//1. set the Index properties if desired
			if (rtr instanceof IndexConfigurable)
			{
				((IndexConfigurable)rtr).setIndex(this);
			}
			// we're done
			return rtr;		
			
		} catch (Throwable t) {
			logger.error("Couldn't load an index structure called "+structureName, t);
			return null;
		}
	}

	/** Return the input stream associated with the specified structure of this index
	  * @param structureName  The name of the structure of which you want the inputstream. Eg "lexicon"
	  * @return Required structure, or null if not found */
	public Object getIndexStructureInputStream(String structureName)
	{
		//no caching on inputstreams
		return loadIndexStructure(structureName+"-inputstream");
	}
	
	/**
	 * Closes the data structures of the index.
	 */
	public void close() throws IOException
	{
		//invoke the close methods on all currently open index structures
		for (Object o : structureCache.values())
		{
			try{
				IndexUtil.close(o);
			}catch (IOException ioe) {/* ignore */} 
		}
		structureCache.clear();
		flushProperties();	
	}

	/** Write any dirty properties down to disk */
	protected void flushProperties() throws IOException
	{
		if (dirtyProperties)
		{
			final String propertiesFilename = path + ApplicationSetup.FILE_SEPARATOR + prefix + PROPERTIES_SUFFIX;
			if ((Files.exists(propertiesFilename) && ! Files.canWrite(propertiesFilename))||
				(! Files.exists(propertiesFilename) && ! Files.canWrite(path)))
			{
				logger.warn("Could not write to index properties at "+propertiesFilename 
					+ " because you do not have write permission on the index - some changes may be lost");
				return;
			}
			
			final OutputStream outputStream = Files.writeFileStream(propertiesFilename); 
			properties.store(outputStream,this.toString());
			outputStream.close();
			dirtyProperties = false;
			
		}
	}

	/** Write any dirty data structures down to disk */
	public void flush() throws IOException
	{
		flushProperties();
	}

	/** Returns the InvertedIndex to use for this index */	
	public InvertedIndex getInvertedIndex()
	{
		return (InvertedIndex)getIndexStructure("inverted");
	}
	/** Return the DirectIndex associated with this index */
	public DirectIndex getDirectIndex()
	{
		return (DirectIndex)getIndexStructure("direct");
	}
	/** Return the Lexicon associated with this index */
	@SuppressWarnings("unchecked")
	public Lexicon<String> getLexicon()
	{
		return (Lexicon<String>)getIndexStructure("lexicon");
	}
	/** Return the DocumentIndex associated with this index */
	public DocumentIndex getDocumentIndex()
	{
		return (DocumentIndex)getIndexStructure("document");
	}
	/** 
	 * Get the Meta Index structure
	 */
	public MetaIndex getMetaIndex()
	{
		return (MetaIndex)getIndexStructure("meta");
	}
	/** 
	 * Get the collection statistics 
	 */
	public CollectionStatistics getCollectionStatistics()
	{
		return (CollectionStatistics)getIndexStructure("collectionstatistics");
	}


	/** Returns a String representation of this index */
	public String toString()
	{
		return "Index("+path+","+prefix+")";
	}

	
	/** Returns the path of this index */
	public String getPath()
	{
		return path;
	}

	/** Returns the prefix of this index */
	public String getPrefix()
	{
		return prefix;
	}
	
	/** add an index structure to this index. Structure will be called structureName, and instantiated by a class
	 * 	called className. Instantiation parameters are "String,String", which are "path,prefix".
	 * @param structureName
	 * @param className
	 */
	public void addIndexStructure(String structureName, String className)
	{
		properties.setProperty("index."+structureName + ".class", className);
		properties.setProperty("index."+structureName+".parameter_types", "java.lang.String,java.lang.String");
		properties.setProperty("index."+structureName+".parameter_values", "path,prefix");
		dirtyProperties = true;
	}
	
	 /** tell the index about a new index structure it provides. Class and parameter types specified as Class objects
	  * instead of Strings. */
	public void addIndexStructure(String structureName, Class<?> className, Class<?>[] paramTypes, String[] paramValues)
	{
		final int l = paramTypes.length;
		String[] SparamTypes = new String[l];
		for(int i=0;i<l;i++)
			SparamTypes[i] = paramTypes[i].getName();
		addIndexStructure(structureName, className.getName(), SparamTypes, paramValues);
	}
	
	/** add an index structure to this index. Structure will be called structureName, and instantiated by a class
	 * 	called className. Instantiation type parameters or values are non-default.
	 */ 
	public void addIndexStructure(String structureName, String className, String[] paramTypes, String[] paramValues)
	{
		properties.setProperty("index."+structureName + ".class", className);
		properties.setProperty("index."+structureName+".parameter_types", join(paramTypes, ","));
		properties.setProperty("index."+structureName+".parameter_values",join(paramValues,","));
		dirtyProperties = true;
	}
	/** add an index structure to this index. Structure will be called structureName, and instantiated by a class
	 * 	called className. Instantiation type parameters or values are non-default.
	 */ 
	public void addIndexStructure(String structureName, String className, String paramTypes, String paramValues)
	{
		properties.setProperty("index."+structureName + ".class", className);
		properties.setProperty("index."+structureName+".parameter_types", paramTypes);
		properties.setProperty("index."+structureName+".parameter_values",paramValues);
		dirtyProperties = true;
	}
	
	 /** tell the index about a new input stream index structure it provides. */
	public void addIndexStructureInputStream(String structureName, String className)
	{
		addIndexStructure(structureName+"-inputstream", className);
	}
	
	/** tell the index about a new input stream index structure it provides. */
	public void addIndexStructureInputStream(String structureName, Class<?> className, Class<?>[] paramTypes, String[] paramValues)
	{
		addIndexStructure(structureName+"-inputstream", className, paramTypes, paramValues);
	}
	
	
	 /** tell the index about a new input stream index structure it provides. */
	public void addIndexStructureInputStream(String structureName, String className, String[] paramTypes, String[] paramValues)
	{
		addIndexStructure(structureName+"-inputstream", className, paramTypes, paramValues);
	}
	
	/** tell the index about a new input stream index structure it provides. */
	public void addIndexStructureInputStream(String structureName, String className, String paramTypes, String paramValues)
	{
		addIndexStructure(structureName+"-inputstream", className, paramTypes, paramValues);
	}
	
	/** set an arbitrary property in the index 
 	  * @param key Key to of the property to set
 	  * @param value Value of the property to set */
	public void setIndexProperty(String key, String value)
	{
		properties.setProperty(key, value);
		dirtyProperties = true;
	}
	
	/** get an arbitrary property in the index 
 	  * @param key Key of the property to get 
 	  * @param defaultValue value of the property to use if property is not set 
 	  * @return Value of the property */
	public String getIndexProperty(String key, String defaultValue)
	{
		return properties.getProperty(key, defaultValue);
	}

	/** get an arbitrary int property from the index */
	public int getIntIndexProperty(String key, int defaultValue)
	{
		String rtr = properties.getProperty(key, null);
		if (rtr== null)
			return defaultValue;
		return Integer.parseInt(rtr);
	}
	

	/**
	 * Factory method for load an index. This method should be used in order to
	 * load an existing index in the applications.
	 * @param path String the path in which the 
	 *		data structures will be created. 
	 * @param prefix String the prefix of the files
	 *		to be created.
	 */
	public static Index createIndex(String path, String prefix) {
		Index i = new Index(path, prefix, false);
		if (! i.loadSuccess)
		{
			lastLoadError = i.loadError;
			return null;
		}
		return i;
	}
	
	/**
	 * Factory method create a new index. This method should be used in order to
	 * load a new index in the applications.
	 * @param path String the path in which the 
	 *		data structures will be created. 
	 * @param prefix String the prefix of the files
	 *		to be created.
	 */
	public static Index createNewIndex(String path, String prefix) {
		Index i = new Index(path, prefix, true);
		if (! i.loadSuccess)
		{
			lastLoadError = i.loadError;
			return null;
		}
		return i;
	}

	/** Returns the last warning given by an index being loaded. */
	public static String getLastIndexLoadError()
	{
		return lastLoadError;
	}

	/** Returns true if it is likely that an index exists at the specified location
 	  * @param path
 	  * @param prefix
 	  * @return true if a .properties or a .log files exists */
	public static boolean existsIndex(String path, String prefix) {
		if (!(new File(path)).isAbsolute())
			path = ApplicationSetup.makeAbsolute(path, ApplicationSetup.TERRIER_VAR);
		return allExists(path + ApplicationSetup.FILE_SEPARATOR + prefix + PROPERTIES_SUFFIX)
			/*|| allExists(path + ApplicationSetup.FILE_SEPARATOR + prefix + LOG_SUFFIX)*/;
	}

	
	/** 
	 * Factory method for creating an 
	 * index. This method should be used in order to
	 * load an existing index in the applications.
	 */
	public static Index createIndex() {
		return createIndex(
			ApplicationSetup.TERRIER_INDEX_PATH,
			ApplicationSetup.TERRIER_INDEX_PREFIX);
	}
	
	/** returns true if all named files exist */
	protected static boolean allExists(String... files)
	{
		for(int i=0;i<files.length;i++)
		{
			if (! Files.exists(files[i]))
			{
				logger.debug("Files  "+files[i] + " doesn't exist");
				return false;
			}
		}
		return true;
	}

	/** joins a series of strings together with a delimiter */
	protected static String join(String[] input, String joinString)
	{
		StringBuilder rtr = new StringBuilder();
		int i = input.length;
		for(String s : input)
		{
			rtr.append(s);
			if (i > 0)
				rtr.append(joinString);
		}
		return rtr.toString();
	}

	/** empty class array */	
	protected static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
	/**
	 * main
	 * @param args
	 */
	public static void main (String[] args)
	{
		Index index = Index.createIndex();
        System.out.println("Index path: "+ index.getPath() + " prefix: "+ index.getPrefix() );
	}
}
