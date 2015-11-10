package jasmin.core;

import java.util.ArrayList;

/**
 * Encapsulates a set of arguments, in order to pass them from the Parser to the commands.
 * Also contains several often-used methods concerning fetching, storing and checking
 * of arguments.
 * 
 * @author Jakob Kummerow
 */
public class Parameters {
	
	private FullArgument[] argument;
	public int numArguments;
	public int size;
	public int defaultSize;
	public boolean signed;
	public String wholeLine;
	public String mnemo;
	public long a, b, c, result;
	public String label;
	
	public double fa, fb;
	
	protected DataSpace dsp;
	protected Parser parser;
	
	public Parameters(DataSpace dataSpace, Parser p) {
		dsp = dataSpace;
		parser = p;
	}
	
	private static FullArgument noArgument = new FullArgument("", "", 0, Op.NULL, -1, false, null);
	
	public FullArgument argument(int argumentIndex) {
		if (argumentIndex < numArguments) {
			return argument[argumentIndex];
		} else {
			return noArgument;
		}
	}
	
	public ParseError consistentSizes() {
		return ((size(0) & size(1) & size(2)) != 0) ? null : new ParseError(wholeLine, argument(1),
			"Size mismatch");
	}
	
	public void set(String wholeCodeLine, String command, ArrayList<FullArgument> args,
			int defaultOperationSize,
			boolean signedAccessToRegMem) {
		wholeLine = wholeCodeLine;
		mnemo = command;
		numArguments = args.size();
		argument = new FullArgument[(numArguments < 2 ? 2 : numArguments)];
		for (int i = 0; i < numArguments; i++) {
			argument[i] = args.get(i);
		}
		defaultSize = defaultOperationSize;
		signed = signedAccessToRegMem;
		size = Math.max(size(0), size(1));
		if (size == -1) {
			size = defaultSize;
		}
		for (int i = 0; i < numArguments; i++) {
			if (Op.matches(argument[i].address.type, Op.MU)) {
				argument[i].address.size = size;
			} else if (Op.matches(argument[i].address.type, Op.FLOAT) && (argument[i].address.size == -1)) {
				argument[i].address.size = size;
			}
			if (argument[i].address.dynamic) {
				argument[i].calculateAddress(dsp);
			} else {
				argument[i].address.value = dsp.getInitial(argument[i], signed);
			}
		}
	}
	
	public long get(Address a) {
		if (a.dynamic) {
			if ((a.type & Op.REG) != 0) {
				long value = a.getShortcut();
				if (signed) {
					if (a.size == 1) {
						value = (byte) value;
					} else if (a.size == 2) {
						value = (short) value;
					} else if (a.size == 4) {
						value = (int) value;
					}
				}
				return value;
			}
			a.value = dsp.getUpdate(a, signed);
		}
		return a.value;
	}
	
	public long get(int argumentIndex) {
		FullArgument arg = argument[argumentIndex];
		if (arg == null) {
			return 0;
		}
		// take the shortcut as early as possible!
		if ((!signed) && ((arg.address.type & Op.REG) != 0)) {
			return arg.address.getShortcut();
		}
		if (arg.cAddress != null) {
			arg.address.address = arg.cAddress.calculateEffectiveAddress(true);
		}
		return get(arg.address);
	}
	
	public double getF(int argumentIndex, int dataType) {
		long a = get(argumentIndex);
		if (Op.matches(argument(argumentIndex).address.type, Op.MEM)) {
			if (dataType == Fpu.FLOAT) {
				switch (size(argumentIndex)) {
				case 4: {
					return Float.intBitsToFloat((int) a);
				}
				case 8: {
					return Double.longBitsToDouble(a);
				}
				}
			}
			if (dataType == Fpu.INTEGER) {
				return Fpu.doubleFromLong(a);
			}
			if (dataType == Fpu.PACKEDBCD) {
				return Fpu.doubleFromPackedBCD(a);
			}
		}
		if (Op.matches(argument(argumentIndex).address.type, Op.FPUREG)) {
			return Double.longBitsToDouble(a);
		}
		return 0.0;
	}
	
	public void put(Address a, long value, MemCellInfo memCellInfo) {
		if (a.dynamic) {
			dsp.put(value, a, memCellInfo);
		}
	}
	
	public void put(int argumentIndex, long value, MemCellInfo memCellInfo) {
		FullArgument arg = argument(argumentIndex);
		if (arg.cAddress != null) {
			arg.address.address = arg.cAddress.calculateEffectiveAddress(true);
		}
		put(arg.address, value, memCellInfo);
	}
	
