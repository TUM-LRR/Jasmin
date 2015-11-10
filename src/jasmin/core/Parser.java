package jasmin.core;

import jasmin.gui.JasDocument;
import java.util.ArrayList;
import java.util.regex.*;

/**
 * @author Jakob Kummerow
 */
public class Parser {
	
	// several other components, needed to call their methods
	private final DataSpace dataspace;
	private final CommandLoader commandLoader;
	public JasDocument doc;
	
	// when the whole program is executed, commands and parameters are cached to
	// greatly improve execution speed in loops and frequently called subroutines
	private Parameters[] paramCache;
	private JasminCommand[] commandCache;
	private boolean[] cached;
	
	public Parser(DataSpace newDataSpace, CommandLoader cl, JasDocument jasDoc) {
		dataspace = newDataSpace;
		commandLoader = cl;
		doc = jasDoc;
	}
	
	/**
	 * Parses one line of assembler code
	 * 
	 * @param string
	 *        the code line
	 * @return a ParseResult object containing information about this line
	 */
	public ParseResult parse(String string, String lastLabel) {
		
		string = upcase(string);
		string = string.replaceAll("\\r", "");
		string = string.replaceAll("\\n", "");
		
		int tokenStartPos = 0, argstartcommand = 0;
		
		ParseResult result = new ParseResult();
		result.originalLine = string;
		if (string.matches("[ \t]*")) {
			result.empty = true;
			return result;
		}
		
		// save the original string for later reference, manipulate a copy of it
		String s = escape(string); // escaping has to be done to allow for spaces and tabs inside strings
		
		// comment at the end? strip it!
		s = stripComments(s, result);
		
		// label at the beginning? remember it, then strip it!
		// first, see whether there might be a label in the string
		if (labelEnd(s) != -1) {
			// if so, check its validity
			String checkedLabel = getLabel(s);
			if (checkedLabel == null) {
				result.error = new ParseError(result.originalLine, getRawLabelString(s), 0, "Invalid Label");
				return result;
			} else {
				result.label = checkedLabel;
				lastLabel = checkedLabel;
				tokenStartPos = labelEnd(s);
				s = stripLabel(s);
			}
		}
		
		// anything left?
		if (s.matches("[, \t]*")) {
			result.labelOnly = true;
			return result;
		}
		
		// seems to be a command, analyze it
		ArrayList<FullArgument> arguments = new ArrayList<FullArgument>();
		String command = null, argument = "";
		boolean commaDone = false;
		int nextSize = -1, size, type, lastType = Op.NULL;
		boolean sizeExplicit = false;
		String[] tokens = s.split("[ \t]"); // split the string into "words"/tokens
		for (String token : tokens) { // and look at any ...
			if (!token.equals("")) { // ... non-empty word
			
				token = unescape(token);
				argument = hex2dec(token); // convert any numbers to decimal
				type = getOperandType(argument);
				// the token's start position is needed for correct error highlighting
				tokenStartPos = string.indexOf(token, tokenStartPos);
				// syntax checking
				if (lastType == Op.SIZEQUALI) {
					if (!Op.matches(type, Op.MEM | Op.IMM | Op.LABEL | Op.VARIABLE)) {
						result.error = new ParseError(string, token, tokenStartPos,
							"Only an immediate or a memory location is allowed after a size qualifier");
						return result;
					}
				}
				if (type == Op.COMMA) {
					if (!Op.matches(lastType, Op.PARAM)) {
						result.error = new ParseError(string, token, tokenStartPos,
							"A comma must only be placed after a parameter");
						return result;
					}
					commaDone = true;
				}
				// the actual parsing
				
				if (Op.matches(type, Op.SIZEQUALI)) {
					// if it's a size qualifier, remember its information and we're done
					nextSize = getOperandSize(token, type);
				} else if (type == Op.COMMA) {
					// if it was only a comma, do nothing else
				} else if (command == null) {
					// if there was no command so far, the current token will be it
					command = token;
					result.mnemo = command;
					argstartcommand = tokenStartPos;
					commaDone = true; // no comma necessary directly after the command
					// otherwise, it's an argument for a command
				} else {
					// determine the argument's size
					size = -1;
					if (!commaDone && Op.matches(type, Op.PARAM)) {
						result.error = new ParseError(string, token, tokenStartPos,
							"You must place a comma between any two parameters");
						return result;
					}
					if (nextSize != -1) { // previous size qualifier?
						if (Op.matches(type, Op.IMM | Op.CHARS)) {
							if (getOperandSize(argument, type) > nextSize) {
								result.error = new ParseError(string, token, tokenStartPos,
									"Operand does not match previous size qualifier.");
								return result;
							}
						}
						size = nextSize;
						nextSize = -1;
						sizeExplicit = true;
					} else if (!Op.matches(type, Op.IMM)) {
						size = getOperandSize(argument, type);
					}
					// update the type information with the size, e.g. we had MEM and now get M32
					type = getSizedOperandType(argument, type, size);
					// add the argument to the list of arguments
					arguments.add(new FullArgument(argument, token, tokenStartPos, type, size, sizeExplicit,
						dataspace));
					sizeExplicit = false;
					commaDone = false;
					if (type == Op.FPUQUALI) {
						commaDone = true;
					}
					// if the first token was a prefix and the current argument is actually
					// the command, no comma is required afterwards
					if (commandLoader.commandExists(argument)) {
						commaDone = true;
					}
				}
				lastType = type; // remember the argument's type
				tokenStartPos += token.length();// and add its length to the search start position for the
				// next one
			}
		}
		
		// swap command and first argument if the command is actually a prefix
		if (Op.matches(getOperandType(command), Op.PREFIX) && (arguments.size() > 0)) {
			String temp = command;
			command = arguments.get(0).arg;
			result.mnemo = command;
			arguments.set(0, new FullArgument(temp, temp, 0, Op.PREFIX, -1, false, dataspace));
			argstartcommand += temp.length();
		} else if ((arguments.size() > 0) && (arguments.get(0).address.type == Op.PREFIX)) {
			result.error = new ParseError(string, arguments.get(0),
				"Prefixes must be placed before the command");
			return result;
		}
		
		// check whether the command exists
		if (commandLoader.commandExists(command) == false) {
			result.mnemo = null;
			result.error = new ParseError(string, command, argstartcommand, "Unknown command");
			return result;
		}
		// load the command object
		JasminCommand cmd = (JasminCommand) commandLoader.getCommand(command);
		if (cmd instanceof PreprocCommand) {
			// note: PreprocCommands will be executed later on!
			if (lastLabel == null) {
				result.error = new ParseError(string, command, 0,
					"Preprocessor commands must be preceded by a label.");
				return result;
			} else {
				dataspace.registerConstant(lastLabel);
			}
		} else if (cmd instanceof PseudoCommand) {
			// if a pseudo command follows a label, the label has to be registered as a variable,
			// as the command won't be executed right away
			if (lastLabel != null) {
				dataspace.registerVariable(lastLabel);
			}
		} else {
			// if a normal command follows a label that previously may have been a constant/variable, reset
			// its state
			if (lastLabel != null) {
				dataspace.unregisterConstant(lastLabel);
				dataspace.unregisterVariable(lastLabel);
			}
		}
		
		// check for >1 memory access
		if (!cmd.overrideMaxMemAccess(command)) {
			int numMemoryAccesses = 0;
			for (int i = 0; i < arguments.size(); i++) {
				if ((arguments.get(i).address.type & Op.MEM) != 0) {
					numMemoryAccesses++;
				}
				if (numMemoryAccesses > 1) {
					result.error = new ParseError(string, arguments.get(i), "Only one memory access allowed.");
					return result;
				}
				
			}
		}
		
		// initialize a Parameters object for the command to play with
		Parameters param = new Parameters(dataspace, this);
		param.set(string, command, arguments, cmd.defaultSize(command), cmd.signed());
		if (lastLabel != null) {
			param.label = lastLabel;
		}
		for (int i = 0; i < arguments.size(); i++) {
			result.usedLabels.addAll(arguments.get(i).usedLabels);
		}
		
		for (int i = 0; i < arguments.size(); i++) {
			// check validity of the arguments
			String errorMsg = isValidOperand(arguments.get(i), false);
			if (errorMsg != null) {
				result.error = new ParseError(string, arguments.get(i), errorMsg);
				return result;
			}
		}
		
		// let the command do command-specific validating of its arguments
		ParseError error = cmd.validate(param);
		if (error != null) {
			result.error = error;
			return result;
		}
		
		result.command = cmd;
		result.param = param;
		
		// preprocessing commands are executed right now
		if (cmd instanceof PreprocCommand) {
			cmd.execute(param);
		}
		return result;
	}
	
