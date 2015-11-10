/*
 * JasDocument.java
 *  
 * Created on 16. März 2006, 17:43
 */
package jasmin.gui;

import jasmin.core.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.*;
import java.lang.Thread.State;
import java.util.*;
import java.util.concurrent.Semaphore;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.undo.UndoManager;

/**
 * This class represents a gui and all data of an jasmin document.
 * 
 * @author Kai Orend
 */
public final class JasDocument extends javax.swing.JPanel implements Runnable {
	
	private static final long serialVersionUID = 1L;
	private String title = "new document";
	/** The line which is currently executed. */
	private int markedLine = 0;
	private SyntaxHighlighter highlighter = null;
	private MemoryTableModel model = null;
	private final LinkedList<IGuiModule> modules;
	private IGuiModule lastSelected = null;
	public CommandLoader cmdLoader = null;
	public DataSpace data = null;
	public Parser parser = null;
	private LinkedList<RegisterPanel> registerPanels = null;
	private ArrayList<LineNumber> lineNumbers = null;
	private int lastHeight = 0;
	private Semaphore lineNumbersUpdating;
	// This was supposed to enable viewing a detailed history of which memory cell or register was changed
	// how long ago. That was never implemented, however; just leave lastSteps = 1 for now.
	private int lastSteps = 1;
	public boolean[] cachedLineDone;
	/**
	 * The UndoManager is used for the source editor.
	 */
	public UndoManager undoManager = null;
	/* Is true while the assembler program is running. */
	public boolean running;
	/**
	 * The frame in which this document is.
	 */
	public MainFrame frame = null;
	private HelpBrowser helpBrowser = null;
	private FpuPanel fpuPanel = null;
	private Thread runningThread = null;
	private File snapshot = null;
	private String lastPathCode = null;
	private String lastPathMem = null;
	
	/**
	 * Creates a new Document.
	 * 
	 * @param label
	 *        currently unused.
	 */
	public JasDocument(JLabel label, MainFrame frame) {
		this.frame = frame;
		initComponents();
		
		highlighter = new SyntaxHighlighter(this);
		
		data = new DataSpace(frame.getProperty("memory", 4096), frame.getProperty("offset", 0));
		cmdLoader = new CommandLoader(data, "jasmin.commands", JasminCommand.class);
		
		/*
		 * checking whether there is a context help for every command and a
		 * command for every context help file
		 */
		for (String mnemo : cmdLoader.getMnemoList()) {
			if (frame.helpLoader.get(mnemo) == null) {
				System.out.println("The command " + mnemo + " has no context help file!");
			}
		}
		for (String mnemo : frame.helpLoader.getMnemoList()) {
			if (cmdLoader.getCommand(mnemo.toUpperCase()) == null) {
				System.out.println("No matching command for helpfile " + mnemo + "!");
			}
		}
		/*
		 * end of context help checking
		 */
		parser = new Parser(data, cmdLoader, this);
		data.setParser(parser);
		
		model = new MemoryTableModel(data, this);
		jTable1.setModel(model);
		jTable1.setDefaultRenderer(String.class, new MemoryTableRenderer(model));
		registerPanels = new LinkedList<RegisterPanel>();
		RegisterSet[] regs = data.getRegisterSets();
		for (int i = 0; i < regs.length; i++) {
			RegisterPanel gp = new RegisterPanel(regs[i], this);
			jPanel3.add(gp);
			registerPanels.add(gp);
		}
		lineNumbers = new ArrayList<LineNumber>();
		lineNumbersUpdating = new Semaphore(1);
		lastHeight = 3;
		
		running = false;
		
		fpuPanel = new FpuPanel(data.fpu, this);
		jPanel13.add(fpuPanel);
		
		// initialisation of GUI output modules. just add any new modules to this list:
		modules = new LinkedList<IGuiModule>();
		
		modules.add(new SevenSegment());
		modules.add(new StripLight());
		modules.add(new Console());
		modules.add(new VGA());
		
		for (IGuiModule mod : modules) {
			mod.setDataSpace(data);
			jTabbedPane1.addTab(mod.getTabLabel(), (Component) mod);
		}
		updateAll();
		setRegisterMode(DataSpace.SIGNED);
		helpBrowser = new HelpBrowser(frame);
		
		jPanelHelp.add(helpBrowser);
		
		// Set focus to the editor (when opened via "New Document" link on the Welcome Page).
		setFocusCycleRoot(true);
		setFocusTraversalPolicy(new javax.swing.LayoutFocusTraversalPolicy() {
			
			@SuppressWarnings("unused")
			public Component getDefaultComponent(Container cont) {
				return getEditor();
			}
		});
	}
	
	public void makeLayout() {
		validate();
		jSplitPane2.setDividerLocation(
			frame.getProperty("split2.location", jSplitPane2.getDividerLocation()));
		jSplitPane1.setDividerLocation(
			frame.getProperty("split1.location", frame.getWidth() - 350 - jSplitPane2.getDividerLocation()));
		jSplitPane3.setDividerLocation(
			frame.getProperty("split3.location", jSplitPane3.getDividerLocation()));
		jSplitPane4.setDividerLocation(frame.getProperty("split4.location", frame.getHeight() - 350));
	}
	
	public boolean hasSnapshot() {
		return snapshot != null;
	}
	
