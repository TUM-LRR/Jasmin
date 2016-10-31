/**
 *
 */
package jasmin.core;

import java.util.Hashtable;

/**
 * @author Jakob Kummerow, Florian Dollinger
 */
public class Registers {

	private int NUMREG = 9; // A, B, C, D, SI, DI, SP, BP, IP

	private LongWrapper[] reg;

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
		// addressedListeners = new ArrayList[NUMREG];
		// globalListeners = new ArrayList<IListener>();
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

        /*
        * @param reg
        * @return returns the name of the given registerpart (L: LOW, H: HIGH, X: LOW and HIGH, E: EXTENDED)
        */
        public char registerType(Address reg){

            // LOW or HIGH
            if(reg.type == Op.R8){
                // LOW
                    if(reg.rshift == 0){
                            return 'L';
                    // HIGH
                    } else if(reg.rshift == 8){
                            return 'H';
                    }

            // X
            } else if(reg.type == Op.R16){
                return 'X';

            // EXTENDED
            } else if(reg.type == Op.R32){
                return 'E';
            }

            return 'U'; // Unknown
        }


	public void set(Address address, long value) {

                // Set the value
		value <<= address.rshift;
		address.shortcut.value = (address.shortcut.value & ~address.mask) | (value & address.mask);

                // Set the "time" of last change
		this.dirty[address.address] = dirtyTimeStamp;

                // Set the parts that are changed
                switch(registerType(address)){
                    case 'L':
                        this.dirtyParts[address.address] = 0b0001;
                        break;
                    case 'H':
                        this.dirtyParts[address.address] = 0b0010;
                        break;
                    case 'X':
                        this.dirtyParts[address.address] = 0b0011;
                        break;
                    case 'E':
                        this.dirtyParts[address.address] = 0b1111;
                        break;
                    default:
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
	 * @param address
	 * @param steps
	 * @return if the byte has been changed in the last given steps
	 */
	public boolean isDirty(Address address, int steps) {

            if((dirtyTimeStamp - dirty[address.address]) <= steps){

                switch(registerType(address)){

                    case 'L':
                        return (this.dirtyParts[address.address] & 0b0001) == 0b0001;
                    case 'H':
                        return (this.dirtyParts[address.address] & 0b0010) == 0b0010;
                    case 'X':
                        return (this.dirtyParts[address.address] & 0b0011) == 0b0011;
                    case 'E':
                        return (this.dirtyParts[address.address] & 0b1111) == 0b1111;
                    default:
                        return false;

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
}
