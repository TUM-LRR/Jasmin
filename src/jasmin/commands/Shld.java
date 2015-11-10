package jasmin.commands;

import jasmin.core.*;

public class Shld extends JasminCommand {
	
	public String[] getID() {
		return new String[] { "SHLD", "SHRD" };
	}
	
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.M16 | Op.M32 | Op.R16 | Op.R32 | Op.MU);
		if (e != null) {
			return e;
		}
		e = p.validate(1, Op.R16 | Op.R32);
		if (e != null) {
			return e;
		}
		if ((Op.matches(p.type(2), Op.R8) && !(p.argument(2).equals(dataspace.CL)))
			|| !Op.matches(p.type(2), Op.R8 | Op.I8)) {
			return new ParseError(p.wholeLine, p.argument(2),
				"third register must be CL or an 8-bit immediate");
		}
		return ((p.size(0) & p.size(1)) != 0) ? null : new ParseError(p.wholeLine, p.argument(1),
			"Size mismatch");
	}
	
	public void execute(Parameters p) {
		if (((p.b = (p.get(2))) == 0) || (p.b >= (p.size(0) * 8))) {
			return;
		}
		p.a = p.get(0);
		
		if (p.mnemo.equals("SHLD")) {
			if (p.size(0) == 2) {
				int buffer = (int) (((p.a & 0xFFFFL) << 16) | (p.get(1) & 0xFFFFL));
				p.result = ((buffer << p.b) & 0xFFFF0000L) >> 16;
				dataspace.fCarry = getBit(buffer, 32 - p.b);
				setFlags(p, SF + ZF + PF);
				dataspace.fOverflow = ((p.b == 1) && (getBit(p.a, 15) != getBit(p.result, 15)));
			} else if (p.size(0) == 4) {
				long buffer = (((p.a & 0xFFFFFFFFL) << 32) | (p.get(1) & 0xFFFFFFFFL));
				p.result = ((buffer << p.b) & 0xFFFFFFFF00000000L) >> 32;
				dataspace.fCarry = getBit(buffer, 64 - p.b);
				setFlags(p, SF + ZF + PF);
				dataspace.fOverflow = ((p.b == 1) && (getBit(p.a, 31) != getBit(p.result, 31)));
			}
		}
		if (p.mnemo.equals("SHRD")) {
			if (p.size(0) == 2) {
				int buffer = (int) ((p.a & 0xFFFFL) | ((p.get(1) & 0xFFFFL) << 16));
				p.result = ((buffer >> p.b) & 0xFFFFL);
				dataspace.fCarry = getBit(buffer, p.b - 1);
				setFlags(p, SF + ZF + PF);
				dataspace.fOverflow = ((p.b == 1) && (getBit(p.a, 15) != getBit(p.result, 15)));
			} else if (p.size(0) == 4) {
				long buffer = (((p.a & 0xFFFFFFFFL) << 32) | (p.get(1) & 0xFFFFFFFFL));
				p.result = ((buffer >> p.b) & 0xFFFFFFFF00000000L) >> 32;
				dataspace.fOverflow = ((p.b == 1) && (getBit(p.a, 31) != getBit(p.result, 31)));
				dataspace.fCarry = getBit(buffer, p.b - 1);
				setFlags(p, SF + ZF + PF);
			}
		}
		p.put(0, p.result, null);
		
	}
	
}
