package jasmin.commands;

import jasmin.core.*;
import jasmin.gui.JasDocument;

public class Shr extends JasminCommand {
	
	@Override
	public String[] getID() {
		return new String[] { "SHR", "SHL", "SAR", "SAL" };
	}
	
	@Override
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.M8 | Op.M16 | Op.M32 | Op.R8 | Op.R16 | Op.R32 | Op.MU);
		if (e != null) {
			return e;
		}
		if ((Op.matches(p.type(1), Op.R8) && (p.argument(1).address != dataspace.CL))
			|| !Op.matches(p.type(1), Op.R8 | Op.I8)) {
			return new ParseError(p.wholeLine, p.argument(1),
				"second argument must be CL or an 8-bit immediate");
		}
		return p.validate(2, Op.NULL);
	}
	
	@Override
	public void execute(JasDocument jasDocument, Parameters p) {
		p.a = p.get(0);
		p.b = p.get(1);

		if (p.b <= 0) {
			return;
		}

		if (p.mnemo.endsWith("L")) {
			p.result = p.a << p.b;
			p.put(0, p.result, null);
			setFlags(p, CF + SF + ZF + PF);
			if (p.b == 1) {
				if (dataspace.fCarry == getBit(p.result, p.size(0) * 8 - 1)) {
					dataspace.fOverflow = false;
				} else {
					dataspace.fOverflow = true;
				}
			}
		} else {
			if (p.mnemo.equals("SHR")) {
				if (p.b == 1) {
					dataspace.fOverflow = getBit(p.a, p.size(0) * 8 - 1);
				}
			} else { // SAR
				p.signed = true;
				p.a = p.get(0);

				if (p.b == 1) {
					dataspace.fOverflow = false;
				}
			}

			p.result = p.a >> p.b;
			p.put(0, p.result, null);
			setFlags(p, SF + ZF + PF);

			if (p.b < p.size(0) * 4) {
				dataspace.fCarry = getBit(p.a, p.b - 1);
			} else {
				dataspace.fCarry = p.a < 0;
			}
		}
	}
	
}
