package jasmin.commands;

import jasmin.core.*;

/**
 * @author Jakob Kummerow
 */

public class Std extends JasminCommand {

	public String[] getID() {
		return new String[] {"STD", "CLD", "STC", "CLC", "CMC"};
	}

	public ParseError validate(Parameters p) {
		return p.validate(0, Op.NULL);
	}

	public void execute(Parameters p) {
		if (p.mnemo.equals("STD")) {
			dataspace.fDirection = true;
		} else if (p.mnemo.equals("CLD")) {
			dataspace.fDirection = false;
		} else if (p.mnemo.equals("STC")) {
			dataspace.fCarry = true;
		} else if (p.mnemo.equals("CLC")) {
			dataspace.fCarry = false;
		} else if (p.mnemo.equals("CMC")) {
			dataspace.fCarry = !dataspace.fCarry;
		}

	}

}
