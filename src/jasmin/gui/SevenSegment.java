/*
 * SevenSegment.java Created on 15. September 2006, 16:10
 */

package jasmin.gui;

import jasmin.core.*;
import java.awt.*;
import java.util.Random;
import javax.swing.JOptionPane;

public class SevenSegment extends javax.swing.JPanel implements IGuiModule, IListener {

	DataSpace data;
	
	// hard-coded config
	private static int width = 4; // specify HALF THE thickness here
	private static int length = 40;
	private static int distance = 1;
	private static int border = 20;
	
	// dynamic config
	private int digits = 4;
	private Address address = new Address(Op.MEM, 4, 0);
	private long value;
	
	private float factor = 1.0f;
	private int len, wid, distanceBetween;
	private int colormode;
	
	private static int COLOR_BLUE = 0;
	private static int COLOR_JASMIN = 1;
	
	private PolygonObject[] polys;
	
	/** Creates new form SevenSegment */
	public SevenSegment() {
		initComponents();
		colormode = ((new Random()).nextBoolean() ? COLOR_BLUE : COLOR_JASMIN);
	}
	
	public void setDataSpace(DataSpace dsp) {
		data = dsp;
		address.address = data.getMemAddressStart();
	}
	
	public String getTabLabel() {
		return "7-Segment";
	}
	
	private void updateValue() {
		address.size = digits;
		value = data.getUpdate(address, false);
	}
	
	public void updateAll() {
		updateValue();
		this.repaint();
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
		
		if (colormode == COLOR_BLUE) {
			digitColor = new Color(64, 144, 255);
		} else if (colormode == COLOR_JASMIN) {
			digitColor = new Color(248, 128, 224);
		}
		deadDigitColor = darkenColor(digitColor, 0.2f);
		backgroundColor = darkenColor(digitColor, 0.1f);
		
		Graphics2D graphics = (Graphics2D) g;
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(this.getBackground());
		graphics.fillRect(0, 0, getWidth(), getHeight());
		
		getFactor();
		graphics.setColor(backgroundColor);
		int totalWidth = digits * getRealDigitWidth() + (digits - 1) * distanceBetween + border + border;
		int totalHeight = getRealDigitHeight() + border + border;
		graphics.fillRect(getx0() - border, gety0() - border, totalWidth, totalHeight);
		polys = new PolygonObject[7 * digits];
		for (int i = digits - 1; i >= 0; i--) {
			Polygon[] p = constructPolygon();
			fillCoords(p, getx0() + (digits - i - 1) * (distanceBetween + getRealDigitWidth()), gety0());
			for (int j = 0; j < p.length; j++) {
				polys[i * p.length + j] = new PolygonObject(p[j], address, 1L << (i * 8 + j));
				graphics.setColor((((value >> (i * 8 + j)) & 1) == 1 ? digitColor : deadDigitColor));
				graphics.fillPolygon(p[j]);
			}
		}
	}
	
	private static Polygon[] constructPolygon() {
		Polygon[] pa = new Polygon[7];
		for (int i = 0; i < pa.length; i++) {
			pa[i] = new Polygon();
			pa[i].xpoints = new int[6];
			pa[i].ypoints = new int[6];
			pa[i].npoints = 6;
		}
		return pa;
	}
	
	private void fillCoords(Polygon[] p, int x0, int y0) {
		
		int left = wid + distance + x0;
		int right = left + len;
		int top = y0;
		int bottom = y0 + wid + wid;
		setc(p, 0, 0, left, top + wid);
		setc(p, 0, 1, left + wid, bottom);
		setc(p, 0, 2, right - wid, bottom);
		setc(p, 0, 3, right, top + wid);
		setc(p, 0, 4, right - wid, top);
		setc(p, 0, 5, left + wid, top);
		
		top = wid + distance + y0;
		bottom = top + len;
		left = x0;
		right = x0 + wid + wid;
		setc(p, 5, 0, left + wid, top);
		setc(p, 5, 1, left, top + wid);
		setc(p, 5, 2, left, bottom - wid);
		setc(p, 5, 3, left + wid, bottom);
		setc(p, 5, 4, right, bottom - wid);
		setc(p, 5, 5, right, top + wid);
		
		// middle
		shiftc(p, 0, 6, 0, len + distance + distance);
		// bottom
		shiftc(p, 6, 3, 0, len + distance + distance);
		// bottom left
		shiftc(p, 5, 4, 0, len + distance + distance);
		// top right
		shiftc(p, 5, 1, len + distance + distance, 0);
		// bottom right
		shiftc(p, 1, 2, 0, len + distance + distance);
		
	}
	
	private static void setc(Polygon[] p, int segment, int pointnumber, int x, int y) {
		p[segment].xpoints[pointnumber] = x;
		p[segment].ypoints[pointnumber] = y;
	}
	
