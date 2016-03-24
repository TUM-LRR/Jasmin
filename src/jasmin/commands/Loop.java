package jasmin.commands;

import jasmin.core.*;

public class Loop extends JasminCommand {
	
	@Override
	public String[] getID() {
		return new String[] { "LOOP", "LOOPE", "LOOPNE", "LOOPNZ", "LOOPZ" };
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
	
	public boolean testCondition(String command) {
		return command.equals("LOOP") || testCC(command.substring(4));
	}
	
	@Override
	public void execute(Parameters p) {
		p.a = p.get(0);
		long ecx = dataspace.ECX.getShortcut() - 1;
		p.put(dataspace.ECX, ecx, null);
		if ((ecx != 0) && testCondition(p.mnemo)) {
			dataspace.setInstructionPointer((int) p.a);
		}
	}
	
}
