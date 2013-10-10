/**
 * 
 */
package de.unirostock.sems.cbarchive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * @author martin
 * 
 */
public class ArchiveEntry
{
	
	private static final String	knownFormats	= "/formatDict.prop";
	private static Properties		formats			= new Properties ();
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
		}
	}
	
	private CombineArchive archive;
	private String relName;
	private String format;
	
	
	
	
	
	
	public ArchiveEntry (CombineArchive archive, String relName, String format)
	{
		super ();
		this.archive = archive;
		this.relName = relName;
		this.format = format;
	}


	
	public String getRelName ()
	{
		return relName;
	}


	
	public String getFormat ()
	{
		return format;
	}


	public static String getFormat (String s)
	{
		return formats.getProperty (s, s);
	}


	/**
	 * The main method to update our format dictionary.
	 *
	 * @param args the arguments
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main (String[] args)
		throws FileNotFoundException,
			IOException
	{
		// merge format dictionaries. thanks to frank :)
		// see: https://github.com/fbergmann/CombineArchive
		
		final URL franksList = new URL (
			"https://raw.github.com/fbergmann/CombineArchive/master/LibCombine/Entry.cs");
		final Pattern franksPattern = Pattern
			.compile ("^\\s*\\{\\s*\"([^\"]+)\"\\s*,\\s*\"([^\"]+)\"\\s*\\},\\s*$");
		
		System.out.println ("known so far");
		for (Object f : formats.keySet ())
			System.out.println (f);
		System.out.println ("/known so far");
		
		formats.store (
			new FileOutputStream (ArchiveEntry.class.getResource (knownFormats)
				.getFile ()), null);
		
		// load franks version and update our version
		BufferedReader in = null;
		try
		{
			in = new BufferedReader (new InputStreamReader (franksList.openStream ()));
			String line;
			while ( (line = in.readLine ()) != null)
			{
				Matcher m = franksPattern.matcher (line);
				if (m.matches ())
					formats.put (m.group (1), m.group (2));
			}
			// update our version
			formats.store (
				new FileOutputStream (ArchiveEntry.class.getResource (knownFormats)
					.getFile ()), null);
		}
		catch (IOException e)
		{
			e.printStackTrace ();
		}
		finally
		{
			try
			{
				in.close ();
			}
			catch (IOException e)
			{
			}
		}
	}
}
