package jasmin.commands;

import jasmin.core.*;

public class Cmpxchg extends JasminCommand {
	
	public String[] getID() {
		return new String[] { "CMPXCHG" };
	}
	
	public ParseError validate(Parameters p) {
		ParseError e = p.firstRegMemSecondReg();
		if (e != null) {
			return e;
		}
		return p.validate(2, Op.NULL);
	}
	
	public void execute(Parameters p) {
		p.prepareAB();
		p.c = p.b;
		p.b = p.a;
		Address A = dataspace.getMatchingRegister(dataspace.EAX, p.size);
		p.a = p.get(A);
		
		// compare
		p.b = 0 - p.b; // necessary for flags
		p.result = p.a + p.b;
		setFlags(p, ZF + CF + PF + AF + SF + OF);
		
		// exchange
		if (p.result == 0) {
			p.put(0, p.c, null);
		} else {
			p.put(A, p.b, null);
		}
	}
	
}
