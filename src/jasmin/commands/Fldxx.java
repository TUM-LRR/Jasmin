/**
 * 
 */
package jasmin.commands;

import jasmin.core.*;


/**
 * @author Jakob Kummerow
 *
 */
public class Fldxx extends FpuCommand {

	public String[] getID() {
		return new String[] {"FLD1", "FLDL2E", "FLDL2T", "FLDLG2", 
				"FLDLN2", "FLDPI", "FLDZ"};
	}

	public ParseError validate(Parameters p) {
		return p.validate(0, Op.NULL);
	}

	public void execute(Parameters p) {
		switch (p.mnemo) {
			case "FLD1":                // 1
				fpu.push(1);
				break;
			case "FLDL2E":        // base-2 log of e
				fpu.push(Math.log(Math.E) / Math.log(2));
				break;
			case "FLDL2T":        // base-2 log of 10
				fpu.push(Math.log(10) / Math.log(2));
				break;
			case "FLDLG2":        // base-10 log of 2
				fpu.push(Math.log10(2));
				break;
			case "FLDLN2":        // base-e log of 2
				fpu.push(Math.log(2));
				break;
			case "FLDPI":        // PI
				fpu.push(Math.PI);
				break;
			case "FLDZ":        // 0
				fpu.push(0);
				break;
		}

	}

}
