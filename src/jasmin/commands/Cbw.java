package jasmin.commands;

import jasmin.core.*;

/**
 * @author Jakob Kummerow
 */

public class Cbw extends JasminCommand {

	public String[] getID() {
		return new String[] {"CBW", "CWDE", "CWD", "CDQ"};
	}

	public ParseError validate(Parameters p) {
		return p.validate(0, Op.NULL);
	}
	
	public boolean signed() {
		return true;
	}

	public void execute(Parameters p) {
		if (p.mnemo.equals("CBW")) {
			p.put(dataspace.AX, p.get(dataspace.AL), null);
		} else if (p.mnemo.equals("CWDE")) {
			p.put(dataspace.EAX, p.get(dataspace.AX), null);
		} else if (p.mnemo.equals("CWD")) {
			p.a = p.get(dataspace.AX) >> 16;
			p.a &= 0xFFFF;
			p.put(dataspace.DX, p.a, null);
		} else if (p.mnemo.equals("CDQ")) {
			p.a = p.get(dataspace.EAX) >> 32;
			p.put(dataspace.EDX, p.a, null);
		}
	}

}
