/**
 * 
 */
package jasmin.gui;

import jasmin.core.*;
import java.awt.Polygon;

/**
 * @author Jakob Kummerow
 */
public class PolygonObject {
	
	public Polygon poly;
	public Address address;
	public long bitmask;
	
	public PolygonObject(Polygon poly, Address address, long bitmask) {
		this.poly = poly;
		this.address = address;
		this.bitmask = bitmask;
	}
	
	public PolygonObject(Address address, long bitmask) {
		this.address = address;
		this.bitmask = bitmask;
	}
	
	public void invertBit(DataSpace dsp) {
		long memval = dsp.getUpdate(address, false);
		memval ^= bitmask;
		dsp.put(memval, address, null);
	}
	
}
