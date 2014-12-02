/**
 * CGen.java
 * Generates Makefiles for generic C project directory tree
 */
import de.yjk.cgen.Makefile;
import de.yjk.cgen.Mainfile;
import de.yjk.cgen.Target;
import de.yjk.cgen.MakeFormatter;

import java.io.File;
import java.io.IOException;

/**
 * generates set of Makefiles for a C project
 * containing C source files in "src",
 * external projects in "libs",
 * test source files in "tests",
 * and header files in "include".
 * In the end, the top Makefile creates an archive of the source objects
 * with the desired name in the root directory.
 */
public class CGen extends Mainfile
{
	/** the name of the folder containing the C source files */
	public static final String SRC_DIR = "src";
	/** the name of the folder which will contain external projects */
	public static final String LIBS_DIR = "libs";
	/** the name of the folder containing the test source files */
	public static final String TESTS_DIR = "tests";

	/** the folder containing the C source files */
	private Makefile src;
	/** the folder which will contain external projects */
	private Makefile libs;
	/** the folder containing the test source files */
	private Makefile tests;
	/** the name of the archive to be created */
	private String archive_file_name;

	/**
	 * @param bd		the bd parameter to pass to
	 *			the Mainfile superconstructor
	 * @param archive	the desired name of the archive
	 */
	public CGen(File bd, String archive) throws Makefile
						    .NotDirectoryException,
						    IOException
	{
		super(bd);

		src = new Makefile(new File(bd, SRC_DIR), this);
		libs = new Makefile(new File(bd, LIBS_DIR), this);
		tests = new Makefile(new File(bd, TESTS_DIR), this);

		archive_file_name = archive + Makefile.ARCHIVE_EXT;
		src.setCustomArchiveName(archive_file_name);
		/*
		 * Don't need an archive for the test folder,
		 * since its objects won't be used anywhere else.
		 */
		tests.setMakeObjectArchive(false);
		/*
		 * Don't automatically clean the libs folder,
		 * so we won't have to reclone the repositories.
		 */
		libs.setAutoClean(false);

		addSubdir(libs);
		addSubdir(src);
		addSubdir(tests);

		src.populateFull();
		tests.populateFull();

		addTarget(new ArchiveCopyTarget());
	}

	/**
	 * Add git repository targets.
	 * @param git_target	the Git repository to add
	 */
	public void addGitTargets(GitTargets git_target)
	{
		git_target.addToMakefile(libs);
	}

	/**
	 * rule to copy the archive from the "src" folder
	 * to the root directory.
	 */
	private class ArchiveCopyTarget extends Target
	{
		/** the copy command */
		private static final String COPY_CMD = "cp";

		public ArchiveCopyTarget()
		{
			super(archive_file_name);
		}

		protected void
		genCommand(MakeFormatter output) throws IOException
		{
			String
			source_file_name = SRC_DIR + File.separator +
					   archive_file_name;
			String
			command = MakeFormatter.genList(COPY_CMD,
							source_file_name,
							archive_file_name);
			output.write(command);
		}
	}
}
