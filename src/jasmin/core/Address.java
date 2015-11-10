package jasmin.core;

/**
 * @author Jakob Kummerow
 */

public class Address {
	
	public int type = Op.NULL;
	public int datatype = Fpu.NOFPUDATA;
	public int size = -1;
	public int address = -1;
	public long value = 0;
	public boolean dynamic = false;
	
	// special stuff for registers
	public LongWrapper shortcut;
	public int rshift;
	public long mask;
	
	public boolean equals(Address a) {
		return (a.type == this.type) && (a.size == this.size) && (a.address == this.address)
			&& (a.mask == this.mask);
	}
	
	public Address(int aType, int aSize, int aAddress) {
		type = aType;
		size = aSize;
		address = aAddress;
		if (Op.matches(aType, Op.MEM | Op.REG | Op.FPUREG)) {
			dynamic = true;
		}
	}
	
	public Address(int aType, int aSize, long aValue) {
		type = aType;
		size = aSize;
		value = aValue;
	}
	
	public Address clone() {
		Address a = new Address(type, size, address);
		a.datatype = datatype;
		a.value = value;
		a.dynamic = dynamic;
		return a;
	}
	
	public boolean containsAddress(int address) {
		return ((address >= this.address) && (address < this.address + this.size));
	}
	
	public long getShortcut() {
		return (shortcut.value & mask) >> rshift;
	}
	
}
