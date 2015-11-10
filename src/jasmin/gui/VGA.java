/*
 * VGA.java 
 * 
 * (c) Jakob Kummerow 
 * 
 * Created on 2008-11-21
 */

package jasmin.gui;

import jasmin.core.*;
import java.awt.*;
import java.util.Random;
import javax.swing.JOptionPane;

public class VGA extends javax.swing.JPanel implements IGuiModule, IListener {
	
	private static final long serialVersionUID = -7941383198012026141L;
	
	DataSpace data;
	
	private static int MODE_BINARY = 0;
	private static int MODE_8COLOR = 1;
	private static int MODE_TRUECOLOR = 2;
	
	private static int COLOR_BLUE = 0;
	private static int COLOR_JASMIN = 1;
	
	private int pixelwidth;
	private int pixeldistance;
	
	private int screenwidth = 16;
	private int screenheight = 16;
	private Address address = new Address(Op.MEM, 4, 0);
	private int mode = -1;
	private int colormode;
	
	// internal values
	private int x0, y0;
	private boolean[][] binColorValues;
	private boolean[][][] eightColorValues;
	private short[][][] trueColorValues;
	private int maxAddress;
	private int numBytes;
	private boolean visible = false;
	
	private PolygonObject[][] polys;
	
	public VGA() {
		initComponents();
		colormode = ((new Random()).nextBoolean() ? COLOR_BLUE : COLOR_JASMIN);
	}
	
	public void clear() {
		updateAndRepaint();
	}
	
	public String getTabLabel() {
		return "Graphics";
	}
	
	public void setActivated(boolean activated) {
		if (activated == visible) {
			return;
		}
		visible = activated;
		if (activated) {
			updateAndRepaint();
			data.addMemoryListener(this);
		} else {
			data.removeMemoryListener(this);
		}
	}
	
	public void setDataSpace(DataSpace dsp) {
		data = dsp;
		address.address = data.getMemAddressStart();
		address.size = 1;
		maxAddress = data.getMemAddressStart() + data.getMEMSIZE();
		mode = MODE_BINARY;
		calcNumBytes();
		updateValue();
	}
	
	public void notifyChanged(int changedAddress, int newValue) {
		
		if ((changedAddress >= address.address) && (changedAddress < address.address + numBytes)) {
			if (mode == MODE_BINARY) {
				int y = (changedAddress - address.address) * 8 / screenwidth;
				int x = (changedAddress - address.address) * 8 - y * screenwidth;
				for (int j = 0; j < 8; j++) {
					if ((x < screenwidth) && (y < screenheight)) {
						binColorValues[x][y] = (((newValue >> j) & 1) == 1);
						paintPixel(x, y, (Graphics2D) getGraphics());
						x++;
						if (x >= screenwidth) {
							x = 0;
							y++;
						}
					}
				}
			} else if (mode == MODE_8COLOR) {
				int y = (changedAddress - address.address) * 2 / screenwidth;
				int x = (changedAddress - address.address) * 2 - y * screenwidth;
				for (int j = 0; j < 2; j++) {
					if ((x < screenwidth) && (y < screenheight)) {
						for (int k = 0; k < 3; k++) {
							eightColorValues[x][y][k] = (((newValue >> (j * 4 + k)) & 1) == 1);
						}
						paintPixel(x, y, (Graphics2D) getGraphics());
						x++;
						if (x >= screenwidth) {
							x = 0;
							y++;
						}
					}
				}
			} else if (mode == MODE_TRUECOLOR) {
				int y = (changedAddress - address.address) / 4 / screenwidth;
				int x = (changedAddress - address.address) / 4 - y * screenwidth;
				int k = (changedAddress - address.address) % 4;
				if (k != 3) {
					trueColorValues[x][y][k] = (short) newValue;
					paintPixel(x, y, (Graphics2D) getGraphics());
				}
			}
		}
	}
	