	/**
	 * Saves a Snapshot of the data in a temp file.
	 */
	public void takeSnapshot() {
		try {
			snapshot = File.createTempFile(this.hashCode() + "", null);
			snapshot.deleteOnExit();
			data.save(snapshot);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Loads the snapshot from the temp file.
	 */
	public void resumeSnapshot() {
		data.load(snapshot);
		updateAll();
	}
	
	private void checkButtonStates() {
		jMenuItem8.setEnabled(
			!running && ((getEditor().getSelectionEnd() - getEditor().getSelectionStart()) > 0));
		jMenuItem9.setEnabled(
			!running && ((getEditor().getSelectionEnd() - getEditor().getSelectionStart()) > 0));
		jMenuItem10.setEnabled(!running);
		jMenuItem11.setEnabled(!running && undoManager.canUndo());
		jMenuItem12.setEnabled(!running && undoManager.canRedo());
	}
	
	/**
	 * Sets the Execution mark to the current ISP.
	 */
	public final void updateExecutionMark() {
		if (markedLine < lineNumbers.size()) {
			lineNumbers.get(markedLine).setBackground(NumberPanel.getBackground());
		}
		
		markedLine = data.getInstructionPointer();
		if (markedLine < lineNumbers.size()) {
			JPanel panel = lineNumbers.get(markedLine);
			panel.setBackground(new Color(0, 255, 0));
		}
	}
	
	/**
	 * Scroll to the current Position of the Execution Mark.
	 */
	private void scrollToExecutionMark() {
		if (markedLine < lineNumbers.size()) {
			getEditor().scrollRectToVisible(lineNumbers.get(markedLine).getBounds());
		}
	}
	
	/**
	 * @return The number of excuted lines since last gui update. Is used to highlight changed data.
	 */
	public final int getLastStepCount() {
		return lastSteps;
	}
	
	private void updateLineNumbers() {
		if (!lineNumbersUpdating.tryAcquire()) {
			return;
		}
		int height = NumberPanel.getHeight();
		while (lastHeight < height) {
			LineNumber panel = new LineNumber(lineNumbers.size(), this);
			
			NumberPanel.add(panel);
			panel.doLayout();
			int pheight = (int) panel.getPreferredSize().getHeight();
			panel.setBounds(0, lastHeight, NumberPanel.getWidth(), pheight);
			lineNumbers.add(panel);
			lastHeight += pheight;
		}
		updateExecutionMark();
		lineNumbersUpdating.release();
	}
	
	public JTextPane getEditor() {
		return jTextPane1;
	}
	
	/**
	 * @return The title of this document.
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @param title
	 *        Sets the title and updates it in the actual frame.
	 */
	public void setTitle(String title) {
		this.title = title;
		frame.updateTitle(this);
	}
	
	/**
	 * Shows a file dialog for saving the source code.
	 */
	public void save() {
		frame.fileChooser.resetChoosableFileFilters();
		frame.fileChooser.addChoosableFileFilter(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.toString().toUpperCase().endsWith("ASM") || pathname.isDirectory();
			}
			
			@Override
			public String getDescription() {
				return "Assembler Code (*.asm)";
			}
		});
		
		if (lastPathCode == null) {
			frame.getProperty("lastpath.asm");
		}
		if (lastPathCode != null) {
			frame.fileChooser.setSelectedFile(new File(lastPathCode));
		}
		if (frame.fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = frame.fileChooser.getSelectedFile();
			if (file != null) {
				lastPathCode = file.getPath();
				if (!lastPathCode.toLowerCase().endsWith(".asm")) {
					lastPathCode += ".asm";
					file = new File(lastPathCode);
				}
				frame.putProperty("lastpath.asm", file.getPath());
				
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter(file));
					out.write(highlighter.getText().toCharArray());
					setTitle(file.getName());
					out.close();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this, ex.toString());
				}
				setTitle(file.getName());
			}
		}
	}
	
	/**
	 * Shows a file dialog for saving the virtual hardware.
	 */
	public void saveData() {
		frame.fileChooser.resetChoosableFileFilters();
		frame.fileChooser.addChoosableFileFilter(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.toString().toUpperCase().endsWith(".MEM") || pathname.isDirectory();
			}
			
			@Override
			public String getDescription() {
				return "Memory and Register data (*.mem)";
			}
		});
		if (lastPathMem == null) {
			frame.getProperty("lastpath.mem");
		}
		if (lastPathMem != null) {
			frame.fileChooser.setSelectedFile(new File(lastPathMem));
		}
		if (frame.fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = frame.fileChooser.getSelectedFile();
			if (file != null) {
				lastPathMem = file.getPath();
				if (!lastPathMem.toLowerCase().endsWith(".mem")) {
					lastPathMem += ".mem";
					file = new File(lastPathMem);
				}
				frame.putProperty("lastpath.mem", lastPathMem);
				try {
					data.save(file);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this, ex.toString());
				}
			}
		}
	}
	
	/**
	 * Shows a file dialog for opening the hardware from a file.
	 */
	public void openData() {
		frame.fileChooser.resetChoosableFileFilters();
		frame.fileChooser.addChoosableFileFilter(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.toString().toUpperCase().endsWith(".MEM") || pathname.isDirectory();
			}
			
			@Override
			public String getDescription() {
				return "Memory and Register data (*.mem)";
			}
		});
		if (lastPathMem == null) {
			frame.getProperty("lastpath.mem");
		}
		if (lastPathMem != null) {
			frame.fileChooser.setSelectedFile(new File(lastPathMem));
		}
		if (frame.fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = frame.fileChooser.getSelectedFile();
			if (file != null) {
				lastPathMem = file.getPath();
				frame.putProperty("lastpath.mem", lastPathMem);
				try {
					data.load(file);
					updateAll();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(this, ex.toString());
				}
				
			}
		}
	}
	
	/**
	 * Shows a file dialog for loading the source code.
	 * 
	 * @return True if a file was loaded, false otherwise.
	 */
	public boolean open() {
		frame.fileChooser.resetChoosableFileFilters();
		frame.fileChooser.addChoosableFileFilter(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.toString().toUpperCase().endsWith(".ASM") || pathname.isDirectory();
			}
			
			@Override
			public String getDescription() {
				return "Assembler Code (*.asm)";
			}
		});
		if (lastPathCode == null) {
			frame.getProperty("lastpath.asm");
		}
		if (lastPathCode != null) {
			frame.fileChooser.setSelectedFile(new File(lastPathCode));
		}
		if (frame.fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = frame.fileChooser.getSelectedFile();
			if (file != null) {
				lastPathCode = file.getPath();
				frame.putProperty("lastpath.asm", lastPathCode);
				try {
					BufferedReader in = new BufferedReader(new FileReader(file));
					char[] data = new char[(int) file.length()];
					in.read(data);
					String text = new String(data);
					getEditor().setText(text);
					setTitle(file.getName());
					in.close();
					return true;
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, ex.toString());
				}
				
			}
			
		}
		return false;
	}
	
	/**
	 * Makes a pause of the execution.
	 */
	public void pauseProgram() {
		if (runningThread != null) {
			if (runningThread.getState().equals(State.TIMED_WAITING)) {
				// maybe the JASMINSLEEP command needs to be woken up
				runningThread.interrupt();
			}
		}
		running = false;
		scrollToExecutionMark();
	}
	
	/**
	 * This method is used to execute the assembler by the new thread.
	 */
	public void run() {
		int numberOfLines = highlighter.getNumberOfLines();
		cachedLineDone = new boolean[numberOfLines];
		parser.clearCache(numberOfLines);
		// If the currentline is at a breakpoint, the break point should be skipped.
		boolean skipbreakpoint = false;
		if (isBreakPoint(data.getInstructionPointer())) {
			skipbreakpoint = true;
		}
		int lineNumber = data.getInstructionPointer();
		while ((lineNumber < numberOfLines)) {
			if (!running) {
				break;
			}
			if (isBreakPoint(lineNumber) && !skipbreakpoint) {
				break;
			} else if (isBreakPoint(lineNumber) && skipbreakpoint) {
				skipbreakpoint = false;
			}
			if ((lineNumber < numberOfLines)) {
				
				data.setInstructionPointer(lineNumber + 1);
				try {
					executeLineNumber(lineNumber, true);
				} catch (Exception ex) {
					updateExecutionMark();
					scrollToExecutionMark();
					ErrorLabel.setText(ex.toString() + "");
					ex.printStackTrace();
					break;
				}
			}
			lineNumber = data.getInstructionPointer();
		}
		
		data.updateDirty();
		running = false;
		Thread.yield();
		updateAll();
		frame.checkButtonStates();
		runningThread = null;
	}
	
	/**
	 * Creates a new thread to run the assembler program in it.
	 */
	public void runProgram() {
		if ((runningThread == null) && !running) {
			running = true;
			runningThread = new Thread(this);
			runningThread.start();
		}
	}
	
	/**
	 * Executes at the current EIP and increment it.
	 */
	public void step() {
		int lineNumber = data.getInstructionPointer();
		data.setInstructionPointer(lineNumber + 1);
		if (lineNumber < highlighter.getNumberOfLines()) {
			executeLineNumber(lineNumber, false);
		}
		updateAll();
		scrollToExecutionMark();
	}
	
	private boolean isBreakPoint(int lineNumber) {
		if (lineNumber >= lineNumbers.size()) {
			return false;
		}
		return lineNumbers.get(lineNumber).isBreakPoint();
	}
	
	/**
	 * Updates the GUI of this document.
	 */
	public void updateAll() {
		updateExecutionMark();
		model.updateChanged();
		Iterator<RegisterPanel> iter = registerPanels.iterator();
		while (iter.hasNext()) {
			RegisterPanel rp = iter.next();
			rp.update();
		}
		for (IGuiModule mod : modules) {
			mod.updateAll();
		}
		jCheckCarry.setSelected(data.fCarry);
		jCheckAuxiliary.setSelected(data.fAuxiliary);
		jCheckZero.setSelected(data.fZero);
		jCheckSign.setSelected(data.fSign);
		jCheckDirection.setSelected(data.fDirection);
		jCheckTrap.setSelected(data.fTrap);
		jCheckParity.setSelected(data.fParity);
		jCheckOverflow.setSelected(data.fOverflow);
		fpuPanel.update();
		
	}
	
	/**
	 * update highlighting of registers and the memory cells they are pointing to
	 */
	public void updateMemoryHighlight(boolean highlight) {
		Iterator<RegisterPanel> iter = registerPanels.iterator();
		while (iter.hasNext()) {
			RegisterPanel rp = iter.next();
			rp.setHighlight(highlight);
			rp.update();
		}
		model.enableHighlighting(highlight);
	}
	
	public boolean isHighlightingEnabled() {
		return toggleButtonHighlight.isSelected();
	}
	
	public Color getRegisterColor(Address register) {
		if (register == data.EAX) {
			return new Color(255, 200, 190);
		}
		if (register == data.EBX) {
			return new Color(250, 250, 200);
		}
		if (register == data.ECX) {
			return new Color(200, 240, 200);
		}
		if (register == data.EDX) {
			return new Color(200, 200, 240);
		}
		if (register == data.ESI) {
			return new Color(250, 230, 180);
		}
		if (register == data.EDI) {
			return new Color(240, 200, 240);
		}
		return null;
	}
	
	/**
	 * clear error display (in case of reset etc.)
	 */
	public void clearErrorLine() {
		ErrorLabel.setText("");
	}
	
	/**
	 * clear anything that needs clearing when everything is reset
	 */
	public void clearAll() {
		for (IGuiModule mod : modules) {
			mod.clear();
		}
		highlighter.reparseAll();
	}
	
	private void setRegisterMode(int mode) {
		Iterator<RegisterPanel> iter = registerPanels.iterator();
		while (iter.hasNext()) {
			RegisterPanel rp = iter.next();
			rp.setMode(mode);
		}
		jToggleButton5.setSelected(false);
		jToggleButton6.setSelected(false);
		jToggleButton7.setSelected(false);
		jToggleButton8.setSelected(false);
		if (mode == DataSpace.BIN) {
			jToggleButton6.setSelected(true);
		} else if (mode == DataSpace.UNSIGNED) {
			jToggleButton7.setSelected(true);
		} else if (mode == DataSpace.HEX) {
			jToggleButton5.setSelected(true);
		} else if (mode == DataSpace.SIGNED) {
			jToggleButton8.setSelected(true);
		}
	}
	
	public void executeLineNumber(int lineNumber, boolean cached) {
		
		ParseError error = highlighter.executeLine(lineNumber, cached);
		if (error != null) {
			ErrorLabel.setText(error.errorMsg);
			pauseProgram();
		} else if (!cached) {
			ErrorLabel.setText("");
		}
	}
	
	/**
	 * Executes the current line without modifying the EIP register.
	 */
	public void executeCurrentLine() {
		executeLineNumber(highlighter.getLineNumberByOffset(getEditor().getCaretPosition()), false);
		scrollToExecutionMark();
		updateAll();
	}
	
	/**
	 * @param label
	 *        The label.
	 * @return The linenumber of in which the label is delcared.
	 */
	public final int getLabelLine(String label) {
		return highlighter.getLabelDefinitionLine(label);
	}
	
	public void parsingDone(int lineNumber, String mnemo, ParseError error) {
		if (lineNumber == highlighter.getLineNumberByOffset(getEditor().getCaretPosition())) {
			setHelp(mnemo);
			if (error != null) {
				ErrorLabel.setText(error.errorMsg);
			} else {
				ErrorLabel.setText("");
			}
		}
	}
	
	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this
	 * code. The content of this method is always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		
		jPopupMenu1 = new javax.swing.JPopupMenu();
		jMenuItem11 = new javax.swing.JMenuItem();
		jMenuItem12 = new javax.swing.JMenuItem();
		jSeparator1 = new javax.swing.JSeparator();
		jMenuItem8 = new javax.swing.JMenuItem();
		jMenuItem9 = new javax.swing.JMenuItem();
		jMenuItem10 = new javax.swing.JMenuItem();
		jSplitPane2 = new javax.swing.JSplitPane();
		jSplitPane1 = new javax.swing.JSplitPane();
		jPanel1 = new javax.swing.JPanel();
		jPanel6 = new javax.swing.JPanel();
		jScrollPane2 = new javax.swing.JScrollPane();
		jTable1 = new javax.swing.JTable();
		jToolBar1 = new javax.swing.JToolBar();
		toggleButtonDesc = new javax.swing.JToggleButton();
		toggleButtonHex = new javax.swing.JToggleButton();
		toggleButtonHighlight = new javax.swing.JToggleButton();
		jPanel9 = new javax.swing.JPanel();
		toggleButton8Bit = new javax.swing.JToggleButton();
		toggleButton16Bit = new javax.swing.JToggleButton();
		toggleButton32Bit = new javax.swing.JToggleButton();
		jSplitPane4 = new javax.swing.JSplitPane();
		jPanel4 = new javax.swing.JPanel();
		jPanel7 = new javax.swing.JPanel();
		ErrorLabel = new javax.swing.JLabel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jPanel2 = new javax.swing.JPanel();
		NumberPanel = new javax.swing.JPanel();
		jTextPane1 = new javax.swing.JTextPane();
		jTabbedPane1 = new javax.swing.JTabbedPane();
		jPanelHelp = new javax.swing.JPanel();
		jSplitPane3 = new javax.swing.JSplitPane();
		jPanel8 = new javax.swing.JPanel();
		jPanel5 = new javax.swing.JPanel();
		jCheckCarry = new javax.swing.JCheckBox();
		jCheckOverflow = new javax.swing.JCheckBox();
		jCheckSign = new javax.swing.JCheckBox();
		jCheckZero = new javax.swing.JCheckBox();
		jCheckParity = new javax.swing.JCheckBox();
		jCheckAuxiliary = new javax.swing.JCheckBox();
		jCheckTrap = new javax.swing.JCheckBox();
		jCheckDirection = new javax.swing.JCheckBox();
		jPanel10 = new javax.swing.JPanel();
		jToolBar2 = new javax.swing.JToolBar();
		jToggleButton6 = new javax.swing.JToggleButton();
		jToggleButton8 = new javax.swing.JToggleButton();
		jToggleButton7 = new javax.swing.JToggleButton();
		jToggleButton5 = new javax.swing.JToggleButton();
		jPanel3 = new javax.swing.JPanel();
		jPanel13 = new javax.swing.JPanel();
		
		jMenuItem11.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/undo.png"))); // NOI18N
		jMenuItem11.setMnemonic('u');
		jMenuItem11.setText("Undo");
		jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem11ActionPerformed(evt);
			}
		});
		jPopupMenu1.add(jMenuItem11);
		
		jMenuItem12.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/redo.png"))); // NOI18N
		jMenuItem12.setMnemonic('r');
		jMenuItem12.setText("Redo");
		jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem12ActionPerformed(evt);
			}
		});
		jPopupMenu1.add(jMenuItem12);
		jPopupMenu1.add(jSeparator1);
		
		jMenuItem8.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/editcut.png"))); // NOI18N
		jMenuItem8.setMnemonic('t');
		jMenuItem8.setText("Cut");
		jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem8ActionPerformed(evt);
			}
		});
		jPopupMenu1.add(jMenuItem8);
		
		jMenuItem9.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/editcopy.png"))); // NOI18N
		jMenuItem9.setMnemonic('c');
		jMenuItem9.setText("Copy");
		jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem9ActionPerformed(evt);
			}
		});
		jPopupMenu1.add(jMenuItem9);
		
		jMenuItem10.setIcon(new javax.swing.ImageIcon(getClass().getResource(
			"/jasmin/gui/resources/icons/editpaste.png"))); // NOI18N
		jMenuItem10.setMnemonic('p');
		jMenuItem10.setText("Paste");
		jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItem10ActionPerformed(evt);
			}
		});
		jPopupMenu1.add(jMenuItem10);
		
		setLayout(new java.awt.BorderLayout());
		
		jSplitPane2.setBorder(null);
		jSplitPane2.setDividerLocation(300);
		jSplitPane2.setDividerSize(3);
		jSplitPane2.setContinuousLayout(true);
		
		jSplitPane1.setBorder(null);
		jSplitPane1.setDividerLocation(600);
		jSplitPane1.setDividerSize(3);
		jSplitPane1.setContinuousLayout(true);
		jSplitPane1.addMouseListener(new java.awt.event.MouseAdapter() {
			
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jSplitPane1MouseClicked(evt);
			}
			
			@Override
			public void mouseReleased(java.awt.event.MouseEvent evt) {
				jSplitPane1MouseReleased(evt);
			}
		});
		jSplitPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
			
			@Override
			public void componentMoved(java.awt.event.ComponentEvent evt) {
				jSplitPane1ComponentMoved(evt);
			}
			
			@Override
			public void componentResized(java.awt.event.ComponentEvent evt) {
				jSplitPane1ComponentResized(evt);
			}
		});
		jSplitPane1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
			
			@Override
			public void mouseDragged(java.awt.event.MouseEvent evt) {
				jSplitPane1MouseDragged(evt);
			}
		});
		jSplitPane1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
			
			public void propertyChange(java.beans.PropertyChangeEvent evt) {
				jSplitPane1PropertyChange(evt);
			}
		});
		
		jPanel1.addComponentListener(new java.awt.event.ComponentAdapter() {
			
			@Override
			public void componentResized(java.awt.event.ComponentEvent evt) {
				jPanel1ComponentResized(evt);
			}
		});
		jPanel1.setLayout(new java.awt.BorderLayout());
		
		jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Memory"));
		jPanel6.setLayout(new java.awt.BorderLayout());
		
		jScrollPane2.setBorder(null);
		
		jTable1.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null, null },
			{ null, null, null, null }, { null, null, null, null }, { null, null, null, null } },
			new String[] { "Title 1",
				"Title 2", "Title 3", "Title 4" }));
		jScrollPane2.setViewportView(jTable1);
		
		jPanel6.add(jScrollPane2, java.awt.BorderLayout.CENTER);
		
		toggleButtonDesc.setText("desc");
		toggleButtonDesc.setToolTipText("Show cells in descending order");
		toggleButtonDesc.setBorderPainted(false);
		toggleButtonDesc.setOpaque(false);
		toggleButtonDesc.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				toggleButtonDescActionPerformed(evt);
			}
		});
		jToolBar1.add(toggleButtonDesc);
		
		toggleButtonHex.setSelected(true);
		toggleButtonHex.setText("hex");
		toggleButtonHex.setToolTipText("Show addresses as hex numbers");
		toggleButtonHex.setBorderPainted(false);
		toggleButtonHex.setOpaque(false);
		toggleButtonHex.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				toggleButtonHexActionPerformed(evt);
			}
		});
		jToolBar1.add(toggleButtonHex);
		
		toggleButtonHighlight.setText("highlight");
		toggleButtonHighlight.setToolTipText("Highlight cells that registers are pointing to");
		toggleButtonHighlight.setBorderPainted(false);
		toggleButtonHighlight.setOpaque(false);
		toggleButtonHighlight.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				toggleButtonHighlightActionPerformed(evt);
			}
		});
		jToolBar1.add(toggleButtonHighlight);
		
		jPanel9.setOpaque(false);
		jToolBar1.add(jPanel9);
		
		toggleButton8Bit.setText("8 Bit");
		toggleButton8Bit.setBorderPainted(false);
		toggleButton8Bit.setOpaque(false);
		toggleButton8Bit.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				toggleButton8BitActionPerformed(evt);
			}
		});
		jToolBar1.add(toggleButton8Bit);
		
		toggleButton16Bit.setText("16Bit");
		toggleButton16Bit.setBorderPainted(false);
		toggleButton16Bit.setOpaque(false);
		toggleButton16Bit.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				toggleButton16BitActionPerformed(evt);
			}
		});
		jToolBar1.add(toggleButton16Bit);
		
		toggleButton32Bit.setSelected(true);
		toggleButton32Bit.setText("32Bit");
		toggleButton32Bit.setBorderPainted(false);
		toggleButton32Bit.setOpaque(false);
		toggleButton32Bit.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				toggleButton32BitActionPerformed(evt);
			}
		});
		jToolBar1.add(toggleButton32Bit);
		
		jPanel6.add(jToolBar1, java.awt.BorderLayout.NORTH);
		
		jPanel1.add(jPanel6, java.awt.BorderLayout.CENTER);
		
		jSplitPane1.setRightComponent(jPanel1);
		
		jSplitPane4.setBorder(null);
		jSplitPane4.setDividerLocation(700);
		jSplitPane4.setDividerSize(3);
		jSplitPane4.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		jSplitPane4.setContinuousLayout(true);
		
		jPanel4.setLayout(new java.awt.BorderLayout());
		
		jPanel7.setLayout(new java.awt.BorderLayout());
		
		ErrorLabel.setForeground(new java.awt.Color(255, 0, 0));
		jPanel7.add(ErrorLabel, java.awt.BorderLayout.CENTER);
		
		jPanel4.add(jPanel7, java.awt.BorderLayout.SOUTH);
		
		jScrollPane1.setBorder(null);
		
		jPanel2.setLayout(new java.awt.BorderLayout());
		
		NumberPanel.setPreferredSize(new java.awt.Dimension(40, 100));
		NumberPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
			
			@Override
			public void componentResized(java.awt.event.ComponentEvent evt) {
				NumberPanelComponentResized(evt);
			}
		});
		NumberPanel.setLayout(null);
		jPanel2.add(NumberPanel, java.awt.BorderLayout.WEST);
		
		jTextPane1.setBorder(null);
		jTextPane1.addMouseListener(new java.awt.event.MouseAdapter() {
			
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				jTextPane1MouseClicked(evt);
			}
		});
		jTextPane1.addCaretListener(new javax.swing.event.CaretListener() {
			
			public void caretUpdate(javax.swing.event.CaretEvent evt) {
				jTextPane1CaretUpdate(evt);
			}
		});
		jTextPane1.addInputMethodListener(new java.awt.event.InputMethodListener() {
			
			/**
			 * @param evt
			 *        the Event that triggered this action
			 */
			public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
			}
			
			public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
				jTextPane1InputMethodTextChanged(evt);
			}
		});
		jTextPane1.addKeyListener(new java.awt.event.KeyAdapter() {
			
			@Override
			public void keyPressed(java.awt.event.KeyEvent evt) {
				jTextPane1KeyPressed(evt);
			}
			
			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
				jTextPane1KeyReleased(evt);
			}
			
			@Override
			public void keyTyped(java.awt.event.KeyEvent evt) {
				jTextPane1KeyTyped(evt);
			}
		});
		jPanel2.add(jTextPane1, java.awt.BorderLayout.CENTER);
		
		jScrollPane1.setViewportView(jPanel2);
		
		jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);
		
		jSplitPane4.setLeftComponent(jPanel4);
		
		jTabbedPane1.setTabPlacement(SwingConstants.LEFT);
		jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
			
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
				jTabbedPane1StateChanged(evt);
			}
		});
		
		jPanelHelp.addComponentListener(new java.awt.event.ComponentAdapter() {
			
			@Override
			public void componentResized(java.awt.event.ComponentEvent evt) {
				jPanelHelpComponentResized(evt);
			}
		});
		jPanelHelp.setLayout(new java.awt.BorderLayout());
		jTabbedPane1.addTab("Help", jPanelHelp);
		
		jSplitPane4.setRightComponent(jTabbedPane1);
		
		jSplitPane1.setLeftComponent(jSplitPane4);
		
		jSplitPane2.setRightComponent(jSplitPane1);
		
		jSplitPane3.setBorder(null);
		jSplitPane3.setDividerSize(3);
		jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
		
		jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Registers"));
		jPanel8.addComponentListener(new java.awt.event.ComponentAdapter() {
			
			@Override
			public void componentResized(java.awt.event.ComponentEvent evt) {
				jPanel8ComponentResized(evt);
			}
		});
		jPanel8.setLayout(new java.awt.BorderLayout());
		
		jPanel5.setLayout(new java.awt.GridLayout(4, 2));
		
		jCheckCarry.setText("Carry");
		jCheckCarry.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jCheckCarry.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jCheckCarry.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckCarryActionPerformed(evt);
			}
		});
		jPanel5.add(jCheckCarry);
		
		jCheckOverflow.setText("Overflow");
		jCheckOverflow.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jCheckOverflow.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jCheckOverflow.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckOverflowActionPerformed(evt);
			}
		});
		jPanel5.add(jCheckOverflow);
		
		jCheckSign.setText("Sign");
		jCheckSign.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jCheckSign.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jCheckSign.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckSignActionPerformed(evt);
			}
		});
		jPanel5.add(jCheckSign);
		
		jCheckZero.setText("Zero");
		jCheckZero.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jCheckZero.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jCheckZero.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckZeroActionPerformed(evt);
			}
		});
		jPanel5.add(jCheckZero);
		
		jCheckParity.setText("Parity");
		jCheckParity.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jCheckParity.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jCheckParity.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckParityActionPerformed(evt);
			}
		});
		jPanel5.add(jCheckParity);
		
		jCheckAuxiliary.setText("Auxiliary");
		jCheckAuxiliary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jCheckAuxiliary.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jCheckAuxiliary.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckAuxiliaryActionPerformed(evt);
			}
		});
		jPanel5.add(jCheckAuxiliary);
		
		jCheckTrap.setText("Trap");
		jCheckTrap.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jCheckTrap.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jCheckTrap.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckTrapActionPerformed(evt);
			}
		});
		jPanel5.add(jCheckTrap);
		
		jCheckDirection.setText("Direction");
		jCheckDirection.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jCheckDirection.setMargin(new java.awt.Insets(0, 0, 0, 0));
		jCheckDirection.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCheckDirectionActionPerformed(evt);
			}
		});
		jPanel5.add(jCheckDirection);
		
		jPanel8.add(jPanel5, java.awt.BorderLayout.SOUTH);
		
		jPanel10.setLayout(new java.awt.BorderLayout());
		
		jToolBar2.setRollover(true);
		
		jToggleButton6.setText("bin");
		jToggleButton6.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jToggleButton6.setMaximumSize(new java.awt.Dimension(45, 27));
		jToggleButton6.setMinimumSize(new java.awt.Dimension(45, 27));
		jToggleButton6.setOpaque(false);
		jToggleButton6.setPreferredSize(new java.awt.Dimension(45, 27));
		jToggleButton6.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jToggleButton6ActionPerformed(evt);
			}
		});
		jToolBar2.add(jToggleButton6);
		
		jToggleButton8.setSelected(true);
		jToggleButton8.setText("±dec");
		jToggleButton8.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jToggleButton8.setMaximumSize(new java.awt.Dimension(45, 27));
		jToggleButton8.setMinimumSize(new java.awt.Dimension(45, 27));
		jToggleButton8.setOpaque(false);
		jToggleButton8.setPreferredSize(new java.awt.Dimension(45, 27));
		jToggleButton8.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jToggleButton8ActionPerformed(evt);
			}
		});
		jToolBar2.add(jToggleButton8);
		
		jToggleButton7.setText("dec");
		jToggleButton7.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jToggleButton7.setMaximumSize(new java.awt.Dimension(45, 27));
		jToggleButton7.setMinimumSize(new java.awt.Dimension(45, 27));
		jToggleButton7.setOpaque(false);
		jToggleButton7.setPreferredSize(new java.awt.Dimension(45, 27));
		jToggleButton7.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jToggleButton7ActionPerformed(evt);
			}
		});
		jToolBar2.add(jToggleButton7);
		
		jToggleButton5.setText("hex");
		jToggleButton5.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
		jToggleButton5.setMaximumSize(new java.awt.Dimension(45, 27));
		jToggleButton5.setMinimumSize(new java.awt.Dimension(45, 27));
		jToggleButton5.setOpaque(false);
		jToggleButton5.setPreferredSize(new java.awt.Dimension(45, 27));
		jToggleButton5.addActionListener(new java.awt.event.ActionListener() {
			
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jToggleButton5ActionPerformed(evt);
			}
		});
		jToolBar2.add(jToggleButton5);
		
		jPanel10.add(jToolBar2, java.awt.BorderLayout.NORTH);
		
		jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));
		jPanel10.add(jPanel3, java.awt.BorderLayout.CENTER);
		
		jPanel8.add(jPanel10, java.awt.BorderLayout.NORTH);
		
		jSplitPane3.setTopComponent(jPanel8);
		
		jPanel13.setLayout(new java.awt.BorderLayout());
		jSplitPane3.setBottomComponent(jPanel13);
		
		jSplitPane2.setLeftComponent(jSplitPane3);
		
		add(jSplitPane2, java.awt.BorderLayout.CENTER);
	}// </editor-fold>//GEN-END:initComponents
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {// GEN-FIRST:event_jTabbedPane1StateChanged
		Component cmp = jTabbedPane1.getSelectedComponent();
		if (lastSelected != null) {
			lastSelected.setActivated(false);
		}
		if (cmp instanceof IGuiModule) {
			lastSelected = (IGuiModule) cmp;
			lastSelected.setActivated(true);
		}
	}// GEN-LAST:event_jTabbedPane1StateChanged
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void toggleButtonHighlightActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_toggleButtonHighlightActionPerformed
		updateMemoryHighlight(toggleButtonHighlight.isSelected());
	}// GEN-LAST:event_toggleButtonHighlightActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void toggleButtonHexActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_toggleButtonHexActionPerformed
		// trigger a redraw
		model.enableDescending(model.isDescending());
	}// GEN-LAST:event_toggleButtonHexActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem12ActionPerformed
		undoManager.redo();
		frame.checkButtonStates();
	}// GEN-LAST:event_jMenuItem12ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem11ActionPerformed
		undoManager.undo();
		frame.checkButtonStates();
	}// GEN-LAST:event_jMenuItem11ActionPerformed
	
	private void jTextPane1MouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jTextPane1MouseClicked
		if (evt.getButton() == MouseEvent.BUTTON3) {
			checkButtonStates();
			jPopupMenu1.show(jTextPane1, evt.getX(), evt.getY());
		}
		
	}// GEN-LAST:event_jTextPane1MouseClicked
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem10ActionPerformed
		getEditor().paste();
		frame.checkButtonStates();
	}// GEN-LAST:event_jMenuItem10ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem9ActionPerformed
		getEditor().copy();
	}// GEN-LAST:event_jMenuItem9ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem8ActionPerformed
		getEditor().cut();
	}// GEN-LAST:event_jMenuItem8ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jPanel8ComponentResized(java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_jPanel8ComponentResized
		frame.putProperty("split3.location", jSplitPane3.getDividerLocation());
	}// GEN-LAST:event_jPanel8ComponentResized
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jSplitPane1ComponentResized(java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_jSplitPane1ComponentResized
		frame.putProperty("split2.location", jSplitPane2.getDividerLocation());
	}// GEN-LAST:event_jSplitPane1ComponentResized
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jPanelHelpComponentResized(java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_jPanelHelpComponentResized
		frame.putProperty("split4.location", jSplitPane4.getDividerLocation());
	}// GEN-LAST:event_jPanelHelpComponentResized
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jPanel1ComponentResized(java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_jPanel1ComponentResized
		frame.putProperty("split1.location", jSplitPane1.getDividerLocation());
	}// GEN-LAST:event_jPanel1ComponentResized
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jSplitPane1ComponentMoved(java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_jSplitPane1ComponentMoved
	}// GEN-LAST:event_jSplitPane1ComponentMoved
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jSplitPane1MouseDragged(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jSplitPane1MouseDragged
	}// GEN-LAST:event_jSplitPane1MouseDragged
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jSplitPane1MouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jSplitPane1MouseClicked
	}// GEN-LAST:event_jSplitPane1MouseClicked
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jSplitPane1MouseReleased(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jSplitPane1MouseReleased
	}// GEN-LAST:event_jSplitPane1MouseReleased
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jSplitPane1PropertyChange(java.beans.PropertyChangeEvent evt) {// GEN-FIRST:event_jSplitPane1PropertyChange
	}// GEN-LAST:event_jSplitPane1PropertyChange
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jToggleButton8ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jToggleButton8ActionPerformed
		setRegisterMode(DataSpace.SIGNED);
	}// GEN-LAST:event_jToggleButton8ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jTextPane1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {// GEN-FIRST:event_jTextPane1InputMethodTextChanged
	}// GEN-LAST:event_jTextPane1InputMethodTextChanged
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jTextPane1KeyTyped(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_jTextPane1KeyTyped
	}// GEN-LAST:event_jTextPane1KeyTyped
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jToggleButton6ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jToggleButton6ActionPerformed
		setRegisterMode(DataSpace.BIN);
	}// GEN-LAST:event_jToggleButton6ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jToggleButton7ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jToggleButton7ActionPerformed
		setRegisterMode(DataSpace.UNSIGNED);
	}// GEN-LAST:event_jToggleButton7ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jToggleButton5ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jToggleButton5ActionPerformed
		setRegisterMode(DataSpace.HEX);
	}// GEN-LAST:event_jToggleButton5ActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void NumberPanelComponentResized(java.awt.event.ComponentEvent evt) {// GEN-FIRST:event_NumberPanelComponentResized
		updateLineNumbers();
	}// GEN-LAST:event_NumberPanelComponentResized
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void toggleButton32BitActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_toggleButton32BitActionPerformed
		jTable1.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
		model.setMode(MemoryTableModel.DWORD);
		toggleButton32Bit.setSelected(true);
		toggleButton16Bit.setSelected(false);
		toggleButton8Bit.setSelected(false);
		
	}// GEN-LAST:event_toggleButton32BitActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void toggleButton16BitActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_toggleButton16BitActionPerformed
		jTable1.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
		model.setMode(MemoryTableModel.WORD);
		toggleButton32Bit.setSelected(false);
		toggleButton16Bit.setSelected(true);
		toggleButton8Bit.setSelected(false);
		
	}// GEN-LAST:event_toggleButton16BitActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void toggleButton8BitActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_toggleButton8BitActionPerformed
		jTable1.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
		model.setMode(MemoryTableModel.BYTE);
		toggleButton32Bit.setSelected(false);
		toggleButton16Bit.setSelected(false);
		toggleButton8Bit.setSelected(true);
		
	}// GEN-LAST:event_toggleButton8BitActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void toggleButtonDescActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_toggleButtonDescActionPerformed
	
		model.enableDescending(!model.isDescending());
		toggleButtonDesc.setSelected(model.isDescending());
		
	}// GEN-LAST:event_toggleButtonDescActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jCheckDirectionActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jCheckDirectionActionPerformed
		data.fDirection = jCheckDirection.isSelected();
	}// GEN-LAST:event_jCheckDirectionActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jCheckTrapActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jCheckTrapActionPerformed
		data.fTrap = jCheckTrap.isSelected();
	}// GEN-LAST:event_jCheckTrapActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jCheckZeroActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jCheckZeroActionPerformed
		data.fZero = jCheckZero.isSelected();
	}// GEN-LAST:event_jCheckZeroActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jCheckSignActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jCheckSignActionPerformed
		data.fSign = jCheckSign.isSelected();
	}// GEN-LAST:event_jCheckSignActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jCheckOverflowActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jCheckOverflowActionPerformed
		data.fOverflow = jCheckOverflow.isSelected();
	}// GEN-LAST:event_jCheckOverflowActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jCheckCarryActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jCheckCarryActionPerformed
	
		data.fCarry = jCheckCarry.isSelected();
	}// GEN-LAST:event_jCheckCarryActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jCheckAuxiliaryActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jCheckAuxiliaryActionPerformed
		data.fAuxiliary = jCheckAuxiliary.isSelected();
	}// GEN-LAST:event_jCheckAuxiliaryActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jCheckParityActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jCheckParityActionPerformed
		data.fParity = jCheckParity.isSelected();
	}// GEN-LAST:event_jCheckParityActionPerformed
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jTextPane1CaretUpdate(javax.swing.event.CaretEvent evt) {// GEN-FIRST:event_jTextPane1CaretUpdate
		int lineNumber = highlighter.getLineNumberByOffset(evt.getDot());
		ParseError error = highlighter.getErrorByLineNumber(lineNumber);
		if (error != null) {
			ErrorLabel.setText(error.errorMsg);
		} else {
			ErrorLabel.setText("");
		}
		String mnemo = highlighter.getMnemoByLineNumber(lineNumber);
		setHelp(mnemo);
	}// GEN-LAST:event_jTextPane1CaretUpdate
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jTextPane1KeyReleased(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_jTextPane1KeyReleased
	}// GEN-LAST:event_jTextPane1KeyReleased
	
	/**
	 * @param evt
	 *        the Event that triggered this action
	 */
	private void jTextPane1KeyPressed(java.awt.event.KeyEvent evt) {// GEN-FIRST:event_jTextPane1KeyPressed
	}// GEN-LAST:event_jTextPane1KeyPressed
	
	public boolean isMemAddressAsHex() {
		return toggleButtonHex.isSelected();
	}
	
	/**
	 * The keyword of the current context help. Is used for deciding if the help should be updated.
	 */
	private String currentHelpkeyWord = "";
	
	/**
	 * Sets the context help for a given command.
	 * 
	 * @param keyword
	 *        The name of the command for which the context help should be set.
	 */
	public void setHelp(String keyword) {
		if ((keyword != null) && !currentHelpkeyWord.equalsIgnoreCase(keyword)) {
			String help = frame.helpLoader.get(keyword);
			if (help != null) {
				helpBrowser.setText(help);
				currentHelpkeyWord = keyword;
			} else {
				helpBrowser.setText("No help found for " + keyword);
				currentHelpkeyWord = "";
			}
		}
		
		if (keyword == null) {
			helpBrowser.setText("No help available for current context.");
			currentHelpkeyWord = "";
		}
	}
	
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JLabel ErrorLabel;
	private javax.swing.JPanel NumberPanel;
	private javax.swing.JCheckBox jCheckAuxiliary;
	private javax.swing.JCheckBox jCheckCarry;
	private javax.swing.JCheckBox jCheckDirection;
	private javax.swing.JCheckBox jCheckOverflow;
	private javax.swing.JCheckBox jCheckParity;
	private javax.swing.JCheckBox jCheckSign;
	private javax.swing.JCheckBox jCheckTrap;
	private javax.swing.JCheckBox jCheckZero;
	private javax.swing.JMenuItem jMenuItem10;
	private javax.swing.JMenuItem jMenuItem11;
	private javax.swing.JMenuItem jMenuItem12;
	private javax.swing.JMenuItem jMenuItem8;
	private javax.swing.JMenuItem jMenuItem9;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel10;
	private javax.swing.JPanel jPanel13;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JPanel jPanel3;
	private javax.swing.JPanel jPanel4;
	private javax.swing.JPanel jPanel5;
	private javax.swing.JPanel jPanel6;
	private javax.swing.JPanel jPanel7;
	private javax.swing.JPanel jPanel8;
	private javax.swing.JPanel jPanel9;
	private javax.swing.JPanel jPanelHelp;
	private javax.swing.JPopupMenu jPopupMenu1;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane2;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JSplitPane jSplitPane1;
	private javax.swing.JSplitPane jSplitPane2;
	private javax.swing.JSplitPane jSplitPane3;
	private javax.swing.JSplitPane jSplitPane4;
	private javax.swing.JTabbedPane jTabbedPane1;
	private javax.swing.JTable jTable1;
	private javax.swing.JTextPane jTextPane1;
	private javax.swing.JToggleButton jToggleButton5;
	private javax.swing.JToggleButton jToggleButton6;
	private javax.swing.JToggleButton jToggleButton7;
	private javax.swing.JToggleButton jToggleButton8;
	private javax.swing.JToolBar jToolBar1;
	private javax.swing.JToolBar jToolBar2;
	private javax.swing.JToggleButton toggleButton16Bit;
	private javax.swing.JToggleButton toggleButton32Bit;
	private javax.swing.JToggleButton toggleButton8Bit;
	private javax.swing.JToggleButton toggleButtonDesc;
	private javax.swing.JToggleButton toggleButtonHex;
	private javax.swing.JToggleButton toggleButtonHighlight;
	
	// End of variables declaration//GEN-END:variables
	
}
