package jasmin.commands;

import jasmin.core.*;

/**
 * @author Yang Guo
 */

public class PopAD extends JasminCommand {

	public String[] getID() {
		return new String[]{"POPAD"};
	}

	public ParseError validate(Parameters p) {
		return p.validate(0, Op.NULL);
	}
	
	public void execute(Parameters p) {
		p.pop(dataspace.EDI);
		p.pop(dataspace.ESI);
		p.pop(dataspace.EBP);
		p.pop(dataspace.EBX); // is overwritten in the next step
		p.pop(dataspace.EBX);
		p.pop(dataspace.EDX);
		p.pop(dataspace.ECX);
		p.pop(dataspace.EAX);
	}
		
}
