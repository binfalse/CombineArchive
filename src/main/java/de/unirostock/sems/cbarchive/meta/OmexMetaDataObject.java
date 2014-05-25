package de.unirostock.sems.cbarchive.meta;

import org.jdom2.Element;

import de.binfalse.bflog.LOGGER;
import de.unirostock.sems.cbarchive.ArchiveEntry;
import de.unirostock.sems.cbarchive.meta.omex.OmexDescription;


public class OmexMetaDataObject
	extends MetaDataObject
{
	private OmexDescription description;
	
	public OmexMetaDataObject (ArchiveEntry about, OmexDescription description)
	{
		super (about);
		this.description = description;
	}
	
	
	public OmexMetaDataObject (ArchiveEntry about, String fragmentIdentifier, OmexDescription description)
	{
		super (about, fragmentIdentifier);
		this.description = description;
	}
	
	
	/*public OmexMetaDataObject (String alternativeAbout, OmexDescription description)
	{
		super (alternativeAbout);
		this.description = description;
	}*/
	
	
	@Override
	public void injectDescription (Element parent)
	{
		description.toXML (parent);
	}
	
	public OmexDescription getOmexDescription ()
	{
		return description;
	}


	/*public static OmexMetaDataObject tryToRead (Element element, ArchiveEntry about)
	{
		try
		{
			OmexDescription desc = new OmexDescription (element);
			return new OmexMetaDataObject (about, desc);
		}
		catch (Exception e)
		{
			LOGGER.debug (e, "could not parse OMEX description");
		}
		return null;
	}*/


	public static OmexMetaDataObject tryToRead (Element element, ArchiveEntry about,
		String fragmentIdentifier)
	{
		try
		{
			OmexDescription desc = new OmexDescription (element);
			if (desc.isEmpty ())
				return null;
			System.out.println (about.getRelativeName () + " -- " + fragmentIdentifier + " -> " + desc);
			if (fragmentIdentifier == null)
				return new OmexMetaDataObject (about, desc);
			return new OmexMetaDataObject (about, fragmentIdentifier, desc);
		}
		catch (Exception e)
		{
			LOGGER.debug (e, "could not parse OMEX description");
		}
		return null;
	}


	/*public static OmexMetaDataObject tryToRead (Element element, String alternativeAbout)
	{
		try
		{
			OmexDescription desc = new OmexDescription (element);
			return new OmexMetaDataObject (alternativeAbout, desc);
		}
		catch (Exception e)
		{
			LOGGER.debug (e, "could not parse OMEX description");
		}
		return null;
	}*/
	
}
