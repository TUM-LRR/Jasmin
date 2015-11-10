/**
 * 
 */
package jasmin.core;

/**
 * @author Jakob Kummerow
 */
public interface IListener {
	
	/**
	 * This method will be called when the value listened to changes. Warning: in the current implementation,
	 * this method will NOT be called in the event of a DataSpace Reset! To react to such a reset (which is,
	 * in a sense, not a regular "change" anyway), you have to rely on some other notification.
	 * 
	 * @param address
	 *        the address where the change occurred
	 * @param newValue
	 *        the new value of the watched field
	 */
	public void notifyChanged(int address, int newValue);
	
}
