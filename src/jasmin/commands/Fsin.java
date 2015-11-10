/**
 * 
 */
package jasmin.commands;

import jasmin.core.*;


/**
 * @author Jakob Kummerow
 *
 */
public class Fsin extends FpuCommand {

	public String[] getID() {
		return new String[] {"FSIN", "FCOS", "FSINCOS", "FSQRT"};
	}

	public ParseError validate(Parameters p) {
		return p.validate(0, Op.NULL);
	}

	public void execute(Parameters p) {
		
		if (p.mnemo.equals("FSIN")) {
			fpu.put(0, Math.sin(fpu.get(0)));
			
		} else if (p.mnemo.equals("FCOS")) {
			fpu.put(0, Math.cos(fpu.get(0)));
			
		} else if (p.mnemo.equals("FSINCOS")) {
			double a = fpu.get(0);
			fpu.put(0, Math.sin(a));
			fpu.push(Math.cos(a));
			
		} else if (p.mnemo.equals("FSQRT")) {
			fpu.put(0, Math.sqrt(fpu.get(0)));
		}

	}

}