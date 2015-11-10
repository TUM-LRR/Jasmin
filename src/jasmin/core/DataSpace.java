package jasmin.core;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.*;

/**
 * @author Yang Guo, Jakob Kummerow
 */

public class DataSpace {
	
	// //////////////////////////////////////
	// "CONSTANTS"
	
	/**
	 * guess what: the FPU (floating point unit)! ;-)
	 */
	public Fpu fpu;
	
	/**
	 * the Parser working with this DataSpace. Needed to call some of its functions for error checking.
	 */
	public Parser parser;
	
	/**
	 * ! always use this to link the DataSpace to a parser!
	 * 
	 * @param p
	 *        the parser to assign
	 */
	public void setParser(Parser p) {
		parser = p;
	}
	
	/**
	 * names of the registers
	 */
	private static String[] registers = { "EAX", "AX", "AL", "AH", "EBX", "BX", "BL", "BH", "ECX", "CX",
		"CL", "CH", "EDX", "DX",
		"DL", "DH", "ESI", "SI", "EDI", "DI", "ESP", "SP", "EBP", "BP", "EIP" };
	
	/**
	 * @return an array containing the mnemonics of the registers
	 */
	public static String[] getRegisterList() {
		return registers;
	}
	
	/**
	 * SmallArguments referring to the registers
	 */
	public Address EAX, AX, AH, AL, EBX, BX, BH, BL, ECX, CX, CH, CL, EDX, DX, DH, DL, ESI, SI, EDI,
			DI, ESP, SP, EBP, BP,
			EIP;
	
	private RegisterSet[] registerSets;
	
	/**
	 * @param size
	 *        1, 2 or 4 depending on the mode of the memory table
	 * @return the address where the stackpointer points to. for highlighting in the GUI
	 */
	public Address Stack(int size) {
		return new Address(Op.MEM, size, (int) ESP.getShortcut());
	}
	
	/**
	 * the supported repeat prefixes
	 */
	private static String[] prefixes = { "REP", "REPE", "REPZ", "REPNE", "REPNZ" };
	
	private static String prefixesMatchingString = CalculatedAddress.createMatchingString(prefixes);
	public static Pattern prefixesMatchingPattern = Pattern.compile(prefixesMatchingString);
	
	public static final int BIN = 2;
	public static final int HEX = 16;
	public static final int SIGNED = -10;
	public static final int UNSIGNED = 10;
	
	// //////////////////////////////////////
	// VARIABLES
	
	/**
	 * size of the memory
	 */
	private int MEMSIZE;
	
	/**
	 * @return maximum size of the memory
	 */
	public int getMEMSIZE() {
		return MEMSIZE;
	}
	
	private int memAddressStart = 0x10000; // appropriate custom value is assigned in constructor
	
	public int getMemAddressStart() {
		return memAddressStart;
	}
	
	/**
	 * space for memory and register
	 */
	private Memory memory;
	private Registers reg;
	private TreeMap<Integer, MemCellInfo> memInfo;
	private TreeMap<Integer, MemCellInfo> regInfo;
	private int nextReservableAddress = memAddressStart; // appropriate custom value is assigned in
	// constructor
	private boolean addressOutOfRange = false;
	
	/**
	 * Checks whether an access to an invalid (out-of-range) address occured. The corresponding flag must be
	 * reset
	 * manually afterwards!
	 * 
	 * @return true if there was an access to an invalid address
	 */
	public boolean addressOutOfRange() {
		return addressOutOfRange;
	}
	
	/**
	 * sets the flag indicating that an invalid memory address was accessed
	 */
	public void setAddressOutOfRange() {
		addressOutOfRange = true;
	}
	
	/**
	 * resets the flag indicating that an invalid memory address was accessed
	 */
	public void clearAddressOutOfRange() {
		addressOutOfRange = false;
	}
	
	/**
	 * status flags
	 */
	public boolean fCarry, fOverflow, fSign, fZero, fParity, fAuxiliary, fTrap, fDirection;
	
	/**
	 * size of the registers
	 */
	private int REGSIZE = 36;
	
	/**
	 * Hashtable: register mnemo -> register address
	 */
	private Hashtable<String, Address> regtable;
	
	/**
	 * set Instruction Pointer, which in this case is not an Address, but rather a line number for the
	 * interpreter
	 * 
	 * @param ip
	 *        the new value of the instruction pointer
	 */
	public void setInstructionPointer(int ip) {
		putInteger(ip, EIP);
	}
	
