package jasmin.commands;

import jasmin.core.*;

/**
 * @author Jakob Kummerow
 */

public class Add extends JasminCommand {
	
	public String[] getID() {
		return new String[] { "ADD", "ADC", "SUB", "SBB", "CMP" };
	}
	
	public ParseError validate(Parameters p) {
		ParseError e = p.numericDestOK();
		if (e != null) {
			return e;
		}
		e = p.numericSrcOK();
		if (e != null) {
			return e;
		}
		e = p.validate(2, Op.NULL);
		return e;
	}
	
	public void execute(Parameters p) {
		p.prepareAB();

		switch (p.mnemo) {
			case "ADD":
				p.result = p.a + p.b;
				break;
			case "ADC":
				p.result = p.a + p.b + (dataspace.fCarry ? 1 : 0);
				break;
			case "SUB":
			case "CMP":
				p.b = 0 - p.b; // this is necessary for setFlags() to work correctly

				p.result = p.a + p.b;
				break;
			case "SBB":
				p.b = 0 - p.b;
				p.result = p.a + p.b - (dataspace.fCarry ? 1 : 0);
				break;
		}
		
		setFlags(p, OF + SF + ZF + AF + CF + PF);
		if (!p.mnemo.equals("CMP")) {
			p.put(0, p.result, null);
		}
	}
	
}