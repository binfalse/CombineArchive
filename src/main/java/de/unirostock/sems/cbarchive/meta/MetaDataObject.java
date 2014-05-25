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
package de.unirostock.sems.cbarchive.meta;

import org.jdom2.Element;

import de.unirostock.sems.cbarchive.ArchiveEntry;



/**
 * The abstract Class MetaDataObject representing some meta data.
 * 
 * @author Martin Scharm
 */
public abstract class MetaDataObject
{
	
	/** The entry this is all about. */
	protected ArchiveEntry	about;
	
	/** The fragment identifier. */
	protected String				fragmentIdentifier;
	
	
	/**
	 * Instantiates a new meta data object.
	 * 
	 * @param about
	 *          the entry to describe
	 */
	public MetaDataObject (ArchiveEntry about)
	{
		this.about = about;
		this.fragmentIdentifier = null;
	}
	
	
	/**
	 * Instantiates a new meta data object.
	 * 
	 * @param about
	 *          the entry to describe
	 * @param fragmentIdentifier
	 *          the fragment identifier pointing into <code>about</code>
	 */
	public MetaDataObject (ArchiveEntry about, String fragmentIdentifier)
	{
		this.about = about;
		this.fragmentIdentifier = fragmentIdentifier;
	}
	
	
	/**
	 * Gets the about.
	 * 
	 * @return the path to the entity described by this object
	 */
	public String getAbout ()
	{
		if (fragmentIdentifier != null)
			return about.getRelativeName () + "#" + fragmentIdentifier;
		
		return about.getRelativeName ();
	}
	
	
	/**
	 * Inject the description into <code>parent</code>.
	 * 
	 * @param parent
	 *          the parent element that will host the description
	 */
	public abstract void injectDescription (Element parent);
	
	
	/**
	 * Dummy method. To be <em>overwritten</em> in sub-classes.
	 * 
	 * @param element
	 *          the describing element
	 * @param about
	 *          the entry to be described
	 * @param fragmentIdentifier
	 *          the optional fragment identifier pointing into <code>about</code>
	 *          (leave <code>null</code> if in doubt)
	 * @return <code>null</code>, since this is abstract and we cannot read
	 *         anything here.
	 */
	public static MetaDataObject tryToRead (Element element, ArchiveEntry about,
		String fragmentIdentifier)
	{
		return null;
	}
}
