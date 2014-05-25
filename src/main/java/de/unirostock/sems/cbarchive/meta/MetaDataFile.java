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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.xml.transform.TransformerException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.Utils;



/**
 * The Class MetaDataFile providing some static functions to read and write meta
 * data files.
 * 
 * @author Martin Scharm
 */
public class MetaDataFile
{
	
	/**
	 * Read a meta data file containing descriptions about <code>entries</code>.
	 * 
	 * @param file
	 *          the file containing meta data
	 * @param entries
	 *          the entries available in this archive
	 * @throws ParseException
	 *           the parse exception
	 * @throws JDOMException
	 *           the jDOM exception
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public static void readFile (File file, HashMap<String, ArchiveEntry> entries)
		throws ParseException,
			JDOMException,
			IOException
	{
		Document doc = Utils.readXmlDocument (file);
		
		List<Element> nl = Utils.getElementsByTagName (doc.getRootElement (),
			"Description", Utils.rdfNS);
		for (int i = 0; i < nl.size (); i++)
		{
			Element current = nl.get (i);
			String about = current.getAttributeValue ("about", Utils.rdfNS);
			if (about.startsWith ("./"))
				about = about.substring (2);
			if (about.startsWith ("/"))
				about = about.substring (1);
			ArchiveEntry currentEntry = null;
			String fragmentIdentifier = null;
			
			// try to find the corresponding entry
			for (ArchiveEntry entry : entries.values ())
			{
				if (about.startsWith (entry.getRelativeName ().substring (2)))
				{
					currentEntry = entry;
					if (about.length () > entry.getRelativeName ().length () - 2
						&& about.charAt (entry.getRelativeName ().length () - 2) == '#')
						fragmentIdentifier = about.substring (entry.getRelativeName ()
							.length () - 1);
				}
			}
			
			if (currentEntry == null)
			{
				LOGGER.warn ("found no entry for description (about=", about, ")");
				continue;
			}
			
			MetaDataObject object = null;
			
			// is that omex?
			object = OmexMetaDataObject.tryToRead (current, currentEntry,
				fragmentIdentifier);
			
			/*
			 * ···································
			 * optional: other meta data formats..
			 * ···································
			 */
			
			if (object == null)
			{
				// is it default?
				object = DefaultMetaDataObject.tryToRead (current, currentEntry,
					fragmentIdentifier);
			}
			
			if (object != null)
			{
				currentEntry.addDescription (object);
			}
			else
				LOGGER.warn ("could not parse description for ", about);
		}
		
	}
	
	
	/**
	 * Write the meta data about <code>entries</code> to files. One meta data file
	 * per entry.
	 * 
	 * @param baseDir
	 *          the base directory of the archive: the files will be stored in
	 *          <code>baseDir/metadata(-[-0-9a-f]+)?.rdf"</code>
	 * @param entries
	 *          the archive entries
	 * @return the list of files that were created
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws TransformerException
	 *           the transformer exception
	 */
	public static List<File> writeFiles (File baseDir,
		HashMap<String, ArchiveEntry> entries)
		throws IOException,
			TransformerException
	{
		List<File> outputs = new ArrayList<File> ();
		
		for (ArchiveEntry e : entries.values ())
		{
			File output = new File (baseDir.getAbsolutePath () + File.separator
				+ "metadata-" + UUID.randomUUID ().toString () + ".rdf");
			while (output.exists ())
				output = new File (baseDir.getAbsolutePath () + File.separator
					+ "metadata-" + UUID.randomUUID ().toString () + ".rdf");
			
			Document xmlDoc = new Document ();
			Element rdf = new Element ("RDF", Utils.rdfNS);
			xmlDoc.addContent (rdf);
			rdf.addNamespaceDeclaration (Utils.dcNS);
			rdf.addNamespaceDeclaration (Utils.vcNS);
			for (MetaDataObject meta : e.getDescriptions ())
			{
				Element Description = new Element ("Description", Utils.rdfNS);
				Description.setAttribute ("about", meta.getAbout (), Utils.rdfNS);
				rdf.addContent (Description);
				meta.injectDescription (Description);
			}
			
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
	 * Write all meta data about <code>entries</code> to a single file.
	 * 
	 * @param baseDir
	 *          the base directory of the archive: the file will be stored in
	 *          <code>baseDir/metadata(-[-0-9a-f]+)?.rdf"</code>
	 * @param entries
	 *          the archive entries
	 * @return the list of files that were created (should be always of size one)
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws TransformerException
	 *           the transformer exception
	 */
	public static List<File> writeFile (File baseDir,
		HashMap<String, ArchiveEntry> entries)
		throws IOException,
			TransformerException
	{
		File output = new File (baseDir.getAbsolutePath () + File.separator
			+ "metadata.rdf");
		while (output.exists ())
			output = new File (baseDir.getAbsolutePath () + File.separator
				+ "metadata-" + UUID.randomUUID ().toString () + ".rdf");
		
		Document xmlDoc = new Document ();
		Element rdf = new Element ("RDF", Utils.rdfNS);
		xmlDoc.addContent (rdf);
		rdf.addNamespaceDeclaration (Utils.dcNS);
		rdf.addNamespaceDeclaration (Utils.vcNS);
		
		for (ArchiveEntry e : entries.values ())
			for (MetaDataObject meta : e.getDescriptions ())
			{
				Element Description = new Element ("Description", Utils.rdfNS);
				Description.setAttribute ("about", meta.getAbout (), Utils.rdfNS);
				rdf.addContent (Description);
				meta.injectDescription (Description);
			}
		
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
}
