package jasmin.commands;

import jasmin.core.*;
import jasmin.gui.JasDocument;

/**
 * @author Yang Guo
 */

public class Ret extends JasminCommand {
	
	public String[] getID() {
		return new String[] { "RET" };
	}
	
	public ParseError validate(Parameters p) {
		return p.validate(0, Op.NULL);
	}
	
	public void execute(JasDocument jasDocument, Parameters p) {
		p.pop(dataspace.EIP);
	}
	
}
