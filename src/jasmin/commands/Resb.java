package jasmin.commands;

import jasmin.core.*;

/**
 * @author Yang Guo
 */

public class Resb extends PseudoCommand {
	
	@Override
	public String[] getID() {
		return new String[] { "RESB", "RESW", "RESD", "RESQ" };
	}
	
	@Override
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.IMM | Op.CONST);
		if (e != null) {
			return e;
		}
		long howmany = p.get(0) * getOperationSize(p.mnemo);
		if ((howmany < 1) || (howmany > dataspace.getMEMSIZE())) {
			return new ParseError(p.wholeLine, p.argument(0),
				"invalid reservation size");
		}
		return p.validate(1, Op.NULL);
	}
	
	@Override
	public void execute(Parameters p) {
		int size = getOperationSize(p.mnemo);
		Address a = dataspace.malloc(size, (int) p.get(0));
		if (p.label != null) {
			dataspace.setVariableAddress(p.label, a.address);
		}
		a.size = size * (int) p.get(0);
		dataspace.setDirty(a);
		
	}
	
	@Override
	public boolean signed() {
		return true;
	}
	
}
