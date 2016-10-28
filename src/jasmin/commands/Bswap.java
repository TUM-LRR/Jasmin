package jasmin.commands;

import jasmin.core.*;


public class Bswap extends JasminCommand {

	public String[] getID() {
		return new String[] {"BSWAP"};
	}

	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.R32);
		if (e != null) return e;
		return p.validate(1, Op.NULL);
	}

	public void execute(Parameters p) {
		long dest;
		long source = p.get(0);
		dest = ((source & 0x000000FF) << 24)
			| ((source & 0x0000FF00) <<  8)
			| ((source & 0x00FF0000) >>  8)
			| ((source & 0xFF000000) >> 24);
		p.put(0, dest, null);
	}

}
