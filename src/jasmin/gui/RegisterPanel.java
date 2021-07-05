/*
 * RegisterPanel.java
 *
 * Created on 23. März 2006, 20:33
 */

package jasmin.gui;

import jasmin.core.*;
import java.awt.Color;
import javax.swing.JTextField;

/**
 * @author Kai Orend, Florian Dollinger
 */
public class RegisterPanel extends javax.swing.JPanel {
	
	private RegisterSet register = null;
	private DataSpace data = null;
	
	private JasDocument doc = null;
	
	private int mode = DataSpace.BIN;
	private long value;
	
	/** Creates new form RegisterPanel */
	public RegisterPanel(RegisterSet reg, JasDocument doc) {
		initComponents();
		register = reg;
		data = doc.data;
		this.doc = doc;
		jPanel25.setVisible(false);
		jLabel17.setText(reg.E + ": ");
		if (reg.E.length() > 0) {
			jLabel1.setText(reg.E);
		} else {
			jLabel1.setText(" ");
		}
		
		if (reg.X.length() > 0) {
			jLabel9.setText(reg.X);
		} else {
			jLabel9.setText("  ");
			jPanel14.setBackground(jPanel3.getBackground());
			jPanel20.setBackground(jPanel3.getBackground());
		}
		
		if (reg.L.length() > 0) {
			jLabel10.setText(reg.H);
		} else {
			jLabel10.setText("  ");
			jPanel15.setBackground(jPanel3.getBackground());
		}
		
		if (reg.H.length() > 0) {
			jLabel14.setText(reg.L);
		} else {
			jLabel14.setText("  ");
			jPanel21.setBackground(jPanel3.getBackground());
		}
		
		setHighlight(doc.isHighlightingEnabled());
	}
	
	public void setHighlight(boolean highlight) {
		Color bg = null;
		if (highlight) {
			bg = doc.getRegisterColor(register.aE);
		}
		if (bg == null) {
			bg = new Color(255, 255, 255);
		}
		jTextField1.setBackground(bg);
		jTextField2.setBackground(bg);
		jTextField5.setBackground(bg);
		jTextField7.setBackground(bg);
		jTextField9.setBackground(bg);
	}
	
	public void setMode(int mode) {
		this.mode = mode;
		update();
	}
	
	
	public void update() {
		
		value = register.aE.getShortcut();
		jTextField9.setText(DataSpace.getString(value, 4, mode));

                // Was the whole extended register touched?
                boolean dirtyE = data.isDirty(register.aE, doc.getLastStepCount());
                
                // Update the subregisters (e.g.: AL, AH, ... for EAX)
                // We want to know which Part is really touched
                
                // Are the low-, high- or x-parts touched?
                boolean dirtyL;
                boolean dirtyH;
                boolean dirtyX;

                // If there is a x-part, ...
                if(register.aX != null){
                	// ... we can see directly if it was touched or not.
        		dirtyX = data.isDirty(register.aX, doc.getLastStepCount());
                } else {
                	// Otherwise we just copy the state of the next wider register (E_X).
			dirtyX = dirtyE;
                }
                
                
                if(register.aL != null){
                	dirtyL = (data.isDirty(register.aL, doc.getLastStepCount()) | dirtyX);
                } else {
                	dirtyL = dirtyX;
                }
                
                
                if(register.aH != null){
                	dirtyH = (data.isDirty(register.aH, doc.getLastStepCount()) | dirtyX);
                } else {
        		dirtyH = dirtyX;
                }
                
                // Was any one of the registers touched?
                boolean dirtyAny = dirtyH | dirtyL | dirtyE;
                
                // Collapsed View
                if (dirtyAny) {
                        // If the Register is touched, the Font of the collapsed Registers is set to BOLD 
			jTextField9.setFont(jTextField9.getFont().deriveFont(java.awt.Font.BOLD));
		} else {
			// Otherwise it is displayed PLAIN
			jTextField9.setFont(jTextField9.getFont().deriveFont(java.awt.Font.PLAIN));
		}

        	// Expanded View
		update(jTextField7, value & 0xFFL, dirtyL);		// LSB
		update(jTextField5, (value >> 8) & 0xFFL, dirtyH);
		update(jTextField2, (value >> 16) & 0xFFL, dirtyE);
		update(jTextField1, (value >> 24) & 0xFFL, dirtyE);	// MSB
		
	}
	
	
	private void update(JTextField field, long value, boolean dirty) {
		if (dirty) {
			field.setFont(field.getFont().deriveFont(java.awt.Font.BOLD));
		} else {
			field.setFont(field.getFont().deriveFont(java.awt.Font.PLAIN));
		}
		field.setText(DataSpace.getString(value, 1, mode));
	}
	
