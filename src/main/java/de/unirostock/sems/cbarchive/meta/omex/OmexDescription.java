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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jdom2.Element;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import de.unirostock.sems.cbarchive.Utils;



/**
 * The Class OmexDescription to parse and create meta data for entries in
 * CombineArchives.
 * 
 * @author martin scharm
 */
public class OmexDescription
{
	
	/** The description. */
	private String			description;
	
	/** The creators. */
	private List<VCard>	creators;
	
	/** The date created. */
	private Date				created;
	
	/** The dates modified. */
	public List<Date>		modified;
	
	
	/**
	 * Gets the description.
	 * 
	 * @return the description
	 */
	public String getDescription ()
	{
		return description;
	}
	
	
	/**
	 * Sets the description.
	 * 
	 * @param description
	 * 				  the description
	 */
	public void setDescription (String description)
	{
		this.description = description;
	}
	
	
	/**
	 * Gets the creators.
	 * 
	 * @return the creators
	 */
	public List<VCard> getCreators ()
	{
		return creators;
	}
	
	
	/**
	 * Gets the date created.
	 * 
	 * @return the date created
	 */
	public Date getCreated ()
	{
		return created;
	}
	
	
	/**
	 * Gets the dates modified.
	 * 
	 * @return the dates modified
	 */
	public List<Date> getModified ()
	{
		return modified;
	}
	
	
	/**
	 * Instantiates a new omex description.
	 * 
	 * @param description
	 *          the textual description
	 */
	public OmexDescription (String description)
	{
		creators = new ArrayList<VCard> ();
		modified = new ArrayList<Date> ();
		created = new Date ();
		this.description = description;
	}
	
	
	/**
	 * Instantiates a new omex description.
	 * 
	 * @param creators
	 *          the creators
	 * @param modified
	 *          the date of modifications
	 * @param description
	 *          the textual description
	 */
	public OmexDescription (List<VCard> creators, List<Date> modified,
		String description)
	{
		this.creators = creators;
		this.modified = modified;
		this.created = new Date ();
		this.description = description;
	}
	
	
	/**
	 * Instantiates a new omex description.
	 * 
	 * @param creator
	 *          the creator
	 * @param created
	 *          the date of creation
	 * @param description
	 *          the textual description
	 */
	public OmexDescription (VCard creator, Date created, String description)
	{
		this.creators = new ArrayList<VCard> ();
		this.creators.add (creator);
		this.modified = new ArrayList<Date> ();
		this.created = created;
		this.description = description;
	}
	
	
	/**
	 * Instantiates a new omex description.
	 * 
	 * @param creators
	 *          the creators
	 * @param created
	 *          the date of creation
	 * @param description
	 *          the textual description
	 */
	public OmexDescription (List<VCard> creators, Date created, String description)
	{
		this.creators = creators;
		this.modified = new ArrayList<Date> ();
		this.created = created;
		this.description = description;
	}
	
	
	/**
	 * Instantiates a new omex description.
	 * 
	 * @param creators
	 *          the creators
	 * @param modified
	 *          the date of modifications
	 * @param created
	 *          the date of creation
	 * @param description
	 *          the textual description
	 */
	public OmexDescription (List<VCard> creators, List<Date> modified,
		Date created, String description)
	{
		this.creators = creators;
		this.modified = modified;
		this.created = created;
		this.description = description;
	}
	
	
	/**
	 * Instantiates a new omex description.
	 */
	public OmexDescription ()
	{
		creators = new ArrayList<VCard> ();
		modified = new ArrayList<Date> ();
		created = new Date ();
		description = null;
	}
	
	
	/**
	 * Instantiates a new omex description.
	 * 
	 * @param creators
	 *          the creators
	 * @param modified
	 *          the date of modifications
	 */
	public OmexDescription (List<VCard> creators, List<Date> modified)
	{
		this.creators = creators;
		this.modified = modified;
		this.created = new Date ();
		description = null;
	}
	
	
	/**
	 * Instantiates a new omex description.
	 * 
	 * @param creator
	 *          the creator
	 * @param created
	 *          the date of creation
	 */
	public OmexDescription (VCard creator, Date created)
	{
		this.creators = new ArrayList<VCard> ();
		this.creators.add (creator);
		this.modified = new ArrayList<Date> ();
		this.created = created;
		description = null;
	}
	
	
	/**
	 * Instantiates a new omex description.
	 * 
	 * @param creators
	 *          the creators
	 * @param created
	 *          the date of creation
	 */
	public OmexDescription (List<VCard> creators, Date created)
	{
		this.creators = creators;
		this.modified = new ArrayList<Date> ();
		this.created = created;
		description = null;
	}
	
	
	/**
	 * Instantiates a new omex description.
	 * 
	 * @param creators
	 *          the creators
	 * @param modified
	 *          the date of modifications
	 * @param created
	 *          the date of creation
	 */
	public OmexDescription (List<VCard> creators, List<Date> modified,
		Date created)
	{
		this.creators = creators;
		this.modified = modified;
		this.created = created;
		description = null;
	}
	
	
	/**
	 * Checks if description is empty.
	 * 
	 * @return true, if is empty
	 */
	public boolean isEmpty ()
	{
		boolean haveCreator = false;
		if (creators != null)
			for (VCard vc : creators)
				if (!vc.isEmpty ())
				{
					haveCreator = true;
					break;
				}
		return ! ( (description != null && description.length () > 0)
			|| haveCreator || created != null);
	}
	
	
	/**
	 * Attach the description to an XML tree.
	 * 
	 * @param parent
	 *          the parent element
	 */
	public void toXML (Element parent)
	{
		if (modified == null)
			modified = new ArrayList<Date> ();
		
		if (modified.size () < 1)
		{
			if (created == null)
				created = new Date ();
			modified.add (created);
		}
		
		if (description != null && description.length () > 0)
		{
			Element description = new Element ("description", Utils.dcNS);
			description.setText (this.description);
			parent.addContent (description);
		}
		
		// vcards
		if (creators != null)
			for (VCard vc : creators)
				vc.toXml (parent);
		
		if (created != null)
		{
			Element created = new Element ("created", Utils.dcNS);
			created.setAttribute ("parseType", "Resource", Utils.rdfNS);
			Element W3CDTF = new Element ("W3CDTF", Utils.dcNS);
			W3CDTF.setText (Utils.dateFormater.format (this.created));
			created.addContent (W3CDTF);
			parent.addContent (created);
		}
		
		for (Date date : modified)
		{
			Element modified = new Element ("modified", Utils.dcNS);
			modified.setAttribute ("parseType", "Resource", Utils.rdfNS);
			Element modW3CDTF = new Element ("W3CDTF", Utils.dcNS);
			modW3CDTF.setText (Utils.dateFormater.format (date));
			modified.addContent (modW3CDTF);
			parent.addContent (modified);
		}
		
	}
	
	
	/**
	 * Instantiates a new omex description parsed from an XML subtree.
	 * 
	 * @param parent
	 *          the parent element
	 * @throws ParseException
	 *           the parse exception
	 */
	public OmexDescription (Element parent) throws ParseException
	{
		creators = new ArrayList<VCard> ();
		modified = new ArrayList<Date> ();
		
		List<Element> list = Utils.getElementsByTagName (parent, "description",
			Utils.dcNS);
		if (list.size () > 0)
			description = list.get (0).getText ();
		
		list = Utils.getElementsByTagName (parent, "creator", Utils.dcNS);
		if (list.size () > 0)
			for (int i = 0; i < list.size (); i++)
				creators.add (new VCard (list.get (i)));
		
		list = Utils.getElementsByTagName (parent, "created", Utils.dcNS);
		if (list.size () > 0)
		{
			list = Utils.getElementsByTagName (list.get (0), "W3CDTF", Utils.dcNS);
			if (list.size () > 0)
				created = Utils.dateFormater.parse (list.get (0).getText ());
		}
		
		list = Utils.getElementsByTagName (parent, "modified", Utils.dcNS);
		if (list.size () > 0)
		{
			for (int i = 0; i < list.size (); i++)
			{
				List<Element> date = Utils.getElementsByTagName (list.get (i),
					"W3CDTF", Utils.dcNS);
				if (date.size () > 0)
					modified.add (Utils.dateFormater.parse (date.get (0).getText ()));
			}
		}
		
	}
	
	
	/**
	 * Export a JSON description of this OMEX description.
	 * 
	 * @return the JSON object
	 */
	@SuppressWarnings("unchecked")
	public JSONObject toJsonDescription ()
	{
		JSONObject descr = new JSONObject ();
		descr.put ("description", description);
		JSONArray array = new JSONArray ();
		for (VCard c : creators)
			array.add (c.toJsonObject ());
		descr.put ("creators", array);
		descr.put ("created", Utils.dateFormater.format (created));
		array = new JSONArray ();
		for (Date d : modified)
			array.add (Utils.dateFormater.format (d));
		descr.put ("modified", array);
		return descr;
	}
}
