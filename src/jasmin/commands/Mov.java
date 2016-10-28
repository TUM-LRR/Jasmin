package jasmin.commands;

import jasmin.core.*;

/**
 * @author Yang Guo
 */

public class Mov extends JasminCommand {
	
	public String[] getID() {
		return new String[] { "MOV", "CMOVA", "CMOVAE", "CMOVB", "CMOVBE", "CMOVC",
			"CMOVE", "CMOVG", "CMOVGE", "CMOVL", "CMOVLE", "CMOVNA", "CMOVNAE",
			"CMOVNB", "CMOVNBE", "CMOVNC", "CMOVNE", "CMOVNG", "CMOVNGE", "CMOVNL",
			"CMOVNLE", "CMOVNO", "CMOVNP", "CMOVNS", "CMOVNZ", "CMOVO",
			"CMOVP", "CMOVPE", "CMOVPO", "CMOVS", "CMOVZ" };
	}
	
	public ParseError validate(Parameters p) {
		ParseError e = p.numericDestOK();
		if (e != null) {
			return e;
		}
		e = p.numericSrcOK();
		if (e != null) {
			return e;
		}
		return null;
	}
	
	private boolean testCondition(String command) {
		return command.equals("MOV") || testCC(command.substring(4));
	}
	
	public void execute(Parameters p) {
		if (testCondition(p.mnemo)) {
			if (p.type(1) == Op.LABEL) {
				p.put(0, p.get(1), new MemCellInfo(p.argument(1)));
			} else {
				p.put(0, p.get(1), null);
			}
		}
	}
	
}
