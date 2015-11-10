package jasmin.commands;

import jasmin.core.*;

/**
 * @author Yang Guo
 */

public class Pop extends JasminCommand {
	
	/**
	 * @param mnemo
	 *        the mnemo for the command whose default size is requested
	 */
	public int defaultSize(String mnemo) {
		return 2;
	}
	
	public String[] getID() {
		return new String[] { "POP" };
	}
	
	public ParseError validate(Parameters p) {
		ParseError e = p.numericDestOK();
		if (e != null) {
			return e;
		}
		e = p.validateAllSizes(2, 4);
		return e;
	}
	
	public void execute(Parameters p) {
		p.pop(p.argument(0).address);
	}
	
}
