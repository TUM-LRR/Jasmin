package jasmin.commands;

import jasmin.core.*;

/**
 * @author Yang Guo
 */

public class Bt extends JasminCommand {
	
	/**
	 * @param mnemo
	 *        the mnemo for the command whose default size is requested
	 */
	@Override
	public int defaultSize(String mnemo) {
		return 2;
	}
	
	@Override
	public String[] getID() {
		return new String[] { "BT", "BTC", "BTR", "BTS" };
	}
	
	@Override
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.R16 | Op.R32 | Op.M16 | Op.M32 | Op.MU);
		if (e != null) {
			return e;
		}
		e = p.validate(1, Op.R16 | Op.R32 | Op.I8);
		if (e != null) {
			return e;
		}
		e = p.consistentSizes();
		if (e != null) {
			return e;
		}
		return p.validate(2, Op.NULL);
	}
	
	@Override
	public void execute(Parameters p) {
		long offset = p.get(1);
		Address a = p.argument(0).address.clone();
		if (Op.matches(p.type(0), Op.REG)) {
			offset = offset % (p.size(0) * 8);
		}
		if (Op.matches(p.type(0), Op.MEM)) {
			FullArgument arg = p.argument(0);
			if (arg.cAddress != null) {
				a.address = arg.cAddress.calculateEffectiveAddress(true);
			}
			a.address += offset / 8;
			offset = offset % 8;
		}
		dataspace.fCarry = getBit(p.get(a), offset);
		if (p.mnemo.endsWith("C")) {
			p.put(a, setBit(p.get(a), !dataspace.fCarry, offset), null);
		} else if (p.mnemo.endsWith("S")) {
			p.put(a, setBit(p.get(a), true, offset), null);
		} else if (p.mnemo.endsWith("R")) {
			p.put(a, setBit(p.get(a), false, offset), null);
		}
	}
	
}
