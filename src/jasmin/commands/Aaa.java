package jasmin.commands;

import jasmin.core.*;

/**
 * @author Yang Guo
 */

public class Aaa extends JasminCommand {
	
	public String[] getID() {
		return new String[] { "AAA", "AAS", "AAD", "AAM", "DAA", "DAS" };
	}
	
	public ParseError validate(Parameters p) {
		return p.validate(0, Op.NULL);
	}
	
	public void execute(Parameters p) {
		long al = p.get(dataspace.AL);
		switch (p.mnemo) {
			case "AAA":
				al = 0xF & al;
				dataspace.fCarry = dataspace.fAuxiliary = ((al > 9) || dataspace.fAuxiliary);
				if (dataspace.fCarry) {
					p.put(dataspace.AL, 0xF & (al + 6), null);
					p.put(dataspace.AH, p.get(dataspace.AH) + 1, null);
				} else {
					p.put(dataspace.AL, al, null);
				}
				break;
			case "AAS":
				al = 0xF & al;
				dataspace.fCarry = dataspace.fAuxiliary = ((al > 9) || dataspace.fAuxiliary);
				if (dataspace.fCarry) {
					p.put(dataspace.AL, 0xF & (al - 6), null);
					p.put(dataspace.AH, p.get(dataspace.AH) - 1, null);

				} else {
					p.put(dataspace.AL, al, null);
				}
				break;
			case "DAA": {
				boolean _fCarry = dataspace.fCarry;
				dataspace.fAuxiliary = (dataspace.fAuxiliary || ((al & 0xF) > 9));
				if (dataspace.fAuxiliary) {
					p.put(dataspace.AL, al + 6, null);
					dataspace.fCarry |= ((((al + 6) >> 8) & 1) == 1);
				}
				dataspace.fCarry = ((al > 0x99) || _fCarry);
				if (dataspace.fCarry) {
					p.put(dataspace.AL, p.get(dataspace.AL) + 0x60, null);
				}
				break;
			}
			case "DAS": {
				boolean _fCarry = dataspace.fCarry;
				dataspace.fAuxiliary = (dataspace.fAuxiliary || ((al & 0xF) > 9));
				if (dataspace.fAuxiliary) {
					p.put(dataspace.AL, al - 6, null);
					dataspace.fCarry |= ((((al - 6) >> 8) & 1) == 1);
				}
				dataspace.fCarry = ((al > 0x99) || _fCarry);
				if (dataspace.fCarry) {
					p.put(dataspace.AL, p.get(dataspace.AL) - 0x60, null);
				}
				break;
			}
			case "AAM":
				p.put(dataspace.AH, al / 10, null);
				p.put(dataspace.AL, p.result = (al % 10), null);
				setFlags(p, PF & SF & ZF);
				break;
			default:
				p.put(dataspace.AL, p.result = (p.get(dataspace.AL) + 10 * p.get(dataspace.AH)), null);
				p.put(dataspace.AH, 0, null);
				setFlags(p, PF & SF & ZF);
				break;
		}
	}
	
}
