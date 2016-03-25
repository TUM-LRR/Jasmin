/*
 * FpuStackTableModel.java
 *
 * Created on 13. Mai 2006, 17:59
 *
 */

package jasmin.gui;

import jasmin.core.Fpu;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * @author Kai Orend
 */
public class FpuStackTableModel implements TableModel {
	
	private Fpu fpu = null;
	private List<TableModelListener> listener = null;
	private JasDocument doc = null;
	
	/** Creates a new instance of FpuStackTableModel */
	public FpuStackTableModel(Fpu fpu, JasDocument doc) {
		this.fpu = fpu;
		listener = new ArrayList<>();
		this.doc = doc;
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
		if (columnIndex == 1) {
			fpu.putRegisterContent(rowIndex, aValue.toString());
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
			return "name";
		case 1:
			return "value";
		}
		return null;
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
		return columnIndex == 1;
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
		switch (columnIndex) {
		case 0:
			return fpu.getRegisterName(rowIndex);
		case 1:
			return fpu.getRegisterContent(rowIndex, 10);
		}
		return null;
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
		return fpu.getNumRegisters();
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
		return 2;
	}
	
}
