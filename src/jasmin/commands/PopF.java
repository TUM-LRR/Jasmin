package jasmin.commands;

import jasmin.core.*;

/**
 * @author Yang Guo
 */

public class PopF extends jasmin.core.JasminCommand {
	
	public String[] getID() {
		return new String[] { "POPF", "POPFD", "SAHF" };
	}
	
	public ParseError validate(Parameters p) {
		return p.validate(0, Op.NULL);
	}
	
	public void execute(Parameters p) {
		int size = 2;
		if (p.mnemo.endsWith("D")) {
			size = 4;
		}
		
		long word;
		if (p.mnemo.startsWith("S")) {
			word = (int) p.get(dataspace.AH);
		} else {
			long esp = p.get(dataspace.ESP);
			long ebp = p.get(dataspace.EBP);
			if (esp + size > ebp) {
				return;
			}
			word = (int) p.pop(size);
		}
		
		dataspace.fCarry = getBit(word, 0);
		dataspace.fParity = getBit(word, 2);
		dataspace.fAuxiliary = getBit(word, 4);
		dataspace.fZero = getBit(word, 6);
		dataspace.fSign = getBit(word, 7);
		if (p.mnemo.startsWith("P")) {
			dataspace.fTrap = getBit(word, 8);
			dataspace.fDirection = getBit(word, 10);
			dataspace.fOverflow = getBit(word, 11);
		}
	}
	
}
