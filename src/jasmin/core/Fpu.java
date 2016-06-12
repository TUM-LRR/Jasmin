package jasmin.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Fpu {
	
	private double[] registers;
	private byte[] tags;
	private int top = 0;
	
	private static int numRegisters = 8;
	
	/**
	 * a reg-ex string matching any of the FPU register names
	 */
	private static String registersMatchingString = "((ST0)|(ST1)|(ST2)|(ST3)|(ST4)|(ST5)|(ST6)|(ST7))";
	private static String qualifiersMatchingString = "((TO))";
	public static Pattern pRegisters = Pattern.compile(registersMatchingString);
	public static Pattern pQualifiers = Pattern.compile(qualifiersMatchingString);
	
	public boolean fC0, fC1, fC2, fC3, fStackFault, fPrecision, fUnderflow, fOverflow, fZeroDivide, fDenormalized, fInvalid;
	
	public static byte TAGVALID = 0;
	public static byte TAGZERO = 1;
	public static byte TAGSPECIAL = 2;
	public static byte TAGEMPTY = 3;
	
	public static int FLOAT = 2003;
	public static int PACKEDBCD = 2002;
	public static int INTEGER = 2001;
	public static int NOFPUDATA = 0;
	
	/**
	 * by default, memory accesses operate on 8 bytes / 64 bit of data
	 */
	public static int defaultOperandSize = 8;
	
	/**
	 * default constructor. initializes internal variables.
	 */
	public Fpu() {
		registers = new double[numRegisters];
		tags = new byte[numRegisters];
		for (int i = 0; i < numRegisters; i++) {
			tags[i] = TAGEMPTY;
		}
		globalListeners = new ArrayList<>();
	}
	
	/**
	 * resets all internal variables to their initial values
	 */
	public void clear() {
		registers = new double[numRegisters];
		tags = new byte[numRegisters];
		for (int i = 0; i < numRegisters; i++) {
			tags[i] = TAGEMPTY;
		}
		fC0 = fC1 = fC2 = fC3 = false;
		fStackFault = fPrecision = fUnderflow = fOverflow = fZeroDivide = fDenormalized = fInvalid = false;
		top = 0;
	}
	
	// ////////////////////
	// GUI functions: getRegisterContents(), putRegisterContents()
	
	/**
	 * returns the content of the specified FPU register as a String in the specified number system
	 * 
	 * @param registerNumber
	 *        the number of the register whose contents is to be accessed, e.g. "0" for "ST0"
	 * @param radix
	 *        the base of the number system in which the output shall be represented. Currently, any value except 2, 10
	 *        or 16 is ignored.
	 * @return the contents of the specified register as a String
	 */
	public String getRegisterContent(int registerNumber, int radix) {
		if (radix == 10) {
			return Double.toString(registers[registerNumber]);
		} else if (radix == 16) {
			return Double.toHexString(registers[registerNumber]);
		} else if (radix == 2) {
			String s = Long.toBinaryString(Double.doubleToRawLongBits(registers[registerNumber]));
			while (s.length() < 64) {
				s = "0" + s;
			}
			return s;
		}
		return "";
	}
	
	/**
	 * sets thw content of a register to the specified String, which must be the representation of a double value (only
	 * in the decimal system, currently)
	 * 
	 * @param registerNumber
	 *        the number of the register whose content is to be set
	 * @param content
	 *        the String containing the new value
	 * @return true if everything went fine, false otherwise
	 */
	public boolean putRegisterContent(int registerNumber, String content) {
		try {
			registers[registerNumber] = Double.parseDouble(content);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	
	/**
	 * returns the number of FPU registers. For an x87 FPU as currently implemented, this will always be 8.
	 * 
	 * @return the number of FPU registers
	 */
	public int getNumRegisters() {
		return numRegisters;
	}
	
	/**
	 * returns the current name of the register with the given (hardware) index. For example, if the first hardware
	 * register is currently assigned the name "ST3", then this name is returned for input "0"
	 * 
	 * @param registerNumber
	 *        the (static, hardware) index of the register
	 * @return the current name of the register
	 */
	public String getRegisterName(int registerNumber) {
		int temp = registerNumber - top;
		if (temp < 0) {
			temp += numRegisters;
		}
		return "ST" + temp;
	}
	
	/**
	 * converts the values of the FPU flags into a word value and returns it
	 * 
	 * @return a word value whose bits represent the status of the FPU's flags
	 */
	public long getStatusWord() {
		long word = 0;
		if (fInvalid) {
			word |= 1;
		}
		if (fDenormalized) {
			word |= 2;
		}
		if (fZeroDivide) {
			word |= 4;
		}
		if (fOverflow) {
			word |= 8;
		}
		if (fUnderflow) {
			word |= 16;
		}
		if (fPrecision) {
			word |= 32;
		}
		if (fStackFault) {
			word |= 64;
		}
		
		if (fC0) {
			word |= 256;
		}
		if (fC1) {
			word |= 512;
		}
		if (fC2) {
			word |= 1024;
		}
		word |= (top << 11);
		if (fC3) {
			word |= 16384;
		}
		// System.out.println("FPU status word: "+Long.toBinaryString(word));
		return word;
	}
	
	/**
	 * sets the FPU's flags from a given word value such as the one produced by getStatusWord().
	 * 
	 * @param statusWord
	 *        the word value whose bits represent the status of the FPU's flags
	 */
	public void putStatusWord(long statusWord) {
		fInvalid = ((statusWord & 1) == 1);
		fDenormalized = ((statusWord & 2) == 2);
		fZeroDivide = ((statusWord & 4) == 4);
		fOverflow = ((statusWord & 8) == 8);
		fUnderflow = ((statusWord & 16) == 16);
		fPrecision = ((statusWord & 32) == 32);
		fStackFault = ((statusWord & 64) == 64);
		
		fC0 = ((statusWord & 256) == 256);
		fC1 = ((statusWord & 512) == 512);
		fC2 = ((statusWord & 1024) == 1024);
		top = (int) ((statusWord >> 11) & 7);
		fC3 = ((statusWord & 16384) == 16384);
	}
	
	/**
	 * converts the FPU's registers' tags into a word value and returns it
	 * 
	 * @return the word value containing the FPU's registers' tag values
	 */
	public long getTagWord() {
		long word = 0;
		for (int i = 0; i < numRegisters; i++) {
			word = word << 2;
			word |= tags[numRegisters - 1 - i];
		}
		return word;
	}
	
	/**
	 * restores the registers' tags from a given word value, such as the one produced by getTagWord().
	 * 
	 * @param word
	 *        the word value containing the registers' tag values
	 */
	public void putTagWord(long word) {
		for (int i = 0; i < numRegisters; i++) {
			tags[i] = (byte) (word & 3);
			word = word >> 2;
		}
	}
	
	/**
	 * returns the appropriate tag for an FPU register containing the specified double value
	 * 
	 * @param d
	 *        the contents of the register
	 * @return the register's tag
	 */
	private static byte getTag(double d) {
		if (Double.isNaN(d) || Double.isInfinite(d)) {
			return TAGSPECIAL;
		}
		if (d == 0) {
			return TAGZERO;
		}
		return TAGVALID;
	}
	
	private void setStackOverflow() {
		fInvalid = true;
		fStackFault = true;
		fC1 = true;
	}
	
	private void setStackUnderflow() {
		fInvalid = true;
		fStackFault = true;
		fC1 = false;
	}
	
	public void push(double value) {
		top--;
		if (top == -1) {
			top += numRegisters;
		}
		if (!(tags[top] == TAGEMPTY)) {
			setStackOverflow();
		}
		registers[top] = value;
		tags[top] = getTag(value);
	}
	
	public double pop() {
		if (tags[top] == TAGEMPTY) {
			setStackUnderflow();
		}
		double value = registers[top];
		tags[top] = TAGEMPTY;
		top++;
		if (top == numRegisters) {
			top = 0;
		}
		return value;
	}
	
	public void put(int relativePosition, double value) {
		int position = (top + relativePosition) % numRegisters;
		registers[position] = value;
		tags[position] = getTag(value);
		notifyListeners(relativePosition, 0);
	}
	
	public void put(Address a, long doubleBits) {
		put(a.address, Double.longBitsToDouble(doubleBits));
	}
	
	public long get(Address a) {
		return Double.doubleToRawLongBits(get(a.address));
	}
	
	public int getAddress(String fpuregister) {
		return Integer.valueOf(fpuregister.substring(2));
	}
	
	public double get(int relativePosition) {
		int position = top + relativePosition % numRegisters;
		if (tags[position] == TAGEMPTY) {
			setStackUnderflow();
		}
		return registers[position];
	}
	
	public static double doubleFromLong(long a) {
		return Double.valueOf(Long.toString(a));
	}
	
	public static long longFromPackedBCD(long a) {
		long b = 0;
		long temp;
		long counter = 0;
		while (a > 0) {
			temp = a & 15;
			b += temp * ((int) Math.pow(10, counter));
			a = a >> 4;
			counter++;
		}
		return b;
	}
	
	public static double doubleFromPackedBCD(long a) {
		return Double.valueOf(Long.toString(longFromPackedBCD(a)));
	}
	
	public static long longFromDouble(double d) {
		return ((Double) d).longValue();
	}
	
	public static long packedBCDFromLong(long a) {
		long b = 0;
		long temp;
		long counter = 0;
		while (a > 0) {
			temp = a % 10;
			temp = temp << (4 * counter);
			b = b | temp;
			a /= 10;
			counter++;
		}
		return b;
	}
	
	public static long packedBCDFromDouble(double d) {
		long a = ((Double) d).longValue();
		return packedBCDFromLong(a);
	}
	
	/**
	 * checks whether a given double value fits into the specified number of bytes
	 * 
	 * @param d
	 *        the double value
	 * @param numberBytes
	 *        the number of bytes
	 * @return true if it fits, false if not
	 */
	public static boolean fitsInto(double d, int numberBytes) {
		long l = ((Double) d).longValue();
		return Parser.getOperandSize(l) <= numberBytes;
	}
	
	/**
	 * extracts the exponent of a given double value
	 * 
	 * @param d
	 *        the double value whose exponent is to be extracted
	 * @return the exponent of the input value
	 */
	public static long doubleExponent(double d) {
		long l = Double.doubleToRawLongBits(d);
		long mask = 2047;
		l = l >> 52;
		l &= mask;
		return l;
	}
	
	/**
	 * extracts the mantissa out of a given double value
	 * 
	 * @param d
	 *        the double value whose mantissa is to be extracted
	 * @return the mantissa of the input value
	 */
	public static long doubleMantissa(double d) {
		long l = Double.doubleToRawLongBits(d);
		l = l << 12;
		l = l >> 12;
		return l;
	}
	
	/**
	 * Returns the sign of the specified double value
	 * 
	 * @param d
	 *        the double value whose sign is to be extracted
	 * @return 1 if the input value is positive, -1 if it is negative
	 */
	public static long doubleSign(double d) {
		if (((Double.doubleToRawLongBits(d) >> 63) & 1) == 1) {
			return -1;
		} else {
			return 1;
		}
	}
	
	/**
	 * checks whether a given double value is denormalized
	 * 
	 * @param d
	 *        the double value to be checked
	 * @return true if the input value is denormalized
	 */
	public static boolean doubleIsDenormal(double d) {
		long exp = doubleExponent(d);
		long mant = doubleMantissa(d);
		return (exp == 0) && (mant != 0);
	}
	
	// ////// LISTENER SUPPORT
	
	private List<IListener> globalListeners;
	
	public void addListener(IListener l) {
		globalListeners.add(l);
		System.out.println("FPU listener (global) added: " + l.getClass().getName());
	}
	
	public void removeListener(IListener l) {
		globalListeners.remove(l);
		System.out.println("FPU listener (global) removed: " + l.getClass().getName());
	}
	
	private void notifyListeners(int address, int newValue) {
		for (IListener l : globalListeners) {
			l.notifyChanged(address, newValue);
		}
	}
	
}
