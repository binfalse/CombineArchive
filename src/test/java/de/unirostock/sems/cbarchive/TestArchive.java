/**
 * 
 */
package de.unirostock.sems.cbarchive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;
import de.unirostock.sems.cbarchive.meta.MetaDataObject;
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
	
	private List<File> testFiles = new ArrayList<File> ();
	
	/**
	 * create some test files
	 * @throws IOException 
	 */
	@Before
	public void initialize () throws IOException
	{
		// lets create 6 test files, the first one will serve as an archive
		for (int i = 0; i < 6; i++)
			testFiles.add (File.createTempFile ("combineArchive", "test" + i));
		//LOGGER.setMinLevel (LOGGER.DEBUG);
	}
	/**
	 * delete test files
	 */
	@After
	public void destroy ()
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
	 * @throws URISyntaxException 
	 */
	@Test
	public void someRandomTests () throws IOException, JDOMException, ParseException, CombineArchiveException, TransformerException, URISyntaxException
	{
		// lets create the archive
		testFiles.get (0).delete ();
		CombineArchive ca = new CombineArchive (testFiles.get (0));
		
		List<ArchiveEntry> entries = new ArrayList<ArchiveEntry> ();
		for (int i = 1; i < testFiles.size (); i++)
			entries.add (ca.addEntry (testFiles.get (i), "/sub" + i + "/file" + i + ".ext", new URI ("http://identifiers.org/combine.specifications/sbml")));
		
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
			entries.add (ca.addEntry (testFiles.get (i), "/sub" + i + "/file" + i + ".ext", new URI ("http://identifiers.org/combine.specifications/sbml")));
		assertEquals ("unexpected number of entries in archive after resubmitting all files", testFiles.size () - 1, ca.getNumEntries ());
		
		// we should still be able to add known file under a different name
		for (int i = 1; i < testFiles.size (); i++)
			entries.add (ca.addEntry (testFiles.get (i), "./file" + i + ".ext", new URI ("http://identifiers.org/combine.specifications/sbml")));
		assertEquals ("unexpected number of entries in archive after adding all files under different names", 2 * (testFiles.size () - 1), ca.getNumEntries ());
		
		// and this should overwrite our last commit
		for (int i = 1; i < testFiles.size (); i++)
			entries.add (ca.addEntry (testFiles.get (i), "file" + i + ".ext", new URI ("http://identifiers.org/combine.specifications/sbml")));
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
	 * @throws URISyntaxException 
	 */
	@Test
	public void testAddWholeMetaFile () throws IOException, JDOMException, ParseException, CombineArchiveException, TransformerException, URISyntaxException
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
			new File ("test/base/path"),
			new File ("test/base/path/file.sbml"),
			new URI ("http://identifiers.org/combine.specifications/sbml"));
		
		SBMLFile.addDescription (new OmexMetaDataObject (new OmexDescription (creators, new Date ())));

		ArchiveEntry CellMLFile = ca.addEntry (
			new File ("test/base/path/subdir/file.cellml"),
			"/subdir/file.cellml",
			new URI ("http://identifiers.org/combine.specifications/cellml.1.0"),
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
	
	/**
	 * 
	 */
	@Test
	public void testMainEntries ()
	{
		// lets create the archive
		for (int i = 0; i < 6; i++)
		{
			if (testFiles.get (i).isDirectory ())
			{
				try
				{
					Utils.delete (testFiles.get (i));
					testFiles.get (i).createNewFile ();
				}
				catch (IOException e)
				{
					LOGGER.error (e, "couldn't recreate testfiles.");
				}
			}
		}
		testFiles.get (0).delete ();
		
		
		
		CombineArchive ca = null;
		try
		{
			ca = new CombineArchive (testFiles.get (0));
		}
		catch (IOException | JDOMException | ParseException
			| CombineArchiveException e)
		{
			LOGGER.error (e, "couldn't read archive");
			fail ("couldn't read archive");
		}
		

		
		List<ArchiveEntry> entries = new ArrayList<ArchiveEntry> ();
		for (int i = 1; i < testFiles.size (); i++)
		{
			try
			{
				if (i % 2 == 0)
				{
					ArchiveEntry ae = ca.addEntry (testFiles.get (i), "/sub" + i + "/file" + i + ".ext", new URI ("http://identifiers.org/combine.specifications/sbml"));
					entries.add (ae);
					if (i == 2)
						ca.addMainEntry (ae);
				}
				else
				{
					entries.add (ca.addEntry (testFiles.get (i), "/sub" + i + "/file" + i + ".ext", new URI ("http://identifiers.org/combine.specifications/sbml"), true));
				}
			}
			catch (IOException | URISyntaxException e)
			{
				LOGGER.error (e, "couldn't add entry ", i, " to archive");
				fail ("couldn't add entry "+ i + " to archive");
			}
		}
		
		assertEquals ("expected 4 master files", 4, ca.getMainEntries ().size ());
		assertNotNull ("main entries not backwards compatible", ca.getMainEntry ());
		
		List<VCard> creators = new ArrayList<VCard> ();
		creators.add (new VCard ("Scharm", "Martin",
			"martin.scharm@uni-rostock.de", "University of Rostock"));
		creators.add (new VCard ("Waltemath", "Dagmar",
			"dagmar.waltemath@uni-rostock.de", "University of Rostock"));
		
		for (ArchiveEntry e : entries)
			e.addDescription (new OmexMetaDataObject (new OmexDescription (creators, new Date ())));
		
		assertEquals ("unexpected number of entries in archive after creation", testFiles.size () - 1, ca.getNumEntries ());
		
		
		
		try
		{
			ca.pack ();
			ca.close ();
		}
		catch (IOException | TransformerException e)
		{
			LOGGER.error (e, "couldn't pack/close archive");
			fail ("couldn't pack/close archive");
		}
		
		
	}

	/**
	 */
	@Test
	public void testPaperExample ()
	{
		try
		{
			LOGGER.setLogToStdErr (false);
			CombineArchive ca = new CombineArchive (new File ("test/paper-repressilator.omex"), true);
			assertFalse ("did not expected to see some errors", ca.hasErrors ());
			assertEquals ("expected to nevertheless find some entries", 2, ca.getEntries ().size ());
			int meta = 0;
			for (ArchiveEntry entry : ca.getEntries ())
			{
				meta += entry.getDescriptions ().size ();
			}
			assertEquals ("expected to see exactly 0 descriptions.", 0, meta);
			assertEquals ("expected to see exactly 1 description for the archive.", 1, ca.getDescriptions ().size ());
			ca.close ();
		}
		catch (Exception e)
		{
			e.printStackTrace ();
			fail ("unexpected error occured");
		}
		LOGGER.setLogToStdErr (true);
	}

	/**
	 */
	@Test
	public void testBrokenArchive ()
	{
		try
		{
			LOGGER.setLogToStdErr (false);
			CombineArchive ca = new CombineArchive (new File ("test/broken-archive-by-felix.omex"), true);
			assertTrue ("expected to see some errors", ca.hasErrors ());
			assertEquals ("expected to nevertheless find some entries", 2, ca.getEntries ().size ());
			int meta = 0;
			for (ArchiveEntry entry : ca.getEntries ())
			{
				meta += entry.getDescriptions ().size ();
				/*for (MetaDataObject mo : entry.getDescriptions ())
					System.out.println (entry.getEntityPath () + " -> " + mo.getAbout ());*/
			}
			assertEquals ("expected to see exactly 2 descriptions.", 2, meta);
			/*List<String> errors = ca.getErrors ();
			for (String s : errors)
				System.out.println (s);*/
			assertEquals ("expected to see exactly 2 errors.", 2, ca.getErrors ().size ());
			ca.close ();
		}
		catch (Exception e)
		{
			e.printStackTrace ();
			fail ("unexpected error occured");
		}
		LOGGER.setLogToStdErr (true);
	}
	
	/**
	 */
	@Test
	public void testMove ()
	{
		// lets create the archive
		for (int i = 0; i < 6; i++)
		{
			if (testFiles.get (i).isDirectory ())
			{
				try
				{
					Utils.delete (testFiles.get (i));
					testFiles.get (i).createNewFile ();
				}
				catch (IOException e)
				{
					LOGGER.error (e, "couldn't recreate testfiles.");
				}
			}
		}
		testFiles.get (0).delete ();
		
		CombineArchive ca = null;
		try
		{
			ca = new CombineArchive (testFiles.get (0));
		}
		catch (IOException | JDOMException | ParseException
			| CombineArchiveException e)
		{
			LOGGER.error (e, "couldn't read archive");
			fail ("couldn't read archive");
		}
		
		List<ArchiveEntry> entries = new ArrayList<ArchiveEntry> ();
		for (int i = 1; i < testFiles.size (); i++)
		{
			try
			{
				entries.add (ca.addEntry (testFiles.get (i), "/sub" + i + "/file" + i + ".ext", new URI ("http://identifiers.org/combine.specifications/sbml")));
			}
			catch (IOException | URISyntaxException e)
			{
				LOGGER.error (e, "couldn't add entry ", i, " to archive");
				fail ("couldn't add entry "+ i + " to archive");
			}
		}
		
		List<VCard> creators = new ArrayList<VCard> ();
		creators.add (new VCard ("Scharm", "Martin",
			"martin.scharm@uni-rostock.de", "University of Rostock"));
		creators.add (new VCard ("Waltemath", "Dagmar",
			"dagmar.waltemath@uni-rostock.de", "University of Rostock"));
		
		for (ArchiveEntry e : entries)
			e.addDescription (new OmexMetaDataObject (new OmexDescription (creators, new Date ())));
		
		assertEquals ("unexpected number of entries in archive after creation", testFiles.size () - 1, ca.getNumEntries ());
		
		try
		{
			ca.pack ();
			ca.close ();
		}
		catch (IOException | TransformerException e)
		{
			LOGGER.error (e, "couldn't pack/close archive");
			fail ("couldn't pack/close archive");
		}
		
		
		// test the move
		try
		{
			ca = new CombineArchive (testFiles.get (0));
		}
		catch (IOException | JDOMException | ParseException
			| CombineArchiveException e)
		{
			LOGGER.error (e, "couldn't read archive");
			fail ("couldn't read archive");
		}
		
		ArchiveEntry entry = ca.getEntry ("/sub3/file3.ext");
		assertEquals ("unexpected number of meta for /sub3/file3.ext", 1, entry.getDescriptions ().size ());
		assertEquals ("meta of /sub3/file3.ext is not for /sub3/file3.ext", "/sub3/file3.ext", entry.getDescriptions ().get (0).getAbout ());
		
		
		try
		{
			ca.moveEntry ("/sub3/file3.ext", "/sub1/file3.ext");
			ca.moveEntry ("/sub4/file4.ext", "/sub4-2/file4.ext");
		}
		catch (IOException e)
		{
			LOGGER.error (e, "couldn't move entry");
			fail ("couldn't move entry");
		}
		
		
		assertNull ("mhpf. this file shouldn't be there anymore.", ca.getEntry ("/sub3/file3.ext"));
		assertNull ("mhpf. this file shouldn't be there anymore.", ca.getEntry ("/sub4/file4.ext"));
		
		entry = ca.getEntry ("/sub1/file3.ext");
		assertNotNull ("moving failed", entry);
		List<MetaDataObject> meta = entry.getDescriptions ();
		assertEquals ("unexpected number of meta for /sub1/file3.ext", 1, meta.size ());
		for (MetaDataObject m : meta)
			assertEquals ("meta of /sub1/file3.ext is not for /sub1/file3.ext", "/sub1/file3.ext", m.getAbout ());
		
		entry = ca.getEntry ("/sub4-2/file4.ext");
		assertNotNull ("moving failed", entry);
		meta = entry.getDescriptions ();
		assertEquals ("unexpected number of meta for /sub4-2/file4.ext", 1, meta.size ());
		for (MetaDataObject m : meta)
			assertEquals ("meta of /sub1/file3.ext is not for /sub4-2/file4.ext", "/sub4-2/file4.ext", m.getAbout ());
		

		try
		{
			ca.pack ();
			ca.close ();
		}
		catch (IOException | TransformerException e)
		{
			LOGGER.error (e, "couldn't pack/close archive");
			fail ("couldn't pack/close archive");
		}
		
		// finally make sure we also stored the stuff correctly!
		try
		{
			ca = new CombineArchive (testFiles.get (0));
		}
		catch (IOException | JDOMException | ParseException
			| CombineArchiveException e)
		{
			LOGGER.error (e, "couldn't read archive");
			fail ("couldn't read archive");
		}
		
		assertNull ("mhpf. this file shouldn't be there anymore.", ca.getEntry ("/sub3/file3.ext"));
		assertNull ("mhpf. this file shouldn't be there anymore.", ca.getEntry ("/sub4/file4.ext"));
		
		entry = ca.getEntry ("/sub1/file3.ext");
		assertNotNull ("moving failed", entry);
		meta = entry.getDescriptions ();
		assertEquals ("unexpected number of meta for /sub1/file3.ext", 1, meta.size ());
		for (MetaDataObject m : meta)
			assertEquals ("meta of /sub1/file3.ext is not for /sub1/file3.ext", "/sub1/file3.ext", m.getAbout ());
		
		entry = ca.getEntry ("/sub4-2/file4.ext");
		assertNotNull ("moving failed", entry);
		meta = entry.getDescriptions ();
		assertEquals ("unexpected number of meta for /sub4-2/file4.ext", 1, meta.size ());
		for (MetaDataObject m : meta)
			assertEquals ("meta of /sub1/file3.ext is not for /sub4-2/file4.ext", "/sub4-2/file4.ext", m.getAbout ());
		

		try
		{
			ca.pack ();
			ca.close ();
		}
		catch (IOException | TransformerException e)
		{
			LOGGER.error (e, "couldn't pack/close archive");
			fail ("couldn't pack/close archive");
		}
	}
	
	/**
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws TransformerException 
	 */
	@Test
	public void testModifyMeta () throws IOException, URISyntaxException, TransformerException
	{
		fail ("this is to be done");
		
		// lets create the archive
		for (int i = 0; i < 6; i++)
		{
			if (testFiles.get (i).isDirectory ())
			{
				try
				{
					Utils.delete (testFiles.get (i));
					testFiles.get (i).createNewFile ();
				}
				catch (IOException e)
				{
					LOGGER.error (e, "couldn't recreate testfiles.");
				}
			}
		}
		testFiles.get (0).delete ();
		
		
		
		CombineArchive ca = null;
		try
		{
			ca = new CombineArchive (testFiles.get (0));
		}
		catch (IOException | JDOMException | ParseException
			| CombineArchiveException e)
		{
			LOGGER.error (e, "couldn't read archive");
			fail ("couldn't read archive");
		}
		
		
		

		ArchiveEntry SBMLFile = ca.addEntry (
			testFiles.get (1),
			"/somefile",
			new URI ("http://identifiers.org/combine.specifications/sbml"));
		
		Element metaParent = new Element ("stuff");
		Element metaElement = new Element ("myMetaElement");
		metaElement.setAttribute ("someAttribute", "someValue");
		metaElement.addContent ("some content");
		metaParent.addContent (metaElement);
		SBMLFile.addDescription ("someFragment", new DefaultMetaDataObject (metaParent));

		ca.pack ();
		
		MetaDataObject mdo = SBMLFile.getDescriptions ().get (0);
		assertNotNull ("expected to find some meta data", mdo);
		
		String meta = Utils.prettyPrintDocument (new Document (mdo.getXmlDescription ()));
		System.out.println (meta);
		
		
		
		
		
		
		metaParent = new Element ("stuff");
		metaElement = new Element ("myMetaElement");
		metaElement.setAttribute ("someAttribute", "someValue");
		metaElement.addContent ("some content");
		metaParent.addContent (metaElement);
	}
}
