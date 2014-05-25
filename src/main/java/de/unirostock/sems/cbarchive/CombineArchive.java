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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.json.simple.JSONObject;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.cbarchive.meta.MetaDataFile;
import de.unirostock.sems.cbarchive.meta.OmexMetaDataObject;
import de.unirostock.sems.cbarchive.meta.omex.OmexDescription;



/**
 * The Class CombineArchive to create/read/manipulate/store etc.
 * CombineArchives.
 * 
 * 
 * @author martin scharm
 */
public class CombineArchive
{
	
	/** The Constant MANIFEST_LOCATION. */
	public static final String						MANIFEST_LOCATION	= "manifest.xml";
	
	/** The archive entries. */
	private HashMap<String, ArchiveEntry>	entries;
	
	/** The base directory - here we'll store the files. */
	private File													baseDir;
	
	/** The main entry. */
	private ArchiveEntry									mainEntry;
	
	
	/**
	 * Instantiates a new empty combine archive.
	 * 
	 * @throws IOException
	 *           if we cannot create a temporary directory
	 */
	public CombineArchive () throws IOException
	{
		entries = new HashMap<String, ArchiveEntry> ();
		baseDir = Files.createTempDirectory ("CombineArchive").toFile ();
		baseDir.deleteOnExit ();
	}
	
	
	/**
	 * Instantiates a new empty combine archive.
	 * 
	 * @return the main entry
	 */
	/*
	 * private CombineArchive (File temporaryDirectory) throws IOException,
	 * JDOMException, ParseException
	 * {
	 * entries = new HashMap<String, ArchiveEntry> ();
	 * baseDir = temporaryDirectory;
	 * File mani = new File (baseDir.getAbsolutePath () + File.separatorChar +
	 * MANIFEST_LOCATION);
	 * if (mani.exists ())
	 * parseManifest (mani);
	 * }
	 */
	
