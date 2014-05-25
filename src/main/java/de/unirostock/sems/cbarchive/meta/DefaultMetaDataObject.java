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
 * The Class DefaultMetaDataObject, a fall back if we do not understand the meta
 * data that is encoded in the XML.
 * 
 * @author Martin Scharm
 */
public class DefaultMetaDataObject
	extends MetaDataObject
{
	
	/** The description. */
	protected Element	description;
	
	
	/**
	 * Instantiates a new default meta data object.
	 * 
	 * @param about
	 *          the entry described by this object
	 * @param descriptionElement
	 *          the description element
	 */
	public DefaultMetaDataObject (ArchiveEntry about, Element descriptionElement)
	{
		super (about);
		this.description = descriptionElement;
	}
	
	
	/**
	 * Instantiates a new default meta data object.
	 * 
	 * @param about
	 *          the entry described by this object
	 * @param fragmentIdentifier
	 *          the fragment identifier pointing into <code>about</code>
	 * @param descriptionElement
	 *          the description element
	 */
	public DefaultMetaDataObject (ArchiveEntry about, String fragmentIdentifier,
		Element descriptionElement)
	{
		super (about, fragmentIdentifier);
		this.description = descriptionElement;
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
		for (Element child : description.getChildren ())
			parent.addContent (child.clone ());
	}
	
	
	/**
	 * Gets the node rooting the description(s).
	 * 
	 * @return the description
	 */
	public Element getDescription ()
	{
		return description;
	}
	
	
	/**
	 * Try to read a meta data object.
	 * 
	 * @param element
	 *          the element rooting the meta data subtree
	 * @param about
	 *          the entry the is described by <code>element</code>
	 * @param fragmentIdentifier
	 *          the optional fragment identifier pointing into <code>about</code>
	 *          (leave <code>null</code> if in doubt)
	 * @return the default meta data object
	 */
	public static DefaultMetaDataObject tryToRead (Element element,
		ArchiveEntry about, String fragmentIdentifier)
	{
		if (fragmentIdentifier == null)
			return new DefaultMetaDataObject (about, element);
		return new DefaultMetaDataObject (about, fragmentIdentifier, element);
	}
	
}
