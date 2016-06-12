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
		switch (p.mnemo) {
			case "STD":
				dataspace.fDirection = true;
				break;
			case "CLD":
				dataspace.fDirection = false;
				break;
			case "STC":
				dataspace.fCarry = true;
				break;
			case "CLC":
				dataspace.fCarry = false;
				break;
			case "CMC":
				dataspace.fCarry = !dataspace.fCarry;
				break;
		}

	}

}
