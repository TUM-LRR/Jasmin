package jasmin.core;

import java.util.ArrayList;

/**
 * @author Jakob Kummerow
 */

public class Op {
	
	public static final int ERROR = Integer.parseInt("00000000000000000000000000000001", 2);
	public static final int NULL = Integer.parseInt("00000000000000000000000000000010", 2);
	
	public static final int R8 = Integer.parseInt("00000000000000000000000000000100", 2);
	public static final int R16 = Integer.parseInt("00000000000000000000000000001000", 2);
	public static final int R32 = Integer.parseInt("00000000000000000000000000010000", 2);
	public static final int R64 = Integer.parseInt("00000000000000000000000000100000", 2);
	public static final int REG = Integer.parseInt("00000000000000000000000000111100", 2);
	
	public static final int M8 = Integer.parseInt("00000000000000000000000001000000", 2);
	public static final int M16 = Integer.parseInt("00000000000000000000000010000000", 2);
	public static final int M32 = Integer.parseInt("00000000000000000000000100000000", 2);
	public static final int M64 = Integer.parseInt("00000000000000000000001000000000", 2);
	public static final int MU = Integer.parseInt("00000000000000000000010000000000", 2);
	public static final int MEM = Integer.parseInt("00000000000000000000011111000000", 2);
	
	public static final int I8 = Integer.parseInt("00000000000000000000100000000000", 2);
	public static final int I16 = Integer.parseInt("00000000000000000001000000000000", 2);
	public static final int I32 = Integer.parseInt("00000000000000000010000000000000", 2);
	public static final int I64 = Integer.parseInt("00000000000000000100000000000000", 2);
	public static final int IMM = Integer.parseInt("00000000000000000111100000000000", 2);
	
	public static final int CHARS = Integer.parseInt("00000000000000001000000000000000", 2);
	public static final int STRING = Integer.parseInt("00000000000000010000000000000000", 2);
	public static final int LABEL = Integer.parseInt("00000000000000100000000000000000", 2);
	public static final int VARIABLE = Integer.parseInt("00000000000001000000000000000000", 2);
	public static final int SIZEQUALI = Integer.parseInt("00000000000010000000000000000000", 2);
	public static final int COMMA = Integer.parseInt("00000000000100000000000000000000", 2);
	public static final int PREFIX = Integer.parseInt("00000000001000000000000000000000", 2);
	public static final int FPUREG = Integer.parseInt("00000000110000000000000000000000", 2);
	public static final int FPUST0 = Integer.parseInt("00000000100000000000000000000000", 2);
	public static final int FPUQUALI = Integer.parseInt("00000001000000000000000000000000", 2);
	
	public static final int FLOAT = Integer.parseInt("00000010000000000000000000000000", 2);
	public static final int CONST = Integer.parseInt("00000100000000000000000000000000", 2);
	
	public static final int PARAM = Integer.parseInt("00000110110001111111111111111100", 2);
	
	public static final int getDefinition(int type, int size) {
		if (type == MEM) {
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
		}
		if (type == REG) {
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
		}
		if (type == IMM) {
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
	
	public static final boolean matches(int opA, int opB) {
		return ((opA & opB) != 0);
	}
	
	public static final boolean sameSize(int opA, int opB) {
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
	
	public static final String humanName(int op) {
		if (op == M8) {
			return "an 8-bit memory location";
		} else if (op == M16) {
			return "a 16-bit memory location";
		} else if (op == M32) {
			return "a 32-bit memory location";
		} else if (op == M64) {
			return "a 64-bit memory location";
		} else if (op == MU) {
			return "a memory location of undefined size";
		} else if (op == MEM) {
			return "a memory location";
		} else if (op == R8) {
			return "an 8-bit register";
		} else if (op == R16) {
			return "a 16-bit register";
		} else if (op == R32) {
			return "a 32-bit register";
		} else if (op == R64) {
			return "a 64-bit register";
		} else if (op == REG) {
			return "a register";
		} else if (op == I8) {
			return "an 8-bit immediate";
		} else if (op == I16) {
			return "a 16-bit immediate";
		} else if (op == I32) {
			return "a 32-bit immediate";
		} else if (op == I64) {
			return "a 64-bit immediate";
		} else if (op == IMM) {
			return "an immediate";
		} else if (op == LABEL) {
			return "a label";
		} else if (op == VARIABLE) {
			return "a variable";
		} else if (op == CONST) {
			return "a constant";
		} else if (op == SIZEQUALI) {
			return "a size qualifier";
		} else if (op == PREFIX) {
			return "a prefix";
		} else if (op == CHARS) {
			return "a short string";
		} else if (op == STRING) {
			return "a string";
		} else if (op == FPUREG) {
			return "an FPU register";
		} else if (op == FPUST0) {
			return "ST0";
		} else if (op == FPUQUALI) {
			return "an FPU qualifier";
		} else if (op == NULL) {
			return "empty";
		} else if (op == ERROR) {
			return "ERROR!";
		} else if (op == FLOAT) {
			return "a floating-point constant";
		}
		return "no human name defined for type " + op;
	}
	
	public static final String[] humanNamesArray(int ops) {
		ArrayList<String> list = new ArrayList<String>();
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
	public static final String[] splitLongString(String longString, int chunksize) {
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
