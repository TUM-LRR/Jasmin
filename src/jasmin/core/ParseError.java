package jasmin.core;

/**
 * @author Jakob Kummerow
 *         just a class to pass errors during parsing
 */
public class ParseError {
	
	/**
	 * the error message
	 */
	public String errorMsg;
	
	/**
	 * where the error starts in the line
	 */
	public int startPos;
	
	/**
	 * how long the error (string) is
	 */
	public int length;
	
	/**
	 * method designed for System.out.print()
	 * 
	 * @return a string representation of the object. only for debugging purposes.
	 */
	public String toString() {
		return "Characters from " + startPos + ", length " + length + ": " + errorMsg;
	}
	
	/**
	 * the method called by the constructors
	 * 
	 * @param line
	 * @param errorString
	 * @param searchStartPosition
	 * @param errorMessage
	 */
	private void setValues(String line, String errorString, int searchStartPosition, String errorMessage) {
		startPos = line.indexOf(errorString, searchStartPosition);
		length = errorString.length();
		errorMsg = errorMessage;
	}
	
	/**
	 * @param line
	 *        the original whole line containing the error
	 * @param errorString
	 *        the error itself
	 * @param searchStartPosition
	 *        where the error occured in that line
	 * @param errorMessage
	 *        the message to give to the user
	 */
	public ParseError(String line, String errorString, int searchStartPosition, String errorMessage) {
		setValues(line, errorString, searchStartPosition, errorMessage);
	}
	
	/**
	 * @param line
	 *        the original whole line containing the error
	 * @param invalidArgument
	 *        the argument that is faulty
	 * @param errorMsg
	 *        the message to give to the user
	 */
	public ParseError(String line, FullArgument invalidArgument, String errorMsg) {
		setValues(line, invalidArgument.original, invalidArgument.startPos, errorMsg);
	}
	
}
