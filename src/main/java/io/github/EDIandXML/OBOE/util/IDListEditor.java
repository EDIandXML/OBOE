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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.DataElements.IDList;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;

/**
 * simple id list editor
 *
 * 
 * OBOE - Open Business Objects for EDI
 * 
 *
 * @author Joe McVerry
 * 
 */

public class IDListEditor extends JFrame
		implements ActionListener, WindowListener, ChangeListener {

	private static final long serialVersionUID = 1L;
	ArrayList<String> names;
	String savePath;
	String filename;

	JTable table;
	JMenuBar menuBar;
	JMenu menuFile;
	JMenuItem menuItem;
	JScrollPane tableScrollPane;
	TemplateDataElement workingDE;

	Coder_IDViewer idViewer;
	TransactionSetMessageEditor parent;
	IDList idl;

	String filterList = "";
	boolean include = true;

	static Logger logr = LogManager.getLogger(IDListEditor.class.getName());

	/**
	 * Constructor
	 *
	 * @param inParent Parent frame
	 * @param edited   TemplateDE to be edited
	 */
	public IDListEditor(TransactionSetMessageEditor inParent, IDList idList,
			TemplateDataElement edited) throws Exception {

		super(edited.getName());
		idl = idList;

		parent = inParent;
		workingDE = edited;

		idViewer = new Coder_IDViewer(idl, (IDList) edited.getIDList());

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		menuFile = new JMenu("File");
		menuBar.add(menuFile);

		menuItem = new JMenuItem("Save");
		menuItem.setActionCommand("save");
		menuItem.addActionListener(this);
		menuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		menuFile.add(menuItem);

		menuItem = new JMenuItem("Save And Exit");
		menuItem.setActionCommand("sexit");
		menuItem.addActionListener(this);
		menuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		menuFile.add(menuItem);

		menuItem = new JMenuItem("Exit");
		menuItem.setActionCommand("exit");
		menuItem.addActionListener(this);
		menuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		menuFile.add(menuItem);

		menuFile = new JMenu("Select");
		menuBar.add(menuFile);

		menuItem = new JMenuItem("Select all");
		menuItem.setActionCommand("all");
		menuItem.addActionListener(this);
		menuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		menuFile.add(menuItem);

		menuItem = new JMenuItem("Deselect all");
		menuItem.setActionCommand("none");
		menuItem.addActionListener(this);
		menuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
		menuFile.add(menuItem);

		table = new JTable(idViewer);
		// table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableScrollPane = new JScrollPane(table);
		tableScrollPane.setPreferredSize(new Dimension(250, 120));
		tableScrollPane.setMinimumSize(new Dimension(250, 120));
		tableScrollPane.setAlignmentX(LEFT_ALIGNMENT);

		// getContentPane().setLayout(new GridLayout(2, 0));
		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));

		listPane.add(tableScrollPane);
		listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel cbPane = new JPanel();
		cbPane.setLayout(new BoxLayout(cbPane, BoxLayout.X_AXIS));
		cbPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		cbPane.add(Box.createHorizontalGlue());

		getContentPane().add(listPane, BorderLayout.CENTER);
		getContentPane().add(cbPane, BorderLayout.SOUTH);

		addWindowListener(this);

		pack();
		setSize(new Dimension(550, 400));

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	/**
	 * handle to some action event
	 *
	 * @param ae ActionEvent object
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		String command = ae.getActionCommand();
		if (command.equals("save")) {
			save();
		}
		if (command.equals("sexit")) {
			boolean ret = save();
			parent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			if (ret == true) {
				setVisible(false);
			}
		}
		if (command.equals("exit")) {
			if (idViewer.getChanged()) {
				int resp = JOptionPane.showConfirmDialog(this,
						"Data has been changed.  Do you want to continue?.",
						"Data Changed - Continue with exit?",
						JOptionPane.YES_NO_OPTION);

				if (resp == JOptionPane.NO_OPTION) {
					return;
				}

			}

			parent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			setVisible(false);
		}
		if (command.equals("all")) {
			idViewer.setAll();
		}
		if (command.equals("none")) {
			idViewer.unsetAll();
		}
	}

	/**
	 * Save the object from components shown
	 */
	public boolean save() {
		Boolean b;
		IDList idlsave = (IDList) workingDE.getIDList();

		ArrayList<String> v = idl.getCodes();
		int set = 0;

		for (int i = 0; i < idViewer.getRowCount(); i++) {
			b = (Boolean) idViewer.getValueAt(i, 0);
			if (b.booleanValue()) {
				set++;
			}
		}

		if (set == 0) {
			JOptionPane.showMessageDialog(this,
					"It is not logical to remove all of the IDList codes and to use the Data Element object.  Either add at least one code or mark the data element as not used.",
					"Must Have At Least One Code", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		boolean equal = (v.size() == set);

		((IDList) workingDE.getIDList())
				.setName("IDList" + workingDE.getID() + ".xml");

		if (equal) {
			((IDList) workingDE.getIDList()).setFiltered(false);
			return true;
		}

		include = ((v.size() / 2) > set);
		StringBuilder sb = new StringBuilder();
		if (include) {
			sb.append("include=\"");
		} else {
			sb.append("exclude=\"");
		}

		((IDList) workingDE.getIDList()).setFiltered(true);

		try {
			idlsave = new IDList();
			idlsave.setFiltered(true);
			idlsave.setName("IDList" + workingDE.getID() + ".xml");
			workingDE.setIDList(idlsave);
			boolean started = true;
			StringBuilder sb2 = new StringBuilder();
			String lastV = "";
			for (int r = 0; r < idViewer.getRowCount(); r++) {
				b = (Boolean) idViewer.getValueAt(r, 0);
				if (b.booleanValue()) {
					idlsave.add((String) idViewer.getValueAt(r, 1),
							(String) idViewer.getValueAt(r, 2));
				}
				if (b.booleanValue() == include) {
					if (sb2.length() == 0) {
						if (!started) {
							sb.append(',');
						}
						sb2.append((String) idViewer.getValueAt(r, 1));
						started = false;
					}
					lastV = (String) idViewer.getValueAt(r, 1);
				} else {
					if (sb2.length() == 0) {
						continue;
					}
					String sb2str = sb2.toString();
					if (Util.isInteger(sb2str) && Util.isInteger(lastV)
							&& (Integer.parseInt(sb2str) < Integer
									.parseInt(lastV))) {
						sb2.append('-' + lastV);

					} else if (sb2str.compareTo(lastV) < 0) {
						sb2.append('-' + lastV);
					}
					sb.append(sb2.toString());
					sb2 = new StringBuilder();
				}
			}
			if (sb2.length() > 0) {
				if (sb2.toString().compareTo(lastV) < 0) {
					sb2.append('-' + lastV);
				}
			}
			sb.append(sb2.toString() + '"');
			idlsave.setFilterList(sb.toString());

		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Unknown Java Excetion - see stack trace in console window for "
							+ e1.getMessage(),
					e1.getMessage(), JOptionPane.ERROR_MESSAGE);

			return true;
		} finally {
			parent.setChanged(true);
			idViewer.resetTBoolean();
		}
		return true;
	}

	/**
	 * used to test if component change, let user know
	 *
	 * @param we windowevent.
	 */

	@Override
	public void windowClosing(WindowEvent we) {
		if (idViewer.getChanged()) {
			int resp = JOptionPane.showConfirmDialog(this,
					"Data has been changed.  Do you want to continue?.",
					"Data Changed - Continue with exit?",
					JOptionPane.YES_NO_OPTION);

			if (resp == JOptionPane.NO_OPTION) {
				return;
			}
		}

		parent.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		setVisible(false);
		parent.repaint();

	}

	/**
	 * Not used
	 *
	 * @param we window event
	 */
	@Override
	public void windowOpened(java.awt.event.WindowEvent we) {
	}

	/**
	 * Not used
	 *
	 * @param we window event
	 */
	@Override
	public void windowClosed(java.awt.event.WindowEvent we) {
	}

	/**
	 * Not used
	 *
	 * @param we window event
	 */
	/**
	 * Not used
	 *
	 * @param we window event
	 */
	@Override
	public void windowIconified(java.awt.event.WindowEvent we) {
	}

	/**
	 * Not used
	 *
	 * @param we window event
	 */
	@Override
	public void windowDeiconified(java.awt.event.WindowEvent we) {
	}

	/**
	 * Not used
	 *
	 * @param we window event
	 */
	@Override
	public void windowActivated(java.awt.event.WindowEvent we) {
	}

	/**
	 * Not used
	 *
	 * @param we window event
	 */
	@Override
	public void windowDeactivated(java.awt.event.WindowEvent we) {
	}

	@Override
	public void stateChanged(javax.swing.event.ChangeEvent ce) {

	}

	/**
	 * table model to view idlists in a table
	 */
	static class Coder_IDViewer extends AbstractTableModel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		ArrayList<String> code;
		ArrayList<String> value;
		ArrayList<String> editCode;
		Boolean aBoolean[], tBoolean[];
		int inRow;
		String columnName[] = { "Selected", "ID Code", "Description" };

		int columnCount = 3;
		int rowCount = 0;

		/**
		 * Constructor for TableModel
		 *
		 * @param idListSave IDList to be displayed against
		 * @param idListEdit IDList to be workedon
		 */
		public Coder_IDViewer(IDList idListSave, IDList idListEdit) {
			code = idListSave.getCodes();
			value = idListSave.getValues();

			if (idListEdit == null) {
				editCode = new ArrayList<String>(0);
			} else {
				editCode = idListEdit.getCodes();
			}

			aBoolean = new Boolean[code.size()];
			tBoolean = new Boolean[code.size()];

			String codeFull, codeEdited;
			int i, j;
			for (i = 0, j = 0; (i < value.size())
					&& (j < editCode.size()); i++) {
				codeEdited = editCode.get(j);
				codeFull = code.get(i);
				if (codeFull.compareTo(codeEdited) == 0) {
					aBoolean[i] = true;
					tBoolean[i] = true;
					j++;
				} else {
					aBoolean[i] = false;
					tBoolean[i] = false;
				}
			}

			for (; i < value.size(); i++) {
				aBoolean[i] = false;
				tBoolean[i] = false;
			}

		}

		/**
		 * Constructor
		 */
		public Coder_IDViewer() {
		}

		/**
		 * TableModel method
		 *
		 * @param c int column number
		 * @return String
		 */
		@Override
		public String getColumnName(int c) {
			return columnName[c];
		}

		/**
		 * TableModel method
		 *
		 * @param r int row of object
		 * @param c int column of object
		 * @return Object at r,c
		 */
		@Override
		public Object getValueAt(int r, int c) {
			if (c == 0) {
				return aBoolean[r];
			}
			if (c == 1) {
				return code.get(r);
			}
			return value.get(r);
		}

		/**
		 * TableModel method
		 *
		 * @param o Object to set at r,c
		 * @param r int object row
		 * @param c int Object column
		 */
		@Override
		public void setValueAt(Object o, int r, int c) {
			if (c == 0) {
				Boolean inb = (Boolean) o;
				if (inb.booleanValue() != aBoolean[r].booleanValue()) {
					aBoolean[r] = inb;
				}
			}
			fireTableCellUpdated(r, c);
		}

		/**
		 * TableModel method
		 *
		 * @return int # of rows in table
		 */
		@Override
		public int getRowCount() {
			return value.size();
		}

		/**
		 * TableModel method
		 *
		 * @return int # of columns in table
		 */
		@Override
		public int getColumnCount() {
			return columnCount;
		}

		/**
		 * TableModel Method
		 *
		 * @param r int row to test
		 * @param c int column to test
		 * @return boolean if this cell at r,c is editable
		 */
		@Override
		public boolean isCellEditable(int r, int c) {
			if (c == 0) {
				return true;
			}
			return false;
		}

		/**
		 * TableModel method
		 *
		 * @param c int column
		 * @return Class of objects in this column
		 */

		@Override
		public Class<?> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/**
		 * Turns on all checkboxes to indicate all are being used
		 */
		public void setAll() {
			Boolean b = true;
			for (int r = 0; r < value.size(); r++) {
				setValueAt(b, r, 0);
				fireTableCellUpdated(r, 0);
			}
		}

		/**
		 * turns off check boxes to indicate none are being used.
		 */
		public void unsetAll() {
			Boolean b = false;
			for (int r = 0; r < value.size(); r++) {
				setValueAt(b, r, 0);
				fireTableCellUpdated(r, 0);
			}
		}

		/**
		 *
		 */
		public void resetTBoolean() {
			for (int r = 0; r < value.size(); r++) {
				tBoolean[r] = aBoolean[r].booleanValue();
			}
		}

		/**
		 * check if table changed
		 *
		 * @return boolean
		 */
		public boolean getChanged() {
			for (int i = 0; i < aBoolean.length; i++) {
				if (aBoolean[i].booleanValue() != tBoolean[i].booleanValue()) {
					return true;
				}
			}
			return false;
		}

	}
}
