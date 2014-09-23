/**
 * ArchiveTarget.java
 * Archive file
 */
package de.yjk.cgen;

import java.io.IOException;
import java.util.Collection;

/**
 * Archive file target and its dependencies,
 * which are object files that compose it
 */
public class ArchiveTarget extends Target
{
	/**
	 * Calls corresponding superconstructor in Target
	 * @param n	n in superconstructor
	 * @param ds	ds in superconstructor
	 */
	public ArchiveTarget(String n, Collection<String> ds)
	{
		super(n, ds);
	}

	protected void genCommand(MakeFormatter output) throws IOException
	{
		String
		command = MakeFormatter
			  .genList(MakeFormatter.genUseVar(Makefile.AR_VAR),
				   MakeFormatter.genUseVar(Makefile
							   .AR_FLAGS_VAR),
				   Makefile.USE_OUT_IN_VARS);
		output.write(command);
	}
}
