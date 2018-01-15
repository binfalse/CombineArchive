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

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.jdom2.Document;
import org.jdom2.Element;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.cbarchive.Utils;



/**
 * The abstract Class MetaDataObject representing some meta data.
 * 
 * @author Martin Scharm
 */
public abstract class MetaDataObject
{
	
	/** The entry this is all about. */
	protected MetaDataHolder	about;
	
	/** The fragment identifier. */
	protected String					fragmentIdentifier;
	
	/** The description. */
	protected Element					description;
	
	
	/**
	 * Instantiates a new meta data object.
	 * 
	 * @param describingElement
	 *          the element rooting the subtree that describes an entity
	 */
	public MetaDataObject (Element describingElement)
	{
		this.about = null;
		this.fragmentIdentifier = null;
		this.description = describingElement;
	}
	
	
	/**
	 * Sets the about.
	 * 
	 * @param about
	 *          the path to the entity described by this object
	 */
	public void setAbout (MetaDataHolder about)
	{
		this.about = about;
		this.fragmentIdentifier = null;
		description.setAttribute ("about", getAbout (), Utils.rdfNS);
	}
	
	
	/**
	 * Sets the about.
	 * 
	 * @param about
	 *          the path to the entity described by this object
	 * @param fragmentIdentifier
	 *          the fragment identifier pointing into <code>about</code>
	 */
	public void setAbout (MetaDataHolder about, String fragmentIdentifier)
	{
		this.about = about;
		this.fragmentIdentifier = fragmentIdentifier;
		description.setAttribute ("about", getAbout (), Utils.rdfNS);
	}
	
	
	/**
	 * Gets the about.
	 * 
	 * @return the path to the entity described by this object
	 */
	public String getAbout ()
	{
		if (about == null)
			return "";
		
		if (fragmentIdentifier != null)
			return about.getEntityPath () + "#" + fragmentIdentifier;
		
		return about.getEntityPath ();
	}
	
	
	/**
	 * Inject the description into <code>parent</code>.
	 * 
	 * @param parent
	 *          the parent element that will host the description
	 */
	public abstract void injectDescription (Element parent);
	
	
	/**
	 * Get the XML description of {@link #getAbout()}.
	 * 
	 * @return the XML subtree rooting the description
	 */
	public Element getXmlDescription ()
	{
		description.setAttribute ("about", getAbout (), Utils.rdfNS);
		return description;
	}
	
	
	/**
	 * Checks if two meta data objects are equal, but it neglects the paths to the
	 * meta data holder.
	 * 
	 * Thus, it just checks the fragment identifier and the actual meta data
	 *
	 * @param otherMeta
	 *          the other meta data object
	 * @return true, if both objects are equal w/o respect to the path
	 */
	public boolean equalsPathNoMatter (MetaDataObject otherMeta)
	{
		if (otherMeta == null)
			return false;
		
		if ( (fragmentIdentifier == null && otherMeta.fragmentIdentifier == null)
			|| (fragmentIdentifier != null && otherMeta.fragmentIdentifier != null
				&& fragmentIdentifier.equals (otherMeta.fragmentIdentifier)))
		{
			try
			{
				Element elementOne = description.clone ().setAttribute ("about", "", Utils.rdfNS);
				Element elementTwo = otherMeta.description.clone ().setAttribute ("about", "", Utils.rdfNS);
				
				String one = Utils
					.prettyPrintDocument (new Document (elementOne));
				String two = Utils
					.prettyPrintDocument (new Document (elementTwo));
				return one.equals (two);
			}
			catch (IOException | TransformerException e)
			{
				e.printStackTrace ();
				LOGGER.error (e, "cannot convert jdom element tree to string");
			}
		}
		return false;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (obj == null || !MetaDataObject.class.isAssignableFrom (obj.getClass ()))
			return false;
		
		MetaDataObject otherMeta = (MetaDataObject) obj;
		
		if (about == otherMeta.about && equalsPathNoMatter (otherMeta))
			return true;
		return false;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	abstract public MetaDataObject clone ();
}