	public void putF(int argumentIndex, double value, int dataType) {
		if (Op.matches(argument(argumentIndex).address.type, Op.MEM)) {
			if (dataType == Fpu.FLOAT) {
				long longvalue = 0;
				switch (size(argumentIndex)) {
				case 4: {
					longvalue = Float.floatToRawIntBits((float) value);
					break;
				}
				case 8: {
					longvalue = Double.doubleToRawLongBits(value);
					break;
				}
				}
				dsp.put(longvalue, argument[argumentIndex].address, null);
			} else if (dataType == Fpu.PACKEDBCD) {
				dsp.put(Fpu.packedBCDFromDouble(value), argument[argumentIndex].address, null);
			} else if (dataType == Fpu.INTEGER) {
				dsp.put(Fpu.longFromDouble(value), argument[argumentIndex].address, null);
			}
		} else if (Op.matches(argument(argumentIndex).address.type, Op.FPUREG)) {
			dsp.put(Double.doubleToRawLongBits(value), argument[argumentIndex].address, null);
		}
	}
	
	public void push(Address a) {
		long pushvalue = get(a);
		// decrease stack pointer
		dsp.put(dsp.ESP.getShortcut() - a.size, dsp.ESP, null);
		// write new value to stack
		dsp.put(pushvalue, dsp.Stack(a.size), dsp.memInfo(a));
	}
	
	public void pop(Address a) {
		long newESP = dsp.ESP.getShortcut() + a.size;
		if (newESP > dsp.EBP.getShortcut()) {
			dsp.setAddressOutOfRange();
			return;
		}
		// get value from stack and write it to destination
		Address stack = dsp.Stack(a.size);
		dsp.put(dsp.getUpdate(stack, false), a, dsp.memInfo(stack));
		// increase stack pointer
		dsp.put(newESP, dsp.ESP, null);
	}
	
	public long pop(int size) {
		long newESP = dsp.ESP.getShortcut() + size;
		if (newESP > dsp.EBP.getShortcut()) {
			return 0;
		}
		// get current stack address
		Address stack = dsp.Stack(size);
		// increase stack pointer
		dsp.put(newESP, dsp.ESP, null);
		// get the value from the sack and return it
		return dsp.getUpdate(stack, false);
	}
	
	public String arg(int argumentIndex) {
		return argument(argumentIndex).arg;
	}
	
	public String originalarg(int argumentIndex) {
		return argument(argumentIndex).original;
	}
	
	public int startpos(int argumentIndex) {
		return argument(argumentIndex).startPos;
	}
	
	public int size(int argumentIndex) {
		return argument(argumentIndex).address.size;
	}
	
	public int type(int argumentIndex) {
		return argument(argumentIndex).address.type;
	}
	
	/**
	 * @return null if the destination operand (the first one) is correct, a ParseError otherwise
	 */
	public ParseError numericDestOK() {
		if (!Op.matches(type(0), Op.REG | Op.MEM)) {
			return new ParseError(wholeLine, argument(0),
				"Invalid parameter. Must specify a register or a memory address as destination.");
		}
		return null;
	}
	
	/**
	 * @return null if the source operand (the second one) is correct, a ParseError otherwise
	 */
	public ParseError numericSrcOK() {
		
		boolean immediateDefaultSizeFlag = false;
		
		if ((size(0) & size(1) & size(2)) == 0) {
			return new ParseError(wholeLine, argument(1), "Size mismatch");
		}
		
		if (!Op.matches(type(1), Op.REG | Op.MEM | Op.IMM | Op.CHARS | Op.VARIABLE | Op.LABEL | Op.CONST)) {
			return new ParseError(wholeLine, argument(1),
				"Invalid parameter. Must specify a register, a memory address or an immediate as operand.");
		}
		
		if (Op.matches(type(1), Op.IMM) && (size(1) == -1)) {
			argument[1].address.size = parser.getOperandSize(arg(1), type(1));
			immediateDefaultSizeFlag = true;
		}
		
		if ((size(0) >= 0) && (size(1) > size(0))) {
			return new ParseError(wholeLine, argument(1), "Operand too large, does not fit into destination.");
		}
		if (Op.matches(type(0), Op.REG) && Op.matches(type(1), Op.REG) && (type(0) != type(1))) {
			return new ParseError(wholeLine, argument(1), "Register sizes mismatch.");
		}
		if (immediateDefaultSizeFlag) {
			argument(1).address.size = -1;
		}
		return null;
	}
	
	public ParseError firstRegMemSecondReg() {
		if (!Op.matches(type(0), Op.REG | Op.MEM)) {
			return new ParseError(wholeLine, argument(0),
				"First argument must be a register or memory address");
		}
		if (!Op.matches(type(1), Op.REG)) {
			return new ParseError(wholeLine, argument(1), "Second argument must be a register");
		}
		return numericSrcOK();
	}
	
	/**
	 * prepare the first two operands as integers
	 */
	public void prepareAB() {
		a = get(0);
		b = get(1);
	}
	
