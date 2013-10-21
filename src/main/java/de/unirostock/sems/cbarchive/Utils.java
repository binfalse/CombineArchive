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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.TransformerException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.binfalse.bflog.LOGGER;



/**
 * Some tools.
 * 
 * @author martin scharm
 */
public class Utils
{
	
	public final static int			BUFFER_SIZE	= 8 * 1024;
	public final static String	NEWLINE			= System
																						.getProperty ("line.separator");
	
	
	/**
	 * Reads an XML file and creates a Document.
	 * 
	 * @param f
	 *          the file to read
	 * @return the document
	 * @throws JDOMException
	 *           the JDOM exception
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public static final Document readXmlDocument (File f)
		throws JDOMException,
			IOException
	{
		SAXBuilder builder = new SAXBuilder ();
		return (Document) builder.build (f);
	}
	
	
	/**
	 * Write a ZIP file.
	 * 
	 * @param directoryToZip
	 *          the directory to zip
	 * @param destination
	 *          the destination file
	 * @param fileList
	 *          the file list to store in the ZIP
	 * @throws FileNotFoundException
	 *           the file not found exception
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	private static void writeZipFile (File directoryToZip, File destination,
		List<File> fileList) throws IOException
	{
		
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		try
		{
			fos = new FileOutputStream (destination);
			zos = new ZipOutputStream (fos);
			
			for (File file : fileList)
				if (!file.isDirectory ())
					addZipEntry (directoryToZip, file, zos);
		}
		catch (IOException e)
		{
			LOGGER.error ("cannot write zip file: " + destination + " - zipping: "
				+ directoryToZip, e);
			throw e;
		}
		finally
		{
			if (fos != null)
				fos.close ();
			if (zos != null)
				zos.close ();
		}
	}
	
	
	/**
	 * Adds a zip entry to a zip file.
	 * 
	 * @param directoryToZip
	 *          the directory to zip
	 * @param file
	 *          the file to add
	 * @param zos
	 *          the ZipOutputStream
	 * @throws FileNotFoundException
	 *           the file not found exception
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	private static void addZipEntry (File directoryToZip, File file,
		ZipOutputStream zos) throws FileNotFoundException, IOException
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream (file);
			ZipEntry zipEntry = new ZipEntry (file.getCanonicalPath ().substring (
				directoryToZip.getCanonicalPath ().length () + 1,
				file.getCanonicalPath ().length ()));
			zos.putNextEntry (zipEntry);
			
			byte[] bytes = new byte[BUFFER_SIZE];
			int length;
			while ( (length = fis.read (bytes)) >= 0)
				zos.write (bytes, 0, length);
			zos.closeEntry ();
		}
		catch (IOException e)
		{
			LOGGER.error ("add zip entry: " + file + " - zipping: " + directoryToZip,
				e);
			throw e;
		}
		finally
		{
			fis.close ();
		}
	}
	
	
	/**
	 * Create a zip file.
	 * 
	 * @param directory
	 *          the directory to zip
	 * @param destination
	 *          the destination of the zip file
	 * @throws FileNotFoundException
	 *           the file not found exception
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public static void packZip (File directory, File destination,
		List<File> fileList) throws FileNotFoundException, IOException
	{
		writeZipFile (directory, destination, fileList);
	}
	
	
	/**
	 * Unpack a zip file.
	 * 
	 * @param zipFilePath
	 *          the zip file path
	 * @param destDirectory
	 *          the destination directory to write the packed files to
	 * @return true, if successful
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public static boolean unpackZip (File zipFilePath, File destDirectory,
		boolean deleteOnExit) throws IOException
	{
		File destDir = destDirectory;
		if (!destDir.exists ())
			destDir.mkdir ();
		ZipInputStream zipIn = null;
		try
		{
			zipIn = new ZipInputStream (new FileInputStream (zipFilePath));
			ZipEntry entry = zipIn.getNextEntry ();
			while (entry != null)
			{
				File filePath = new File (destDirectory + File.separator
					+ entry.getName ());
				filePath.getParentFile ().mkdirs ();
				if (deleteOnExit)
				{
					filePath.getParentFile ().deleteOnExit ();
					filePath.deleteOnExit ();
				}
				if (!entry.isDirectory ())
					extractFile (zipIn, filePath);
				zipIn.closeEntry ();
				entry = zipIn.getNextEntry ();
			}
		}
		catch (IOException e)
		{
			LOGGER.error ("add unzip file: " + zipFilePath + " - to: "
				+ destDirectory, e);
			throw e;
		}
		finally
		{
			if (zipIn != null)
				zipIn.close ();
		}
		return true;
	}
	
	
	/**
	 * Extract a file from a zip archive.
	 * 
	 * @param zipIn
	 *          the zip stream
	 * @param filePath
	 *          the file path
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	private static void extractFile (ZipInputStream zipIn, File filePath)
		throws IOException
	{
		BufferedOutputStream bos = null;
		try
		{
			bos = new BufferedOutputStream (new FileOutputStream (filePath));
			byte[] bytesIn = new byte[BUFFER_SIZE];
			int read = 0;
			while ( (read = zipIn.read (bytesIn)) != -1)
				bos.write (bytesIn, 0, read);
		}
		catch (IOException e)
		{
			LOGGER.error ("cannot unzip file: " + filePath, e);
			throw e;
			
		}
		finally
		{
			if (bos != null)
				bos.close ();
		}
	}
	
	/**
	 * The Class SimpleOutputStream.
	 */
	public static class SimpleOutputStream
		extends OutputStream
	{
		
		/** The string. */
		private StringBuilder	string	= new StringBuilder ();
		
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.OutputStream#write(int)
		 */
		@Override
		public void write (int b) throws IOException
		{
			this.string.append ((char) b);
		}
		
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString ()
		{
			return this.string.toString ();
		}
		
		
		/**
		 * Reset.
		 */
		public void reset ()
		{
			this.string = new StringBuilder ();
		}
	}
	
	
	/**
	 * Gets elements of an XML subtree by tag name.
	 * 
	 * @param parent
	 *          the root of the subtree
	 * @param name
	 *          the tag name
	 * @param ns
	 *          the namespace
	 * @return the elements by sharing this tag name
	 */
	public static List<Element> getElementsByTagName (Element parent,
		String name, Namespace ns)
	{
		List<Element> nodeList = new ArrayList<Element> ();
		List<Element> togo = new ArrayList<Element> ();
		togo.add (parent);
		
		while (togo.size () > 0)
		{
			parent = togo.remove (0);
			nodeList.addAll (parent.getChildren (name, ns));
			togo.addAll (0, parent.getChildren ());
		}
		
		return nodeList;
	}
	
	
	/**
	 * Pretty print an XML document.
	 * 
	 * @param doc
	 *          the XML document
	 * @return the beautified string
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws TransformerException
	 *           the transformer exception
	 */
	public static String prettyPrintDocument (Document doc)
		throws IOException,
			TransformerException
	{
		SimpleOutputStream out = new SimpleOutputStream ();
		XMLOutputter outputter = new XMLOutputter (Format.getPrettyFormat ());
		outputter.output (doc, out);
		return out.toString ();
	}
	
}
