/**
 * 
 */
package de.unirostock.sems.cbarchive;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;



/**
 * @author martin
 * 
 */
public class Tools
{
	public final static String NEWLINE = System.getProperty("line.separator");
	
	public static boolean mkDir (File file)
	{
		return file.exists () || file.mkdirs ();
	}
	
	public static final Document createNewDocument () throws ParserConfigurationException
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		return docBuilder.newDocument();
	}
	
	public static final Document createNewDocument (File f) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		return docBuilder.parse (f);
	}
	
	
	public static void copyInputStream (InputStream in, OutputStream out) throws IOException
	{
		try
		{
			byte[] buffer = new byte [4096];
			int len = in.read (buffer);
			while ((len = in.read (buffer)) >= 0)
				out.write (buffer, 0, len);
		}
		catch (IOException e)
		{
			e.printStackTrace ();
			throw new IOException ("unable to copy streams");
		}
		finally
		{
			try
			{
				in.close();
				out.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	
	public static boolean unpackZip (File f, File targetDir)
	{
		ZipFile zipFile = null;
		try
		{
			zipFile = new ZipFile (f);
			Enumeration<? extends ZipEntry> entries = zipFile.entries ();
			
			if (!mkDir (targetDir))
					throw new IOException ("target dir doesn't exists and not able to create it: " + targetDir);
			
			while (entries.hasMoreElements ())
			{
				ZipEntry entry = entries.nextElement ();
				File file = new File (targetDir, File.separator + entry.getName ());
				
				if (entry.isDirectory ())
				{
					if (!mkDir (file))
						throw new IOException ("could not create directory: " + file);
				}
				else
				{
					if (!mkDir (file.getParentFile ()))
					{
						throw new IOException ("could not create directory: "
							+ file.getParentFile ());
					}
					copyInputStream (zipFile.getInputStream (entry),
						new BufferedOutputStream (new FileOutputStream (file)));
				}
			}
			zipFile.close ();
			return true;
		}
		catch (ZipException e)
		{
			e.printStackTrace ();
		}
		catch (IOException e)
		{
			e.printStackTrace ();
		}
		finally
		{
			try
			{
				if (zipFile != null)
					zipFile.close ();
			}
			catch (IOException e)
			{
				e.printStackTrace ();
			}
		}
		return false;
	}
	
	public static class SimpleOutputStream extends OutputStream
	{

	  private StringBuilder string = new StringBuilder();
	  
	  @Override
	  public void write(int b) throws IOException
	  {
	      this.string.append((char) b );
	  }
	  
	  public String toString()
	  {
	      return this.string.toString();
	  }
	  
	  public void reset ()
	  {
	  	this.string = new StringBuilder();
	  }
	}

	public static String prettyPrintDocument(Document doc) throws IOException, TransformerException
	{
		return prettyPrintDocument (doc, new SimpleOutputStream ()).toString ();
  }
	
	public static OutputStream prettyPrintDocument (Document doc, OutputStream out) throws IOException, TransformerException
	{
	  TransformerFactory tf = TransformerFactory.newInstance();
	  Transformer transformer = tf.newTransformer();
	  transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	  transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	  transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	  transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	  transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	
	  transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	  return out;
	}
}
