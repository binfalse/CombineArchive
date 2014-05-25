/**
 * CombineArchive - a JAVA library to read/write/create/... CombineArchives
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import de.unirostock.sems.cbarchive.meta.MetaDataObject;



/**
 * The Class ArchiveEntry represents a single entry in a CombineArchive.
 * 
 * @author martin scharm
 */
public class ArchiveEntry
{
	
	/** The archive containing this entry. */
	private CombineArchive	archive;
	
	/** The relative path name to that file. */
	private String					relativeName;
	
	/** The format. */
	private String					format;
	
	/** The descriptions. */
	private List<MetaDataObject>	descriptions;
	
	
	/**
	 * Instantiates a new archive entry.
	 * 
	 * @param archive
	 *          the CombineArchive
	 * @param relativeName
	 *          the relative path name
	 * @param format
	 *          the format
	 * @param description
	 *          the description
	 */
	public ArchiveEntry (CombineArchive archive, String relativeName,
		String format)
	{
		super ();
		descriptions = new ArrayList<MetaDataObject> ();
		this.archive = archive;
		this.relativeName = relativeName;
		this.format = format;
	}
	
	
	/**
	 * Gets the corresponding file.
	 * 
	 * @return the file
	 */
	public File getFile ()
	{
		return new File (archive.getBaseDir ().getAbsolutePath () + File.separator
			+ relativeName);
	}
	
	
	/**
	 * Gets the relative path name.
	 * 
	 * @return the relative path name
	 */
	public String getRelativeName ()
	{
		return relativeName;
	}
	
	
	/**
	 * Gets the format.
	 * 
	 * @return the format
	 */
	public String getFormat ()
	{
		return format;
	}
	
	
	/**
	 * Gets the descriptions.
	 * 
	 * @return the descriptions
	 */
	public List<MetaDataObject> getDescriptions ()
	{
		return this.descriptions;
	}
	
	
	/**
	 * Adds another meta object describing this entry.
	 * 
	 * @param description
	 *          the new description
	 */
	public void addDescription (MetaDataObject description)
	{
		this.descriptions.add (description);
	}


	/**
	 * Export a JSON description of this entry.
	 *
	 * @return the jSON object
	 */
	@SuppressWarnings("unchecked")
	public JSONObject toJsonObject ()
	{
		JSONObject descr = new JSONObject ();
		descr.put ("relativeName", relativeName);
		descr.put ("format", format);
//		TODO: JSONArray meta = new JSONArray ();
//		for (MetaDataObject m : descriptions)
//			meta.add (m.toJson ());
//		descr.put ("meta", meta);
		return descr;
	}
}
