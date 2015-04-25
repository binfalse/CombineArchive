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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.cbarchive.meta.DefaultMetaDataObject;
import de.unirostock.sems.cbarchive.meta.MetaDataObject;
import de.unirostock.sems.cbarchive.meta.OmexMetaDataObject;
import de.unirostock.sems.cbarchive.meta.omex.VCard;
import de.unirostock.sems.cbarchive.meta.omex.OmexDescription;



/**
 * The Class TestWeb.
 *
 * @author Martin Scharm
 */
public class TestArchive
{
	
	/** The test files. */
	private List<File> testFiles = new ArrayList<File> ();
	
	/**
	 * create some test files.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Before
	public void initialize () throws IOException
	{
		// lets create 6 test files, the first one will serve as an archive
		for (int i = 0; i < 6; i++)
		{
			File f = File.createTempFile ("combineArchive", "test" + i);
			testFiles.add (f);
			BufferedWriter bw = new BufferedWriter( new FileWriter (f));
			bw.write ("i:" + i);
			bw.close ();
		}
		//LOGGER.setMinLevel (LOGGER.DEBUG);
		
	}
	
	/**
	 * delete test files.
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
	 * Test slashes/backslashes conversation on non-slash-based os'..
	 * This test was actually written by the DDMoRe team.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 * @throws JDOMException the jDOM exception
	 * @throws ParseException the parse exception
	 * @throws CombineArchiveException the combine archive exception
	 * @throws TransformerException the transformer exception
	 */
	@Test
	public void shouldGetAndRemoveEntryFromExistingArchive ()
		throws IOException,
			URISyntaxException,
			JDOMException,
			ParseException,
			CombineArchiveException,
			TransformerException
	{
	  // skip that check if our os uses slashes...
		if (File.separator.equals ("/"))
			return;
		
		final String WIN_FILE = "\\sub1\\file1.ext";
		final String UNIX_FILE = "/sub1/file2.ext";
		
		testFiles.get (0).delete ();
		CombineArchive ca = new CombineArchive (testFiles.get(0));
		
		ca.addEntry (testFiles.get(0), WIN_FILE, new URI ("http://identifiers.org/combine.specifications/sbml"));
		ca.addEntry (testFiles.get(1), UNIX_FILE, new URI ("http://identifiers.org/combine.specifications/sbml"));
		
		assertEquals ("unexpected number of entries in archive after creation", 2, ca.getNumEntries ());
		
		ca.pack ();
		ca.close ();
		
		CombineArchive readArchive = new CombineArchive (testFiles.get(0));
		String message = " ";
		boolean isUnixFileRemoved = readArchive.removeEntry (UNIX_FILE);
		message= (isUnixFileRemoved == false)? message+" Failed to remove file in Unix format : "+ UNIX_FILE+"\n":message;
		boolean isWinFileRemoved = readArchive.removeEntry (WIN_FILE);
		message= (isWinFileRemoved == false)? message+" Failed to remove file Windows format : "+ WIN_FILE+"\n":message;
		
		readArchive.close();
		assertTrue (message, (isUnixFileRemoved && isWinFileRemoved));
	}
	
	
	/**
	 * Test add and remove.
	 * @throws CombineArchiveException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * @throws TransformerException 
	 */
	@Test
	public void testAdd () throws IOException, JDOMException, ParseException, CombineArchiveException, URISyntaxException, TransformerException
	{
		LOGGER.setMinLevel (LOGGER.ERROR);
		testFiles.get (0).delete ();
		
		
		List<VCard> creators = new ArrayList<VCard> ();
		creators.add (new VCard ("Scharm", "Martin",
			"martin.scharm@uni-rostock.de", "University of Rostock"));
		OmexDescription omex = new OmexDescription (creators, new Date ());
		
		
		CombineArchive ca = new CombineArchive (testFiles.get(0));

		assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (1).getParentFile (), testFiles.get (1), new URI ("http://identifiers.org/combine.specifications/sbml")));
		assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (1).getParentFile (), testFiles.get (1), new URI ("http://identifiers.org/combine.specifications/sbml"), true));
		assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (1), "/sub" + 1 + "/file" + 1 + ".ext", new URI ("http://identifiers.org/combine.specifications/sbml")));
		assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (1), "/sub" + 1 + "/file" + 1 + ".ext", new URI ("http://identifiers.org/combine.specifications/sbml"), true));
		
		// test unsupported locations
		try
		{
			ca.addEntry (testFiles.get (1), CombineArchive.MANIFEST_LOCATION, new URI ("http://identifiers.org/combine.specifications/sbml"));
			fail ("overwrote manifest");
		}
		catch (IllegalArgumentException e){}
		try
		{
			ca.addEntry (testFiles.get (1), CombineArchive.METADATA_LOCATION, new URI ("http://identifiers.org/combine.specifications/sbml"));
			fail ("overwrote meta");
		}
		catch (IllegalArgumentException e){}
		try
		{
			ca.addEntry (testFiles.get (1), "/metadata-1.rdf", new URI ("http://identifiers.org/combine.specifications/sbml"));
			fail ("overwrote meta");
		}
		catch (IllegalArgumentException e){}
		try
		{
			ca.addEntry (testFiles.get (1), "/metadata-123.rdf", new URI ("http://identifiers.org/combine.specifications/sbml"));
			fail ("overwrote meta");
		}
		catch (IllegalArgumentException e){}
		
		// test unsupported features
		try
		{
			assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (1).getParentFile (), new File (testFiles.get (1) + "doesnotexist"), new URI ("http://identifiers.org/combine.specifications/sbml")));
			fail ("added nonexistent file?");
		}
		catch (IOException e)
		{
			// ok
		}
		try
		{
			assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (1).getParentFile (), new File (testFiles.get (1) + "doesnotexist"), new URI ("http://identifiers.org/combine.specifications/sbml"), true));
			fail ("added nonexistent file?");
		}
		catch (IOException e)
		{
			// ok
		}
		try
		{
			assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (2), testFiles.get (1), new URI ("http://identifiers.org/combine.specifications/sbml")));
			fail ("added file from non-parent dir?");
		}
		catch (IOException e)
		{
			// ok
		}
		try
		{
			assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (2), testFiles.get (1), new URI ("http://identifiers.org/combine.specifications/sbml"), true));
			fail ("added file from non-parent dir?");
		}
		catch (IOException e)
		{
			// ok
		}
		
		// test deprecated methods
		assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (1), "/sub" + 1 + "/file" + 1 + ".ext", "http://identifiers.org/combine.specifications/sbml"));
		assertNull ("could add entry with invalid uir?", ca.addEntry (testFiles.get (1), "/sub" + 1 + "/file" + 1 + ".ext", "s t u f f"));
		
		assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (1), "/sub" + 1 + "/file" + 1 + ".ext", "http://identifiers.org/combine.specifications/sbml", true));
		assertNull ("could add entry with invalid uir?", ca.addEntry (testFiles.get (1), "/sub" + 1 + "/file" + 1 + ".ext", "s t u f f", false));


		assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (1).getParentFile (), testFiles.get (1), "http://identifiers.org/combine.specifications/sbml"));
		assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (1).getParentFile (), testFiles.get (1), "http://identifiers.org/combine.specifications/sbml", true));

		assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (1).getParentFile (), testFiles.get (1), "http://identifiers.org/combine.specifications/sbml", omex));
		assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (1).getParentFile (), testFiles.get (1), "http://identifiers.org/combine.specifications/sbml", omex, true));
		
		assertNull ("couldn't add entry", ca.addEntry (testFiles.get (1).getParentFile (), testFiles.get (1), "s t u f f", true));
		assertNull ("couldn't add entry", ca.addEntry (testFiles.get (1).getParentFile (), testFiles.get (1), "s t u f f"));
		try
		{
			assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (1).getParentFile (), new File (testFiles.get (1) + "doesnotexist"), "http://identifiers.org/combine.specifications/sbml", true));
			fail ("added nonexistent file?");
		}
		catch (IOException e)
		{
			// ok
		}
		try
		{
			assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (2), testFiles.get (1), "http://identifiers.org/combine.specifications/sbml", true));
			fail ("added file from non-parent dir?");
		}
		catch (IOException e)
		{
			// ok
		}
		try
		{
			assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (1).getParentFile (), new File (testFiles.get (1) + "doesnotexist"), "http://identifiers.org/combine.specifications/sbml", omex, true));
			fail ("added nonexistent file?");
		}
		catch (IOException e)
		{
			// ok
		}
		try
		{
			assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (2), testFiles.get (1), "http://identifiers.org/combine.specifications/sbml", omex, true));
			fail ("added file from non-parent dir?");
		}
		catch (IOException e)
		{
			// ok
		}
		try
		{
			assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (1).getParentFile (), new File (testFiles.get (1) + "doesnotexist"), "http://identifiers.org/combine.specifications/sbml", omex));
			fail ("added nonexistent file?");
		}
		catch (IOException e)
		{
			// ok
		}
		try
		{
			assertNotNull ("couldn't add entry", ca.addEntry (testFiles.get (2), testFiles.get (1), "http://identifiers.org/combine.specifications/sbml", omex));
			fail ("added file from non-parent dir?");
		}
		catch (IOException e)
		{
			// ok
		}
		
		


		ca.pack ();
		ca.close ();
		LOGGER.setMinLevel (LOGGER.WARN);
	}
	
	
	/**
	 * Test local files by URI -> file:/path/to/file.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws JDOMException the jDOM exception
	 * @throws ParseException the parse exception
	 * @throws CombineArchiveException the combine archive exception
	 * @throws TransformerException the transformer exception
	 * @throws URISyntaxException the uRI syntax exception
	 */
	@Test
	public void someRandomTests () throws IOException, JDOMException, ParseException, CombineArchiveException, TransformerException, URISyntaxException
	{
		// test locations
		assertEquals ("unexpected manifest path", "/manifest.xml", CombineArchive.MANIFEST_LOCATION);
		assertEquals ("unexpected metadata path", "/metadata.rdf", CombineArchive.METADATA_LOCATION);
		
		// lets create the archive
		testFiles.get (0).delete ();
		CombineArchive ca = new CombineArchive (testFiles.get (0));
		assertEquals ("archive path is expected to be '.'", ".", ca.getEntityPath ());

		assertFalse ("expected to not have sbml entries", ca.hasEntriesWithFormat (new URI ("http://identifiers.org/combine.specifications/sbml")));
		assertFalse ("expected to not have sbml entries", ca.HasEntriesWithFormat (new URI ("http://identifiers.org/combine.specifications/sbml")));
		assertEquals ("expected different number of sbml entries", 0, ca.getNumEntriesWithFormat (new URI ("http://identifiers.org/combine.specifications/sbml")));
		assertNull ("expected no main entry", ca.getMainEntry ());
		
		List<ArchiveEntry> entries = new ArrayList<ArchiveEntry> ();
		for (int i = 1; i < testFiles.size (); i++)
		{
			ArchiveEntry ae = ca.addEntry (testFiles.get (i), "/sub" + i + "/file" + i + ".ext", new URI ("http://identifiers.org/combine.specifications/sbml"));
			entries.add (ae);
			assertEquals ("unexpected file name", "file" + i + ".ext", ae.getFileName ());
		}
		
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
		
		assertTrue ("expected to have sbml entries", ca.hasEntriesWithFormat (new URI ("http://identifiers.org/combine.specifications/sbml")));
		assertFalse ("expected to not have cell entries", ca.hasEntriesWithFormat (new URI ("http://identifiers.org/combine.specifications/cellml")));
		assertTrue ("expected to have sbml entries", ca.HasEntriesWithFormat (new URI ("http://identifiers.org/combine.specifications/sbml")));
		assertEquals ("expected different number of sbml entries", testFiles.size () - 1, ca.getNumEntriesWithFormat (new URI ("http://identifiers.org/combine.specifications/sbml")));
		
		// we should still be able to add known file under a different name
		for (int i = 1; i < testFiles.size (); i++)
			entries.add (ca.addEntry (testFiles.get (i), "./file" + i + ".ext", new URI ("http://identifiers.org/combine.specifications/sbml")));
		assertEquals ("unexpected number of entries in archive after adding all files under different names", 2 * (testFiles.size () - 1), ca.getNumEntries ());
		
		// and this should overwrite our last commit
		for (int i = 1; i < testFiles.size (); i++)
			entries.add (ca.addEntry (testFiles.get (i), "file" + i + ".ext", new URI ("http://identifiers.org/combine.specifications/sbml")));
		assertEquals ("unexpected number of entries in archive after submitting last commit with same path", 2 * (testFiles.size () - 1), ca.getNumEntries ());
		
		// test replacing a file
		File tmp1 = File.createTempFile ("combineArchiveTestFile", "tmp");
		File tmp2 = File.createTempFile ("combineArchiveTestFile", "tmp");
		tmp1.deleteOnExit (); tmp2.deleteOnExit ();
		ArchiveEntry ae = entries.get (entries.size () - 1);
		ca.extract (ae.getPath (), tmp1);
		List<VCard> creators = new ArrayList<VCard> ();
		creators.add (new VCard ("Scharm", "Martin",
			"martin.scharm@uni-rostock.de", "University of Rostock"));
		OmexMetaDataObject meta = new OmexMetaDataObject (new OmexDescription (creators, new Date ()));
		ae.addDescription (meta);
		ArchiveEntry ae2 = ca.replaceFile (testFiles.get (1), ae);
		// make sure we still have the meta data
		assertEquals ("lost some meta data while replacing file?", ae.getDescriptions ().size (), ae2.getDescriptions ().size ());
		assertEquals ("lost some meta data while replacing file?", 1, ae2.getDescriptions ().size ());
		ca.extract (ae.getPath (), tmp2);
		// make sure the files differ
		byte [] a = Files.readAllBytes (tmp1.toPath ());
		byte [] b = Files.readAllBytes (tmp2.toPath ());
		assertFalse ("files do not have changed...", Arrays.equals (a, b));
		tmp1.delete (); tmp2.delete ();
		
		ae.extractFile (tmp2);
		b = Files.readAllBytes (tmp2.toPath ());
		assertFalse ("files do not have changed...", Arrays.equals (a, b));
		tmp2.delete ();
		
		assertEquals ("entry returned wrong archive", ca, ae.getArchive ());
		
		tmp2 = ae.getFile ();
		b = Files.readAllBytes (tmp2.toPath ());
		assertFalse ("files do not have changed...", Arrays.equals (a, b));
		tmp2.delete ();
		
		
		ae.setFormat (new URI ("http://identifiers.org/combine.specifications/sbml"));
		assertEquals ("setting format failed", new URI ("http://identifiers.org/combine.specifications/sbml"), ae.getFormat ());
		
		assertTrue (ae.removeDescription (meta));
		meta.setAbout (ae);
		assertEquals ("setting about failed...", ae.getEntityPath (), meta.getAbout ());
		meta.setAbout (ae2);
		assertEquals ("setting about failed...", ae2.getEntityPath (), meta.getAbout ());
		
		try
		{
			tmp1.mkdirs ();
			tmp1.deleteOnExit ();
			// test directories
			ca.extract (ae.getPath (), tmp1);
			tmp1.delete ();
		}
		catch (IOException e)
		{
			fail ("directories should be ok!?");
		}
		
		
		// and lets remove top-level entries
		assertTrue ("unable to remove /file3.ext", ca.removeEntry ("/file3.ext"));
		assertTrue ("unable to remove file2.ext", ca.removeEntry ("/file2.ext"));
		assertTrue ("unable to remove ./file3.ext", ca.removeEntry ("./file1.ext"));
		assertEquals ("unexpected number of entries in archive after deleting number 3 top-level files", 2 * (testFiles.size () - 1) - 3, ca.getNumEntries ());
		
		assertTrue ("expected to get an iterator", ca.getEnumerator () instanceof Iterator);
		
		ca.pack ();
		ca.close ();
		tmp1.delete ();
	}
	
	/**
	 * Test add whole meta file.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws JDOMException the jDOM exception
	 * @throws ParseException the parse exception
	 * @throws CombineArchiveException the combine archive exception
	 * @throws TransformerException the transformer exception
	 * @throws URISyntaxException the uRI syntax exception
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
		
		Element root = new Element ("root");
		SBMLFile.getDescriptions ().get (0).injectDescription (root);
		Element date = root.getChildren ().get (root.getChildren ().size () - 1).getChildren ().get (0);
		date.setText ("test");
		System.out.println (Utils.prettyPrintDocument (new Document (root)));
		assertNull ("expected to not be able to interprete the meta xml", OmexMetaDataObject.tryToRead (metaParent));
		assertNull ("expected to not be able to interprete the meta xml", OmexMetaDataObject.tryToRead (root));
		
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
	 * Test main entries.
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

		int nMain = ca.getMainEntries ().size ();
		ArchiveEntry ae = ca.getMainEntries ().get (0);
		ca.removeMainEntry (ae);
		assertEquals ("unexpected number of main entries", --nMain, ca.getMainEntries ().size ());
		
		ae = ca.getMainEntries ().get (0);
		try
		{
			assertTrue ("couldn't remove entry", ca.removeEntry (ae));
			assertFalse ("double-removed entry?", ca.removeEntry (ae));
		}
		catch (IOException e)
		{
			LOGGER.error (e, "error removing an entry");
			fail ("couldn't remove entry");
		}
		assertEquals ("unexpected number of main entries", --nMain, ca.getMainEntries ().size ());
		
		ae = ca.getMainEntries ().get (0);
		try
		{
			assertTrue ("couldn't remove entry", ca.removeEntry (ae.getEntityPath ()));
			assertFalse ("double-removed entry?", ca.removeEntry (ae.getEntityPath ()));
		}
		catch (IOException e)
		{
			LOGGER.error (e, "error removing an entry");
			fail ("couldn't remove entry");
		}
		assertEquals ("unexpected number of main entries", --nMain, ca.getMainEntries ().size ());
		
		
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
	 * Test paper example.
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
	 * Test broken archive.
	 */
	@Test
	public void testBrokenArchive2 ()
	{
		LOGGER.setLogToStdErr (false);
		
		File [] broken = new File [] {
			new File ("test/paper-repressilator-brokenmanifest.omex"),
			new File ("test/paper-repressilator-mod-manifest.omex"),
			new File ("test/paper-repressilator-mod-manifest-2.omex"),
			new File ("test/paper-repressilator-mod-manifest-3.omex")
		};
		
		for (File f : broken)
		{
			try
			{
				CombineArchive ca = new CombineArchive (f, true);
				assertTrue ("expected to see an error if archive manifest is broken", ca.hasErrors ());
				ca.close ();
			}
			catch (Exception e)
			{
				e.printStackTrace ();
				fail ("unexpected error occured");
			}

			try
			{
				CombineArchive ca = new CombineArchive (f);
				fail ("expected to not be able to continue");
				ca.close ();
			}
			catch (Exception e)
			{
				// ok
			}
		}
		
		
		
		LOGGER.setLogToStdErr (true);
		
	}

	/**
	 * Test broken archive.
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
			
			ca.clearErrors ();
			assertFalse ("expected to see no more errors", ca.hasErrors ());
			
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
	 * Test move.
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
		
		ca.setMainEntry (ca.getEntry ("/sub3/file3.ext"));
		
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
		// is it still the main entry?
		assertTrue (ca.getMainEntries ().contains (entry));
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

		// moving non-existent..
		try
		{
			ca.moveEntry ("/stuff.noext", "/somehwere.ext");
			fail ("moved non-existent entry!?");
		}
		catch (IOException e)
		{
			// ok...
		}
		

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
	 * Test modify meta.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 * @throws TransformerException the transformer exception
	 */
	@Test
	public void testModifyMeta () throws IOException, URISyntaxException, TransformerException
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
		
		String meta = Utils.prettyPrintDocument (new Document (mdo.getXmlDescription ().clone ()));
		
		metaParent = new Element ("stuff");
		metaElement = new Element ("myMetaElement");
		metaElement.setAttribute ("someAttribute", "someValue");
		metaElement.addContent ("some other content");
		metaParent.addContent (metaElement);
		mdo.getXmlDescription ().addContent (metaParent);
		
		// make sure we have the new meta
		MetaDataObject mdo2 = SBMLFile.getDescriptions ().get (0);
		String meta2 = Utils.prettyPrintDocument (new Document (mdo2.getXmlDescription ().clone ()));
		assertFalse ("meta did not change!?", meta.equals (meta2));
		

		ca.pack ();
		
		ArchiveEntry SBMLFile2 = ca.getEntry ("/somefile");
		mdo2 = SBMLFile2.getDescriptions ().get (0);
		String meta3 = Utils.prettyPrintDocument (new Document (mdo2.getXmlDescription ().clone ()));
		assertFalse ("meta did not change!?", meta.equals (meta3));
		assertTrue ("meta did change!?", meta2.equals (meta3));
	}
	
	

	/**
	 * Test exceptions.
	 */
	@Test
	public void testExceptions ()
	{
		Exception e = new CombineArchiveException ("some exception");
		assertEquals ("expected different message", "some exception", e.getMessage ());
	}
	
	/**
	 * @throws TransformerException 
	 * @throws IOException 
	 */
	@Test
	public void testVcard () throws IOException, TransformerException
	{
		VCard vc = new VCard ();
		
		assertTrue ("expected vcard to be empty", vc.isEmpty ());
		Element el = new Element ("root");
		vc.toXml (el);
		assertEquals ("vcard shouldn't produce xml...", 0, el.getChildren ().size ());

		vc.setGivenName ("");
		assertTrue("expected vcard to be empty", vc.isEmpty ());
		vc.setFamilyName ("");
		assertTrue("expected vcard to be empty", vc.isEmpty ());
		vc.setEmail ("");
		assertTrue("expected vcard to be empty", vc.isEmpty ());
		
		vc.setFamilyName ("fam");
		assertFalse("expected vcard to be non-empty", vc.isEmpty ());
		el = new Element ("root");
		vc.toXml (el);
		assertEquals ("vcard should produce xml...", 1, el.getChildren ().size ());
		
		vc.setGivenName ("first");
		assertFalse("expected vcard to be non-empty", vc.isEmpty ());
		el = new Element ("root");
		vc.toXml (el);
		assertEquals ("vcard should produce xml...", 1, el.getChildren ().size ());
		
		vc.setEmail ("m@il");
		assertFalse("expected vcard to be non-empty", vc.isEmpty ());
		el = new Element ("root");
		vc.toXml (el);
		assertEquals ("vcard should produce xml...", 1, el.getChildren ().size ());
		
		
		vc.setOrganization ("uni");
		assertFalse("expected vcard to be non-empty", vc.isEmpty ());
		el = new Element ("root");
		vc.toXml (el);
		assertEquals ("vcard should produce xml...", 1, el.getChildren ().size ());
		
		assertEquals ("expected different family name", "fam", vc.getFamilyName ());
		assertEquals ("expected different given name", "first", vc.getGivenName ());
		assertEquals ("expected different mail address", "m@il", vc.getEmail ());
		assertEquals ("expected different organization", "uni", vc.getOrganization ());
		
		Object json = vc.toJsonObject ();
		assertTrue ("expected a json object", json instanceof JSONObject);
	}
	
	

	/**
	 * Test utils.
	 */
	@Test
	public void testMain ()
	{
		LOGGER.setMinLevel (LOGGER.ERROR);
		Main.main (null);
		Main m = new Main ();
		Utils u = new Utils ();
		LOGGER.setMinLevel (LOGGER.WARN);
	}
	
	
	/**
	 * Test example.
	 * @throws IOException 
	 */
	@Test
	public void testExample () throws IOException
	{
		File dir = new File ("/tmp/base/path/subdir");
		dir.mkdirs ();
		
		File f = new File ("/tmp/base/path/file.sbml");
		f.createNewFile ();
		f.deleteOnExit ();
		
		f = new File ("/tmp/base/path/subdir/file.cellml");
		f.createNewFile ();
		f.deleteOnExit ();
		
		Example e = new Example ();
		try
		{
			Example.PRINT = false;
			e.main (null);
		}
		catch (Exception e1)
		{
			LOGGER.error (e);
			fail ("example failed");
		}
	}
	
	

	/**
	 * Test utils.
	 */
	@Test
	public void testUtils ()
	{
		try
		{
			Utils.SimpleOutputStream sos = new Utils.SimpleOutputStream ();
			sos.write ('c');
			assertEquals ("unexpected string", "c", sos.toString ());
			sos.reset ();
			assertEquals ("unexpected string", "", sos.toString ());
		}
		catch (IOException e)
		{
			LOGGER.error (e);
			fail ("sos tests failed");
		}

		assertEquals ("extension detector wrong", "sbml", Utils.getExtension ("file.sbml"));
		assertEquals ("extension detector wrong", "sbml", Utils.getExtension ("file.somehting.sbml"));
		assertNull ("extension detector wrong", Utils.getExtension ("filesbml"));
		
		try
		{
			File f = File.createTempFile ("combinearchive", "test");
			try
			{
				Utils.delete (f);
			}
			catch (IOException e)
			{
				fail ("couldn't delete file");
			}
			try
			{
				Utils.delete (f);
				fail ("deleted non-existing file?");
			}
			catch (IOException e)
			{
			}
		}
		catch (IOException e)
		{
			LOGGER.error (e);
		}
		LOGGER.setMinLevel (LOGGER.WARN);
	}
	
}
