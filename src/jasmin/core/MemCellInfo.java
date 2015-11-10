package jasmin.core;

/**
 * @author Jakob Kummerow
 */

public class MemCellInfo {
	
	public int type;
	public String value = null;
	public int size;
	
	public MemCellInfo() {
	};
	
	public MemCellInfo(int dataType, String dataValue, int affectedSize) {
		type = dataType;
		value = dataValue;
		size = affectedSize;
	}
	
	public MemCellInfo(FullArgument a) {
		type = a.address.type;
		value = a.arg;
		size = a.address.size;
	}
	
}
