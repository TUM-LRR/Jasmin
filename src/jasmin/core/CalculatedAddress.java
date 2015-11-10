package jasmin.core;

import java.util.HashSet;
import java.util.regex.*;

/**
 * an extension for DataSpace for the address calculation
 * 
 * @author Jakob Kummerow
 */
public class CalculatedAddress {
	
	/**
	 * some reg-exp patterns that are used frequently (most of them for checking correctness of calculated
	 * memory addresses)
	 */
	private static String registersMatchingString = createMatchingString(DataSpace.getRegisterList());
	public static Pattern pRegisters = Pattern.compile(registersMatchingString);
	
	// base + displacement
	public static Pattern pBasePlusDisplacement = Pattern.compile(registersMatchingString + "\\+\\d+");
	// base - displacement
	public static Pattern pBaseMinusDisplacement = Pattern.compile(registersMatchingString + "\\-\\d+");
	// (index*scale)
	public static Pattern pIndexScale = Pattern.compile(registersMatchingString + "\\*\\d+");
	// (index*scale) + displacement
	public static Pattern pIndexScalePlusDisplacement = Pattern.compile(registersMatchingString
		+ "\\*\\d+\\+\\d+");
	// (index*scale) - displacement
	public static Pattern pIndexScaleMinusDisplacement = Pattern.compile(registersMatchingString
		+ "\\*\\d+\\-\\d+");
	// base + index + displacement
	public static Pattern pBaseIndexPlusDisplacement = Pattern.compile(registersMatchingString + "\\+"
		+ registersMatchingString
		+ "\\+\\d+");
	// base + index - displacement
	public static Pattern pBaseIndexMinusDisplacement = Pattern.compile(registersMatchingString + "\\+"
		+ registersMatchingString
		+ "\\-\\d+");
	// base + (index*scale)
	public static Pattern pBaseIndexScale = Pattern
		.compile(registersMatchingString + "\\+" + registersMatchingString + "\\*\\d+");
	// base + index // implicit scale: *1
	public static Pattern pBaseIndex = Pattern.compile(registersMatchingString + "[\\+\\-]"
		+ registersMatchingString);
	// base + (index*scale) + displacement
	public static Pattern pBaseIndexScalePlusDisplacement = Pattern.compile(registersMatchingString + "\\+"
		+ registersMatchingString + "\\*\\d+\\+\\d+");
	// base + (index*scale) - displacement
	public static Pattern pBaseIndexScaleMinusDisplacement = Pattern.compile(registersMatchingString + "\\+"
		+ registersMatchingString + "\\*\\d+\\-\\d+");
	
	/**
	 * the associated DataSpace, mainly needed for accessing registers
	 */
	private final DataSpace dsp;
	
	/**
	 * @param dataspace
	 *        the DataSpace that the CalculatedAddress is to be associated with
	 */
	public CalculatedAddress(DataSpace dataspace) {
		dsp = dataspace;
	}
	
	private Address base = null;
	private Address index = null;
	private int scale = 0;
	private int displacement = 0;
	
	public HashSet<String> usedLabels;
	
	/**
	 * @param size
	 *        compute validity of the address assuming this is the size of the access
	 * @param executeNow
	 *        pass true here if the access will occur right now. Passing false will disable parts of the error
	 *        checking
	 *        as the concerned circumstances might have changed by the time the access occurs
	 * @return null if the address is valid, or a String with an error message
	 */
	public String isValid(int size, boolean executeNow) {
		if (!((index == null) || (scale == 1) || (scale == 2) || (scale == 4) || (scale == 8))) {
			return "Scale factor must be either 1, 2, 4, or 8.";
		}
		if (index == dsp.ESP) {
			return "ESP cannot be used as an index register.";
		}
		
		if (((base != null) && (base.size != 4)) || ((index != null) && (index.size != 4))) {
			return "Only 32bit registers are valid for address calculation.";
		}
		if ((calculateEffectiveAddress(executeNow) + size) > (dsp.getMEMSIZE() + dsp.getMemAddressStart())) {
			return "Memory address out of range";
		}
		if (calculateEffectiveAddress(executeNow) < dsp.getMemAddressStart()) {
			return "Memory address out of range";
		}
		return null;
	}
	
	/**
	 * calculates the effective address that this CalculatedAddress refers to
	 * 
	 * @param executeNow
	 *        you will most probably want to pass true here. Passing false is only usefull for validity
	 *        checking.
	 * @return the address of the memory cell that this CalculatedAddress actually refers to
	 */
	public int calculateEffectiveAddress(boolean executeNow) {
		if (executeNow) {
			int base_value = base != null ? (int) base.getShortcut() : 0;
			int index_value = index != null ? (int) index.getShortcut() : 0;
			return base_value + (index_value * scale) + displacement;
		} else {
			if ((base == null) && (index == null)) {
				return displacement;
			} else {
				return dsp.getMemAddressStart();
			}
		}
	}
	
