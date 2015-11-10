package jasmin.commands;

import jasmin.core.*;

public class Cmpxchg8b extends JasminCommand {
	
	public String[] getID() {
		return new String[] { "CMPXCHG8B" };
	}
	
	/**
	 * @param mnemo
	 *        the mnemo for the command whose default size is requested
	 */
	public int defaultSize(String mnemo) {
		return 8;
	}
	
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.M64 | Op.MU);
		if (e != null) {
			return e;
		}
		return p.validate(1, Op.NULL);
	}
	
	public void execute(Parameters p) {
		p.argument(0).address.size = 8;
		p.b = p.get(0);
		if (p.b == ((dataspace.EDX.getShortcut() << 32) | dataspace.EAX.getShortcut())) {
			dataspace.fZero = true;
			p.put(0, (dataspace.ECX.getShortcut() << 32) | dataspace.EBX.getShortcut(), null);
		} else {
			dataspace.fZero = false;
			p.put(dataspace.EAX, 0xFFFFFFFF & p.b, null);
			p.put(dataspace.EDX, p.b >> 32, null);
		}
	}
	
}
