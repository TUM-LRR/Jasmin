package jasmin.commands;

import jasmin.core.*;

/**
 * @author Yang Guo, Jakob Kummerow
 */
public class Push extends JasminCommand {
	
	/**
	 * @param mnemo
	 *        the mnemo for the command whose default size is requested
	 */
	@Override
	public int defaultSize(String mnemo) {
		return 2;
	}
	
	@Override
	public String[] getID() {
		return new String[] { "PUSH" };
	}
	
	@Override
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.REG | Op.MEM | Op.IMM | Op.LABEL | Op.VARIABLE | Op.CONST);
		if (e != null) {
			return e;
		}
		e = p.validateAllSizes(2, 4);
		if (e != null) {
			return e;
		}
		e = p.validate(1, Op.NULL);
		return e;
	}
	
	@Override
	public void execute(Parameters p) {
		p.argument(0).address.size = p.size;
		p.push(p.argument(0).address);
	}
	
}
