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
		switch (p.mnemo) {
			case "FADD":
				p.fa += p.fb;
				break;
			case "FSUB":
				p.fa -= p.fb;
				break;
			case "FSUBR":
				p.fa = p.fb - p.fa;
				break;
			case "FMUL":
				p.fa *= p.fb;
				break;
			case "FDIV":
				p.fa /= p.fb;
				break;
			case "FDIVR":
				p.fa = p.fb / p.fa;
				break;
		}
		
		p.putF(0, p.fa, Fpu.FLOAT);
	}
	
}
