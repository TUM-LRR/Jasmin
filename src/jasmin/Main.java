/*
 * Main.java
 *
 * Created on 16. März 2006, 17:22
 *
 */

package jasmin;

import jasmin.gui.MainFrame;

/**
 * @author kai
 */
public class Main {
	
	/** Creates a new instance of Main */
	public Main() {
	}
	
	/**
	 * @param args
	 *        the command line arguments
	 */
	public static void main(String[] args) {
		System.setProperty("swing.aatext", "true");
		MainFrame frame = new MainFrame();
		frame.setVisible(true);
		
		// System.out.println(System.getProperties());
		// System.out.println(System.getProperty("java.class.path"));
		//
		// System.out.println(new File(System.getProperty("java.class.path")));
		// System.out.println(new File(System.getProperty("java.class.path")).getParentFile());
		//
		// System.out.println(new File("."));
		//
		// System.out.println(new File("./"));
		
	}
	
}