	public ParseError execute(String line, String lastLabel, int lineNumber) {
		// cached execution
		if ((lineNumber != -1) && cached[lineNumber]) {
			JasminCommand command = commandCache[lineNumber];
			if (command != null) {
				Parameters p = paramCache[lineNumber];
				command.execute(p);
				if (dataspace.addressOutOfRange()) {
					dataspace.clearAddressOutOfRange();
					return new ParseError("", "", 0,
						"Memory address out of range. Might be a stack over-/underflow.");
				}
			}
			return null;
		}
		// if not yet or not at all cached: normal parsing
		ParseResult parseResult = parse(line, lastLabel);
		// if cached execution is enabled, save the command and its parameters
		if (lineNumber != -1) {
			commandCache[lineNumber] = parseResult.command;
			paramCache[lineNumber] = parseResult.param;
			cached[lineNumber] = true;
		}
		
		if (parseResult.error != null) {
			return parseResult.error;
		}
		if (parseResult.empty || parseResult.labelOnly || (parseResult.command == null)) {
			return null;
		}
		
		if (parseResult.param != null) {
			// check arguments again, with executeNow=true
			for (int i = 0; i < parseResult.param.numArguments; i++) {
				// check validity of the arguments
				String errorMsg = isValidOperand(parseResult.param.argument(i), true);
				if (errorMsg != null) {
					return new ParseError(parseResult.originalLine, parseResult.param.argument(i), errorMsg);
				}
			}
		}
		
		// execute now
		parseResult.command.execute(parseResult.param);
		// if the command was a pseudo command, update the address its variable refers to
		if (lineNumber == -1) {
			dataspace.updateDirty();
		}
		// if an invalid memory access occurred that validating could not prevent, report it to the GUI
		if (dataspace.addressOutOfRange()) {
			dataspace.clearAddressOutOfRange();
			return new ParseError("", "", 0, "Memory address out of range. Might be a stack over-/underflow.");
		}
		return null;
	}
	
