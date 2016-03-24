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
		switch (p.mnemo) {
			case "FABS":
				fpu.put(0, Math.abs(fpu.get(0)));

				break;
			case "FCHS":
				fpu.put(0, -fpu.get(0));
				break;
		}
	}

}
