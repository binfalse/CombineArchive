/**
 * CombineArchive - a JAVA library to read/write/create/... CombineArchives
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
package de.unirostock.sems.cbarchive.meta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.CombineArchive;
import de.unirostock.sems.cbarchive.CombineArchiveException;
import de.unirostock.sems.cbarchive.Utils;



/**
 * The Class MetaDataFile providing some static functions to read and write meta
 * data files.
 * 
 * @author Martin Scharm
 */
public class MetaDataFile
	extends MetaDataHolder
{
	
	/**
	 * Read a meta data file containing descriptions about the
	 * {@link CombineArchive archive} and/or its {@link ArchiveEntry
	 * entries} given in <code>entries</code>.
	 * 
	 * @param file
	 *          the file containing meta data
	 * @param entries
	 *          the entries available in the corresponding archive
	 * @param archive
	 *          the archive which contains this file
	 * @param metaMetaHolder
	 *          the meta data of meta data
	 * @param metaDataFiles
	 *          the meta data file to evaluate
	 * @param continueOnError
	 *          ignore errors and continue (as far as possible)
	 * @param errors
	 *          the list of occurred errors
	 * @throws ParseException
	 *           the parse exception
	 * @throws JDOMException
	 *           the jDOM exception
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws CombineArchiveException
	 *           the combine archive exception
	 */
	public static void readFile (Path file, HashMap<String, ArchiveEntry> entries,
		CombineArchive archive, MetaDataHolder metaMetaHolder,
		List<Path> metaDataFiles, boolean continueOnError, List<String> errors)
		throws ParseException,
			JDOMException,
			IOException,
			CombineArchiveException
	{
		Document doc = null;
		try
		{
			doc = Utils.readXmlDocument (file);
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
		
		List<Element> nl = Utils.getElementsByTagName (doc.getRootElement (),
			"Description", Utils.rdfNS);
		for (int i = 0; i < nl.size (); i++)
		{
			Element current = nl.get (i);
			String about = current.getAttributeValue ("about", Utils.rdfNS);
			if (about == null)
			{
				LOGGER.error ("meta description " + i + " in " + file
					+ " does not contain an `about` value. so we cannot assign it to an entity.");
				errors.add ("meta description " + i + " in " + file
					+ " does not contain an `about` value. so we cannot assign it to an entity.");
				if (!continueOnError)
					throw new CombineArchiveException ("meta description " + i + " in "
						+ file
						+ " does not contain an `about` value. so we cannot assign it to an entity.");
				continue;
			}
			
			if (about.equals (".") || about.equals ("/"))
			{
				// this entry describes the archive itself
				if (!addMetaToEntry (archive, current, null))
					LOGGER.warn ("could not parse description for ", about);
				continue;
			}
			
			if (about.startsWith ("./"))
				about = about.substring (2);
			while (about.startsWith ("/"))
				about = about.substring (1);
			about = "/" + about;
			MetaDataHolder currentEntry = null;
			String fragmentIdentifier = null;
			
			about = Utils.pathFixer (Paths.get (about).normalize ().toString ());
			
			// try to find the corresponding entry
			for (ArchiveEntry entry : entries.values ())
			{
				if (about.startsWith (entry.getFilePath ())
					&& (about.length () == entry.getFilePath ().length ()
						|| about.charAt (entry.getFilePath ().length ()) == '#'))
				{
					currentEntry = entry;
					if (about.length () > entry.getFilePath ().length ()
						&& about.charAt (entry.getFilePath ().length ()) == '#')
						fragmentIdentifier = about
							.substring (entry.getFilePath ().length () - 1);
					break;
				}
			}
			
			if (currentEntry == null)
			{
				for (Path p : metaDataFiles)
				{
					String path = p.toString ();
					if (about.startsWith (path) && (about.length () == path.length ()
						|| about.charAt (path.length ()) == '#'))
					{
						currentEntry = metaMetaHolder;
						if (about.length () > path.length ()
							&& about.charAt (path.length ()) == '#')
							fragmentIdentifier = about.substring (path.length () - 1);
						break;
					}
				}
			}
			
			if (currentEntry == null)
			{
				LOGGER.error ("found no entry for description ", i, " in ", file,
					" (about=", about, ").");
				errors.add ("found no entry for description " + i + " in " + file
					+ " (about=" + about + ").");
				if (!continueOnError)
					throw new CombineArchiveException ("found no entry for description "
						+ i + " in " + file + " (about=" + about + ").");
				continue;
			}
			
			if (!addMetaToEntry (currentEntry, current, fragmentIdentifier))
				LOGGER.warn ("could not parse description for ", about);
		}
	}
	
	
	/**
	 * Adds all descriptions of a file to a single entry.
	 * 
	 * @param file
	 *          the file containing the meta data
	 * @param entry
	 *          the entry in the archive
	 * @return the number of descriptions added to <code>entry</code>
	 * @throws JDOMException
	 *           the jDOM exception
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public static int addAllMetaToEntry (Path file, ArchiveEntry entry)
		throws JDOMException,
			IOException
	{
		int added = 0;
		Document doc = Utils.readXmlDocument (file);
		
		List<Element> nl = Utils.getElementsByTagName (doc.getRootElement (),
			"Description", Utils.rdfNS);
		for (int i = 0; i < nl.size (); i++)
		{
			Element current = nl.get (i);
			String fragmentIdentifier = null;
			String about = current.getAttributeValue ("about", Utils.rdfNS);
			if (about != null)
			{
				// is there a fragment identifier
				int p = about.indexOf ("#");
				if (p >= 0 && about.length () > p + 1)
					fragmentIdentifier = about.substring (p + 1);
			}
			
			if (!addMetaToEntry (entry, current, fragmentIdentifier))
				LOGGER.warn ("could not parse description for ", about);
			else
				added++;
		}
		return added;
	}
	
	
	/**
	 * Associates some meta data to a file.
	 * 
	 * This function won't associate the same meta data twice to the same object.
	 * 
	 * @param entity
	 *          the entity that is described by <code>subtree</code>
	 * @param subtree
	 *          the current xml subtree which describes <code>entry</code>
	 * @param fragmentIdentifier
	 *          the fragment identifier
	 * @return true, if successful
	 */
	private static boolean addMetaToEntry (MetaDataHolder entity, Element subtree,
		String fragmentIdentifier)
	{
		if (entity == null)
			return false;
		
		MetaDataObject object = null;
		
		// is that omex?
		object = OmexMetaDataObject.tryToRead (subtree);
		
		/*
		 * ···································
		 * optional: other meta data formats..
		 * ···································
		 */
		
		if (object == null)
		{
			// is it default?
			object = DefaultMetaDataObject.tryToRead (subtree);
		}
		
		if (object != null)
		{
			object.setAbout (entity, fragmentIdentifier);
			
			// do not add the same meta twice...
			for (MetaDataObject obj : entity.getDescriptions ())
				if (object.equals (obj))
					return true;
				
			entity.addDescription (fragmentIdentifier, object);
		}
		else
			return false;
		
		return true;
	}
	
	
	/**
	 * Write the meta data about the {@link CombineArchive archive} and its
	 * {@link ArchiveEntry entries} given in <code>archive</code> and
	 * <code>entries</code> to a single meta data files.
	 * 
	 * <p>
	 * This method will create one meta data file per entry. Meta data files will
	 * be named <code>baseDir/metadata(-[-0-9a-f]+)?.rdf</code>. See
	 * {@link #writeFile(File,HashMap,CombineArchive,MetaDataHolder)} if you want
	 * to
	 * store all meta data in a single file.
	 * </p>
	 * 
	 * @param baseDir
	 *          the base directory to store the files
	 * @param entries
	 *          the archive entries
	 * @param archive
	 *          the archive which will contain the files
	 * @param metaMetaHolder
	 *          the meta data of meta data
	 * @return the list of files that were created
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws TransformerException
	 *           the transformer exception
	 */
	public static List<File> writeFiles (File baseDir,
		HashMap<String, ArchiveEntry> entries, CombineArchive archive,
		MetaDataHolder metaMetaHolder) throws IOException, TransformerException
	{
		List<File> outputs = new ArrayList<File> ();
		
		// archive itself
		File output = getMetaOutputFile (baseDir);
		
		Document xmlDoc = new Document ();
		Element rdf = new Element ("RDF", Utils.rdfNS);
		xmlDoc.addContent (rdf);
		rdf.addNamespaceDeclaration (Utils.dcNS);
		rdf.addNamespaceDeclaration (Utils.vcNS);
		
		exportMetaData (archive, rdf);
		
		// meta of meta
		exportMetaData (metaMetaHolder, rdf);
		
		try (BufferedWriter bw = new BufferedWriter (new FileWriter (output)))
		{
			bw.write (Utils.prettyPrintDocument (xmlDoc));
		}
		catch (IOException | TransformerException e)
		{
			LOGGER.error (e, "cannot write omex descriptions to ", output);
			throw e;
		}
		outputs.add (output);
		
		// all entries
		for (ArchiveEntry e : entries.values ())
		{
			output = getMetaOutputFile (baseDir);
			
			xmlDoc = new Document ();
			rdf = new Element ("RDF", Utils.rdfNS);
			xmlDoc.addContent (rdf);
			rdf.addNamespaceDeclaration (Utils.dcNS);
			rdf.addNamespaceDeclaration (Utils.vcNS);
			exportMetaData (e, rdf);
			
			try (BufferedWriter bw = new BufferedWriter (new FileWriter (output)))
			{
				bw.write (Utils.prettyPrintDocument (xmlDoc));
			}
			catch (IOException | TransformerException ex)
			{
				LOGGER.error (ex, "cannot write omex descriptions to ", output);
				throw ex;
			}
			
			outputs.add (output);
		}
		
		return outputs;
	}
	
	
	/**
	 * Write the meta data about the {@link CombineArchive archive} and its
	 * {@link ArchiveEntry entries} given in <code>archive</code> and
	 * <code>entries</code> to a single meta data file.
	 * 
	 * <p>
	 * This method will create one meta data file for all description. Thus, the
	 * returned list of files will be of size one. The meta data file will be
	 * named <code>baseDir/metadata(-[-0-9a-f]+)?.rdf</code>. See
	 * {@link #writeFiles(File,HashMap,CombineArchive,MetaDataHolder)} if you want
	 * to store the
	 * meta data in a multiple files, one for each entry.
	 * </p>
	 * 
	 * @param baseDir
	 *          the base directory to store the file
	 * @param entries
	 *          the archive entries
	 * @param archive
	 *          the archive which will contain the files
	 * @param metaMetaHolder
	 *          the meta data of meta data
	 * @return the list of files th
	 *         the meta data of meta dataat were created (should be always of size
	 *         one)
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws TransformerException
	 *           the transformer exception
	 */
	public static List<File> writeFile (File baseDir,
		HashMap<String, ArchiveEntry> entries, CombineArchive archive,
		MetaDataHolder metaMetaHolder) throws IOException, TransformerException
	{
		File output = getMetaOutputFile (baseDir);
		
		Document xmlDoc = new Document ();
		Element rdf = new Element ("RDF", Utils.rdfNS);
		xmlDoc.addContent (rdf);
		rdf.addNamespaceDeclaration (Utils.dcNS);
		rdf.addNamespaceDeclaration (Utils.vcNS);
		
		// archive itself
		exportMetaData (archive, rdf);
		
		// meta of meta
		exportMetaData (metaMetaHolder, rdf);
		
		// all entries
		for (ArchiveEntry e : entries.values ())
			exportMetaData (e, rdf);
		
		try (BufferedWriter bw = new BufferedWriter (new FileWriter (output)))
		{
			bw.write (Utils.prettyPrintDocument (xmlDoc));
		}
		catch (IOException | TransformerException e)
		{
			LOGGER.error (e, "cannot write omex descriptions to ", output);
			throw e;
		}
		
		List<File> outputs = new ArrayList<File> ();
		outputs.add (output);
		return outputs;
	}
	
	
	/**
	 * Find a file to write the meta data to.
	 * 
	 * @param baseDir
	 *          the base directory
	 * @return the output file
	 */
	private static File getMetaOutputFile (File baseDir)
	{
		File output = new File (
			baseDir.getAbsolutePath () + File.separator + "metadata.rdf");
		int it = 0;
		while (output.exists ())
			output = new File (baseDir.getAbsolutePath () + File.separator
				+ "metadata-" + ++it + ".rdf");
		
		return output;
	}
	
	
	/**
	 * Export the meta data of an entity.
	 * 
	 * @param entity
	 *          the entity
	 * @param rdf
	 *          the RDF node which will host the description
	 */
	private static void exportMetaData (MetaDataHolder entity, Element rdf)
	{
		for (MetaDataObject meta : entity.getDescriptions ())
		{
			Element Description = new Element ("Description", Utils.rdfNS);
			Description.setAttribute ("about", meta.getAbout (), Utils.rdfNS);
			rdf.addContent (Description);
			meta.injectDescription (Description);
		}
	}
	
	
	@Override
	public String getEntityPath ()
	{
		return "/metadata.rdf";
	}
}
