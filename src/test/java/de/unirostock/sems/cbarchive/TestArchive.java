/**
 * 
 */
package de.unirostock.sems.cbarchive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;
import de.unirostock.sems.cbarchive.meta.OmexMetaDataObject;
import de.unirostock.sems.cbarchive.meta.omex.VCard;
import de.unirostock.sems.cbarchive.meta.omex.OmexDescription;;


/**
 * The Class TestWeb.
 *
 * @author Martin Scharm
 */
public class TestArchive
{
	
	private static List<File> testFiles = new ArrayList<File> ();
	
	/**
	 * create some test files
	 * @throws IOException 
	 */
	@BeforeClass
	public static void initialize () throws IOException
	{
		// lets create 6 test files, the first one will serve as an archive
		for (int i = 0; i < 6; i++)
			testFiles.add (File.createTempFile ("combineArchive", "test" + i));
		
	}
	/**
	 * delete test files
	 */
	@AfterClass
	public static void destroy ()
	{
		for (File f : testFiles)
			try
			{
				Utils.delete (f);
			}
			catch (IOException e)
			{
				LOGGER.warn (e, "could not delete ", f);
			}
	}

	/**
	 * Test local files by URI -> file:/path/to/file.
	 * @throws CombineArchiveException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws IOException 
	 * @throws TransformerException 
	 */
	@Test
	public void someRandomTests () throws IOException, JDOMException, ParseException, CombineArchiveException, TransformerException
	{
		// lets create the archive
		testFiles.get (0).delete ();
		CombineArchive ca = new CombineArchive (testFiles.get (0));
		
		List<ArchiveEntry> entries = new ArrayList<ArchiveEntry> ();
		for (int i = 1; i < testFiles.size (); i++)
			entries.add (ca.addEntry (testFiles.get (i), "/sub" + i + "/file" + i + ".ext", CombineFormats.getFormatIdentifier ("sbml")));
		
		assertEquals ("unexpected number of entries in archive after creation", testFiles.size () - 1, ca.getNumEntries ());
		
		// lets remove some entries
		assertTrue ("unable to remove /sub3/file3.ext", ca.removeEntry ("/sub3/file3.ext"));
		assertEquals ("unexpected number of entries in archive after deleting number 3", testFiles.size () - 2, ca.getNumEntries ());
		assertFalse ("removed an entry that doesn't exist!? /sub3/file3.ext was deleted before.", ca.removeEntry ("/sub3/file3.ext"));
		assertFalse ("removed an entry that doesn't exist!? /sub2/file4.ext", ca.removeEntry ("/sub2/file4.ext"));
		
		assertTrue ("unable to remove ./sub4/file4.ext", ca.removeEntry ("./sub4/file4.ext"));
		assertEquals ("unexpected number of entries in archive after deleting number 4", testFiles.size () - 3, ca.getNumEntries ());

		ca.removeEntry (entries.get (0));
		assertEquals ("unexpected number of entries in archive after deleting the first inserted entry", testFiles.size () - 4, ca.getNumEntries ());
		
		// lets re-add all and make sure we do not have doubles..
		entries = new ArrayList<ArchiveEntry> ();
		for (int i = 1; i < testFiles.size (); i++)
			entries.add (ca.addEntry (testFiles.get (i), "/sub" + i + "/file" + i + ".ext", CombineFormats.getFormatIdentifier ("sbml")));
		assertEquals ("unexpected number of entries in archive after resubmitting all files", testFiles.size () - 1, ca.getNumEntries ());
		
		// we should still be able to add known file under a different name
		for (int i = 1; i < testFiles.size (); i++)
			entries.add (ca.addEntry (testFiles.get (i), "./file" + i + ".ext", CombineFormats.getFormatIdentifier ("sbml")));
		assertEquals ("unexpected number of entries in archive after adding all files under different names", 2 * (testFiles.size () - 1), ca.getNumEntries ());
		
		// and this should overwrite our last commit
		for (int i = 1; i < testFiles.size (); i++)
			entries.add (ca.addEntry (testFiles.get (i), "file" + i + ".ext", CombineFormats.getFormatIdentifier ("sbml")));
		assertEquals ("unexpected number of entries in archive after submitting last commit with same path", 2 * (testFiles.size () - 1), ca.getNumEntries ());
		
		// and lets remove top-level entries
		assertTrue ("unable to remove /file3.ext", ca.removeEntry ("/file3.ext"));
		assertTrue ("unable to remove file2.ext", ca.removeEntry ("/file2.ext"));
		assertTrue ("unable to remove ./file3.ext", ca.removeEntry ("./file1.ext"));
		assertEquals ("unexpected number of entries in archive after deleting number 3 top-level files", 2 * (testFiles.size () - 1) - 3, ca.getNumEntries ());
		
		ca.pack ();
		ca.close ();
	}
	
	/**
	 * @throws IOException
	 * @throws JDOMException
	 * @throws ParseException
	 * @throws CombineArchiveException
	 * @throws TransformerException 
	 */
	@Test
	public void testAddWholeMetaFile () throws IOException, JDOMException, ParseException, CombineArchiveException, TransformerException
	{
		// this is basically the Example.java
		
		// lets create the archive
		testFiles.get (0).delete ();
		CombineArchive ca = new CombineArchive (testFiles.get (0));

		List<VCard> creators = new ArrayList<VCard> ();
		creators.add (new VCard ("Scharm", "Martin",
			"martin.scharm@uni-rostock.de", "University of Rostock"));
		creators.add (new VCard ("Waltemath", "Dagmar",
			"dagmar.waltemath@uni-rostock.de", "University of Rostock"));
		

		ArchiveEntry SBMLFile = ca.addEntry (
			new File ("/tmp/base/path"),
			new File ("/tmp/base/path/file.sbml"),
			CombineFormats.getFormatIdentifier ("sbml"));
		
		SBMLFile.addDescription (new OmexMetaDataObject (new OmexDescription (creators, new Date ())));

		ArchiveEntry CellMLFile = ca.addEntry (
			new File ("/tmp/base/path/subdir/file.cellml"),
			"/subdir/file.cellml",
			CombineFormats.getFormatIdentifier ("cellml.1.0"),
			true);
		
		CellMLFile.addDescription (new OmexMetaDataObject (new OmexDescription (creators, new Date ())));
		
		Element metaParent = new Element ("stuff");
		Element metaElement = new Element ("myMetaElement");
		metaElement.setAttribute ("someAttribute", "someValue");
		metaElement.addContent ("some content");
		metaParent.addContent (metaElement);
		CellMLFile.addDescription ("someFragment", new DefaultMetaDataObject (metaParent));
		
		ca.pack ();
		
		// end of Example.java
		
		// extract meta data file
		testFiles.get (1).delete ();
		Files.createDirectories (testFiles.get (1).toPath ());
		ca.extractTo (testFiles.get (1));
		
		int prevDescriptions = CellMLFile.getDescriptions ().size ();
		int toAdd = prevDescriptions + SBMLFile.getDescriptions ().size ();
		CellMLFile.addAllDescriptions (new File (testFiles.get (1) + "/metadata.rdf"));
		assertEquals ("expected so see a different number of descriptions after adding all descriptions from a file", prevDescriptions + toAdd, CellMLFile.getDescriptions ().size ());
		

		ca.close ();
		
	}
}
