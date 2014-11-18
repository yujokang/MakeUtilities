/**
 * CGenMain.java
 * Executable code for creating Makefiles for a generic C project,
 * using the "CGen" class
 */
import de.yjk.cgen.Mainfile;

import java.io.File;
import java.io.IOException;

/**
 * the driver class that creates Makefiles for a C project
 * that contains "src", "include", "libs" and "tests" folders,
 * and supports passing in pairs of Git repository names and their URLS
 * to be cloned into the "libs" folder.
 * If the root folder does not exist, all the necessary folders
 * will be created.
 */
public class CGenMain
{
	/** the beginning of the optional Git arguments */
	private static final int GIT_ARGS_START = 2;

	/**
	 * Add external projects according to command line arguments.
	 * @param out	the project to which to add the Git rules
	 * @param args	the command line arguments,
	 *		which contain the Git arguments
	 */
	private static void addProjects(CGen out, String[] args)
	{
		int arg_i;

		for (arg_i = GIT_ARGS_START; arg_i < args.length; arg_i += 2) {
			String folder = args[arg_i];
			String url = args[arg_i + 1];

			out.addGitTargets(new GitTargets(folder, url));
		}
	}

	public static void main(String[] args)
	{
		String root_name;
		String archive_name;
		File root_file;

		if (args.length < GIT_ARGS_START) {
			System.out.println("Please give the " +
					   "directory and output archive " +
					   "names.");
			return;
		}
		if ((args.length - GIT_ARGS_START) % 2 == 1) {
			System.out.println("Please enter git folders and " +
					   "their URLs in pairs.\n");
			return;
		}
		root_name = args[0];
		archive_name = args[1];
		root_file = new File(root_name);

		/* Try to create directory if it doesn't already exist */
		if (!root_file.exists()) {
			if (root_file.mkdir()) {
				File src_file = new File(root_file,
							 CGen.SRC_DIR);
				File libs_file = new File(root_file,
							 CGen.LIBS_DIR);
				File tests_file = new File(root_file,
							   CGen.TESTS_DIR);
				File include_file = new File(root_file,
							     Mainfile
							     .INCLUDE_NAME);
				boolean missing_src = !src_file.mkdir();
				boolean missing_libs = !libs_file.mkdir();
				boolean missing_tests = !tests_file.mkdir();
				boolean missing_include = !include_file.mkdir();

				if (missing_src || missing_libs ||
				    missing_tests || missing_include) {
					if (missing_src) {
						System.out
						.println("Could not create " +
							 "\"src\" folder.");
					} else {
						src_file.delete();
					}
					if (missing_libs) {
						System.out
						.println("Could not create " +
							 "\"libs\" folder.");
					} else {
						libs_file.delete();
					}
					if (missing_tests) {
						System.out
						.println("Could not create " +
							 "\"tests\" folder.");
					} else {
						tests_file.delete();
					}
					if (missing_include) {
						System.out
						.println("Could not create " +
							 "\"include\" folder.");
					} else {
						include_file.delete();
					}
					root_file.delete();
					return;
				}
			} else {
				System.out.printf("Could not create %s.\n",
						  root_name);
				return;
			}
		}

		try {
			CGen root = new CGen(root_file, archive_name);
			/* add Git projects */
			addProjects(root, args);
			/* generate Makefiles and common resource file */
			root.generateProject();
		} catch (IOException ioe) {
			System.out.println("Failed to create Makefiles: " +
					   ioe);
			ioe.printStackTrace();
		}
	}
}
