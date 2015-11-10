package jasmin.commands;

import jasmin.core.*;

public class Rcl extends JasminCommand {
	
	public String[] getID() {
		return new String[] { "RCL", "ROR", "ROL", "RCR" };
	}
	
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.REG | Op.M8 | Op.M16 | Op.M32);
		if (e != null) {
			return e;
		}
		if ((Op.matches(p.type(1), Op.R8) && !(p.argument(1).address == dataspace.CL))
			|| !Op.matches(p.type(1), Op.R8 | Op.I8)) {
			return new ParseError(p.wholeLine, p.argument(1),
				"second register must be CL or an 8-bit immediate");
		}
		return p.validate(2, Op.NULL);
	}
	
	public void execute(Parameters p) {
		if ((p.b = p.get(1)) == 0) {
			return;
		}
		long buffer = 0;
		int bitsize = p.size(0) * 8;
		int buffersize = bitsize; // incremented later
		
		long mask = 0xFFFFFFFFL; // explicitly converting to long is vital!
		mask = (mask >> (32 - bitsize));
		int shortmask = (0x7FFFFFFF >> (32 - bitsize));
		// prepare buffer
		if (p.mnemo.startsWith("RC")) {
			p.b = p.b % (bitsize + 1);
			buffersize += 1;
			buffer = (p.get(0) & mask)
				| ((dataspace.fCarry ? 1L : 0) << bitsize)
				| ((p.get(0) & shortmask) << (bitsize + 1));
		} else if (p.mnemo.startsWith("RO")) {
			p.b = p.b % bitsize;
			buffer = (p.get(0) & mask) | ((p.get(0) & mask) << bitsize);
		}
		// now rotate
		if (p.mnemo.endsWith("R")) {
			p.result = mask & (buffer >> p.b);
			if (p.b == 1) {
				dataspace.fOverflow = getBit(p.result, bitsize - 2) ^ getBit(p.result, bitsize - 1);
			}
			dataspace.fCarry = getBit(buffer, p.b - 1);
		} else {
			p.result = mask & (buffer >> (buffersize - p.b));
			dataspace.fCarry = getBit(buffer, (buffersize - p.b) % buffersize);
			if (p.b == 1) {
				dataspace.fOverflow = dataspace.fCarry ^ getBit(p.result, bitsize - 1);
			}
		}
		
		p.put(0, p.result, null);
	}
}
