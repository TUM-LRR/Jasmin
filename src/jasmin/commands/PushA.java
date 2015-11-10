package jasmin.commands;

import jasmin.core.*;

/**
 * @author Yang Guo
 */

public class PushA extends JasminCommand {
	
	public String[] getID() {
		return new String[] { "PUSHA" };
	}
	
	public ParseError validate(Parameters p) {
		return p.validate(0, Op.NULL);
	}
	
	public void execute(Parameters p) {
		long esp = dataspace.ESP.getShortcut();
		p.push(dataspace.AX);
		p.push(dataspace.CX);
		p.push(dataspace.DX);
		p.push(dataspace.BX);
		p.push(new Address(Op.IMM, 2, esp));
		p.push(dataspace.BP);
		p.push(dataspace.SI);
		p.push(dataspace.DI);
	}
	
}
