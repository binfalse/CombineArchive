/**
 * CombineArchive - a JAVA library to read/write/create/.. CombineArchives
 * Copyright (C) 2013 Martin Scharm - http://binfalse.de/contact/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.json.simple.JSONObject;

import de.binfalse.bflog.LOGGER;



/**
 * The Class CombineArchive to create/read/manipulate/store etc.
 * CombineArchives.
 * 
 * 
 * @author martin scharm
 */
public class CombineArchive
{
	
	public static final String MANIFEST_LOCATION = "manifest.xml";
	
	/** The OMEX namespace. */
	public static final Namespace					omexNs	= Namespace
																									.getNamespace ("http://identifiers.org/combine.specifications/omex-manifest");
	
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
	 * @param temporaryDirectory the directory containing the extracted archive
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws IOException 
	 */
	private CombineArchive (File temporaryDirectory) throws IOException, JDOMException, ParseException
	{
		entries = new HashMap<String, ArchiveEntry> ();
		baseDir = temporaryDirectory;
		File mani = new File (baseDir.getAbsolutePath () + File.separatorChar + MANIFEST_LOCATION);
		if (mani.exists ())
			parseManifest (mani);
	}
	
	
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
	 * The current version of the concerning file will be copied,
	 * so upcoming modifications of the source file won't have affect the version
	 * in our archive.
	 * The path of this file in the archive will be the path <code>file</code>
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
	 */
	public ArchiveEntry addEntry (File baseDir, File file, String format,
		OmexDescription description, boolean mainEntry) throws IOException
	{
		if (!file.exists ())
			throw new IOException ("file does not exist.");
		
		if (!file.getAbsolutePath ().contains (baseDir.getAbsolutePath ()))
			throw new IOException ("file must be in basedir.");
		
		String localName = file.getAbsolutePath ().replace (
			baseDir.getAbsolutePath (), "");
		if (localName.equals ("/manifest.xml"))
			throw new IllegalArgumentException (
				"it's not allowed to name a file manifest.xml");
		
		File destination = new File (this.baseDir.getAbsolutePath () + localName);
		destination.getParentFile ().mkdirs ();
		destination.getParentFile ().deleteOnExit ();
		
		Files.copy (file.toPath (), destination.toPath (),
			java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		destination.deleteOnExit ();
		
		ArchiveEntry entry = new ArchiveEntry (this, "." + localName, format,
			description);
		
		entries.put (entry.getRelativeName (), entry);
		
		if (mainEntry)
		{
			LOGGER.error ("setting main entry:");
			this.mainEntry = entry;
		}
		
		return entry;
	}
	
	
	/**
	 * Adds an entry to the archive.
	 * The current version of the concerning file will be copied,
	 * so upcoming modifications of the source file won't have affect the version
	 * in our archive.
	 * The path of this file in the archive will be the path <code>file</code>
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
	 */
	public ArchiveEntry addEntry (File baseDir, File file, String format,
		OmexDescription description) throws IOException
	{
		if (!file.exists ())
			throw new IOException ("file does not exist.");
		
		if (!file.getAbsolutePath ().contains (baseDir.getAbsolutePath ()))
			throw new IOException ("file must be in basedir.");
		
		String localName = file.getAbsolutePath ().replace (
			baseDir.getAbsolutePath (), "");
		if (localName.equals ("/manifest.xml"))
			throw new IllegalArgumentException (
				"it's not allowed to name a file manifest.xml");
		
		File destination = new File (this.baseDir.getAbsolutePath () + localName);
		destination.getParentFile ().mkdirs ();
		destination.getParentFile ().deleteOnExit ();
		
		Files.copy (file.toPath (), destination.toPath (),
			java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		destination.deleteOnExit ();
		
		ArchiveEntry entry = new ArchiveEntry (this, "." + localName, format,
			description);
		
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
	private Element createManifestEntry (String location, String format, boolean mainEntry)
	{
		Element element = new Element ("content", omexNs);
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
	private List<File> writeManifest () throws IOException, TransformerException
	{
		List<File> fileList = new ArrayList<File> ();
		File manifestFile = new File (baseDir.getAbsolutePath () + File.separator
			+ MANIFEST_LOCATION);
		fileList.add (manifestFile);
		
		Document doc = new Document ();
		Element root = new Element ("omexManifest", omexNs);
		doc.addContent (root);
		
		root.addContent (createManifestEntry ("./" + MANIFEST_LOCATION, omexNs.getURI (), false));
		
		Vector<OmexDescription> descriptions = new Vector<OmexDescription> ();
		for (ArchiveEntry e : entries.values ())
		{
			root.addContent (createManifestEntry (e.getRelativeName (),
				e.getFormat (), e == mainEntry));
			fileList.add (new File (baseDir.getAbsolutePath () + File.separator
				+ e.getRelativeName ()));
			if (e.getDescription () != null)
				descriptions.add (e.getDescription ());
		}
		
		File descr = OmexDescriptionFile.writeFile (descriptions, baseDir);
		root.addContent (createManifestEntry ("."
			+ descr.getAbsolutePath ().replace (baseDir.getAbsolutePath (), ""),
			"http://identifiers.org/combine.specifications/omex-metadata", false));
		fileList.add (descr);
		
		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter (new FileWriter (manifestFile));
			bw.write (Utils.prettyPrintDocument (doc));
		}
		catch (IOException | TransformerException e)
		{
			e.printStackTrace ();
			LOGGER.error ("cannot write manifest file to " + manifestFile);
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
		List<File> fileList = writeManifest ();
		
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
		Vector<File> descr = new Vector<File> ();
		List<Element> nl = Utils.getElementsByTagName (doc.getRootElement (),
			"content", omexNs);
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
			
			// System.out.println (">>" + location + "   " + format + "   " + master);
			if (format.equals (CombineFormats.getFormatIdentifier ("omex")))
			{
				// descriptions.addAll (OmexDescriptionFile.readFile (new File
				// (baseDir.getAbsolutePath () + File.separator + location), false));
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
			
			// System.out.println ("<<" + location + "   " + format + "   " + master);
			
			ArchiveEntry entry = new ArchiveEntry (this, location, format, null);
			if (master != null && Boolean.parseBoolean (master))
				mainEntry = entry;
			entries.put (location, entry);
		}
		
		// parse all descriptions
		for (File f : descr)
		{
			Vector<OmexDescription> des = OmexDescriptionFile.readFile (f, false);
			for (OmexDescription d : des)
			{
				ArchiveEntry entry = entries.get (d.getAbout ());
				if (entry != null)
					entry.setDescription (d);
			}
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
	 * @param zipFile
	 *          the zipped Combine archive
	 * @return the combine archive
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 */
	public static CombineArchive readExtractedArchive (File baseDir) throws IOException, JDOMException, ParseException
	{
		CombineArchive arch = new CombineArchive ();
		arch.baseDir = baseDir;
		File mani = new File (baseDir.getAbsolutePath ()
			+ File.separator + MANIFEST_LOCATION);
		if (mani.exists ())
			arch.parseManifest (mani);
		return arch;
	}
	
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
