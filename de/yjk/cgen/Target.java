/**
 * Target.java
 * Generates special rule for a special target,
 * which will be listed under TARGETS
 */
package de.yjk.cgen;

import java.io.IOException;
import java.util.Collection;

/**
 * Abstract class to generate custom rule,
 * whose name will be listed under TARGETS
 */
public abstract class Target
{
	/** the target to be generated */
	private String name;
	/** files on which the rule depends */
	private String[] dependencies;

	/**
	 * Generic constructor for all numbers of dependencies.
	 * @param n	name
	 * @param n_ds	number of dependencies
	 */
	private void init(String n, int n_ds)
	{
		name = n;
		dependencies = new String[n_ds];
	}

	/**
	 * Constructor for multiple dependencies,
	 * if a collection can be created ahead of time
	 * @param n	name
	 * @param ds	contains elements to put into dependencies
	 */
	public Target(String n, Collection<String> ds)
	{
		int dependency_i;
		init(n, ds.size());

		dependency_i = 0;

		for (String d : ds) {
			dependencies[dependency_i] = d;
			dependency_i++;
		}
	}

	/**
	 * Constructor for variable number of dependencies,
	 * each as an argument
	 * @param n	name
	 * @param ds	array of elements to put into dependencies
	 */
	public Target(String n, String ... ds)
	{
		int dependency_i;
		init(n, ds.length);

		dependency_i = 0;
		for (dependency_i = 0; dependency_i < ds.length;
		     dependency_i++) {
			dependencies[dependency_i] = ds[dependency_i];
		}
	}

	/**
	 * Get name of rule, so that it can be used in target lists
	 * @return	name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Generate the rule to create the target
	 * @param output	the output stream
	 * @throws		IOException if there was error during writing
	 */
	public void genRule(MakeFormatter output) throws IOException
	{
		/* write header */
		Makefile.writeRuleHeader(output, name, dependencies);
		/* write body */
		output.indent();
		genCommand(output);
		output.unindent();
		output.newLine();
	}

	/**
	 * Generate the body of the rule to create the target
	 * @param output	the output stream
	 * @throws		IOException if there was error during writing
	 */
	protected abstract void
	genCommand(MakeFormatter output) throws IOException;
}
