/*
 * Console.java
 * 
 * @author Johannes Roith <roith@in.tum.de> 
 * 
 * Created on 08. November 2007, 10:30 
 * 
 * extended in Nov 2008 by Jakob Kummerow
 * Reads a NULL-terminated C-like string from the selected offset 
 * (one byte per character (ASCII) until a NULL-Byte is found. 
 * 
 * Alternatively (configurable) appends any characters it finds at the 
 * given address to the existing output ("pipe mode")
 */

package jasmin.gui;

import jasmin.core.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Console extends javax.swing.JPanel implements IGuiModule, IListener {
	
	private javax.swing.JTextArea output;
	private javax.swing.JScrollPane jScrollPane1;
	private JPopupMenu jPopupMenu;
	private JMenuItem menuItemClear;
	
	DataSpace data;
	private Address address = new Address(Op.MEM, 1, 0);
	
	private static int MODE_ARRAY = 0;
	private static int MODE_PIPE = 1;
	private int mode = -1;
	
	private boolean visible = false;
	private String queue = "";
	private int lastLength = 0;
	
	/** Creates new form Console */
	public Console() {
		
		output = new javax.swing.JTextArea();
		output.setEditable(false);
		Color bgColor = new Color(30, 46, 28);
		output.setBackground(bgColor);
		output.setForeground(Color.GREEN);
		output.setFont(new Font("Monospaced", Font.PLAIN, 15));
		output.setBorder(javax.swing.BorderFactory.createLineBorder(bgColor, 5));
		output.setText("");
		
		jPopupMenu = new javax.swing.JPopupMenu();
		JMenuItem menuItemAddress = new javax.swing.JMenuItem();
		menuItemAddress.setText("Change Address");
		menuItemAddress.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				AddressActionPerformed(evt);
			}
		});
		jPopupMenu.add(menuItemAddress);
		
		JMenuItem menuItemMode = new javax.swing.JMenuItem();
		menuItemMode.setText("Change Mode");
		menuItemMode.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				ModeActionPerformed(evt);
			}
		});
		jPopupMenu.add(menuItemMode);
		
		menuItemClear = new javax.swing.JMenuItem();
		menuItemClear.setText("Clear");
		menuItemClear.addActionListener(new java.awt.event.ActionListener() {
			
			/**
			 * @param evt
			 *        the Event that triggered this action
			 */
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				clear();
			}
		});
		jPopupMenu.add(menuItemClear);
		
		output.addMouseListener(new java.awt.event.MouseAdapter() {
			
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
					formMouseClicked(evt);
				}
			}
		});

		jScrollPane1 = new javax.swing.JScrollPane();
		setLayout(new java.awt.BorderLayout());
		jScrollPane1.setViewportView(output);
		add(jScrollPane1, java.awt.BorderLayout.CENTER);
	}
	
	public String getTabLabel() {
		return "Console";
	}
	
	public void setDataSpace(DataSpace dsp) {
		data = dsp;
		address.address = data.getMemAddressStart();
		setMode(MODE_ARRAY);
	}
	
	public void clear() {
		output.setText("");
		lastLength = 0;
	}
	
	private void formMouseClicked(java.awt.event.MouseEvent evt) {
		if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
			jPopupMenu.show(this, evt.getX(), evt.getY());
		}
	}
	
	public void updateArray() {
		String str = "";
		long currentByte = 0;
		int origAddr = address.address;
		lastLength = 0;
		while (true) {
			currentByte = data.getUpdate(address, false);
			if (currentByte == 0) {
				break;
			}
			address.address++;
			lastLength++;
			str += (char) currentByte;
		}
		address.address = origAddr;
		output.setText(str);
	}
	
	public void updateAll() {
		// nothing to do here
	}
	
	// Adapted from SevenSegment.java
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void AddressActionPerformed(ActionEvent evt) {
		String newAddress = JOptionPane.showInputDialog("Please enter a new address for the console to use:",
			"0x"
				+ Integer.toHexString(address.address));
		int address_temp = 0;
		if (newAddress != null) {
			try {
				address_temp = new Integer(Parser.hex2dec(newAddress.toUpperCase()));
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "The entered value was not valid!");
			}
			if ((address_temp < data.getMemAddressStart())
				|| (address_temp > data.getMemAddressStart() + data.getMEMSIZE())) {
				JOptionPane.showMessageDialog(null, "The entered value was not valid!");
			} else {
				// handle listeners
				if (mode == MODE_PIPE) {
					data.removeMemoryListener(this, address.address);
					data.addMemoryListener(this, address_temp);
				}
				// save change
				address = new Address(Op.MEM, 1, address_temp);
			}
			if (mode == MODE_ARRAY) {
				updateArray();
			}
		}
	}
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void ModeActionPerformed(ActionEvent evt) {
		
		String[] options = { "Array-based", "Pipe-like", "Cancel" };
		String msg = "Choose the input mode";
		String title = "Please choose";
		int choice = JOptionPane.showOptionDialog(null, msg, title, JOptionPane.DEFAULT_OPTION,
			JOptionPane.QUESTION_MESSAGE,
			null, options, null);
		
		if (choice == 0) {
			setMode(MODE_ARRAY);
		} else if (choice == 1) {
			setMode(MODE_PIPE);
		}
		
	}
	
	private void setMode(int newMode) {
		if (mode == newMode) {
			return;
		}
		// remove old listener
		if (mode == MODE_ARRAY) {
			data.removeMemoryListener(this);
		} else if (mode == MODE_PIPE) {
			data.removeMemoryListener(this, address.address);
		}
		// add new listener
		if (newMode == MODE_ARRAY) {
			if (visible) {
				data.addMemoryListener(this);
				menuItemClear.setEnabled(false);
				updateArray();
			}
		} else if (newMode == MODE_PIPE) {
			data.addMemoryListener(this, address.address);
			menuItemClear.setEnabled(true);
			clear();
		}
		// save mode
		mode = newMode;
	}
	
	public void setActivated(boolean active) {
		visible = active;
		if (visible && (mode == MODE_ARRAY)) {
			data.addMemoryListener(this);
			updateArray();
		} else if (visible && (mode == MODE_PIPE)) {
			if (queue.length() > 0) {
				for (int i = 0; i < queue.length(); i++) {
					writeKey(queue.charAt(i));
				}
				queue = "";
			}
		} else if (!visible && (mode == MODE_ARRAY)) {
			data.removeMemoryListener(this);
		}
	}
	
	private void writeKey(int key) {
		output.setEditable(true);
		output.setCaretPosition(output.getText().length());
		KeyEvent ke = new KeyEvent(output, KeyEvent.KEY_PRESSED, 0, 0, key, KeyEvent.CHAR_UNDEFINED);
		output.dispatchEvent(ke);
		ke = new KeyEvent(output, KeyEvent.KEY_TYPED, 0, 0, KeyEvent.VK_UNDEFINED, (char) key);
		output.dispatchEvent(ke);
		output.setEditable(false);
	}
	
	public void notifyChanged(int changedAddress, int newValue) {
		if (mode == MODE_PIPE) {
			if (visible) {
				writeKey(newValue);
			} else {
				queue += (char) newValue;
			}
		} else if (mode == MODE_ARRAY) {
			if (visible) {
				// changed byte is within string
				if ((changedAddress >= address.address) && (changedAddress < address.address + lastLength)) {
					int pos = changedAddress - address.address;
					if (newValue != 0) {
						output.replaceRange("" + (char) newValue, pos, pos + 1);
					} else {
						// changed byte is now the end of the string
						lastLength = pos;
						output.replaceRange("", pos, output.getText().length());
					}
				} else if (changedAddress == address.address + lastLength) {
					if (newValue != 0) {
						output.append("" + (char) newValue);
						lastLength++;
						// check whether the changed byte filled a "hole" and the string continues
						int origAddress = address.address;
						address.address += lastLength;
						int currentByte;
						do {
							currentByte = (int) data.getUpdate(address, false);
							if (currentByte != 0) {
								output.append("" + (char) currentByte);
								address.address++;
								lastLength++;
							}
						} while (currentByte != 0);
						address.address = origAddress;
					}
				}
			} // </ if(visible) >
				// do nothing if invisible. full output will be reconstructed when activated.
		}
	}
}
