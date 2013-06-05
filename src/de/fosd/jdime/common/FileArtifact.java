/*******************************************************************************
 * Copyright (c) 2013 Olaf Lessenich.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Olaf Lessenich - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package de.fosd.jdime.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import de.fosd.jdime.common.operations.MergeOperation;
import de.fosd.jdime.strategy.DirectoryStrategy;
import de.fosd.jdime.strategy.MergeStrategy;

/**
 * This class represents an artifact of a program.
 * 
 * @author Olaf Lessenich *
 */
public class FileArtifact extends Artifact<FileArtifact> {

	/**
	 * Logger.
	 */
	private static final Logger LOG = Logger.getLogger(FileArtifact.class);

	/**
	 * File in which the artifact is stored.
	 */
	private File file;
	
	/**
	 * Creates a new instance of an artifact.
	 * 
	 * @param file
	 *            where the artifact is stored
	 * @throws FileNotFoundException
	 *             FileNotFoundException
	 */
	public FileArtifact(final File file) throws FileNotFoundException {
		this(null, file);
	}

	/**
	 * Creates a new instance of an artifact.
	 * 
	 * @param revision
	 *            the artifact belongs to
	 * @param file
	 *            where the artifact is stored
	 * @throws FileNotFoundException
	 *             FileNotFoundException
	 */
	public FileArtifact(final Revision revision, final File file)
			throws FileNotFoundException {
		this(revision, file, true);
	}

