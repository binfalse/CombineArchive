/**
 * CombineArchive - a JAVA library to read/write/create/.. CombineArchives
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

import java.util.List;

import org.jdom2.Element;
import org.json.simple.JSONObject;



// TODO: Auto-generated Javadoc
/**
 * The Class VCard representing a VCard entity of the Omex description.
 * 
 * @author martin
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
		return familyName == null || givenName == null || familyName.length () < 1
			|| givenName.length () < 1;
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
			OmexDescription.vcNS);
		if (list.size () > 0)
			familyName = list.get (0).getText ();
		list = Utils.getElementsByTagName (element, "given-name",
			OmexDescription.vcNS);
		if (list.size () > 0)
			givenName = list.get (0).getText ();
		list = Utils.getElementsByTagName (element, "email", OmexDescription.vcNS);
		if (list.size () > 0)
			email = list.get (0).getText ();
		list = Utils.getElementsByTagName (element, "organization-name",
			OmexDescription.vcNS);
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
		
		Element creator = new Element ("creator", OmexDescription.dcNS);
		Element bag = new Element ("Bag", OmexDescription.rdfNS);
		Element li = new Element ("li", OmexDescription.rdfNS);
		li.setAttribute ("parseType", "Resource", OmexDescription.rdfNS);
		
		if ( (familyName != null && familyName.length () > 0)
			|| (givenName != null && givenName.length () > 0))
		{
			Element n = new Element ("n", OmexDescription.vcNS);
			n.setAttribute ("parseType", "Resource", OmexDescription.rdfNS);
			if (familyName != null && familyName.length () > 0)
			{
				Element famName = new Element ("family-name", OmexDescription.vcNS);
				famName.setText (familyName);
				n.addContent (famName);
			}
			if (givenName != null && givenName.length () > 0)
			{
				Element givName = new Element ("given-name", OmexDescription.vcNS);
				givName.setText (givenName);
				n.addContent (givName);
			}
			li.addContent (n);
		}
		
		if (email != null && email.length () > 0)
		{
			Element mail = new Element ("email", OmexDescription.vcNS);
			mail.setText (email);
			li.addContent (mail);
		}
		
		if (organization != null && organization.length () > 0)
		{
			Element org = new Element ("org", OmexDescription.vcNS);
			Element orgName = new Element ("organization-name", OmexDescription.vcNS);
			orgName.setText (organization);
			org.setAttribute ("parseType", "Resource", OmexDescription.rdfNS);
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
