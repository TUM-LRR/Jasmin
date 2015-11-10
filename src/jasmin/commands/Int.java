/*
 * Int.java
 *
 * @author Johannes Roith <roith@in.tum.de>
 * Created on 08. November 2007, 20:00
 * 
 * To use the command:
 * - pass 0x21 as the only parameter 
 * - set AH to 0xA
 * - set DX to the address where you want the input string to be written
 * - set the first byte at [DX] to the max number of characters that can be entered
 * 
 * - now execute "INT 0x21" and enter the string into the dialog box.
 *
 */

package jasmin.commands;

import jasmin.core.*;

public class Int extends jasmin.core.JasminCommand {
	
	public String[] getID() {
		return new String[] { "INT", "INTO" };
	}
	
	public ParseError validate(Parameters p) {
		ParseError e = p.validate(0, Op.I8);
		if (e != null) {
			return e;
		}
		return p.validate(1, Op.NULL);
	}
	
	public void execute(Parameters p) {
		int id = (int) p.get(0);
		switch (id) {
		case 0x21:
			handleDOSInterrupt(p);
			break;
		}
	}
	
	private void handleDOSInterrupt(Parameters p) {
		int function = (int) p.get(dataspace.AH);
		// Buffered Input handler
		if (function == 0x0A) {
			// create internal argument for mem access
			Address argument = new Address(Op.MEM, 1, 0);
			argument.address = (int) p.get(dataspace.DX);
			// read max input size from "byte [dx]"
			int maxsize = (int) dataspace.getUpdate(argument, false);
			// show dialog box, read user's input
			char[] chars = readLine(maxsize).toCharArray();
			// write actual size of input into the byte after the definition of max size
			argument.address++;
			dataspace.put(chars.length, argument, null);
			// write the input to memory, beginning at the next byte after the input's size
			for (int i = 0; i < chars.length; i++) {
				if (i >= (maxsize)) {
					break;
				}
				argument.address++;
				dataspace.put(chars[i], argument, null);
			}
			// write NULL byte after input string
			argument.address++;
			dataspace.put(0x0, argument, null);
		}
	}
	
	public String readLine(int maxLength) {
		String input = javax.swing.JOptionPane.showInputDialog("Input (max. " + maxLength + " characters):");
		if (input != null) {
			if (input.length() > maxLength) {
				return input.substring(0, maxLength - 1);
			} else {
				return input;
			}
		}
		return "";
	}
	
}
