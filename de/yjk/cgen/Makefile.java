/**
 * Makefile.java
 * Populates Makefile with rules, and generates the Makefiles
 * for a directory and its subdirectories.
 * Using the populate() function automatically generates rules
 * to make object files from all the source files
 * directly contained in the directory and an archive from the object files,
 * and recursively does the same for any selected, or all subdirectories
 */
package de.yjk.cgen;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collection;

import java.io.File;
import java.io.IOException;

import de.yjk.utils.FileFormatter;

/**
 * Contains Makefile components.
 * Used to generate Makefile
 */
public class Makefile
{
	/** the directory that will contain the Makefile */
	private File build_dir;
	/** the top-level directory and its Makefile data */
	private Mainfile root;
	/** names of objects to generate directly in this subdirectory */
	private List<String> object_names;
	/** special target outputs */
	private List<Target> targets;
	/** subdirectories, which will at least require own Makefiles */
	private List<Makefile> subdirs;

	/* common variable names */
	public static final String CC_VAR = "CC"; /** C compiler variable */
	public static final String CPP_VAR = "CXX"; /** C++ compiler variable */
	/** make command variable */
	private static final String MAKE_VAR = "MAKE";
	/* shell commands */
	/** variable for archive creator */
	public static final String AR_VAR = "AR";
	/** variable for command to remove files when cleaning */
	private static final String RM_VAR = "RM";
	/* lists of rules */
	/** variable for list of subdirectories */
	private static final String SUBDIRS_VAR = "SUBDIRS";
	/** variable for objects list */
	private static final String OBJECTS_VAR = "OBJS";
	/**
	 * variable for list of targets that
	 * will be used outside of the directory
	 */
	private static final String TARGETS_VAR = "TARGETS";
	/* directories */
	/** variable for the include directory option */
	protected static final String INCLUDE_VAR = "INCLUDE";
	/* command flags */
	/** variable for the archive command flags */
	public static final String AR_FLAGS_VAR = "AR_FLAGS";
	/** variable for the file removal flags */
	public static final String RM_FLAGS_VAR = "RM_FLAGS";
	/**
	 * variable for the static part of C preprocessor flags,
	 * ie. everything except for include flags
	 */
	public static final String STATIC_CPPFLAGS_VAR = "_CPPFLAGS";
	/** variable fo all the C preprocessor flags */
	public static final String CPPFLAGS_VAR = "CPPFLAGS";

	/** value of the C preprocessor flags */
	private static final String
	CPPFLAGS_VAL = MakeFormatter
		       .genList(MakeFormatter.genUseVar(STATIC_CPPFLAGS_VAR),
				MakeFormatter.genUseVar(INCLUDE_VAR));

	/** mark between the rule name and dependencies in the rule header */
	private static final String RULE_NAME_END = ":";

	/** name of Makefile, relative to this directory */
	private static final String MAKEFILE_NAME = "Makefile";
	/** make flag to switch to subdirectory */
	public static final String MAKE_SWITCH_FLAG = "-C";
	/** Makefile's default rule */
	public static final String MAKE_ALL_RULE = "all";
	/** Makefile's clean rule */
	public static final String MAKE_CLEAN_RULE = "clean";
	/**
	 * declares list of phony rules,
	 * ie. rules for subdirectories that are not generated
	 */
	public static final String PHONY_DECL = ".PHONY:";
	/** include command in Makefile */
	public static final String INCLUDE_CMD = "include";

	/* extensions */
	/** extension marker */
	public static final String EXT_MARKER = ".";
	/* output files */
	/** archive extension */
	public static final String ARCHIVE_EXT = EXT_MARKER + "a";
	/** object file extension */
	public static final String OBJ_EXT = EXT_MARKER + "o";
	/* input files */
	/** C source extension */
	public static final String C_EXT = EXT_MARKER + "c";
	/* C++ source extensions */
	/** regular C++ extension */
	public static final String CPP_0_EXT = EXT_MARKER + "cpp";
	/** first alternative C++ extension */
	public static final String CPP_1_EXT = EXT_MARKER + "cxx";
	/** second alternative C++ extension */
	public static final String CPP_2_EXT = EXT_MARKER + "c++";
	/** array of source code extensions */
	public static final String[] CODE_EXTS = {C_EXT, CPP_0_EXT, CPP_1_EXT,
						  CPP_2_EXT};
	/** the C preprocessor flag before a directory to include */
	public static final String INCLUDE_FLAG = "-I";

	/**
	 * built-in variables for output (after "-o" flag)
	 * and list of dependencies
	 */
	public static String
	USE_OUT_IN_VARS = MakeFormatter.genList("$@", "$^");