	/**
	 * get Instruction Pointer, which in this case is not an Address, but rather a line number for the
	 * interpreter
	 * 
	 * @return the instruction pointer's current value
	 */
	public int getInstructionPointer() {
		return (int) EIP.getShortcut();
	}
	
	/**
	 * the hashtable for the variables (symbol names)
	 */
	private Hashtable<String, Integer> variables;
	
	private Hashtable<String, Long> constants;
	
	// //////////////////////////////////////
	// CONSTRUCTORS
	
	/**
	 * default constructor
	 * 
	 * @param size
	 *        defines the size of the simulated memory
	 * @param startAddress
	 *        the address of the first byte of the simulated memory
	 */
	public DataSpace(int size, int startAddress) {
		fpu = new Fpu();
		initMem(size, startAddress);
		initRegisters();
		clearDirty();
		clearFlags();
	}
	
	// //////////////////////////////////////
	// INIT
	/**
	 * initiate memory
	 * 
	 * @param size
	 *        which size the memory array has. The smallest multiple of 4 greater than size will be taken
	 * @param startAddress
	 *        the address of the first byte of memory
	 */
	private void initMem(int size, int startAddress) {
		variables = new Hashtable<String, Integer>();
		constants = new Hashtable<String, Long>();
		MEMSIZE = (size + 3) - ((size + 3) % 4);
		memory = new Memory(MEMSIZE, startAddress);
		memAddressStart = startAddress;
		nextReservableAddress = startAddress;
		memInfo = new TreeMap<Integer, MemCellInfo>();
	}
	
	/**
	 * initialize the register array, the hashtable for the registers, the RegisterSets for the GUI and the
	 * SmallArguments referring to the registers
	 */
	private void initRegisters() {
		regtable = new Hashtable<String, Address>();
		reg = new Registers();
		regInfo = new TreeMap<Integer, MemCellInfo>();
		EAX = reg.constructAddress("EAX", regtable);
		AX = reg.constructAddress("AX", regtable);
		AH = reg.constructAddress("AH", regtable);
		AL = reg.constructAddress("AL", regtable);
		EBX = reg.constructAddress("EBX", regtable);
		BX = reg.constructAddress("BX", regtable);
		BH = reg.constructAddress("BH", regtable);
		BL = reg.constructAddress("BL", regtable);
		ECX = reg.constructAddress("ECX", regtable);
		CX = reg.constructAddress("CX", regtable);
		CH = reg.constructAddress("CH", regtable);
		CL = reg.constructAddress("CL", regtable);
		EDX = reg.constructAddress("EDX", regtable);
		DX = reg.constructAddress("DX", regtable);
		DH = reg.constructAddress("DH", regtable);
		DL = reg.constructAddress("DL", regtable);
		ESI = reg.constructAddress("ESI", regtable);
		SI = reg.constructAddress("SI", regtable);
		EDI = reg.constructAddress("EDI", regtable);
		DI = reg.constructAddress("DI", regtable);
		ESP = reg.constructAddress("ESP", regtable);
		SP = reg.constructAddress("SP", regtable);
		EBP = reg.constructAddress("EBP", regtable);
		BP = reg.constructAddress("BP", regtable);
		EIP = reg.constructAddress("EIP", regtable);
		
		registerSets = new RegisterSet[9];
		registerSets[0] = new RegisterSet(AL, AH, AX, EAX, "AL", "AH", "AX", "EAX");
		registerSets[1] = new RegisterSet(BL, BH, BX, EBX, "BL", "BH", "BX", "EBX");
		registerSets[2] = new RegisterSet(CL, CH, CX, ECX, "CL", "CH", "CX", "ECX");
		registerSets[3] = new RegisterSet(DL, DH, DX, EDX, "DL", "DH", "DX", "EDX");
		registerSets[4] = new RegisterSet(null, null, SI, ESI, "", "", "SI", "ESI");
		registerSets[5] = new RegisterSet(null, null, DI, EDI, "", "", "DI", "EDI");
		registerSets[6] = new RegisterSet(null, null, SP, ESP, "", "", "SP", "ESP");
		registerSets[7] = new RegisterSet(null, null, BP, EBP, "", "", "BP", "EBP");
		registerSets[8] = new RegisterSet(null, null, null, EIP, "", "", "", "EIP");
		
		put(MEMSIZE + memAddressStart, ESP, null);
		put(MEMSIZE + memAddressStart, EBP, null);
		reg.clearDirty();
		
	}
	
