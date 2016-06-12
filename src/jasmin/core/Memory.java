package jasmin.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * replacement for the object Byte which is signed.
 * Addtionally there is a dirty tag to trace when the byte has been changed.
 * Serializable in order to be able to be saved to file by ObjectOutputStream
 * 
 * @author Yang Guo
 */
public class Memory implements Serializable {
	
	// //////////////////////////////////////
	// CONSTANTS
	
	/**
	 * the custom zero
	 */
	public static final int ZERO = -128;
	
	private static final long serialVersionUID = 2724629957195481378L;
	
	// //////////////////////////////////////
	// PROPERTIES
	
	/**
	 * the field where the values are actually stored
	 */
	private byte values[];
	
	/**
	 * the dirty tag / works like a time stamp
	 */
	private int dirty[];
	
	/**
	 * the "timestamp" which goes up on every call to updateDirty()
	 */
	private int dirtysteps;
	
	/**
	 * custom index >0 for the first element
	 */
	private int firstIndex;
	
	// //////////////////////////////////////
	// CONSTRUCTORS
	
	/**
	 * sets the newly constructed unsigned byte to given value the dirty tag is set
	 * 
	 * @param size
	 *        the amount of cells you want to be created
	 * @param startIndex
	 *        the index/address of the first cell
	 */
	@SuppressWarnings("unchecked")
	public Memory(int size, int startIndex) {
		this.values = new byte[size];
		this.dirty = new int[size];
		for (int i = 0; i < size; i++) {
			this.values[i] = ZERO;
			this.dirty[i] = Integer.MIN_VALUE;
		}
		this.dirtysteps = Integer.MIN_VALUE + 2;
		this.firstIndex = startIndex;
		this.addressedListeners = new ArrayList[size];
		this.globalListeners = new ArrayList<>();
	}
	
	// //////////////////////////////////////
	// DIRTY MANAGEMENT
	
	/**
	 * set dirty tag to the initial value
	 */
	public void setDirty(int index) {
		this.dirty[index - firstIndex] = dirtysteps;
	}
	
	/**
	 * update the dirty state, that is, decrement the dirty tag by 1
	 */
	public void updateDirty() {
		dirtysteps++;
		if (dirtysteps == Integer.MAX_VALUE) {
			clearDirty();
		}
	}
	
	/**
	 * clear the dirty tag
	 */
	public void clearDirty(int index) {
		dirty[index - firstIndex] = Integer.MIN_VALUE;
	}
	
	/**
	 * clear all dirty tags
	 */
	public void clearDirty() {
		dirtysteps = Integer.MIN_VALUE + 2;
		for (int i = 0; i < dirty.length; i++) {
			dirty[i] = Integer.MIN_VALUE;
		}
	}
	
	/**
	 * @param steps
	 * @return if the byte has been changed in the last given steps
	 */
	public boolean isDirty(int index, int steps) {
		return ((dirtysteps - dirty[index - firstIndex]) <= steps);
	}
	
	// //////////////////////////////////////
	// CONCERNING VALUE
	
	/**
	 * @param value
	 *        value to be set to sets the value
	 */
	public void set(int index, int value) {
		this.values[index - firstIndex] = (byte) (value + ZERO);
		// setDirty() -- inlined for better performance
		this.dirty[index - firstIndex] = dirtysteps;
		notifyListeners(index, value);
	}
	
	/**
	 * resets the whole thing
	 */
	public void reset() {
		for (int i = 0; i < values.length; i++) {
			this.values[i] = ZERO;
		}
		clearDirty();
	}
	
	/**
	 * @return the correct integer value
	 */
	public int get(int index) {
		return (values[index - firstIndex]) - ZERO;
	}
	
	// //////////////////////////////////////
	// LISTENER SUPPORT
	
	private List<IListener>[] addressedListeners;
	private List<IListener> globalListeners;
	
	public void addListener(IListener l, int address) {
		if (addressedListeners[address - firstIndex] == null) {
			addressedListeners[address - firstIndex] = new ArrayList<>();
		}
		addressedListeners[address - firstIndex].add(l);
		System.out.println("memory listener[" + address + "] added: " + l.getClass().getName());
	}
	
	public void addListener(IListener l) {
		globalListeners.add(l);
		System.out.println("memory listener (global) added: " + l.getClass().getName());
	}
	
	public void removeListener(IListener l, int address) {
		addressedListeners[address - firstIndex].remove(l);
	}
	
	public void removeListener(IListener l) {
		globalListeners.remove(l);
		System.out.println("memory listener (global) removed: " + l.getClass().getName());
	}
	
	private void notifyListeners(int address, int newValue) {
		for (IListener l : globalListeners) {
			l.notifyChanged(address, newValue);
		}
		if (addressedListeners[address - firstIndex] != null) {
			for (IListener l : addressedListeners[address - firstIndex]) {
				l.notifyChanged(address, newValue);
			}
		}
	}
	
}