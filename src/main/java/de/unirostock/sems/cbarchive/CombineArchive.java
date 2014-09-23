/**
 * CombineArchive - a JAVA library to read/write/create/.. CombineArchives
 * Copyright (c) 2014, Martin Scharm <combinearchive-code@binfalse.de>
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package de.unirostock.sems.cbarchive;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.cbarchive.meta.MetaDataFile;
import de.unirostock.sems.cbarchive.meta.MetaDataHolder;
import de.unirostock.sems.cbarchive.meta.MetaDataObject;
import de.unirostock.sems.cbarchive.meta.OmexMetaDataObject;
import de.unirostock.sems.cbarchive.meta.omex.OmexDescription;



/**
 * The Class CombineArchive to create/read/manipulate/store etc.
 * CombineArchives.
 * <p>
 * We directly operate on the ZIP file, which will be kept open. Therefore, do
 * not forget to finally close the CombineArchive when you're finished.
 * </p>
 * 
 * @see <a href="https://sems.uni-rostock.de/projects/combinearchive/">
 *      sems.uni-rostock.de/projects/combinearchive</a>
 * @author martin scharm
 */
public class CombineArchive
	extends MetaDataHolder
	implements Closeable
{
	
	/** The Constant MANIFEST_LOCATION. */
	public static final String						MANIFEST_LOCATION	= "/manifest.xml";
	/** The Constant METADATA_LOCATION. */
	public static final String						METADATA_LOCATION	= "/metadata.rdf";
	
	/** The archive entries. */
	private HashMap<String, ArchiveEntry>	entries;
	
	/** The zip archive. */
	private FileSystem										zipfs;
	
	/** The main entry. */
	private List<ArchiveEntry>									mainEntries;
	
	/** A list of files containing meta data. */
	private List<Path>										metaDataFiles;
	
	private List<String> errors;
	
	private static final String MIME_REGEX = "[a-zA-Z0-9+.-]+/[a-zA-Z0-9+.-]+";
	private static final String PURL_PREFIX = "http://purl.org/NET/mediatypes/";
	
	
	/**
	 * Instantiates a new empty combine archive.
	 * 
	 * @param zipFile
	 *          the archive to read, will be created if non-existent
	 * 
	 * @throws IOException
	 *           if we cannot create a temporary directory
	 * @throws CombineArchiveException
	 * @throws ParseException
	 * @throws JDOMException
	 */
	public CombineArchive (File zipFile)
		throws IOException,
			JDOMException,
			ParseException,
			CombineArchiveException
	{
		errors = new ArrayList<String> ();
		mainEntries = new ArrayList<ArchiveEntry> ();
		entries = new HashMap<String, ArchiveEntry> ();
		Map<String, String> zip_properties = new HashMap<String, String> ();
		zip_properties.put ("create", "true");
		zip_properties.put ("encoding", "UTF-8");
		zipfs = FileSystems.newFileSystem (
			URI.create ("jar:" + zipFile.toURI ()), zip_properties);
		
		metaDataFiles = new ArrayList<Path> ();
		
		// read manifest
		Path mani = zipfs.getPath (MANIFEST_LOCATION).normalize ();
		if (Files.isRegularFile (mani))
			parseManifest (mani, false);
	}
	
	
	/**
	 * Instantiates a new empty combine archive.
	 * 
	 * If <code>continueOnError</code> is true we won't raise an exception
	 * in case of errors. So you can continue working on the archive even
	 * if a file is missing. <strong>But handle with care!</strong> You
	 * might worsen the whole situation. In any case you should make sure
	 * that there are no errors using {@link #hasErrors()}. The list of
	 * occurred errors can then be obtained using {@link #getErrors()}.
	 * 
	 * @param zipFile
	 *          the archive to read, will be created if non-existent
	 * @param continueOnError
	 * 					ignore errors and continue (as far as possible)
	 * 
	 * @throws IOException
	 *           if we cannot create a temporary directory
	 * @throws CombineArchiveException
	 * @throws ParseException
	 * @throws JDOMException
	 */
	public CombineArchive (File zipFile, boolean continueOnError)
		throws IOException,
		JDOMException,
		ParseException,
		CombineArchiveException
	{
		errors = new ArrayList<String> ();
		mainEntries = new ArrayList<ArchiveEntry> ();
		entries = new HashMap<String, ArchiveEntry> ();
		Map<String, String> zip_properties = new HashMap<String, String> ();
		zip_properties.put ("create", "true");
		zip_properties.put ("encoding", "UTF-8");
		try
		{
			zipfs = FileSystems.newFileSystem (
				URI.create ("jar:" + zipFile.toURI ()), zip_properties);
		}
		catch (IOException e)
		{
			LOGGER.error (e, "cannot read archive " + zipFile.toURI () + " (file system creation failed)");
			errors.add ("cannot read archive " + zipFile.toURI () + " (file system creation failed)");
			if (!continueOnError)
				throw e;
			return;
		}
		
		metaDataFiles = new ArrayList<Path> ();
		
		// read manifest
		Path mani = zipfs.getPath (MANIFEST_LOCATION).normalize ();
		if (Files.isRegularFile (mani))
			parseManifest (mani, continueOnError);
	}
	
	
	/**
	 * Gets the the first main entry of this archive, if defined. As of RC2 of the spec there may be more than one main entry, so you should use {@link #getMainEntries()} instead.
	 * 
	 * @return the first main entry, or <code>null</code> if there is no main entry
	 * @deprecated as of version 0.8.2, replaced by
	 *             {@link #getMainEntries()}
	 */
	public ArchiveEntry getMainEntry ()
	{
		if (mainEntries == null)
			return null;
		return mainEntries.size () > 0 ? mainEntries.get (0) : null;
	}
	
	/**
	 * Gets the main entries as defined in the archive.
	 *
	 * @return the main entries
	 */
	public List<ArchiveEntry> getMainEntries ()
	{
		return mainEntries;
	}
	
	
	/**
	 * Sets a main entry of the archive. Other main entries get replaced. Use {@link #addMainEntry(ArchiveEntry)} to add another main entry.
	 * 
	 * @param mainEntry
	 *          the new main entry
	 */
	public void setMainEntry (ArchiveEntry mainEntry)
	{
		this.mainEntries.clear ();
		addMainEntry (mainEntry);
	}
	
	/**
	 * Adds an entry to the list of main entries in this archive.
	 *
	 * @param mainEntry the main entry
	 */
	public void addMainEntry (ArchiveEntry mainEntry)
	{
		this.mainEntries.add (mainEntry);
	}
	
	
	/**
	 * Removes an entry from the list of main entries.
	 *
	 * @param entry the entry to be removed
	 */
	public void removeMainEntry (ArchiveEntry entry)
	{
		this.mainEntries.remove (entry);
	}
	
	
	/**
	 * Prepare location for our entries-map.
	 * <p>
	 * Paths such as <code>./path/to/file</code> and <code>path/to/file</code> are
	 * rewritten to <code>/path/to/file</code> to match the keys of the
	 * entries-map.
	 * </p>
	 * 
	 * @param location
	 *          the location
	 * @return the string
	 */
	private String prepareLocation (String location)
	{
		if (location.startsWith ("./"))
			location = location.substring (1);
		
		if (!location.startsWith ("/"))
			location = "/" + location;
		
		return location;
	}
	
	
	/**
	 * Retrieves an entry stored at a specified location. The location should
	 * start with <code>/</code> (the root of the archive).
	 * 
	 * @param location
	 *          the location
	 * @return the entry
	 */
	public ArchiveEntry getEntry (String location)
	{
		location = prepareLocation (location);
		return entries.get (location);
	}
	
	
	/**
	 * Removes an entry defined by its relative location from the archive. The
	 * location should start with <code>/</code> (the root of the archive).
	 * 
	 * @param location
	 *          the location of the corresponding file
	 * @return true if we found that entry and removed it successfully
	 * @throws IOException
	 */
	public boolean removeEntry (String location) throws IOException
	{
		location = prepareLocation (location);
		
		ArchiveEntry entry = entries.remove (location);
		
		if (entry != null)
		{
			if (mainEntries == entry)
				mainEntries = null;
			Files.delete (entry.getPath ());
			return true;
		}
		return false;
	}
	
	
	/**
	 * Removes an entry from the archive.
	 * 
	 * @param entry
	 *          the entry to remove
	 * @return true if we found that entry and removed it successfully
	 * @throws IOException
	 */
	public boolean removeEntry (ArchiveEntry entry) throws IOException
	{
		if (entries.remove (entry.getFilePath ()) != null)
		{
			if (mainEntries == entry)
				mainEntries = null;
			Files.delete (entry.getPath ());
			return true;
		}
		return false;
	}
	
	
	/**
	 * Retireves an entry by its location.
	 * 
	 * @param location
	 *          the location of the entry, should start with <code>/</code> (root
	 *          of the archive).
	 * @return the entry located at <code>location</code>, or <code>null</code> if
	 *         there is no such entry
	 */
	public ArchiveEntry getEntryByLocation (String location)
	{
		location = prepareLocation (location);
		return entries.get (location);
	}
	
	
	/**
	 * Adds an entry to the archive.
	 * <p>
	 * The current version of the concerning file will be copied immediately.
	 * Thus, upcoming modifications of the source file won't affect the version in
	 * our archive. The path of this file in the archive will be
	 * <code>targetName</code>, it may include sub directories, e.g.
	 * <code>/path/in/archive/file.ext</code>. If there is already a file in the
	 * archive having the same path we'll overwrite it.
	 * </p>
	 * 
	 * @param toInsert
	 *          the file to insert
	 * @param targetName
	 *          the target name of the file in the archive
	 * @param format
	 *          the format URI, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>
	 * @return the archive entry or null if adding failed
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public ArchiveEntry addEntry (File toInsert, String targetName, URI format)
		throws IOException
	{
		return addEntry (toInsert, targetName, format, false);
	}
	
	
	/**
	 * Adds an entry to the archive.
	 * <p>
	 * The current version of the concerning file will be copied immediately.
	 * Thus, upcoming modifications of the source file won't affect the version in
	 * our archive. The path of this file in the archive will be
	 * <code>targetName</code>, it may include sub directories, e.g.
	 * <code>/path/in/archive/file.ext</code>. If there is already a file in the
	 * archive having the same path we'll overwrite it.
	 * </p>
	 * 
	 * @param toInsert
	 *          the file to insert
	 * @param targetName
	 *          the target name of the file in the archive
	 * @param format
	 *          the format URI, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>
	 * @return the archive entry or null if adding failed
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @deprecated as of version 0.9, replaced by
	 *             {@link #addEntry(java.io.File,java.lang.String,java.net.URI)}
	 */
	public ArchiveEntry addEntry (File toInsert, String targetName, String format)
		throws IOException
	{
		URI formatUri = null;
		try
		{
			formatUri = new URI (format);
		}
		catch (URISyntaxException e)
		{
			LOGGER.warn (e, "could not parse URI ", format);
			errors.add ("could not parse URI " + format);
			return null;
		}
		
		return addEntry (toInsert, targetName, formatUri, false);
	}
	
	
	/**
	 * Adds an entry to the archive.
	 * <p>
	 * The current version of the concerning file will be copied immediately.
	 * Thus, upcoming modifications of the source file won't affect the version in
	 * our archive. The path of this file in the archive will be
	 * <code>targetName</code>, it may include sub directories, e.g.
	 * <code>/path/in/archive/file.ext</code>. If there is already a file in the
	 * archive having the same path we'll overwrite it.
	 * </p>
	 * 
	 * @param toInsert
	 *          the file to insert
	 * @param targetName
	 *          the target name of the file in the archive
	 * @param format
	 *          the format URI, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>
	 * @param mainEntry
	 *          the main entry
	 * @return the archive entry or null if adding failed
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public ArchiveEntry addEntry (File toInsert, String targetName,
		URI format, boolean mainEntry) throws IOException
	{
		targetName = prepareLocation (targetName);
		
		if (targetName.equals (MANIFEST_LOCATION))
			throw new IllegalArgumentException ("it's not allowed to name a file "
				+ MANIFEST_LOCATION);
		
		if (targetName.equals (METADATA_LOCATION))
			throw new IllegalArgumentException ("it's not allowed to name a file "
				+ METADATA_LOCATION);
		
		// we also do not allow files with names like metadata-[0-9]*.rdf
		if (targetName.matches ("^/metadata-[0-9]*\\.rdf$"))
			throw new IllegalArgumentException (
				"it's not allowed to name a file like metadata-[0-9]*.rdf");
		
		// insert to zip
		Path insertPath = zipfs.getPath (targetName).normalize ();
		Files.createDirectories (insertPath.getParent ());
		Files.copy (toInsert.toPath (), insertPath, Utils.COPY_OPTION);
		
		ArchiveEntry entry = new ArchiveEntry (this, insertPath, format);
		entries.put (entry.getFilePath (), entry);
		
		if (mainEntry)
		{
			LOGGER.debug ("setting main entry:");
			addMainEntry (entry);
		}
		
		return entry;
	}
	
	
	/**
	 * Adds an entry to the archive.
	 * <p>
	 * The current version of the concerning file will be copied immediately.
	 * Thus, upcoming modifications of the source file won't affect the version in
	 * our archive. The path of this file in the archive will be
	 * <code>targetName</code>, it may include sub directories, e.g.
	 * <code>/path/in/archive/file.ext</code>. If there is already a file in the
	 * archive having the same path we'll overwrite it.
	 * </p>
	 * 
	 * @param toInsert
	 *          the file to insert
	 * @param targetName
	 *          the target name of the file in the archive
	 * @param format
	 *          the format URI, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>
	 * @param mainEntry
	 *          the main entry
	 * @return the archive entry or null if adding failed
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @deprecated as of version 0.9, replaced by
	 *             {@link #addEntry(java.io.File,java.lang.String,java.net.URI,boolean)}
	 */
	public ArchiveEntry addEntry (File toInsert, String targetName,
		String format, boolean mainEntry) throws IOException
	{
		URI formatUri = null;
		try
		{
			formatUri = new URI (format);
		}
		catch (URISyntaxException e)
		{
			LOGGER.warn (e, "could not parse URI ", format);
			errors.add ("could not parse URI " + format);
			return null;
		}
		
		return addEntry (toInsert, targetName, formatUri, mainEntry);
	}
	
	
	/**
	 * Adds an entry to the archive.
	 * <p>
	 * The current version of the concerning file will be copied immediately.
	 * Thus, upcoming modifications of the source file won't affect the version in
	 * our archive. The path of this file in the archive will be the path of
	 * <code>file</code> relative to <code>baseDir</code>. If there is already a
	 * file in the archive having the same relative path we'll overwrite it.
	 * </p>
	 * 
	 * @param baseDir
	 *          the base dir
	 * @param file
	 *          the file
	 * @param format
	 *          the format URI, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>
	 * @param mainEntry
	 *          is this the main entry of the archive? (default:
	 *          <code>false</code>)
	 * @return the archive entry or null if adding failed
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public ArchiveEntry addEntry (File baseDir, File file, URI format,
		boolean mainEntry) throws IOException
	{
		if (!file.exists ())
			throw new IOException ("file does not exist.");
		
		if (!file.getAbsolutePath ().contains (baseDir.getAbsolutePath ()))
			throw new IOException ("file must be in basedir.");
		
		String localName = file.getAbsolutePath ().replace (
			baseDir.getAbsolutePath (), "");
		
		return addEntry (file, localName, format, mainEntry);
	}
	
	
	/**
	 * Adds an entry to the archive.
	 * <p>
	 * The current version of the concerning file will be copied immediately.
	 * Thus, upcoming modifications of the source file won't affect the version in
	 * our archive. The path of this file in the archive will be the path of
	 * <code>file</code> relative to <code>baseDir</code>. If there is already a
	 * file in the archive having the same relative path we'll overwrite it.
	 * </p>
	 * 
	 * @param baseDir
	 *          the base dir
	 * @param file
	 *          the file
	 * @param format
	 *          the format URI, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>
	 * @param mainEntry
	 *          is this the main entry of the archive? (default:
	 *          <code>false</code>)
	 * @return the archive entry or null if adding failed
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @deprecated as of version 0.9, replaced by
	 *             {@link #addEntry(java.io.File,java.io.File,java.net.URI,boolean)}
	 */
	public ArchiveEntry addEntry (File baseDir, File file, String format,
		boolean mainEntry) throws IOException
	{
		URI formatUri = null;
		try
		{
			formatUri = new URI (format);
		}
		catch (URISyntaxException e)
		{
			LOGGER.warn (e, "could not parse URI ", format);
			errors.add ("could not parse URI " + format);
			return null;
		}
		
		return addEntry (baseDir, file, formatUri, mainEntry);
	}
	
	
	/**
	 * Adds an entry to the archive.
	 * The current version of the concerning file will be copied immediately.
	 * Thus, upcoming modifications of the source file won't affect the version
	 * in our archive.
	 * The path of this file in the archive will be the path of <code>file</code>
	 * relative to <code>baseDir</code>.
	 * If there is already a file in the archive having the same relative path
	 * we'll delete it.
	 * 
	 * @param baseDir
	 *          the base dir
	 * @param file
	 *          the file
	 * @param format
	 *          the format URI, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>
	 * @return the archive entry or null if adding failed
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public ArchiveEntry addEntry (File baseDir, File file, URI format)
		throws IOException
	{
		if (!file.exists ())
			throw new IOException ("file does not exist.");
		
		if (!file.getAbsolutePath ().contains (baseDir.getAbsolutePath ()))
			throw new IOException ("file must be in basedir.");
		
		String localName = file.getAbsolutePath ().replace (
			baseDir.getAbsolutePath (), "");
		
		return addEntry (file, localName, format, false);
	}
	
	
	/**
	 * Adds an entry to the archive.
	 * The current version of the concerning file will be copied immediately.
	 * Thus, upcoming modifications of the source file won't affect the version
	 * in our archive.
	 * The path of this file in the archive will be the path of <code>file</code>
	 * relative to <code>baseDir</code>.
	 * If there is already a file in the archive having the same relative path
	 * we'll delete it.
	 * 
	 * @param baseDir
	 *          the base dir
	 * @param file
	 *          the file
	 * @param format
	 *          the format URI, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>
	 * @return the archive entry or null if adding failed
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @deprecated as of version 0.9, replaced by
	 *             {@link #addEntry(java.io.File,java.io.File,java.net.URI)}
	 */
	public ArchiveEntry addEntry (File baseDir, File file, String format)
		throws IOException
	{
		URI formatUri = null;
		try
		{
			formatUri = new URI (format);
		}
		catch (URISyntaxException e)
		{
			LOGGER.warn (e, "could not parse URI ", format);
			errors.add ("could not parse URI " + format);
			return null;
		}
		
		return addEntry (baseDir, file, formatUri, false);
	}
	
	
	/**
	 * Adds an entry to the archive.
	 * The current version of the concerning file will be copied immediately.
	 * Thus, upcoming modifications of the source file won't affect the version
	 * in our archive.
	 * The path of this file in the archive will be the path of <code>file</code>
	 * relative to <code>baseDir</code>.
	 * If there is already a file in the archive having the same relative path
	 * we'll delete it.
	 * 
	 * @param baseDir
	 *          the base dir
	 * @param file
	 *          the file
	 * @param format
	 *          the format URI, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>
	 * @param description
	 *          the description
	 * @param mainEntry
	 *          is this the main entry of the archive? (default:
	 *          <code>false</code>)
	 * @return the archive entry or null if adding failed
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @deprecated as of version 0.5, replaced by
	 *             {@link #addEntry(java.io.File,java.io.File,java.lang.String,boolean)}
	 */
	@Deprecated
	public ArchiveEntry addEntry (File baseDir, File file, String format,
		OmexDescription description, boolean mainEntry) throws IOException
	{
		if (!file.exists ())
			throw new IOException ("file does not exist.");
		
		if (!file.getAbsolutePath ().contains (baseDir.getAbsolutePath ()))
			throw new IOException ("file must be in basedir.");
		
		String localName = file.getAbsolutePath ().replace (
			baseDir.getAbsolutePath (), "");
		
		ArchiveEntry entry = addEntry (file, localName, format, mainEntry);
		entry.addDescription (new OmexMetaDataObject (description));
		return entry;
	}
	
	
	/**
	 * Adds an entry to the archive.
	 * The current version of the concerning file will be copied immediately.
	 * Thus, upcoming modifications of the source file won't affect the version
	 * in our archive.
	 * The path of this file in the archive will be the path of <code>file</code>
	 * relative to <code>baseDir</code>.
	 * If there is already a file in the archive having the same relative path
	 * we'll delete it.
	 * 
	 * @param baseDir
	 *          the base dir
	 * @param file
	 *          the file
	 * @param format
	 *          the format URI, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>
	 * @param description
	 *          the description
	 * @return the archive entry or null if adding failed
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @deprecated as of version 0.5, replaced by
	 *             {@link #addEntry(java.io.File,java.io.File,java.lang.String)}
	 */
	@Deprecated
	public ArchiveEntry addEntry (File baseDir, File file, String format,
		OmexDescription description) throws IOException
	{
		if (!file.exists ())
			throw new IOException ("file does not exist.");
		
		if (!file.getAbsolutePath ().contains (baseDir.getAbsolutePath ()))
			throw new IOException ("file must be in basedir.");
		
		String localName = file.getAbsolutePath ().replace (
			baseDir.getAbsolutePath (), "");
		
		ArchiveEntry entry = addEntry (file, localName, format, false);
		entry.addDescription (new OmexMetaDataObject (description));
		return entry;
	}
	
	
	/**
	 * Gets entries sharing a certain format.
	 * 
	 * @param format
	 *          the format URI of interest, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>
	 * @return the entries with that format
	 */
	public List<ArchiveEntry> getEntriesWithFormat (URI format)
	{
		
		List<ArchiveEntry> list = new ArrayList<ArchiveEntry> ();
		for (ArchiveEntry e : entries.values ())
			if (e.getFormat ().equals (format))
				list.add (e);
		
		return list;
	}
	
	
	/**
	 * Counts entries with a certain format.
	 * 
	 * @param format
	 *          the format URI of interest, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>
	 * @return the number of entries with that format
	 */
	public int getNumEntriesWithFormat (URI format)
	{
		return getEntriesWithFormat (format).size ();
	}
	
	
	/**
	 * Checks whether there are entries with a certain format.
	 * 
	 * @param format
	 *          the format URI of interest, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>
	 * @return true, if there is at least one entry in this archive having this
	 *         format
	 */
	public boolean HasEntriesWithFormat (URI format)
	{
		return getEntriesWithFormat (format).size () > 0;
	}
	
	
	/**
	 * Retrieves all entries.
	 * 
	 * @return the entries
	 */
	public Collection<ArchiveEntry> getEntries ()
	{
		return entries.values ();
	}
	
	
	/**
	 * Gets the number of entries stored in this archive.
	 * 
	 * @return the number of entries
	 */
	public int getNumEntries ()
	{
		return entries.size ();
	}
	
	
	/**
	 * Gets the enumerator of entries.
	 * 
	 * @return the iterator
	 */
	public Iterator<ArchiveEntry> getEnumerator ()
	{
		return entries.values ().iterator ();
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unirostock.sems.cbarchive.meta.MetaDataHolder#getEntityPath()
	 */
	@Override
	public String getEntityPath ()
	{
		return ".";
	}
	
	
	/**
	 * Creates a manifest entry.
	 * 
	 * @param location
	 *          the location of the entry
	 * @param format
	 *          the format of the entry, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>
	 * @return the XML node
	 */
	private Element createManifestEntry (String location, URI format,
		boolean mainEntry)
	{
		Element element = new Element ("content", Utils.omexNs);
		element.setAttribute ("location", location);
		element.setAttribute ("format", format.toString ());
		if (mainEntry)
			element.setAttribute ("master", "" + mainEntry);
		return element;
	}
	
	
	/**
	 * Write the manifest.
	 * 
	 * @param singleFile
	 *          write meta data to a single file?
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws TransformerException
	 *           the transformer exception
	 */
	private void writeManifest (boolean singleFile)
		throws IOException,
			TransformerException
	{
		File manifestFile = File.createTempFile ("combineArchiveManifest", "tmp");
		
		Document doc = new Document ();
		Element root = new Element ("omexManifest", Utils.omexNs);
		doc.addContent (root);
		 
		root.addContent (createManifestEntry (".",
			Utils.getOmexSpecUri (), false));
		root.addContent (createManifestEntry (MANIFEST_LOCATION,
			Utils.getOmexManifestUri (), false));
		
		for (ArchiveEntry e : entries.values ())
		{
			root.addContent (createManifestEntry (e.getPath ().toString (),
				e.getFormat (), mainEntries.contains (e)));
		}
		
		File baseDir = Files.createTempDirectory ("combineArchive").toFile ();
		
		List<File> descr = singleFile ? MetaDataFile.writeFile (baseDir, entries,
			this) : MetaDataFile.writeFiles (baseDir, entries, this);
		for (File f : descr)
		{
			root.addContent (createManifestEntry (
				f.getAbsolutePath ().replace (baseDir.getAbsolutePath (), ""),
				Utils.getOmexMetaDataUri (), false));
			
			// copy to zip
			Path newMeta = zipfs.getPath (
				f.getAbsolutePath ().replace (baseDir.getAbsolutePath (), ""))
				.normalize ();
			Files.copy (f.toPath (), newMeta, Utils.COPY_OPTION);
			metaDataFiles.add (newMeta);
			// delete original
			f.delete ();
		}
		baseDir.delete ();
		
		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter (new FileWriter (manifestFile));
			bw.write (Utils.prettyPrintDocument (doc));
		}
		catch (IOException | TransformerException e)
		{
			e.printStackTrace ();
			LOGGER.error (e, "cannot write manifest file to ", manifestFile);
			throw e;
		}
		finally
		{
			if (bw != null)
				try
				{
					bw.close ();
				}
				catch (IOException e)
				{
				}
		}
		
		// insert manifest into zip
		Files.copy (manifestFile.toPath (), zipfs.getPath (MANIFEST_LOCATION)
			.normalize (), Utils.COPY_OPTION);
		
		manifestFile.delete ();
	}
	
	
	/**
	 * Pack this archive: generates manifest and meta data files.
	 * <p>
	 * While we're working directly in the ZIP this generates manifest and meta
	 * data files. If <code>multipleMetaFiles</code> is set to <code>true</code>
	 * (default: <code>false</code>, see {@link #pack()}) we will generate one
	 * meta data file for each archive entry (instead of combining all meta data
	 * in a single file).
	 * </p>
	 * 
	 * @param multipleMetaFiles
	 *          should we create one meta file per archive entry or combine all
	 *          meta data in a single file?
	 * @throws TransformerException
	 * @throws IOException
	 */
	public void pack (boolean multipleMetaFiles)
		throws IOException,
			TransformerException
	{
		for (Path meta : metaDataFiles)
			Files.delete (meta);
		metaDataFiles = new ArrayList<Path> ();
		writeManifest (!multipleMetaFiles);
	}
	
	
	/**
	 * Pack this archive.
	 * <p>
	 * While we're working directly in the ZIP this generates manifest and meta
	 * data files. This method will generate a single meta data file for all meta
	 * data associated to the entries in this archive. See {@link #pack(boolean)}
	 * if you prefer creating multiple meta data files.
	 * </p>
	 * 
	 * @throws TransformerException
	 * @throws IOException
	 */
	public void pack () throws IOException, TransformerException
	{
		pack (false);
	}
	
	
	/**
	 * Parses a manifest file.
	 * 
	 * @param manifest
	 *          the manifest
	 * @param continueOnError
	 * 					ignore errors and continue
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws JDOMException
	 *           the jDOM exception
	 * @throws ParseException
	 *           the parse exception
	 * @throws CombineArchiveException
	 */
	private void parseManifest (Path manifest, boolean continueOnError)
		throws IOException,
			JDOMException,
			ParseException,
			CombineArchiveException
	{
		Document doc = null;
		try
		{
			doc = Utils.readXmlDocument (manifest);
		}
		catch (JDOMException e)
		{
			LOGGER.error (e, "cannot read manifest of archive");
			errors.add ("cannot read manifest of archive. xml seems to be invalid.");
			if (!continueOnError)
				throw e;
			return;
		}
		catch (IOException e)
		{
			LOGGER.error (e, "cannot read manifest of archive.");
			errors.add ("cannot read manifest of archive. io error.");
			if (!continueOnError)
				throw e;
			return;
		}
		metaDataFiles = new ArrayList<Path> ();
		List<Element> nl = Utils.getElementsByTagName (doc.getRootElement (),
			"content", Utils.omexNs);
		for (int i = 0; i < nl.size (); i++)
		{
			Element content = nl.get (i);
			
			String location = null;
			String master = null;
			URI format = null;
			
			Attribute attr = content.getAttribute ("location");
			if (attr != null)
				location = attr.getValue ();
			attr = content.getAttribute ("format");
			if (attr != null)
			{
				try
				{
					if (!attr.getValue ().startsWith ("http"))
						throw new URISyntaxException (attr.getValue (), "expected http uri");
					format = new URI (attr.getValue ());
				}
				catch (URISyntaxException e)
				{
					boolean foundMime = false;
					// is it a mimetype?
					String mime = attr.getValue ();
					if (mime.matches (MIME_REGEX))
					{
						try
						{
							format = new URI (PURL_PREFIX + mime);
							foundMime = true;
						}
						catch (URISyntaxException e1)
						{
							LOGGER.error ("couldn't convert mime ", mime, " to uri ", PURL_PREFIX, mime);
							errors.add ("couldn't convert mime " + mime + " to uri " + PURL_PREFIX + mime);
						}
					}
					if (!foundMime)
					{
						LOGGER.error ("archive seems to be corrupt. format ", attr.getValue (),
							" not a valid URI.");
						errors.add ("archive seems to be corrupt. format " + attr.getValue () +
							" not a valid URI.");
						if (!continueOnError)
							throw new IOException ("archive seems to be corrupt. format " + attr.getValue () +
								" not a valid URI.");
						continue;
					}
				}
			}
			attr = content.getAttribute ("master");
			if (attr != null)
				master = attr.getValue ();
			
			if (location == null)
			{
				LOGGER.error ("manifest invalid. unknown location of entry ", i);
				errors.add ("manifest invalid. unknown location of entry " + i);
				if (!continueOnError)
					throw new IOException ("manifest invalid. unknown location of entry "
						+ i);
				continue;
			}
			
			if (format.equals (Utils.getOmexSpecUri ()))
			{
				// that's the archive itself -> skip
				continue;
			}
			
			if( !location.startsWith("/") )
			{
				location = "/" + location;
			}
			location = Paths.get (location).normalize ().toString ();
			
			Path locFile = zipfs.getPath (location).normalize ();
			if (!Files.isRegularFile (locFile))
			{
				LOGGER.error ("archive seems to be corrupt. file ", locFile,
					" not found.");
				errors.add ("archive seems to be corrupt. file " + locFile
					+ " not found.");
				if (!continueOnError)
					throw new IOException ("archive seems to be corrupt. file " + locFile
						+ " not found.");
				continue;
			}
			
			if (format.equals (Utils.getOmexMetaDataUri ()))
			{
				metaDataFiles.add (locFile);
				// since that's not a real entry
				continue;
			}
			
			if (format.equals (Utils.getOmexManifestUri ()))
			{
				// that's this manifest -> skip
				continue;
			}
			
			ArchiveEntry entry = new ArchiveEntry (this, locFile, format);
			if (master != null && Boolean.parseBoolean (master))
				addMainEntry (entry);
			entries.put (location, entry);
		}
		
		// parse all descriptions
		for (Path f : metaDataFiles)
		{
			MetaDataFile.readFile (f, entries, this, continueOnError, errors);
		}
	}
	
	
	/**
	 * Move an entry. (rename it)
	 * 
	 * @param oldPath
	 *          the old path to the entry
	 * @param newPath
	 *          the target path
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public void moveEntry (String oldPath, String newPath) throws IOException
	{
		String alt = prepareLocation (oldPath);
		String neu = prepareLocation (newPath);
		
		ArchiveEntry entry = getEntryByLocation (alt);
		if (entry == null)
			throw new IOException ("no such entry in archive");
		
		boolean wasMain = mainEntries.contains (entry);
		entries.remove (alt);
		
		Path neuPath = zipfs.getPath (neu).normalize ();
		Files.createDirectories (neuPath.getParent ());
		Files.move (zipfs.getPath (alt).normalize (), neuPath,
			StandardCopyOption.ATOMIC_MOVE);
		ArchiveEntry newEntry = new ArchiveEntry (this, neuPath, entry.getFormat ());
		
		entries.put (neu, newEntry);
		if (wasMain)
		{
			addMainEntry (newEntry);
		}
		
		// move meta data
		List<MetaDataObject> meta = entry.getDescriptions ();
		for (MetaDataObject m : meta)
		{
			newEntry.addDescription (m);
		}
		
	}
	
	
	/**
	 * Extract an entry from this archive.
	 * 
	 * @param archivePath
	 *          the path of the entry in our archive, should start with an
	 *          <code>/</code>
	 * @param destination
	 *          the destination to write the entry to
	 * @return the file that was written (=<code>destination</code>)
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public File extract (Path archivePath, File destination) throws IOException
	{
		if (!Files.isRegularFile (archivePath))
			throw new IOException (archivePath + " is not a regular file");
		
		if (destination.isDirectory ())
			destination = destination.toPath ()
				.resolve ("./" + archivePath.normalize ().toString ()).normalize ()
				.toFile ();
		
		Files.createDirectories (destination.toPath ().getParent ());
		Files.copy (archivePath, destination.toPath (), Utils.COPY_OPTION);
		
		return destination;
	}
	
	
	/**
	 * Extract the whole archive to the disk.
	 * 
	 * @param destination
	 *          the destination
	 * @return true, if successful extracted
	 * @throws IOException
	 */
	public File extractTo (File destination) throws IOException
	{
		try (DirectoryStream<Path> directoryStream = Files
			.newDirectoryStream (zipfs.getPath ("/"));)
		{
			for (Path file : directoryStream)
			{
				extract (file, destination.toPath ());
			}
		}
		return destination;
	}
	
	
	/**
	 * Extract an entry or a directory.
	 * 
	 * @param zipPath
	 *          the what
	 * @param destination
	 *          the to
	 * @throws IOException
	 * @throws Exception
	 *           the exception
	 */
	private static void extract (Path zipPath, Path destination)
		throws IOException
	{
		if (Files.isDirectory (zipPath))
		{
			try (DirectoryStream<Path> directoryStream = Files
				.newDirectoryStream (zipPath);)
			{
				for (Path file : directoryStream)
				{
					extract (file, destination);
				}
			}
		}
		else
		{
			Path fileOutZip = destination.resolve (
				"./" + zipPath.normalize ().toString ()).normalize ();
			Files.createDirectories (fileOutZip.getParent ());
			Files.copy (zipPath, fileOutZip, Utils.COPY_OPTION);
		}
	}
	
	
	/**
	 * Gets the errors that occurred during creating/reading of an archive.
	 *
	 * @return the errors
	 */
	public List<String> getErrors ()
	{
		return errors;
	}
	
	/**
	 * Checks for errors.
	 *
	 * @return true, if there are errors
	 */
	public boolean hasErrors ()
	{
		return errors.size () > 0;
	}
	
	/**
	 * Clear all errors.
	 */
	public void clearErrors ()
	{
		errors.clear ();
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close () throws IOException
	{
		zipfs.close ();
	}
}
