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
package de.unirostock.sems.cbarchive.meta.omex;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import de.unirostock.sems.cbarchive.Utils;



/**
 * The Class OmexDescription to parse and create meta data for entries in
 * CombineArchives.
 * 
 * @author martin
 */
public class OmexDescription
{
	
	/** The description. */
	private String												description;
	
	/** The creators. */
	private Vector<VCard>									creators;
	
	/** The date created. */
	private Date													created;
	
	/** The dates modified. */
	public Vector<Date>										modified;
	
	
	
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
	 * Gets the creators.
	 * 
	 * @return the creators
	 */
	public Vector<VCard> getCreators ()
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
	public Vector<Date> getModified ()
	{
		return modified;
	}
	
	
	/**
	 * Instantiates a new omex description.
	 */
	public OmexDescription ()
	{
		creators = new Vector<VCard> ();
		modified = new Vector<Date> ();
		created = new Date ();
	}
	
	
	/**
	 * Instantiates a new omex description.
	 * 
	 * @param creators
	 *          the creators
	 * @param modified
	 *          the date of modifications
	 */
	public OmexDescription (Vector<VCard> creators, Vector<Date> modified)
	{
		this.creators = creators;
		this.modified = modified;
		this.created = new Date ();
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
		this.creators = new Vector<VCard> ();
		this.creators.add (creator);
		this.modified = new Vector<Date> ();
		this.created = created;
	}
	
	
	/**
	 * Instantiates a new omex description.
	 * 
	 * @param creators
	 *          the creators
	 * @param created
	 *          the date of creation
	 */
	public OmexDescription (Vector<VCard> creators, Date created)
	{
		this.creators = creators;
		this.modified = new Vector<Date> ();
		this.created = created;
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
	public OmexDescription (Vector<VCard> creators, Vector<Date> modified,
		Date created)
	{
		this.creators = creators;
		this.modified = modified;
		this.created = created;
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
		return !((description != null && description.length () > 0) || haveCreator || created != null);
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
			modified = new Vector<Date> ();
		
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
		creators = new Vector<VCard> ();
		modified = new Vector<Date> ();
		
		
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
