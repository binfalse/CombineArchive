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
package de.unirostock.sems.cbarchive.meta.omex;

import java.util.List;

import org.jdom2.Element;
import org.json.simple.JSONObject;

import de.unirostock.sems.cbarchive.Utils;



/**
 * The Class VCard representing a VCard entity of the Omex description.
 * 
 * @author martin scharm
 */
public class VCard
{
	
	/** The family name. */
	private String	familyName;
	
	/** The given name. */
	private String	givenName;
	
	/** The email. */
	private String	email;
	
	/** The organization. */
	private String	organization;
	
	
	/**
	 * Checks if the VCard is empty.
	 * 
	 * @return true, if it is empty
	 */
	public boolean isEmpty ()
	{
		return (familyName == null || familyName.length () < 1)
			&& (givenName == null || givenName.length () < 1)
			&& (email == null || email.length () < 1);
	}
	
	
	/**
	 * Instantiates an empty VCard.
	 * 
	 */
	public VCard ()
	{
		this.familyName = null;
		this.givenName = null;
		this.email = null;
		this.organization = null;
	}
	
	
	/**
	 * Instantiates a new VCard.
	 * 
	 * @param familyName
	 *          the family name
	 * @param givenName
	 *          the given name
	 * @param email
	 *          the email
	 * @param organization
	 *          the organization
	 */
	public VCard (String familyName, String givenName, String email,
		String organization)
	{
		this.familyName = familyName;
		this.givenName = givenName;
		this.email = email;
		this.organization = organization;
	}
	
	
	/**
	 * Instantiates a new VCard from an entity of the Omex description.
	 * 
	 * @param element
	 *          the XML element which roots the VCard entity
	 */
	public VCard (Element element)
	{
		List<Element> list = Utils.getElementsByTagName (element, "family-name",
			Utils.vcNS);
		if (list.size () > 0)
			familyName = list.get (0).getText ();
		list = Utils.getElementsByTagName (element, "given-name", Utils.vcNS);
		if (list.size () > 0)
			givenName = list.get (0).getText ();
		list = Utils.getElementsByTagName (element, "email", Utils.vcNS);
		if (list.size () > 0)
			email = list.get (0).getText ();
		list = Utils
			.getElementsByTagName (element, "organization-name", Utils.vcNS);
		if (list.size () > 0)
			organization = list.get (0).getText ();
	}
	
	
	/**
	 * Appends the VCard tree to an XML element.
	 * 
	 * @param parent
	 *          the parent element in the XML tree
	 */
	public void toXml (Element parent)
	{
		if (isEmpty ())
			return;
		
		Element creator = new Element ("creator", Utils.dcNS);
		Element bag = new Element ("Bag", Utils.rdfNS);
		Element li = new Element ("li", Utils.rdfNS);
		li.setAttribute ("parseType", "Resource", Utils.rdfNS);
		
		if ( (familyName != null && familyName.length () > 0)
			|| (givenName != null && givenName.length () > 0))
		{
			Element n = new Element ("n", Utils.vcNS);
			n.setAttribute ("parseType", "Resource", Utils.rdfNS);
			if (familyName != null && familyName.length () > 0)
			{
				Element famName = new Element ("family-name", Utils.vcNS);
				famName.setText (familyName);
				n.addContent (famName);
			}
			if (givenName != null && givenName.length () > 0)
			{
				Element givName = new Element ("given-name", Utils.vcNS);
				givName.setText (givenName);
				n.addContent (givName);
			}
			li.addContent (n);
		}
		
		if (email != null && email.length () > 0)
		{
			Element mail = new Element ("email", Utils.vcNS);
			mail.setText (email);
			li.addContent (mail);
		}
		
		if (organization != null && organization.length () > 0)
		{
			Element org = new Element ("org", Utils.vcNS);
			Element orgName = new Element ("organization-name", Utils.vcNS);
			orgName.setText (organization);
			org.setAttribute ("parseType", "Resource", Utils.rdfNS);
			org.addContent (orgName);
			li.addContent (org);
		}
		
		creator.addContent (bag);
		bag.addContent (li);
		
		parent.addContent (creator);
	}
	
	
	/**
	 * Gets the family name.
	 * 
	 * @return the family name
	 */
	public String getFamilyName ()
	{
		return familyName;
	}
	
	
	/**
	 * Gets the given name.
	 * 
	 * @return the given name
	 */
	public String getGivenName ()
	{
		return givenName;
	}
	
	
	/**
	 * Gets the email address.
	 * 
	 * @return the email
	 */
	public String getEmail ()
	{
		return email;
	}
	
	
	/**
	 * Gets the organization.
	 * 
	 * @return the organization
	 */
	public String getOrganization ()
	{
		return organization;
	}
	
	
	/**
	 * Sets the family name.
	 * 
	 * @param familyName
	 *          the new family name
	 */
	public void setFamilyName (String familyName)
	{
		this.familyName = familyName;
	}
	
	
	/**
	 * Sets the given name.
	 * 
	 * @param givenName
	 *          the new given name
	 */
	public void setGivenName (String givenName)
	{
		this.givenName = givenName;
	}
	
	
	/**
	 * Sets the email.
	 * 
	 * @param email
	 *          the new email
	 */
	public void setEmail (String email)
	{
		this.email = email;
	}
	
	
	/**
	 * Sets the organization.
	 * 
	 * @param organization
	 *          the new organization
	 */
	public void setOrganization (String organization)
	{
		this.organization = organization;
	}
	
	
	/**
	 * Export this VCard as a JSON object.
	 * 
	 * @return the object
	 */
	@SuppressWarnings("unchecked")
	public Object toJsonObject ()
	{
		JSONObject descr = new JSONObject ();
		descr.put ("familyName", familyName);
		descr.put ("givenName", givenName);
		descr.put ("email", email);
		descr.put ("organization", organization);
		return descr;
	}
}
