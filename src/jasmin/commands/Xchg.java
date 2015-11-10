package jasmin.commands;

import jasmin.core.*;

public class Xchg extends JasminCommand {
	
	public String[] getID() {
		return new String[] { "XCHG" };
	}
	
	public ParseError validate(Parameters p) {
		ParseError e = p.consistentSizes();
		if (e != null) {
			return e;
		}
		if (p.validate(0, Op.MEM) == null) {
			return p.validate(1, Op.REG);
		}
		e = p.validate(0, Op.REG);
		if (e != null) {
			return e;
		}
		return p.validate(1, Op.MEM | Op.REG);
	}
	
	public void execute(Parameters p) {
		long tmp = p.get(1);
		p.put(1, p.get(0), null);
		p.put(0, tmp, null);
	}
	
}
