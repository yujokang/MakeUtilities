/**
 * MakeFormatter.java
 * Extends FileFormatter to generate Makefiles in the Makefile/shell syntax.
 */
package de.yjk.cgen;

import de.yjk.utils.FileFormatter;

import java.io.File;
import java.io.IOException;

/**
 * Generates files formatted in the Makefile/shell syntax
 */
public class MakeFormatter extends FileFormatter
{
	/** template for using value of variable */
	private static final String VAR_USE_FMT = "$(%s)";
	/** template for assigning value of variable */
	private static final String VAR_DEF_FMT = "%s=%s";
	/** template for concatenating value to variable */
	private static final String VAR_CAT_FMT = "%s+=%s";

	/** delimiter between list elements, such as multiple options */
	public static final char LIST_DELIM = ' ';

	/**
	 * Constructor for FileFormatter(File)
	 * @param in_file	File object to pass to the superconstructor
	 * @throws IOException	if that is thrown by superconstructor
	 */
	public MakeFormatter(File in_file) throws IOException
	{
		super(in_file);
	}

	/**
	 * Write line to assign value to named variable
	 * @param name		name of variable
	 * @param value		new value of variable
	 * @throws IOException	if there was an error during writing
	 */
	public void assignVar(String name, String value) throws IOException
	{
		write(String.format(VAR_DEF_FMT, name, value));
		newLine();
	}

	/**
	 * Write line to append value to named variable
	 * @param name		name of variable
	 * @param append_value	value to append variable
	 * @throws IOException	if there was an error during writing
	 */
	public void appendVar(String name,
			      String append_value) throws IOException
	{
		write(String.format(VAR_CAT_FMT, name, append_value));
		newLine();
	}

	/**
	 * Write variable dereferencing expression
	 * @param name		name of variable
	 * @return		expression for dereferencing variable
	 */
	public static String genUseVar(String name)
	{
		return String.format(VAR_USE_FMT, name);
	}

	/**
	 * Join String elements by list delimiter
	 * @param elements	array of elements to join.
	 *			Can be null or empty, for an empty return value
	 * @return		delimited list of element
	 */
	public static String genList(String ... elements)
	{
		if (elements != null && elements.length > 0) {
			/* count and allocate number of characters needed */
			/*
			 * will have to subtract 1 because output won't need
			 * delimiter before first element,
			 * and add 1 for 0 character
			 */
			int char_count = elements.length - 1 + 1;
			StringBuilder list_builder;

			for (String element : elements) {
				char_count += element.length();
			}
			list_builder = new StringBuilder(char_count);

			/* copy over needed characters */
			/*
			 * first element is special,
			 * without delimiter in front
			 */
			list_builder.append(elements[0].toCharArray());
			/*
			 * rest of elements have delimiter in front,
			 * to separate from the previous element
			 */
			for (int element_i = 1; element_i < elements.length;
			     element_i++) {
				list_builder.append(LIST_DELIM);
				list_builder.append(elements[element_i]
						    .toCharArray());
			}
			return list_builder.toString();
		} else {
			/* return empty String for null array or no elements */
			return "";
		}
	}
}
