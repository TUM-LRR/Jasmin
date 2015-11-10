/**
 * IGuiModule.java
 */
package jasmin.gui;

import jasmin.core.DataSpace;

/**
 * This interface must be implemented by all GUI modules that want to be shown in the tabbed area below the code in a
 * JasDocument window. <br />
 * A note about registering as a memory listener vs. reacting to calls of updateAll(): <br />
 * A listener is notified far more often than updateAll() is called, because it is triggered by any write to the given
 * memory location while a program is being run, whereas updateAll() is only called after the program has finished.
 * Therefore, from a performance point of view, the following is recommendable: <br/>
 * If the module needs to update its output during program execution (to allow for animations), or if it monitors a
 * large number of memory cells, it should register as a listener ( <code>dataspace.addMemoryListener(...)</code> ) <br />
 * Otherwise, it is preferable to just react to calls of updateAll().
 * 
 * @author Jakob Kummerow
 */
public interface IGuiModule {
	
	/**
	 * This method is called when the module is activated, i.e. made visible to the user (as opposed to being within a
	 * hidden tab). The module should react to this information to skip unnecessary updates, which would not be seen
	 * anyway but would still incur a performance penalty.
	 * 
	 * @param activated
	 *        whether the module is now active (true) or hidden (false)
	 */
	public void setActivated(boolean activated);
	
	/**
	 * This method is called when the GUI thinks that the module should update its output, usually after the internal
	 * state (registers and/or memory) has changed as a result of program execution or user interaction. <br />
	 * Note that if the module is registered as a memory listener by itself and determines its update actions that way,
	 * it might be a good idea to do nothing in this method.
	 */
	public void updateAll();
	
	/**
	 * This method is called when Jasmin is reset by the user, i.e. all registers and memory are cleared. <br />
	 * Note that memory listeners are NOT notified in the event of a memory reset, but this method will be called, so
	 * any action that is necessary upon a reset should be placed here.
	 */
	public void clear();
	
	/**
	 * Using this method, the module should return the label that is to be shown on the tab activating the module
	 * 
	 * @return a label for this module's tab
	 */
	public String getTabLabel();
	
	/**
	 * This method will be called right after the constructor of this module is called to pass the associated DataSpace.
	 * Any initialisation actions of the module that need knowledge of and/or access to the DataSpace (such as listener
	 * registration) should be placed here.
	 * 
	 * @param dsp
	 *        the DataSpace
	 */
	public void setDataSpace(DataSpace dsp);
	
}
