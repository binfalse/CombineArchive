/**
 * 
 */
package de.unirostock.sems.cbarchive;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * @author martin
 *
 */
public class VCard
{
	private String familyName;
	private String givenName;
	private String email;
  private String organization;

	public void debug ()
	{
		System.out.println ("familyName: " + familyName);
		System.out.println ("givenName: " + givenName);
		System.out.println ("email: " + email);
		System.out.println ("organization: " + organization);
	}
  
  public boolean isEmpty ()
  {
  	return familyName == null || givenName == null || familyName.length () < 1 || givenName.length () < 1;
  }
	

  public VCard()
  {
  }
  
  public VCard (Element element)
	{
	  NodeList list = element.getElementsByTagNameNS (OmexDescription.vcNS, "family-name");
	  if (list.getLength () > 0)
	  	familyName = list.item (0).getTextContent ();
	  list = element.getElementsByTagNameNS(OmexDescription.vcNS, "given-name");
	  if (list.getLength () > 0)
	  	givenName = list.item (0).getTextContent ();
	  list = element.getElementsByTagNameNS(OmexDescription.vcNS, "email");
	  if (list.getLength () > 0)
	  	email = list.item (0).getTextContent ();
	  list = element.getElementsByTagNameNS(OmexDescription.vcNS, "organization-name");
	  if (list.getLength () > 0)
	  	organization = list.item (0).getTextContent ();
	}
  
  public void toXml (Document doc, Element parent)
  {
  	if (isEmpty ())
  		return;
  	
  	Element creator = doc.createElementNS (OmexDescription.dcNS, "dcterms:creator");
  	Element bag = doc.createElementNS (OmexDescription.rdfNS, "rdf:Bag");
  	Element li = doc.createElementNS (OmexDescription.rdfNS, "rdf:li");
		li.setAttributeNode (OmexDescription.getResAttr (doc));
		
		if ((familyName != null && familyName.length () > 0) || (givenName != null && givenName.length () > 0))
		{
	  	Element n = doc.createElementNS (OmexDescription.vcNS, "vCard:n");
			n.setAttributeNode (OmexDescription.getResAttr (doc));
			if (familyName != null && familyName.length () > 0)
			{
		  	Element famName = doc.createElementNS (OmexDescription.vcNS, "vCard:family-name");
				famName.appendChild (doc.createTextNode (familyName));
		  	n.appendChild (famName);
			}
			if (givenName != null && givenName.length () > 0)
			{
		  	Element givName = doc.createElementNS (OmexDescription.vcNS, "vCard:given-name");
				givName.appendChild (doc.createTextNode (givenName));
		  	n.appendChild (givName);
			}
	  	li.appendChild (n);
		}

		if (email != null && email.length () > 0)
		{
	  	Element mail = doc.createElementNS (OmexDescription.vcNS, "vCard:email");
			mail.appendChild (doc.createTextNode (email));
	  	li.appendChild (mail);
		}

		if (organization != null && organization.length () > 0)
		{
	  	Element org = doc.createElementNS (OmexDescription.vcNS, "vCard:org");
	  	Element orgName = doc.createElementNS (OmexDescription.vcNS, "vCard:organization-name");
			orgName.appendChild (doc.createTextNode (organization));
			org.setAttributeNode (OmexDescription.getResAttr (doc));
	  	org.appendChild (orgName);
	  	li.appendChild (org);
		}
		
  	
  	creator.appendChild (bag);
  	bag.appendChild (li);
  	
  	parent.appendChild (creator);
  }

}
