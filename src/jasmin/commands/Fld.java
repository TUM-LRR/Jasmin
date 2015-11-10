/**
 * 
 */
package jasmin.commands;

import jasmin.core.*;

/**
 * @author Jakob Kummerow
 */
public class Fld extends FpuCommand {
	
	public String[] getID() {
		return new String[] { "FLD", "FST", "FSTP" };
	}
	
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.M32 | Op.M64 | Op.FPUREG);
		if (e != null) {
			return e;
		}
		e = p.validate(1, Op.NULL);
		return e;
	}
	
	public void execute(Parameters p) {
		if (p.mnemo.equals("FLD")) {
			double d = p.getF(0, Fpu.FLOAT);
			fpu.push(d);
			return;
		}
		p.putF(0, fpu.get(0), Fpu.FLOAT);
		if (p.mnemo.equals("FSTP")) {
			fpu.pop();
		}
	}
	
}
