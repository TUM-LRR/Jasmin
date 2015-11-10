package jasmin.commands;

import jasmin.core.*;

public class Lea extends JasminCommand {

	public String[] getID() {
		return new String[] {"LEA"};
	}

	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.REG);
		if (e != null) { return e; }
		e = p.validate(1, Op.MEM);
		if (e != null) { return e; }
		return p.validate(2, Op.NULL);
	}

	public void execute(Parameters p) {
		p.put(0, p.argument(1).cAddress.calculateEffectiveAddress(true), null);
	}

}