	/**
	 * reads the CalculatedAddress from a String such as "[EAX+EBX*2+5]"
	 * 
	 * @param s
	 *        the String containing a representation of the address
	 * @return null if everything went fine, or a String containing an error message
	 */
	public String readFromString(String s) {
		if (Parser.pMemory.matcher(s).matches()) {
			if (s.indexOf("]") != (s.length() - 1)) {
				return "Malformed memory address";
			}
		}
		// label support
		usedLabels = new HashSet<String>();
		for (String label : dsp.getVariableList()) {
			Matcher m = Pattern.compile("[\\[\\-\\+*]" + label + "[\\]\\-\\+*]").matcher(s);
			int pos = 0;
			while (m.find(pos)) {
				s = s.replace(m.group(), m.group().replace(label, Integer.toString(dsp.getVariable(label))));
				pos = Math.max(m.end() - 1, 0);
				usedLabels.add(label);
			}
		}
		// constants support
		for (String constant : dsp.getConstantList()) {
			Matcher m = Pattern.compile("[\\[\\-\\+*]" + constant + "[\\]\\-\\+*]").matcher(s);
			int pos = 0;
			while (m.find(pos)) {
				s = s.replace(m.group(), m.group()
					.replace(constant, Long.toString(dsp.getConstant(constant))));
				pos = Math.max(m.end() - 1, 0);
				usedLabels.add(constant);
			}
		}
		// strip []'s
		s = s.substring(1, s.length() - 1);
		base = index = null;
		scale = displacement = 0;
		// displacement
		if (Parser.pDecimal.matcher(s).matches()) {
			displacement = Integer.valueOf(s);
			return null;
		}
		// base
		if (pRegisters.matcher(s).matches()) {
			base = dsp.getRegisterArgument(s);
			return null;
		}
		// base + displacement
		if (pBasePlusDisplacement.matcher(s).matches()) {
			base = dsp.getRegisterArgument(s.substring(0, s.indexOf("+")));
			displacement = Integer.valueOf(s.substring(s.indexOf("+") + 1));
			return null;
		}
		if (pBaseMinusDisplacement.matcher(s).matches()) {
			base = dsp.getRegisterArgument(s.substring(0, s.indexOf("-")));
			displacement = -Integer.valueOf(s.substring(s.indexOf("-") + 1));
			return null;
		}
		// (index*scale)
		if (pIndexScale.matcher(s).matches()) {
			index = dsp.getRegisterArgument(s.substring(0, s.indexOf("*")));
			scale = Integer.valueOf(s.substring(s.indexOf("*") + 1));
			return null;
		}
		// (index*scale) + displacement
		if (pIndexScalePlusDisplacement.matcher(s).matches()) {
			index = dsp.getRegisterArgument(s.substring(0, s.indexOf("*")));
			scale = Integer.valueOf(s.substring(s.indexOf("*") + 1, s.indexOf("+")));
			displacement = Integer.valueOf(s.substring(s.indexOf("+") + 1));
			return null;
		}
		if (pIndexScaleMinusDisplacement.matcher(s).matches()) {
			index = dsp.getRegisterArgument(s.substring(0, s.indexOf("*")));
			scale = Integer.valueOf(s.substring(s.indexOf("*") + 1, s.indexOf("-")));
			displacement = -Integer.valueOf(s.substring(s.indexOf("-") + 1));
			return null;
		}
		// base + index
		if (pBaseIndex.matcher(s).matches()) {
			s += "+0";
		}
		// base + index + displacement
		if (pBaseIndexPlusDisplacement.matcher(s).matches()) {
			base = dsp.getRegisterArgument(s.substring(0, s.indexOf("+")));
			index = dsp.getRegisterArgument(s.substring(s.indexOf("+") + 1, s.lastIndexOf("+")));
			displacement = Integer.valueOf(s.substring(s.lastIndexOf("+") + 1));
			scale = 1;
			return null;
		}
		if (pBaseIndexMinusDisplacement.matcher(s).matches()) {
			base = dsp.getRegisterArgument(s.substring(0, s.indexOf("+")));
			index = dsp.getRegisterArgument(s.substring(s.indexOf("+") + 1, s.lastIndexOf("-")));
			displacement = -Integer.valueOf(s.substring(s.lastIndexOf("-") + 1));
			scale = 1;
			return null;
		}
		// base + (index*scale)
		if (pBaseIndexScale.matcher(s).matches()) {
			s += "+0";
		}
		// base + (index*scale) + displacement
		if (pBaseIndexScalePlusDisplacement.matcher(s).matches()) {
			base = dsp.getRegisterArgument(s.substring(0, s.indexOf("+")));
			index = dsp.getRegisterArgument(s.substring(s.indexOf("+") + 1, s.indexOf("*")));
			scale = Integer.valueOf(s.substring(s.indexOf("*") + 1, s.lastIndexOf("+")));
			displacement = Integer.valueOf(s.substring(s.lastIndexOf("+") + 1));
			return null;
		}
		if (pBaseIndexScaleMinusDisplacement.matcher(s).matches()) {
			base = dsp.getRegisterArgument(s.substring(0, s.indexOf("+")));
			index = dsp.getRegisterArgument(s.substring(s.indexOf("+") + 1, s.indexOf("*")));
			scale = Integer.valueOf(s.substring(s.indexOf("*") + 1, s.lastIndexOf("-")));
			displacement = -Integer.valueOf(s.substring(s.lastIndexOf("-") + 1));
			return null;
		}
		// System.out.println("Malformed address: "+s);
		return "Malformed memory address";
	}
	
	/**
	 * a small helper function used above. It takes an array of Strings as input and produces a regular
	 * expression
	 * matching any of the input strings
	 * 
	 * @param input
	 *        the array of strings
	 * @return a regular expression matching any of the strings in the input array
	 */
	public static String createMatchingString(String[] input) {
		String result = "(";
		for (String word : input) {
			// Hack: EIP can't be accessed directly, so we don't count it as a register by excluding it
			// from strings generated by this function.
			if (word != "EIP") {
				if (!result.equals("(")) {
					result += "|";
				}
				result += "(" + word + ")";
			}
		}
		result += ")";
		return result;
	}
	
}
