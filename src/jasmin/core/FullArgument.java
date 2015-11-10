package jasmin.core;

import java.util.HashSet;

/**
 * @author Jakob Kummerow
 */

public class FullArgument {
	
	public String arg = null;
	public String original = null;
	public int startPos = 0;
	public Address address;
	public boolean sizeExplicit = false;
	public CalculatedAddress cAddress = null;
	public HashSet<String> usedLabels;
	
	public FullArgument(String argument, String aOriginal, int aStart, int aType, int aSize,
			boolean sizeExplicit, DataSpace dsp) {
		if ((aType & Op.REG) != 0) {
			address = dsp.getRegisterArgument(argument);
		} else {
			address = new Address(aType, aSize, 0);
		}
		this.sizeExplicit = sizeExplicit;
		arg = argument;
		original = aOriginal;
		startPos = aStart;
		usedLabels = new HashSet<String>();
		if (Op.matches(aType, Op.LABEL | Op.CONST | Op.VARIABLE)) {
			usedLabels.add(argument);
		}
	}
	
	public FullArgument(int aType, int aSize, int aAddress) {
		address = new Address(aType, aSize, aAddress);
	}
	
	/**
	 * sets the address/cAddress field inside the specified FullArgument to the value corresponding
	 * to its arg field
	 * 
	 * @param a
	 *        the FullArgument whose address is to be set
	 */
	public void calculateAddress(DataSpace dsp) {
		if ((address.type & Op.MEM) != 0) {
			cAddress = new CalculatedAddress(dsp);
			cAddress.readFromString(arg);
			address.address = cAddress.calculateEffectiveAddress(true);
			usedLabels.addAll(cAddress.usedLabels);
		}
		if ((address.type & Op.FPUREG) != 0) {
			address.address = dsp.fpu.getAddress(arg);
		}
	}
	
}
