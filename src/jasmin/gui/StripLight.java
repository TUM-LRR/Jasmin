/**
 * StripLight.java Created on 17. November 2008, 03:37
 */
package jasmin.gui;

import jasmin.core.*;
import java.awt.*;
import javax.swing.JOptionPane;

/**
 * Striplight
 * 
 * @author Alexander Ried
 */
public class StripLight extends javax.swing.JPanel implements IGuiModule, IListener {
	
	private DataSpace data;
	
	// hard-coded config
	private static int DISTANCE = 5;
	private static int BORDER = 20;
	private static int BAR_WIDTH = 7;
	private static int BAR_HEIGHT = 20;
	
	// dynamic config
	private int bars = 16;
	private Address address;
	private long value;
	
	private PolygonObject[] polys;
	
	public StripLight() {
		initComponents();
	}
	
	public void setDataSpace(DataSpace dsp) {
		data = dsp;
		address = new Address(Op.MEM, (bars + 7) / 8, data.getMemAddressStart());
	}
	
	public String getTabLabel() {
		return "StripLight";
	}
	
	public void updateValue() {
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
	
	// colors
	private Color lightOn = new Color(255, 255, 0);
	private Color lightOff = darkenColor(lightOn, 0.2f);
	private Color backgroundColor = darkenColor(lightOff, 0.1f);
	
	public void paint(Graphics g) {
		if ((getWidth() == 0) || (getHeight() == 0)) {
			return;
		}
		Graphics2D graphics = (Graphics2D) g;
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// fill entire graphics-panel with panel-background
		graphics.setColor(this.getBackground());
		graphics.fillRect(0, 0, getWidth(), getHeight());
		
		int desiredWidth = bars * BAR_WIDTH + (bars - 1) * BAR_WIDTH * 2 + BORDER + BORDER;
		int desiredHeight = BAR_HEIGHT + BORDER + BORDER;
		float widthFactor = (float) this.getWidth() / desiredWidth;
		float heightFactor = (float) this.getHeight() / desiredHeight;
		
		float factor = (widthFactor < heightFactor ? widthFactor : heightFactor);
		int high = Math.round(BAR_HEIGHT * factor);
		int wid = Math.round(BAR_WIDTH * factor);
		int dist = Math.round(DISTANCE * factor);
		int bord = Math.round(BORDER * factor);
		
		// fill segment of our display with display-background
		graphics.setColor(backgroundColor);
		int totalWidth = bars * wid + (bars - 1) * dist + bord * 2;
		int totalHeight = high + bord * 2;
		graphics.fillRect(getx0(wid, dist) - bord, gety0(high) - bord, totalWidth, totalHeight);
		// paint the bars
		polys = new PolygonObject[bars];
		for (int i = bars - 1; i >= 0; i--) {
			graphics.setColor(((value >> i) & 1) == 1 ? lightOn : lightOff);
			Polygon p = createRectangle(getx0(wid, dist) + (bars - i - 1) * (dist + wid), gety0(high), wid,
				high);
			graphics.fillPolygon(p);
			long mask = (1L << i);
			polys[i] = new PolygonObject(p, address, mask);
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
	
	private Polygon createRectangle(int x0, int y0, int width, int height) {
		Polygon p = new Polygon();
		p.addPoint(x0, y0);
		p.addPoint(x0 + width, y0);
		p.addPoint(x0 + width, y0 + height);
		p.addPoint(x0, y0 + height);
		return p;
	}
	
	/**
	 * @return the first pixel to paint on (x-value) (used for centering the painting)
	 */
	private int getx0(int width, int dist) {
		return (this.getWidth() - (bars * width) - (bars - 1) * dist) / 2;
	}
	
	/**
	 * @return the first pixel to paint on (y-value) (used for centering the painting)
	 */
	private int gety0(int height) {
		return (this.getHeight() - height) / 2;
	}
	
	private void initComponents() {
		jPopupMenu = new javax.swing.JPopupMenu();
		MenuItemAddress = new javax.swing.JMenuItem();
		MenuItemDigits = new javax.swing.JMenuItem();
		
		MenuItemAddress.setText("Change address");
		MenuItemAddress.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				MenuItemAddressActionPerformed(evt);
			}
		});
		
		jPopupMenu.add(MenuItemAddress);
		
		MenuItemDigits.setText("Change digits");
		MenuItemDigits.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				MenuItemDigitsActionPerformed(evt);
			}
		});
		
		jPopupMenu.add(MenuItemDigits);
		
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
	private void MenuItemAddressActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		String newAddress = JOptionPane.showInputDialog("Please enter a new address for the display to use:",
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
				address.address = address_temp;
			}
			updateValue();
		}
		repaint();
	}
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void MenuItemDigitsActionPerformed(java.awt.event.ActionEvent evt) {// GEN
		String newDigits = JOptionPane.showInputDialog("Please enter the number of bars: (1-32)", "" + bars);
		int digits_temp = 0;
		if (newDigits != null) {
			try {
				digits_temp = new Integer(newDigits);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "The entered value was not valid!");
			}
			if ((digits_temp > 32) || (digits_temp < 1)) {
				JOptionPane.showMessageDialog(null, "The entered value was not valid!");
			} else {
				bars = digits_temp;
				address.size = (bars + 7) / 8;
			}
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
	
	private javax.swing.JMenuItem MenuItemAddress;
	private javax.swing.JMenuItem MenuItemDigits;
	private javax.swing.JPopupMenu jPopupMenu;
	
	public void clear() {
		// nothing to do here, because updateAll() gets called anyway
	}
	
	/**
	 * @param activated
	 *        the new activation state of this component
	 */
	public void setActivated(boolean activated) {
		if (activated) {
			data.addMemoryListener(this);
			updateAll();
		} else {
			data.removeMemoryListener(this);
		}
	}
	
	/**
	 * @param newValue
	 *        the new value of the memory byte at the changed address
	 */
	public void notifyChanged(int address, int newValue) {
		if (this.address.containsAddress(address)) {
			updateAll();
		}
	}
	
}
