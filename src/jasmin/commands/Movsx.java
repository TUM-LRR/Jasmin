package jasmin.commands;

import jasmin.core.*;

/**
 * @author Jakob Kummerow
 */

public class Movsx extends JasminCommand {

	public String[] getID() {
		return new String[] {"MOVSX"};
	}

	public boolean signed() {
		return true;
	}

	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.R16 | Op.R32);
		if (e != null) { return e; }
		if (p.type(0) == Op.R16) {
			e = p.validate(1, Op.R8 | Op.M8);
		} else if (p.type(1) == Op.R32) {
			e = p.validate(1, Op.R8 | Op.R16 | Op.M8 | Op.M16);
		}
		if (e != null) { return e; }
		return p.validate(2, Op.NULL);
	}

	public void execute(Parameters p) {
		p.put(0, p.get(1), null);
	}

}