	private static void shiftc(Polygon[] p, int sourcesegment, int destsegment, int xshift, int yshift) {
		for (int i = 0; i < 6; i++) {
			p[destsegment].xpoints[i] = p[sourcesegment].xpoints[i] + xshift;
			p[destsegment].ypoints[i] = p[sourcesegment].ypoints[i] + yshift;
		}
	}
	
	private static int getDefaultDigitWidth() {
		return width + distance + length + distance + width;
	}
	
	private static int getDefaultDigitHeight() {
		return width + distance + length + distance + distance + length + distance + width;
	}
	
	private int getRealDigitWidth() {
		return wid + distance + len + distance + wid;
	}
	
	private int getRealDigitHeight() {
		return wid + distance + len + distance + distance + len + distance + wid;
	}
	
	private void getFactor() {
		int desiredWidth = digits * getDefaultDigitWidth() + (digits - 1) * width * 2 + border + border;
		int desiredHeight = getDefaultDigitHeight() + border + border;
		float widthFactor = (float) this.getWidth() / desiredWidth;
		float heightFactor = (float) this.getHeight() / desiredHeight;
		
		factor = (widthFactor < heightFactor ? widthFactor : heightFactor);
		len = Math.round(length * factor);
		wid = Math.round(width * factor);
		distanceBetween = wid + wid;
	}
	
	private int getx0() {
		return (this.getWidth() - digits * getRealDigitWidth() - (digits - 1) * distanceBetween) / 2;
	}
	
	private int gety0() {
		return (this.getHeight() - getRealDigitHeight()) / 2;
	}
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	private void initComponents() {
		jPopupMenu = new javax.swing.JPopupMenu();
		menuItemAddress = new javax.swing.JMenuItem();
		menuItemDigits = new javax.swing.JMenuItem();
		menuItemColor = new javax.swing.JMenuItem();
		
		menuItemAddress.setText("Change address");
		menuItemAddress.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuItemAddressActionPerformed(evt);
			}
		});
		
		jPopupMenu.add(menuItemAddress);
		
		menuItemDigits.setText("Change digits");
		menuItemDigits.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				menuItemDigitsActionPerformed(evt);
			}
		});
		
		jPopupMenu.add(menuItemDigits);
		
		menuItemColor.setText("Change color");
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
		String newAddress = JOptionPane.showInputDialog("Please enter a new address for the display to use:",
			"0x" + Integer.toHexString(address.address));
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
				address = new Address(Op.MEM, digits, address_temp);
			}
			updateValue();
		}
		repaint();
	}
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void menuItemDigitsActionPerformed(java.awt.event.ActionEvent evt) {
		String newDigits = JOptionPane.showInputDialog("Please enter the number of digits: (1-8)",
			"" + digits);
		int digits_temp = 0;
		if (newDigits != null) {
			try {
				digits_temp = new Integer(newDigits);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "The entered value was not valid!");
			}
			if ((digits_temp > 8) || (digits_temp < 1)) {
				JOptionPane.showMessageDialog(null, "The entered value was not valid!");
			} else {
				digits = digits_temp;
			}
		}
		updateAll();
	}
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void menuItemColorActionPerformed(java.awt.event.ActionEvent evt) {
		String[] options = { "blue", "jasmin", "Cancel" };
		String msg = "Choose the color:";
		String title = "Please choose";
		int choice = JOptionPane.showOptionDialog(null, msg, title, JOptionPane.DEFAULT_OPTION,
			JOptionPane.QUESTION_MESSAGE, null, options, null);
		
		if (choice == 0) {
			colormode = COLOR_BLUE;
		} else if (choice == 1) {
			colormode = COLOR_JASMIN;
		}
		updateAll();
	}
	
	private void formMouseClicked(java.awt.event.MouseEvent evt) {
		if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
			jPopupMenu.show(this, evt.getX(), evt.getY());
		} else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {
			processLeftClick(evt);
		}
	}
	
	private void processLeftClick(java.awt.event.MouseEvent evt) {
		for (PolygonObject p : polys) {
			if (p.poly.contains(evt.getPoint())) {
				p.invertBit(data);
				return;
			}
		}
	}
	
	// Variables declaration
	private javax.swing.JMenuItem menuItemAddress;
	private javax.swing.JMenuItem menuItemDigits;
	private javax.swing.JMenuItem menuItemColor;
	private javax.swing.JPopupMenu jPopupMenu;
	
	public void clear() {
		// nothing to see here, move on!
	}
	
	/**
	 * @param activated
	 *        the new activation state of this component
	 */
	public void setActivated(boolean activated) {
		if (activated) {
			data.addMemoryListener(this);
		} else {
			data.removeMemoryListener(this);
		}
	}
	
	/**
	 * @param newValue
	 */
	public void notifyChanged(int address, int newValue) {
		if (this.address.containsAddress(address)) {
			updateAll();
		}
	}
	
}
