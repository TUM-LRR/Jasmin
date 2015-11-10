/*
 * MemoryTableModel.java
 *
 * Created on 20. März 2006, 21:33
 *
 */

package jasmin.gui;

import jasmin.core.*;
import java.awt.Color;
import java.util.*;
import javax.swing.event.*;
import javax.swing.table.TableModel;

/**
 * @author Kai Orend
 */

public class MemoryTableModel implements TableModel {
	
	private DataSpace data = null;
	private LinkedList<TableModelListener> listener = null;
	
	private boolean descending = false;
	
	public static byte BYTE = 1;
	public static byte WORD = 2;
	public static byte DWORD = 4;
	private byte mode = DWORD;
	private JasDocument doc = null;
	
	/** Creates a new instance of MemoryTableModel */
	public MemoryTableModel(DataSpace space, JasDocument doc) {
		this.data = space;
		listener = new LinkedList<TableModelListener>();
		this.doc = doc;
		doHighlight = doc.isHighlightingEnabled();
	}
	
	public boolean isDescending() {
		return descending;
	}
	
	public void enableDescending(boolean descending) {
		this.descending = descending;
		fireChangedEvent();
	}
	
	public void setMode(byte mode) {
		this.mode = mode;
		fireChangedEvent();
	}
	
	/**
	 * Removes a listener from the list that is notified each time a change to the data model occurs.
	 * 
	 * @param l
	 *        the TableModelListener
	 */
	public void removeTableModelListener(TableModelListener l) {
		listener.remove(l);
	}
	
	/**
	 * Adds a listener to the list that is notified each time a change to the data model occurs.
	 * 
	 * @param l
	 *        the TableModelListener
	 */
	public void addTableModelListener(TableModelListener l) {
		listener.add(l);
		
	}
	
	public boolean isDirty(int row) {
		int index = getRowIndex(row) + data.getMemAddressStart();
		return data.isDirty(new Address(Op.MEM, mode, index), doc.getLastStepCount());
	}
	
	public boolean isStack(int row) {
		int index = getRowIndex(row) + data.getMemAddressStart();
		return index >= data.getUpdate(data.ESP, false);
	}
	
	private boolean doHighlight;
	
	public void enableHighlighting(boolean highlight) {
		doHighlight = highlight;
		fireChangedEvent();
	}
	
	public Address getPointingRegister(int row) {
		int index = getRowIndex(row) + data.getMemAddressStart();
		for (RegisterSet rs : data.getRegisterSets()) {
			
			if ((int) rs.aE.getShortcut() == index) {
				
				return rs.aE;
			}
		}
		return null;
	}
	
	public Color getCellColor(int row) {
		if (!doHighlight) {
			return null;
		}
		return doc.getRegisterColor(getPointingRegister(row));
	}
	
	/**
	 * Sets the value in the cell at <code>columnIndex</code> and <code>rowIndex</code> to <code>aValue</code>
	 * .
	 * 
	 * @param aValue
	 *        the new value
	 * @param rowIndex
	 *        the row whose value is to be changed
	 * @param columnIndex
	 *        the column whose value is to be changed
	 * @see #getValueAt
	 * @see #isCellEditable
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		int index = getRowIndex(rowIndex) + data.getMemAddressStart();
		switch (columnIndex) {
		case 1:
			data.putString(aValue.toString(), new Address(Op.MEM, mode, index), DataSpace.SIGNED);
			break;
		case 2:
			data.putString(aValue.toString(), new Address(Op.MEM, mode, index), DataSpace.UNSIGNED);
			break;
		case 3:
			data.putString(aValue.toString(), new Address(Op.MEM, mode, index), DataSpace.HEX);
			break;
		}
		fireChangedEvent(rowIndex);
	}
	
	private void fireChangedEvent(int row) {
		Iterator<TableModelListener> iter = listener.iterator();
		while (iter.hasNext()) {
			TableModelListener l = iter.next();
			l.tableChanged((new TableModelEvent(this, row)));
		}
	}
	
	private void fireChangedEvent() {
		Iterator<TableModelListener> iter = listener.iterator();
		while (iter.hasNext()) {
			TableModelListener l = iter.next();
			l.tableChanged((new TableModelEvent(this)));
		}
	}
	
	/**
	 * Returns the name of the column at <code>columnIndex</code>. This is used to initialize the table's
	 * column header
	 * name. Note: this name does not need to be unique; two columns in a table can have the same name.
	 * 
	 * @param columnIndex
	 *        the index of the column
	 * @return the name of the column
	 */
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "address";
		case 1:
			return "signed int";
		case 2:
			return "unsigned int";
		case 3:
			return "hex";
			
		}
		return "Titel";
	}
	
	private int getRowIndex(int index) {
		if (descending) {
			return ((getRowCount() - 1) - index) * mode;
		} else {
			return index * mode;
		}
	}
	
	/**
	 * Returns the most specific superclass for all the cell values in the column. This is used by the
	 * <code>JTable</code> to set up a default renderer and editor for the column.
	 * 
	 * @param columnIndex
	 *        the index of the column
	 * @return the common ancestor class of the object values in the model.
	 */
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}
	
	/**
	 * Returns true if the cell at <code>rowIndex</code> and <code>columnIndex</code> is editable. Otherwise,
	 * <code>setValueAt</code> on the cell will not change the value of that cell.
	 * 
	 * @param rowIndex
	 *        the row whose value to be queried
	 * @param columnIndex
	 *        the column whose value to be queried
	 * @return true if the cell is editable
	 * @see #setValueAt
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 1:
			return true;
		case 2:
			return true;
		case 3:
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the value for the cell at <code>columnIndex</code> and <code>rowIndex</code>.
	 * 
	 * @param rowIndex
	 *        the row whose value is to be queried
	 * @param columnIndex
	 *        the column whose value is to be queried
	 * @return the value Object at the specified cell
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		int index = getRowIndex(rowIndex) + data.getMemAddressStart();
		switch (columnIndex) {
		case 0:
			if (doc.isMemAddressAsHex()) {
				return "0x" + Integer.toHexString(index).toUpperCase();
			} else {
				return index + "";
			}
		case 1:
			return data.getSignedMemory(index, mode) + "";
		case 2:
			return data.getUnsignedMemory(index, mode) + "";
		case 3:
			return getHex(index);
		}
		return null;
	}
	
	private String getHex(int address) {
		long value = data.getUpdate(new Address(Op.MEM, mode, address), false);
		return DataSpace.getString(value, mode, DataSpace.HEX);
		
	}
	
	/**
	 * Returns the number of rows in the model. A <code>JTable</code> uses this method to determine how many
	 * rows it
	 * should display. This method should be quick, as it is called frequently during rendering.
	 * 
	 * @return the number of rows in the model
	 * @see #getColumnCount
	 */
	public int getRowCount() {
		
		return data.getMEMSIZE() / mode;
	}
	
	/**
	 * Returns the number of columns in the model. A <code>JTable</code> uses this method to determine how
	 * many columns
	 * it should create and display by default.
	 * 
	 * @return the number of columns in the model
	 * @see #getRowCount
	 */
	public int getColumnCount() {
		return 4;
	}
	
	public void updateChanged() {
		fireChangedEvent();
	}
	
}
