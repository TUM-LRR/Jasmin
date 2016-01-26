/**
 * 
 */
package jasmin.core;

import java.util.Hashtable;

/**
 * @author Jakob Kummerow
 */
public class Registers {
	
	int NUMREG = 9; // A, B, C, D, SI, DI, SP, BP, IP
	
	LongWrapper[] reg;
	
	/**
	 * the dirty tag / works like a time stamp
	 */
	private int dirty[];
        private int dirtyParts[];
	
	/**
	 * the "timestamp" which goes up on every call to updateDirty()
	 */
	private int dirtyTimeStamp;
	
	// @SuppressWarnings("unchecked")
	public Registers() {
		reg = new LongWrapper[9];
		dirty = new int[NUMREG];
                dirtyParts = new int[NUMREG];
		for (int i = 0; i < NUMREG; i++) {
			reg[i] = new LongWrapper();
			dirty[i] = Integer.MIN_VALUE;
                        dirtyParts[i] = 0;
		}
		dirtyTimeStamp = Integer.MIN_VALUE + 2;
		// addressedListeners = new LinkedList[NUMREG];
		// globalListeners = new LinkedList<IListener>();
	}
	
	public Address constructAddress(String registerName, Hashtable<String, Address> registerTable) {
		// if the register address exists already, return it
		Address register = registerTable.get(registerName);
		if (register != null) {
			return register;
		}
		// otherwise, construct a new address object
		register = new Address(Op.REG, 0, 0);
		// determine the size
		if (registerName.matches("E..")) {
			register.type = Op.R32;
			register.size = 4;
			register.mask = 0xFFFFFFFFL;
			register.rshift = 0;
		} else if (registerName.matches(".[XIP]")) {
			register.type = Op.R16;
			register.size = 2;
			register.mask = 0xFFFFL;
			register.rshift = 0;
		} else if (registerName.matches(".H")) {
			register.type = Op.R8;
			register.size = 1;
			register.mask = 0xFF00L;
			register.rshift = 8;
		} else if (registerName.matches(".L")) {
			register.type = Op.R8;
			register.size = 1;
			register.mask = 0xFFL;
			register.rshift = 0;
		}
		// determine the shortcut
		if (registerName.matches(".?A.")) {
			register.address = 0;
		} else if (registerName.matches("E?B[HLX]")) {
			register.address = 1;
		} else if (registerName.matches("E?C.")) {
			register.address = 2;
		} else if (registerName.matches("E?D[HLX]")) {
			register.address = 3;
		} else if (registerName.matches("E?SI")) {
			register.address = 4;
		} else if (registerName.matches("E?DI")) {
			register.address = 5;
		} else if (registerName.matches("E?SP")) {
			register.address = 6;
		} else if (registerName.matches("E?BP")) {
			register.address = 7;
		} else if (registerName.matches("E?IP")) {
			register.address = 8;
		}
		register.shortcut = reg[register.address];
		// add the new object to the register table
		registerTable.put(registerName, register);
		
		return register;
	}
	
	public void set(Address address, long value) {

		value <<= address.rshift;
		address.shortcut.value = (address.shortcut.value & ~address.mask) | (value & address.mask);
		this.dirty[address.address] = dirtyTimeStamp;
                
                // low:      mask: 0xFF,   shift: 0
                // high:     mask: 0xFF00, shift: 8
                // extended: mask: 0xFFFFFFFF, shift: 0
                // X:        mask: 0xFFFF, shift: 0
                
                // low is dirty
                if(address.rshift == 0 && address.mask == 0xFF){
                    this.dirtyParts[address.address] = 0b0001;
                }
                
                // high is dirty
                if(address.rshift == 8 && address.mask == 0xFF00){
                    this.dirtyParts[address.address] = 0b0010;
                }
                
                // X is dirty
                if(address.rshift == 0 && address.mask == 0xFFFF){
                    this.dirtyParts[address.address] = 0b0011;
                }
                
                // E is dirty
                // TODO: 0xFFFFFFFF as constant is -1 ?!
                if(address.rshift == 0 && address.mask == Long.decode("0xFFFFFFFF")){
                    this.dirtyParts[address.address] = 0b1111;
                }

                // notifyListeners(address, (int) value);


	}
	
	/**
	 * resets the whole thing
	 */
	public void reset() {
		for (int i = 0; i < NUMREG; i++) {
			reg[i].value = 0L;
		}
		clearDirty();
	}
	
	/**
	 * set dirty tag to the initial value
	 */
	public void setDirty(Address address) {
		this.dirty[address.address] = dirtyTimeStamp;
	}
	
	/**
	 * @param steps
	 * @return if the byte has been changed in the last given steps
	 */
	public boolean isDirty(Address address, int steps) {
            
            if((dirtyTimeStamp - dirty[address.address]) <= steps){

                // low is dirty
                if(address.rshift == 0 && address.mask == 0xFF){
                    return (this.dirtyParts[address.address] & 0b0001) == 0b0001;
                }
                
                // high is dirty
                if(address.rshift == 8 && address.mask == 0xFF00){
                    return (this.dirtyParts[address.address] & 0b0010) == 0b0010;
                }
                
                // X is dirty
                if(address.rshift == 0 && address.mask == 0xFFFF){
                    return (this.dirtyParts[address.address] & 0b0011) == 0b0011;
                }
                
                // E is dirty
                if(address.rshift == 0 && address.mask == Long.decode("0xFFFFFFFF")){
                    return (this.dirtyParts[address.address] & 0b1111) == 0b1111;
                }
            }
            
            return false;

                
	}
	
	/**
	 * update the dirty state, that is, decrement the dirty tag by 1
	 */
	public void updateDirty() {
		dirtyTimeStamp++;
		if (dirtyTimeStamp == Integer.MAX_VALUE) {
			clearDirty();
		}
	}
	
	/**
	 * clear the dirty tag
	 */
	public void clearDirty(Address address) {
		dirty[address.address] = Integer.MIN_VALUE;
	}
	
	/**
	 * clear all dirty tags
	 */
	public void clearDirty() {
		dirtyTimeStamp = Integer.MIN_VALUE + 2;
		for (int i = 0; i < dirty.length; i++) {
			dirty[i] = Integer.MIN_VALUE;
		}
	}
	
	// //////////////////////////////////////
	// LISTENER SUPPORT
	/*
	private LinkedList<IListener>[] addressedListeners;
	private LinkedList<IListener> globalListeners;
	
	public void addListener(IListener l) {
		globalListeners.add(l);
	}
	
	public void removeListener(IListener l) {
		globalListeners.remove(l);
	}
	
	private void notifyListeners(Address address, int newValue) {
		for (IListener l : globalListeners) {
			l.notifyChanged(address.address, newValue);
		}
		if (addressedListeners[address.address] != null) {
			for (IListener l : addressedListeners[address.address]) {
				l.notifyChanged(address.address, newValue);
			}
		}
	}
	*/
}
