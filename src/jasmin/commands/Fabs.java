/**
 * 
 */
package jasmin.commands;

import jasmin.core.*;


/**
 * @author Jakob Kummerow
 *
 */
public class Fabs extends FpuCommand {

	public String[] getID() {
		return new String[] {"FABS", "FCHS"};
	}

	public ParseError validate(Parameters p) {
		return p.validate(0, Op.NULL);
	}

	public void execute(Parameters p) {
		if (p.mnemo.equals("FABS")) {
			fpu.put(0, Math.abs(fpu.get(0)));
			
		} else if (p.mnemo.equals("FCHS")) {
			fpu.put(0, -fpu.get(0));
		} 
	}

}