	// //////////////////////////////////////
	// SAVE AND LOAD
	
	// IMPORTANT:
	// make sure that for every new non-constant field you define,
	// save it into file in the save() method
	// and the same (in the reversed order) in the load() method
	
	/**
	 * save the relevante data to file therefore it will be zipped and buffered
	 * 
	 * @param file
	 *        file to be saved to
	 * @return true if everything went alright, false otherwise
	 */
	public boolean save(File file) {
		ObjectOutputStream oos;
		ZipOutputStream zos;
		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			zos.putNextEntry(new ZipEntry("dataspace"));
			zos.setLevel(9);
			oos = new ObjectOutputStream(zos);
			save(oos);
			oos.flush();
			oos.close();
			zos.closeEntry();
			zos.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * directly provides a stream to save to
	 * 
	 * @param oos
	 *        the object stream to save to
	 * @throws IOException
	 */
	public void save(ObjectOutputStream oos) throws IOException {
		oos.writeInt(MEMSIZE);
		oos.writeInt(REGSIZE);
		oos.writeObject(reg);
		oos.writeObject(regInfo);
		oos.writeObject(memory);
		oos.writeObject(memInfo);
		oos.writeObject(variables);
		oos.writeObject(constants);
		oos.writeBoolean(fCarry);
		oos.writeBoolean(fOverflow);
		oos.writeBoolean(fSign);
		oos.writeBoolean(fZero);
		oos.writeBoolean(fParity);
		oos.writeBoolean(fAuxiliary);
		oos.writeBoolean(fTrap);
		oos.writeBoolean(fDirection);
		oos.writeInt(nextReservableAddress);
	}
	
	/**
	 * load the relevante data from file unzips, utilizes buffer
	 * 
	 * @param file
	 *        file to be loaded from
	 * @return true if everything went alright, false otherwise
	 */
	public boolean load(File file) {
		ObjectInputStream ois;
		ZipInputStream zis;
		if (!file.exists()) {
			return false;
		}
		try {
			zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
			zis.getNextEntry();
			ois = new ObjectInputStream(zis);
			load(ois);
			ois.close();
			zis.closeEntry();
			zis.close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * directly provides a stream to load from
	 * 
	 * @param ois
	 *        the object stream to load from
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public void load(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		MEMSIZE = ois.readInt();
		REGSIZE = ois.readInt();
		reg = (Registers) ois.readObject();
		regInfo = (TreeMap<Integer, MemCellInfo>) ois.readObject();
		memory = (Memory) ois.readObject();
		memInfo = (TreeMap<Integer, MemCellInfo>) ois.readObject();
		variables = (Hashtable<String, Integer>) ois.readObject();
		constants = (Hashtable<String, Long>) ois.readObject();
		fCarry = ois.readBoolean();
		fOverflow = ois.readBoolean();
		fSign = ois.readBoolean();
		fZero = ois.readBoolean();
		fParity = ois.readBoolean();
		fAuxiliary = ois.readBoolean();
		fTrap = ois.readBoolean();
		fDirection = ois.readBoolean();
		nextReservableAddress = ois.readInt();
	}
	
	// //////////////////////////////////////
	// INPUTS
	
	/**
	 * puts a given value into the specified location
	 * 
	 * @param value
	 * @param address
	 *        where to store. could be both register or memory
	 * @see Address
	 */
	private void putInteger(long value, Address address) {
		if ((address.type & Op.REG) != 0) {
			reg.set(address, value);
		} else if ((address.type & Op.MEM) != 0) {
			if ((address.address < memAddressStart)
				|| ((address.address + address.size) > (MEMSIZE + memAddressStart))) {
				addressOutOfRange = true;
				return;
			}
			long mask = 255; // bitmask to filter 8 bits
			int bytebuffer = 0; // somewhere to save the 8 bits
			for (int i = 0; i < address.size; i++) {
				bytebuffer = (int) (value & mask); // masking
				memory.set(address.address + i, bytebuffer); // write the byte
				value = value >> 8; // shift to another byte (with more significance)
			}
		} else {
			return;
		}
		
	}
	
	/**
	 * puts a given value into the specified location, accepting binary and hexadecimal Strings.
	 * utilizes the functionality of Parser.hex2dec()
	 * 
	 * @param numberString
	 *        the String to interpret from
	 * @param address
	 *        the location to store to. Could be both register or memory
	 * @param type
	 *        BIN or HEX
	 */
	public void putString(String numberString, Address address, int type) {
		numberString = numberString.toUpperCase();
		if (type == BIN) {
			if (!numberString.endsWith("B")) {
				numberString += "B";
			}
		} else if (type == HEX) {
			if (!(numberString.startsWith("0X") || numberString.endsWith("H"))) {
				numberString = "0X" + numberString;
			}
		}
		// use the hex2dec method
		numberString = Parser.hex2dec(numberString);
		if (!Op.matches(parser.getOperandType(numberString), Op.IMM)) {
			System.out.println("DataSpace.putString: invalid OperandType!");
			return;
		}
		// then pass the results to putInteger
		putInteger(Long.parseLong(numberString), address);
	}
	
	// //////////////////////////////////////
	// OUTPUT
	
	/**
	 * returns a memory value which should be interpreted as unsigned
	 * 
	 * @param address
	 *        the address of the (first) memory cell to retrieve
	 * @param size
	 *        the number of memory cells to retrieve
	 * @return a long value from the specified location
	 */
	public long getUnsignedMemory(int address, int size) {
		long result = 0;
		long bitmask = 255;
		for (int i = 0; i < size; i++) {
			result = result | ((bitmask & memory.get(address + i)) << (8 * i));
		}
		return result;
	}
	
	/**
	 * returns a value from memory which should be interpreted as signed, therefore the native type casting of
	 * Java is used
	 * 
	 * @param address
	 *        the address of the (first) memory cell to retrieve
	 * @param size
	 *        the number of memory cells to retrieve
	 * @return a long value from the specified location
	 */
	public long getSignedMemory(int address, int size) {
		if (size == 8) {
			long value = 0;
			for (int i = 0; i < size; i++) {
				value = value | (((long) memory.get(address + i)) << (8 * i));
			}
			return value;
		}
		if (size == 4) {
			int value = 0;
			for (int i = 0; i < size; i++) {
				value = (value | (memory.get(address + i) << (8 * i)));
			}
			return value;
		}
		if (size == 2) {
			short value = 0;
			for (int i = 0; i < size; i++) {
				value = (short) (value | (memory.get(address + i) << (8 * i)));
			}
			return value;
		}
		if (size == 1) {
			return (byte) (memory.get(address));
		}
		throw new RuntimeException("getSignedMemory called with invalid size!");
	}
	
	/**
	 * returns a register's value, which should be interpreted as signed, therefore the native type casting of
	 * Java is used
	 * 
	 * @param address
	 *        the Address object specifying the desired register
	 * @return a long value from the specified location
	 */
	private long getSignedRegister(Address address) {
		if (address.size == 8) {
			return address.getShortcut();
		}
		if (address.size == 4) {
			return (int) address.getShortcut();
		}
		if (address.size == 2) {
			return (short) address.getShortcut();
		}
		if (address.size == 1) {
			return (byte) address.getShortcut();
		}
		throw new RuntimeException("getSignedRegister called with invalid size. This can't happen. ;-)");
	}
	
	/**
	 * specially designed for the GUI
	 * 
	 * @param value
	 *        the input value to be formatted.
	 * @param size
	 *        the size of the value in bytes
	 * @param type
	 *        BIN, HEX, SIGNED or UNSIGNED
	 * @return a String containing the value of the register regx in the given type
	 */
	public static String getString(long value, int size, int type) {
		if (type == SIGNED) {
			switch (size) {
			case 1:
				return String.valueOf((byte) value);
			case 2:
				return String.valueOf((short) value);
			case 4:
				return String.valueOf((int) value);
			}
		} else if (type == UNSIGNED) {
			return String.valueOf(value);
		} else if (type == BIN) {
			return Integer.toBinaryString((int) value);
		} else if (type == HEX) {
			String temp = Integer.toHexString((int) value).toUpperCase();
			while (temp.length() < (size * 2)) {
				temp = "0" + temp;
			}
			temp = "0x" + temp;
			return temp;
		}
		return "";
	}
	
	// //////////////////////////////////////
	// Dirty Management
	
	/**
	 * decrement all dirty flags to indicate a processing step
	 */
	public void updateDirty() {
		memory.updateDirty();
		reg.updateDirty();
	}
	
	/**
	 * set the dirty flag for a register or memory location. Remember that the dirty flag for memory locations
	 * is set automatically upon write access.
	 * 
	 * @param address
	 *        the argument addressing the register or memory location
	 */
	public void setDirty(Address address) {
		if ((address.type & Op.REG) != 0) {
			reg.setDirty(address);
		} else if ((address.type & Op.MEM) != 0) {
			for (int i = 0; i < address.size; i++) {
				memory.setDirty(i + address.address);
			}
		}
	}
	
	/**
	 * @return if there were operations on the address in the last given steps
	 * @param a
	 *        which register/memory address to be traced
	 * @param steps
	 *        how many steps to be traced
	 */
	public boolean isDirty(Address a, int steps) {
		if ((a.type & Op.MEM) != 0) {
			for (int i = 0; i < a.size; i++) {
				if (memory.isDirty(i + a.address, steps)) {
					return true;
				}
			}
		} else if ((a.type & Op.REG) != 0) {
			return reg.isDirty(a, steps);
		}
		return false;
	}
	
	// //////////////////////////////////////
	// RESETS
	
	/**
	 * clears the registers
	 */
	private void clearReg() {
		reg.reset();
		put(MEMSIZE + memAddressStart, ESP, null);
		put(MEMSIZE + memAddressStart, EBP, null);
		reg.clearDirty();
	}
	
	/**
	 * clears the memory
	 */
	private void clearMem() {
		memory.reset();
		memInfo = new TreeMap<Integer, MemCellInfo>();
		nextReservableAddress = memAddressStart;
		variables.clear();
		constants.clear();
	}
	
	/**
	 * set all dirty flags to 0
	 */
	private void clearDirty() {
		memory.clearDirty();
		reg.clearDirty();
	}
	
	/**
	 * clears the status flags, sets them to false
	 */
	private void clearFlags() {
		fCarry = fOverflow = fSign = fZero = fParity = fAuxiliary = fTrap = fDirection = false;
	}
	
	/**
	 * clear everything memory, registers, flags, dirty and journal
	 */
	public void clear() {
		clearFlags();
		clearMem();
		addressOutOfRange = false;
		clearReg();
		fpu.clear();
	}
	
	// //////////////////////////////////////
	// SUPPORTING METHODS
	
	/**
	 * returns the Address referring to the register specified by the given String
	 * 
	 * @param s
	 *        the String representation of the register
	 * @return the Address referring to the register
	 */
	public Address getRegisterArgument(String s) {
		return regtable.get(s);
	}
	
	/**
	 * @param a
	 *        an Argument referring to a register or memory cell about which information is to be retrieved
	 * @param writeAccess
	 *        pass true to create the MemCellInfo object if necessary
	 * @return the MemCellInfo object associated with the memory cell with the specified index
	 */
	public MemCellInfo memInfo(Address a) {
		Integer index = new Integer(a.address);
		if (Op.matches(a.type, Op.MEM)) {
			return memInfo.get(index);
		} else if (Op.matches(a.type, Op.REG)) {
			return regInfo.get(index);
		}
		return null;
	}
	
	private void memInfoPut(Address a, MemCellInfo info) {
		Integer index = new Integer(a.address);
		if (Op.matches(a.type, Op.MEM)) {
			if ((a.address >= (MEMSIZE + memAddressStart)) || (a.address < memAddressStart)) {
				return;
			}
			memInfo.put(index, info);
		} else if (Op.matches(a.type, Op.REG)) {
			regInfo.put(index, info);
		}
	}
	
	private void memInfoDelete(Address a) {
		Integer index = new Integer(a.address);
		if (Op.matches(a.type, Op.MEM)) {
			if ((a.address >= (MEMSIZE + memAddressStart)) || (a.address < memAddressStart)) {
				return;
			}
			memInfo.remove(index);
		} else if (Op.matches(a.type, Op.REG)) {
			regInfo.remove(index);
		}
	}
	
	/**
	 * returns the register matching the given register with the specified size, e.g. passing EAX and
	 * 2 will return AX. (!) Be sure to pass one of DataSpace's registers, such as dataspace.EAX, not a newly
	 * created
	 * one!
	 * 
	 * @param wantedBigRegister
	 * @param size
	 * @return an argument referring to the desired register
	 */
	public Address getMatchingRegister(Address wantedBigRegister, int size) {
		for (RegisterSet rs : registerSets) {
			if (rs.aE == wantedBigRegister) {
				if (size == 1) {
					return rs.aL;
				}
				if (size == 2) {
					return rs.aX;
				}
				if (size == 4) {
					return rs.aE;
				}
			}
		}
		return null;
	}
	
	/**
	 * @param register
	 *        the register mnemonic as a String
	 * @return how many bytes the register takes
	 */
	public int getRegisterSize(String register) {
		Address reg = regtable.get(register);
		if (reg != null) {
			return reg.size;
		}
		return 0;
	}
	
	/**
	 * @return an array of type RegisterSet which contains the String names of the registers which belong
	 *         together.
	 */
	public RegisterSet[] getRegisterSets() {
		return registerSets;
	}
	
	/**
	 * @param value
	 *        value to be stored
	 * @param a
	 *        an argument describing the register/memory address where the value is to be stored
	 * @param memCellInfo
	 *        the MemCellInfo object that is to be stored. Pass null if none is needed. general purpose method
	 *        to store
	 *        a value
	 */
	public void put(long value, Address a, MemCellInfo memCellInfo) {
		if (a == null) {
			return;
		}
		if ((a.type & (Op.MEM | Op.REG)) != 0) {
			putInteger(value, a);
			if (memCellInfo == null) {
				memInfoDelete(a);
			} else {
				memInfoPut(a, memCellInfo);
			}
			return;
		}
		if ((a.type & Op.FPUREG) != 0) {
			fpu.put(a, value);
			return;
		}
	}
	
	/**
	 * retrieves the value of a non-static argument such as a register (as opposed to, e.g., an immediate)
	 * 
	 * @param a
	 *        the argument specifying the register/memory location whose value is to be read
	 * @param signed
	 *        true if the value is to be interpreted as a signed number
	 * @return the value at the given address
	 */
	public long getUpdate(Address a, boolean signed) {
		if (a == null) {
			return 0;
		}
		if ((a.type & Op.MEM) != 0) {
			if ((a.address < memAddressStart) || ((a.address + a.size) > (MEMSIZE + memAddressStart))) {
				addressOutOfRange = true;
				return 0;
			}
			MemCellInfo info = memInfo.get(a.address);
			if (info != null) {
				if (info.type == Op.LABEL) {
					long currentLabelLine = getInitial(info.value, Op.LABEL, info.size, false);
					long storedLabelLine = getUnsignedMemory(a.address, a.size);
					if (currentLabelLine != storedLabelLine) {
						put(currentLabelLine, a, info);
					}
					return currentLabelLine;
				}
			}
			if (signed) {
				return getSignedMemory(a.address, a.size);
			} else {
				return getUnsignedMemory(a.address, a.size);
			}
		}
		if ((a.type & Op.REG) != 0) {
			MemCellInfo info = regInfo.get(a.address);
			if (info != null) {
				if (info.type == Op.LABEL) {
					long currentLabelLine = getInitial(info.value, Op.LABEL, info.size, false);
					long storedLabelLine = a.getShortcut();
					if (currentLabelLine != storedLabelLine) {
						put(currentLabelLine, a, info);
					}
					return currentLabelLine;
				}
			}
			if (signed) {
				return getSignedRegister(a);
			} else {
				return a.getShortcut();
			}
		}
		if ((a.type & Op.FPUREG) != 0) {
			return fpu.get(a);
		}
		return a.value;
	}
	
	/**
	 * retrieves the value of a static argument, such as an immediate, or a label
	 * 
	 * @param a
	 *        the argument whose value is to be returned
	 * @param signed
	 *        signed access?
	 * @return the argument's value
	 */
	public long getInitial(FullArgument a, boolean signed) {
		return getInitial(a.arg, a.address.type, a.address.size, signed);
	}
	
	private long getInitial(String src, int type, int size, boolean signed) {
		if ((type & Op.IMM) != 0) {
			long value = Long.valueOf(src);
			if (signed) {
				return value;
			} else {
				value = (value & (((long) 1 << (size * 8)) - 1));
				return value;
			}
		} else if (type == Op.CHARS) {
			return Parser.getCharsAsNumber(src);
		} else if (type == Op.LABEL) {
			return parser.doc.getLabelLine(src);
		} else if (type == Op.VARIABLE) {
			return getVariable(src);
		} else if (type == Op.CONST) {
			return getConstant(src);
		} else if (type == Op.FLOAT) {
			if (size == 8) {
				return Double.doubleToRawLongBits(Double.parseDouble(src));
			} else if (size == 4) {
				return Float.floatToRawIntBits(Float.parseFloat(src));
			}
		}
		return 0;
	}
	
	// //////////////////////////////////////
	// SYMBOLNAME RESERVATIONS
	
	/**
	 * allocates/reservates memory: a specified block size in bytes, multiplied by a given number of blocks.
	 * Currently does not check whether the starting address is a multiple of the requested size, e.g. it is
	 * perfectly possible that a 4-byte block will start at address 3
	 * 
	 * @param size
	 *        the size of the required memory block
	 * @param howmany
	 *        the number of blocks
	 * @return a Address referring to the first reserved memory block
	 */
	public Address malloc(int size, int howmany) {
		int result = nextReservableAddress;
		nextReservableAddress += size * howmany;
		if (nextReservableAddress > (MEMSIZE + memAddressStart)) {
			addressOutOfRange = true;
			return null;
		}
		Address a = new Address(Op.MEM, size, result);
		return a;
	}
	
	/**
	 * registers the given String as a variable
	 * 
	 * @param label
	 *        the label that is to be registered
	 */
	public void registerVariable(String label) {
		if (!variables.containsKey(label)) {
			// note that the address will be overwritten later on!
			variables.put(label, nextReservableAddress);
		}
		if (constants.containsKey(label)) {
			System.out.println(label + " is now a variable!");
			constants.remove(label);
		}
	}
	
	public void setVariableAddress(String variable, int address) {
		variables.put(variable, address);
	}
	
	/**
	 * registers the given String as a constant
	 * 
	 * @param label
	 *        the label that is to be registered
	 */
	public void registerConstant(String label) {
		if (!constants.containsKey(label)) {
			constants.put(label, 0L);
		}
		if (variables.containsKey(label)) {
			System.out.println(label + " is now a constant!");
			variables.remove(label);
		}
	}
	
	public void setConstantValue(String label, long value) {
		constants.put(label, value);
		if (variables.containsKey(label)) {
			variables.remove(label);
		}
	}
	
	/**
	 * unregisters the specified variable
	 * 
	 * @param label
	 *        the variable that is to be unregistered
	 */
	public void unregisterVariable(String label) {
		if (label != null) {
			variables.remove(label);
		}
	}
	
	public void unregisterConstant(String constant) {
		if (constant != null) {
			constants.remove(constant);
		}
	}
	
	/**
	 * @param symbol
	 *        the name
	 * @return the start address
	 */
	public int getVariable(String symbol) {
		return variables.get(symbol);
	}
	
	public long getConstant(String constant) {
		if (constants == null) {
			return 0L;
		}
		return constants.get(constant);
	}
	
	/**
	 * @return list of all registered variables
	 */
	public String[] getVariableList() {
		Enumeration<String> enumeration = variables.keys();
		String[] result = new String[variables.size()];
		int i = 0;
		while (enumeration.hasMoreElements()) {
			result[i++] = enumeration.nextElement();
		}
		return result;
	}
	
	public String[] getConstantList() {
		Enumeration<String> enumeration = constants.keys();
		String[] result = new String[constants.size()];
		int i = 0;
		while (enumeration.hasMoreElements()) {
			result[i++] = enumeration.nextElement();
		}
		return result;
	}
	
	/**
	 * checks whether a given String is a known/registered variable
	 * 
	 * @param symbol
	 *        the String that is to be checked
	 * @return true if the String represents a registered variable, false otherwise
	 */
	public boolean isVariable(String symbol) {
		return (variables.containsKey(symbol));
	}
	
	public boolean isConstant(String symbol) {
		return (constants.containsKey(symbol));
	}
	
	// //////////////////////////////////////
	// LISTENER SUPPORT
	
	public void addMemoryListener(IListener l, int address) {
		memory.addListener(l, address);
	}
	
	public void addMemoryListener(IListener l) {
		memory.addListener(l);
	}
	
	/*public void addRegisterListener(IListener l) {
		reg.addListener(l);
	}*/
	
	public void removeMemoryListener(IListener l, int address) {
		memory.removeListener(l, address);
	}
	
	public void removeMemoryListener(IListener l) {
		memory.removeListener(l);
	}
	
	/*public void removeRegisterListener(IListener l) {
		reg.removeListener(l);
	}*/
}