	/**
	 * Clears the cache for commands and parameters and initializes it with an array of the given size.
	 * 
	 * @param cacheSize
	 *        the desired size of the cache, i.e. the number of lines that are about to be cached
	 */
	public void clearCache(int cacheSize) {
		paramCache = new Parameters[cacheSize];
		commandCache = new JasminCommand[cacheSize];
		cached = new boolean[cacheSize];
	}
	
	// a couple of often-used reg-ex patterns
	public static final String DELIM = "[\\[\\]\\+\\-\\*\\:\\.\t,;' ]";
	private static final Pattern pHex0x = Pattern.compile("(^|" + DELIM + ")(0X[0-9A-F]+)(" + DELIM + "|$)");
	private static final Pattern pHexH = Pattern.compile("(^|" + DELIM + ")([0-9][0-9A-F]*H)(" + DELIM
		+ "|$)");
	private static final Pattern pHexDollar = Pattern.compile("(^|" + DELIM + ")(\\$[0-9][0-9A-F]*)(" + DELIM
		+ "|$)");
	private static final Pattern pBinB = Pattern.compile("(^|" + DELIM + ")([01]+B)(" + DELIM + "|$)");
	private static final Pattern pOctO = Pattern.compile("(^|" + DELIM + ")([0-7]+O)(" + DELIM + "|$)");
	private static final Pattern pOctQ = Pattern.compile("(^|" + DELIM + ")([0-7]+Q)(" + DELIM + "|$)");
	
