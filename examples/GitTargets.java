/**
 * GitTargets.java
 * Aggregates targets that are taken from Git repositories:
 * the folders themselves, and any objects inside them
 * that become available by the time "make" is run inside the folder.
 */
import de.yjk.cgen.Makefile;
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
	INCLUDE_TARGET = ".." + File.separator +
			 MakeFormatter.genUseVar(Makefile.INCLUDE_VAR);

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
	public void addOutput(LibTarget new_output)
	{
		new_output.setIsFirst(outputs.size() == 0);
		outputs.add(new_output);
	}

	/**
	 * Add a target for copying an header file
	 * from the "include" folder of the Git project
	 * to the "include" folder under the root folder.
	 * @param include_file	the name of the header file to copy
	 */
	public void addIncludeOutput(String include_file)
	{
		addOutput(new LibTarget(include_file,
					MakeFormatter.genUseVar(Makefile
								.INCLUDE_VAR),
					INCLUDE_TARGET));
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
			lib_dir.addTarget(src_dir);
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

	/**
	 * The Target subclass for copying external files
	 * into the desired location.
	 */
	public class LibTarget extends Target
	{
		/** the copy command for copying files out of a folder */
		private static final String COPY_CMD = "cp";
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
		 * Set the target path.
		 * @param target_name	the file name of the target and source
		 * @param dir_in_target	the directory to which to copy the file
		 */
		private void setTarget(String target_name,
				       String dir_in_target)
		{
			target = dir_in_target + File.separator + target_name;
		}

		/**
		 * generic constructor,
		 * assuming needed parts of all fields are given
		 * @param target_name		the file name of the target
		 *				and source
		 * @param dir_in_project	the directory in the Git folder
		 *				that contains the source file
		 * @param dir_in_target		the directory to which to
		 *				copy the file
		 */
		private void init(String target_name, String dir_in_project,
				  String dir_in_target)
		{
			setSource(target_name, dir_in_project);
			setTarget(target_name, dir_in_target);

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
			super(target_name, project);
			init(target_name, dir_in_project, dir_in_target);
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
			super(target_name, project);
			init(target_name, dir_in_project, LOCAL_DIR);
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
					  .genList(COPY_CMD, source, target);

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
			}

			output.write(extract_command);
		}
	}
}