	private long getValue(JTextField field) {
		String s = field.getText().toUpperCase();
		if (mode == DataSpace.HEX) {
			if (!s.startsWith("0X")) {
				s = "0X" + s;
			}
		} else if (mode == DataSpace.BIN) {
			if (!s.endsWith("B")) {
				s = s + "B";
			}
		}
		String dec = Parser.hex2dec(s);
		return Long.parseLong(dec);
	}
	
	private void edit(JTextField field, int index) {
		try {
			long fieldValue = getValue(field);
			fieldValue <<= (index * 8);
			long mask = 0xFFL << (index * 8);
			value = (value & ~mask) | (fieldValue & mask);
			data.put(value, register.aE, null);
		} catch (Exception ex) {
			// ex.printStackTrace();
		}
	}
	
	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this
	 * code. The
	 * content of this method is always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">                          
	private void initComponents() {
		jPanel25 = new javax.swing.JPanel();
		jPanel1 = new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jTextField1 = new javax.swing.JTextField();
		jPanel3 = new javax.swing.JPanel();
		jLabel2 = new javax.swing.JLabel();
		jPanel4 = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();
		jLabel3 = new javax.swing.JLabel();
		jTextField2 = new javax.swing.JTextField();
		jPanel6 = new javax.swing.JPanel();
		jLabel4 = new javax.swing.JLabel();
		jPanel13 = new javax.swing.JPanel();
		jPanel14 = new javax.swing.JPanel();
		jLabel9 = new javax.swing.JLabel();
		jTextField5 = new javax.swing.JTextField();
		jPanel15 = new javax.swing.JPanel();
		jLabel10 = new javax.swing.JLabel();
		jPanel19 = new javax.swing.JPanel();
		jPanel20 = new javax.swing.JPanel();
		jLabel13 = new javax.swing.JLabel();
		jTextField7 = new javax.swing.JTextField();
		jPanel21 = new javax.swing.JPanel();
		jLabel14 = new javax.swing.JLabel();
		jPanel26 = new javax.swing.JPanel();
		jLabel17 = new javax.swing.JLabel();
		jTextField9 = new javax.swing.JTextField();
		jButton1 = new javax.swing.JButton();
		
		setLayout(new java.awt.BorderLayout());
		
		setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
		jPanel25.setLayout(new javax.swing.BoxLayout(jPanel25, javax.swing.BoxLayout.X_AXIS));
		
		jPanel1.setLayout(new java.awt.BorderLayout());
		
		jPanel2.setLayout(new java.awt.BorderLayout());
		
		jLabel1.setText("EAX");
		jLabel1.addFocusListener(new java.awt.event.FocusAdapter() {
			
			public void focusLost(java.awt.event.FocusEvent evt) {
				jLabel1FocusLost(evt);
			}
		});
		
		jPanel2.add(jLabel1, java.awt.BorderLayout.NORTH);
		
		jPanel1.add(jPanel2, java.awt.BorderLayout.NORTH);
		
		jTextField1.setText("000");
		jTextField1.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jTextField1ActionPerformed(evt);
			}
		});
		jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
			
			public void focusLost(java.awt.event.FocusEvent evt) {
				jTextField1FocusLost(evt);
			}
		});
		
		jPanel1.add(jTextField1, java.awt.BorderLayout.CENTER);
		
		jPanel3.setLayout(new java.awt.BorderLayout());
		
		jLabel2.setText(" ");
		jPanel3.add(jLabel2, java.awt.BorderLayout.SOUTH);
		
		jPanel1.add(jPanel3, java.awt.BorderLayout.SOUTH);
		
		jPanel25.add(jPanel1);
		
		jPanel4.setLayout(new java.awt.BorderLayout());
		
		jPanel5.setLayout(new java.awt.BorderLayout());
		
		jLabel3.setText(" ");
		jPanel5.add(jLabel3, java.awt.BorderLayout.NORTH);
		
		jPanel4.add(jPanel5, java.awt.BorderLayout.NORTH);
		
		jTextField2.setText("000");
		jTextField2.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jTextField2ActionPerformed(evt);
			}
		});
		jTextField2.addFocusListener(new java.awt.event.FocusAdapter() {
			
			public void focusLost(java.awt.event.FocusEvent evt) {
				jTextField2FocusLost(evt);
			}
		});
		
		jPanel4.add(jTextField2, java.awt.BorderLayout.CENTER);
		
		jPanel6.setLayout(new java.awt.BorderLayout());
		
		jLabel4.setText(" ");
		jPanel6.add(jLabel4, java.awt.BorderLayout.SOUTH);
		
		jPanel4.add(jPanel6, java.awt.BorderLayout.SOUTH);
		
		jPanel25.add(jPanel4);
		
		jPanel13.setLayout(new java.awt.BorderLayout());
		
		jPanel14.setLayout(new java.awt.BorderLayout());
		
		jPanel14.setBackground(javax.swing.UIManager.getDefaults().getColor("ToggleButton.select"));
		jLabel9.setText("AX");
		jPanel14.add(jLabel9, java.awt.BorderLayout.NORTH);
		
		jPanel13.add(jPanel14, java.awt.BorderLayout.NORTH);
		
		jTextField5.setText("000");
		jTextField5.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jTextField5ActionPerformed(evt);
			}
		});
		jTextField5.addFocusListener(new java.awt.event.FocusAdapter() {
			
			public void focusLost(java.awt.event.FocusEvent evt) {
				jTextField5FocusLost(evt);
			}
		});
		
		jPanel13.add(jTextField5, java.awt.BorderLayout.CENTER);
		
		jPanel15.setLayout(new java.awt.BorderLayout());
		
		jPanel15.setBackground(javax.swing.UIManager.getDefaults().getColor(
			"OptionPane.warningDialog.titlePane.background"));
		jLabel10.setText("AH");
		jPanel15.add(jLabel10, java.awt.BorderLayout.SOUTH);
		
		jPanel13.add(jPanel15, java.awt.BorderLayout.SOUTH);
		
		jPanel25.add(jPanel13);
		
		jPanel19.setLayout(new java.awt.BorderLayout());
		
		jPanel20.setLayout(new java.awt.BorderLayout());
		
		jPanel20.setBackground(javax.swing.UIManager.getDefaults().getColor("ToggleButton.select"));
		jLabel13.setText(" ");
		jPanel20.add(jLabel13, java.awt.BorderLayout.NORTH);
		
		jPanel19.add(jPanel20, java.awt.BorderLayout.NORTH);
		
		jTextField7.setText("000");
		jTextField7.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jTextField7ActionPerformed(evt);
			}
		});
		jTextField7.addFocusListener(new java.awt.event.FocusAdapter() {
			
			public void focusLost(java.awt.event.FocusEvent evt) {
				jTextField7FocusLost(evt);
			}
		});
		
		jPanel19.add(jTextField7, java.awt.BorderLayout.CENTER);
		
		jPanel21.setLayout(new java.awt.BorderLayout());
		
		jPanel21.setBackground(javax.swing.UIManager.getDefaults().getColor(
			"OptionPane.questionDialog.titlePane.background"));
		jLabel14.setText("AL");
		jPanel21.add(jLabel14, java.awt.BorderLayout.SOUTH);
		
		jPanel19.add(jPanel21, java.awt.BorderLayout.SOUTH);
		
		jPanel25.add(jPanel19);
		
		add(jPanel25, java.awt.BorderLayout.CENTER);
		
		jPanel26.setLayout(new java.awt.BorderLayout());
		
		jLabel17.setText("EAX:");
		jLabel17.setPreferredSize(new java.awt.Dimension(35, 15));
		jPanel26.add(jLabel17, java.awt.BorderLayout.WEST);
		
		jTextField9.setText("0");
		jTextField9.setPreferredSize(new java.awt.Dimension(50, 19));
		jTextField9.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jTextField9ActionPerformed(evt);
			}
		});
		jTextField9.addFocusListener(new java.awt.event.FocusAdapter() {
			
			public void focusLost(java.awt.event.FocusEvent evt) {
				jTextField9FocusLost(evt);
			}
		});
		
		jPanel26.add(jTextField9, java.awt.BorderLayout.CENTER);
		
		add(jPanel26, java.awt.BorderLayout.NORTH);
		
		jButton1.setText(">");
		jButton1.setMaximumSize(new java.awt.Dimension(10, 10));
		jButton1.setMinimumSize(new java.awt.Dimension(10, 10));
		jButton1.setPreferredSize(new java.awt.Dimension(10, 10));
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});
		
		add(jButton1, java.awt.BorderLayout.WEST);
		
	}// </editor-fold>                        
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jTextField7FocusLost(java.awt.event.FocusEvent evt) {// GEN-FIRST:event_jTextField7FocusLost
		jTextField7ActionPerformed(null);
	}// GEN-LAST:event_jTextField7FocusLost
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jTextField5FocusLost(java.awt.event.FocusEvent evt) {// GEN-FIRST:event_jTextField5FocusLost
		jTextField5ActionPerformed(null);
	}// GEN-LAST:event_jTextField5FocusLost
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jTextField2FocusLost(java.awt.event.FocusEvent evt) {// GEN-FIRST:event_jTextField2FocusLost
		jTextField2ActionPerformed(null);
	}// GEN-LAST:event_jTextField2FocusLost
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jTextField1FocusLost(java.awt.event.FocusEvent evt) {// GEN-FIRST:event_jTextField1FocusLost
		jTextField1ActionPerformed(null);
	}// GEN-LAST:event_jTextField1FocusLost
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jLabel1FocusLost(java.awt.event.FocusEvent evt) {// GEN-FIRST:event_jLabel1FocusLost
	
	}// GEN-LAST:event_jLabel1FocusLost
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jTextField9FocusLost(java.awt.event.FocusEvent evt) {// GEN-FIRST:event_jTextField9FocusLost
		jTextField9ActionPerformed(null);
	}// GEN-LAST:event_jTextField9FocusLost
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jTextField7ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jTextField7ActionPerformed
		edit(jTextField7, 0);
	}// GEN-LAST:event_jTextField7ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jTextField5ActionPerformed
		edit(jTextField5, 1);
	}// GEN-LAST:event_jTextField5ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jTextField2ActionPerformed
		edit(jTextField2, 2);
	}// GEN-LAST:event_jTextField2ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jTextField1ActionPerformed
		edit(jTextField1, 3);
	}// GEN-LAST:event_jTextField1ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jTextField9ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jTextField9ActionPerformed
		try {
			long l = getValue(jTextField9);
			data.put(l, register.aE, null);
		} catch (Exception ex) {
			// ex.printStackTrace();
		}
	}// GEN-LAST:event_jTextField9ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed
	
		if (jPanel25.isVisible()) {
			jPanel25.setVisible(false);
			jPanel26.setVisible(true);
			jButton1.setText("^");
		} else {
			jPanel25.setVisible(true);
			jPanel26.setVisible(false);
			jButton1.setText(">");
		}
		this.doLayout();
		this.getParent().doLayout();
		this.getParent().doLayout();
	}// GEN-LAST:event_jButton1ActionPerformed
	
	// Variables declaration - do not modify                     
	private javax.swing.JButton jButton1;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel10;
	private javax.swing.JLabel jLabel13;
	private javax.swing.JLabel jLabel14;
	private javax.swing.JLabel jLabel17;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel9;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel13;
	private javax.swing.JPanel jPanel14;
	private javax.swing.JPanel jPanel15;
	private javax.swing.JPanel jPanel19;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel20;
	private javax.swing.JPanel jPanel21;
	private javax.swing.JPanel jPanel25;
	private javax.swing.JPanel jPanel26;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JTextField jTextField1;
	private javax.swing.JTextField jTextField2;
	private javax.swing.JTextField jTextField5;
	private javax.swing.JTextField jTextField7;
	private javax.swing.JTextField jTextField9;
	// End of variables declaration                   
	
}
