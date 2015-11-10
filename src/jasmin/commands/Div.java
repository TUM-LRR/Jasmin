package jasmin.commands;

import jasmin.core.*;

import java.math.BigInteger;

public class Div extends jasmin.core.JasminCommand {
	
	@Override
	public String[] getID() {
		return new String[] { "DIV", "IDIV" };
	}
	
	@Override
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.MEM | Op.REG);
		if (e != null) {
			return e;
		}
		return p.validate(1, Op.NULL);
	}
	
	@Override
	public void execute(Parameters p) {
		if (p.mnemo.equals("IDIV")) {
			p.signed = true;
		}
		p.a = p.get(0);
		if (p.size == 1) {
			long ax = p.get(dataspace.AX);
			p.put(dataspace.AL, ax / p.a, null);
			p.put(dataspace.AH, ax % p.a, null);
		} else if (p.size == 2) {
			long dxax = (p.get(dataspace.DX) << 16) | p.get(dataspace.AX);
			p.put(dataspace.AX, dxax / p.a, null);
			p.put(dataspace.DX, dxax % p.a, null);
		} else if (p.size == 4) {
			BigInteger edx = new BigInteger("" + p.get(dataspace.EDX));
			BigInteger eax = new BigInteger("" + p.get(dataspace.EAX));
			BigInteger edxeax = edx.shiftLeft(32).add(eax);
			BigInteger pa = new BigInteger("" + p.a);
			eax = edxeax.divide(pa);
			// we can't use BigInteger.mod() here because it behaves differently
			edx = edxeax.subtract(eax.multiply(pa));
			p.put(dataspace.EAX, eax.longValue(), null);
			p.put(dataspace.EDX, edx.longValue(), null);
		}
	}
	
}
