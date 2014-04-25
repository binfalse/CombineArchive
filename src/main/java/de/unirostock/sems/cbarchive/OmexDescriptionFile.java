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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import javax.xml.transform.TransformerException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import de.binfalse.bflog.LOGGER;



/**
 * The Class OmexDescriptionFile representing a meta data file in a combine
 * archive.
 * 
 * @author martin
 */
public class OmexDescriptionFile
{
	
	/**
	 * Writes the descriptions to single files, one description per file.
	 * 
	 * @param descriptions
	 *          the omex descriptions
	 * @param basePath
	 *          the base path
	 * @return the vector of created files
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws TransformerException
	 *           the transformer exception
	 */
	public static Vector<File> writeFiles (Vector<OmexDescription> descriptions,
		File basePath) throws IOException, TransformerException
	{
		Vector<File> outputs = new Vector<File> ();
		
		for (int i = 0; i < descriptions.size (); i++)
		{
			File output = new File (basePath.getAbsolutePath () + File.separator
				+ "metadata-" + i + ".rdf");
			while (output.exists ())
				output = new File (basePath.getAbsolutePath () + File.separator
					+ "metadata-" + i + "-" + UUID.randomUUID ().toString () + ".rdf");
			outputs.add (output);
			Document xmlDoc = new Document ();
			Element rdf = new Element ("RDF", OmexDescription.rdfNS);
			xmlDoc.addContent (rdf);
			rdf.addNamespaceDeclaration (OmexDescription.dcNS);
			rdf.addNamespaceDeclaration (OmexDescription.vcNS);
			
			descriptions.elementAt (i).toXML (rdf);
			
			BufferedWriter bw = null;
			try
			{
				bw = new BufferedWriter (new FileWriter (output));
				bw.write (Utils.prettyPrintDocument (xmlDoc));
			}
			catch (IOException | TransformerException e)
			{
				LOGGER.error (e, "cannot write omex descriptions to ", output);
				throw e;
			}
			finally
			{
				if (bw != null)
					bw.close ();
			}
		}
		return outputs;
	}
	
	
	/**
	 * Write Omex descriptions, all in one single file.
	 * 
	 * @param descriptions
	 *          the Omex descriptions
	 * @param basePath
	 *          the base path of the archive
	 * @return the file
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws TransformerException
	 *           the transformer exception
	 */
	public static File writeFile (Vector<OmexDescription> descriptions,
		File basePath) throws IOException, TransformerException
	{
		File output = new File (basePath.getAbsolutePath () + File.separator
			+ "metadata.rdf");
		while (output.exists ())
			output = new File (basePath.getAbsolutePath () + File.separator
				+ "metadata-" + UUID.randomUUID ().toString () + ".rdf");
		
		Document xmlDoc = new Document ();
		Element rdf = new Element ("RDF", OmexDescription.rdfNS);
		xmlDoc.addContent (rdf);
		rdf.addNamespaceDeclaration (OmexDescription.dcNS);
		rdf.addNamespaceDeclaration (OmexDescription.vcNS);
		
		for (OmexDescription description : descriptions)
			description.toXML (rdf);
		
		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter (new FileWriter (output));
			bw.write (Utils.prettyPrintDocument (xmlDoc));
		}
		catch (IOException | TransformerException e)
		{
			LOGGER.error (e, "cannot write omex descriptions to ", output);
			throw e;
		}
		finally
		{
			if (bw != null)
				bw.close ();
		}
		
		return output;
	}
	
	
	/**
	 * Read an Omex description file and extract Omex descriptions. Same as
	 * calling
	 * 
	 * <pre>
	 * OmexDescriptionFile.readFile (new File (pathToFile), ignoreErrs);
	 * </pre>
	 * 
	 * @param pathToFile
	 *          path to the file containing Omex descriptions
	 * @param ignoreErrs
	 *          if true we don't throw an error if we're unable to parse a single
	 *          Omex description (in that case we'll just skip it and continue
	 *          parsing the next one).
	 * @return the list of Omex descriptions encoded in file
	 * @throws Exception
	 *           the exception
	 */
	public static Vector<OmexDescription> readFile (String pathToFile,
		boolean ignoreErrs) throws Exception
	{
		return readFile (new File (pathToFile), ignoreErrs);
	}
	
	
	/**
	 * Read an Omex description file and extract Omex descriptions.
	 * 
	 * @param file
	 *          the file containing Omex descriptions
	 * @param ignoreErrs
	 *          if true we don't throw an error if we're unable to parse a single
	 *          Omex description (in that case we'll just skip it and continue
	 *          parsing the next one).
	 * @return the list of Omex descriptions encoded in file
	 * @throws ParseException
	 *           the parse exception
	 * @throws JDOMException
	 *           the jDOM exception
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public static Vector<OmexDescription> readFile (File file, boolean ignoreErrs)
		throws ParseException,
			JDOMException,
			IOException
	{
		Document doc = Utils.readXmlDocument (file);
		
		Vector<OmexDescription> descriptions = new Vector<OmexDescription> ();
		
		List<Element> nl = Utils.getElementsByTagName (doc.getRootElement (),
			"Description", OmexDescription.rdfNS);
		for (int i = 0; i < nl.size (); i++)
		{
			try
			{
				descriptions.add (new OmexDescription (nl.get (i)));
			}
			catch (ParseException e)
			{
				LOGGER.error (e, "cannot parse OmexDescription in ", file);
				if (!ignoreErrs)
					throw e;
			}
		}
		
		return descriptions;
	}
}
