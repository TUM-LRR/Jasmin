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

	@Override
	public boolean equals(Object other) {
		if (other instanceof Address) {
			Address a = (Address) other;
			return (a.type == this.type) && (a.size == this.size) && (a.address == this.address)
					&& (a.mask == this.mask);
		}
		return false;
	}
	
	public Address(int type, int size, int address) {
		this.type = type;
		this.size = size;
		this.address = address;
		if (Op.matches(type, Op.MEM | Op.REG | Op.FPUREG)) {
			dynamic = true;
		}
	}
	
	public Address(int type, int size, long value) {
		this.type = type;
		this.size = size;
		this.value = value;
	}

	@Override
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
