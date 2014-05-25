package de.unirostock.sems.cbarchive.meta;

import org.jdom2.Element;

import de.unirostock.sems.cbarchive.ArchiveEntry;


public class DefaultMetaDataObject
	extends MetaDataObject
{
	Element description;
	
	

	public DefaultMetaDataObject (ArchiveEntry about, Element descriptionElement)
	{
		super (about);
		this.description = descriptionElement;
	}
	
	public DefaultMetaDataObject (ArchiveEntry about, String fragmentIdentifier, Element descriptionElement)
	{
		super (about, fragmentIdentifier);
		this.description = descriptionElement;
	}
	
	/*public DefaultMetaDataObject (String alternativeAbout, Element descriptionElement)
	{
		super (alternativeAbout);
		this.description = descriptionElement;
	}*/
	

	@Override
	public void injectDescription (Element parent)
	{
		for (Element child : description.getChildren ())
			parent.addContent (child.clone ());
	}

	/*public static DefaultMetaDataObject tryToRead (Element e, ArchiveEntry about)
	{
		return new DefaultMetaDataObject (about, e);
	}*/

	public static DefaultMetaDataObject tryToRead (Element e, ArchiveEntry about,
		String fragmentIdentifier)
	{
		if (fragmentIdentifier == null)
			return new DefaultMetaDataObject (about, e);
		return new DefaultMetaDataObject (about, fragmentIdentifier, e);
	}

	/*public static DefaultMetaDataObject tryToRead (Element e, String alternativeAbout)
	{
		return new DefaultMetaDataObject (alternativeAbout, e);
	}*/
	
}
