/**
 * 
 */
package de.unirostock.sems.cbarchive;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * @author martin
 *
 */
public class CombineArchive
{
	public static final String omexNs = "http://identifiers.org/combine.specifications/omex-manifest";

  public HashMap<String, ArchiveEntry> entries;
  public Vector<OmexDescription> descriptions;
  public File archive;
  public File baseDir;
  public ArchiveEntry mainEntry;
  
  public CombineArchive () throws IOException
  {
  	entries = new HashMap<String, ArchiveEntry> ();
  	descriptions = new Vector<OmexDescription> ();
  	
  	baseDir = Files.createTempDirectory ("CombineArchive").toFile ();
  }
  
  // obviously we'll overwrite
  public ArchiveEntry addEntry (File baseDir, File file, String format, OmexDescription description) throws IOException
  {
  	if (!file.exists ())
  		throw new IOException ("file does not exist.");
  	
  	if (!file.getAbsolutePath ().contains (baseDir.getAbsolutePath ()))
  		throw new IOException ("file must be in basedir.");
  	
  	String localName = file.getAbsolutePath ().replace (baseDir.getAbsolutePath (), "");
  	
  	File target = new File (this.baseDir.getAbsolutePath () + localName);
  	
  	Files.copy (file.toPath (), target.toPath (), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
  	
  	
  	ArchiveEntry entry = new ArchiveEntry (this, "." + localName, format);
  	
  	entries.put (entry.getRelName (), entry);
  	if (description != null)
  		descriptions.add (description);
  	
  	return entry;
  }
  
  /*
  
  public void parseManifest (File manifest) throws ParserConfigurationException, SAXException, IOException
  {
	  Document doc = Tools.createNewDocument (manifest);
  	NodeList nl = doc.getElementsByTagNameNS (omexNs, "content");
  	for (int i = 0; i < nl.getLength (); i++)
  	{
  		 Element content = (Element) nl.item(i); 
       String location = content.getAttribute("location");
       String format = content.getAttribute("format");
       String master = content.getAttribute("master");
       ArchiveEntry entry = new ArchiveEntry
                       (
                           this,
                           location,
                           format
                       );
       if (format.equals (ArchiveEntry.getFormat ("omex")))
       {
      	 // TODO: parse description
      	 // that's not a real entry
      	 continue;
       }
       
       if (format.equals (ArchiveEntry.getFormat ("manifest")))
       {
      	 // that's this manifest -> skip
      	 continue;
       }
       
       if (master != null && Boolean.parseBoolean (master))
      	 mainEntry = entry;
       entries.add (entry);
  	}
  	
  }
  
  public CombineArchive InitializeFromArchive (File f) throws IOException
  {
  	File tmp = Files.createTempDirectory (CombineArchive.class.getName ()).toFile ();
  	if (!Tools.unpackZip (f, tmp))
  		throw new IOException ("unable to unpack zip file");
  	
  	CombineArchive arch = new CombineArchive ();
  	arch.baseDir = tmp;
  	// TODO: arch.parseManifest (new File (tmp + File.separator + "manifest.xml"));
  	arch.archive = f;
  	return arch;
  }*/
}
