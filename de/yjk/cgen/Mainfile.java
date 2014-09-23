/**
 * Mainfile.java
 * Makefile data for root directory, as well as common definitions file
 * and include directory
 */
package de.yjk.cgen;

import java.io.File;
import java.io.IOException;

/**
 * Specific data for Makefile of root directory,
 * where a common definitions file needs to be generated,
 * and the include directory could be.
 * Use this class as the entry point to generate all Makefiles for the project.
 */
public class Mainfile extends Makefile
{
	/** the root directory of the project */
	private File dir;
	/** common.mk, the common resource file */
	private File common_file;
	/**
	 * is there a common include file
	 * that needs to be in the C compile flags?
	 */
	private boolean has_include;

	/** name of the common resource file, relative to the root directory */
	public static final String COMMON_NAME = "common.mk";
	/** name of the include directory, relative to the root directory */
	public static final String INCLUDE_NAME = "include";

	/* Values of common variables */
	/* shell commands */
	/** value of archive creator */
	private static final String AR_VAL = "ar";
	/** command to remove file when cleaning */
	private static final String RM_VAL = "rm";
	/** C compiler command */
	private static final String CC_VAL = "gcc";
	/** C++ compiler command */
	private static final String CPP_VAL = "g++";
	/* make command */
	private static final String MAKE_VAL = "make";
	/* command flags */
	/** archive flags */
	private static final String AR_FLAGS_VAL = "cr -o";
	/** file removal flags */
	private static final String RM_FLAGS_VAL = "-f";
	/**
	 * static part of C preprocessor flags,
	 * ie. everything except for include flags
	 */
	private static final String
	STATIC_CPPFLAGS_VAL = "-g -Wall -Wextra -Werror";
	/** the characters in the relative path from directory to parent */
	private static final char[]
	TOWARDS_PARENT = {'.', '.', File.separatorChar};

	/**
	 * @param bd				build_dir to pass to
	 *					the superconstructor
	 * @throws NotDirectoryException	if bd is not a directory
	 * @throws IOException			if bd could not be
	 *					canonicalized
	 */
	public Mainfile(File bd) throws NotDirectoryException,
					IOException
	{
		/* bd is given; This object is its own root */
		super(bd, null);
		setRoot(this);

		/* check existence of include directory */
		File include_file = new File(bd, INCLUDE_NAME);
		has_include = include_file.exists() &&
			      include_file.isDirectory();
	}

	/**
	 * Find the relative path
	 * from a descendant directory to the root directory
	 * @param descendant			must be descendant directory
	 *					contained,
	 *					which can reach the
	 *					root directory by a number of
	 *					"../"
	 * @return				path from descendant to root
	 * @throws NotDescendantException	if purported descendant
	 *					is not actually contained
	 *					in the file subtree rooted
	 *					in this directory
	 * @throws IOException			if finding paths failed
	 */
	protected String
	findRelPath(Makefile descendant) throws NotDescendantException,
					 	IOException
	{
		String root_path = getBuildPath();
		String descendant_path = descendant.getBuildPath();

		if (descendant_path.startsWith(root_path)) {
			/*
			 * find depth of descendant relative to root,
			 * and generate the path to go up the file tree
			 */
			String
			path_to_undo = descendant_path
				       .substring(root_path.length());
			String[]
			descent_segments = path_to_undo.split(File.separator);
			int depth = 0;
			StringBuilder path_builder;

			for (String descent_segment : descent_segments) {
				/* does segment go into a directory */
				if (descent_segment.length() > 0) {
					depth++;
				}
			}
			path_builder = new StringBuilder(TOWARDS_PARENT.length *
							 depth
							 + 1);
			for (int segment_i = 0; segment_i < depth;
			     segment_i++) {
				path_builder.append(TOWARDS_PARENT);
			}

			return path_builder.toString();
		} else {
			throw new NotDescendantException(descendant_path);
		}
	}

	/**
	 * error thrown by findRelPath if given directory is not
	 * contained in path subtree of this root directory
	 */
	public class NotDescendantException extends IOException
	{
		/* format of error message */
		private static final String
		NOT_DESCENDANT_FORMAT = "%s is not a descendant of %s.";

		/*
		 * @param false_descendant_path	the directory that is
		 *				supposed to be a descendant
		 */
		public NotDescendantException(String false_descendant_path)
		{
			super(String.format(false_descendant_path));
		}
	}

	/**
	 * Returns if there is a common include directory
	 * @return has_include
	 */
	public boolean hasInclude()
	{
		return has_include;
	}

	/**
	 * Generate the Makefiles for the whole project,
	 * as well as the common resources file
	 * @throws IOException	if there was an error in writing the Makefiles
	 *			or the resources
	 */
	public void generateProject() throws IOException
	{
		/* generate common directory file */
		File common_file = new File(getBuildDir(), COMMON_NAME);
		MakeFormatter output = new MakeFormatter(common_file);

		/* set shell command variables */
		output.assignVar(Makefile.CC_VAR, CC_VAL);
		output.assignVar(Makefile.CPP_VAR, CPP_VAL);
		output.assignVar(Makefile.AR_VAR, AR_VAL);
		output.assignVar(Makefile.STATIC_CPPFLAGS_VAR,
				 STATIC_CPPFLAGS_VAL);
		output.assignVar(Makefile.AR_FLAGS_VAR, AR_FLAGS_VAL);
		output.assignVar(Makefile.RM_FLAGS_VAR, RM_FLAGS_VAL);

		output.close();

		/* Generate rest of project */
		generate();
	}
}
