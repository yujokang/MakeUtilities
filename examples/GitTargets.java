/**
 * GitTargets.java
 * Aggregates targets that are taken from Git repositories:
 * the folders themselves, and any objects inside them
 * that become available by the time "make" is run inside the folder.
 */
import de.yjk.cgen.Makefile;
import de.yjk.cgen.Mainfile;
import de.yjk.cgen.MakeFormatter;
import de.yjk.cgen.Target;

import java.util.List;
import java.util.LinkedList;

import java.io.IOException;
import java.io.File;

/**
 * aggregate of Target subclass instances that are taken from Git repositories.
 * Includes one target for the whole folder,
 * and a variable number of targets for files inside the folder,
 * some of which may only appear after running "make".
 */
public class GitTargets
{
	/** the git clone command for fetching the entire folder */
	private static final String GIT_CLONE_CMD = "git clone";
	/** the variable for the git clone command */
	private static final String GIT_CLONE_VAR = "GIT_CMD";

	/**
	 * the standard destination for include files:
	 * the "include" folder in the root directory,
	 * which is assumed to directly contain the folder
	 * containing these targets
	 */
	private static final String
	INCLUDE_TARGET = ".." + File.separator + Mainfile.INCLUDE_NAME;

	/** the project folder */
	private String project;
	/** the URL from which to clone the repository */
	private String git_url;
	/** the target for the folder itself */
	private GitDirTarget src_dir;
	/**
	 * targets for contents of the folder,
	 * which are the outputs we actually want
	 */
	private List<LibTarget> outputs;

	/**
	 * @param p	the project folder, ie the "project" field
	 * @param gu	the Git URL, ie the "git_url" field
	 */
	public GitTargets(String p, String gu)
	{
		project = p;
		git_url = gu;
		src_dir = new GitDirTarget();
		outputs = new LinkedList<LibTarget>();
	}

	/**
	 * Add an extra output file to copy from the repository folder.
	 * @param new_output	the target to add
	 */
	private void addOutput(LibTarget new_output)
	{
		new_output.setIsFirst(outputs.size() == 0);
		outputs.add(new_output);
	}

	/**
	 * Add an extra output file to copy from the repository folder
	 * to the given target folder
	 * @param target_name		the file name of the target
	 *				and source
	 * @param dir_in_project	the directory in the Git folder
	 *				that contains the source file
	 * @param dir_in_target		the directory to which to
	 *				copy the file
	 */
	public void addOutput(String target_name, String dir_in_project,
			      String dir_in_target)
	{
		addOutput(new LibTarget(target_name, dir_in_project,
					dir_in_target));
	}

	/**
	 * Add an extra output file to copy from the repository folder
	 * to the folder containing this rule
	 * @param target_name		the file name of the target
	 *				and source
	 * @param dir_in_project	the directory in the Git folder
	 *				that contains the source file
	 */
	public void addOutput(String target_name, String dir_in_project)
	{
		addOutput(new LibTarget(target_name, dir_in_project));
	}

	/**
	 * Add a target for copying a header file
	 * from the "include" folder of the Git project
	 * to the "include" folder under the root folder.
	 * @param include_file	the name of the header file to copy
	 */
	public void addIncludeOutput(String include_file)
	{
		addOutput(include_file, Mainfile.INCLUDE_NAME,
			  INCLUDE_TARGET);
	}

	/**
	 * Add all the targets to a Makefile.
	 * @param lib_dir	the Makefile to which to add the targets.
	 */
	public void addToMakefile(Makefile lib_dir)
	{
		lib_dir.addAssignment(GIT_CLONE_VAR, GIT_CLONE_CMD);
		lib_dir.addTarget(src_dir);
		for (LibTarget output : outputs) {
			lib_dir.addTarget(output);
		}
	}

	/**
	 * The Target subclass for cloning the folder
	 * stored in the Git repository.
	 */
	private class GitDirTarget extends Target
	{
		public GitDirTarget()
		{
			super(project);
		}

		protected void
		genCommand(MakeFormatter output) throws IOException
		{
			String
			command = MakeFormatter
				  .genList(MakeFormatter
					   .genUseVar(GIT_CLONE_VAR),
					   git_url);
			output.write(command);
		}
	}


	/*
	 * Calculate the target to put into the Target super constructor
	 * of the LibTarget class below
	 * @param target_name		the file name of the target
	 *				and source
	 * @param dir_in_target		the directory to which to
	 *				copy the file
	 * returns			path to target
	 */
	private static String calcTarget(String target_name,
					 String dir_in_target)
	{
		return dir_in_target + File.separator + target_name;
	}

	/**
	 * The Target subclass for copying external files
	 * into the desired location.
	 */
	private class LibTarget extends Target
	{
		/** the copy command for copying files out of a folder */
		private static final String COPY_CMD = "cp";
		/**
		 * the local directory, which is the default target directory
		 */
		private static final String LOCAL_DIR = ".";

		/** the path to the source file inside the Git repository */
		private String source;
		/** the destination path to which to copy the source */
		private String target;
		/**
		 * Is this the first target
		 * after the repository has been cloned?
		 * If so, we will need to build the project in the repository.
		 * false by default.
		 */
		private boolean is_first;

		/**
		 * Set the source path.
		 * @param target_name		the file name of the target
		 *				and source
		 * @param dir_in_project	the directory in the Git folder
		 *				that contains the source file
		 */
		private void setSource(String target_name,
				       String dir_in_project)
		{
			source = project + File.separator + dir_in_project +
				 File.separator + target_name;
		}

		/**
		 * generic constructor,
		 * assuming needed parts of all fields are given
		 * @param target_name		the file name of the target
		 *				and source
		 * @param dir_in_project	the directory in the Git folder
		 *				that contains the source file
		 */
		private void init(String target_name, String dir_in_project)
		{
			setSource(target_name, dir_in_project);

			is_first = false;
		}

		/**
		 * Complete constructor
		 * @param target_name		the file name of the target
		 *				and source
		 * @param dir_in_project	the directory in the Git folder
		 *				that contains the source file
		 * @param dir_in_target		the directory to which to
		 *				copy the file
		 */
		public LibTarget(String target_name, String dir_in_project,
				 String dir_in_target)
		{
			super(calcTarget(target_name, dir_in_target), project);
			init(target_name, dir_in_project);
		}

		/**
		 * partial constructor,
		 * in which the target is copied into the same directory
		 * as the Makefile
		 * @param target_name		the file name of the target
		 *				and source
		 * @param dir_in_project	the directory in the Git folder
		 *				that contains the source file
		 */
		public LibTarget(String target_name, String dir_in_project)
		{
			super(calcTarget(target_name, LOCAL_DIR), project);
			init(target_name, dir_in_project);
		}

		/**
		 * Set the "is_first" field to mark
		 * if this rule is the first one after
		 * the folder has been cloned.
		 * isf:	the new value for "is_first"
		 */
		public void setIsFirst(boolean isf)
		{
			is_first = isf;
		}

		protected void
		genCommand(MakeFormatter output) throws IOException
		{
			String
			extract_command = MakeFormatter
					  .genList(COPY_CMD, source, getName());

			/*
			 * The first rule after the clone rule
			 * will build the project.
			 */
			if (is_first) {
				String
				make_command = MakeFormatter
					       .genList(MakeFormatter
							.genUseVar(Makefile
								   .MAKE_VAR),
							Makefile
							.MAKE_SWITCH_FLAG,
							project);

				output.write(make_command);
				output.newLine();
			}

			output.write(extract_command);
		}
	}
}
