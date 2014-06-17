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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
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
	
	/** The RDF namespace. */
	public static final Namespace					rdfNS					= Namespace
																												.getNamespace ("rdf",
																													"http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	
	/** The DC namespace. */
	public static final Namespace					dcNS					= Namespace
																												.getNamespace (
																													"dcterms",
																													"http://purl.org/dc/terms/");
	
	/** The vcard namespace. */
	public static final Namespace					vcNS					= Namespace
																												.getNamespace ("vCard",
																													"http://www.w3.org/2006/vcard/ns#");
	
	/** The OMEX namespace. */
	public static final Namespace					omexNs				= Namespace
																												.getNamespace ("http://identifiers.org/combine.specifications/omex-manifest");
	
	/** The date formater. */
	public static final SimpleDateFormat	dateFormater	= new SimpleDateFormat (
																												"yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");
	
	/** The default buffer size. */
	public final static int								BUFFER_SIZE		= 8 * 1024;
	
	/** The newline character. */
	public final static String						NEWLINE				= System
																												.getProperty ("line.separator");
	
	/** The COPY_OPTION used to copy/move files. */
	public static final CopyOption[]			COPY_OPTION		= new CopyOption[] {
		StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };
	
	
	/**
	 * Reads an XML file and creates a Document.
	 * 
	 * @param fileToRead
	 *          the file to read
	 * @return the XML document
	 * @throws JDOMException
	 *           the JDOM exception
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public static final Document readXmlDocument (Path fileToRead)
		throws JDOMException,
			IOException
	{
		SAXBuilder builder = new SAXBuilder ();
		return (Document) builder.build (Files.newInputStream (fileToRead,
			StandardOpenOption.READ));
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
			LOGGER.error (e, "cannot write zip file: ", destination, " - zipping: ",
				directoryToZip);
			throw e;
		}
		finally
		{
			if (zos != null)
				zos.close ();
			if (fos != null)
				fos.close ();
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
			LOGGER.error (e, "add zip entry: ", file, " - zipping: ", directoryToZip);
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
	 * @param fileList
	 *          the file list
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
	 * @param deleteOnExit
	 *          the delete on exit
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
			LOGGER.error (e, "add unzip file: ", zipFilePath, " - to: ",
				destDirectory);
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
			LOGGER.error (e, "cannot unzip file: ", filePath);
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
	
	
	/**
	 * Extracts the extension of a filename.
	 * 
	 * @param fileName
	 *          the file name
	 * @return the extension, or null if we're not able to find an extension
	 */
	public static String getExtension (String fileName)
	{
		int dot = fileName.lastIndexOf (".");
		if (dot >= 0)
			return fileName.substring (dot + 1);
		return null;
	}
	
	
	/**
	 * Recursively delete file or directory.
	 * 
	 * @param f
	 *          the file/dir to delete
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public static void delete (File f) throws IOException
	{
		if (f.isDirectory ())
			for (File c : f.listFiles ())
				delete (c);
		if (!f.delete ())
			throw new FileNotFoundException ("Failed to delete file: " + f);
	}
}
