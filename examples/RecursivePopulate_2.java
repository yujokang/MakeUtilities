/**
 * RecursivePopulate_2.java
 * Example for creating new project in a chosen file,
 * as indicated in the first command line argument
 */
import java.io.File;
import java.io.IOException;

import de.yjk.cgen.Mainfile;

/**
 * Just contains the main method, and the constants used in it
 */
public class RecursivePopulate_2
{
	public static void main(String[] args)
	{
		String root_name;
		File root_file;

		if (args.length == 0) {
			System.out.println("Please give the directory name.");
			return;
		}
		root_name = args[0];
		root_file = new File(root_name);

		/* Try to create directory if it doesn't already exist */
		if (!root_file.exists() && !root_file.mkdir()) {
			System.out.printf("Could not create %s.\n",
					  root_name);
			return;
		}

		try {
			Mainfile root = new Mainfile(root_file);
			/* recursively populate with all the subdirectories. */
			root.populateFull();
			/* generate Makefiles and common resource file */
			root.generateProject();
		} catch (IOException ioe) {
			System.out.println("Failed to create Makefiles: " +
					   ioe);
			ioe.printStackTrace();
		}
	}
}