	public ParseError validate(int operandNumber, int allowedTypes) {
		boolean matched = false;
		if (Op.matches(type(operandNumber), allowedTypes)) {
			matched = true;
		}
		if (!matched) {
			return new ParseError(wholeLine, argument(operandNumber), errorMsg(allowedTypes));
		}
		return null;
	}
	
	public ParseError validateAll(int allowedTypes) {
		for (int i = 0; i < numArguments; i++) {
			ParseError e = validate(i, allowedTypes);
			if (e != null) {
				return e;
			}
		}
		return null;
	}
	
	public ParseError validateAllSizes(int minSize, int maxSize) {
		int size;
		for (int i = 0; i < numArguments; i++) {
			if (!argument(i).sizeExplicit) {
				size = parser.getOperandSize(arg(i), type(i));
			} else {
				size = size(i);
			}
			if (!Op.matches(type(i), Op.CHARS | Op.STRING)) {
				if (size < minSize) {
					if (Op.matches(type(i), Op.IMM | Op.MU) && !argument(i).sizeExplicit) {
						argument(i).address.size = minSize;
						// Sign-extend IMMs if necessary:
						argument[i].address.value = dsp.getInitial(argument(i), signed);
					} else {
						return new ParseError(wholeLine, argument[i], "Operand must be at least " + minSize
							+ " byte" + (minSize != 1 ? "s" : "") + " large");
					}
				}
				if (size > maxSize) {
					return new ParseError(wholeLine, argument[i], "Operand must not be larger than "
						+ maxSize + " byte" + (maxSize != 1 ? "s" : ""));
				}
				this.size = Math.max(this.size, size(i));
				this.size = Math.max(this.size, size);
			}
		}
		return null;
	}
	
	public static String errorMsg(int allowedTypes) {
		String[] array = Op.humanNamesArray(allowedTypes);
		int number = array.length;
		if (number == 0) {
			return "Invalid operand (no description available)";
		}
		String msg = "Operand must be ";
		for (int i = 0; i < (number - 1); i++) {
			msg += array[i] + ", ";
			if (i == (number - 2)) {
				msg += "or ";
			}
		}
		msg += array[number - 1] + ". ";
		return msg;
	}
	
	/**
	 * checks whether the FPU register ST0 is contained in the arguments list
	 * 
	 * @return true if ST0 is contained in the arguments list
	 */
	public ParseError st0contained() {
		if (arg(0).equals("ST0") || arg(1).equals("ST0")) {
			return null;
		} else {
			return new ParseError(wholeLine, argument(1), "One of the arguments must be ST0");
		}
	}
	
	private void shiftParam(int from, int to) {
		argument[to] = argument[from];
		if (numArguments <= to) {
			numArguments = to + 1;
		}
	}
	
	/**
	 * adds implicit (not specified) arguments for an FPU command to the arguments list
	 */
	public void normalizeParameters() {
		// no args -> add ST1 as destination and ST0 as source
		if (Op.matches(type(0), Op.NULL)) {
			argument[0] = new FullArgument(Op.FPUREG, 8, 1);
			argument[1] = new FullArgument(Op.FPUREG, 8, 0);
			return;
		}
		// only one arg -> move it to source and insert ST0 as destination
		if (Op.matches(type(1), Op.NULL)) {
			shiftParam(0, 1);
			argument[0] = new FullArgument(Op.FPUREG, 8, 0);
			return;
		}
		// "to" qualifier -> move arg to destination and add ST0 as source
		if (Op.matches(type(0), Op.FPUQUALI)) {
			shiftParam(1, 0);
			argument[1] = new FullArgument(Op.FPUREG, 8, 0);
		}
	}
	
	/**
	 * adds implicit (not specified) arguments for a popping FPU command to the arguments list
	 */
	public void normalizePopParameters() {
		// no args -> add ST1 as destination and ST0 as source
		if (Op.matches(type(0), Op.NULL)) {
			argument[0] = new FullArgument(Op.FPUREG, 8, 1);
			argument[1] = new FullArgument(Op.FPUREG, 8, 0);
			if (numArguments < 2) {
				numArguments = 2;
			}
			return;
		}
		// only one arg -> add ST0 as source
		if (Op.matches(type(1), Op.NULL)) {
			argument[1] = new FullArgument(Op.FPUREG, 8, 0);
			if (numArguments < 2) {
				numArguments = 2;
			}
		}
	}
	
	// DIAGNOSTICS
	
	/**
	 * prints the contents of this Parameters object to the standard output
	 */
	public void print() {
		for (int i = 0; i < numArguments; i++) {
			System.out.println("arg: " + arg(i) + " type: " + Op.humanName(type(i)) + " size: " + size(i));
		}
	}
	
}
