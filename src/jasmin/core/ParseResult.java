/**
 * 
 */
package jasmin.core;

import java.util.HashSet;

/**
 * @author Jakob Kummerow
 */
public class ParseResult {
	
	// String-based information for the highlighter
	public String originalLine;
	public boolean empty = false;
	public String label;
	public boolean labelOnly = false;
	public String mnemo;
	public int commentStartPos = -1;
	public HashSet<String> usedLabels = new HashSet<>();
	
	// executable objects that the Parser found
	public JasminCommand command;
	public Parameters param;
	
	public ParseError error;
}
