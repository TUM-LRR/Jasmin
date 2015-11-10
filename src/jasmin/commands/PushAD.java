package jasmin.commands;

import jasmin.core.*;

/**
 * @author Yang Guo
 */

public class PushAD extends JasminCommand {
	
	public String[] getID() {
		return new String[] { "PUSHAD" };
	}
	
	public ParseError validate(Parameters p) {
		return p.validate(0, Op.NULL);
	}
	
	public void execute(Parameters p) {
		long esp = dataspace.ESP.getShortcut();
		p.push(dataspace.EAX);
		p.push(dataspace.ECX);
		p.push(dataspace.EDX);
		p.push(dataspace.EBX);
		p.push(new Address(Op.IMM, 4, esp));
		p.push(dataspace.EBP);
		p.push(dataspace.ESI);
		p.push(dataspace.EDI);
	}
	
}
