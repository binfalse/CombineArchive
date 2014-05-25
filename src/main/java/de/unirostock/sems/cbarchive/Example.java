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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.jdom2.Element;
import org.jdom2.JDOMException;

import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;
import de.unirostock.sems.cbarchive.meta.MetaDataObject;
import de.unirostock.sems.cbarchive.meta.OmexMetaDataObject;
import de.unirostock.sems.cbarchive.meta.omex.OmexDescription;
import de.unirostock.sems.cbarchive.meta.omex.VCard;



/**
 * @author martin
 * 
 */
public class Example
{
	/*
	 * execute these commands before running the example:
	 * 
	 * mkdir -p /tmp/base/path/subdir
	 * touch /tmp/base/path/{file.sbml,subdir/file.cellml}
	 * 
	 * 
	 * the directory tree in /tmp/base should then look like:
	 * 
	 * /tmp/base
	 * /tmp/base/path
	 * /tmp/base/path/subdir
	 * /tmp/base/path/subdir/file.cellml
	 * /tmp/base/path/file.sbml
	 * 
	 */
	
	public static void createExample () throws IOException, TransformerException
	{
		// let's create some 'creators' -> meta data.
		Vector<VCard> creators = new Vector<VCard> ();
		creators.add (new VCard ("Scharm", "Martin",
			"martin.scharm@uni-rostock.de", "University of Rostock"));
		creators.add (new VCard ("Waltemath", "Dagmar",
			"dagmar.waltemath@uni-rostock.de", "University of Rostock"));
		
		// create a new empty archive
		CombineArchive ca = new CombineArchive ();
		
		// add an entry to the archive
		ArchiveEntry SBMLFile = ca.addEntry (
			// this command will add /tmp/base/path/file.sbml to the root of our archive
			// (because base path in that case is /tmp/base/path/). thus we'll see
			// /file.sbml in our archive.
			new File ("/tmp/base/path"),
			new File ("/tmp/base/path/file.sbml"),
			// format is http://identifiers.org/combine.specifications/sbml - here i use
			// the class CombineFormats to get the SBML identifier
			CombineFormats.getFormatIdentifier ("sbml"));

		// we'll add some description. creators as defined above
		SBMLFile.addDescription (new OmexMetaDataObject (SBMLFile, new OmexDescription (creators, new Date ())));
		
		// add another entry to the archive
		ArchiveEntry CellMLFile = ca.addEntry (
			// this time we add /tmp/base/path/subdir/file.cellml to the /subdir of our
			// archive (because base path again is /tmp/base/path/). thus we'll see
			// /subdir/file.cellml in our archive.
			new File ("/tmp/base/path"),
			new File ("/tmp/base/path/subdir/file.cellml"),
			// format is http://identifiers.org/combine.specifications/cellml.1.0 -
			// again using CombineFormats to get the correct identifier
			CombineFormats.getFormatIdentifier ("cellml.1.0"));
		
		// same description, but feel free to define different authors.
		// but this time we describe the fragment 'someFragment' inside the file
		CellMLFile.addDescription (new OmexMetaDataObject (CellMLFile, new OmexDescription (creators, new Date ())));
		
		// just for fun: add some other meta data:
		Element metaParent = new Element ("stuff");
		Element metaElement = new Element ("myMetaElement");
		metaElement.setAttribute ("someAttribute", "someValue");
		metaElement.addContent ("some content");
		metaParent.addContent (metaElement);
		CellMLFile.addDescription (new DefaultMetaDataObject (CellMLFile, "someFragment", metaParent));
		
		// write the archive to /tmp/testArchive.zip
		ca.exportArchive (new File ("/tmp/testArchive.zip"));
	}
	
	
	public static void readExample ()
		throws IOException,
			JDOMException,
			ParseException
	{
		File archiveFile = new File ("/tmp/testArchive.zip");
		File destination = new File ("/tmp/myDestination");
		
		// if you don't provide a destination we'll unpack the archive to a
		// temporary directory.
		// in that case all files will be deleted if the vm exits.
		// if destination is given we won't delete anything, so you can work with
		// the unpacked archive
		CombineArchive ca = CombineArchive.readArchive (archiveFile, destination);
		
		// iterate over all entries in the archive
		for (ArchiveEntry entry : ca.getEntries ())
		{
			// display some information about the archive
			System.out.println (">>> file name in archive: " + entry.getRelativeName () + "  -- apparently of format: " + entry.getFormat ());
			// read entry.getFile () in your application to get the contents
			System.out.println ("file is available in: " + entry.getFile ().getAbsolutePath ());
			
			// read the descriptions
			for (MetaDataObject description : entry.getDescriptions ())
			{
				System.out.println ("+ found some meta data about " + description.getAbout ());
				if (description instanceof OmexMetaDataObject)
				{
					OmexDescription desc = ((OmexMetaDataObject) description).getOmexDescription ();
					System.out.println ("file was created: " + desc.getCreated ());
					
					// who's created the archive?
					VCard firstCreator = desc.getCreators ().elementAt (0);
					System.out.println ("file's first author: "
						+ firstCreator.getGivenName () + " " + firstCreator.getFamilyName ());
				}
				else
				{
					System.out.println ("found some meta data fo type '" + description.getClass ().getName () + "' that we do not respect in this small example.");
				}
			}
		}
	}
	
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main (String[] args) throws Exception
	{
		// create an archive
		createExample ();
		
		// read the archive
		readExample ();
	}
	
}