	private void calcNumBytes() {
		numBytes = screenwidth * screenheight;
		if (mode == MODE_BINARY) {
			numBytes /= 8;
		} else if (mode == MODE_8COLOR) {
			numBytes /= 2;
		} else if (mode == MODE_TRUECOLOR) {
			numBytes *= 4;
		}
	}
	
	private void getFactor() {
		if ((screenwidth > 160) || (screenheight > 120)) {
			pixeldistance = 0;
		} else {
			pixeldistance = 1;
		}
		int availableWidth = this.getWidth() - (screenwidth + 1) * pixeldistance;
		int availableHeight = this.getHeight() - (screenheight + 1) * pixeldistance;
		int pw = (availableWidth / (screenwidth + 2));
		int ph = (availableHeight / (screenheight + 2));
		
		pixelwidth = Math.min(pw, ph);
		x0 = (getWidth() - screenwidth * (pixelwidth + pixeldistance)) / 2;
		y0 = (getHeight() - screenheight * (pixelwidth + pixeldistance)) / 2;
	}
	
	private short getByte(Address address) {
		if (address.address < maxAddress) {
			return (short) data.getUpdate(address, false);
		} else {
			return 0;
		}
	}
	
	public void updateValue() {
		int numPixels = screenwidth * screenheight;
		int origAddress = address.address;
		if (mode == MODE_BINARY) {
			eightColorValues = null;
			trueColorValues = null;
			binColorValues = new boolean[screenwidth][screenheight];
			polys = new PolygonObject[screenwidth][screenheight];
			int bytes = (numPixels + 7) / 8;
			int x = 0, y = 0;
			for (int i = 0; i < bytes; i++) {
				short currentByte = getByte(address);
				Address currentAddress = new Address(address.type, address.size, address.address);
				for (int j = 0; j < 8; j++) {
					if ((x < screenwidth) && (y < screenheight)) {
						binColorValues[x][y] = (((currentByte >> j) & 1) == 1);
						polys[x][y] = new PolygonObject(currentAddress, 1 << j);
						x++;
						if (x >= screenwidth) {
							x = 0;
							y++;
						}
					}
				}
				address.address++;
			}
		} else if (mode == MODE_8COLOR) {
			binColorValues = null;
			trueColorValues = null;
			eightColorValues = new boolean[screenwidth][screenheight][3];
			int bytes = (numPixels + 1) / 2;
			int x = 0, y = 0;
			for (int i = 0; i < bytes; i++) {
				short currentByte = getByte(address);
				for (int j = 0; j < 2; j++) {
					if ((x < screenwidth) && (y < screenheight)) {
						for (int k = 0; k < 3; k++) {
							eightColorValues[x][y][k] = (((currentByte >> (j * 4 + k)) & 1) == 1);
						}
						x++;
						if (x >= screenwidth) {
							x = 0;
							y++;
						}
					}
				}
				address.address++;
			}
		} else if (mode == MODE_TRUECOLOR) {
			binColorValues = null;
			eightColorValues = null;
			trueColorValues = new short[screenwidth][screenheight][3];
			for (int y = 0; y < screenheight; y++) {
				for (int x = 0; x < screenwidth; x++) {
					for (int k = 0; k < 3; k++) {
						short currentByte = getByte(address);
						trueColorValues[x][y][k] = currentByte;
						address.address++;
					}
					address.address++; // skip 4th byte per pixel to avoid "packed rgb" storage
				}
			}
		}
		address.address = origAddress;
	}
	
	private void updateAndRepaint() {
		updateValue();
		this.repaint();
	}
	
	public void updateAll() {
		// do nothing (all actions are triggered via listener notification)
	}
	
	private static Color darkenColor(Color original, float brightness) {
		int r = Math.round(original.getRed() * brightness);
		int g = Math.round(original.getGreen() * brightness);
		int b = Math.round(original.getBlue() * brightness);
		return new Color(r, g, b);
	}
	
	private Color digitColor;
	private Color deadDigitColor;
	private Color backgroundColor;
	
