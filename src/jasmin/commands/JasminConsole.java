/**
 * This is the SLEEP command. It is NOT part of an x86's instruction set, but is useful to have in
 * the simulator.
 */
package jasmin.commands;

import jasmin.core.JasminCommand;
import jasmin.core.Op;
import jasmin.core.Parameters;
import jasmin.core.ParseError;
import jasmin.gui.Console;
import jasmin.gui.JasDocument;

/**
 * @author Emil Suleymanov
 */
public class JasminConsole extends JasminCommand {

  @Override
  public String[] getID() {
    return new String[]{"JASMINCONSOLE"};
  }

  @Override
  public ParseError validate(Parameters p) {
    ParseError e = p.validate(0, Op.STRING | Op.CHARS);
    if (e != null) {
      return e;
    }

    switch (p.argument(0).arg) {
      case "'clear'":
        return p.validate(1, Op.NULL);
      case "'mode'":
        e = p.validate(1, Op.MEM | Op.REG | Op.I8 | Op.I16 | Op.I32 | Op.I64);
        if (e == null) {
          if (p.get(1) != 0 && p.get(1) != 1) {
            return new ParseError(p.wholeLine, p.argument(1), "Must be 0 for MODE_ARRAY or 1 for MODE_PIPE!");
          }
        }
        break;
      default:
        return new ParseError(p.wholeLine, p.argument(0), "Invalid command!");
    }

    return null;
  }

  @Override
  public void execute(JasDocument jasDocument, Parameters p) {
    jasDocument.modules.forEach(module -> {
      if (module instanceof Console) {
        Console c = (Console) module;

        switch (p.argument(0).arg) {
          case "'clear'":
            c.clear();
            break;
          case "'mode'":
            c.setMode((int) p.get(1));
            break;
          default:
            break;
        }
      }
    });
  }

}
