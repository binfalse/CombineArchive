/**
 * CombineArchive - a JAVA library to read/write/create/... CombineArchives
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
package de.unirostock.sems.cbarchive;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import org.jdom2.JDOMException;

import de.unirostock.sems.cbarchive.meta.MetaDataFile;
import de.unirostock.sems.cbarchive.meta.MetaDataHolder;



/**
 * The Class ArchiveEntry represents a single entry in a CombineArchive.
 * 
 * @author martin scharm
 */
public class ArchiveEntry
	extends MetaDataHolder
{
	
	/** The archive containing this entry. */
	private CombineArchive	archive;
	
	/** The relative path name to that file. */
	private Path						relativeName;
	
	/** The format, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>. */
	private URI					format;
	
	
	/**
	 * Instantiates a new archive entry.
	 * 
	 * @param archive
	 *          the corresponding CombineArchive
	 * @param relativeName
	 *          the relative path name within <code>archive</code>
	 * @param format
	 *          the format, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>
	 */
	public ArchiveEntry (CombineArchive archive, Path relativeName, URI format)
	{
		super ();
		this.archive = archive;
		this.relativeName = relativeName;
		this.format = format;
	}
	
	
	/**
	 * Extract this file to <code>target</code>. If <code>target</code> is a
	 * directory we'll write to <code>target/getFileName ()</code>.
	 * 
	 * @param target
	 *          the target to write this item to.
	 * @return the file (=<code>target</code>)
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 */
	public File extractFile (File target) throws IOException
	{
		return archive.extract (relativeName, target);
	}
	
	
	/**
	 * Gets the archive that contains this entry.
	 *
	 * @return the archive
	 */
	public CombineArchive getArchive ()
	{
		return archive;
	}
	
	
	/**
	 * Checks if is main entry.
	 *
	 * @return true, if is main entry
	 */
	public boolean isMainEntry ()
	{
		return archive.getMainEntries ().contains (this);
	}
	
	
	/**
	 * Gets the corresponding file.
	 * 
	 * @return the file
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * 
	 * @deprecated as of version 0.6, replaced by {@link #extractFile (File target)}
	 */
	@Deprecated
	public File getFile () throws IOException
	{
		return extractFile (File.createTempFile ("combineArchive",
			Utils.getExtension (relativeName.getFileName ().toString ())));
	}
	
	
	/**
	 * Gets the path to this entry.
	 * <p>
	 * Be aware that this path points to the entry as it is zipped in the archive.
	 * Thus, some operations might fail or result in unexpected behaviour.
	 * </p>
	 * 
	 * @return the path
	 */
	public Path getPath ()
	{
		return relativeName;
	}
	
	
	/**
	 * Gets the file name (w/o path) of this entry in the archive.
	 * 
	 * @return the relative path name
	 */
	public String getFileName ()
	{
		return relativeName.getFileName ().toString ();
	}
	
	
	/**
	 * Gets the relative path name of this file in the archive.
	 * <p>
	 * The path will usually start with '<code>/</code>', but do not rely on that.
	 * Depending on the archive it might also start with '<code>./</code>' or
	 * without anything.
	 * </p>
	 * 
	 * @return the relative path name
	 */
	public String getFilePath ()
	{
		return relativeName.toString ();
	}
	
	
	/**
	 * Gets the path to this entity. Equals {@link #getFilePath()} for entities of
	 * type {@link ArchiveEntry}.
	 * 
	 * @return the file path
	 */
	@Override
	public String getEntityPath ()
	{
		return getFilePath ();
	}
	
	
	/**
	 * Gets the format as reported by the archive's manifest.
	 * 
	 * @return the format, see <a href="https://sems.uni-rostock.de/trac/combine-ext/wiki/CombineFormatizer">CombineFormatizer</a>
	 */
	public URI getFormat ()
	{
		return format;
	}
	
	
	/**
	 * Sets the format of this entry.
	 * 
	 * @param format the format of this entry
	 */
	public void setFormat (URI format)
	{
		this.format = format;
	}
	
	
	/**
	 * Add all descriptions in
	 * <code>metaDataFile</code> (assuming all are about this entry).
	 * 
	 * @param metaDataFile
	 *          the file containing the meta data
	 * @return number of entries added, or -1 in case of an error
	 * @throws IOException
	 *           Signals that an I/O exception has occurred.
	 * @throws JDOMException
	 *           Signals problems with the jDOM parser
	 */
	public int addAllDescriptions (File metaDataFile)
		throws JDOMException,
			IOException
	{
		return MetaDataFile.addAllMetaToEntry (metaDataFile.toPath (), this);
	}
}