	public void paint(Graphics g) {
		if ((getWidth() == 0) || (getHeight() == 0)) {
			return;
		}
		Graphics2D graphics = (Graphics2D) g;
		
		if (mode == MODE_BINARY) {
			if (colormode == COLOR_BLUE) {
				digitColor = new Color(64, 144, 255);
			} else if (colormode == COLOR_JASMIN) {
				digitColor = new Color(248, 128, 224);
			}
			deadDigitColor = darkenColor(digitColor, 0.2f);
			backgroundColor = darkenColor(digitColor, 0.1f);
		}
		
		getFactor();
		graphics.setColor(this.getBackground());
		graphics.fillRect(0, 0, getWidth(), getHeight());
		graphics.setColor(backgroundColor);
		graphics.fillRect(x0 - pixelwidth, y0 - pixelwidth, (screenwidth + 2) * (pixelwidth + pixeldistance) - 3 * pixeldistance,
			(screenheight + 2) * (pixelwidth + pixeldistance) - 3 * pixeldistance);
		
		for (int x = 0; x < screenwidth; x++) {
			for (int y = 0; y < screenheight; y++) {
				paintPixel(x, y, graphics);
			}
		}
		
	}
	
	private void paintPixel(int x, int y, Graphics2D graphics) {
		if (mode == MODE_BINARY) {
			if (binColorValues[x][y]) {
				graphics.setColor(digitColor);
			} else {
				graphics.setColor(deadDigitColor);
			}
		} else if (mode == MODE_8COLOR) {
			graphics.setColor(new Color(eightColorValues[x][y][0] ? 255 : 0, eightColorValues[x][y][1] ? 255 : 0,
				eightColorValues[x][y][2] ? 255 : 0));
		} else if (mode == MODE_TRUECOLOR) {
			graphics.setColor(new Color(trueColorValues[x][y][0], trueColorValues[x][y][1], trueColorValues[x][y][2]));
		}
		Polygon p = createRectangle(x0 + x * (pixelwidth + pixeldistance), y0 + y * (pixelwidth + pixeldistance), pixelwidth,
			pixelwidth);
		polys[x][y].poly = p;
		graphics.fillPolygon(p);
	}
	