	/**
	 * converts hex, oct and bin numbers inside a string into decimal numbers. Numbers need to have
	 * appropriate uppercase(!) pre- or suffixes (e.g. "0X...") for hex2dec to know what to do.
	 * 
	 * @param s
	 *        the original string
	 * @return the string with all numbers contained in it converted to decimal format
	 */
	public static String hex2dec(String s) {
		try {
			if (pHex0x.matcher(s).find()) {
				s = replaceNumber(s, pHex0x, 16, 2, 0);
			}
			if (pHexH.matcher(s).find()) {
				s = replaceNumber(s, pHexH, 16, 0, 1);
			}
			if (pHexDollar.matcher(s).find()) {
				s = replaceNumber(s, pHexDollar, 16, 1, 0);
			}
			if (pBinB.matcher(s).find()) {
				s = replaceNumber(s, pBinB, 2, 0, 1);
			}
			if (pOctO.matcher(s).find()) {
				s = replaceNumber(s, pOctO, 8, 0, 1);
			}
			if (pOctQ.matcher(s).find()) {
				s = replaceNumber(s, pOctQ, 8, 0, 1);
			}
		} catch (NumberFormatException e) {
		}
		return s;
	}
	
	/**
	 * an internal helper function for hex2dec(), replaces a given type of numbers inside the passed string
	 * 
	 * @param s
	 *        the string with the numbers in it
	 * @param numberPattern
	 *        the regex-pattern of the affected numbers
	 * @param base
	 *        the number system's base of the affected numbers
	 * @param skipFirst
	 *        the number of bytes at the beginning of the pattern that should be skipped when converting
	 *        because they
	 *        are a prefix
	 * @param skipLast
	 *        the number of bytes at the end of the pattern that should be skipped when converting because
	 *        they are a
	 *        suffix
	 * @return the string with the affected numbers converted to decimal format
	 */
	private static String replaceNumber(String s, Pattern numberPattern, int base, int skipFirst, int skipLast) {
		Matcher m = numberPattern.matcher(s);
		int matchstart = 0;
		while (m.find(matchstart)) {
			String n = m.group(2);
			s = s.replaceFirst(n, Long.toString(Long.parseLong(n.substring(skipFirst,
				n.length() - skipLast), base)));
			matchstart = m.end(2);
		}
		return s;
	}
	
	/**
	 * Removes comments from a line of code. Comments are recognised by their beginning ";"
	 * 
	 * @param s
	 *        the string that may contain a comment
	 * @return the string with any comment removed
	 */
	public static String stripComments(String s, ParseResult result) {
		int commentStart = s.indexOf(";");
		if (commentStart != -1) {
			s = s.substring(0, commentStart);
			result.commentStartPos = commentStart;
			// undo the effect escape() has had on commas
			int index = s.indexOf(" , ");
			while (index > -1) {
				result.commentStartPos -= 2;
				index = s.indexOf(" , ", index + 3);
			}
		}
		return s.trim();
	}
	
	/**
	 * Determines the position in the String where a label that might be contained ends
	 * 
	 * @param s
	 *        the string that is to be examined
	 * @return the position of the colon where the label ends, -1 if there is no label in the string
	 */
	private static int labelEnd(String s) {
		int colonIndex = s.indexOf(":");
		int semicolonIndex = s.indexOf(";");
		if ((semicolonIndex == -1) || (colonIndex < semicolonIndex)) {
			return colonIndex;
		} else {
			return -1;
		}
	}
	
	/**
	 * Removes a label from a given String
	 * 
	 * @param s
	 *        the String that may contain a label
	 * @return the String with any label removed
	 */
	public static String stripLabel(String s) {
		int labelEnd = labelEnd(s);
		if (labelEnd == -1) {
			return s.trim();
		} else {
			return s.substring(labelEnd + 1).trim();
		}
	}
	
	/**
	 * Returns the "raw label" that might be inside a String, "raw" meaning it is not checked for validity.
	 * 
	 * @param s
	 *        the String that is to be examined
	 * @return the label inside the String, or null if there was none
	 */
	private static String getRawLabelString(String s) {
		int labelEnd = labelEnd(s);
		if (labelEnd == -1) {
			return null;
		} else {
			String label = s.substring(0, labelEnd(s));
			int index = 0;
			while ((index < label.length())
				&& ((label.charAt(index) == ' ') || (label.charAt(index) == '\t'))) {
				index++;
			}
			label = label.substring(index, label.length());
			return label;
		}
	}
	
