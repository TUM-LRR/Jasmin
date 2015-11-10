package jasmin.commands;

import jasmin.core.*;

/**
 * @author Jakob Kummerow
 */

public class And extends JasminCommand {

	public String[] getID() {
		return new String[] {"AND", "OR", "XOR", "TEST"};
	}

	public ParseError validate(Parameters p) {
		ParseError e = p.numericDestOK();
		if (e != null) { return e; }
		e = p.numericSrcOK();
		if (e != null) { return e; }
		e = p.validate(2, Op.NULL);
		return e;
	}

	public void execute(Parameters p) {
		p.prepareAB();
		if (p.mnemo.equals("AND") || p.mnemo.equals("TEST")) {
			p.result = p.a & p.b;
		}
		if (p.mnemo.equals("OR")) {
			p.result = p.a | p.b;
		}
		if (p.mnemo.equals("XOR")) {
			p.result = p.a ^ p.b;
		}
		setFlags(p, SF + ZF + PF);
		dataspace.fOverflow = false;
		dataspace.fCarry = false;
		if (!p.mnemo.equals("TEST")) {
			p.put(0, p.result, null);
		}
	}

}