	/**
	 * Convert arbitrary collection of strings into an array
	 * @param collection	collection to convert to array
	 * @return		array containing exactly
	 *			the elements of collection
	 */
	public static String[] toArray(Collection<String> collection)
	{
		String[] ret_array = new String[collection.size()];
		int element_i = 0;

		for (String element : collection) {
			ret_array[element_i++] = element;
		}

		return ret_array;
	}

	/**
	 * Generate Makefile rule header for
	 * an array of dependencies
	 * @param output	the output stream to write to
	 * @param name		name of rule
	 * @param dependencies	arrayr of names of dependencies
	 * @throws		IOException if writing failed
	 */
	public static void
	writeRuleHeader(MakeFormatter output, String name,
			String ... dependencies) throws IOException
	{
		output.write(name + Makefile.RULE_NAME_END);
		if (dependencies.length > 0) {
			output.write(MakeFormatter.LIST_DELIM +
				     MakeFormatter.genList(dependencies));
		}
		output.newLine();
	}

	/**
	 * Generate Makefile rule header for
	 * arbitrary Collection of dependencies.
	 * @param output	the output stream to write to
	 * @param name		name of rule
	 * @param dependencies	names of dependencies
	 * @throws		IOException if writing failed
	 */
	public static void
	writeRuleHeader(MakeFormatter output, String name,
			Collection<String> dependencies) throws IOException
	{
		writeRuleHeader(output, name, toArray(dependencies));
	}

	/**
	 * sets root Makefile
	 * @param r	root
	 */
	protected void setRoot(Mainfile r)
	{
		root = r;
	}

	/**
	 * Return the directory that will contain this Makefile.
	 * Used by genProject to generate common resources file
	 * @return	build_dir
	 */
	protected File getBuildDir()
	{
		return build_dir;
	}

	/**
	 * Return the path to the directory that will contain this Makefile.
	 * Used by findRelPath(Makefile subdir)
	 * to find relative path to root directory
	 * @return		canonical path of build_dir
	 * @throws IOException	if constructing canonical path failed
	 */
	protected String getBuildPath() throws IOException
	{
		return build_dir.getCanonicalPath();
	}

	/**
	 * Helper function to get name of directory,
	 * which is used in generate
	 */
	private String getName()
	{
		return build_dir.getName();
	}

	/**
	 * @param bd				arbitrary path that will be
	 *					canonicalized to build_dir
	 * @param r				root
	 * @throws NotDirectoryException	if bd is not a directory
	 * @throws IOException			if bd could not be
	 *					canonicalized
	 */
	public Makefile(File bd, Mainfile r) throws NotDirectoryException,
						    IOException
	{
		if (!bd.isDirectory()) {
			throw new NotDirectoryException(bd);
		}
		build_dir = bd.getCanonicalFile();
		setRoot(r);

		object_names = new LinkedList<String>();
		targets = new LinkedList<Target>();
		subdirs = new LinkedList<Makefile>();
	}

	/**
	 * Generate name of archive according to the directory name
	 */
	private String toArchiveName()
	{
		return getName() + ARCHIVE_EXT;
	}

	/**
	 * Add a object file
	 * @param object	name of object file,
	 *			relative to this directory, to add
	 */
	public void addObject(String object)
	{
		object_names.add(object);
	}

	/**
	 * Add a target
	 * @param target	target to add
	 */
	public void addTarget(Target target)
	{
		targets.add(target);
	}

	/**
	 * Add a subdirectory's Makefile data
	 * @param subdir	subdirectory's Makefile data
	 */
	public void addSubdir(Makefile subdir)
	{
		subdirs.add(subdir);
	}

