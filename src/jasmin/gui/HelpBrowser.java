/*
 * HelpBrowser.java
 *
 * Created on 28. April 2006, 19:39
 */

package jasmin.gui;

import jasmin.core.Parser;
import java.awt.*;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

/**
 * @author Kai Orend
 */
public class HelpBrowser extends javax.swing.JPanel {
	
	private static final long serialVersionUID = 1611148227929564585L;
	
	private MainFrame mframe = null;
	LinkedList<URL> history;
	LinkedList<URL> forwardhistory;
	URL currentURL = null;
	String[] fontNames = null;
	
	public JComboBox getFontBox() {
		return fontBox;
	}
	
	/** Creates new form HelpBrowser */
	public HelpBrowser(MainFrame frame) {
		this.mframe = frame;
		
		initComponents();
		HtmlPane.setContentType("text/html");
		HtmlPane.setEditable(false);
		history = new LinkedList<URL>();
		forwardhistory = new LinkedList<URL>();
		HTMLEditorKit editorKit = createEditorKit();
		HtmlPane.setEditorKit(editorKit);
		fontNames = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		fontBox.setModel(new ComboBoxModel() {
			
			/**
			 * @param l
			 */
			public void addListDataListener(ListDataListener l) {
			}
			
			public Object getElementAt(int index) {
				return fontNames[index];
			}
			
			public Object getSelectedItem() {
				String font = mframe.getProperty("font");
				if (font == null) {
					font = "Monospaced";
				}
				return font;
			}
			
			public int getSize() {
				return fontNames.length;
			}
			
			/**
			 * @param l
			 */
			public void removeListDataListener(ListDataListener l) {
			}
			
			public void setSelectedItem(Object anItem) {
				mframe.putProperty("font", anItem.toString());
			}
		});
		fontBox.setRenderer(new ListCellRenderer() {
			
			/**
			 * @param list
			 * @param index
			 * @param isSelected
			 * @param cellHasFocus
			 */
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Font font = new Font(value.toString(), Font.PLAIN, 12);
				JLabel label = new JLabel();
				label.setText(value.toString());
				label.setFont(font);
				return label;
			}
		});
		DefaultComboBoxModel languageModel = new DefaultComboBoxModel();
		Iterator<String> iter = mframe.helpLoader.getLanguages().iterator();
		int select = 0;
		int index = 0;
		while (iter.hasNext()) {
			
			String l = iter.next().toString();
			languageModel.addElement(l);
			if (l.equalsIgnoreCase(mframe.getProperty("language"))) {
				select = index;
			}
			index++;
		}
		
