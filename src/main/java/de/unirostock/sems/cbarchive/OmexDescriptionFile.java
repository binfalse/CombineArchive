/**
 * 
 */
package de.unirostock.sems.cbarchive;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.Vector;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.binfalse.bflog.LOGGER;


/**
 * @author martin
 *
 */
public class OmexDescriptionFile
{
	public static void writeFiles (Vector<OmexDescription> descriptions, File basePath)
	{
		
	}
	
	public static void writeFile (Vector<OmexDescription> descriptions, File basePath) throws Exception
	{
		File output = new File (basePath.getAbsolutePath () + File.separator + "omexDescriptions.xml");
		while (output.exists ())
			output = new File (basePath.getAbsolutePath () + File.separator + "omexDescriptions-" + UUID.randomUUID ().toString () + ".xml");
		
		Document xmlDoc = Tools.createNewDocument ();
		Element rdf = xmlDoc.createElementNS (OmexDescription.rdfNS, "rdf:RDF");
		xmlDoc.appendChild (rdf);

		rdf.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:dcterms", OmexDescription.dcNS);
		rdf.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:vCard", OmexDescription.vcNS);
		
		for (OmexDescription description : descriptions)
		{
			//System.out.println ("writing descr");
			description.toXML (xmlDoc, rdf);
		}
		
		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter (new FileWriter (output));
			bw.write (Tools.prettyPrintDocument (xmlDoc));
		}
		catch (IOException | TransformerException e)
		{
			e.printStackTrace();
			LOGGER.error ("cannot write omex descriptions to " + output);
			throw e;
		}
		finally
		{
			if (bw != null)
				try
				{
					bw.close ();
				}
				catch (IOException e)
				{}
		}
		
	}
	
	
	
	/**
	 * Read an Omex description file and extract Omex descriptions. Same as calling 
	 * 
	 * <pre>
	 * OmexDescriptionFile.readFile (new File (pathToFile), ignoreErrs);
	 * </pre>
	 *
	 * @param pathToFile path to the file containing Omex descriptions
	 * @param ignoreErrs if true we don't throw an error if we're unable to parse a single Omex description (in that case we'll just skip it and continue parsing the next one).
	 * @return the list of Omex descriptions encoded in file
	 * @throws Exception the exception
	 */
	public static Vector<OmexDescription> readFile (String pathToFile, boolean ignoreErrs) throws Exception
	{
		return readFile (new File (pathToFile), ignoreErrs);
	}
	
	/**
	 * Read an Omex description file and extract Omex descriptions.
	 *
	 * @param file the file containing Omex descriptions
	 * @param ignoreErrs if true we don't throw an error if we're unable to parse a single Omex description (in that case we'll just skip it and continue parsing the next one).
	 * @return the list of Omex descriptions encoded in file
	 * @throws Exception the exception
	 */
	public static Vector<OmexDescription> readFile (File file, boolean ignoreErrs) throws Exception
	{
		  	Document doc = Tools.createNewDocument (file);
		
		Vector<OmexDescription> descriptions = new Vector<OmexDescription> ();

		NodeList nl = doc.getElementsByTagNameNS (OmexDescription.rdfNS, "Description");
		//nl = doc.getElementsByTagName ("rdf:Description");
		//System.out.println ("found " + nl.getLength () + " rdf:Description tags");
		for (int i = 0; i < nl.getLength (); i++)
		{
			/*Element tmp = (Element) nl.item (i);
			System.out.println (tmp.getNamespaceURI ());
			System.out.println (OmexDescription.rdfNS);
			System.out.println (OmexDescription.rdfNS.equals (tmp.getNamespaceURI ()));
			*/
			try
			{
				descriptions.add (new OmexDescription ((Element) nl.item (i)));
			}
			catch (Exception e)
			{
				LOGGER.error ("cannot parse OmexDescription", e);
				if (!ignoreErrs)
					throw e;
			}
		}
		
		return descriptions;
	}
}
