package jasmin.commands;

import jasmin.core.*;

public class Equ extends PreprocCommand {
	
	@Override
	public String[] getID() {
		return new String[] { "EQU" };
	}
	
	@Override
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.IMM);
		if (e != null) {
			return e;
		}
		e = p.validate(1, Op.NULL);
		if (e != null) {
			return e;
		}
		execute(p);
		return null;
	}
	
	@Override
	public void execute(Parameters p) {
		dataspace.setConstantValue(p.label, p.get(0));
	}
	
}
