package jasmin.core;

/**
 * The super class for all commands.
 * 
 * @author Jakob Kummerow
 */
public abstract class JasminCommand {
	
	/**
	 * the default operation size of the command if no explicit size is specified, e.g. "mov [0], 1" treats
	 * the 1 as a
	 * 32bit-value and overwrites memory cells 0-3
	 * 
	 * @param mnemo
	 *        the mnemo of the command whose default operation size is to be returned
	 * @return the default operation size of the command
	 */
	public int defaultSize(String mnemo) {
		return 4;
	}
	
	/**
	 * should the first bit be treated as a sign when this command accesses data? Note that this is irrelevant
	 * in some
	 * cases such as ADD
	 * 
	 * @return true if the command wants the first bit interpreted as a sign
	 */
	public boolean signed() {
		return false;
	}
	
	/**
	 * returns true if the indicated bit is set (1) or false if it is unset (0)
	 * 
	 * @param word
	 *        the number/bitstring
	 * @param position
	 *        the number of the bit of interest, counted from the right starting with 0
	 * @return true if the bit is set, false otherwise
	 */
	protected boolean getBit(long word, long position) {
		return (word & (1L << position)) != 0;
	}
	
	/**
	 * set the specified bit in the specified word to 1 if the passed boolean is true, 0 otherwise
	 * 
	 * @param word
	 *        the number/bitstring
	 * @param flag
	 *        the desired value of the bit
	 * @param position
	 *        the number of the bit of interest
	 * @return the updated input number/bitstring
	 */
	protected long setBit(long word, boolean flag, long position) {
		if (!flag) {
			return word & (~(1L << position));
		}
		return word | (1L << position);
	}
	
	/**
	 * [to be implemented]
	 * 
	 * @return a list of mnemonics the commands reacts on
	 */
	abstract public String[] getID();
	
	/**
	 * [to be implemented] check whether a given set of parameters is OK
	 * 
	 * @param p
	 *        the parameters
	 * @return a <code>ParseError</code>, if anything was wrong; <code>null</code> otherwise
	 */
	abstract public ParseError validate(Parameters p);
	
	/**
	 * [to be implemented] the execution of the command
	 * 
	 * @param p
	 *        the parameters
	 */
	abstract public void execute(Parameters p);
	
	protected DataSpace dataspace;
	
	/**
	 * sets which data space the command works on;
	 * 
	 * @param newDataSpace
	 */
	public void setDataSpace(DataSpace newDataSpace) {
		dataspace = newDataSpace;
	}
	
	/**
	 * override the default maximum of one memory access
	 * 
	 * @param mnemo
	 *        the mnemo of the command in question
	 */
	public boolean overrideMaxMemAccess(String mnemo) {
		return false;
	}
	
	protected static int CF = 1;
	protected static int OF = 2;
	protected static int SF = 4;
	protected static int ZF = 8;
	protected static int PF = 16;
	protected static int AF = 32;
	
