package jasmin.commands;

import jasmin.core.*;

/**
 * @author Jakob Kummerow
 */

public class Fadd extends FpuCommand {
	
	public String[] getID() {
		return new String[] { "FADD", "FSUB", "FSUBR", "FMUL", "FDIV", "FDIVR" };
	}
	
	public int defaultSize = Fpu.defaultOperandSize;
	
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.M32 | Op.M64 | Op.FPUREG | Op.FPUQUALI | Op.VARIABLE);
		if (e != null) {
			return e;
		}
		if (Op.matches(p.type(0), Op.FPUREG)) {
			e = p.validate(1, Op.FPUREG | Op.NULL);
			if (e != null) {
				return e;
			}
			if (Op.matches(p.type(1), Op.FPUREG)) {
				e = p.st0contained();
				if (e != null) {
					return e;
				}
			}
			return p.validate(2, Op.NULL);
		} else if (Op.matches(p.type(0), Op.FPUQUALI)) {
			e = p.validate(1, Op.FPUREG);
			if (e != null) {
				return e;
			}
			return p.validate(2, Op.NULL);
		} else {
			e = p.validate(1, Op.NULL);
			if (e != null) {
				return e;
			}
		}
		return null;
	}
	
	public void execute(Parameters p) {
		p.normalizeParameters();
		p.fa = p.getF(0, Fpu.FLOAT);
		p.fb = p.getF(1, Fpu.FLOAT);
		if (p.mnemo.equals("FADD")) {
			p.fa += p.fb;
			
		} else if (p.mnemo.equals("FSUB")) {
			p.fa -= p.fb;
			
		} else if (p.mnemo.equals("FSUBR")) {
			p.fa = p.fb - p.fa;
			
		} else if (p.mnemo.equals("FMUL")) {
			p.fa *= p.fb;
			
		} else if (p.mnemo.equals("FDIV")) {
			p.fa /= p.fb;
			
		} else if (p.mnemo.equals("FDIVR")) {
			p.fa = p.fb / p.fa;
		}
		
		p.putF(0, p.fa, Fpu.FLOAT);
	}
	
}
