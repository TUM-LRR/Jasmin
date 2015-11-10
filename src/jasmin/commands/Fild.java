package jasmin.commands;

import jasmin.core.*;

/**
 * @author Jakob Kummerow
 */

public class Fild extends FpuCommand {
	
	public String[] getID() {
		return new String[] { "FILD", "FIST", "FISTP" };
	}
	
	public int defaultSize = Fpu.defaultOperandSize;
	
	public ParseError validate(Parameters p) {
		ParseError e = null;
		if (p.mnemo.equals("FILD") || p.mnemo.equals("FISTP")) {
			e = p.validate(0, Op.M16 | Op.M32 | Op.M64);
		} else if (p.mnemo.equals("FIST")) {
			p.defaultSize = 4;
			e = p.validate(0, Op.M16 | Op.M32);
		}
		
		if (e != null) {
			return e;
		}
		e = p.validate(1, Op.NULL);
		return e;
	}
	
	public void execute(Parameters p) {
		if (p.mnemo.equals("FILD")) {
			double d = p.getF(0, Fpu.INTEGER);
			fpu.push(d);
			return;
		}
		Double d = 0.0;
		if (p.mnemo.equals("FIST")) {
			d = fpu.get(0);
		}
		if (p.mnemo.equals("FISTP")) {
			d = fpu.pop();
		}
		p.putF(0, d, Fpu.INTEGER);
	}
	
}