		languageSelector.setModel(languageModel);
		if (index > 0) {
			languageSelector.setSelectedIndex(select);
		}
	}
	
	public HTMLEditorKit createEditorKit() {
		return new HTMLEditorKit() {
			
			private static final long serialVersionUID = 1810506704478973003L;
			
			public ViewFactory getViewFactory() {
				return new HTMLEditorKit.HTMLFactory() {
					
					public View create(Element elem) {
						Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);
						if (o instanceof HTML.Tag) {
							HTML.Tag kind = (HTML.Tag) o;
							if (kind.toString().equalsIgnoreCase("fontbox")) {
								return new ComponentView(elem) {
									
									protected Component createComponent() {
										return fontBox;
									}
								};
							} else if (kind.toString().equalsIgnoreCase("fontsizechooser")) {
								return new ComponentView(elem) {
									
									protected Component createComponent() {
										fontSizeChooser.setValue(mframe.getProperty("font.size", 12));
										return fontSizeChooser;
									}
								};
							} else if (kind.toString().equalsIgnoreCase("memorySizeChooser")) {
								return new ComponentView(elem) {
									
									protected Component createComponent() {
										memorySizeChooser.setValue(mframe.getProperty("memory", 1024));
										return memorySizeChooser;
									}
								};
							} else if (kind.toString().equalsIgnoreCase("languageSelector")) {
								return new ComponentView(elem) {
									
									protected Component createComponent() {
										// languageSelector.setValue((Integer)mframe.getProperty("language",1024));
										return languageSelector;
									}
								};
							} else if (kind.toString().equalsIgnoreCase("offsetchooser")) {
								return new ComponentView(elem) {
									
									protected Component createComponent() {
										offsetTextField.setText("" + mframe.getProperty("offset", 0));
										return offsetTextField;
									}
								};
							}
						}
						return super.create(elem);
					}
					
				};
				
			}
		};
		
	}
	
	/**
	 * @param text
	 */
	public void setText(String text) {
		HtmlPane.setText(text);
	}
	
	/**
	 * @param url
	 */
	public void openUrl(URL url) {
		System.out.println("HelpBrowser: openURL " + url);
		forwardhistory.clear();
		String s = url.toString();
		if (s.endsWith("#openFile")) {
			mframe.open();
		} else if (s.endsWith("#new")) {
			mframe.newDocument();
		} else {
			
			try {
				HtmlPane.setContentType("text/html");
				HtmlPane.setPage(url);
			} catch (Exception ex) {
				HtmlPane.setText("URL could not be resolved: " + url + "\n" + ex);
			}
		}
		if (currentURL != null) {
			history.add(currentURL);
		}
		currentURL = url;
		mframe.checkButtonStates();
		
	}
	
	public boolean canBack() {
		return !history.isEmpty();
	}
	
	public boolean canForward() {
		return !forwardhistory.isEmpty();
	}
	
	public void back() {
		URL url = history.removeLast();
		forwardhistory.add(currentURL);
		try {
			HtmlPane.setContentType("text/html");
			HtmlPane.setPage(url);
			currentURL = url;
		} catch (Exception ex) {
			HtmlPane.setText("URL could not be resolved: " + url);
		}
		mframe.checkButtonStates();
	}
	
	public void forward() {
		URL url = forwardhistory.removeLast();
		history.add(currentURL);
		try {
			HtmlPane.setContentType("text/html");
			HtmlPane.setPage(url);
			currentURL = url;
		} catch (Exception ex) {
			HtmlPane.setText("URL could not be resolved: " + url);
		}
		mframe.checkButtonStates();
	}
	
	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
	 * content of this method is always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
	private void initComponents() {
		fontBox = new javax.swing.JComboBox();
		fontSizeChooser = new javax.swing.JSpinner();
		memorySizeChooser = new javax.swing.JSpinner();
		languageSelector = new javax.swing.JComboBox();
		offsetTextField = new javax.swing.JTextField();
		jScrollPane1 = new javax.swing.JScrollPane();
		HtmlPane = new javax.swing.JEditorPane();
		
		fontBox.setMaximumRowCount(12);
		fontBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
		fontSizeChooser.addChangeListener(new javax.swing.event.ChangeListener() {
			
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
				fontSizeChooserStateChanged(evt);
			}
		});
		
		memorySizeChooser.addChangeListener(new javax.swing.event.ChangeListener() {
			
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
				memorySizeChooserStateChanged(evt);
			}
		});
		
		languageSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
		languageSelector.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				languageSelectorActionPerformed(evt);
			}
		});
		
		offsetTextField.setText("jTextField1");
		offsetTextField.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				offsetTextFieldActionPerformed(evt);
			}
		});
		
		setLayout(new java.awt.BorderLayout());
		
		HtmlPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
			
			public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
				HtmlPaneHyperlinkUpdate(evt);
			}
		});
		
		jScrollPane1.setViewportView(HtmlPane);
		
		add(jScrollPane1, java.awt.BorderLayout.CENTER);
		
	}// </editor-fold>//GEN-END:initComponents
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void offsetTextFieldActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_offsetTextFieldActionPerformed
		int value = 0;
		String dec = Parser.hex2dec(offsetTextField.getText().toUpperCase());
		try {
			value = Integer.parseInt(dec);
			mframe.putProperty("offset", value);
		} catch (Exception ex) {
			
		}
		offsetTextField.setText(mframe.getProperty("offset"));
	}// GEN-LAST:event_offsetTextFieldActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void memorySizeChooserStateChanged(javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_memorySizeChooserStateChanged
		mframe.putProperty("memory", ((Integer) memorySizeChooser.getValue()).intValue());
	}// GEN-LAST:event_memorySizeChooserStateChanged
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void fontSizeChooserStateChanged(javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_fontSizeChooserStateChanged
		mframe.putProperty("font.size", ((Integer) fontSizeChooser.getValue()).intValue());
	}// GEN-LAST:event_fontSizeChooserStateChanged
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void languageSelectorActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_languageSelectorActionPerformed
		mframe.putProperty("language", languageSelector.getSelectedItem() + "");
		mframe.helpLoader.reInit(languageSelector.getSelectedItem().toString());
	}// GEN-LAST:event_languageSelectorActionPerformed
	
	private void HtmlPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {// GEN-FIRST:event_HtmlPaneHyperlinkUpdate
		if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			URL url = evt.getURL();
			if (url != null) {
				openUrl(url);
			}
		}
	}// GEN-LAST:event_HtmlPaneHyperlinkUpdate
	
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JEditorPane HtmlPane;
	private javax.swing.JComboBox fontBox;
	private javax.swing.JSpinner fontSizeChooser;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JComboBox languageSelector;
	private javax.swing.JSpinner memorySizeChooser;
	private javax.swing.JTextField offsetTextField;
	// End of variables declaration//GEN-END:variables
	
}
