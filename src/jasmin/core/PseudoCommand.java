package jasmin.core;

public abstract class PseudoCommand extends JasminCommand {

	 protected static int getOperationSize(String mnemo) {  
         if (mnemo.endsWith("B")) { return 1; }  
         else if (mnemo.endsWith("W")) { return 2; }  
         else if (mnemo.endsWith("D")) { return 4; }  
         else if (mnemo.endsWith("Q")) { return 8; }  
         return 0;  
    }

}
