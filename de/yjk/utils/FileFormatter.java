/**
 * FileFormatter.java
 * Helps create formatted code files by preserving indentation across lines
 */
package de.yjk.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.IOException;
/**
 * Subclass of BufferedWriter that preserves indentation across new lines,
 * unless explicitly unindented.
 * Helps generate readable code.
 */
public class FileFormatter extends BufferedWriter
{
	/** Character for single indent */
	private static final char INDENT_CHAR = '\t';

	/** Indentation count, initialized to 0 */
	private int indentation;
	/**
	 * Was a new line started? If so, need to add indents.
	 * Initialized to true
	 */
	private boolean new_line;

	/**
	 * General constructor helper that initializes fields in this class,
	 * and not specific to any BufferedWriter constructor.
	 */
	private void init()
	{
		indentation = 0;
		new_line = true;
	}

	/**
	 * Constructor that automatically wraps new FileWriter around
	 * the given File object,
	 * and passes it to the BufferedWriter(Writer) constructor
	 * @param in_file	File object used to
	 *			generate the Writer argument
	 * @throws IOException	if that is thrown by FileWriter constructor
	 */
	public FileFormatter(File in_file) throws IOException
	{
		super(new FileWriter(in_file));
		init();
	}

	/**
	 * Constructor wrapper for the BufferedWriter(Writer) constructor
	 * @param out	the Writer to pass to the BufferedWriter constructor
	 */
	public FileFormatter(Writer out)
	{
		super(out);
		init();
	}

	/**
	 * Constructor wrapper for the BufferedWriter(Writer, int) constructor
	 * @param out	the Writer to pass to the BufferedWriter constructor
	 * @param sz	the int size to pass to the BufferedWriter constructor
	 */
	public FileFormatter(Writer out, int sz)
	{
		super(out, sz);
		init();
	}

	/**
	 * Wrapper to newLine of superclass
	 * to record fact that new line was started
	 * @throws IOException	if superclass newLine function throws it
	 */
	public void newLine() throws IOException
	{
		super.newLine();
		new_line = true;
	}

	/**
	 * Increment indentation
	 */
	public void indent()
	{
		indentation++;
	}

	/**
	 * Decrement indentation, unless it is not positive,
	 * in which case the function throws an UnindentException
	 */
	public void unindent()
	{
		if (indentation <= 0) {
			throw new UnindentException();
		}
		indentation--;
	}

	/**
	 * Write indentation characters if Writer is on new line,
	 * and disable automatic indenting on this line, which is no longer new
	 * @throws IOException	if writing indentation characters failed
	 */
	private void checkIndent() throws IOException
	{
		if (new_line) {
			/* write the proper number of indentation characters */
			char[] indents = new char[indentation];
			int indent_i;

			for (indent_i = 0; indent_i < indentation; indent_i++) {
				indents[indent_i] =  INDENT_CHAR;
			}
			super.write(indents, 0, indentation);

			/* line is no longer new */
			new_line = false;
		}
	}

	/**
	 * Wrapper class for BufferedWriter.write(int),
	 * with attempted indentation
	 * @param c	character value to pass to super method
	 * @throws IOException	if indentation or super method fails
	 */
	public void write(int c) throws IOException
	{
		checkIndent();
		super.write(c);
	}

	/**
	 * Wrapper class for BufferedWriter.write(char[], int, int),
	 * with attempted indentation
	 * @param cbuf	character buffer to pass to super method
	 * @param off	start offset to pass to super method
	 * @param len	write length to pass to super method
	 * @throws IOException	if indentation or super method fails
	 */
	public void write(char[] cbuf, int off, int len) throws IOException
	{
		checkIndent();
		super.write(cbuf, off, len);
	}

	/**
	 * Wrapper class for BufferedWriter.write(String, int, int),
	 * with attempted indentation
	 * @param s	character string to pass to super method
	 * @param off	start offset to pass to super method
	 * @param len	write length to pass to super method
	 * @throws IOException	if indentation or super method fails
	 */
	public void write(String s, int off, int len) throws IOException
	{
		checkIndent();
		super.write(s, off, len);
	}

	/**
	 * Wrapper class for Writer.write(char[]),
	 * with attempted indentation
	 * @param cbuf	character buffer to pass to super method
	 * @throws IOException	if indentation or super method fails
	 */
	public void write(char[] cbuf) throws IOException
	{
		checkIndent();
		super.write(cbuf);
	}

	/**
	 * Wrapper class for Writer.write(String),
	 * with attempted indentation
	 * @param s	character string to pass to super method
	 * @throws IOException	if indentation or super method fails
	 */
	public void write(String s) throws IOException
	{
		checkIndent();
		super.write(s);
	}

	/**
	 * Exception when unindent() is called, but there is no more indentation
	 */
	public static class UnindentException extends RuntimeException
	{
		/* Static error message for this Exception */
		private static final String
		UNINDENT_ERROR_MESSAGE = "Attempted unindent while " +
					 "there was no indentation";

		/*
		 * Sole constructor, which sets a static message
		 */
		public UnindentException()
		{
			super(UNINDENT_ERROR_MESSAGE);
		}
	}
}
