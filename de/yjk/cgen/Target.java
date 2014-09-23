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
	 * @param n	name
	 * @param ds	contains elements to put into dependencies
	 */
	public Target(String n, Collection<String> ds)
	{
		name = n;
		dependencies = new String[ds.size()];
		int dependency_i = 0;

		for (String d : ds) {
			dependencies[dependency_i] = d;
			dependency_i++;
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
