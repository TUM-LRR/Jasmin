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
		
		if (p.mnemo.equals("FLD1")) {				// 1
			fpu.push(1);
			
		} else if (p.mnemo.equals("FLDL2E")) { 		// base-2 log of e
			fpu.push(Math.log(Math.E)/Math.log(2));
			
		} else if (p.mnemo.equals("FLDL2T")) {		// base-2 log of 10
			fpu.push(Math.log(10)/Math.log(2));
			
		} else if (p.mnemo.equals("FLDLG2")) {		// base-10 log of 2
			fpu.push(Math.log10(2));
			
		} else if (p.mnemo.equals("FLDLN2")) {		// base-e log of 2
			fpu.push(Math.log(2));
			
		} else if (p.mnemo.equals("FLDPI")) {		// PI
			fpu.push(Math.PI);
			
		} else if (p.mnemo.equals("FLDZ")) {		// 0
			fpu.push(0);
		}

	}

}
