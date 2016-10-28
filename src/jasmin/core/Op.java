package jasmin.core;

import java.util.ArrayList;

/**
 * @author Jakob Kummerow
 */

public class Op {
	
	public static final int ERROR = 0b1;
	public static final int NULL = 0b10;
	
	public static final int R8 = 0b100;
	public static final int R16 = 0b1000;
	public static final int R32 = 0b10000;
	public static final int R64 = 0b100000;
	public static final int REG = 0b111100;
	
	public static final int M8 = 0b1000000;
	public static final int M16 = 0b10000000;
	public static final int M32 = 0b100000000;
	public static final int M64 = 0b1000000000;
	public static final int MU = 0b10000000000;
	public static final int MEM = 0b11111000000;
	
	public static final int I8 = 0b100000000000;
	public static final int I16 = 0b1000000000000;
	public static final int I32 = 0b10000000000000;
	public static final int I64 = 0b100000000000000;
	public static final int IMM = 0b111100000000000;
	
	public static final int CHARS = 0b1000000000000000;
	public static final int STRING = 0b10000000000000000;
	public static final int LABEL = 0b100000000000000000;
	public static final int VARIABLE = 0b1000000000000000000;
	public static final int SIZEQUALI = 0b10000000000000000000;
	public static final int COMMA = 0b100000000000000000000;
	public static final int PREFIX = 0b1000000000000000000000;
	public static final int FPUREG = 0b110000000000000000000000;
	public static final int FPUST0 = 0b100000000000000000000000;
	public static final int FPUQUALI = 0b1000000000000000000000000;
	
	public static final int FLOAT = 0b10000000000000000000000000;
	public static final int CONST = 0b100000000000000000000000000;
	
	public static final int PARAM = 0b110110001111111111111111100;

	public static int getDefinition(int type, int size) {
		switch (type) {
			case MEM:
				switch (size) {
					case 1:
						return M8;
					case 2:
						return M16;
					case 4:
						return M32;
					case 8:
						return M64;
					case -1:
						return MU;
					default:
						return ERROR;
				}
			case REG:
				switch (size) {
					case 1:
						return R8;
					case 2:
						return R16;
					case 4:
						return R32;
					case 8:
						return R64;
					default:
						return ERROR;
				}
			case IMM:
				switch (size) {
					case 1:
						return I8;
					case 2:
						return I16;
					case 4:
						return I32;
					case 8:
						return I64;
					default:
						return ERROR;
				}
		}
		return ERROR;
	}
	
	public static boolean matches(int opA, int opB) {
		return ((opA & opB) != 0);
	}
	
	public static boolean sameSize(int opA, int opB) {
		if (((opA == R8) || (opA == M8) || (opA == I8) || (opA == MU)) &&
				((opB == R8) || (opB == M8) || (opB == I8) || (opB == MU))) {
			return true;
		}
		if (((opA == R16) || (opA == M16) || (opA == I16) || (opA == MU)) &&
				((opB == R16) || (opB == M16) || (opB == I16) || (opB == MU))) {
			return true;
		}
		if (((opA == R32) || (opA == M32) || (opA == I32) || (opA == MU)) &&
				((opB == R32) || (opB == M32) || (opB == I32) || (opB == MU))) {
			return true;
		}
		if (((opA == R64) || (opA == M64) || (opA == I64) || (opA == MU) || (opA == FPUREG)) &&
				((opB == R64) || (opB == M64) || (opB == I64) || (opB == MU) || (opB == FPUREG))) {
			return true;
		}
		return false;
	}
	
	public static String humanName(int op) {
		switch (op) {
			case M8:
				return "an 8-bit memory location";
			case M16:
				return "a 16-bit memory location";
			case M32:
				return "a 32-bit memory location";
			case M64:
				return "a 64-bit memory location";
			case MU:
				return "a memory location of undefined size";
			case MEM:
				return "a memory location";
			case R8:
				return "an 8-bit register";
			case R16:
				return "a 16-bit register";
			case R32:
				return "a 32-bit register";
			case R64:
				return "a 64-bit register";
			case REG:
				return "a register";
			case I8:
				return "an 8-bit immediate";
			case I16:
				return "a 16-bit immediate";
			case I32:
				return "a 32-bit immediate";
			case I64:
				return "a 64-bit immediate";
			case IMM:
				return "an immediate";
			case LABEL:
				return "a label";
			case VARIABLE:
				return "a variable";
			case CONST:
				return "a constant";
			case SIZEQUALI:
				return "a size qualifier";
			case PREFIX:
				return "a prefix";
			case CHARS:
				return "a short string";
			case STRING:
				return "a string";
			case FPUREG:
				return "an FPU register";
			case FPUST0:
				return "ST0";
			case FPUQUALI:
				return "an FPU qualifier";
			case NULL:
				return "empty";
			case ERROR:
				return "ERROR!";
			case FLOAT:
				return "a floating-point constant";
		}
		return "no human name defined for type " + op;
	}
	
