package jasmin.commands;

import jasmin.core.*;

public class Inc extends JasminCommand {
	
	public String[] getID() {
		return new String[] { "INC", "DEC", "NEG", "NOT" };
	}
	
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.REG | Op.M8 | Op.M16 | Op.M32);
		if (e != null) {
			return e;
		}
		return p.validate(1, Op.NULL);
	}
	
	public void execute(Parameters p) {
		p.a = p.get(0);

		switch (p.mnemo) {
			case "INC":
				p.b = 1;
				p.result = p.a + p.b;
				break;
			case "DEC":
				p.b = -1;
				p.result = p.a + p.b;
				break;
			case "NEG":
				p.b = -p.a;
				p.a = 0;
				p.result = p.a + p.b;
				break;
			case "NOT":
				p.result = ~p.a;
				break;
		}
		
		if (!p.mnemo.equals("NOT")) {
			setFlags(p, OF + SF + ZF + AF + PF);
		}
		
		if (p.mnemo.equals("NEG")) {
			dataspace.fCarry = p.result != 0;
		}
		p.put(0, p.result, null);
	}
	
}
