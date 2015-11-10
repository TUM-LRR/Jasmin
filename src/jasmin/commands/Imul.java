package jasmin.commands;

import jasmin.core.*;

public class Imul extends JasminCommand {
	
	@Override
	public String[] getID() {
		return new String[] { "IMUL" };
	}
	
	@Override
	public boolean signed() {
		return true;
	}
	
	@Override
	public ParseError validate(Parameters p) {
		ParseError e;
		e = p.validate(3, Op.NULL);
		if (e != null) {
			return e;
		}
		if (p.validate(2, Op.NULL) == null) {
			return validate2(p);
		}
		e = p.validate(2, Op.IMM | Op.CONST);
		if (e != null) {
			return e;
		}
		e = p.validate(1, Op.MEM | Op.REG);
		if (e != null) {
			return e;
		}
		e = p.validate(0, Op.REG);
		if (e != null) {
			return e;
		}
		e = p.consistentSizes();
		return e;
	}
	
	private ParseError validate2(Parameters p) {
		if (p.validate(1, Op.NULL) == null) {
			return p.validate(0, Op.REG | Op.MEM);
		}
		ParseError e = p.validate(1, Op.MEM | Op.REG | Op.IMM | Op.CONST);
		if (e != null) {
			return e;
		}
		e = p.validate(0, Op.REG);
		return e;
	}
	
	@Override
	public void execute(Parameters p) {
		if (p.validate(2, Op.NULL) == null) {
			if (p.validate(1, Op.NULL) == null) {
				ex1(p);
				return;
			}
			p.a = p.get(0);
			p.b = p.get(1);
		} else {
			p.a = p.get(1);
			p.b = p.get(2);
		}
		if (p.size == 1) {
			p.b *= p.a;
			p.put(0, p.b, null);
			p.result = (p.b >> 8) & 0xFF;
		} else if (p.size == 2) {
			p.b *= p.a;
			p.put(0, p.b & 0xFFFF, null);
			p.result = ((p.b >> 16) & 0xFFFF);
		} else if (p.size == 4) {
			p.b *= p.a;
			p.put(0, p.b & 0xFFFFFFFF, null);
			p.result = ((p.b >> 32) & 0xFFFFFFFF);
		}
		dataspace.fOverflow = dataspace.fCarry = (p.result != 0);
	}
	
	private void ex1(Parameters p) {
		p.a = p.get(0);
		if (p.size == 1) {
			long al = p.get(dataspace.AL);
			al *= p.a;
			p.put(dataspace.AX, al, null);
			p.result = (al >> 8) & 0xFF;
		} else if (p.size == 2) {
			long ax = p.get(dataspace.AX);
			ax *= p.a;
			p.put(dataspace.AX, ax & 0xFFFF, null);
			p.result = ((ax >> 16) & 0xFFFF);
			p.put(dataspace.DX, p.result, null);
		} else if (p.size == 4) {
			long eax = p.get(dataspace.EAX);
			eax *= p.a;
			p.put(dataspace.EAX, eax & 0xFFFFFFFF, null);
			p.result = ((eax >> 32) & 0xFFFFFFFF);
			p.put(dataspace.EDX, p.result, null);
		}
		dataspace.fOverflow = dataspace.fCarry = (p.result != 0);
	}
	
}
