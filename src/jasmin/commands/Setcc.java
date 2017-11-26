package jasmin.commands;

import jasmin.core.*;
import jasmin.gui.JasDocument;

public class Setcc extends JasminCommand {

	public String[] getID() {
		return new String[] {"SETA", "SETNBE", "SETNC", "SETNE", "SETLE", 
				"SETNG", "SETL", "SETNGE", "SETGE", "SETNL",
				"SETG", "SETNLE", "SETNO", "SETNP", "SETNS", 
				"SETNZ", "SETO", "SETP", "SETPE", "SETPO", 
				"SETS", "SETZ"};
	}

	public ParseError validate(Parameters p) {
		ParseError e = p.validate(1, Op.R8 | Op.M8);
		if (e != null) { return e; }
		return p.validate(1, Op.NULL);
	}

	public void execute(JasDocument jasDocument, Parameters p) {
		if (testCC(p.mnemo.substring(3))) {
			p.put(0, 1, null);
		} else {
			p.put(0, 0, null);
		}
	}

}
