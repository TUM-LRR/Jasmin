package jasmin.commands;

import jasmin.core.*;

/**
 * @author Yang Guo
 */

public class Mul extends jasmin.core.JasminCommand {
	
	public String[] getID() {
		return new String[] { "MUL" };
	}
	
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.MEM | Op.REG);
		if (e != null) {
			return e;
		}
		return p.validate(1, Op.NULL);
	}
	
	public void execute(Parameters p) {
		p.a = p.get(0);
		if (p.size == 1) {
			long al = dataspace.AL.getShortcut();
			al *= p.a;
			p.put(dataspace.AX, al, null);
			p.result = (al >> 8) & 0xFFL;
		} else if (p.size == 2) {
			long ax = dataspace.AX.getShortcut();
			ax *= p.a;
			p.put(dataspace.AX, ax & 0xFFFFL, null);
			p.result = ((ax >> 16) & 0xFFFFL);
			p.put(dataspace.DX, p.result, null);
		} else if (p.size == 4) {
			long eax = dataspace.EAX.getShortcut();
			eax *= p.a;
			p.put(dataspace.EAX, eax & 0xFFFFFFFFL, null);
			p.result = ((eax >> 32) & 0xFFFFFFFFL);
			p.put(dataspace.EDX, p.result, null);
		}
		dataspace.fOverflow = dataspace.fCarry = (p.result != 0);
		
	}
	
}
