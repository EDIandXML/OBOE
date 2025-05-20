/**
 * Copyright 2025 Joe McVerry
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.github.EDIandXML.OBOE.util;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.ElementRules;
import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.Identifier;
import io.github.EDIandXML.OBOE.TransactionSetFactory;
import io.github.EDIandXML.OBOE.Containers.ContainerType;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.DataElements.IDList;
import io.github.EDIandXML.OBOE.DataElements.IDListProcessor;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateCompositeElement;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.Templates.TemplateElement;
import io.github.EDIandXML.OBOE.Templates.TemplateLoop;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;
import io.github.EDIandXML.OBOE.Templates.TemplateTable;
import io.github.EDIandXML.OBOE.Templates.TemplateTransactionSet;

/**
 * application to maintain message descriptions
 * 
 * OBOE - Open Business Objects for EDI
 * 
 *
 * @author Joe McVerry
 * 
 */

public class TransactionSetMessageEditor extends JFrame
		implements WindowListener, ActionListener, TreeSelectionListener,
		MouseListener, TreeWillExpandListener {

	private static final long serialVersionUID = 1L;

	TemplateTransactionSet tsWork = null;
	TemplateTransactionSet tsStatic = null;

	int saveFormat;
	String filename;
	String dirname;
	String saveString;
	JTree jt;
	JCheckBoxMenuItem menuUseName = null;
	JScrollPane jsp;
	JPanel jp;
	DefaultTreeModel dtm;
	SQLEDITree selectedObject;
	Object RICEObject;
	Object RICEObjectParent;
	Object RICEObjectWorkingParent;
	ArrayList<IDListProcessor> idListsWork;

	JMenuBar jmb = null;

	boolean changed = false;

	DefaultMutableTreeNode top;

	static Logger logr = LogManager
			.getLogger(TransactionSetMessageEditor.class.getName());

	static final String SESSION_FILE = "session.properties";
	Properties props = new Properties();
	final String XMLDIRLASTUSED = "last.used.xml.directory";
	final String FILEDIRLASTUSED = "last.used.file.directory";
	FileOutputStream propertyOutputFile;

	/**
	 * constructor
	 *
	 * @param inID String transaction set id to process
	 */

	public TransactionSetMessageEditor(String inID) {

		super("OBOE - Rule and EDI Code Builder");

		try {
			props.load(new FileInputStream(SESSION_FILE));
		} catch (FileNotFoundException e) {
			try {
				props.put(XMLDIRLASTUSED, Util.getMessageDescriptionFolder());
			} catch (IOException e1) {

				props.put(XMLDIRLASTUSED, "");
			}
			props.put(FILEDIRLASTUSED, System.getProperty("user.dir"));

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}

		try {
			propertyOutputFile = new FileOutputStream(SESSION_FILE);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.exit(3);
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			logr.error(e1.getMessage(), e1);
			JOptionPane.showMessageDialog(this,
					e1.getMessage() + " See stack trace in log file",
					"UIManager Exception Occur", JOptionPane.ERROR_MESSAGE);
		}

		jp = new JPanel();
		jmb = new JMenuBar();
		JMenuItem jmi;

		JMenu jm = new JMenu("File");
		jmb.add(jm);

		jmi = new JMenuItem("Open...");
		jmi.setActionCommand("open");
		jmi.setMnemonic('O');
		jmi.addActionListener(this);
		jmi.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));

		jm.add(jmi);
		jmi = new JMenuItem("Save Transaction Description");
		jmi.setEnabled(false);
		jmi.setActionCommand("SaveRulesFile");
		jmi.setMnemonic('R');
		jmi.addActionListener(this);
		jmi.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		jm.add(jmi);

		jmi = new JMenuItem("Close");

		jmi.setActionCommand("close");
		jmi.setMnemonic('X');
		jmi.addActionListener(this);
		jmi.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		jm.add(jmi);

		jm = new JMenu("Node Control");
		jm.setEnabled(false);
		jmb.add(jm);
		jmi = new JMenuItem("Show Mandatory From Here");
		jmi.setActionCommand("ShowMandatory");
		jmi.addActionListener(this);
		// showMandatoryOnly.setAccelerator()
		jm.add(jmi);
		jmi = new JMenuItem("Show All From Here");
		jmi.setActionCommand("ShowAll");
		jmi.addActionListener(this);
		// showMandatoryOnly.setAccelerator()
		jm.add(jmi);

		jm = new JMenu("Create Programs");
		jmb.add(jm);
		jm.setEnabled(false);

		jmi = new JMenuItem("Create Inbound Java Program");
		jmi.setEnabled(true);
		jmi.setActionCommand("BuildInboundJava");
		jmi.setMnemonic('I');
		jmi.addActionListener(this);
		jmi.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
		jm.add(jmi);

		jmi = new JMenuItem("Create Outbound Java Program");
		jmi.setEnabled(true);
		jmi.setActionCommand("BuildOutboundJava");
		jmi.setMnemonic('O');
		jmi.addActionListener(this);
		jmi.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
		jm.add(jmi);

		setJMenuBar(jmb);

		jm.add(new JSeparator());

		menuUseName = new JCheckBoxMenuItem(
				"Build Using Object Name not its ID", true);

		jm.add(menuUseName);

		jp.setLayout(new GridLayout(1, 1));

		if (inID != null) {
			buildTree(inID);
		}

		addWindowListener(this);

		getContentPane().add(jp, java.awt.BorderLayout.CENTER);
		pack();
		setSize(new Dimension(660, 480));
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setVisible(true);

	}

	/**
	 * Builds the start of the tree from an incoming TransactionSet id
	 *
	 * @param inFileName String filename
	 */
	public void buildTree(String inFileName) {

		jp.removeAll();

		setTitle("OBOE - Rules and EDI Code Builder: loading " + inFileName);

		pack();

		File f = new File(inFileName);
		dirname = f.getParent();

		try {
			tsWork = (TemplateTransactionSet) TransactionSetFactory
					.buildTransactionSet(f).getMyTemplate();
			tsStatic = (TemplateTransactionSet) TransactionSetFactory
					.buildTransactionSet(
							new File(getBaseDirectory() + f.getName()))
					.getMyTemplate();
		} catch (Exception e1) {
			logr.error(e1.getMessage(), e1);
			JOptionPane.showMessageDialog(this,
					e1.getMessage() + " See stack trace in log file",
					"Exception Creating From User File",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		filename = inFileName;
		idListsWork = new ArrayList<IDListProcessor>();

		top = new DefaultMutableTreeNode(new SQLEDITree(inFileName, null));

		dtm = new DefaultTreeModel(top);
		jt = new JTree(dtm);

		jt.addTreeWillExpandListener(this);

		/*
		 * this will disable collapse/expand when the tree node is double
		 * clicked
		 */

		jt.setUI(new javax.swing.plaf.basic.BasicTreeUI() {
			@Override
			protected boolean isToggleEvent(MouseEvent event) {
				return false;
			}
		});

		TemplateTable tblWork = tsWork.getHeaderTemplateTable();
		TemplateTable tblStatic = tsStatic.getHeaderTemplateTable();

		jt.setShowsRootHandles(true);

		addTableToTree(top, tblWork, tblStatic);

		tblWork = tsWork.getDetailTemplateTable();
		tblStatic = tsStatic.getDetailTemplateTable();

		if (tblStatic != null) {
			addTableToTree(top, tblWork, tblStatic);
		}

		tblWork = tsWork.getSummaryTemplateTable();
		tblStatic = tsStatic.getSummaryTemplateTable();

		if (tblStatic != null) {
			addTableToTree(top, tblWork, tblStatic);
		}

		jt.addTreeSelectionListener(this);
		jt.addMouseListener(this);

		jp.removeAll();

		jsp = new JScrollPane(jt);
		jp.add(jsp);

		setTitle("OBOE - Rules and EDI Code Builder: " + filename + " "
				+ tsWork.getName());
		idListsWork = new ArrayList<IDListProcessor>();
		changed = false;
		collapseToUsed(new TreePath(top));

		jmb.getMenu(0).getMenuComponent(1).setEnabled(true);
		jmb.getMenu(1).setEnabled(true);
		jmb.getMenu(2).setEnabled(true);

		pack();

	}

	/**
	 * add an object to the displayed tree
	 *
	 * @param parent  DefaultMututableTreeNode
	 * @param child   DefaultMututableTreeNode
	 * @param visible boolean is this node to be seen on the tree
	 */

	public void addToTree(DefaultMutableTreeNode parent,
			DefaultMutableTreeNode child, boolean visible) {
		SQLEDITree rt = (SQLEDITree) child.getUserObject();

		dtm.insertNodeInto(child, parent, parent.getChildCount());
		var tp = new TreePath(child.getPath());
		if (visible) {
			rt.setInUse();
			jt.scrollPathToVisible(tp);

		} else {
			rt.setNotInUse();
			// jt.collapsePath(tp);

		}

	}

	TreePath tp;

	boolean headerDone = false;
	ArrayList<String> preBuffer = new ArrayList<String>();

	/**
	 * sets up TemplateTable tree structure
	 *
	 * @return DefaultMutableTreeNode for JTree object
	 * @param parent    DefaultMutableTreeNode
	 * @param tblWork   TemplateTable being worked on
	 * @param tblStatic
	 * @exception OBOEException see OBOEException
	 */
	public DefaultMutableTreeNode addTableToTree(DefaultMutableTreeNode parent,
			TemplateTable tblWork, TemplateTable tblStatic)
			throws OBOEException {
		SQLEDITree rt = new SQLEDITree("Table " + tblStatic.getShortName(),
				tblWork);
		DefaultMutableTreeNode thisNode = new DefaultMutableTreeNode(rt);
		addToTree(parent, thisNode, true);

		rt.setInUse();
		jt.expandPath(new TreePath(thisNode));
		TemplateSegment tsStatic;
		TemplateLoop tlStatic;
		for (var elm : tblStatic.getContainer()) {
			if (elm.getContainerType() == ContainerType.Segment) {
				tsStatic = (TemplateSegment) elm;
				addSegmentToTree(thisNode,
						(tblWork == null ? null
								: tblWork.getTemplateSegment(elm.getID())),
						tsStatic, true);
			} else if (elm.getContainerType() == ContainerType.Loop) {
				tlStatic = (TemplateLoop) elm;
				addLoopToTree(thisNode,
						(tblWork == null ? null
								: tblWork.getTemplateLoop(elm.getID())),
						tlStatic, true);
			}
		}

		return thisNode;
	}

	/**
	 * sets up TemplateLoop tree structure
	 *
	 * @return DefaultMutableTreeNode for JTree object
	 * @param parent   DefaultMutableTreeNode
	 * @param loopWork TemplateLoop being worked on
	 * @exception OBOEException see OBOEException
	 */
	public DefaultMutableTreeNode addLoopToTree(DefaultMutableTreeNode parent,
			TemplateLoop loopWork, TemplateLoop loopStatic, boolean visible)
			throws OBOEException {
		String treeText = "<html>";
		if (loopStatic.isRequired()) {
			treeText += "<font color=\"blue\">";
		} else {
			treeText += "<font color=\"black\">";
		}

		treeText += "Loop " + loopStatic.getID() + " Name:"
				+ loopStatic.getName();
		treeText += "</html>";
		boolean isVisible = visible & loopStatic.isUsed() & loopWork != null;
		SQLEDITree rt = new SQLEDITree(treeText, isVisible, loopStatic);
		DefaultMutableTreeNode thisNode = new DefaultMutableTreeNode(rt);

		if (isVisible) {
			rt.setInUse();
		} else {
			rt.setNotInUse();
		}

		addToTree(parent, thisNode, isVisible);

		for (var elm : loopStatic.getContainer()) {
			if (elm.getContainerType() == ContainerType.Segment) {
				var tsStatic = (TemplateSegment) elm;
				addSegmentToTree(thisNode,
						(loopWork == null ? null
								: loopWork.getTemplateSegment(elm.getID())),
						tsStatic, isVisible);
			} else if (elm.getContainerType() == ContainerType.Loop) {
				var tlStatic = (TemplateLoop) elm;
				addLoopToTree(thisNode,
						(loopWork == null ? null
								: loopWork.getTemplateLoop(elm.getID())),
						tlStatic, isVisible);
			}
		}

		if (isVisible) {
			jt.expandPath(new TreePath(thisNode));
		} else {
			jt.collapsePath(new TreePath(thisNode));
		}
		return thisNode;
	}

	String currentSegID = "";

	/**
	 * sets up Segment tree structure
	 *
	 * @return DefaultMutableTreeNode for TemplateTable tree object
	 * @param parent     DefaultMutableTreeNode
	 * @param intSegWork TemplateSegment being worked on
	 * @exception OBOEException see OBOEException
	 */
	public DefaultMutableTreeNode addSegmentToTree(
			DefaultMutableTreeNode parent, TemplateSegment segWork,
			TemplateSegment segStatic, boolean visible) throws OBOEException {
		SQLEDITree rt;
		String treeText = "<html>";
		if (segStatic.isRequired()) {
			treeText += "<font color=\"blue\">";
		} else {
			treeText += "<font color=\"black\">";
		}

		treeText += "Segment " + segStatic.getID() + " Name:"
				+ segStatic.getName();
		treeText += "</html>";
		boolean isVisible = visible & segStatic.isUsed() & segWork != null;

		rt = new SQLEDITree(treeText, isVisible, segStatic);
		DefaultMutableTreeNode thisNode = new DefaultMutableTreeNode(rt);

		if (isVisible) {
			rt.setInUse();
		} else {
			rt.setNotInUse();
		}

		currentSegID = segStatic.getID();
		addToTree(parent, thisNode, isVisible);

		for (var elm : segStatic.myElementContainer
				.getAllTemplateElementsValues()) {
			if (elm.IAmATemplateComposite()) {
				var tce = (TemplateCompositeElement) elm;
				addCompositeToTree(thisNode,
						(segWork == null ? null
								: (TemplateCompositeElement) segWork
										.getTemplateElement(tce.getPosition())),
						tce, isVisible);

			} else if (elm.IAmATemplateDE()) {
				var tde = (TemplateDataElement) elm;
				addDataElementToTree(thisNode,
						(segWork == null ? null
								: segWork
										.getTemplateElement(tde.getPosition())),
						tde, isVisible);
			}
		}

		if (isVisible) {
			jt.expandPath(new TreePath(thisNode));
		} else {
			jt.collapsePath(new TreePath(thisNode));
		}
		return thisNode;
	}

	/**
	 * sets up Composite tree structure
	 *
	 * @return DefaultMutableTreeNode for Segment tree object
	 * @param parent   DefaultMutuableTreeNode - parent node
	 * @param tcdeWork TemplateComposite being worked on
	 */
	public DefaultMutableTreeNode addCompositeToTree(
			DefaultMutableTreeNode parent, TemplateCompositeElement tcdeWork,
			TemplateCompositeElement tcdeStatic, boolean visible) {
		if (tcdeStatic == null) {
			return null;
		}
		SQLEDITree rt;
		String treeText = "<html>";
		if (tcdeStatic.isRequired()) {
			treeText += "<font color=\"blue\">";
		} else {
			treeText += "<font color=\"black\">";
		}

		treeText += currentSegID + df.format((tcdeStatic.getPosition()))
				+ " Composite " + tcdeStatic.getID() + " "
				+ tcdeStatic.getName();
		treeText += "</html>";
		boolean isVisible = visible & tcdeStatic.isUsed() & tcdeWork != null;
		rt = new SQLEDITree(treeText, isVisible, tcdeStatic);
		DefaultMutableTreeNode thisNode = new DefaultMutableTreeNode(rt);

		if (isVisible) {
			rt.setInUse();
		} else {
			rt.setNotInUse();
		}

		addToTree(parent, thisNode, isVisible);

		String saveSegID = currentSegID;
		currentSegID = currentSegID + df.format((tcdeStatic.getPosition()))
				+ "-";
		for (var elm : tcdeStatic.myElementContainer
				.getAllTemplateElementsValues()) {
			var tde = (TemplateDataElement) elm;
			addDataElementToTree(thisNode,
					(tcdeWork == null ? null
							: tcdeWork.getTemplateElement(tde.getPosition())),
					tde, isVisible);
		}
		currentSegID = saveSegID;

		if (isVisible) {
			jt.expandPath(new TreePath(thisNode));
		} else {
			jt.collapsePath(new TreePath(thisNode));
		}
		return thisNode;
	}

	static DecimalFormat df = new DecimalFormat("00");

	/**
	 * sets up DE tree structure
	 *
	 * @return DefaultMutableTreeNode for Segment or Composite tree object
	 * @param parent  DefaultMutableTreeNode - parent node
	 * @param tdeWork
	 * @param tdeWork TemplateDE being worked on
	 */

	public DefaultMutableTreeNode addDataElementToTree(
			DefaultMutableTreeNode parent, TemplateElement tdeWork,
			TemplateDataElement tdeStatic, boolean visible) {
		if (tdeStatic == null) {
			return null;
		}
		SQLEDITree rt = null;

		DefaultMutableTreeNode thisNode = null;
		DefaultMutableTreeNode useNode = null;
		boolean isVisible = visible & tdeStatic.isUsed() & tdeWork != null;

		boolean stillWorking = true;

		selectedObject = (SQLEDITree) parent.getUserObject();
		if (selectedObject.getObject() instanceof TemplateSegment) {
			TemplateSegment tseg = (TemplateSegment) selectedObject.getObject();
			if (tseg.getID().equals("ST")) {
				if (tdeStatic.getID().compareTo("143") == 0) {
					rt = new SQLEDITree(
							"Transaction Set Identifier Data Element", true,
							tdeStatic);
					thisNode = new DefaultMutableTreeNode(rt);
					useNode = new DefaultMutableTreeNode(
							new SQLEDITree(filename, isVisible, null));
					stillWorking = false;
				}
			}
		}

		if (stillWorking) {
			String treeText = "<html>";
			if (tdeStatic.isRequired()) {
				treeText += "<font color=\"blue\">";
			} else {
				treeText += "<font color=\"black\">";
			}

			treeText += currentSegID + df.format((tdeStatic.getPosition()))
					+ " " + tdeStatic.getID() + " " + tdeStatic.getName();
			treeText += "</html>";

			rt = new SQLEDITree(treeText, isVisible, tdeStatic);
			thisNode = new DefaultMutableTreeNode(rt);
			if (tdeStatic.getIDList() == null) {
				useNode = new DefaultMutableTreeNode(
						new SQLEDITree("Used", isVisible, null));
			} else {
				if (tdeStatic.getIDList() instanceof IDList) {
					useNode = new DefaultMutableTreeNode(new SQLEDITree(
							"<html><font color=\"green\">Select</html>",
							isVisible, tdeStatic.getIDList(), tdeStatic));
				} else {
					useNode = new DefaultMutableTreeNode(
							new SQLEDITree(
									"Class lookup using " + tdeStatic
											.getIDList().getClass().getName(),
									isVisible, null));
				}
			}
		}

		addToTree(parent, thisNode, isVisible);
		dtm.insertNodeInto(useNode, thisNode, thisNode.getChildCount());
		if (isVisible) {
			rt.setInUse();
			if (tdeStatic.getIDList() instanceof IDList) {
				int i;
				IDList idl;
				for (i = 0; i < idListsWork.size(); i++) {
					idl = (IDList) idListsWork.get(i);

					if (((IDList) tdeStatic.getIDList()).getShortName()
							.compareTo(idl.getShortName()) == 0) {
						break;
					}
				}
				if (i >= idListsWork.size()) {
					idListsWork.add(tdeStatic.getIDList());
				}
			}

		} else {
			rt.setNotInUse();
		}

		return thisNode;
	}

	/**
	 * registered event handler
	 *
	 * @param ae awt actionevent
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		String aec = ae.getActionCommand();
		if (aec.equals("close")) {
			if (changed) {
				int resp = JOptionPane.showConfirmDialog(this,
						"Data has been changed.  Do you want to continue?.",
						"Data Changed - Continue with exit?",
						JOptionPane.YES_NO_OPTION);

				if (resp == JOptionPane.NO_OPTION) {
					return;
				}

			}
			dispose();
			System.exit(0);
		}

		setCursor(new Cursor(Cursor.WAIT_CURSOR));

		if (aec.startsWith("open")) {
			try {
				String path = props.getProperty(XMLDIRLASTUSED);
				JFileChooser jfc = new JFileChooser(path);
				jfc.setFileFilter(new FileFilter("xml"));
				jfc.setDialogTitle("Open Message Description");
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = jfc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					filename = jfc.getSelectedFile().getPath();
					dirname = jfc.getSelectedFile().getParent();
					props.put(XMLDIRLASTUSED, dirname);
					props.store(propertyOutputFile, SESSION_FILE);
					if (filename.endsWith(".xml")) {
						buildTree(filename);
					} else {
						JOptionPane.showMessageDialog(this,
								"File name must end with a .xml string",
								"File Name Incompatible",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			} catch (Exception e1) {
				logr.error(e1.getMessage(), e1);

				JOptionPane.showMessageDialog(this,
						e1.getMessage() + " See stack trace in log file",
						"Exception Occur", JOptionPane.ERROR_MESSAGE);
			}
		}

		if (aec.equals("BuildInboundJava")) {
			try {
				String path = props.getProperty(FILEDIRLASTUSED);
				File f = new File(path);
				JFileChooser jfc = new JFileChooser(f);
				jfc.setFileFilter(new FileFilter("java"));
				jfc.setDialogTitle("Specify Java File Name.");
				JLabel jl = new JLabel("");
				jfc.setAccessory(jl);

				int returnVal = jfc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					var pgmfilename = jfc.getSelectedFile().getPath();
					dirname = jfc.getSelectedFile().getParent();
					props.put(FILEDIRLASTUSED, dirname);
					props.store(propertyOutputFile, SESSION_FILE);
					CharArrayWriter caw = new CharArrayWriter(100000);
					preBuffer = new ArrayList<String>();
					saveRulesFile(caw, false);
					TransactionSet ts = TransactionSetFactory
							.buildTransactionSetFromString(caw.toString());
					String envType;
					if (ts.getFormat() == Format.X12_FORMAT) {
						envType = "x12.envelope";
					} else {
						envType = "EDIFACT.envelope";
					}

					new OBOECodeGenerator(envType, ts, "p",
							this.menuUseName.getState(), pgmfilename, null);
				}

			} catch (Exception e1) {
				logr.error(e1.getMessage(), e1);

				JOptionPane.showMessageDialog(this,
						e1.getMessage() + " See stack trace in log file",
						"Exception Occur", JOptionPane.ERROR_MESSAGE);
			}

		}

		if (aec.equals("BuildOutboundJava")) {
			try {
				String path = props.getProperty(FILEDIRLASTUSED);
				File f = new File(path);
				JFileChooser jfc = new JFileChooser(f);
				jfc.setFileFilter(new FileFilter("java"));

				jfc.setDialogTitle("Specify Java File Name.");
				JLabel jl = new JLabel("");
				jfc.setAccessory(jl);

				int returnVal = jfc.showOpenDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String pgmfilename = jfc.getSelectedFile().getPath();
					dirname = jfc.getSelectedFile().getParent();
					props.put(FILEDIRLASTUSED, dirname);
					props.store(propertyOutputFile, SESSION_FILE);
					CharArrayWriter caw = new CharArrayWriter(100000);
					saveRulesFile(caw, false);
					TransactionSet ts = TransactionSetFactory
							.buildTransactionSetFromString(caw.toString());
					String envType;
					if (ts.getFormat() == Format.X12_FORMAT) {
						envType = "x12.envelope";
					} else {
						envType = "EDIFACT.envelope";
					}

					new OBOECodeGenerator(envType, ts, "b",
							this.menuUseName.getState(), pgmfilename, null);
				}

			} catch (Exception e1) {
				logr.error(e1.getMessage(), e1);

				JOptionPane.showMessageDialog(this,
						e1.getMessage() + " See stack trace in log file",
						"Exception Occur", JOptionPane.ERROR_MESSAGE);
			}

		}

		if (aec.startsWith("ShowMandatory")) {

			collapseToMandatory(jt.getSelectionPath());
		}
		if (aec.startsWith("ShowAll")) {

			expandAll(jt.getSelectionPath());
		}

		if (aec.startsWith("SaveRulesFile")) {
			try {
				File f = new File(filename);
				JFileChooser jfc = new JFileChooser(f);
				jfc.setFileFilter(new FileFilter("xml"));
				jfc.setSelectedFile(f);
				jfc.setDialogTitle("Save Transaction File");
				jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				JCheckBox jcb = new JCheckBox("Delete Unsed Objects", false);
				jfc.setAccessory(jcb);
				int returnVal = jfc.showSaveDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					filename = jfc.getSelectedFile().getPath();
					dirname = jfc.getSelectedFile().getParent();
					dirname = jfc.getSelectedFile().getParent();
					props.put(XMLDIRLASTUSED, dirname);
					props.store(propertyOutputFile, SESSION_FILE);
					FileWriter fw = new FileWriter(filename);
					saveRulesFile(fw, jcb.isSelected());
					fw.flush();
					fw.close();
				}

			} catch (IOException e1) {
				logr.error(e1.getMessage(), e1);
				JOptionPane.showMessageDialog(this,
						e1.getMessage() + " See stack trace in start window",
						"Exception Occur", JOptionPane.ERROR_MESSAGE);
			}

		}

		if (aec.startsWith("edit")) {
			doItemWork();
		}

		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		repaint();

	}

	public void collapseToUsed(TreePath inPath) {

		boolean expand = true;

		if (inPath == null) {
			return;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) inPath
				.getLastPathComponent();
		selectedObject = (SQLEDITree) node.getUserObject();
		var par = selectedObject.owningObject;
		if (par instanceof Identifier) {
			String got = ((Identifier) selectedObject.owningObject).getID();
			String get = got;
		}
		for (int i = node.getChildCount() - 1; i > -1; i--) {

			DefaultMutableTreeNode tn = (DefaultMutableTreeNode) node
					.getChildAt(i);

			TreePath path = inPath.pathByAddingChild(tn);
			collapseToUsed(path);
			SQLEDITree rt = (SQLEDITree) tn.getUserObject();

			expand = rt.displayed;

		}
		if (expand) {
			jt.expandPath(inPath);
			jt.makeVisible(inPath);
			jt.setSelectionPath(inPath);

		} else {
			jt.collapsePath(inPath);
		}

	}

	public void collapseToMandatory(TreePath inPath) {

		boolean expand = true;
		SQLEDITree selectedObject = null;
		if (inPath == null) {
			return;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) inPath
				.getLastPathComponent();
		selectedObject = (SQLEDITree) node.getUserObject();
		for (int i = node.getChildCount() - 1; i > -1; i--) {

			DefaultMutableTreeNode tn = (DefaultMutableTreeNode) node
					.getChildAt(i);
			TreePath path = inPath.pathByAddingChild(tn);
			collapseToMandatory(path);
			// Expansion or collapse must be done bottom-up

			if (selectedObject.getObject() instanceof TemplateTable) {
				;
			} else if (selectedObject.getObject() instanceof TemplateLoop) {
				TemplateLoop tl = (TemplateLoop) selectedObject.getObject();
				if (tl.getRequired() != 'M') {
					expand = false;
				}
			} else if (selectedObject.getObject() instanceof TemplateSegment) {
				TemplateSegment ts = (TemplateSegment) selectedObject
						.getObject();
				if (ts.getRequired() != 'M') {
					expand = false;
				}
			} else if (selectedObject
					.getObject() instanceof TemplateCompositeElement) {
				TemplateCompositeElement tcde = (TemplateCompositeElement) selectedObject
						.getObject();
				if (tcde.getRequired() != 'M') {
					expand = false;
				}
			} else if (selectedObject
					.getObject() instanceof TemplateDataElement) {
				TemplateDataElement tde = (TemplateDataElement) selectedObject
						.getObject();
				if (tde.getRequired() != 'M') {
					expand = false;
				}
			}

		}
		if (expand) {
			jt.expandPath(inPath);
			jt.makeVisible(inPath);
			jt.setSelectionPath(inPath);
			// jt.scrollPathToVisible(inPath);
		} else {
			jt.collapsePath(inPath);
		}

	}

	public void expandAll(TreePath inPath) {

		if (inPath == null) {
			return;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) inPath
				.getLastPathComponent();
		selectedObject = (SQLEDITree) node.getUserObject();
		for (int i = node.getChildCount() - 1; i > -1; i--) {

			DefaultMutableTreeNode tn = (DefaultMutableTreeNode) node
					.getChildAt(i);
			TreePath path = inPath.pathByAddingChild(tn);
			expandAll(path);

		}
		jt.expandPath(inPath);
		jt.makeVisible(inPath);
		jt.setSelectionPath(inPath);
		// jt.scrollPathToVisible(inPath);

	}

	/**
	 * mouseEntered handler
	 *
	 * @param me mouseevent
	 */
	@Override
	public void mouseEntered(java.awt.event.MouseEvent me) {
	}

	/**
	 * mousePressed handler
	 *
	 * @param me mouseevent
	 */
	@Override
	public void mousePressed(java.awt.event.MouseEvent me) {
	}

	/**
	 * mouseClicked handler
	 *
	 * @param me mouseevent
	 */
	@Override
	public void mouseClicked(java.awt.event.MouseEvent me) {
		if (selectedObject.isInUse() && (me.getClickCount() == 2)) {
			doItemWork();
		}

	}

	/**
	 * mouseExited handler
	 *
	 * @param me mouseevent
	 */
	@Override
	public void mouseExited(java.awt.event.MouseEvent me) {
	}

	/**
	 * mouseReleased handler
	 *
	 * @param me mouseevent
	 */
	@Override
	public void mouseReleased(java.awt.event.MouseEvent me) {
	}

	/**
	 * windowClosing handler
	 *
	 * @param we windowevent
	 */
	@Override
	public void windowClosing(WindowEvent we) {
		if (changed) {
			int resp = JOptionPane.showConfirmDialog(this,
					"Data has been changed.  Select YES to continue exit.",
					"Data Changed - Continue with exit?",
					JOptionPane.YES_NO_OPTION);

			if (resp == JOptionPane.NO_OPTION) {
				return;
			}
		}

		System.exit(0);
	}

	/**
	 * windowOpened handler
	 *
	 * @param we windowevent
	 */
	@Override
	public void windowOpened(java.awt.event.WindowEvent we) {
	}

	/**
	 * windowClosed handler
	 *
	 * @param we windowevent
	 */
	@Override
	public void windowClosed(java.awt.event.WindowEvent we) {
	}

	/**
	 * windowIconified handler
	 *
	 * @param we windowevent
	 */
	@Override
	public void windowIconified(java.awt.event.WindowEvent we) {
	}

	/**
	 * windowDeiconified handler
	 *
	 * @param we windowevent
	 */
	@Override
	public void windowDeiconified(java.awt.event.WindowEvent we) {
	}

	/**
	 * windowActivated handler
	 *
	 * @param we windowevent
	 */
	@Override
	public void windowActivated(java.awt.event.WindowEvent we) {
	}

	/**
	 * windowDeactivated handler
	 *
	 * @param we windowevent
	 */
	@Override
	public void windowDeactivated(java.awt.event.WindowEvent we) {
	}

	/**
	 * Tree valueChanged handler, used to reset tree objects
	 *
	 * @param tse treeselectionevent
	 */
	@Override
	public void valueChanged(TreeSelectionEvent tse) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) jt
				.getLastSelectedPathComponent();
		if (node == null) {
			return;
		}
		selectedObject = (SQLEDITree) node.getUserObject();
		RICEObject = selectedObject.getObject();
		RICEObjectParent = selectedObject.getParentObject();
		RICEObjectWorkingParent = selectedObject.getWorkingParentObject();
	}

	/**
	 * we know when the tree will be collapsed
	 *
	 * @param event treeexpansionevent
	 * @throws ExpandVetoException vetoable event
	 */

	@Override
	public void treeWillCollapse(TreeExpansionEvent event)
			throws ExpandVetoException {

		DefaultMutableTreeNode test = (DefaultMutableTreeNode) event.getPath()
				.getLastPathComponent();
		if (test == top) {
			throw new ExpandVetoException(event);
		}

		SQLEDITree selectedObject = (SQLEDITree) test.getUserObject();
		/*
		 * if (selectedObject.toString().startsWith("Table")) { throw new
		 * ExpandVetoException(event); } else if (selectedObject.getObject()
		 * instanceof TemplateLoop) { if (((TemplateLoop)
		 * selectedObject.getObject()).isRequired()) throw new
		 * ExpandVetoException(event); } else if (selectedObject.getObject()
		 * instanceof TemplateSegment) { if (((TemplateSegment)
		 * selectedObject.getObject()).isRequired()) throw new
		 * ExpandVetoException(event); } else if (selectedObject.getObject()
		 * instanceof TemplateComposite) { if (((TemplateComposite)
		 * selectedObject.getObject()).isRequired()) throw new
		 * ExpandVetoException(event); } else if (selectedObject.getObject()
		 * instanceof TemplateDE) { if (((TemplateDE)
		 * selectedObject.getObject()).isRequired()) throw new
		 * ExpandVetoException(event); }
		 */

		changed = true;
		selectedObject.setNotInUse();
	}

	/**
	 * we know when the tree will expand
	 *
	 * @param event treeexpansionevent
	 */

	@Override
	public void treeWillExpand(TreeExpansionEvent event)
			throws ExpandVetoException {

		DefaultMutableTreeNode test = (DefaultMutableTreeNode) event.getPath()
				.getLastPathComponent();
		SQLEDITree selectedObject = (SQLEDITree) test.getUserObject();
		selectedObject.setInUse();
		changed = true;
	}

	/**
	 * helper routine to call item edit screens from item.edit menu item or
	 * double clicking on tree item.
	 */

	public void doItemWork() {
		if (!selectedObject.isInUse()) {
			return;
		}

		if (RICEObject instanceof IDList) {

			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			TemplateDataElement tde = (TemplateDataElement) RICEObjectWorkingParent;

			IDList idlist = (IDList) RICEObject;
			IDListEditor tid;
			try {
				tid = new IDListEditor(this, idlist, tde);
			} catch (Exception e) {
				logr.error(e.getMessage(), e);
				return;
			}
			tid.setVisible(true);
		}

	}

	/**
	 * gets the idlist object data for displayed
	 *
	 * @return IDList IDlist object
	 * @param inTC   TemplateComposite
	 * @param idName IDList name
	 */
	public IDList getIDListFromComposite(TemplateCompositeElement inTC,
			String idName) {
		IDList retIDList = null, testIDList;
		TemplateDataElement tde;
		int i;
		for (i = 0; (i < inTC.getContainerSize()) && (retIDList == null); i++) {
			tde = (TemplateDataElement) inTC.getTemplateElement(i + 1);
			if (tde == null) {
				continue;
			}
			testIDList = (IDList) tde.getIDList();
			if (testIDList == null) {
				continue;
			}
			if (testIDList.getShortName().compareTo(idName) == 0) {
				retIDList = testIDList;
			}
		}
		return retIDList;
	}

	/**
	 * gets the idlist object data for displayed
	 *
	 * @return IDList IDlist object
	 * @param inSeg  TemplateSegment
	 * @param idName idlist name
	 */
	public IDList getIDListFromSegment(TemplateSegment inSeg, String idName) {
		IDList retIDList = null, testIDList;
		TemplateCompositeElement tc;
		TemplateDataElement tde;
		int i;
		for (i = 0; (i < inSeg.getContainerSize())
				&& (retIDList == null); i++) {
			if (inSeg.isTemplateComposite(i + 1)) {
				tc = (TemplateCompositeElement) inSeg.getTemplateElement(i + 1);
				retIDList = getIDListFromComposite(tc, idName);
			} else if (inSeg.isTemplateDE(i + 1)) {
				tde = (TemplateDataElement) inSeg.getTemplateElement(i + 1);
				testIDList = (IDList) tde.getIDList();
				if (testIDList == null) {
					continue;
				}
				if (testIDList.getShortName().compareTo(idName) == 0) {
					retIDList = testIDList;
				}
			}
		}
		if (retIDList != null) {
			return retIDList;
		}

		return retIDList;
	}

	/**
	 * go through the tree structure to save the message description file
	 *
	 * @param writer Writer
	 * @param delete ignore deleted
	 * @throws IOException
	 */
	public void saveRulesFile(Writer writer, boolean delete)
			throws IOException {

		TemplateTable t = null;
		headerDone = false;

		DefaultMutableTreeNode tn = (DefaultMutableTreeNode) jt.getModel()
				.getRoot();

		for (int i = 0; i < tn.getChildCount(); i++) {
			DefaultMutableTreeNode tnTable = (DefaultMutableTreeNode) tn
					.getChildAt(i);
			SQLEDITree userObject = (SQLEDITree) tnTable.getUserObject();
			t = (TemplateTable) userObject.getObject();

			StringBuilder prebuffer$ = new StringBuilder();
			prebuffer$.append("\n");
			prebuffer$.append(addDepth(1));
			prebuffer$.append("<table section=\"");
			prebuffer$.append(t.getShortName() + "\"");
			if (t.getValidatingMethod() != null) {
				prebuffer$.append("\n");
				prebuffer$.append(addDepth(1));
				prebuffer$.append("  validatingMethod=\"");
				prebuffer$.append(t.getValidatingMethod());
				prebuffer$.append("\"");
			}
			prebuffer$.append(">");
			preBuffer.add(new String(prebuffer$));
			int preCount = preBuffer.size();

			for (int j = 0; j < tnTable.getChildCount(); j++) {
				DefaultMutableTreeNode tnSeg = (DefaultMutableTreeNode) tnTable
						.getChildAt(j);
				SQLEDITree testObject = (SQLEDITree) tnSeg.getUserObject();
				if (testObject.getObject() instanceof TemplateSegment) {
					writeSegment(writer,
							(TemplateSegment) testObject.getObject(), 1, tnSeg,
							delete);
				} else {
					writeLoop(writer, (TemplateLoop) testObject.getObject(), 1,
							tnSeg, delete);
				}
			}

			if (preCount == preBuffer.size()) {
				preBuffer.remove(preCount - 1);
			} else {
				writer.write(Util.lineFeed + addDepth(1) + "</table>");
			}

		}

		if (headerDone == true) {
			writer.write("\n" + "</transactionSet>");
			setChanged(false);
		} else {
			JOptionPane.showMessageDialog(this,
					"No data elements selected for building XML file.  File not saved.",
					"No Data To Generate", JOptionPane.ERROR_MESSAGE);
		}

	}

	/**
	 * helper routine to setup xml header
	 */
	private void writeHeader(Writer writer) {
		if (headerDone) {
			return;
		}
		headerDone = true;
		try {

			writer.write("<?xml version=\"1.0\"?>");

			writer.write("\n" + "<transactionSet name=\""
					+ Util.normalize(tsWork.getName()) + "\"");
			writer.write(
					"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
							+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			writer.write(
					"  xsi:noNamespaceSchemaLocation=\"https://raw.githubusercontent.com/EDIandXML/OBOE/main/EDIRules.xsd\""
							+ io.github.EDIandXML.OBOE.util.Util.lineFeed);

			writer.write("  id=\"" + tsWork.getID() + "\"");
			writer.write("\n" + "  revision=\"" + tsWork.getRevision() + "\"");
			writer.write("\n" + "  functionalGroup=\""
					+ tsWork.getFunctionalGroup() + "\"");
			writer.write("\n" + "  description=\""
					+ Util.normalize(tsWork.getShortDescription()) + "\"");
			if (tsWork.getValidatingMethod() != null) {
				writer.write("\n" + "  validatingMethod=\""
						+ tsWork.getValidatingMethod() + "\"");
			}
			writer.write(
					"\n" + "  shortName=\"" + tsWork.getShortName() + "\">");
		} catch (Exception e1) {
			logr.error(e1.getMessage(), e1);

			JOptionPane.showMessageDialog(this,
					e1.getMessage() + " See stack trace in log file",
					"Exception Occur", JOptionPane.ERROR_MESSAGE);
		}

	}

	/**
	 * helper tourine to write buffered print lines.
	 *
	 * @throws IOException
	 */
	private void writeWork(Writer writer, String inString) throws IOException {
		writeHeader(writer);
		for (int i = 0; i < preBuffer.size(); i++) {
			writer.write(preBuffer.get(i));
		}
		writer.write(inString);
		preBuffer = new ArrayList<String>();
	}

	/**
	 * helper routine to format xml text
	 */
	private String addDepth(int depth) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < depth; i++) {
			sb.append("    ");
		}
		return sb.toString();
	}

	boolean doingST = false;

	/**
	 * validates segment, calls checkComposite and checkDE
	 *
	 * @param writer Writer object
	 * @param tLoop  TemplateLoop to check
	 * @param depth  int depth for printing
	 * @param inNode tree node of this object
	 * @param delete ignore deleted
	 * @return boolean true indicates it's okay
	 * @throws IOException
	 */
	public boolean writeLoop(Writer writer, TemplateLoop tLoop, int depth,
			javax.swing.tree.DefaultMutableTreeNode inNode, boolean delete)
			throws IOException {

		SQLEDITree userObject = (SQLEDITree) inNode.getUserObject();
		String usedText = "";

		if (userObject.displayed == false) {
			if (delete == true) {
				return true;
			} else {
				usedText = " used=\'N\'";
			}
		}

		depth++;

		int i;

		StringBuilder sb1 = new StringBuilder("\n" + addDepth(depth)
				+ "<loop name=\"" + tLoop.getName() + "\" id=\""
				+ Util.normalize(tLoop.getID()) + "\"" + "\n" + addDepth(depth)
				+ "  occurs=\"" + tLoop.getOccurs() + "\"" + "\n"
				+ addDepth(depth) + "  required='" + tLoop.getRequired() + "'"
				+ usedText);

		if (tLoop.getValidatingMethod() != null) {
			sb1.append("\n" + addDepth(depth) + "  validatingMethod=\""
					+ tLoop.getValidatingMethod() + "\"");
		}
		sb1.append("\n" + addDepth(depth) + "  shortName=\""
				+ Util.normalize(tLoop.getShortName()) + "\">");

		preBuffer.add(new String(sb1));

		int preCount = preBuffer.size();

		for (i = 0; i < inNode.getChildCount(); i++) {

			DefaultMutableTreeNode tn = (DefaultMutableTreeNode) inNode
					.getChildAt(i);
			userObject = (SQLEDITree) tn.getUserObject();

			if (userObject.getObject() instanceof TemplateSegment) {
				writeSegment(writer, (TemplateSegment) userObject.getObject(),
						depth, tn, delete);
			} else if (userObject.getObject() instanceof TemplateLoop) {
				writeLoop(writer, (TemplateLoop) userObject.getObject(), depth,
						tn, delete);
			}

		}
		if (preCount == preBuffer.size()) {
			preBuffer.remove(preCount - 1);
			return false;
		}

		writeWork(writer, Util.lineFeed + addDepth(depth) + "</loop>");
		return true;

	}

	/**
	 * validates segment, calls checkComposite and checkDE
	 *
	 * @param writer Writer object
	 * @param tSeg   TemplateSegment to check
	 * @param depth  int depth for printing
	 * @param inNode tree node of this object
	 * @param delete ignore deletedd
	 * @return boolean true indicates it's okay
	 * @throws IOException
	 */
	public boolean writeSegment(Writer writer, TemplateSegment tSeg, int depth,
			javax.swing.tree.DefaultMutableTreeNode inNode, boolean delete)
			throws IOException {

		SQLEDITree userObject = (SQLEDITree) inNode.getUserObject();

		String usedText = "";
		if (userObject.displayed == false) {
			if (delete) {
				return true;
			} else {
				usedText = " used=\'N\'";
			}
		}

		depth++;

		int i;

		doingST = tSeg.getID().equalsIgnoreCase("ST");

		StringBuilder sb1 = new StringBuilder(Util.lineFeed + addDepth(depth)
				+ "<segment name=\"" + tSeg.getName() + "\" id=\""
				+ Util.normalize(tSeg.getID()) + "\"" + Util.lineFeed
				+ addDepth(depth) + "  description=\""
				+ Util.normalize(tSeg.getDescription()) + "\"" + Util.lineFeed
				+ addDepth(depth) + "  sequence=\"" + tSeg.getPosition() + "\""
				+ Util.lineFeed + addDepth(depth) + "  occurs=\""
				+ tSeg.getOccurs() + "\"" + Util.lineFeed + addDepth(depth)
				+ "  required='" + tSeg.getRequired() + "'" + usedText);

		if (tSeg.getValidatingMethod() != null) {
			sb1.append(Util.lineFeed + addDepth(depth) + "  validatingMethod=\""
					+ tSeg.getValidatingMethod() + "\"");
		}
		sb1.append(Util.lineFeed + addDepth(depth) + "  shortName=\""
		// +
		// Util.normalize(outXML3.makeXMLName(tSeg.getName(),
		// true))
				+ Util.normalize(tSeg.getShortName()) + "\">");

		preBuffer.add(new String(sb1));

		int preCount = preBuffer.size();

		int elementUsed[] = new int[inNode.getChildCount()];
		for (i = 0; i < inNode.getChildCount(); i++) {

			DefaultMutableTreeNode tn = (DefaultMutableTreeNode) inNode
					.getChildAt(i);
			userObject = (SQLEDITree) tn.getUserObject();

			if (userObject.getObject() instanceof TemplateCompositeElement) {
				elementUsed[i] = writeComposite(writer,
						(TemplateCompositeElement) userObject.getObject(),
						depth, tn, delete);
			} else if (userObject.getObject() instanceof TemplateDataElement) {
				elementUsed[i] = writeDE(writer,
						(TemplateDataElement) userObject.getObject(), depth, tn,
						delete);
			}

		}

		if (TransactionSetFactory.typeSet == -10) {
			ArrayList<ElementRules> rules = tSeg.getElementRules();
			ElementRules er;
			int j, k;

			ruleLoop: for (i = 0; i < rules.size(); i++) {
				er = rules.get(i);
				for (j = 0; j < er.getPositionCount(); j++) {
					{
						for (k = 0; k < elementUsed.length; k++) {
							if (elementUsed[k] == er.getPosition(j + 1)) {
								break;
							}
						}

						if (k == elementUsed.length) {
							continue ruleLoop;
							// one or more of this rule's elements are not used
						}
					}
				}
				writeWork(writer, Util.lineFeed + addDepth(depth + 1));
				writeWork(writer, " <elementRule rule=\"" + er.getRuleText());
				writeWork(writer, "\" positions=\"" + er.getRulePositions());
				writeWork(writer, "\"/>");
			}
		}

		if (preCount == preBuffer.size()) {
			preBuffer.remove(preCount - 1);
			return false;
		}

		writeWork(writer, Util.lineFeed + addDepth(depth) + "</segment>");
		return true;

	}

	/**
	 * validates Composite, calls checkDE
	 *
	 * @param writer Writer object
	 * @param tc     TemplateComposite to check
	 * @param depth  int for printing
	 * @param inNode tree node for this object
	 * @param delete ignore deleted
	 * @return int >0 indicates it's okay, the value is its seq number.
	 * @throws IOException
	 */
	public int writeComposite(Writer writer, TemplateCompositeElement tc,
			int depth, javax.swing.tree.DefaultMutableTreeNode inNode,
			boolean delete) throws IOException {
		SQLEDITree userObject = (SQLEDITree) inNode.getUserObject();

		String usedText = "";
		if (userObject.displayed == false) {
			if (delete) {
				return 1;
			} else {
				usedText = " used=\'N\'";
			}
		}

		depth++;

		StringBuilder sb1 = new StringBuilder();

		sb1.append(Util.lineFeed + addDepth(depth) + "<compositeDE name=\""
				+ Util.normalize(tc.getName()) + "\" id=\""
				+ Util.normalize(tc.getID()) + "\"" + Util.lineFeed
				+ addDepth(depth) + "  sequence=\"" + tc.getPosition() + "\""
				+ Util.lineFeed + addDepth(depth) + "  required=\'"
				+ tc.getRequired() + "\'" + usedText);

		if (tc.getValidatingMethod() != null) {
			sb1.append(Util.lineFeed + addDepth(depth) + "  validatingMethod=\""
					+ tc.getValidatingMethod() + "\"");
		}

		sb1.append(Util.lineFeed + addDepth(depth) + "  shortName=\""
		// +
		// Util.normalize(outXML3.makeXMLName(tc.getName(),
		// false))
				+ tc.getShortName() + "\">");

		preBuffer.add(new String(sb1));
		int preCount = preBuffer.size();

		int i;

		for (i = 0; i < inNode.getChildCount(); i++) {

			DefaultMutableTreeNode tn = (DefaultMutableTreeNode) inNode
					.getChildAt(i);
			userObject = (SQLEDITree) tn.getUserObject();

			if (userObject.getObject() instanceof TemplateDataElement) {
				writeDE(writer, (TemplateDataElement) userObject.getObject(),
						depth, tn, delete);
			}

		}
		if (preCount == preBuffer.size()) {
			preBuffer.remove(preCount - 1);
			return -1;
		}

		writeWork(writer, Util.lineFeed + addDepth(depth) + "</compositeDE>");
		return tc.getPosition();
	}

	/**
	 * validates DataElement
	 *
	 * @param writer Writer object
	 * @param tde    TemplateDE to check
	 * @param depth  int depth for printing
	 * @param delete ignore deleted
	 * @return int >0 indicates it's okay, the value is its seq number.
	 * @throws IOException
	 */
	public int writeDE(Writer writer, TemplateDataElement tde, int depth,
			javax.swing.tree.DefaultMutableTreeNode inNode, boolean delete)
			throws IOException {
		SQLEDITree userObject = (SQLEDITree) inNode.getUserObject();

		String usedText = "";
		if (userObject.displayed == false) {
			if (delete) {
				return 1;
			} else {
				usedText = " used=\'N\'";
			}
		}

		depth++;

		IDListProcessor idl = tde.getIDList();
		if ((idl == null) && (tde.getType().equals("ID"))) {
			tde.setType("AN");
		} else if ((idl != null) && (idl.getCodes() != null)
				&& (idl.getCodes().size() != 0)
				&& (tde.getType().equals("ID") == false)) {
			tde.setType("ID");
		}

		writeWork(writer, Util.lineFeed + addDepth(depth) + "<dataElement ");
		writeWork(writer, "name=\"" + tde.getName() + "\" id=\""
				+ Util.normalize(tde.getID()) + "\"");
		writeWork(writer, Util.lineFeed + addDepth(depth) + "  sequence=\""
				+ tde.getPosition() + "\"");
		writeWork(writer, Util.lineFeed + addDepth(depth) + "  description=\""
				+ Util.normalize(tde.getDescription()) + "\"");
		if (tde.getOccurs() != 1) {
			writeWork(writer, Util.lineFeed + addDepth(depth) + "  occurs=\""
					+ tde.getOccurs() + "\"");
		}
		writeWork(writer, Util.lineFeed + addDepth(depth) + "  type=\""
				+ tde.getType() + "\" required=\"" + tde.getRequired() + "\"");
		writeWork(writer,
				Util.lineFeed + addDepth(depth) + "  minLength=\""
						+ tde.getMinLength() + "\" maxLength=\""
						+ tde.getMaxLength() + "\"" + usedText);

		if (tde.getValidatingMethod() != null) {
			writeWork(writer,
					Util.lineFeed + addDepth(depth) + "  validatingMethod=\""
							+ tde.getValidatingMethod() + "\"");
		}

		writeWork(writer, Util.lineFeed + addDepth(depth) + "  shortName=\""
		// +
		// Util.normalize(outXML3.makeXMLName(tde.getName(),
		// false))
				+ Util.normalize(tde.getShortName()) + "\"");
		writeWork(writer, ">");
		if (doingST && (tde.getID().compareTo("143") == 0)) {
		} else if ((idl == null) || (tde.getType().equals("ID") == false)) {
		} else {
			if (idl instanceof IDList) {
				writeIDCodes(writer, depth, inNode, delete);
			} else {
				// must be a class file
				writeWork(writer,
						Util.lineFeed + addDepth(depth + 1)
								+ "<idListClass className=\""
								+ idl.getClass().getName() + "\"/>");
			}
		}

		switch (tde.defaultFromWhere()) {
		case 'C':
			writeWork(writer,
					Util.lineFeed + addDepth(depth)
							+ "  <default from=\"constant\">"
							+ tde.getDefaultKey() + "</default>");
			break;
		case 'P':
			writeWork(writer,
					Util.lineFeed + addDepth(depth)
							+ "  <default from=\"property\">"
							+ tde.getDefaultKey() + "</default>");
			break;
		case 'M':
			writeWork(writer,
					Util.lineFeed + addDepth(depth)
							+ "  <default from=\"method\">"
							+ tde.getDefaultKey() + "</default>");
			break;
		default:
			break;
		} // switch

		writeWork(writer, Util.lineFeed + addDepth(depth) + " </dataElement>");
		return tde.getPosition();
	}

	/**
	 * used for IDDE templates to build automatic default values
	 *
	 * @param writer Wtiter object
	 * @param depth  for printing
	 * @param inNode tree node
	 * @throws IOException
	 */
	public void writeDefaultAutomatically(Writer writer, int depth,
			DefaultMutableTreeNode inNode) throws IOException {

		selectedObject = (SQLEDITree) inNode.getUserObject();
		TemplateDataElement tde = (TemplateDataElement) selectedObject
				.getObject();

		IDList idl;

		if (tde == null) {
			return;
		}

		idl = (IDList) tde.getIDList();

		ArrayList<String> codes = idl.getCodes();
		if (codes.size() != 1) {
			return;
		}

		writeWork(writer,
				Util.lineFeed + addDepth(depth)
						+ "  <default from=\"constant\">" + codes.get(0)
						+ "</default>");

	}

	/**
	 * used for IDDE templates to get the stored name/value pair
	 *
	 * @param writer Writer object
	 * @param depth  for printing
	 * @param inNode TreeNode
	 * @param delete unused codes
	 * @throws IOException
	 */
	public void writeIDCodes(Writer writer, int depth,
			DefaultMutableTreeNode inNode, boolean delete) throws IOException {

		selectedObject = (SQLEDITree) inNode.getUserObject();
		TemplateDataElement tde = (TemplateDataElement) selectedObject
				.getObject();

		IDList idl;

		if (tde == null) {
			return;
		}

		idl = (IDList) tde.getIDList();
		if (delete) { // user doesn't want deleted id list elements
			writeWork(writer, Util.lineFeed + addDepth(depth) + "  <idList>");
			ArrayList<String> codes = idl.getCodes();
			ArrayList<String> values = idl.getValues();
			for (int r = 0; r < codes.size(); r++) {
				writeWork(writer,
						Util.lineFeed + addDepth(depth + 1) + "    <idCode>"
								+ Util.normalize(codes.get(r)) + "</idCode>");
				writeWork(writer,
						Util.lineFeed + addDepth(depth + 1)
								+ "        <idValue>"
								+ Util.normalize(values.get(r)) + "</idValue>");
			}

			writeWork(writer, Util.lineFeed + addDepth(depth) + "  </idList>");
		} else {
			if ((idl.getShortName() == null)
					|| (idl.getShortName().length() == 0)) {
				writeWork(writer,
						Util.lineFeed + addDepth(depth) + "  <idList>");
				ArrayList<String> codes = idl.getCodes();
				ArrayList<String> values = idl.getValues();
				for (int r = 0; r < codes.size(); r++) {
					writeWork(writer,
							Util.lineFeed + addDepth(depth + 1) + "    <idCode>"
									+ Util.normalize(codes.get(r))
									+ "</idCode>");
					writeWork(writer, Util.lineFeed + addDepth(depth + 1)
							+ "        <idValue>"
							+ Util.normalize(values.get(r)) + "</idValue>");
				}

				writeWork(writer,
						Util.lineFeed + addDepth(depth) + "  </idList>");
			}

			else if (idl.isFiltered() == false) {
				writeWork(writer,
						Util.lineFeed + addDepth(depth)
								+ "  <idListFile fileName=\""
								+ idl.getShortName() + "\"/>");

				writeListToFile(idl);

			} else {
				writeWork(writer, Util.lineFeed + addDepth(depth)
						+ "  <idListFile fileName=\"" + idl.getShortName());
				writeWork(writer, "\" " + idl.getFilterList());
				writeWork(writer, "/>");

				writeListToFile(idl);

			}
		}

		return;
	}

	private void writeListToFile(IDList idl) throws IOException {
		PrintWriter idWriter = new PrintWriter(filename);

		idWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		idWriter.println("<idList");
		idWriter.println(
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		idWriter.println(
				"  xsi:noNamespaceSchemaLocation=\"https://raw.githubusercontent.com/EDIandXML/OBOE/main/EDIRules.xsd\">");
		ArrayList<String> codes = idl.getCodes();
		ArrayList<String> values = idl.getValues();
		for (int r = 0; r < codes.size(); r++) {
			idWriter.println("    <idCode>" + Util.normalize(codes.get(r))
					+ "</idCode>");
			idWriter.println("        <idValue>" + Util.normalize(values.get(r))
					+ "</idValue>");
		}
		idWriter.println("</idList>");
		idWriter.flush();
		idWriter.close();
	}

	/**
	 * used to pull idList object from the TemplateTable
	 *
	 * @return IDList IDlist object
	 * @param inTbl  TemplateTable
	 * @param idName idlist name
	 */
	public IDList getIDListFromTable(TemplateTable inTbl, String idName) {
		if (inTbl == null) {
			return null;
		}
		IDList retIDList = null;
		TemplateSegment tSeg;
		for (var elm : inTbl.getContainer()) {
			if (elm.getContainerType() == ContainerType.Segment) {
				tSeg = (TemplateSegment) elm;
				retIDList = getIDListFromSegment(tSeg, idName);
				if (retIDList != null) {
					break;
				}
			}
		}

		return retIDList;
	}

	/**
	 * sets idList object from templateDE object
	 *
	 * @param inTDE     TemplateDE for display
	 * @param inTDESave TemplateDE being worked on
	 * @exception OBOEException exception thrown
	 */
	public void setIDList(TemplateDataElement inTDE,
			TemplateDataElement inTDESave) throws OBOEException {
		if (inTDE == null) {
			throw new OBOEException("logic error missng in templateDE");
		}

		if (inTDE.getIDList() != null) {
			return; // already has one
		}

		String sName = ((IDList) inTDESave.getIDList()).getShortName();

		int i;
		IDList idl;
		// drill down through working ts to find an element using this idlist

		idl = getIDListFromTable(tsWork.getHeaderTemplateTable(), sName);
		if (idl == null) {
			idl = getIDListFromTable(tsWork.getDetailTemplateTable(), sName);
			if (idl == null) {
				idl = getIDListFromTable(tsWork.getSummaryTemplateTable(),
						sName);
			}
		}

		if (idl != null) {
			inTDE.setIDList(idl);
			return;
		}

		for (i = 0; i < idListsWork.size(); i++) {
			idl = (IDList) idListsWork.get(i);
			if (idl.getShortName().compareTo(sName) == 0) {
				inTDE.setIDList(idl);
				return;
			}
		}
		idl = new IDList(sName);
		if (idl != null) {
			inTDE.setIDList(idl);
		}
	}

	/**
	 * usage: java TransactionSetMessageEditor [inID] where inID is optional
	 * transaction set rule id.
	 *
	 * @param arg String array
	 */
	public static void main(String arg[]) {
		if (arg.length == 0) {
			new TransactionSetMessageEditor(null);
		} else if (arg.length == 1) {
			new TransactionSetMessageEditor(arg[0]);
		} else {
			logr.error("  usage: TransactionSetMessageEditorr [inID]");
			logr.error("    where inID is optional starting rule id.");
		}
	}

	/**
	 * sets the changed attribute
	 *
	 * @param inB - boolean
	 */
	public void setChanged(boolean inB) {
		changed = inB;
	}

	static class SQLEDITree {
		Object owningObject, parentObject = null, workingParentObject = null;
		String saveText;
		boolean displayed = true;

		/**
		 * Constructor for private SQLEDITree class, used for tree work
		 *
		 * @param inText   String text assigned to object
		 * @param inObject Object assigned to tree node
		 */
		protected SQLEDITree(String inText, Object inObject) {
			owningObject = inObject;
			saveText = inText;
		}

		/**
		 * Constructor for private SQLEDITree class, used for tree work
		 *
		 * @param inText   String assigned to object
		 * @param inDisp   in or not in use
		 * @param inObject Object assigned to tree
		 */
		protected SQLEDITree(String inText, boolean inDisp, Object inObject) {
			owningObject = inObject;
			saveText = inText;
			displayed = inDisp;
		}

		/**
		 * Constructor for private SQLEDITree class, used for tree work
		 *
		 * @param inText          String of object
		 * @param inDisp          boolean in use or not in use
		 * @param inObject        object on node
		 * @param inWorkingParent object's working parent
		 */
		protected SQLEDITree(String inText, boolean inDisp, Object inObject,
				Object inWorkingParent) {
			workingParentObject = inWorkingParent;
			owningObject = inObject;
			saveText = inText;
			displayed = inDisp;
		}

		/**
		 * get the object at the node
		 *
		 * @return object at node
		 */
		public Object getObject() {
			return owningObject;
		}

		/**
		 * get the object's parent object
		 *
		 * @return Object
		 */
		public Object getParentObject() {
			return parentObject;
		}

		/**
		 * get the object's working parent object
		 *
		 * @return object
		 */
		public Object getWorkingParentObject() {
			return workingParentObject;
		}

		/**
		 * get the text associated with the object
		 *
		 * @return String
		 */
		@Override
		public String toString() {
			return saveText;
		}

		public void setInUse() {
			displayed = true;
		}

		public void setNotInUse() {
			displayed = false;
		}

		public boolean isInUse() {
			return displayed;
		}
	}

	static class FileFilter extends javax.swing.filechooser.FileFilter {

		String ext = "";

		public FileFilter(String inExt) {
			ext = "." + inExt.toLowerCase();
		}

		@Override
		public String getDescription() {
			if (ext.compareToIgnoreCase(".xml") == 0) {
				return "Files with names that end with the " + ext
						+ " extension.";
			} else {
				return "Files with names that end with the " + ext
						+ " extension.";
			}
		}

		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}

			String name = f.getName().toLowerCase();

			return name.endsWith(ext);

		}
	}

	/**
	 * look in OBOE.properties file for the folder containing the original
	 * transaction message files.
	 *
	 * @return String
	 */

	public static String getBaseDirectory() {
		try {
			final String getName = "baseDirectory";
			return Util.getOBOEProperty(getName);
		} catch (final FileNotFoundException fnfe) {
			logr.error("OBOE.properties file not found");
			logr.fatal(fnfe.getMessage(), fnfe);
			System.exit(0);
		} catch (final IOException ioe) {
			logr.error("Error while reading OBOE.properties file.");
			logr.fatal(ioe.getMessage(), ioe);
			System.exit(0);
		}

		return null;
	}

}
