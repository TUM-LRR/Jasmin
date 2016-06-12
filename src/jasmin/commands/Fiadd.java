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
		switch (p.mnemo) {
			case "FIADD":
				p.fa += p.fb;
				break;
			case "FISUB":
				p.fa -= p.fb;
				break;
			case "FISUBR":
				p.fa = p.fb - p.fa;
				break;
			case "FIMUL":
				p.fa *= p.fb;
				break;
			case "FIDIV":
				p.fa /= p.fb;
				break;
			case "FIDIVR":
				p.fa = p.fb / p.fa;
				break;
		}
		fpu.put(0, p.fa);
	}

}
