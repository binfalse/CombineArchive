/**
 * 
 */
package de.unirostock.sems.cbarchive;


/**
 * The Class CombineArchiveException representing an error while processing CombineArchives.
 *
 * @author Martin Scharm
 */
public class CombineArchiveException
	extends Exception
{
	private static final long	serialVersionUID	= 6173162561700007235L;

	/**
	 * Instantiates a new CombineArchiveException.
	 *
	 * @param msg the error message
	 */
	public CombineArchiveException (String msg)
	{
		super (msg);
	}
}
