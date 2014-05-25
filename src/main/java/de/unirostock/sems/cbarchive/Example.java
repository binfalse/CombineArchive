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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.jdom2.Element;
import org.jdom2.JDOMException;

import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;
import de.unirostock.sems.cbarchive.meta.MetaDataObject;
import de.unirostock.sems.cbarchive.meta.OmexMetaDataObject;
import de.unirostock.sems.cbarchive.meta.omex.OmexDescription;
import de.unirostock.sems.cbarchive.meta.omex.VCard;



/**
 * execute these commands before running the example:
 * 
 * <pre>
 * mkdir -p /tmp/base/path/subdir
 * touch /tmp/base/path/{file.sbml,subdir/file.cellml}
 * </pre>
 * 
 * 
 * the directory tree in /tmp/base should then look like:
 * 
 * <pre>
 * /tmp/base
 * /tmp/base/path
 * /tmp/base/path/subdir
 * /tmp/base/path/subdir/file.cellml
 * /tmp/base/path/file.sbml
 * </pre>
 * 
 * @author Martin Scharm
 * 
 */
public class Example
{
	
	/**
	 * Creates an example archive in <code>/tmp/testArchive.zip</code>.
	 * 
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws TransformerException
	 *           the transformer exception
	 */
	public static void createExample () throws IOException, TransformerException
	{
		// let's create some 'creators' -> meta data.
		List<VCard> creators = new ArrayList<VCard> ();
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
			// format is http://identifiers.org/combine.specifications/sbml - here i
			// use the class CombineFormats to get the SBML identifier
			CombineFormats.getFormatIdentifier ("sbml"));
		
		// we'll add some description. creators as defined above
		SBMLFile.addDescription (new OmexMetaDataObject (SBMLFile,
			new OmexDescription (creators, new Date ())));
		
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
		CellMLFile.addDescription (new OmexMetaDataObject (CellMLFile,
			new OmexDescription (creators, new Date ())));
		
		// just for fun: add some other meta data:
		Element metaParent = new Element ("stuff");
		Element metaElement = new Element ("myMetaElement");
		metaElement.setAttribute ("someAttribute", "someValue");
		metaElement.addContent ("some content");
		metaParent.addContent (metaElement);
		// but this time we describe the fragment 'someFragment' inside the model
		CellMLFile.addDescription (new DefaultMetaDataObject (CellMLFile,
			"someFragment", metaParent));
		
		// write the archive to /tmp/testArchive.zip
		ca.exportArchive (new File ("/tmp/testArchive.zip"));
	}
	
	
	/**
	 * Reads the example in <code>/tmp/testArchive.zip</code>.
	 * 
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws JDOMException
	 *           the jDOM exception
	 * @throws ParseException
	 *           the parse exception
	 */
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
			System.out.println (">>> file name in archive: "
				+ entry.getRelativeName () + "  -- apparently of format: "
				+ entry.getFormat ());
			// read entry.getFile () in your application to get the contents
			System.out.println ("file is available in: "
				+ entry.getFile ().getAbsolutePath ());
			
			// read the descriptions
			for (MetaDataObject description : entry.getDescriptions ())
			{
				System.out.println ("+ found some meta data about "
					+ description.getAbout ());
				if (description instanceof OmexMetaDataObject)
				{
					OmexDescription desc = ((OmexMetaDataObject) description)
						.getOmexDescription ();
					
					// when was it created?
					System.out.println ("file was created: " + desc.getCreated ());
					
					// who's created the archive?
					VCard firstCreator = desc.getCreators ().get (0);
					System.out.println ("file's first author: "
						+ firstCreator.getGivenName () + " "
						+ firstCreator.getFamilyName ());
				}
				else
				{
					System.out.println ("found some meta data fo type '"
						+ description.getClass ().getName ()
						+ "' that we do not respect in this small example.");
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
