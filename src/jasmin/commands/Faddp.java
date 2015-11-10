package jasmin.commands;

import jasmin.core.*;

/**
 * @author Jakob Kummerow
 */

public class Faddp extends FpuCommand {
	
	public String[] getID() {
		return new String[] { "FADDP", "FSUBP", "FSUBRP", "FMULP", "FDIVP", "FDIVRP" };
	}
	
	public int defaultSize = Fpu.defaultOperandSize;
	
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.FPUREG | Op.NULL);
		if (e != null) {
			return e;
		}
		e = p.validate(1, Op.FPUST0 | Op.NULL);
		return e;
	}
	
	public void execute(Parameters p) {
		p.normalizePopParameters();
		p.fa = p.getF(0, Fpu.FLOAT);
		p.fb = p.getF(1, Fpu.FLOAT);
		if (p.mnemo.equals("FADDP")) {
			p.fa += p.fb;
			
		} else if (p.mnemo.equals("FSUBP")) {
			p.fa -= p.fb;
			
		} else if (p.mnemo.equals("FSUBRP")) {
			p.fa = p.fb - p.fa;
			
		} else if (p.mnemo.equals("FMULP")) {
			p.fa *= p.fb;
			
		} else if (p.mnemo.equals("FDIVP")) {
			p.fa /= p.fb;
			
		} else if (p.mnemo.equals("FDIVRP")) {
			p.fa = p.fb / p.fa;
		}
		p.putF(0, p.fa, Fpu.FLOAT);
		fpu.pop();
	}
	
}
