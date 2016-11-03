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
package de.unirostock.sems.cbarchive.meta;

import java.util.ArrayList;
import java.util.List;



/**
 * The Class MetaDataHolder representing objects that may contain meta data.
 * 
 * @author Martin Scharm
 */
public abstract class MetaDataHolder
{
	
	/** The descriptions about the entity. */
	protected List<MetaDataObject> descriptions;
	
	
	/**
	 * Instantiates a new MetaDataHolder.
	 */
	public MetaDataHolder ()
	{
		descriptions = new ArrayList<MetaDataObject> ();
	}
	
	
	/**
	 * Gets the path to this entity.
	 * 
	 * @return the file path
	 */
	public abstract String getEntityPath ();
	
	
	/**
	 * Gets the {@link MetaDataObject MetaDataObjects} describing this entity.
	 * <p>
	 * The returned list can contain any number of {@link MetaDataObject
	 * MetaDataObjects}, but might as well be empty.
	 * </p>
	 * 
	 * 
	 * @return the descriptions
	 */
	public List<MetaDataObject> getDescriptions ()
	{
		return this.descriptions;
	}
	
	
	/**
	 * Removes a certain description of this entity.
	 * 
	 * @param toDelete
	 *          the meta data object to delete
	 * @return true, if successful
	 */
	public boolean removeDescription (MetaDataObject toDelete)
	{
		return descriptions.remove (toDelete);
	}
	
	
	/**
	 * Adds another meta object describing this entry.
	 * 
	 * @param fragmentIdentifier
	 *          the fragment identifier pointing into this entry
	 * @param description
	 *          the new description
	 */
	public void addDescription (String fragmentIdentifier,
		MetaDataObject description)
	{
		description.setAbout (this, fragmentIdentifier);
		
		// we do not need to store meta data twice...
		for (MetaDataObject descr : descriptions)
			if (description.equals (descr))
				return;
			
		this.descriptions.add (description);
	}
	
	
	/**
	 * Adds another meta object describing this entry.
	 * 
	 * @param description
	 *          the new description
	 */
	public void addDescription (MetaDataObject description)
	{
		addDescription (null, description);
	}
}
