package jasmin.commands;

import jasmin.core.*;

/**
 * @author Yang Guo
 */

public class Bsf extends JasminCommand {
	
	public String[] getID() {
		return new String[] { "BSF", "BSR" };
	}
	
	public ParseError validate(Parameters p) {
		ParseError e = p.consistentSizes();
		if (e != null) {
			return e;
		}
		e = p.validate(0, Op.R16 | Op.R32);
		if (e != null) {
			return e;
		}
		e = p.validate(1, Op.R16 | Op.R32 | Op.M16 | Op.M32 | Op.MU);
		if (e != null) {
			return e;
		}
		return p.validate(2, Op.NULL);
	}
	
	public void execute(Parameters p) {
		long word = p.get(1);
		dataspace.fZero = (word == 0);
		if (dataspace.fZero) {
			return;
		}
		if (p.mnemo.endsWith("F")) {
			for (int i = 0; i < p.size * 8; i++) {
				if (getBit(word, i)) {
					p.put(0, i, null);
					return;
				}
			}
		} else if (p.mnemo.endsWith("R")) {
			for (int i = p.size * 8 - 1; i >= 0; i--) {
				if (getBit(word, i)) {
					p.put(0, i, null);
					return;
				}
			}
		}
	}
	
}
