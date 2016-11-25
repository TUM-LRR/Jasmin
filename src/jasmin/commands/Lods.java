package jasmin.commands;

import jasmin.core.*;

/**
 * @author Yang Guo, Jakob Kummerow
 */

public class Lods extends JasminCommand {
	
	@Override
	public String[] getID() {
		return new String[] { "LODS", "LODSB", "LODSW", "LODSD",
			"STOS", "STOSB", "STOSW", "STOSD",
			"SCAS", "SCASB", "SCASW", "SCASD",
			"MOVS", "MOVSB", "MOVSW", "MOVSD",
			"CMPS", "CMPSB", "CMPSW", "CMPSD" };
	}
	
	@Override
	public boolean overrideMaxMemAccess(String mnemo) {
		return mnemo.startsWith("MOVS") || mnemo.startsWith("CMPS");
	}
	
	@Override
	public ParseError validate(Parameters p) {
		ParseError e;
		if (p.mnemo.length() == 4) {
			e = p.validate(0, Op.M8 | Op.M16 | Op.M32 | Op.PREFIX);
		} else {
			e = p.validate(0, Op.PREFIX | Op.NULL);
		}
		if (e != null) {
			return e;
		}
		int position = 0;
		if (p.type(0) == Op.PREFIX) {
			if (p.mnemo.startsWith("MOVS") || p.mnemo.startsWith("LODS") || p.mnemo.startsWith("STOS")) {
				if (!p.arg(0).equals("REP")) {
					return new ParseError(p.wholeLine, p.argument(0), "Only the REP prefix is allowed here");
				}
			} else if (p.mnemo.startsWith("CMPS") || p.mnemo.startsWith("SCAS")) {
				if (p.arg(0).length() <= 3) {
					return new ParseError(p.wholeLine, p.argument(0),
						"Only the REPE/REPZ/REPNE/REPNZ prefixes are allowes here");
				}
			}
			position++;
		}
		if (p.mnemo.length() == 4) {
			e = p.validate(position, Op.M8 | Op.M16 | Op.M32);
			if (e == null) {
				if (p.mnemo.equals("MOVS") || p.mnemo.equals("CMPS")) {
					e = p.validate(position + 1, p.type(position));
					if (e == null) {
						e = p.validate(position + 2, Op.NULL);
					}
				} else {
					e = p.validate(position + 1, Op.NULL);
				}
			}
		} else {
			e = p.validate(position, Op.NULL);
		}
		return e;
	}
	
	private void increaseDecreaseRegister(Address register, int value) {
		if (dataspace.fDirection) {
			dataspace.put(register.getShortcut() - value, register, null);
		} else {
			dataspace.put(register.getShortcut() + value, register, null);
		}
	}
	
	private void internalexecute(Parameters p) {
		if (p.mnemo.length() != 4) {
			if (p.mnemo.endsWith("B")) {
				p.size = 1;
			} else if (p.mnemo.endsWith("W")) {
				p.size = 2;
			} else if (p.mnemo.endsWith("D")) {
				p.size = 4;
			}
		}
		Address dest = null;
		long src = 0;
		if (p.mnemo.startsWith("SCAS")) {
			p.a = p.get(dataspace.getMatchingRegister(dataspace.EAX, p.size));
			p.b = -p.get(new Address(Op.MEM, p.size, (int) dataspace.EDI.getShortcut()));
			p.result = p.a + p.b;
			setFlags(p, OF + SF + ZF + AF + PF + CF);
			increaseDecreaseRegister(dataspace.EDI, p.size);
			return;
		} else if (p.mnemo.startsWith("LODS")) {
			dest = dataspace.getMatchingRegister(dataspace.EAX, p.size);
			src = p.get(new Address(Op.MEM, p.size, (int) dataspace.ESI.getShortcut()));
			increaseDecreaseRegister(dataspace.ESI, p.size);
		} else if (p.mnemo.startsWith("STOS")) {
			dest = new Address(Op.MEM, p.size, (int) dataspace.EDI.getShortcut());
			src = p.get(dataspace.getMatchingRegister(dataspace.EAX, p.size));
			increaseDecreaseRegister(dataspace.EDI, p.size);
		} else if (p.mnemo.startsWith("MOVS")) {
			dest = new Address(Op.MEM, p.size, (int) dataspace.EDI.getShortcut());
			src = p.get(new Address(Op.MEM, p.size, (int) dataspace.ESI.getShortcut()));
			increaseDecreaseRegister(dataspace.EDI, p.size);
			increaseDecreaseRegister(dataspace.ESI, p.size);
		} else if (p.mnemo.startsWith("CMPS")) {
			p.a = p.get(new Address(Op.MEM, p.size, (int) dataspace.ESI.getShortcut()));
			p.b = -p.get(new Address(Op.MEM, p.size, (int) dataspace.EDI.getShortcut()));
			p.result = p.a + p.b;
			setFlags(p, OF + SF + ZF + AF + PF + CF);
			increaseDecreaseRegister(dataspace.ESI, p.size);
			increaseDecreaseRegister(dataspace.EDI, p.size);
			return;
		}
		p.put(dest, src, null);
	}
	
	private boolean testCondition(String prefix) {
		return prefix.equals("REP") || testCC(prefix.substring(3));
	}
	
	@Override
	public void execute(Parameters p) {
		if (p.type(0) == Op.PREFIX) {
			p.size = p.size(1);
			if (dataspace.ECX.getShortcut() != 0) {
				do {
					internalexecute(p);
					p.put(dataspace.ECX, dataspace.ECX.getShortcut() - 1, null);
				} while ((dataspace.ECX.getShortcut() != 0) && testCondition(p.arg(0)));
			}
		} else {
			p.size = p.size(0);
			internalexecute(p);
		}
	}
	
}