	/**
	 * Helper function of populate function to check if
	 * a file is a C or C++ source file, according to its name
	 * @param name	name of possible source file
	 * @return	return true iff
	 *		name ends in one of the source code extensions
	 */
	private static boolean isSource(String name)
	{
		for (String code_ext : CODE_EXTS) {
			if (name.endsWith(code_ext)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * After subdirs has been populated,
	 * populate the Makefile with the existing source files,
	 * an archive that will be generated from the source files,
	 * and, recursively, the subdirectories in subdirs,
	 * with all of their subdirectories.
	 * @return		list of paths to generated library targets
	 * @throws IOException	if there was an error in populating
	 *			the subdirectories,
	 *			or some of the subdirectories in subdirs
	 *			do not exist or are not directories
	 */
	private List<String> populateAfterSubdirsAdded() throws IOException
	{
		List<String> archives = new ArrayList<String>();
		File[] subfiles = build_dir.listFiles();
		ArchiveTarget archive;
		String archive_name;

		for (Makefile subdir : subdirs) {
			List<String> subarchives = subdir.populateFull();
			for (String subarchive : subarchives) {
				archives.add(getName() + File.separator +
					     subarchive);
			}
		}

		/* populate with object files based on source files */
		for (File subfile : subfiles) {
			String name = subfile.getName();
			if (isSource(name)) {
				String object_name =
				name.substring(0,
					       name.lastIndexOf(EXT_MARKER)) +
				OBJ_EXT;
				addObject(object_name);
			}
		}
		/* populate target with archive */
		archive_name = toArchiveName();
		archive = new ArchiveTarget(archive_name, object_names);
		addTarget(archive);

		archives.add(archive_name);
		return archives;
	}

	/**
	 * Populate with the given source files,
	 * an archive that will be generated from the source files,
	 * and, recursively, the given subdirectories,
	 * with all of their subdirectories.
	 * Returns paths to archives to be generated.
	 * @param subdir_names	array of subdirectories to use.
	 * @return		list of paths to generated library targets
	 * @throws IOException	if there was an error in populating
	 *			the subdirectories,
	 *			or some of the subdirectories in subdirs
	 *			do not exist or are not directories
	 */
	public List<String> populate(String ... subdir_names) throws IOException
	{
		/*
		 * check that subdirectories are directories,
		 * and put them into subdirs_list
		 */
		for (String subdir_name : subdir_names) {
			File subdir = new File(build_dir, subdir_name);
			if (subdir.isDirectory()) {
				addSubdir(new Makefile(subdir, root));
			} else {
				throw new NotDirectoryException(subdir);
			}
		}

		return populateAfterSubdirsAdded();
	}

	/**
	 * Populate with the given source files,
	 * an archive that will be generated from the source files,
	 * and, recursively, all the subdirectories in this directory,
	 * with all of their subdirectories.
	 * @return		list of paths to generated library targets
	 * @throws IOException	if there was an error in populating
	 *			the subdirectories,
	 *			or some of the subdirectories in subdirs
	 *			do not exist or are not directories
	 */
	public List<String> populateFull() throws IOException
	{
		File[] subfiles = build_dir.listFiles();
		for (File subfile : subfiles) {
			if (subfile.isDirectory()) {
				addSubdir(new Makefile(subfile, root));
			}
		}

		return populateAfterSubdirsAdded();
	}

	/**
	 * error thrown by populate if purported directory is not a directory
	 */
	public static class NotDirectoryException extends IOException
	{
		/* static part of error message */
		private static final String
		NOT_DIR_MESSAGE = " is not a subdirectory.";

		/*
		 * @param false_dir	the "directory" that
		 *			is not a directory,
		 *			which forms the object-specific part
		 *			of the message
		 */
		public NotDirectoryException(File false_dir)
		{
			super(false_dir.getPath() + NOT_DIR_MESSAGE);
		}
	}

	/**
	 * Generate the Makefile, and those of the subdirectories
	 * @throws IOException	if there was an error in writing the Makefiles
	 */
	protected void generate() throws IOException
	{
		MakeFormatter
		output = new MakeFormatter(new File(build_dir, MAKEFILE_NAME));
		String to_root = root.findRelPath(this);

		/* Collect subdirectory names into Makefile-format list */
		String[] subdir_names = new String[subdirs.size()];
		String subdirs_val;
		int subdir_i = 0;

		for (Makefile subdir : subdirs) {
			subdir_names[subdir_i++] = subdir.getName();
		}
		subdirs_val = MakeFormatter.genList(subdir_names);

		/* Collect object names into Makefile-format list */
		String
		objects_val = MakeFormatter.genList(toArray(object_names));

		/* Collect target names into Makefile-format list */
		String[] target_names = new String[targets.size()];
		String targets_val;
		int target_i = 0;

		for (Target target : targets) {
			target_names[target_i++] = target.getName();
		}
		targets_val = MakeFormatter.genList(target_names);

		/* Declare phony rules */
		output.write(PHONY_DECL + subdirs_val);
		output.newLine();

		/* Import common resources file */
		String common_path = to_root + Mainfile.COMMON_NAME;
		output.write(MakeFormatter.genList(INCLUDE_CMD, common_path));
		output.newLine();

		/* Create C preprocessor headers */
		/* Declare INCLUDE, if it exists */
		if (root.hasInclude()) {
			String include_path = to_root + Mainfile.INCLUDE_NAME;
			output.assignVar(INCLUDE_VAR, INCLUDE_FLAG +
						      include_path);
		}
		output.assignVar(CPPFLAGS_VAR, CPPFLAGS_VAL);

		/* Define SUBDIRS, OBJS and TARGETS */
		output.assignVar(SUBDIRS_VAR, subdirs_val);
		output.assignVar(OBJECTS_VAR, objects_val);
		output.assignVar(TARGETS_VAR, targets_val);

		/* Define default rule */
		writeRuleHeader(output, MAKE_ALL_RULE,
				MakeFormatter.genUseVar(SUBDIRS_VAR),
				MakeFormatter.genUseVar(OBJECTS_VAR),
				MakeFormatter.genUseVar(TARGETS_VAR));

		/* Define special target rules */
		for (Target target : targets) {
			target.genRule(output);
		}

		/* Define subdirectory rules */
		for (Makefile subdir : subdirs) {
			writeRuleHeader(output, subdir.getName());
			output.indent();
			output
			.write(MakeFormatter
			       .genList(MakeFormatter.genUseVar(MAKE_VAR),
					MAKE_SWITCH_FLAG, subdir.getName()));
			output.unindent();
			output.newLine();
		}

		/* Define cleanup rule */
		writeRuleHeader(output, MAKE_CLEAN_RULE);
		output.indent();
		/* clean up own objects and targets */
		output.write(MakeFormatter
			     .genList(MakeFormatter.genUseVar(RM_VAR),
				      MakeFormatter.genUseVar(RM_FLAGS_VAR),
				      MakeFormatter.genUseVar(OBJECTS_VAR),
				      MakeFormatter.genUseVar(TARGETS_VAR)));
		output.newLine();
		/* clean up subdirectories */
		for (Makefile subdir : subdirs) {
			output
			.write(MakeFormatter
			       .genList(MakeFormatter.genUseVar(MAKE_VAR),
					MAKE_SWITCH_FLAG, subdir.getName(),
					MAKE_CLEAN_RULE));
			output.newLine();
		}
		output.unindent();
		output.close();

		/* Write Makefiles in subdirectories */
		for (Makefile subdir : subdirs) {
			subdir.generate();
		}
	}
}

/**
 * Generates files formatted in the Makefile/shell syntax
 */
class MakeFormatter extends FileFormatter
{
	/** template for using value of variable */
	private static final String VAR_USE_FMT = "$(%s)";
	/** template for assigning value of variable */
	private static final String VAR_DEF_FMT = "%s=%s";
	/** template for concatenating value to variable */
	private static final String VAR_CAT_FMT = "%s+=%s";

	/** delimiter between list elements, such as multiple options */
	public static final char LIST_DELIM = ' ';

	/**
	 * Constructor for FileFormatter(File)
	 * @param in_file	File object to pass to the superconstructor
	 * @throws IOException	if that is thrown by superconstructor
	 */
	public MakeFormatter(File in_file) throws IOException
	{
		super(in_file);
	}

	/**
	 * Write line to assign value to named variable
	 * @param name		name of variable
	 * @param value		new value of variable
	 * @throws IOException	if there was an error during writing
	 */
	public void assignVar(String name, String value) throws IOException
	{
		write(String.format(VAR_DEF_FMT, name, value));
		newLine();
	}

	/**
	 * Write line to append value to named variable
	 * @param name		name of variable
	 * @param append_value	value to append variable
	 * @throws IOException	if there was an error during writing
	 */
	public void appendVar(String name,
			      String append_value) throws IOException
	{
		write(String.format(VAR_CAT_FMT, name, append_value));
		newLine();
	}

	/**
	 * Write variable dereferencing expression
	 * @param name		name of variable
	 * @return		expression for dereferencing variable
	 */
	public static String genUseVar(String name)
	{
		return String.format(VAR_USE_FMT, name);
	}

	/**
	 * Join String elements by list delimiter
	 * @param elements	array of elements to join.
	 *			Can be null or empty, for an empty return value
	 * @return		delimited list of element
	 */
	public static String genList(String ... elements)
	{
		if (elements != null && elements.length > 0) {
			/* count and allocate number of characters needed */
			/*
			 * will have to subtract 1 because output won't need
			 * delimiter before first element,
			 * and add 1 for 0 character
			 */
			int char_count = elements.length - 1 + 1;
			StringBuilder list_builder;

			for (String element : elements) {
				char_count += element.length();
			}
			list_builder = new StringBuilder(char_count);

			/* copy over needed characters */
			/*
			 * first element is special,
			 * without delimiter in front
			 */
			list_builder.append(elements[0].toCharArray());
			/*
			 * rest of elements have delimiter in front,
			 * to separate from the previous element
			 */
			for (int element_i = 1; element_i < elements.length;
			     element_i++) {
				list_builder.append(LIST_DELIM);
				list_builder.append(elements[element_i]
						    .toCharArray());
			}
			return list_builder.toString();
		} else {
			/* return empty String for null array or no elements */
			return "";
		}
	}
}
