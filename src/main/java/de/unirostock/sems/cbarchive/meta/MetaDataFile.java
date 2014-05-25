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
import de.unirostock.sems.cbarchive.meta.omex.OmexDescription;


public class MetaDataFile
{

	public static void readFile (File file, HashMap<String, ArchiveEntry>	entries)
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
					if (about.length () > entry.getRelativeName ().length () - 2 && about.charAt (entry.getRelativeName ().length () - 2) == '#')
						fragmentIdentifier = about.substring (entry.getRelativeName ().length () - 1);
				}
			}
			
			if (currentEntry == null)
			{
				LOGGER.warn ("found no entry for description (about=", about, ")");
				continue;
			}
			
			
			MetaDataObject object = null;
			
			// is that omex?
			object = OmexMetaDataObject.tryToRead (current, currentEntry, fragmentIdentifier);
			
			// optional: other meta data formats..
			
			if (object == null)
			{
				// is it default?
				object = DefaultMetaDataObject.tryToRead (current, currentEntry, fragmentIdentifier);
			}
			
			if (object != null)
			{
				currentEntry.addDescription (object);
			}
			else
				LOGGER.warn ("could not parse description for ", about);
		}
		
	}

	public static List<File> writeFiles (File baseDir,
		HashMap<String, ArchiveEntry> entries) throws IOException, TransformerException
	{
		List<File> outputs = new ArrayList<File> ();
		
		for (ArchiveEntry e : entries.values ())
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

	public static List<File> writeFile (File baseDir,
		HashMap<String, ArchiveEntry> entries) throws IOException, TransformerException
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