	/**
	 * Gets the main entry of this archive.
	 * 
	 * @return the main entry
	 */
	public ArchiveEntry getMainEntry ()
	{
		return mainEntry;
	}
	
	
	/**
	 * Gets the base directory containing all files of this archive.
	 * 
	 * @return the base directory
	 */
	public File getBaseDir ()
	{
		return baseDir;
	}
	
	
	/**
	 * Removes an entry defined by its relative location from the archive. The
	 * location has to start with <code>./</code>.
	 * 
	 * @param location
	 *          the relative location of the corresponding file
	 */
	public void removeEntry (String location)
	{
		if (!location.startsWith ("./"))
			throw new IllegalArgumentException ("location has to start with ./");
		
		ArchiveEntry entry = entries.remove (location);
		if (mainEntry == entry)
			mainEntry = null;
	}
	
	
	/**
	 * Removes an entry from the archive.
	 * 
	 * @param entry
	 *          the entry to remove
	 */
	public void removeEntry (ArchiveEntry entry)
	{
		entries.remove (entry.getRelativeName ());
		if (mainEntry == entry)
			mainEntry = null;
	}
	
	
	/**
	 * Gets an entry by its location.
	 * 
	 * @param location
	 *          the relative location of the entry (according to the baseDir). Has
	 *          to start with "./"!
	 * @return the entry by location
	 */
	public ArchiveEntry getEntryByLocation (String location)
	{
		if (!location.startsWith ("./"))
			throw new IllegalArgumentException ("location has to start with ./");
		return entries.get (location);
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
	 *          the format
	 * @param mainEntry
	 *          is this the main entry of the archive?
	 * @return the archive entry
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public ArchiveEntry addEntry (File baseDir, File file, String format,
		boolean mainEntry) throws IOException
	{
		if (!file.exists ())
			throw new IOException ("file does not exist.");
		
		if (!file.getAbsolutePath ().contains (baseDir.getAbsolutePath ()))
			throw new IOException ("file must be in basedir.");
		
		String localName = file.getAbsolutePath ().replace (
			baseDir.getAbsolutePath (), "");
		if (localName.equals ("/" + MANIFEST_LOCATION))
			throw new IllegalArgumentException (
				"it's not allowed to name a file manifest.xml");
		
		File destination = new File (this.baseDir.getAbsolutePath () + localName);
		destination.getParentFile ().mkdirs ();
		destination.getParentFile ().deleteOnExit ();
		
		Files.copy (file.toPath (), destination.toPath (),
			java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		destination.deleteOnExit ();
		
		ArchiveEntry entry = new ArchiveEntry (this, "." + localName, format);
		
		entries.put (entry.getRelativeName (), entry);
		
		if (mainEntry)
		{
			LOGGER.debug ("setting main entry:");
			this.mainEntry = entry;
		}
		
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
	 *          the format
	 * @return the archive entry
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public ArchiveEntry addEntry (File baseDir, File file, String format)
		throws IOException
	{
		if (!file.exists ())
			throw new IOException ("file does not exist.");
		
		if (!file.getAbsolutePath ().contains (baseDir.getAbsolutePath ()))
			throw new IOException ("file must be in basedir.");
		
		String localName = file.getAbsolutePath ().replace (
			baseDir.getAbsolutePath (), "");
		if (localName.equals ("/" + MANIFEST_LOCATION))
			throw new IllegalArgumentException (
				"it's not allowed to name a file manifest.xml");
		
		File destination = new File (this.baseDir.getAbsolutePath () + localName);
		destination.getParentFile ().mkdirs ();
		destination.getParentFile ().deleteOnExit ();
		
		Files.copy (file.toPath (), destination.toPath (),
			java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		destination.deleteOnExit ();
		
		ArchiveEntry entry = new ArchiveEntry (this, "." + localName, format);
		
		entries.put (entry.getRelativeName (), entry);
		
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
	 *          the format
	 * @param description
	 *          the description
	 * @param mainEntry
	 *          is this the main entry of the archive?
	 * @return the archive entry
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
		if (localName.equals ("/" + MANIFEST_LOCATION))
			throw new IllegalArgumentException (
				"it's not allowed to name a file manifest.xml");
		
		File destination = new File (this.baseDir.getAbsolutePath () + localName);
		destination.getParentFile ().mkdirs ();
		destination.getParentFile ().deleteOnExit ();
		
		Files.copy (file.toPath (), destination.toPath (),
			java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		destination.deleteOnExit ();
		
		/*
		 * ArchiveEntry entry = new ArchiveEntry (this, "." + localName, format,
		 * description);
		 */
		ArchiveEntry entry = new ArchiveEntry (this, "." + localName, format);
		entry.addDescription (new OmexMetaDataObject (entry, description));
		
		entries.put (entry.getRelativeName (), entry);
		
		if (mainEntry)
		{
			LOGGER.debug ("setting main entry:");
			this.mainEntry = entry;
		}
		
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
	 *          the format
	 * @param description
	 *          the description
	 * @return the archive entry
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
		if (localName.equals ("/" + MANIFEST_LOCATION))
			throw new IllegalArgumentException (
				"it's not allowed to name a file manifest.xml");
		
		File destination = new File (this.baseDir.getAbsolutePath () + localName);
		destination.getParentFile ().mkdirs ();
		destination.getParentFile ().deleteOnExit ();
		
		Files.copy (file.toPath (), destination.toPath (),
			java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		destination.deleteOnExit ();
		
		ArchiveEntry entry = new ArchiveEntry (this, "." + localName, format);
		entry.addDescription (new OmexMetaDataObject (entry, description));
		
		entries.put (entry.getRelativeName (), entry);
		
		return entry;
	}
	
	
	/**
	 * Gets entries having a certain format.
	 * 
	 * @param format
	 *          the format of interest
	 * @return the entries with that format
	 */
	public List<ArchiveEntry> getEntriesWithFormat (String format)
	{
		String shortFormat = CombineFormats.getFormatIdentifier (format);
		
		List<ArchiveEntry> list = new ArrayList<ArchiveEntry> ();
		for (ArchiveEntry e : entries.values ())
			if (e.getFormat ().equals (format) || e.getFormat ().equals (shortFormat))
				list.add (e);
		
		return list;
	}
	
	
	/**
	 * Gets the number of entries with a certain format.
	 * 
	 * @param format
	 *          the format of interest
	 * @return the number of entries with that format
	 */
	public int getNumEntriesWithFormat (String format)
	{
		return getEntriesWithFormat (format).size ();
	}
	
	
	/**
	 * Checks for entries with a certain format.
	 * 
	 * @param format
	 *          the format of interest
	 * @return true, if there is at least one entry in this archive having this
	 *         format
	 */
	public boolean HasEntriesWithFormat (String format)
	{
		return getEntriesWithFormat (format).size () > 0;
	}
	
	
	/**
	 * Gets the entries.
	 * 
	 * @return the entries
	 */
	public Collection<ArchiveEntry> getEntries ()
	{
		return entries.values ();
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
	
	
	/**
	 * Creates a manifest entry.
	 * 
	 * @param location
	 *          the location of the entry
	 * @param format
	 *          the format of the entry
	 * @return the XML node
	 */
	private Element createManifestEntry (String location, String format,
		boolean mainEntry)
	{
		Element element = new Element ("content", Utils.omexNs);
		element.setAttribute ("location", location);
		element.setAttribute ("format", format);
		if (mainEntry)
			element.setAttribute ("master", "" + mainEntry);
		return element;
	}
	
	
	/**
	 * Write the manifest.
	 * 
	 * @return the list of files to zip
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws TransformerException
	 *           the transformer exception
	 */
	private List<File> writeManifest (boolean singleFile)
		throws IOException,
			TransformerException
	{
		List<File> fileList = new ArrayList<File> ();
		File manifestFile = new File (baseDir.getAbsolutePath () + File.separator
			+ MANIFEST_LOCATION);
		fileList.add (manifestFile);
		
		Document doc = new Document ();
		Element root = new Element ("omexManifest", Utils.omexNs);
		doc.addContent (root);
		
		root.addContent (createManifestEntry ("./" + MANIFEST_LOCATION,
			Utils.omexNs.getURI (), false));
		
		for (ArchiveEntry e : entries.values ())
		{
			root.addContent (createManifestEntry (e.getRelativeName (),
				e.getFormat (), e == mainEntry));
			fileList.add (new File (baseDir.getAbsolutePath () + File.separator
				+ e.getRelativeName ()));
		}
		
		List<File> descr = singleFile ? MetaDataFile.writeFile (baseDir, entries)
			: MetaDataFile.writeFiles (baseDir, entries);// OmexDescriptionFile.writeFile
																										// (descriptions, baseDir);
		for (File f : descr)
		{
			root.addContent (createManifestEntry (
				"." + f.getAbsolutePath ().replace (baseDir.getAbsolutePath (), ""),
				"http://identifiers.org/combine.specifications/omex-metadata", false));
			fileList.add (f);
		}
		
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
		return fileList;
	}
	
	
	/**
	 * Export an archive. Will write the manifest and the meta data files and
	 * packs everything to <code>destination</code>.
	 * 
	 * @param destination
	 *          the destination of the archive
	 * @param multipleMetaFiles
	 *          should we export the meta data to multiple files? (defaults to
	 *          <code>false</code>)
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws TransformerException
	 *           the transformer exception
	 */
	public void exportArchive (File destination, boolean multipleMetaFiles)
		throws IOException,
			TransformerException
	{
		// write current version of manifest
		List<File> fileList = writeManifest (!multipleMetaFiles);
		
		// create zip archive
		Utils.packZip (baseDir, destination, fileList);
	}
	
	
	/**
	 * Export an archive. Will write the manifest and the meta data files and
	 * packs everything to <code>destination</code>.
	 * 
	 * @param destination
	 *          the destination of the archive
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws TransformerException
	 *           the transformer exception
	 */
	public void exportArchive (File destination)
		throws IOException,
			TransformerException
	{
		// write current version of manifest
		List<File> fileList = writeManifest (true);
		
		// create zip archive
		Utils.packZip (baseDir, destination, fileList);
	}
	
	
	/**
	 * Parses a manifest file.
	 * 
	 * @param manifest
	 *          the manifest
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws JDOMException
	 *           the jDOM exception
	 * @throws ParseException
	 *           the parse exception
	 */
	private void parseManifest (File manifest)
		throws IOException,
			JDOMException,
			ParseException
	{
		Document doc = Utils.readXmlDocument (manifest);
		List<File> descr = new ArrayList<File> ();
		List<Element> nl = Utils.getElementsByTagName (doc.getRootElement (),
			"content", Utils.omexNs);
		for (int i = 0; i < nl.size (); i++)
		{
			Element content = nl.get (i);
			
			String location = null;
			String master = null;
			String format = null;
			
			Attribute attr = content.getAttribute ("location");
			if (attr != null)
				location = attr.getValue ();
			attr = content.getAttribute ("format");
			if (attr != null)
				format = attr.getValue ();
			attr = content.getAttribute ("master");
			if (attr != null)
				master = attr.getValue ();
			
			if (location == null)
				throw new IOException ("manifest invalid. unknown location of entry "
					+ i);
			
			File locFile = new File (baseDir.getAbsolutePath () + File.separator
				+ location);
			if (!locFile.exists ())
				throw new IOException ("archive seems to be corrupt. file " + location
					+ " not found.");
			
			if (format.equals (CombineFormats.getFormatIdentifier ("omex")))
			{
				descr.add (new File (baseDir.getAbsolutePath () + File.separator
					+ location));
				// since that's not a real entry
				continue;
			}
			
			if (format.equals (CombineFormats.getFormatIdentifier ("manifest")))
			{
				// that's this manifest -> skip
				continue;
			}
			
			ArchiveEntry entry = new ArchiveEntry (this, location, format);
			if (master != null && Boolean.parseBoolean (master))
				mainEntry = entry;
			entries.put (location, entry);
		}
		
		// parse all descriptions
		for (File f : descr)
		{
			MetaDataFile.readFile (f, entries);
		}
	}
	
	
	/**
	 * Extracts an Combine archive and reads its contents.
	 * 
	 * @param zipFile
	 *          the zipped archive
	 * @param destination
	 *          the destination
	 * @param deleteOnExit
	 *          if true = delete all files after exit
	 * @return the combine archive
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws JDOMException
	 *           the jDOM exception
	 * @throws ParseException
	 *           the parse exception
	 */
	private static CombineArchive readArchive (File zipFile, File destination,
		boolean deleteOnExit) throws IOException, JDOMException, ParseException
	{
		if (deleteOnExit)
			destination.deleteOnExit ();
		if (!Utils.unpackZip (zipFile, destination, deleteOnExit))
			throw new IOException ("unable to unpack zip file");
		
		return readExtractedArchive (destination);
	}
	
	
	/**
	 * Read an archive. The archive will be extracted to <code>destination</code>
	 * and we won't delete the files after exit (as long as we are able to write
	 * to destination).
	 * Please note, we don't care if the destination directory is empty or not. If
	 * it contains files they might get overwritten and thus lost.
	 * 
	 * @param zipFile
	 *          the zipped Combine archive
	 * @param destination
	 *          the destination directory to unpack the archive to
	 * @return the combine archive
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws JDOMException
	 *           the jDOM exception
	 * @throws ParseException
	 *           the parse exception
	 */
	public static CombineArchive readArchive (File zipFile, File destination)
		throws IOException,
			JDOMException,
			ParseException
	{
		if (!destination.isDirectory () && !destination.mkdirs ())
			return readArchive (zipFile,
				Files.createTempDirectory (CombineArchive.class.getName ()).toFile (),
				true);
		return readArchive (zipFile, destination, false);
	}
	
	
	/**
	 * Read an archive. The archive will be extracted to a temporary directory.
	 * All files will be deleted if the tool exits.
	 * 
	 * @param zipFile
	 *          the zipped Combine archive
	 * @return the combine archive
	 * @throws Exception
	 *           the exception
	 */
	public static CombineArchive readArchive (File zipFile) throws Exception
	{
		return readArchive (zipFile,
			Files.createTempDirectory (CombineArchive.class.getName ()).toFile (),
			true);
	}
	
	
	/**
	 * Read an extracted archive.
	 * 
	 * @param baseDir
	 *          the dir containing the extracted archive
	 * @return the combine archive
	 * @throws IOException
	 * @throws ParseException
	 * @throws JDOMException
	 */
	public static CombineArchive readExtractedArchive (File baseDir)
		throws IOException,
			JDOMException,
			ParseException
	{
		CombineArchive arch = new CombineArchive ();
		arch.baseDir = baseDir;
		File mani = new File (baseDir.getAbsolutePath () + File.separator
			+ MANIFEST_LOCATION);
		if (mani.exists ())
			arch.parseManifest (mani);
		return arch;
	}
	
	
	/**
	 * Export a JSON description of this archive.
	 * 
	 * @return the JSON object
	 */
	@SuppressWarnings("unchecked")
	public JSONObject toJsonDescription ()
	{
		JSONObject descr = new JSONObject ();
		
		for (String s : entries.keySet ())
		{
			JSONObject entryDescr = entries.get (s).toJsonObject ();
			if (entries.get (s) == mainEntry)
				entryDescr.put ("master", "true");
			descr.put (s, entryDescr);
		}
		
		return descr;
	}
}
