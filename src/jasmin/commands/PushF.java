package jasmin.commands;

import jasmin.core.*;

/**
 * @author Yang Guo
 */

public class PushF extends jasmin.core.JasminCommand {
	
	@Override
	public String[] getID() {
		return new String[] { "PUSHF", "PUSHFD", "LAHF" };
	}
	
	@Override
	public ParseError validate(Parameters p) {
		return p.validate(0, Op.NULL);
	}
	
	@Override
	public void execute(Parameters p) {
		long word = 2;
		int size = 2;
		if (p.mnemo.endsWith("D")) {
			size = 4;
		}
		word = setBit(word, dataspace.fCarry, 0);
		word = setBit(word, dataspace.fParity, 2);
		word = setBit(word, dataspace.fAuxiliary, 4);
		word = setBit(word, dataspace.fZero, 6);
		word = setBit(word, dataspace.fSign, 7);
		word = setBit(word, dataspace.fTrap, 8);
		word = setBit(word, dataspace.fDirection, 10);
		word = setBit(word, dataspace.fOverflow, 11);
		
		if (p.mnemo.startsWith("L")) {
			p.put(dataspace.AH, (word & 0xFFL), null);
		} else {
			p.push(new Address(Op.IMM, size, word));
		}
	}
	
}
