/**
 * 
 */
package de.unirostock.sems.cbarchive.meta;

import org.jdom2.Element;

import de.unirostock.sems.cbarchive.ArchiveEntry;


/**
 * @author Martin Scharm
 *
 */
public abstract class MetaDataObject
{
	protected ArchiveEntry about;
	protected String fragmentIdentifier;
	//protected String alternativeAbout;

	public MetaDataObject (ArchiveEntry about)
	{
		this.about = about;
	}
	
	public MetaDataObject (ArchiveEntry about, String fragmentIdentifier)
	{
		this.about = about;
		this.fragmentIdentifier = fragmentIdentifier;
	}
	
	/*public MetaDataObject (String alternativeAbout)
	{
		this.alternativeAbout = alternativeAbout;
	}*/
	
	
	
	public String getAbout ()
	{
		/*if (alternativeAbout != null)
			return alternativeAbout;
		*/
		if (fragmentIdentifier != null)
			return about.getRelativeName () + "#" + fragmentIdentifier;
		
		return about.getRelativeName ();
	}
	
	public abstract void injectDescription (Element parent);
	
	

	/*public static MetaDataObject tryToRead (Element e, ArchiveEntry about)
	{
		return null;
	}*/
	public static MetaDataObject tryToRead (Element e, ArchiveEntry about, String fragmentIdentifier)
	{
		return null;
	}
	/*public static MetaDataObject tryToRead (Element e, String alternativeAbout)
	{
		return null;
	}*/
}