	/**
	 * a routine which automatically determines which flags are to be set
	 * 
	 * @param p
	 *        the command's Parameters object
	 * @param flags
	 *        bitmask of the flags to set
	 */
	protected void setFlags(Parameters p, int flags) {
		if ((flags & ZF) == ZF) {
			if ((p.result & ((((long) 1) << (p.size * 8)) - 1)) == 0) {
				dataspace.fZero = true;
			} else {
				dataspace.fZero = false;
			}
		}
		if ((flags & SF) == SF) {
			if ((p.result >> p.size * 8 - 1 & 1) == 1) {
				dataspace.fSign = true;
			} else {
				dataspace.fSign = false;
			}
		}
		if ((flags & PF) == PF) {
			// parity flag = even number of 1s in lowest byte?
			long temp = (p.result & 1) + (p.result >> 1 & 1) + (p.result >> 2 & 1) + (p.result >> 3 & 1)
				+ (p.result >> 4 & 1)
				+ (p.result >> 5 & 1) + (p.result >> 6 & 1) + (p.result >> 7 & 1);
			if (temp % 2 == 0) {
				dataspace.fParity = true;
			} else {
				dataspace.fParity = false;
			}
		}
		if ((flags & CF) == CF) {
			// carry flag = (n+1)th bit
			if (((p.result >> p.size * 8) & 1) == 1) {
				dataspace.fCarry = true;
			} else {
				dataspace.fCarry = false;
			}
		}
		if ((flags & OF) == OF) {
			// overflow flag = incorrect sign
			boolean aSign = ((p.a >> p.size * 8 - 1 & 1) == 1);
			boolean bSign = ((p.b >> p.size * 8 - 1 & 1) == 1);
			boolean resultSign = ((p.result >> p.size * 8 - 1 & 1) == 1);
			if ((aSign == bSign) && (resultSign != aSign)) {
				dataspace.fOverflow = true;
			} else {
				dataspace.fOverflow = false;
			}
		}
		if ((flags & AF) == AF) {
			// adjust / auxiliary carry flag = carry of bit 3, used for BCD only
			
			// This line is just plain wrong:
			// if (((p.result >> 4) & 1) == 1) {
			
			// This line is better, but fails in a few cases, e.g.:
			// MOV AL, 80h; SUB AL, 18h
			// and that can't be fixed easily because the information just isn't there due to
			// SUB inverting the second argument and then adding it.
			if (((p.result >> 4) & 1) != ((((p.a >> 4) & 1) + ((p.b >> 4) & 1)) & 1)) {
				dataspace.fAuxiliary = true;
			} else {
				dataspace.fAuxiliary = false;
			}
		}
	}
	
	/**
	 * for things like conditional jump (JCC)
	 * 
	 * @param cc
	 *        the postfix for the condition
	 * @return true if the condition is met
	 */
	protected boolean testCC(String cc) {
		if (cc.equals("O")) {
			return dataspace.fOverflow;
		}
		if (cc.equals("NO")) {
			return !dataspace.fOverflow;
		}
		if (cc.equals("C") || cc.equals("B") || cc.equals("NAE")) {
			return dataspace.fCarry;
		}
		if (cc.equals("NC") || cc.equals("NB") || cc.equals("AE")) {
			return !dataspace.fCarry;
		}
		if (cc.equals("E") || cc.equals("Z")) {
			return dataspace.fZero;
		}
		if (cc.equals("NE") || cc.equals("NZ")) {
			return !dataspace.fZero;
		}
		if (cc.equals("BE") || cc.equals("NA")) {
			return (dataspace.fCarry || dataspace.fZero);
		}
		if (cc.equals("NBE") || cc.equals("A")) {
			return !(dataspace.fCarry || dataspace.fZero);
		}
		if (cc.equals("S")) {
			return dataspace.fSign;
		}
		if (cc.equals("NS")) {
			return !dataspace.fSign;
		}
		if (cc.equals("P") || cc.equals("PE")) {
			return dataspace.fParity;
		}
		if (cc.equals("NP") || cc.equals("PO")) {
			return !dataspace.fParity;
		}
		if (cc.equals("L") || cc.equals("NGE")) {
			return (dataspace.fSign ^ dataspace.fOverflow);
		} // "^" = "xor" ( "!=" would work as well)
		if (cc.equals("NL") || cc.equals("GE")) {
			return !(dataspace.fSign ^ dataspace.fOverflow);
		}
		if (cc.equals("LE") || cc.equals("NG")) {
			return ((dataspace.fSign ^ dataspace.fOverflow) || dataspace.fZero);
		}
		if (cc.equals("NLE") || cc.equals("G")) {
			return !((dataspace.fSign ^ dataspace.fOverflow) || dataspace.fZero);
		}
		if (cc.equals("CXZ")) {
			return (dataspace.CX.getShortcut() == 0);
		}
		if (cc.equals("ECXZ")) {
			return (dataspace.ECX.getShortcut() == 0);
		}
		
		return false;
	}
	
}