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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
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
 * This example will create the following files/directories:
 * (you may want to delete them afterwards)
 * 
 * <pre>
 * /tmp/testArchive.zip
 * /tmp/myDestination
 * /tmp/myExtractedEntry
 * </pre>
 * 
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
	 * @throws CombineArchiveException
	 * @throws ParseException
	 * @throws JDOMException
	 */
	public static void createExample ()
		throws IOException,
			TransformerException,
			JDOMException,
			ParseException,
			CombineArchiveException
	{
		// let's create some 'creators' -> meta data.
		List<VCard> creators = new ArrayList<VCard> ();
		creators.add (new VCard ("Scharm", "Martin",
			"martin.scharm@uni-rostock.de", "University of Rostock"));
		creators.add (new VCard ("Waltemath", "Dagmar",
			"dagmar.waltemath@uni-rostock.de", "University of Rostock"));
		
		// create a new empty archive. since /tmp/testArchive.zip does not exist
		// it will be created. (if it exists we'd try to read it)
		CombineArchive ca = new CombineArchive (new File ("/tmp/testArchive.zip"));
		
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
		
		// we'll also add some OMEX description. creators as defined above
		SBMLFile.addDescription (new OmexMetaDataObject (new OmexDescription (
			creators, new Date ())));
		
		// add another entry to the archive
		ArchiveEntry CellMLFile = ca.addEntry (
		// this time we add /tmp/base/path/subdir/file.cellml
			new File ("/tmp/base/path/subdir/file.cellml"),
			// and we'd like to see it in /subdir/file.cellml
			"/subdir/file.cellml",
			// format is http://identifiers.org/combine.specifications/cellml.1.0 -
			// again using CombineFormats to get the correct identifier
			CombineFormats.getFormatIdentifier ("cellml.1.0"),
			// in addition: set this entry as main entry in this archive
			true);
		
		// same description, but feel free to define different authors.
		CellMLFile.addDescription (new OmexMetaDataObject (new OmexDescription (
			creators, new Date ())));
		
		// just for fun: add some other meta data:
		Element metaParent = new Element ("stuff");
		Element metaElement = new Element ("myMetaElement");
		metaElement.setAttribute ("someAttribute", "someValue");
		metaElement.addContent ("some content");
		metaParent.addContent (metaElement);
		// but this time we describe the fragment 'someFragment' inside the model
		CellMLFile.addDescription ("someFragment", new DefaultMetaDataObject (
			metaParent));
		
		// finalise the archive (write manifest and meta data) and close it
		ca.pack ();
		ca.close ();
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
	 * @throws CombineArchiveException
	 */
	public static void readExample ()
		throws IOException,
			JDOMException,
			ParseException,
			CombineArchiveException
	{
		File archiveFile = new File ("/tmp/testArchive.zip");
		File destination = new File ("/tmp/myDestination");
		File tmpEntryExtract = new File ("/tmp/myExtractedEntry");
		
		// read the archive stored in `archiveFile`
		CombineArchive ca = new CombineArchive (archiveFile);
		
		// iterate over all entries in the archive
		for (ArchiveEntry entry : ca.getEntries ())
		{
			// display some information about the archive
			System.out.println (">>> file name in archive: " + entry.getFileName ()
				+ "  -- apparently of format: " + entry.getFormat ());
			
			// extract the file to `tmpEntryExtract`
			System.out.println ("file can be read from: "
				+ entry.extractFile (tmpEntryExtract).getAbsolutePath ());
			
			// if you just want to read it, you do not need to extract it
			// instead call for an InputStream:
			InputStream myReader = Files.newInputStream (entry.getPath (),
				StandardOpenOption.READ);
			// but here we do not use it...
			myReader.close ();
			
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
					System.out.println ("found some meta data of type '"
						+ description.getClass ().getName ()
						+ "' that we do not respect in this small example.");
				}
				// No matter what type of description that is, you can always
				// retrieve the XML subtree rooting the meta data using
				Element meta = description.getXmlDescription ();
				// the descriptions are encoded in
				meta.getChildren ();
			}
		}
		
		// ok, last but not least we can extract the whole archive to our disk:
		ca.extractTo (destination);
		// and now that we're finished: close the archive
		ca.close ();
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
