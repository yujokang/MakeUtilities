/*
 * Executable binary
 */
package de.yjk.cgen;

import java.io.IOException;
import java.util.Collection;

public class BinaryTarget extends Target
{
	/* are all dependencies generated with the C compiler? */
	private boolean all_c;
	/*
	 * Calls Target(String, Collection<String>) superconstructor,
	 * and records if the target is all C source code
	 * @param n	n in superconstructor
	 * @param ds	ds in superconstructor
	 * @param ac	all_c
	 */
	public BinaryTarget(String n, Collection<String> ds, boolean ac)
	{
		super(n, ds);
		all_c = ac;
	}

	protected void genCommand(MakeFormatter output) throws IOException
	{
		/* Decide on which compiler to use */
		String command_var = all_c ? Makefile.CC_VAR : Makefile.CPP_VAR;
		String
		command = MakeFormatter
			  .genList(MakeFormatter.genUseVar(command_var),
				   MakeFormatter.genUseVar(Makefile
							   .CPPFLAGS_VAR),
				   Makefile.USE_OUT_IN_VARS);
		output.write(command);
	}
}

