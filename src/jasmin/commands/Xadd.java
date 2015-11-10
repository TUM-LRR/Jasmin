package jasmin.commands;

import jasmin.core.*;

public class Xadd extends JasminCommand {
	
	public String[] getID() {
		return new String[] { "XADD" };
	}
	
	public ParseError validate(Parameters p) {
		ParseError e = p.consistentSizes();
		if (e != null) {
			return e;
		}
		e = p.validate(0, Op.REG | Op.MEM);
		if (e != null) {
			return e;
		}
		return p.validate(1, Op.REG);
	}
	
	public void execute(Parameters p) {
		p.prepareAB();
		p.result = p.a + p.b;
		setFlags(p, OF + SF + ZF + AF + CF + PF);
		p.put(1, p.get(0), null);
		p.put(0, p.result, null);
	}
	
}
