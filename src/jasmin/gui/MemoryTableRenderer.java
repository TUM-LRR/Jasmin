/*
 * MemoryTableRenderer.java
 *
 * Created on 24. März 2006, 23:36
 *
 */

package jasmin.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

/**
 * @author Kai Orend
 */
public class MemoryTableRenderer implements TableCellRenderer {
	
	MemoryTableModel model;
	
	/** Creates a new instance of MemoryTableRenderer */
	public MemoryTableRenderer(MemoryTableModel model) {
		this.model = model;
	}
	
	/**
	 * Returns the component used for drawing the cell. This method is
	 * used to configure the renderer appropriately before drawing.
	 * 
	 * @param table
	 *        the <code>JTable</code> that is asking the
	 *        renderer to draw; can be <code>null</code>
	 * @param value
	 *        the value of the cell to be rendered. It is
	 *        up to the specific renderer to interpret
	 *        and draw the value. For example, if <code>value</code> is the string "true", it could be
	 *        rendered as a
	 *        string or it could be rendered as a check
	 *        box that is checked. <code>null</code> is a
	 *        valid value
	 * @param isSelected
	 *        true if the cell is to be rendered with the
	 *        selection highlighted; otherwise false
	 * @param hasFocus
	 *        if true, render cell appropriately. For
	 *        example, put a special border on the cell, if
	 *        the cell can be edited, render in the color used
	 *        to indicate editing
	 * @param row
	 *        the row index of the cell being drawn. When
	 *        drawing the header, the value of <code>row</code> is -1
	 * @param column
	 *        the column index of the cell being drawn
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		JLabel label = new JLabel();
		label.setText(value.toString());
		Font font = label.getFont();
		if (model.isDirty(row)) {
			label.setFont(font.deriveFont(Font.BOLD));
		} else {
			label.setFont(font.deriveFont(Font.PLAIN));
		}
		if (model.isStack(row)) {
			label.setBackground(new Color(210, 240, 200));
			label.setOpaque(true);
		}
		Color highlight = model.getCellColor(row);
		if (highlight != null) {
			label.setBackground(highlight);
			label.setOpaque(true);
		}
		return label;
	}
	
}
