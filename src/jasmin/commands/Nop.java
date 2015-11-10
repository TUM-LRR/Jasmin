package jasmin.commands;

import jasmin.core.*;

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
	public void execute(Parameters p) {
	}
	
}
