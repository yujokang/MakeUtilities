/**
 * RecursivePopulate.java
 * Example for creating project from current directory
 */
import java.io.File;
import java.io.IOException;

import de.yjk.cgen.Mainfile;

/**
 * Just contains the main method, and the constants used in it
 */
public class RecursivePopulate
{
	/** The current directory, at which the project is rooted */
	private static final String TOP_DIR = ".";

	public static void main(String[] args)
	{
		try {
			Mainfile arduino = new Mainfile(new File(TOP_DIR));
			/* recursively populate with all the subdirectories. */
			arduino.populateFull();
			/* generate Makefiles and common resource file */
			arduino.generateProject();
		} catch (IOException ioe) {
			System.out.println("Failed to create Makefiles: " +
					   ioe);
			ioe.printStackTrace();
		}
	}
}