	/**
	 * Returns the label inside a String, checked for validity.
	 * 
	 * @param s
	 *        the String that is to be examined
	 * @return the label inside the String, or null if there was none
	 */
	public String getLabel(String s) {
		s = s.toUpperCase();
		String label = getRawLabelString(s);
		if (label != null) {
			if ((label.indexOf(" ") != -1) || (label.indexOf("\t") != -1) || (label.indexOf("'") != -1)) {
				return null;
			}
			int labeltype = getOperandType(label);
			if (commandLoader.commandExists(label)) {
				return null;
			}
			if (Op.matches(labeltype, Op.ERROR | Op.LABEL | Op.VARIABLE | Op.CONST)) {
				return label;
			}
		}
		return null;
	}
	
	/**
	 * Updates an operand type with size information obtained elsewhere and passed as a parameter. Currently
	 * only used for memory locations and floating-point types.
	 * 
	 * @param operand
	 *        the operand that the information is about
	 * @param unsizedType
	 *        the type of the operand (without size information)
	 * @param size
	 *        the size of the operand
	 * @return the new type of the operand with contained size information
	 */
	public int getSizedOperandType(String operand, int unsizedType, int size) {
		if (Op.matches(unsizedType, Op.MEM)) {
			return Op.getDefinition(Op.MEM, size);
		}
		if (Op.matches(unsizedType, Op.FLOAT)) {
			try {
				if (size == 4) {
					Float.valueOf(operand);
				} else if ((size == 8) || (size == -1)) {
					Double.valueOf(operand);
				}
			} catch (NumberFormatException e) {
				return Op.ERROR;
			}
			return Op.FLOAT;
		}
		return unsizedType;
	}
	
	// again, some regex patterns that are used often
	public static final Pattern pMemory = Pattern.compile("\\[.*\\]");
	public static final Pattern pDecimal = Pattern.compile("\\-?\\d+");
	public static final Pattern pFloat = Pattern.compile("\\-?[0-9]+\\.[0-9]*(E[\\+\\-]?[0-9]+)?");
	public static final Pattern pSizeQuali = Pattern.compile("((BYTE)|(WORD)|(DWORD)|(QWORD))");
	public static final Pattern pString = Pattern.compile("'.*'");
	
	/**
	 * Returns the type of a given operand. Try not to call this method more often than necessary as it is
	 * rather time-intensive.
	 * 
	 * @param operand
	 *        the operand (as a String)
	 * @return the operand's type (as one of the integer constants defined in Op)
	 */
	public int getOperandType(String operand) {
		// empty
		if (operand == null) {
			return Op.NULL;
		}
		if (operand.equals("")) {
			return Op.NULL;
		}
		// comma
		if (operand.equals(",")) {
			return Op.COMMA;
		}
		// memory address
		if (pMemory.matcher(operand).matches()) {
			return Op.MU;
		}
		// register
		if (CalculatedAddress.pRegisters.matcher(operand).matches()) {
			return Op.getDefinition(Op.REG, dataspace.getRegisterSize(operand));
		}
		// (decimal) immediate
		if (pDecimal.matcher(operand).matches()) {
			try {
				Long.valueOf(operand);
			} catch (NumberFormatException e) {
				return Op.ERROR;
			}
			return Op.getDefinition(Op.IMM, getOperandSize(Long.valueOf(operand)));
		}
		// floating-point constant
		if (pFloat.matcher(operand).matches()) {
			try {
				Double.valueOf(operand);
			} catch (NumberFormatException e) {
				return Op.ERROR;
			}
			return Op.FLOAT;
		}
		// variable
		if (dataspace.isVariable(operand)) {
			return Op.VARIABLE;
		}
		// constant (defined by EQU)
		if (dataspace.isConstant(operand)) {
			return Op.CONST;
		}
		// label (target for jump-commands)
		if (doc.getLabelLine(operand) != -1) {
			return Op.LABEL;
		}
		// size qualifier
		if (pSizeQuali.matcher(operand).matches()) {
			return Op.SIZEQUALI;
		}
		// prefix
		if (DataSpace.prefixesMatchingPattern.matcher(operand).matches()) {
			return Op.PREFIX;
		}
		// string/character constant
		if (pString.matcher(operand).matches()) {
			// string: up to four characters (short) or arbitrarily long
			if (operand.length() <= 6) {
				return Op.CHARS;
			} else {
				return Op.STRING;
			}
		}
		// FPU register
		if (Fpu.pRegisters.matcher(operand).matches()) {
			return Op.FPUREG;
		}
		// FPU qualifier
		if (Fpu.pQualifiers.matcher(operand).matches()) {
			return Op.FPUQUALI;
		}
		return Op.ERROR;
	}
	
