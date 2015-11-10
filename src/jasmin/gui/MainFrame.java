/*
 * mainframe.java Created on 16. März 2006, 17:23
 */

package jasmin.gui;

import jasmin.core.HelpLoader;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.Properties;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.AbstractDocument;
import javax.swing.undo.*;

/**
 * @author Kai Orend
 */
public class MainFrame extends javax.swing.JFrame {
	
	private static final long serialVersionUID = 1L;
	private JasDocument document = null;
	private HelpBrowser helpDocument = null;
	public JFileChooser fileChooser = new JFileChooser();
	public HelpLoader helpLoader = null;
	private Properties properties;
	
	/** Creates new form mainframe */
	public MainFrame() {
		File propfile = new File(System.getProperty("user.home") + File.separator + ".jasmin");
		try {
			properties = new Properties();
			
			if (!propfile.exists()) {
				propfile.createNewFile();
				putProperty("font", "Sans Serif");
				putProperty("font.size", "12");
				putProperty("memory", "4096");
				putProperty("language", "en");
				properties.store(new FileOutputStream(propfile), "Jasmin configuration file");
			} else {
				properties.load(new FileInputStream(propfile));
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Could not open:" + propfile.toString() + "\n"
				+ ex.toString());
			System.exit(1);
		}
		
		initComponents();
		helpLoader = new HelpLoader(getProperty("language"));
		
		checkButtonStates();
		addHelp(getClass().getResource("/jasmin/gui/resources/Welcome.htm"), "Welcome");
		this.setExtendedState(this.getExtendedState() | Frame.MAXIMIZED_BOTH);
		
		String lastpath = getProperty("lastpath");
		if (lastpath != null) {
			fileChooser.setSelectedFile(new File(lastpath));
		}
		
		jMenuItemUndo.setAccelerator(KeyStroke.getKeyStroke('Z', Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask()));
		jMenuItemRedo.setAccelerator(KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask()));
		
