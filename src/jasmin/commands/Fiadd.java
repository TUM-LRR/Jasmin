package jasmin.commands;

import jasmin.core.*;

/**
 * @author Jakob Kummerow
 */

public class Fiadd extends FpuCommand {

	public String[] getID() {
		return new String[]{"FIADD", "FISUB", "FISUBR", "FIMUL", "FIDIV", "FIDIVR"};
	}
	
	public int defaultSize = Fpu.defaultOperandSize;

	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.M16 | Op.M32);
		if (e != null) { return e; }
		e = p.validate(1, Op.NULL);
		return e;
	}

	public void execute(Parameters p) {
		p.normalizeParameters();
		p.fa = p.getF(0, Fpu.INTEGER);
		p.fb = p.getF(1, Fpu.INTEGER);
		if (p.mnemo.equals("FIADD")) {
			p.fa += p.fb;
			
		} else if (p.mnemo.equals("FISUB")) {
			p.fa -= p.fb;
			
		} else if (p.mnemo.equals("FISUBR")) {
			p.fa = p.fb - p.fa;
			
		} else if (p.mnemo.equals("FIMUL")) {
			p.fa *= p.fb;
			
		} else if (p.mnemo.equals("FIDIV")) {
			p.fa /= p.fb;
			
		} else if (p.mnemo.equals("FIDIVR")) {
			p.fa = p.fb / p.fa;
		}
		fpu.put(0, p.fa);
	}

}
