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
	
	/** The description. */
	private OmexDescription	description;
	
	
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
		String format, OmexDescription description)
	{
		super ();
		this.archive = archive;
		this.relativeName = relativeName;
		this.format = format;
		this.description = description;
		if (description != null)
			description.setAbout (relativeName);
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
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public OmexDescription getDescription ()
	{
		return description;
	}
	
	
	/**
	 * Sets the description.
	 * 
	 * @param description
	 *          the new description
	 */
	public void setDescription (OmexDescription description)
	{
		this.description = description;
	}
}
