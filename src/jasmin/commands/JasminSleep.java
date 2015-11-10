/**
 * This is the SLEEP command. It is NOT part of an x86's instruction set, but 
 * is useful to have in the simulator.
 */
package jasmin.commands;

import jasmin.core.*;

/**
 * @author Jakob Kummerow
 */
public class JasminSleep extends JasminCommand {
	
	@Override
	public String[] getID() {
		return new String[] { "JASMINSLEEP" };
	}
	
	@Override
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.IMM | Op.REG | Op.MEM | Op.VARIABLE | Op.CONST);
		if (e != null) {
			return e;
		}
		return p.validate(1, Op.NULL);
	}
	
	@Override
	public void execute(Parameters p) {
		try {
			Thread.sleep(p.get(0));
		} catch (InterruptedException e) {
			// NIGHTMARE NIGHTMARE NIGHTMARE!!!
			// no, probably just the user stopping/pausing program execution
		}
		
	}
	
}
