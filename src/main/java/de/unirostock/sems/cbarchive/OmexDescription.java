/**
 * 
 */
package de.unirostock.sems.cbarchive;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;



/**
 * @author martin
 * 
 */
public class OmexDescription
{
	
	public static final String						rdfNS					= "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String						dcNS					= "http://purl.org/dc/terms/";
	public static final String						vcNS					= "http://www.w3.org/2006/vcard/ns#";
	
	public static final SimpleDateFormat	dateFormater	= new SimpleDateFormat (
																												"yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");
	
	private String												description;
	private String												about;
	private Vector<VCard>									creators;
	private Date													created;
	public Vector<Date>										modified;



	public void debug ()
	{
		System.out.println ("omex descr" + this);
		System.out.println ("descr" + description);
		System.out.println ("about" + about);
		System.out.println ("created" + created);
		System.out.println ("#creators" + creators.size ());
		for (int i = 0; i < creators.size (); i++)
			creators.elementAt (i).debug ();
		System.out.println ("#mods" + modified.size ());
		for (int i = 0; i < modified.size (); i++)
			System.out.println ("mod: " + modified.elementAt (i));
	}
	
	public OmexDescription ()
	{
		creators = new Vector<VCard> ();
		modified = new Vector<Date> ();
		created = new Date ();
	}
	
	
	public boolean isEmpty ()
	{
		boolean haveCreator = false;
		for (VCard vc : creators)
			if (!vc.isEmpty ())
			{
				haveCreator = true;
				break;
			}
		return description != null && description.length () > 0
			&& creators.size () > 0 && haveCreator;
	}
	
	
	public static final Attr getResAttr (Document doc)
	{
		return getAttr (doc, OmexDescription.rdfNS, "rdf:parseType", "Resource");
	}
	
	
	public static final Attr getAttr (Document doc, String ns, String name,
		String value)
	{
		Attr attr = doc.createAttributeNS (ns, name);
		attr.setValue (value);
		return attr;
	}
	
	
	public void toXML (Document doc, Element parent)
	{
		if (modified.size () < 1)
			modified.add (new Date ());
		
		Element Description = doc.createElementNS (OmexDescription.rdfNS,
			"rdf:Description");
		Description.setAttributeNode (OmexDescription.getAttr (doc, OmexDescription.rdfNS, "rdf:about", about));
		parent.appendChild (Description);
		
		if (description != null && description.length () > 0)
		{
			Element description = doc.createElementNS (OmexDescription.dcNS,
				"dcterms:description");
			description.appendChild (doc.createTextNode (this.description));
			Description.appendChild (description);
		}
		
		// vcards
		for (VCard vc : creators)
		{
			vc.toXml (doc, Description);
		}
		
		
		if (created != null)
		{
			Element created = doc.createElementNS (OmexDescription.dcNS,
				"dcterms:created");
			created.setAttributeNode (OmexDescription.getResAttr (doc));
			Element W3CDTF = doc.createElementNS (OmexDescription.dcNS,
				"dcterms:W3CDTF");
			W3CDTF.appendChild (doc.createTextNode (dateFormater.format (this.created)));
			created.appendChild (W3CDTF);
			Description.appendChild (created);
		}
		
		for (Date date : modified)
		{
			Element modified = doc.createElementNS (OmexDescription.dcNS,
				"dcterms:modified");
			modified.setAttributeNode (OmexDescription.getResAttr (doc));
			Element modW3CDTF = doc.createElementNS (OmexDescription.dcNS,
				"dcterms:W3CDTF");
			modW3CDTF.appendChild (doc.createTextNode (dateFormater.format (date)));
			modified.appendChild (modW3CDTF);
			Description.appendChild (modified);
		}
		
	}
	
	
	public OmexDescription (Element element) throws DOMException, ParseException
	{
		creators = new Vector<VCard> ();
		modified = new Vector<Date> ();
		
		about = element.getAttributeNS (rdfNS, "about");
		
		NodeList list = element.getElementsByTagNameNS (dcNS, "description");
		if (list.getLength () > 0)
			description = list.item (0).getTextContent ();
		
		list = element.getElementsByTagNameNS (dcNS, "creator");
		if (list.getLength () > 0)
			for (int i = 0; i < list.getLength (); i++)
				creators.add (new VCard ((Element) list.item (i)));
		
		list = element.getElementsByTagNameNS (dcNS, "created");
		if (list.getLength () > 0)
		{
			list = ((Element) list.item (0)).getElementsByTagNameNS (dcNS, "W3CDTF");
			if (list.getLength () > 0)
				created = dateFormater.parse (list.item (0).getTextContent ());
		}
		
		list = element.getElementsByTagNameNS (dcNS, "modified");
		if (list.getLength () > 0)
		{
			for (int i = 0; i < list.getLength (); i++)
			{
				NodeList date = ((Element) list.item (i)).getElementsByTagNameNS (dcNS,
					"W3CDTF");
				if (date.getLength () > 0)
					modified.add (dateFormater.parse (date.item (0).getTextContent ()));
			}
		}
		
	}
}
