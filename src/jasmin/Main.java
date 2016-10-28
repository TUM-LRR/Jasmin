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

	/* Prevent instance creation */
	private Main() {
		throw new AssertionError("Cannot instantiate Main");
	}
	
	/**
	 * @param args
	 *        the command line arguments
	 */
	public static void main(String[] args) {
		System.setProperty("swing.aatext", "true");
		MainFrame frame = new MainFrame();
		frame.setVisible(true);
	}
	
}