		jMenuItem5.setAccelerator(KeyStroke.getKeyStroke('N', Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask()));
		jMenuItem3.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask()));
		jMenuItem4.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask()));
		
		jMenuItem13.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		jMenuItem15.setAccelerator(KeyStroke.getKeyStroke('P', Toolkit.getDefaultToolkit()
			.getMenuShortcutKeyMask()));
		
		jMenuItem16.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		
		jMenuItem14.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
	}
	
	public void changeLnF(String LnF) {
		
		try {
			
			UIManager.setLookAndFeel(LnF);
			
			SwingUtilities.updateComponentTreeUI(this);
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, LnF, e.toString(), JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * @param key
	 */
	public String getProperty(String key) {
		String result = properties.getProperty(key);
		return result;
	}
	
	public int getProperty(String key, int oldvalue) {
		String result = properties.getProperty(key);
		if (result == null) {
			putProperty(key, oldvalue);
			return oldvalue;
		}
		return Integer.parseInt(result);
	}
	
	public void putProperty(String key, String value) {
		properties.setProperty(key, value);
	}
	
	public void putProperty(String key, int value) {
		properties.setProperty(key, "" + value);
	}
	
	/**
	 * @param url
	 * @param title
	 */
	public void addHelp(URL url, String title) {
		HelpBrowser newbrowser = new HelpBrowser(this);
		newbrowser.openUrl(url);
		DocTab.add(title, newbrowser);
		DocTab.setSelectedComponent(newbrowser);
		helpDocument = newbrowser;
	}
	
	/**
	 * Update all Button states.
	 */
	public synchronized void checkButtonStates() {
		if ((document != null) && !document.running) {
			jButton16.setEnabled(true);
			jButton17.setEnabled(document.hasSnapshot());
			
			jMenu2.setEnabled(true);
			jButton6.setEnabled(false);
			jButton14.setEnabled(false);
			
			jButton15.setEnabled(true);
			
			if (!jButton12.isEnabled()) {
				jButton12.setEnabled(true);
			}
			if (!jMenuItem4.isEnabled()) {
				jMenuItem4.setEnabled(true);
			}
			if (!jMenuItem6.isEnabled()) {
				jMenuItem6.setEnabled(true);
			}
			if (!jButton2.isEnabled()) {
				jButton2.setEnabled(true);
			}
			if (!jMenuItem10.isEnabled()) {
				jMenuItem10.setEnabled(true);
			}
			if (!jMenu3.isEnabled()) {
				jMenu3.setEnabled(true);
			}
			if (!jMenuItem2.isEnabled()) {
				jMenuItem2.setEnabled(true);
			}
			if (!jButton13.isEnabled()) {
				jButton13.setEnabled(true);
			}
			if (!jButton4.isEnabled()) {
				jButton4.setEnabled(true);
			}
			if (!jButton5.isEnabled()) {
				jButton5.setEnabled(true);
			}
			if (!jButton7.isEnabled()) {
				jButton7.setEnabled(true);
			}
			if (!document.getEditor().isEnabled()) {
				document.getEditor().setEnabled(true);
			}
			
			if (jButton8.isEnabled() != (document.undoManager.canUndo())) {
				jButton8.setEnabled((document.undoManager.canUndo()));
				jMenuItemUndo.setEnabled((document.undoManager.canUndo()));
			}
			if (jButton9.isEnabled() != (document.undoManager.canRedo())) {
				jButton9.setEnabled((document.undoManager.canRedo()));
				jMenuItemRedo.setEnabled((document.undoManager.canRedo()));
			}
			
			boolean hasSelection = ((document.getEditor().getSelectionEnd()
				- document.getEditor().getSelectionStart()) > 0);
			if (jButton11.isEnabled() != hasSelection) {
				jButton11.setEnabled(hasSelection);
				jButton10.setEnabled(hasSelection);
				jMenuItem8.setEnabled(hasSelection);
				jMenuItem9.setEnabled(hasSelection);
			}
			
			jMenuItem15.setEnabled(document.running);
			jMenuItem13.setEnabled(!document.running);
			
			jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/jasmin/gui/resources/icons/play_green.png")));
			
		} else {
			jButton16.setEnabled(false);
			jButton17.setEnabled(false);
			
			jButton8.setEnabled(false);
			jMenuItemUndo.setEnabled(false);
			jButton9.setEnabled(false);
			jMenuItemRedo.setEnabled(false);
			jButton11.setEnabled(false);
			jButton10.setEnabled(false);
			jMenuItem8.setEnabled(false);
			jMenuItem9.setEnabled(false);
			jMenuItem15.setEnabled(false);
			
			jButton12.setEnabled(false);
			jMenuItem4.setEnabled(false);
			jMenuItem6.setEnabled(false);
			jButton2.setEnabled(false);
			jMenuItem10.setEnabled(false);
			jMenu2.setEnabled(false);
			jMenuItem2.setEnabled(false);
			jButton13.setEnabled(false);
			jButton4.setEnabled(false);
			jButton5.setEnabled(false);
			jButton7.setEnabled(false);
			jButton15.setEnabled(false);
			jMenu3.setEnabled(false);
		}
		if ((document != null) && document.running) {
			jButton15.setEnabled(true);
			jMenuItem13.setEnabled(false);
			jMenuItem14.setEnabled(false);
			jMenuItem15.setEnabled(false);
			
			jButton4.setEnabled(true);
			
			document.getEditor().setEnabled(false);
			jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource(
				"/jasmin/gui/resources/icons/play_pause.png")));
		}
		if (helpDocument != null) {
			jButton6.setEnabled(helpDocument.canBack());
			jButton14.setEnabled(helpDocument.canForward());
		}
	}
	
	public void open() {
		JasDocument doc = new JasDocument(ErrorLabel, this);
		
		if (doc.open()) {
			addDocument(doc);
		}
		
	}
	
	private void save() {
		document.save();
		DocTab.setSelectedComponent(document);
	}
	
	/**
	 * Updates the title of the tab of an JasDocument.
	 */
	public void updateTitle(JasDocument doc) {
		int index = DocTab.indexOfComponent(doc);
		if (index != -1) {
			DocTab.setTitleAt(index, doc.getTitle());
		}
	}
	
	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this
	 * code. The
	 * content of this method is always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
	private void initComponents() {
		CloseMenu = new javax.swing.JPopupMenu();
		jMenuItem17 = new javax.swing.JMenuItem();
		jPanel2 = new javax.swing.JPanel();
		jToolBar1 = new javax.swing.JToolBar();
		jButton3 = new javax.swing.JButton();
		jButton1 = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		jPanel3 = new javax.swing.JPanel();
		jButton8 = new javax.swing.JButton();
		jButton9 = new javax.swing.JButton();
		jPanel5 = new javax.swing.JPanel();
		jButton10 = new javax.swing.JButton();
		jButton11 = new javax.swing.JButton();
		jButton12 = new javax.swing.JButton();
		jPanel4 = new javax.swing.JPanel();
		jButton6 = new javax.swing.JButton();
		jButton14 = new javax.swing.JButton();
		jPanel6 = new javax.swing.JPanel();
		jButton4 = new javax.swing.JButton();
		jButton5 = new javax.swing.JButton();
		jButton7 = new javax.swing.JButton();
		jButton13 = new javax.swing.JButton();
		jPanel7 = new javax.swing.JPanel();
		jButton15 = new javax.swing.JButton();
		jPanel8 = new javax.swing.JPanel();
		jButton16 = new javax.swing.JButton();
		jButton17 = new javax.swing.JButton();
		DocTab = new javax.swing.JTabbedPane();
		jPanel1 = new javax.swing.JPanel();
		ErrorLabel = new javax.swing.JLabel();
		jMenuBar1 = new javax.swing.JMenuBar();
		jMenu1 = new javax.swing.JMenu();
		jMenuItem5 = new javax.swing.JMenuItem();
		jSeparator2 = new javax.swing.JSeparator();
		jMenuItem3 = new javax.swing.JMenuItem();
		jMenuItem4 = new javax.swing.JMenuItem();
		jSeparator3 = new javax.swing.JSeparator();
		jMenuItem6 = new javax.swing.JMenuItem();
		jMenuItem7 = new javax.swing.JMenuItem();
		jSeparator4 = new javax.swing.JSeparator();
		jMenuItem2 = new javax.swing.JMenuItem();
		jSeparator6 = new javax.swing.JSeparator();
		jMenuItem18 = new javax.swing.JMenuItem();
		jSeparator5 = new javax.swing.JSeparator();
		jMenuItem1 = new javax.swing.JMenuItem();
		jMenu2 = new javax.swing.JMenu();
		jMenuItemUndo = new javax.swing.JMenuItem();
		jMenuItemRedo = new javax.swing.JMenuItem();
		jSeparator1 = new javax.swing.JSeparator();
		jMenuItem8 = new javax.swing.JMenuItem();
		jMenuItem9 = new javax.swing.JMenuItem();
		jMenuItem10 = new javax.swing.JMenuItem();
		jMenu3 = new javax.swing.JMenu();
		jMenuItem13 = new javax.swing.JMenuItem();
		jMenuItem15 = new javax.swing.JMenuItem();
		jMenuItem16 = new javax.swing.JMenuItem();
		jMenuItem14 = new javax.swing.JMenuItem();
		
		jMenuItem17.setText("Close Tab");
		jMenuItem17.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem17ActionPerformed(evt);
			}
		});
		
		CloseMenu.add(jMenuItem17);
		
		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle("Jasmin");
		addWindowListener(new java.awt.event.WindowAdapter() {
			
			public void windowClosed(java.awt.event.WindowEvent evt) {
				formWindowClosed(evt);
			}
			
			public void windowClosing(java.awt.event.WindowEvent evt) {
				formWindowClosing(evt);
			}
		});
		
		jPanel2.setLayout(new java.awt.BorderLayout());
		
		jPanel2.setMinimumSize(new java.awt.Dimension(800, 600));
		jPanel2.setPreferredSize(new java.awt.Dimension(800, 600));
		jToolBar1.setRollover(true);
		jToolBar1.setDoubleBuffered(true);
		jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/new.png")));
		jButton3.setToolTipText("Create a new Document");
		jButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jButton3.setOpaque(false);
		jButton3.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton3ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton3);
		
		jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/fileopen.png")));
		jButton1.setToolTipText("Open Sourcecode");
		jButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jButton1.setOpaque(false);
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton1);
		
		jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/filesaveas.png")));
		jButton2.setToolTipText("Save Sourcecode");
		jButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jButton2.setOpaque(false);
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton2);
		
		jPanel3.setMaximumSize(new java.awt.Dimension(10, 3));
		jPanel3.setMinimumSize(new java.awt.Dimension(10, 3));
		jPanel3.setOpaque(false);
		jPanel3.setPreferredSize(new java.awt.Dimension(10, 3));
		jToolBar1.add(jPanel3);
		
		jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/undo.png")));
		jButton8.setToolTipText("Undo");
		jButton8.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jButton8.setOpaque(false);
		jButton8.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton8ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton8);
		
		jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/redo.png")));
		jButton9.setToolTipText("Redo");
		jButton9.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jButton9.setOpaque(false);
		jButton9.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton9ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton9);
		
		jPanel5.setMaximumSize(new java.awt.Dimension(10, 3));
		jPanel5.setMinimumSize(new java.awt.Dimension(10, 3));
		jPanel5.setOpaque(false);
		jPanel5.setPreferredSize(new java.awt.Dimension(10, 3));
		jToolBar1.add(jPanel5);
		
		jButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/editcut.png")));
		jButton10.setToolTipText("Cut");
		jButton10.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jButton10.setOpaque(false);
		jButton10.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton10ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton10);
		
		jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/editcopy.png")));
		jButton11.setToolTipText("Copy");
		jButton11.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jButton11.setOpaque(false);
		jButton11.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton11ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton11);
		
		jButton12.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/editpaste.png")));
		jButton12.setToolTipText("Paste");
		jButton12.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jButton12.setOpaque(false);
		jButton12.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton12ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton12);
		
		jPanel4.setMaximumSize(new java.awt.Dimension(10, 3));
		jPanel4.setMinimumSize(new java.awt.Dimension(10, 3));
		jPanel4.setOpaque(false);
		jPanel4.setPreferredSize(new java.awt.Dimension(10, 3));
		jToolBar1.add(jPanel4);
		
		jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/back.png")));
		jButton6.setToolTipText("Go back");
		jButton6.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jButton6.setOpaque(false);
		jButton6.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton6ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton6);
		
		jButton14.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/forward.png")));
		jButton14.setToolTipText("Go forward");
		jButton14.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jButton14.setOpaque(false);
		jButton14.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton14ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton14);
		
		jPanel6.setMaximumSize(new java.awt.Dimension(10, 3));
		jPanel6.setMinimumSize(new java.awt.Dimension(10, 3));
		jPanel6.setOpaque(false);
		jPanel6.setPreferredSize(new java.awt.Dimension(10, 3));
		jToolBar1.add(jPanel6);
		
		jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/play_green.png")));
		jButton4.setToolTipText("Run the program");
		jButton4.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jButton4.setOpaque(false);
		jButton4.setPreferredSize(new java.awt.Dimension(24, 24));
		jButton4.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton4ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton4);
		
		jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/play_step.png")));
		jButton5.setToolTipText("Execute the next command");
		jButton5.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jButton5.setOpaque(false);
		jButton5.setPreferredSize(new java.awt.Dimension(24, 24));
		jButton5.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton5ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton5);
		
		jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/play_current.png")));
		jButton7
			.setToolTipText("Execute the line at the caret position without modifying the instruction pointer");
		jButton7.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jButton7.setOpaque(false);
		jButton7.setPreferredSize(new java.awt.Dimension(24, 24));
		jButton7.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton7ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton7);
		
		jButton13.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/play_stop.png")));
		jButton13.setToolTipText("Stop the program");
		jButton13.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jButton13.setOpaque(false);
		jButton13.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton13ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton13);
		
		jPanel7.setMaximumSize(new java.awt.Dimension(10, 3));
		jPanel7.setMinimumSize(new java.awt.Dimension(10, 3));
		jPanel7.setOpaque(false);
		jPanel7.setPreferredSize(new java.awt.Dimension(10, 3));
		jToolBar1.add(jPanel7);
		
		jButton15.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/play_clear3.png")));
		jButton15.setToolTipText("Reset the memory and all registers");
		jButton15.setBorder(null);
		jButton15.setOpaque(false);
		jButton15.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton15ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton15);
		
		jPanel8.setMaximumSize(new java.awt.Dimension(10, 3));
		jPanel8.setMinimumSize(new java.awt.Dimension(10, 3));
		jPanel8.setOpaque(false);
		jPanel8.setPreferredSize(new java.awt.Dimension(10, 3));
		jToolBar1.add(jPanel8);
		
		jButton16.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/takesnapshot.png")));
		jButton16.setToolTipText("Take Snapshot");
		jButton16.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jButton16.setOpaque(false);
		jButton16.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton16ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton16);
		
		jButton17.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/loadsnapshot.png")));
		jButton17.setToolTipText("Load Snapshot");
		jButton17.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jButton17.setOpaque(false);
		jButton17.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton17ActionPerformed(evt);
			}
		});
		
		jToolBar1.add(jButton17);
		
		jPanel2.add(jToolBar1, java.awt.BorderLayout.NORTH);
		
		DocTab.addChangeListener(new javax.swing.event.ChangeListener() {
			
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
				DocTabStateChanged(evt);
			}
		});
		DocTab.addMouseListener(new java.awt.event.MouseAdapter() {
			
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				DocTabMouseClicked(evt);
			}
		});
		DocTab.addContainerListener(new java.awt.event.ContainerAdapter() {
			
			public void componentRemoved(java.awt.event.ContainerEvent evt) {
				DocTabComponentRemoved(evt);
			}
		});
		
		jPanel2.add(DocTab, java.awt.BorderLayout.CENTER);
		
		jPanel1.setLayout(new java.awt.BorderLayout());
		
		ErrorLabel.setForeground(new java.awt.Color(204, 0, 51));
		ErrorLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jPanel1.add(ErrorLabel, java.awt.BorderLayout.CENTER);
		
		jPanel2.add(jPanel1, java.awt.BorderLayout.SOUTH);
		
		getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);
		
		jMenu1.setMnemonic('f');
		jMenu1.setText("File");
		jMenu1.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenu1ActionPerformed(evt);
			}
		});
		
		jMenuItem5.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/new.png")));
		jMenuItem5.setText("New");
		jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem5ActionPerformed(evt);
			}
		});
		
		jMenu1.add(jMenuItem5);
		
		jMenu1.add(jSeparator2);
		
		jMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/fileopen.png")));
		jMenuItem3.setText("Open Code");
		jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem3ActionPerformed(evt);
			}
		});
		
		jMenu1.add(jMenuItem3);
		
		jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/filesaveas.png")));
		jMenuItem4.setMnemonic('s');
		jMenuItem4.setText("Save Code");
		jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem4ActionPerformed(evt);
			}
		});
		
		jMenu1.add(jMenuItem4);
		
		jMenu1.add(jSeparator3);
		
		jMenuItem6.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/leer.gif")));
		jMenuItem6.setText("Save Memory");
		jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem6ActionPerformed(evt);
			}
		});
		
		jMenu1.add(jMenuItem6);
		
		jMenuItem7.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/leer.gif")));
		jMenuItem7.setText("Load Memory");
		jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem7ActionPerformed(evt);
			}
		});
		
		jMenu1.add(jMenuItem7);
		
		jMenu1.add(jSeparator4);
		
		jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/leer.gif")));
		jMenuItem2.setText("Close Document");
		jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem2ActionPerformed(evt);
			}
		});
		
		jMenu1.add(jMenuItem2);
		
		jMenu1.add(jSeparator6);
		
		jMenuItem18.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/leer.gif")));
		jMenuItem18.setText("Configuration");
		jMenuItem18.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem18ActionPerformed(evt);
			}
		});
		
		jMenu1.add(jMenuItem18);
		
		jMenu1.add(jSeparator5);
		
		jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/leer.gif")));
		jMenuItem1.setMnemonic('e');
		jMenuItem1.setText("Exit");
		jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem1ActionPerformed(evt);
			}
		});
		
		jMenu1.add(jMenuItem1);
		
		jMenuBar1.add(jMenu1);
		
		jMenu2.setMnemonic('E');
		jMenu2.setText("Edit");
		jMenu2.setToolTipText("");
		jMenuItemUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/undo.png")));
		jMenuItemUndo.setMnemonic('u');
		jMenuItemUndo.setText("Undo");
		jMenuItemUndo.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemUndoActionPerformed(evt);
			}
		});
		
		jMenu2.add(jMenuItemUndo);
		
		jMenuItemRedo.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/redo.png")));
		jMenuItemRedo.setMnemonic('r');
		jMenuItemRedo.setText("Redo");
		jMenuItemRedo.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemRedoActionPerformed(evt);
			}
		});
		
		jMenu2.add(jMenuItemRedo);
		
		jMenu2.add(jSeparator1);
		
		jMenuItem8.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/editcut.png")));
		jMenuItem8.setMnemonic('t');
		jMenuItem8.setText("Cut");
		jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem8ActionPerformed(evt);
			}
		});
		
		jMenu2.add(jMenuItem8);
		
		jMenuItem9.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/editcopy.png")));
		jMenuItem9.setMnemonic('c');
		jMenuItem9.setText("Copy");
		jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem9ActionPerformed(evt);
			}
		});
		
		jMenu2.add(jMenuItem9);
		
		jMenuItem10.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/editpaste.png")));
		jMenuItem10.setMnemonic('p');
		jMenuItem10.setText("Paste");
		jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem10ActionPerformed(evt);
			}
		});
		
		jMenu2.add(jMenuItem10);
		
		jMenuBar1.add(jMenu2);
		
		jMenu3.setMnemonic('r');
		jMenu3.setText("Run");
		jMenuItem13.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/play_green.png")));
		jMenuItem13.setMnemonic('r');
		jMenuItem13.setText("Run");
		jMenuItem13.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem13ActionPerformed(evt);
			}
		});
		
		jMenu3.add(jMenuItem13);
		
		jMenuItem15.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/play_pause.png")));
		jMenuItem15.setMnemonic('p');
		jMenuItem15.setText("Pause");
		jMenuItem15.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem15ActionPerformed(evt);
			}
		});
		
		jMenu3.add(jMenuItem15);
		
		jMenuItem16.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/play_step.png")));
		jMenuItem16.setMnemonic('s');
		jMenuItem16.setText("Step");
		jMenuItem16.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem16ActionPerformed(evt);
			}
		});
		
		jMenu3.add(jMenuItem16);
		
		jMenuItem14.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/play_current.png")));
		jMenuItem14.setMnemonic('e');
		jMenuItem14.setText("Execute current line");
		jMenuItem14.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem14ActionPerformed(evt);
			}
		});
		
		jMenu3.add(jMenuItem14);
		
		jMenuBar1.add(jMenu3);
		
		setJMenuBar(jMenuBar1);
		
		pack();
	}// </editor-fold>//GEN-END:initComponents
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItemRedoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemRedoActionPerformed
		document.undoManager.redo();
		checkButtonStates();
	}// GEN-LAST:event_jMenuItemRedoActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItemUndoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemUndoActionPerformed
		document.undoManager.undo();
		checkButtonStates();
	}// GEN-LAST:event_jMenuItemUndoActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton17ActionPerformed
		document.resumeSnapshot();
		checkButtonStates();
	}// GEN-LAST:event_jButton17ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton16ActionPerformed
	
		document.takeSnapshot();
		
		checkButtonStates();
	}// GEN-LAST:event_jButton16ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem18ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem18ActionPerformed
		addHelp(getClass().getResource("/jasmin/gui/resources/Configuration.htm"), "Configuration");
	}// GEN-LAST:event_jMenuItem18ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton15ActionPerformed
		document.data.clear();
		document.clearAll();
		document.updateAll();
		document.clearErrorLine();
	}// GEN-LAST:event_jButton15ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void formWindowClosing(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_formWindowClosing
		try {
			
			File propfile = new File(System.getProperty("user.home") + File.separator + ".jasmin");
			if (!propfile.exists()) {
				propfile.createNewFile();
			}
			properties.store(new FileOutputStream(propfile), "Jasmin configuration file");
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ex.toString());
			System.exit(1);
		}
		System.exit(0);
	}// GEN-LAST:event_formWindowClosing
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void formWindowClosed(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_formWindowClosed
	
	}// GEN-LAST:event_formWindowClosed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton14ActionPerformed
		if (helpDocument != null) {
			helpDocument.forward();
		}
	}// GEN-LAST:event_jButton14ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton6ActionPerformed
		if (helpDocument != null) {
			helpDocument.back();
		}
	}// GEN-LAST:event_jButton6ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void DocTabComponentRemoved(java.awt.event.ContainerEvent evt) {// GEN-FIRST:event_DocTabComponentRemoved
		DocTabStateChanged(null);
	}// GEN-LAST:event_DocTabComponentRemoved
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem17ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem17ActionPerformed
		if (document != null) {
			DocTab.remove(document);
		} else if (helpDocument != null) {
			DocTab.remove(helpDocument);
		}
		checkButtonStates();
	}// GEN-LAST:event_jMenuItem17ActionPerformed
	
	private void DocTabMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_DocTabMouseClicked
		if (evt.getButton() == MouseEvent.BUTTON3) {
			CloseMenu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	}// GEN-LAST:event_DocTabMouseClicked
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem2ActionPerformed
		if (document != null) {
			DocTab.remove(document);
		} else if (helpDocument != null) {
			DocTab.remove(helpDocument);
		}
		checkButtonStates();
	}// GEN-LAST:event_jMenuItem2ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenu1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenu1ActionPerformed
	}// GEN-LAST:event_jMenu1ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem7ActionPerformed
		document.openData();
		
	}// GEN-LAST:event_jMenuItem7ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem6ActionPerformed
		document.saveData();
	}// GEN-LAST:event_jMenuItem6ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem4ActionPerformed
		save();
	}// GEN-LAST:event_jMenuItem4ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem3ActionPerformed
		open();
	}// GEN-LAST:event_jMenuItem3ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton2ActionPerformed
		save();
	}// GEN-LAST:event_jButton2ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed
		open();
	}// GEN-LAST:event_jButton1ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem14ActionPerformed
		document.executeCurrentLine();
		checkButtonStates();
	}// GEN-LAST:event_jMenuItem14ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem16ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem16ActionPerformed
		document.step();
		checkButtonStates();
	}// GEN-LAST:event_jMenuItem16ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem15ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem15ActionPerformed
		document.pauseProgram();
		checkButtonStates();
	}// GEN-LAST:event_jMenuItem15ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem13ActionPerformed
		document.runProgram();
		checkButtonStates();
	}// GEN-LAST:event_jMenuItem13ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem10ActionPerformed
		document.getEditor().paste();
		checkButtonStates();
	}// GEN-LAST:event_jMenuItem10ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem9ActionPerformed
		document.getEditor().copy();
	}// GEN-LAST:event_jMenuItem9ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem8ActionPerformed
		document.getEditor().cut();
	}// GEN-LAST:event_jMenuItem8ActionPerformed
	
	private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem5ActionPerformed
		jButton3ActionPerformed(evt);
	}// GEN-LAST:event_jMenuItem5ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton13ActionPerformed
		document.pauseProgram();
		Thread.yield();
		document.data.setInstructionPointer(0);
		document.updateAll();
		checkButtonStates();
	}// GEN-LAST:event_jButton13ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton9ActionPerformed
		document.undoManager.redo();
		checkButtonStates();
	}// GEN-LAST:event_jButton9ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton8ActionPerformed
		document.undoManager.undo();
		checkButtonStates();
	}// GEN-LAST:event_jButton8ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton12ActionPerformed
		document.getEditor().paste();
		checkButtonStates();
	}// GEN-LAST:event_jButton12ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton11ActionPerformed
		document.getEditor().copy();
		checkButtonStates();
	}// GEN-LAST:event_jButton11ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton10ActionPerformed
		document.getEditor().cut();
		checkButtonStates();
	}// GEN-LAST:event_jButton10ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton7ActionPerformed
		document.executeCurrentLine();
		checkButtonStates();
	}// GEN-LAST:event_jButton7ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton4ActionPerformed
		if (document.running) {
			document.pauseProgram();
		} else {
			document.runProgram();
		}
		checkButtonStates();
	}// GEN-LAST:event_jButton4ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton5ActionPerformed
		document.step();
		checkButtonStates();
	}// GEN-LAST:event_jButton5ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void DocTabStateChanged(javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_DocTabStateChanged
		Object odoc = DocTab.getSelectedComponent();
		if (odoc != null) {
			if (odoc instanceof JasDocument) {
				document = (JasDocument) odoc;
				this.setTitle("Jasmin - " + document.getTitle());
				DocTab.setTitleAt(DocTab.indexOfComponent(document), document.getTitle());
				helpDocument = null;
			} else if (odoc instanceof HelpBrowser) {
				helpDocument = (HelpBrowser) odoc;
				document = null;
			}
		} else {
			document = null;
			helpDocument = null;
		}
		
		checkButtonStates();
	}// GEN-LAST:event_DocTabStateChanged
	
	public void newDocument() {
		addDocument(new JasDocument(ErrorLabel, this));
		helpDocument = null;
	}
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton3ActionPerformed
	
		newDocument();
	}// GEN-LAST:event_jButton3ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem1ActionPerformed
	
		formWindowClosing(null);
	}// GEN-LAST:event_jMenuItem1ActionPerformed
	
	private void addDocument(JasDocument doc) {
		doc.undoManager = new UndoManager() {
			
			private static final long serialVersionUID = 5579743695599380564L;
			
			public void undoableEditHappened(UndoableEditEvent e) {
				
				UndoableEdit ue = e.getEdit();
				if (ue instanceof AbstractDocument.DefaultDocumentEvent) {
					AbstractDocument.DefaultDocumentEvent ae = (AbstractDocument.DefaultDocumentEvent) ue;
					if (ae.getType() == DocumentEvent.EventType.CHANGE) {
						super.addEdit(new NoStyleUndo(ae));
					} else {
						super.addEdit(e.getEdit());
					}
					
				}
				checkButtonStates();
				
			}
			
			public void undo() throws CannotUndoException {
				try {
					super.undo();
					checkButtonStates();
				} catch (Exception ex) {
					
				}
			}
			
			public void redo() throws CannotRedoException {
				try {
					super.redo();
					checkButtonStates();
				} catch (Exception ex) {
					
				}
			}
			
		};
		
		doc.getEditor().getDocument().addUndoableEditListener(doc.undoManager);
		doc.getEditor().addCaretListener(new CaretListener() {
			
			/**
			 * @param e
			 */
			public void caretUpdate(CaretEvent e) {
				checkButtonStates();
			}
		});
		doc.undoManager.setLimit(99999);
		
		DocTab.addTab(doc.getTitle(), doc);
		DocTab.setSelectedIndex(DocTab.getTabCount() - 1);
		helpDocument = null;
		document = doc;
		doc.validate();
		doc.makeLayout();
		// Set focus to the editor (when opened via the tool bar button "New").
		doc.getEditor().requestFocusInWindow();
	}
	
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JPopupMenu CloseMenu;
	private javax.swing.JTabbedPane DocTab;
	private javax.swing.JLabel ErrorLabel;
	private javax.swing.JButton jButton1;
	private javax.swing.JButton jButton10;
	private javax.swing.JButton jButton11;
	private javax.swing.JButton jButton12;
	private javax.swing.JButton jButton13;
	private javax.swing.JButton jButton14;
	private javax.swing.JButton jButton15;
	private javax.swing.JButton jButton16;
	private javax.swing.JButton jButton17;
	private javax.swing.JButton jButton2;
	private javax.swing.JButton jButton3;
	private javax.swing.JButton jButton4;
	private javax.swing.JButton jButton5;
	private javax.swing.JButton jButton6;
	private javax.swing.JButton jButton7;
	private javax.swing.JButton jButton8;
	private javax.swing.JButton jButton9;
	private javax.swing.JMenu jMenu1;
	private javax.swing.JMenu jMenu2;
	private javax.swing.JMenu jMenu3;
	private javax.swing.JMenuBar jMenuBar1;
	private javax.swing.JMenuItem jMenuItem1;
	private javax.swing.JMenuItem jMenuItem10;
	private javax.swing.JMenuItem jMenuItem13;
	private javax.swing.JMenuItem jMenuItem14;
	private javax.swing.JMenuItem jMenuItem15;
	private javax.swing.JMenuItem jMenuItem16;
	private javax.swing.JMenuItem jMenuItem17;
	private javax.swing.JMenuItem jMenuItem18;
	private javax.swing.JMenuItem jMenuItem2;
	private javax.swing.JMenuItem jMenuItem3;
	private javax.swing.JMenuItem jMenuItem4;
	private javax.swing.JMenuItem jMenuItem5;
	private javax.swing.JMenuItem jMenuItem6;
	private javax.swing.JMenuItem jMenuItem7;
	private javax.swing.JMenuItem jMenuItem8;
	private javax.swing.JMenuItem jMenuItem9;
	private javax.swing.JMenuItem jMenuItemRedo;
	private javax.swing.JMenuItem jMenuItemUndo;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JSeparator jSeparator2;
	private javax.swing.JSeparator jSeparator3;
	private javax.swing.JSeparator jSeparator4;
	private javax.swing.JSeparator jSeparator5;
	private javax.swing.JSeparator jSeparator6;
	private javax.swing.JToolBar jToolBar1;
	// End of variables declaration//GEN-END:variables
	
}
