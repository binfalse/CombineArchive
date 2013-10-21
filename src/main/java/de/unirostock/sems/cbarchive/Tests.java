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

/**
 * @author martin
 * 
 */
public class Tests
{
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main (String[] args) throws Exception
	{
		/*
		 * 
		 * File f = new File ("/tmp/combine-arch-fb2/tests/manifest0.xml");
		 * 
		 * Vector<OmexDescription> descr = OmexDescriptionFile.readFile (f, false);
		 * descr.addAll (OmexDescriptionFile.readFile (new File
		 * ("/tmp/combine-arch-fb2/tests/manifest1.xml"), false));
		 * 
		 * System.out.println ("got " + descr.size () + " omex descriptions");
		 * for (int i = 0; i < descr.size (); i++)
		 * descr.elementAt (i).debug ();
		 * 
		 * OmexDescriptionFile.writeFiles (descr, f.getParentFile ());
		 */
		
		/*
		 * File f = new File ("/tmp/combine-arch-fb2/tests/manifest.xml");
		 * 
		 * 
		 * CombineArchive ca = new CombineArchive ();
		 * ca.parseManifest (f);
		 */
		
		/*
		 * Vector<VCard> creators = new Vector<VCard> ();
		 * creators.add (new VCard ("Scharm", "Martin",
		 * "martin.scharm@uni-rostock.de", "University of Rostock"));
		 * creators.add (new VCard ("Waltemath", "Dagmar",
		 * "dagmar.waltemath@uni-rostock.de", "University of Rostock"));
		 * 
		 * CombineArchive ca = new CombineArchive ();
		 * ca.addEntry (new File ("/tmp/combine-arch-fb2/files"), new File
		 * ("/tmp/combine-arch-fb2/files/yourls-shortener-1.5.xpi"), "sbml", new
		 * OmexDescription (creators, null));
		 * ca.addEntry (new File ("/tmp/combine-arch-fb2/"), new File
		 * ("/tmp/combine-arch-fb2/files/Waltemath_eBio2013_ms.pdf"), "cellML", new
		 * OmexDescription (creators, null));
		 * ca.exportArchive (new File
		 * ("/tmp/combine-arch-fb2/files/testArchive.zip"));
		 */
		
		/*
		 * File f = new File ("/tmp/combine-arch-fb2/files/testArchive.zip");
		 * CombineArchive ca = CombineArchive.readArchive (f);
		 * System.out.println (ca.getBaseDir () + " -> " + ca.getBaseDir ().exists
		 * ());
		 * 
		 * 
		 * File f2 = new File ("/tmp/combine-arch-fb2/files/testArchive.zip");
		 * CombineArchive ca2 = CombineArchive.readArchive (f2, new File
		 * ("/tmp/de.unirostock.sems.cbarchive.CombineArchive1166008305877479870"));
		 * System.out.println (ca2.getBaseDir () + " -> " + ca2.getBaseDir ().exists
		 * ());
		 */
		
		// CombineArchive.addEntry (new File ("/tmp/combine-arch-fb2"), f, null,
		// null);
		
		System.out.println (CombineFormats.getFormatIdentifier ("omex"));
		
	}
	
}
