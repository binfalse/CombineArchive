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

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.Utils;
import de.unirostock.sems.cbarchive.meta.omex.OmexDescription;



/**
 * The Class OmexMetaDataObject representing meta data in OMEX format.
 * 
 * @author Martin Scharm
 */
public class OmexMetaDataObject
	extends MetaDataObject
{
	
	/** The description. */
	protected OmexDescription	description;
	
	
	/**
	 * Instantiates a new OMEX meta data object.
	 * 
	 * @param about
	 *          the entry described by this object
	 * @param description
	 *          the description
	 */
	public OmexMetaDataObject (ArchiveEntry about, OmexDescription description)
	{
		super (about, createDummyXmltree (about, description));
		this.description = description;
	}
	
	
	/**
	 * Instantiates a new OMEX meta data object.
	 * 
	 * @param about
	 *          the entry described by this object
	 * @param fragmentIdentifier
	 *          the fragment identifier pointing into <code>about</code>
	 * @param description
	 *          the description
	 */
	public OmexMetaDataObject (ArchiveEntry about, String fragmentIdentifier,
		OmexDescription description)
	{
		super (about, fragmentIdentifier, createDummyXmltree (about, description));
		this.description = description;
	}
	
	
	/**
	 * Instantiates a new OMEX meta data object.
	 * 
	 * @param about
	 *          the entry described by this object
	 * @param description
	 *          the description
	 * @param describingElement
	 *          the element rooting the subtree that describes about
	 */
	public OmexMetaDataObject (ArchiveEntry about, OmexDescription description,
		Element describingElement)
	{
		super (about, describingElement);
		this.description = description;
	}
	
	
	/**
	 * Instantiates a new OMEX meta data object.
	 * 
	 * @param about
	 *          the entry described by this object
	 * @param fragmentIdentifier
	 *          the fragment identifier pointing into <code>about</code>
	 * @param description
	 *          the description
	 * @param describingElement
	 *          the element rooting the subtree that describes about
	 */
	public OmexMetaDataObject (ArchiveEntry about, String fragmentIdentifier,
		OmexDescription description, Element describingElement)
	{
		super (about, fragmentIdentifier, describingElement);
		this.description = description;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unirostock.sems.cbarchive.meta.MetaDataObject#injectDescription(org.
	 * jdom2.Element)
	 */
	@Override
	public void injectDescription (Element parent)
	{
		description.toXML (parent);
	}
	
	
	/**
	 * Gets the omex description.
	 * 
	 * @return the omex description
	 */
	public OmexDescription getOmexDescription ()
	{
		return description;
	}
	
	
	/**
	 * Creates a dummy XML tree that represents the OMEX description.
	 * 
	 * @param about
	 *          the entry that this is about
	 * @param description
	 *          the OMEX description description
	 * @return the element
	 */
	private static final Element createDummyXmltree (ArchiveEntry about,
		OmexDescription description)
	{
		Element descElem = new Element ("Description", Utils.rdfNS);
		descElem.setAttribute ("about", about.getFilePath (), Utils.rdfNS);
		description.toXML (descElem);
		return descElem;
	}
	
	
	/**
	 * Try to read a meta data object. Might return null if <code>element</code>
	 * cannot be understood as an OMEX description.
	 * 
	 * @param element
	 *          the element rooting the meta data subtree
	 * @param about
	 *          the entry the is described by <code>element</code>
	 * @param fragmentIdentifier
	 *          the optional fragment identifier pointing into <code>about</code>
	 *          (leave <code>null</code> if in doubt)
	 * @return the OMEX meta data object if in proper format, or null if we cannot
	 *         parse the element
	 */
	public static OmexMetaDataObject tryToRead (Element element,
		ArchiveEntry about, String fragmentIdentifier)
	{
		try
		{
			OmexDescription desc = new OmexDescription (element);
			if (desc.isEmpty ())
				return null;
			if (fragmentIdentifier == null)
				return new OmexMetaDataObject (about, desc, element);
			return new OmexMetaDataObject (about, fragmentIdentifier, desc, element);
		}
		catch (Exception e)
		{
			LOGGER.debug (e, "could not parse OMEX description");
		}
		return null;
	}
	
}