	private Polygon createRectangle(int x0, int y0, int width, int height) {
		Polygon p = new Polygon();
		p.addPoint(x0, y0);
		p.addPoint(x0 + width, y0);
		p.addPoint(x0 + width, y0 + height);
		p.addPoint(x0, y0 + height);
		return p;
	}
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	private void initComponents() {
		jPopupMenu = new javax.swing.JPopupMenu();
		menuItemAddress = new javax.swing.JMenuItem();
		menuItemWidth = new javax.swing.JMenuItem();
		menuItemHeight = new javax.swing.JMenuItem();
		menuItemMode = new javax.swing.JMenuItem();
		menuItemColor = new javax.swing.JMenuItem();
		
		menuItemAddress.setText("Change address");
		menuItemAddress.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuItemAddressActionPerformed(evt);
			}
		});
		
		jPopupMenu.add(menuItemAddress);
		
		menuItemWidth.setText("Change width");
		menuItemWidth.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuItemWidthActionPerformed(evt);
			}
		});
		
		jPopupMenu.add(menuItemWidth);
		
		menuItemHeight.setText("Change height");
		menuItemHeight.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuItemHeightActionPerformed(evt);
			}
		});
		
		jPopupMenu.add(menuItemHeight);
		
		menuItemMode.setText("Change color mode");
		menuItemMode.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuItemModeActionPerformed(evt);
			}
		});
		
		jPopupMenu.add(menuItemMode);
		
		menuItemColor.setText("Change binary color");
		menuItemColor.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuItemColorActionPerformed(evt);
			}
		});
		
		jPopupMenu.add(menuItemColor);
		
		setLayout(null);
		
		addMouseListener(new java.awt.event.MouseAdapter() {
			
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				formMouseClicked(evt);
			}
		});
		
	}
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void menuItemAddressActionPerformed(java.awt.event.ActionEvent evt) {
		String newAddress = JOptionPane.showInputDialog("Please enter a new address for the display to use:", "0x"
			+ Integer.toHexString(address.address));
		int address_temp = 0;
		if (newAddress != null) {
			try {
				address_temp = new Integer(Parser.hex2dec(newAddress.toUpperCase())).intValue();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "The entered value was not valid!");
			}
			if ((address_temp < data.getMemAddressStart()) || (address_temp > data.getMemAddressStart() + data.getMEMSIZE())) {
				JOptionPane.showMessageDialog(null, "The entered value was not valid!");
			} else {
				address = new Address(Op.MEM, 1, address_temp);
			}
			updateAndRepaint();
		}
	}
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void menuItemWidthActionPerformed(java.awt.event.ActionEvent evt) {
		String newDigits = JOptionPane.showInputDialog("Please enter the width in pixels: (default 16)", "" + screenwidth);
		int digits_temp = 0;
		if (newDigits != null) {
			try {
				digits_temp = new Integer(newDigits).intValue();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "The entered value was not valid!");
			}
			screenwidth = digits_temp;
			calcNumBytes();
		}
		updateAndRepaint();
	}
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void menuItemHeightActionPerformed(java.awt.event.ActionEvent evt) {
		String newDigits = JOptionPane.showInputDialog("Please enter the height in pixels: (default 16)", "" + screenheight);
		int digits_temp = 0;
		if (newDigits != null) {
			try {
				digits_temp = new Integer(newDigits).intValue();
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "The entered value was not valid!");
			}
			screenheight = digits_temp;
			calcNumBytes();
		}
		updateAndRepaint();
	}
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void menuItemModeActionPerformed(java.awt.event.ActionEvent evt) {
		String[] options = { "Binary", "8 Colors", "TrueColor", "Cancel" };
		String msg = "Choose the color mode:";
		String title = "Please choose";
		int choice = JOptionPane.showOptionDialog(null, msg, title, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
			null, options, null);
		
		if (choice == 0) {
			mode = MODE_BINARY;
		} else if (choice == 1) {
			mode = MODE_8COLOR;
		} else if (choice == 2) {
			mode = MODE_TRUECOLOR;
		}
		calcNumBytes();
		updateAndRepaint();
	}
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void menuItemColorActionPerformed(java.awt.event.ActionEvent evt) {
		String[] options = { "blue", "jasmin", "Cancel" };
		String msg = "Choose the color:";
		String title = "Please choose";
		int choice = JOptionPane.showOptionDialog(null, msg, title, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
			null, options, null);
		
		if (choice == 0) {
			colormode = COLOR_BLUE;
		} else if (choice == 1) {
			colormode = COLOR_JASMIN;
		}
		repaint();
	}
	
	private void formMouseClicked(java.awt.event.MouseEvent evt) {
		if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
			jPopupMenu.show(this, evt.getX(), evt.getY());
		} else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {
			processLeftClick(evt);
		}
	}
	
	private void processLeftClick(java.awt.event.MouseEvent evt) {
		if (mode == MODE_BINARY) {
			int y = 0;
			for (int j = 0; j < polys[0].length; j++) {
				if (polys[0][j].poly.intersects(0, evt.getY(), this.getWidth(), 1)) {
					y = j;
				}
			}
			for (int x = 0; x < polys.length; x++) {
				if (polys[x][y].poly.contains(evt.getPoint())) {
					polys[x][y].invertBit(data);
				}
			}
		}
	}
	
	// Variables declaration
	private javax.swing.JMenuItem menuItemAddress;
	private javax.swing.JMenuItem menuItemWidth;
	private javax.swing.JMenuItem menuItemHeight;
	private javax.swing.JMenuItem menuItemMode;
	private javax.swing.JMenuItem menuItemColor;
	private javax.swing.JPopupMenu jPopupMenu;
	
}
