/**
 * CombineArchive - a JAVA library to read/write/create/... CombineArchives
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
	private CombineArchive				archive;
	
	/** The relative path name to that file. */
	private String								relativeName;
	
	/** The format. */
	private String								format;
	
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
		// TODO: JSONArray meta = new JSONArray ();
		// for (MetaDataObject m : descriptions)
		// meta.add (m.toJson ());
		// descr.put ("meta", meta);
		return descr;
	}
}
