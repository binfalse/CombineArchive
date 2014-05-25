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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import de.binfalse.bflog.LOGGER;



/**
 * The Class CombineArchive to ease the interaction with formats.
 * 
 * @author martin scharm
 * 
 */
public class CombineFormats
{
	
	/** known formats file. */
	private static final String	knownFormats	= "/formatDict.prop";
	
	/** known formats. */
	private static Properties		formats				= new Properties ();
	static
	{
		try
		{
			InputStream is = ArchiveEntry.class.getResourceAsStream (knownFormats);
			if (is != null)
				formats.load (is);
		}
		catch (IOException e)
		{
			e.printStackTrace ();
			LOGGER.error (e, "error reading known formats: ",
				ArchiveEntry.class.getResourceAsStream (knownFormats));
		}
	}
	
	
	/**
	 * Gets the official identifier of a certain format. You can for example pass
	 * <code>cellml.1.0</code> and you'll retrieve
	 * <code>http://identifiers.org/combine.specifications/cellml.1.0</code>. See
	 * <a href="http://co.mbine.org/standards/specifications/">COMBINE
	 * standards</a> for further
	 * information.
	 * 
	 * @param format
	 *          the format/extension
	 * @return the format identifier or <code>format</code> if no such mapping
	 *         exists
	 * @see <a
	 *      href="http://co.mbine.org/standards/specifications/">http://co.mbine.org/standards/specifications/</a>
	 */
	public static String getFormatIdentifier (String format)
	{
		return formats.getProperty (format.toLowerCase (), format);
	}
	
	
	/**
	 * Gets the format of an identifier. E.g. pass
	 * <code>http://identifiers.org/combine.specifications/sbml.level-3.version-1</code>
	 * and get <code>sbml.level-3.version-1</code>
	 * 
	 * @param identifier
	 *          the identifier of that format
	 * @return the format or <code>identifier</code> if no such mapping exists
	 */
	public static String getFormatFromIdentifier (String identifier)
	{
		String ret = null;
		for (Object tmp : formats.keySet ())
		{
			if (formats.get (tmp).equals (identifier))
			{
				if (ret == null)
					ret = tmp.toString ();
				else
					// ambiguous
					return identifier;
			}
		}
		if (ret != null)
			return ret;
		return identifier;
	}
	
	
	/**
	 * Insert format to dictionary.
	 * 
	 * @param key
	 *          the key
	 * @param value
	 *          the value
	 * @return true, if inserted
	 */
	private static boolean insertFormat (String key, String value)
	{
		String old = formats.getProperty (key);
		if (old != null)
		{
			if (!old.equals (value))
			{
				System.err.println ("inconsistency in " + key + ":");
				System.err.println ("< " + value);
				System.err.println ("> " + old);
				System.err.print ("which one to store? (<,>) [>] ");
				Scanner in = new Scanner (System.in);
				String s = in.nextLine ().trim ();
				in.close ();
				if (!s.startsWith ("<"))
					return false;
			}
			else
				return false;
		}
		formats.put (key, value);
		return true;
	}
	
	
	/**
	 * The main method to update our format dictionary.
	 * 
	 * @param args
	 *          the arguments
	 * @throws FileNotFoundException
	 *           the file not found exception
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public static void main (String[] args)
		throws FileNotFoundException,
			IOException
	{
		LOGGER.setLogToStdOut (true);
		LOGGER.setLogToStdErr (false);
		LOGGER.addLevel (LOGGER.INFO);
		
		int soFar = formats.size ();
		LOGGER.info ("so far we know ", soFar, " types");
		
		// merge format dictionaries. thanks to frank :)
		// see: https://github.com/fbergmann/CombineArchive
		
		final URL franksList = new URL (
			"https://raw.github.com/fbergmann/CombineArchive/master/LibCombine/Entry.cs");
		final Pattern franksPattern = Pattern
			.compile ("^\\s*\\{\\s*\"([^\"]+)\"\\s*,\\s*\"([^\"]+)\"\\s*\\},\\s*$");
		
		// load franks version and update our version
		BufferedReader in = null;
		try
		{
			LOGGER.info ("downloading franks formats list: ", franksList);
			in = new BufferedReader (new InputStreamReader (franksList.openStream ()));
			String line;
			int n = 0;
			LOGGER.info ("reading franks dict");
			while ( (line = in.readLine ()) != null)
			{
				Matcher m = franksPattern.matcher (line);
				if (m.matches ())
				{
					n++;
					insertFormat (m.group (1), m.group (2));
				}
			}
			LOGGER.info ("found ", n, " entries in table");
		}
		catch (IOException e)
		{
			e.printStackTrace ();
		}
		finally
		{
			try
			{
				if (in != null)
					in.close ();
			}
			catch (IOException e)
			{
			}
		}
		
		// reading combine specs table. even if @lenovere wants to see me
		// copy-paste-convert-stupid-office-crap.. ;-)
		// https://twitter.com/lenovere/status/392036232852549632
		
		final URL mbineTable = new URL (
			"http://co.mbine.org/standards/specifications/");
		try
		{
			LOGGER.info ("downloading mbine table: ", mbineTable);
			in = new BufferedReader (new InputStreamReader (mbineTable.openStream ()));
			
			String doc = "";
			boolean start = false;
			String line;
			while ( (line = in.readLine ()) != null)
			{
				// lets speed up the parsing. just store the table and omit other crap
				// on that site
				if (line.contains ("<table"))
				{
					line = line.replaceAll ("^.*<table[^<]*>", "<table>");
					start = true;
				}
				if (line.contains ("</table"))
				{
					line = line.replaceAll ("</table.*$", "</table>");
					doc += line;
					break;
				}
				
				if (start)
					doc += line;
			}
			
			LOGGER.info ("parsing table (", doc.length (), " chars)");
			
			SAXBuilder builder = new SAXBuilder ();
			Document d = (Document) builder.build (new StringReader (doc));
			
			LOGGER.info ("searching for entries");
			
			List<Element> table = Utils.getElementsByTagName (d.getRootElement (),
				"tr", null);
			int n = 0;
			for (Element e : table)
			{
				Element ofInterest = e.getChildren ().get (1);
				if (ofInterest.getName ().equals ("td")) // skip headline
				{
					String s = ofInterest.getText ().trim ();
					insertFormat (s, "http://identifiers.org/combine.specifications/" + s);
					n++;
				}
			}
			LOGGER.info ("found ", n, " entries in table");
			
		}
		catch (IOException | JDOMException e)
		{
			e.printStackTrace ();
		}
		finally
		{
			try
			{
				if (in != null)
					in.close ();
			}
			catch (IOException e)
			{
			}
		}
		
		LOGGER.info ("now we know ", formats.size (), " types");
		LOGGER
			.info ("that's a plus of ", (formats.size () - soFar), " new formats");
		
		// update our version
		formats.store (
			new FileOutputStream (ArchiveEntry.class.getResource (knownFormats)
				.getFile ()), null);
		LOGGER.info ("new file written to: ",
			ArchiveEntry.class.getResource (knownFormats).getFile ());
	}
	
}
