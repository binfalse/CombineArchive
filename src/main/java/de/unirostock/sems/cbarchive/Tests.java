/**
 * 
 */
package de.unirostock.sems.cbarchive;

import java.io.File;
import java.util.Vector;


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
		File f = new File ("/tmp/combine-arch-fb2/tests/manifest0.xml");
		
		Vector<OmexDescription> descr = OmexDescriptionFile.readFile (f, false);
		descr.addAll (OmexDescriptionFile.readFile (new File ("/tmp/combine-arch-fb2/tests/manifest1.xml"), false));
		
		System.out.println ("got " + descr.size () + " omex descriptions");
		for (int i = 0; i < descr.size (); i++)
			descr.elementAt (i).debug ();
		
		OmexDescriptionFile.writeFile (descr, f.getParentFile ());
		
		//CombineArchive.addEntry (new File ("/tmp/combine-arch-fb2"), f, null, null);
	}
	
}
