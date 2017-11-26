package jasmin.commands;

import jasmin.core.*;
import jasmin.gui.JasDocument;

public class Nop extends jasmin.core.JasminCommand {
	
	@Override
	public String[] getID() {
		return new String[] { "NOP", "FNOP" };
	}
	
	@Override
	public ParseError validate(Parameters p) {
		return p.validate(0, Op.NULL);
	}
	
	@SuppressWarnings("unused")
	@Override
	public void execute(JasDocument jasDocument, Parameters p) {
	}
	
}