	/**
	 * Checks whether an operand is valid
	 * 
	 * @param operand
	 *        the operand that is to be examined
	 * @param executeNow
	 *        pass true here if the line containing the operand is just about to be executed. This affects the
	 *        range checking of calculated memory addresses.
	 * @return null if everything's fine, an error message otherwise
	 */
	public String isValidOperand(FullArgument operand, boolean executeNow) {
		if (operand.address.type == Op.ERROR) {
			return "Invalid Expression";
		}
		if ((operand.address.type & Op.MEM) != 0) {
			return isValidAddress(operand.arg, operand.address.size, executeNow);
		}
		return null;
	}
	
	/**
	 * Checks whether a memory address is valid
	 * 
	 * @param s
	 *        the memory address to be validated
	 * @param size
	 *        the number of bytes that are to be written/read
	 * @param executeNow
	 *        pass true here if the line containing the operand is just about to be executed. If false is
	 *        passed, calculated addresses that currently are out of range are not reported as errors.
	 * @return null if everything's fine, an error message otherwise
	 */
	public String isValidAddress(String s, int size, boolean executeNow) {
		if (size == -1) {
			size = 1;
		}
		CalculatedAddress a = new CalculatedAddress(dataspace);
		String errorString = a.readFromString(s);
		if (errorString != null) {
			return errorString;
		}
		return a.isValid(size, executeNow);
	}
	
	/**
	 * Determines the (minimum) size of an immediate operand
	 * 
	 * @param operand
	 *        the operand that is to be examined
	 * @return the size of the operand
	 */
	public static int getOperandSize(long operand) {
		int size = -1;
		if (operand < 0) {
			operand *= (-1);
			operand -= 1;
			if ((operand & 127) == operand) {
				size = 1;
			} else if ((operand & ((1 << 15) - 1)) == operand) {
				size = 2;
			} else if ((operand & ((1 << 31) - 1)) == operand) {
				size = 4;
			} else {
				operand = 8;
			}
		} else {
			if ((operand & 255) == operand) {
				size = 1;
			} else if ((operand & 65535) == operand) {
				size = 2;
			} else if ((operand & Long.valueOf("4294967295")) == operand) {
				size = 4;
			} else {
				size = 8;
			}
		}
		return size;
	}
	
	/**
	 * Returns the size of an operand of any type
	 * 
	 * @param operand
	 *        the operand that is to be examined
	 * @param type
	 *        the operand's type
	 * @return the calculated size of the parameter
	 */
	public int getOperandSize(String operand, int type) {
		int size = -1;
		if (Op.matches(type, Op.SIZEQUALI)) {
			if (operand.equals("BYTE")) {
				size = 1;
			}
			if (operand.equals("WORD")) {
				size = 2;
			}
			if (operand.equals("DWORD")) {
				size = 4;
			}
			if (operand.equals("QWORD")) {
				size = 8;
			}
		} else if (Op.matches(type, Op.REG)) {
			size = dataspace.getRegisterSize(operand);
		} else if (Op.matches(type, Op.IMM)) {
			long arg = Long.valueOf(operand);
			size = getOperandSize(arg);
		} else if (Op.matches(type, Op.FPUREG)) {
			size = 8;
		} else if (type == Op.CHARS) {
			size = roundToOpSize(operand.replaceAll("'", "").length());
		} else if (type == Op.STRING) {
			return 1;
		}
		return size;
	}
	
