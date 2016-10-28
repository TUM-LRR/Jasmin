package jasmin.commands;

import jasmin.core.*;

/**
 * @author Jakob Kummerow
 */

public class DB extends PseudoCommand {
	
	@Override
	public String[] getID() {
		return new String[] { "DB", "DW", "DD", "DQ" };
	}
	
	@Override
	public int defaultSize(String mnemo) {
		return getOperationSize(mnemo);
	}
	
	@Override
	public ParseError validate(Parameters p) {
		ParseError e;
		switch (p.mnemo) {
			case "DD":
				e = p.validateAll(Op.IMM | Op.CHARS | Op.STRING | Op.FLOAT | Op.NULL | Op.LABEL | Op.CONST);
				break;
			case "DQ":
				e = p.validateAll(Op.FLOAT | Op.NULL | Op.LABEL);
				break;
			default:
				e = p.validateAll(Op.IMM | Op.CHARS | Op.STRING | Op.NULL | Op.LABEL | Op.CONST);
				break;
		}
		if (e != null) {
			return e;
		}
		e = p.validateAllSizes(-1, getOperationSize(p.mnemo));
		return e;
	}
	
	@Override
	public void execute(Parameters p) {
		
		for (int i = 0; i < p.numArguments; i++) {
			Address address = dataspace.malloc(p.size, 1);
			if ((p.label != null) && (i == 0)) {
				dataspace.setVariableAddress(p.label, address.address);
			}
			if (p.type(i) == Op.LABEL) {
				dataspace.put(p.get(i), address,
					new MemCellInfo(Op.LABEL, p.arg(i), p.size));
			} else if ((p.type(i) == Op.STRING) || (p.size(i) > p.size)) {
				String[] parts = Op.splitLongString(p.arg(i), p.size);
				for (int j = 0; j < parts.length; j++) {
					if (j > 0) {
						address = dataspace.malloc(p.size, 1);
					}
					dataspace.put(Parser.getCharsAsNumber(parts[j]), address, null);
				}
			} else if (Op.matches(p.type(i), Op.IMM | Op.CHARS | Op.FLOAT | Op.CHARS | Op.STRING | Op.CONST)) {
				dataspace.put(p.get(i), address, null);
			}
		}
		
	}
	
}
