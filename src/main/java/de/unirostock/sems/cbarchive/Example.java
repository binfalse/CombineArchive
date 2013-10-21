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

import org.jdom2.JDOMException;



/**
 * @author martin
 * 
 */
public class Example
{
	
	public static void createExample () throws IOException, TransformerException
	{
		// let's create some 'creators'.
		Vector<VCard> creators = new Vector<VCard> ();
		creators.add (new VCard ("Scharm", "Martin",
			"martin.scharm@uni-rostock.de", "University of Rostock"));
		creators.add (new VCard ("Waltemath", "Dagmar",
			"dagmar.waltemath@uni-rostock.de", "University of Rostock"));
		
		// create a new empty archive
		CombineArchive ca = new CombineArchive ();
		
		// add an entry to the archive
		ca.addEntry (
			// this command will add /tmp/base/path/file.sbml to the root of our archive
			// (because base path in that case is /tmp/base/path/). thus we'll see
			// /file.sbml in our archive.
			new File ("/tmp/base/path"),
			new File ("/tmp/base/path/file.sbml"),
			// format is http://identifiers.org/combine.specifications/sbml - here i use
			// the class CombineFormats to get the SBML identifier
			CombineFormats.getFormatIdentifier ("sbml"),
			// we add it together with a description. creators as defined above
			new OmexDescription (creators, new Date ()));
		
		// add another entry to the archive
		ca.addEntry (
			// this time we add /tmp/base/path/subdir/file.cellml to the /subdir of our
			// archive (because base path again is /tmp/base/path/). thus we'll see
			// /subdir/file.cellml in our archive.
			new File ("/tmp/base/path"),
			new File ("/tmp/base/path/subdir/file.cellml"),
			// format is http://identifiers.org/combine.specifications/cellml.1.0 -
			// again using CombineFormats to get the correct identifier
			CombineFormats.getFormatIdentifier ("cellml.1.0"),
			// same description, but feel free to define different authors.
			new OmexDescription (creators, new Date ()));
		
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
		CombineArchive ca2 = CombineArchive.readArchive (archiveFile, destination);
		
		// iterate over all entries in the archive
		for (ArchiveEntry entry : ca2.getEntries ())
		{
			// display some information about the archive
			System.out.println ("file name in archive: " + entry.getRelativeName ());
			System.out.println ("file is available in: " + entry.getFile ().getAbsolutePath ());
			
			// read the description
			OmexDescription description = entry.getDescription ();
			System.out.println ("file was created: " + description.getCreated ());
			
			// who's created the archive?
			VCard firstCreator = description.getCreators ().elementAt (0);
			System.out.println ("file's first creator: "
				+ firstCreator.getGivenName () + " " + firstCreator.getFamilyName ());
		}
	}
	
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main (String[] args) throws Exception
	{
		createExample ();
		
		readExample ();
	}
	
}
