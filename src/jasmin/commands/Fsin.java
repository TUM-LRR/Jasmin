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
		switch (p.mnemo) {
			case "FSIN":
				fpu.put(0, Math.sin(fpu.get(0)));
				break;
			case "FCOS":
				fpu.put(0, Math.cos(fpu.get(0)));
				break;
			case "FSINCOS":
				double a = fpu.get(0);
				fpu.put(0, Math.sin(a));
				fpu.push(Math.cos(a));
				break;
			case "FSQRT":
				fpu.put(0, Math.sqrt(fpu.get(0)));
				break;
		}

	}

}