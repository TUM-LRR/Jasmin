package jasmin.commands;

import jasmin.core.*;

/**
 * @author Yang Guo
 */

public class Jmp extends JasminCommand {
	
	@Override
	public String[] getID() {
		return new String[] { "JMP", "JCXZ", "JECXZ", "JA", "JAE", "JB", "JBE", "JC",
			"JE", "JG", "JGE", "JL", "JLE", "JNA", "JNAE",
			"JNB", "JNBE", "JNC", "JNE", "JNG", "JNGE", "JNL",
			"JNLE", "JNO", "JNP", "JNS", "JNZ", "JO",
			"JP", "JPE", "JPO", "JS", "JZ" };
	}
	
	@Override
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.LABEL | Op.MEM | Op.REG | Op.IMM | Op.CONST);
		if (e != null) {
			return e;
		}
		e = p.validate(1, Op.NULL);
		return e;
	}
	
	private boolean testCondition(String command) {
		return command.equals("JMP") || testCC(command.substring(1));
	}
	
	@Override
	public void execute(Parameters p) {
		p.a = p.get(0);
		if (testCondition(p.mnemo)) {
			dataspace.setInstructionPointer((int) p.a);
		}
	}
	
}