	public static String[] humanNamesArray(int ops) {
		ArrayList<String> list = new ArrayList<>();
		String reg = "";
		String or;
		boolean done;
		if ((ops & REG) == REG) {
			list.add(humanName(REG));
		} else if (matches(ops, REG)) {
			or = " or ";
			done = false;
			if (matches(ops, R64)) {
				reg = "64bit ";
				done = true;
			}
			if (matches(ops, R32)) {
				if (done) {
					reg = "32bit" + (or.equals("") ? ", " : or) + reg;
					or = "";
				} else {
					reg = "32bit ";
					done = true;
				}
			}
			if (matches(ops, R16)) {
				if (done) {
					reg = "16bit" + (or.equals("") ? ", " : or) + reg;
					or = "";
				} else {
					reg = "16bit ";
					done = true;
				}
			}
			if (matches(ops, R8)) {
				if (done) {
					reg = "an 8bit" + (or.equals("") ? ", " : or) + reg;
					or = "";
				} else {
					reg = "an 8bit ";
					done = true;
				}
			} else {
				reg = "a " + reg;
			}
			reg += "register";
			list.add(reg);
		}
		if ((ops & MEM) == MEM) {
			list.add(humanName(MEM));
		} else if (matches(ops, MEM)) {
			or = " or ";
			done = false;
			if (matches(ops, M64)) {
				reg = "64bit ";
				done = true;
			}
			if (matches(ops, M32)) {
				if (done) {
					reg = "32bit" + (or.equals("") ? ", " : or) + reg;
					or = "";
				} else {
					reg = "32bit ";
					done = true;
				}
			}
			if (matches(ops, M16)) {
				if (done) {
					reg = "16bit" + (or.equals("") ? ", " : or) + reg;
					or = "";
				} else {
					reg = "16bit ";
					done = true;
				}
			}
			if (matches(ops, M8)) {
				if (done) {
					reg = "an 8bit" + (or.equals("") ? ", " : or) + reg;
					or = "";
				} else {
					reg = "an 8bit ";
					done = true;
				}
			} else {
				reg = "a " + reg;
			}
			reg += "memory location";
			list.add(reg);
		}
		if ((ops & IMM) == IMM) {
			list.add(humanName(IMM));
		} else if (matches(ops, IMM)) {
			or = " or ";
			done = false;
			if (matches(ops, I64)) {
				reg = "64bit ";
				done = true;
			}
			if (matches(ops, I32)) {
				if (done) {
					reg = "32bit" + (or.equals("") ? ", " : or) + reg;
					or = "";
				} else {
					reg = "32bit ";
					done = true;
				}
			}
			if (matches(ops, I16)) {
				if (done) {
					reg = "16bit" + (or.equals("") ? ", " : or) + reg;
					or = "";
				} else {
					reg = "16bit ";
					done = true;
				}
			}
			if (matches(ops, I8)) {
				if (done) {
					reg = "an 8bit" + (or.equals("") ? ", " : or) + reg;
					or = "";
				} else {
					reg = "an 8bit ";
					done = true;
				}
			} else {
				reg = "a " + reg;
			}
			reg += "immediate";
			list.add(reg);
		}
		if (matches(ops, LABEL)) {
			list.add(humanName(LABEL));
		}
		if (matches(ops, SIZEQUALI)) {
			list.add(humanName(SIZEQUALI));
		}
		if (matches(ops, PREFIX)) {
			list.add(humanName(PREFIX));
		}
		if (matches(ops, CHARS)) {
			list.add(humanName(CHARS));
		} else if (matches(ops, STRING)) {
			list.add(humanName(STRING));
		}
		if (matches(ops, FPUREG)) {
			list.add(humanName(FPUREG));
		} else if (matches(ops, FPUST0)) {
			list.add(humanName(FPUST0));
		}
		if (matches(ops, FPUQUALI)) {
			list.add(humanName(FPUQUALI));
		}
		if (matches(ops, FLOAT)) {
			list.add(humanName(FLOAT));
		}
		if (matches(ops, NULL)) {
			list.add(humanName(NULL));
		}
		String[] result = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			result[i] = list.get(i);
		}
		return result;
	}
	
	/**
	 * Splits a String into an array of smaller Strings of the specified size. Both the input String
	 * and each of the output Strings must be / will be surrounded by 'apostrophes'
	 * 
	 * @param longString
	 *        the input String
	 * @param chunksize
	 *        the desired size of the output Strings
	 * @return an array of Strings into which the input String was split
	 */
	public static String[] splitLongString(String longString, int chunksize) {
		longString = longString.substring(1, longString.length() - 1);
		int chunks = (longString.length() / chunksize) + (longString.length() % chunksize == 0 ? 0 : 1);
		String[] result = new String[chunks];
		int index = 0;
		while (longString.length() > chunksize) {
			result[index] = "'" + longString.substring(0, chunksize) + "'";
			longString = longString.substring(chunksize);
			index++;
		}
		result[index] = "'" + longString + "'";
		return result;
	}
}