	/**
	 * Converts a String of type Op.CHARS into concatenated ASCII numbers. Take care not to pass a String that
	 * is longer than 4 characters, as for performance reasons no checking is performed.
	 * 
	 * @param chars
	 *        the String that is to be converted
	 * @return an ASCII representation of the input String
	 */
	public static long getCharsAsNumber(String chars) {
		chars = chars.replaceAll("'", "");
		long result = 0;
		for (int i = 0; i < chars.length(); i++) {
			result |= ((chars.charAt(i)) << (8 * i));
		}
		return result;
	}
	
	/**
	 * Rounds an integer number to the next larger power of 2. Intended to be used for String/character
	 * constants
	 * 
	 * @param a
	 *        the native size of the String / operation
	 * @return the size, rounded to the next larger power of 2
	 */
	public static int roundToOpSize(int a) {
		if ((a == 0) || (a == 1) || (a == 2) || (a == 4) || (a == 8)) {
			return a;
		} else if (a == 3) {
			return 4;
		} else if (a < 8) {
			return 8;
		} else {
			while ((a % 4) != 0) {
				a++;
			}
			return a;
		}
	}
	
	/**
	 * returns the index inside the given string where a comment starts (usually the first ";")
	 * 
	 * @param line
	 *        the String with the comment in it
	 * @return the index where the comment starts, -1 if no comment is found
	 */
	public static int commentStart(String line) {
		boolean insidequote = false;
		for (int i = 0; i < line.length(); i++) {
			char current = line.charAt(i);
			if (current == '\'') {
				insidequote = !insidequote;
			} else if (!insidequote) {
				if (current == ';') {
					return i;
				}
			}
		}
		return -1;
		
	}
	
	public static String upcase(String input) {
		boolean insidequote = false;
		String output = "";
		for (int i = 0; i < input.length(); i++) {
			char current = input.charAt(i);
			if (current == '\'') {
				insidequote = !insidequote;
				output += current;
			} else if (insidequote) {
				output += current; // no uppercase inside quoted strings!
			} else {
				output += Character.toUpperCase(current);
			}
		}
		return output;
	}
	
	/**
	 * Escapes spaces, tabs and commas inside any apostrophe-enclosed subsections of the input String. E.g. in
	 * "a 'b c'", the space between b and c would be escaped. Also uppercases all characters that are not
	 * inside a quoted string.
	 * 
	 * @param input
	 *        the input String
	 * @return the input string with all affected characters escaped/converted to uppercase, respectively
	 */
	public static String escape(String input) {
		boolean insidequote = false;
		String output = "";
		for (int i = 0; i < input.length(); i++) {
			char current = input.charAt(i);
			if (current == '\'') {
				insidequote = !insidequote;
				output += current;
			} else if (insidequote) {
				if (current == ' ') {
					output += "\\s";
				} else if (current == '\t') {
					output += "\\t";
				} else if (current == ',') {
					output += "\\c";
				} else if (current == '\\') {
					output += "\\b";
				} else if (current == ';') {
					output += "\\p";
				} else if (current == ':') {
					output += "\\d";
				} else {
					output += current;
				} // no uppercase inside quoted strings!
			} else {
				if (current == ',') {
					output += " , "; // add spaces around every comma
				} else {
					output += current;
				}
			}
		}
		return output;
	}
	
	/**
	 * Undoes the effect of the corresponding escape() function
	 * 
	 * @param input
	 *        the input string
	 * @return the string, restored to its state before being escape()'ed
	 */
	public static String unescape(String input) {
		boolean insidequote = false;
		boolean escapeNext = false;
		String output = "";
		for (int i = 0; i < input.length(); i++) {
			char current = input.charAt(i);
			if (current == '\'') {
				insidequote = !insidequote;
				output += current;
			} else if (insidequote) {
				if (current == '\\') {
					escapeNext = true;
				} else if (escapeNext) {
					if (current == 's') {
						output += ' ';
					} else if (current == 't') {
						output += '\t';
					} else if (current == 'c') {
						output += ',';
					} else if (current == 'b') {
						output += '\\';
					} else if (current == 'p') {
						output += ';';
					} else if (current == 'd') {
						output += ':';
					}
					escapeNext = false;
				} else {
					output += current;
				}
			} else {
				output += current;
			}
		}
		return output;
	}
	
}