	/**
	 * Creates a new instance of an artifact.
	 * 
	 * @param revision
	 *            the artifact belongs to
	 * @param file
	 *            where the artifact is stored
	 * @param checkPresence
	 *            If true, an exception is thrown when the file does not exist
	 * @throws FileNotFoundException
	 *             FileNotFoundException
	 */
	public FileArtifact(final Revision revision, final File file,
			final boolean checkPresence) throws FileNotFoundException {
		assert file != null;

		if (checkPresence && !file.exists()) {
			LOG.fatal("File not found: " + file.getAbsolutePath());
			throw new FileNotFoundException();
		}

		setRevision(revision);
		this.file = file;

		if (LOG.isTraceEnabled()) {
			LOG.trace("Artifact initialized: " + file.getPath());
			LOG.trace("Artifact exists: " + exists());
			LOG.trace("File exists: " + file.exists());
			if (exists()) {
				LOG.trace("Artifact isEmpty: " + isEmpty());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fosd.jdime.common.Artifact#addChild(
	 * de.fosd.jdime.common.Artifact)
	 */
	@Override
	public final FileArtifact addChild(final FileArtifact child) 
			throws IOException {
		assert (child != null);

		assert (!isLeaf()) 
				: "Child elements can not be added to leaf artifacts. "
				+ "isLeaf(" + this + ") = " + isLeaf();

		assert (getClass().equals(child.getClass())) 
				: "Can only add children of same type";

		FileArtifact myChild = new FileArtifact(getRevision(), new File(file
				+ File.separator + child.getId()), false);

		assert (myChild != null);
		return myChild;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fosd.jdime.common.Artifact#copyArtifact(de.fosd.jdime.common.Artifact)
	 */
	@Override
	public final void copyArtifact(final FileArtifact destination)
			throws IOException {
		assert (destination != null);
		assert (destination instanceof FileArtifact);

		FileArtifact dst = (FileArtifact) destination;

		if (dst.isFile()) {
			if (isFile()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Copying file " + this + " to file " + dst);
					LOG.debug("Destination already exists overwriting: "
							+ dst.exists());
				}

				FileUtils.copyFile(file, dst.file);
			} else {
				throw new UnsupportedOperationException(
						"When copying to a file, "
								+ "the source must also be a file.");
			}
		} else if (dst.isDirectory()) {
			if (isFile()) {
				assert (dst.exists()) : "Destination directory does not exist: "
						+ destination;
				if (LOG.isDebugEnabled()) {
					LOG.debug("Copying file " + this + " to directory "
							+ destination);
				}
				FileUtils.copyFileToDirectory(file, dst.file);
			} else if (isDirectory()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Copying directory " + this + " to directory "
							+ dst);
					LOG.debug("Destination already exists overwriting: "
							+ dst.exists());
				}
				FileUtils.copyDirectory(file, dst.file);
			}
		} else {
			LOG.fatal("Failed copying " + this + " to " + dst);
			LOG.fatal("isDirectory(" + this + ") = " + isDirectory());
			LOG.fatal("isDirectory(" + dst + ") = " + dst.isDirectory());
			throw new NotYetImplementedException(
					"Only copying files and directories is supported.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fosd.jdime.common.Artifact#createArtifact(boolean)
	 */
	@Override
	public final void createArtifact(final boolean isLeaf) throws IOException {

		// assert (!artifact.exists() || Main.isForceOverwriting())
		// : "File would be overwritten: " + artifact;
		//
		// if (artifact.exists()) {
		// Artifact.remove(artifact);
		// }

		assert (!exists()) : "File would be overwritten: " + this;

		if (file.getParentFile() != null) {
			boolean createdParents = file.getParentFile().mkdirs();

			if (LOG.isTraceEnabled()) {
				LOG.trace("Had to create parent directories: " 
							+ createdParents);
			}
		}

		if (isLeaf) {
			file.createNewFile();
			if (LOG.isTraceEnabled()) {
				LOG.trace("Created file" + file);
			}
		} else {
			file.mkdir();
			if (LOG.isTraceEnabled()) {
				LOG.trace("Created directory " + file);
			}

		}

		assert (exists());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fosd.jdime.common.Artifact#createEmptyDummy()
	 */
	@Override
	public final FileArtifact createEmptyDummy() 
			throws FileNotFoundException {
		// FIXME: The following works only for Unix-like systems. Do something
		// about it!
		File dummyFile = new File("/dev/null");
		assert (dummyFile.exists()) 
				: "Currently only Unix systems are supported!";

		FileArtifact myEmptyDummy = new FileArtifact(dummyFile);
		myEmptyDummy.setEmptyDummy(true);
		LOG.trace("Artifact is a dummy artifact.");
		return myEmptyDummy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(final Object obj) {
		assert (getId() != null);
		assert (obj != null);
		assert (obj instanceof FileArtifact);
		return getId().equals(((FileArtifact) obj).getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fosd.jdime.common.Artifact#exists()
	 */
	@Override
	public final boolean exists() {
		assert (file != null);
		return file.exists();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fosd.jdime.common.Artifact#hasChild(de.fosd.jdime.common.Artifact)
	 */
	@Override
	public final FileArtifact getChild(final FileArtifact otherChild) {
		assert (otherChild != null);
		assert (!isLeaf());
		assert (exists());

		for (FileArtifact myChild : getChildren()) {
			assert (myChild != null);

			if (myChild.equals(otherChild)) {
				return myChild;
			}
		}

		return null;
	}

	/**
	 * Returns the list of artifacts contained in this directory.
	 * 
	 * @return list of artifacts contained in this directory
	 */
	public final ArtifactList<FileArtifact> getDirContent() {
		assert (isDirectory());

		ArtifactList<FileArtifact> contentArtifacts 
				= new ArtifactList<FileArtifact>();
		File[] content = file.listFiles();

		for (int i = 0; i < content.length; i++) {
			try {
				FileArtifact child 
					= new FileArtifact(getRevision(), content[i]);
				child.setParent(this);
				contentArtifacts.add(child);
			} catch (FileNotFoundException e) {
				// this should not happen
				e.printStackTrace();
			}
		}

		return contentArtifacts;
	}

	/**
	 * Returns the absolute path of this artifact.
	 * 
	 * @return absolute part of the artifact
	 */
	public final String getFullPath() {
		return file.getAbsolutePath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fosd.jdime.common.Artifact#getId()
	 */
	@Override
	public final String getId() {
		assert (file != null);
		return file.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fosd.jdime.common.Artifact#getName()
	 */
	@Override
	public final String getName() {
		return file.getPath();
	}

	/**
	 * Returns the path of this artifact.
	 * 
	 * @return path of the artifact
	 */
	public final String getPath() {
		return file.getPath();
	}

	/**
	 * Returns a reader that can be used to retrieve the content of the
	 * artifact.
	 * 
	 * @return Reader
	 * @throws FileNotFoundException
	 *             If the artifact is a file which is not found
	 */
	public final BufferedReader getReader() throws FileNotFoundException {
		if (isFile()) {
			return new BufferedReader(new FileReader(file));
		} else {
			throw new NotYetImplementedException();
		}
	}

	/**
	 * Returns the list of (relative) filenames contained in this directory.
	 * 
	 * @return list of relative filenames
	 */
	public final List<String> getRelativeDirContent() {
		assert (isDirectory());
		return Arrays.asList(file.list());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fosd.jdime.common.Artifact#hashCode()
	 */
	@Override
	public final int hashCode() {
		assert (getId() != null);
		return getId().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fosd.jdime.common.Artifact#initializeChildren()
	 */
	@Override
	public final void initializeChildren() {
		assert (exists());

		if (isDirectory()) {
			setChildren(getDirContent());
		} else {
			setChildren(null);
		}
	}

	/**
	 * Returns true if artifact is a directory.
	 * 
	 * @return true if artifact is a directory
	 */
	public final boolean isDirectory() {
		return file.isDirectory();
	}

	/**
	 * Returns true if the artifact is empty.
	 * 
	 * @return true if the artifact is empty
	 */
	public final boolean isEmpty() {
		assert (exists());
		if (isDirectory()) {
			return file.listFiles().length == 0;
		} else {
			return FileUtils.sizeOf(file) == 0;
		}
	}

	/**
	 * Returns true if artifact is a normal file.
	 * 
	 * @return true if artifact is a normal file
	 */
	public final boolean isFile() {
		return file.isFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fosd.jdime.common.Artifact#isLeaf()
	 */
	@Override
	public final boolean isLeaf() {
		return !file.isDirectory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fosd.jdime.common.Artifact#merge(
	 * de.fosd.jdime.common.operations.MergeOperation,
	 * de.fosd.jdime.common.MergeContext)
	 */
	@Override
	public final void merge(final MergeOperation<FileArtifact> operation,
			final MergeContext context) throws IOException,
			InterruptedException {
		assert (operation != null);
		assert (context != null);

		MergeStrategy<FileArtifact> strategy 
				= (MergeStrategy<FileArtifact>) (isDirectory() 
				? new DirectoryStrategy() : context.getMergeStrategy());
		assert (strategy != null);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Using strategy: " + strategy.toString());
		}
		strategy.merge(operation, context);
		if (!context.isQuiet()) {
			System.out.println(context.getStdIn());
		}

	}

	/**
	 * Removes the artifact's file.
	 * 
	 * @throws IOException
	 *             If an input output exception occurs
	 */
	public final void remove() throws IOException {
		assert (exists() && !isEmptyDummy()) 
				: "Tried to remove non-existing file: " + getFullPath();

		if (isDirectory()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Deleting directory recursively: " + file);
			}
			FileUtils.deleteDirectory(file);
		} else if (isFile()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Deleting file: " + file);
			}
			file.delete();
		} else {
			throw new UnsupportedOperationException(
					"Only files and directories can be removed at the moment");
		}

		assert (!exists());
	}

	/**
	 * Writes from a BufferedReader to the artifact.
	 * 
	 * @param str
	 *            String to write
	 * @throws IOException
	 *             If an input output exception occurs.
	 */
	public final void write(final String str) throws IOException {
		assert (file != null);
		assert (str != null);

		FileWriter writer = new FileWriter(file);
		writer.write(str);
		writer.close();
	}

}
