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

package io.github.EDIandXML.OBOE;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import io.github.EDIandXML.OBOE.Containers.MetaTemplateContainer;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.DataElements.IDList;
import io.github.EDIandXML.OBOE.DataElements.IDListProcessor;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Parsers.IDListParser;
import io.github.EDIandXML.OBOE.Templates.TemplateCompositeElement;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.Templates.TemplateLoop;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;
import io.github.EDIandXML.OBOE.Templates.TemplateTable;
import io.github.EDIandXML.OBOE.Templates.TemplateTransactionSet;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for building a Transaction Set from an edi xml file this is a
 * subclass of the SAX2 handler <br>
 * Class contains a main method to allow it to be invoked as an
 * application. <br>
 * xmlfilename, <br>
 * 
 * OBOE - Open Business Objects for EDI <br>
 * An EDI and XML Translator Written In Java <br>
 *
 * 
 */

public class TransactionSetFactory extends DefaultHandler
		implements ContentHandler {

	/**
	 * current element number
	 */
	protected int _iElement = 0;
	/**
	 * current line number
	 */
	protected int _iLine = 0;
	/**
	 * current transaction set
	 */
	protected TemplateTransactionSet currentTransactionSet;
	protected TemplateTable currentTable;
	protected TemplateLoop currentLoop;
	protected MetaTemplateContainer currentTemplateContainer;
	protected Stack<MetaTemplateContainer> templateContainerStack = new Stack<>();
	protected TemplateSegment currentSegment = null;
	/**
	 * current composite
	 */
	protected TemplateCompositeElement currentCompositeElement = null;
	/**
	 * current element
	 */
	protected TemplateDataElement currentDataElement = null;
	/**
	 * id list processor
	 */
	protected IDListProcessor currentIDListProcessor = null;

	/**
	 * current id string
	 */
	protected String currentID = "";

	/**
	 * last node parsed
	 */
	protected String nodeName = "no set yet";
	protected String nameOrID = "no set yet";
	/** used to build the shortName if no shortName is defined */
	protected String segmentID = "";
	protected String elementID = "";

	/** simple string processor */
	protected CharArrayWriter contents = new CharArrayWriter();

	/**
	 * current idListFile name
	 */
	protected String idListFile = null;

	/**
	 * default value setup
	 */
	protected String defaultFrom = null;

	/**
	 * current Rule
	 */
	protected ElementRules currentRule;

	public static int typeSet = -10;

	/**
	 * current idCode string code comes before value so save it
	 */
	protected String idCode = null;

	/**
	 * current IDList parser object
	 */
	protected IDListParser idListParser;
	/**
	 * list of IDs
	 */
	protected Hashtable<String, IDList> idLists = new Hashtable<String, IDList>();

	/**
	 * current list of ids
	 */
	protected IDList currentIDList;

	/**
	 * directory path for file as specified in OBOE.properties
	 */
	protected String definedMessageFolder;
	/**
	 * directory path for found file
	 */
	protected String foundMessageFolder;

	/**
	 * parser object
	 */
	SAXParser parser;

	/** log4j object */
	static Logger logr = LogManager.getLogger(TransactionSetFactory.class);

	protected String knownSystemID = null;

	SAXParserFactory spf = null;

	/**
	 * construct the factory with a xml parser
	 *
	 * @throws Exception an xml parser exception
	 */

	public TransactionSetFactory() throws Exception {
		spf = SAXParserFactory.newInstance();
		definedMessageFolder = Util.getMessageDescriptionFolder();
		foundMessageFolder = definedMessageFolder;

		spf.setNamespaceAware(true);

		parser = spf.newSAXParser();

		idListParser = new IDListParser();

	}

	private Locator locator = null;

	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	private void reportPosition() {

		if (locator != null) {

			_iLine = locator.getLineNumber();
		}

	}

	/**
	 * method to call parser with an InputSource argument
	 *
	 * @param is InputSource
	 * @throws IOException  an i/o exception
	 * @throws SAXException an xml parser exception
	 */

	public void parse(InputSource is) throws SAXException, IOException {
		try {
			is.setSystemId("");
			parser.parse(is, this);
		} catch (SAXException e1) {
			logr.error("Caught exception: " + e1.getMessage());
			logr.error("Near element #: " + _iElement + "     line: " + _iLine
					+ "     nodeName: " + nodeName + "     name or id: "
					+ nameOrID);
			logr.error(e1.getMessage(), e1);
			throw e1;
		}

	}

	/**
	 * method to call parser with a String argument
	 *
	 * @param is String of definition data
	 * @throws IOException  an i/o exception
	 * @throws SAXException an xml parser exception
	 */

	public void parse(String is) throws SAXException, IOException {
		try {
			parser.parse(is, this);
		} catch (SAXException e1) {
			logr.error("Caught exception: " + e1.getMessage());
			logr.error("Near element #: " + _iElement + "     line: " + _iLine
					+ "     nodeName: " + nodeName + "     name or id: "
					+ nameOrID);
			logr.error(e1.getMessage(), e1);
			throw e1;
		}

	}

	/**
	 * method called for each xml element found. <br>
	 * process logic
	 * <ul>
	 * <li>test each name found for edi type: transactionSet, table,
	 * segment, dataelement
	 * <li>for each type pull appropriate attributes and construct object
	 * <li>for transaction set build transaction set
	 * <li>for table build a table
	 * <li>for segments build a template segment
	 * <li>for data element build a template datalement</li>
	 *
	 * @param uri        URI of incoming file
	 * @param localName  String of element's local name
	 * @param rawName    String of element's raw name
	 * @param attributes ArrayList of the elements attributes
	 * @throws SAXException many possible exceptions
	 */
	@Override
	public void startElement(java.lang.String uri, java.lang.String localName,
			java.lang.String rawName, Attributes attributes)
			throws SAXException {
		int i;
		reportPosition();
		nodeName = rawName;
		_iElement++;
		contents.reset();

		if (nodeName.equals("transactionSet")) {

			logr.debug("validating = " + spf.isValidating());

			String id = "", tsName = "", revision = "", functionalGroup = "",
					description = "", shortName = null;

			String validatingMethod = null;

			for (i = 0; i < attributes.getLength(); i++) {

				if (attributes.getQName(i).equals("id")) {
					id = attributes.getValue(i);
				}
				if (attributes.getQName(i).equals("name")) {
					tsName = attributes.getValue(i);
				}
				if (attributes.getQName(i).equals("revision")) {
					revision = attributes.getValue(i);
				}
				if (attributes.getQName(i).equals("functionalGroup")) {
					functionalGroup = attributes.getValue(i);
				}
				if (attributes.getQName(i).equals("description")) {
					description = attributes.getValue(i);
				}
				if (attributes.getQName(i).equals("shortName")) {
					shortName = attributes.getValue(i);
				}

				if (attributes.getQName(i).equals("validatingMethod")) {
					validatingMethod = attributes.getValue(i);
				}

			}

			nameOrID = tsName;
			if (shortName == null) {
				shortName = "TransactionSet_" + id;
			}

			Format format;
			if (Character.isDigit(id.charAt(0))) {
				format = Format.X12_FORMAT;
			} else {
				format = Format.EDIFACT_FORMAT;
			}

			currentTransactionSet = new TemplateTransactionSet(format, id,
					tsName, revision, functionalGroup, description, shortName,
					null);

			if (validatingMethod != null) {
				currentTransactionSet.setValidatingMethod(getValidatingMethod(
						validatingMethod, currentTransactionSet));
			}
			// templateContainerStack.push(currentTemplateContainer);
			// nothing to put onto the stack yet
			currentTemplateContainer = currentTransactionSet;

			return;
		}
		if (nodeName.equals("table")) {
			String shortName = null;

			String validatingMethod = null;

			for (i = 0; i < attributes.getLength(); i++) {
				if (attributes.getQName(i).equals("shortName")) {
					shortName = attributes.getValue(i);
					break;
				}
			}
			for (i = 0; i < attributes.getLength(); i++) {
				if (attributes.getQName(i).equals("section")) {
					break;
				}
			}
			if (i >= attributes.getLength()) {
				throw new SAXException(
						"Table section name missing, see element " + _iElement);
			}
			if (attributes.getValue(i).equals("header")) {
				if (currentTransactionSet.getHeaderTemplateTable() == null) {
					currentTable = new TemplateTable(attributes.getValue(i),
							currentTransactionSet);

				} else {
					throw new SAXException(
							"Header table already defined, see element "
									+ _iElement);
				}
			}
			if (attributes.getValue(i).equals("detail")) {
				if (currentTransactionSet.getDetailTemplateTable() == null) {
					currentTable = new TemplateTable(attributes.getValue(i),
							currentTransactionSet);

				} else {
					throw new SAXException(
							"Detail table already defined, see element "
									+ _iElement);
				}
			}
			if (attributes.getValue(i).equals("summary")) {
				if (currentTransactionSet.getSummaryTemplateTable() == null) {
					currentTable = new TemplateTable(attributes.getValue(i),
							currentTransactionSet);

				} else {
					throw new SAXException(
							"Summary table already defined, see element "
									+ _iElement);
				}
			}

			nameOrID = attributes.getValue(i);
			if (shortName == null) {
				shortName = "Table_" + nameOrID;
			}

			for (i = 0; i < attributes.getLength(); i++) {

				if (attributes.getQName(i).equals("validatingMethod")) {
					validatingMethod = attributes.getValue(i);
				}

			}

			if (validatingMethod != null) {
				currentTable.setValidatingMethod(
						getValidatingMethod(validatingMethod, currentTable));
			}
			templateContainerStack.push(currentTemplateContainer);
			currentTemplateContainer.addContainer(currentTable);
			currentTemplateContainer = currentTable;

			return;
		}
		if (nodeName.equals("loop")) {
			String currentID = "";
			String currentName = "";
			int occurs = 1;
			char required = 'O';
			boolean used = true;
			String shortName = null;

			String validatingMethod = null;

			for (i = 0; i < attributes.getLength(); i++) {
				if (attributes.getQName(i).equals("id")) {
					currentID = attributes.getValue(i);
				}
				if (attributes.getQName(i).equals("name")) {
					currentName = attributes.getValue(i);
				}
				if (attributes.getQName(i).equals("required")) {
					required = attributes.getValue(i).charAt(0);
				}
				if (attributes.getQName(i).equals("occurs")) {
					String sOccurs = attributes.getValue(i).trim();
					if (sOccurs.length() > 0) {
						// let a numeric parsing exception occur on its own
						occurs = Integer.parseInt(sOccurs);
					}
				}

				if (attributes.getQName(i).equals("shortName")) {
					shortName = attributes.getValue(i);
				}

				if (attributes.getQName(i).equals("used")) {
					used = attributes.getValue(i).trim().charAt(0) == 'Y';
				}

				if (attributes.getQName(i).equals("validatingMethod")) {
					validatingMethod = attributes.getValue(i);
				}

			}

			if (shortName == null) {
				shortName = "Loop_" + currentID;
			}

			currentLoop = new TemplateLoop(currentID, currentName, occurs,
					required, shortName, used, currentTemplateContainer);

			if (validatingMethod != null) {
				currentLoop.setValidatingMethod(
						getValidatingMethod(validatingMethod, currentLoop));
			}
			templateContainerStack.push(currentTemplateContainer);
			currentTemplateContainer.addContainer(currentLoop);
			currentTemplateContainer = currentLoop;
			return;
		}

		if (nodeName.equals("segment")) {
			String shortName = null;
			currentCompositeElement = null;

			int sequence = 0;
			String description = "";
			String currentName = "";
			char required = 'O';
			int occurs = 1;
			boolean used = true;

			String validatingMethod = null;

			for (i = 0; i < attributes.getLength(); i++) {
				if (attributes.getQName(i).equals("id")) {
					currentID = attributes.getValue(i);
				}
				if (attributes.getQName(i).equals("sequence")) {

					sequence = Integer.parseInt(attributes.getValue(i));

				}
				if (attributes.getQName(i).equals("description")) {
					description = attributes.getValue(i);
				}
				if (attributes.getQName(i).equals("name")) {
					currentName = attributes.getValue(i);
				}
				if (attributes.getQName(i).equals("shortName")) {
					shortName = attributes.getValue(i);
				}
				if (attributes.getQName(i).equals("occurs")) {
					String sOccurs = attributes.getValue(i).trim();
					if (sOccurs.length() > 0) {
						// let a numeric parsing exception occur on its own
						occurs = Integer.parseInt(sOccurs);
					}
				}
				if (attributes.getQName(i).equals("required")) {
					required = attributes.getValue(i).charAt(0);
				}
				if (attributes.getQName(i).equals("used")) {
					used = attributes.getValue(i).trim().charAt(0) == 'Y';
				}

				if (attributes.getQName(i).equals("validatingMethod")) {
					validatingMethod = attributes.getValue(i);
				}

			}

			if (shortName == null) {
				shortName = "Segment_" + currentID + "_" + sequence;
			}

			nameOrID = currentID + " name:" + currentName + " sequence: "
					+ sequence + " shortName: " + shortName;
			currentSegment = new TemplateSegment(currentID, currentName,
					sequence, description, occurs, required, shortName, used,
					currentTemplateContainer);

			if (validatingMethod != null) {
				currentSegment.setValidatingMethod(
						getValidatingMethod(validatingMethod, currentSegment));
			}

			try {
				currentTemplateContainer.addContainer(currentSegment);
			} catch (Exception e1) {
				logr.error(e1.getMessage(), e1);
				throw new SAXException(e1.getMessage());
			}

			segmentID = currentID;
			elementID = currentID;

			return;
		}

		if (nodeName.equals("compositeDE")) {
			currentCompositeElement = setTemplateComposite(
					currentSegment.getContainerSize(), attributes);
			nameOrID = currentCompositeElement.getID();
			try {
				currentSegment.addElement(currentCompositeElement);
			} catch (OBOEException oe) {
				logr.error(oe.getMessage(), oe);
				throw new SAXException(oe.getMessage());
			}
			return;
		}

		if (nodeName.equals("dataElement")) {
			int pos;
			if (currentCompositeElement != null) {
				pos = currentCompositeElement.getContainerSize();
			} else {
				pos = currentSegment.getContainerSize();
			}

			currentDataElement = setDataElement(pos, attributes);
			nameOrID = currentDataElement.getID();

			try {
				if (currentCompositeElement != null) {
					currentCompositeElement.addElement(currentDataElement);
				} else {
					currentSegment.addElement(currentDataElement);
				}
			} catch (OBOEException oe) {
				logr.error(oe.getMessage(), oe);
				throw new SAXException(oe.getMessage());
			}

			currentIDListProcessor = null;
			return;
		}

		if (nodeName.equals("idListClass")) {

			if (currentDataElement.getType().equals("AN")) {
				currentDataElement.setType("ID");
				logr.info("changed dataelement id =\""
						+ currentDataElement.getID()
						+ "\" from type AN to ID because of idListClass node");
			}

			if (currentDataElement.getType().equals("ID") == false) {
				logr.error("cannot use idListClass node with dataelement id =\""
						+ currentDataElement.getID() + "\"");
			}

			try {
				currentIDListProcessor = (IDListProcessor) Class
						.forName(attributes.getValue("className"))
						.getDeclaredConstructor().newInstance();
			} catch (Exception exc) {
				logr.error(exc.getMessage(), exc);
				throw new OBOEException("Exception looking for idListClass="
						+ attributes.getValue("className"));
			}
			currentDataElement.setIDList(currentIDListProcessor);
			return;

		}
		if (nodeName.equals("idListFile")) {
			if ((currentDataElement.getType().equals("A"))
					|| (currentDataElement.getType().equals("AN"))) {
				currentDataElement.setType("ID");
				logr.info("changed dataelement id =\""
						+ currentDataElement.getID()
						+ "\" from type AN to ID because of idListFile node");
			}

			if (currentDataElement.getType().equals("ID") == false) {
				logr.error("cannot use idListFile node with dataelement id =\""
						+ currentDataElement.getID() + "\"");
			}

			currentIDList = idLists
					.get(foundMessageFolder + attributes.getValue("fileName"));

			if (currentIDList == null) {
				currentIDList = new IDList(
						foundMessageFolder + attributes.getValue("fileName"),
						definedMessageFolder, idListParser);
				idLists.put(
						foundMessageFolder + attributes.getValue("fileName"),
						currentIDList);
				currentTransactionSet.addIDListFile(new File(
						foundMessageFolder + attributes.getValue("fileName")));
			}
			for (i = 0; i < attributes.getLength(); i++) {
				if (attributes.getQName(i).equals("fileName")) {
					continue;
				}
				if (attributes.getQName(i).equals("include")) {
					currentIDList = currentIDList.idListWork('i',
							attributes.getValue(i));
					currentIDList.setFilterList(
							"include=\"" + attributes.getValue(i) + "\"");
				}
				if (attributes.getQName(i).equals("exclude")) {
					currentIDList = currentIDList.idListWork('x',
							attributes.getValue(i));
					currentIDList.setFilterList(
							"exclude=\"" + attributes.getValue(i) + "\"");
				}
			}
			currentIDListProcessor = currentIDList;
			currentDataElement.setIDList(currentIDListProcessor);
			return;
		}
		if (nodeName.equals("idList")) {
			if (currentDataElement.getType().equals("AN")) {
				currentDataElement.setType("ID");
				logr.info("changed dataelement id =\""
						+ currentDataElement.getID()
						+ "\" from type AN to ID because of idList node");
			}

			if (currentDataElement.getType().equals("ID") == false) {
				logr.error("cannot use idList node with dataelement id =\""
						+ currentDataElement.getID() + "\"");
			}

			currentIDList = new IDList();
			currentIDListProcessor = currentIDList;
			currentDataElement.setIDList(currentIDListProcessor);
			return;
		}

		if (rawName.equals("idCode")) {
			return;
		}

		if (rawName.equals("idValue")) {
			return;
		}

		if (nodeName.equals("default")) {
			defaultFrom = attributes.getValue(0); // only one attribute
			nameOrID = defaultFrom;
			return;
		}

		if (nodeName.equals("elementRule")) {
			String rule = "", positions = "";
			for (i = 0; i < attributes.getLength(); i++) {
				if (attributes.getQName(i).equals("rule")) {
					rule = attributes.getValue(i);
				}
				if (attributes.getQName(i).equals("positions")) {
					positions = attributes.getValue(i);
				}
			}
			nameOrID = rule;
			currentRule = new ElementRules(rule, positions);

			for (int ci = 1; ci <= currentRule.getPositionCount(); ci++) {
				if ((currentRule.getPosition(ci) < 1)
						|| (currentRule.getPosition(
								ci) > currentSegment.myElementContainer.templateDataElementList
										.lastKey())) {
					throw new SAXException(
							"ElementRule position specification invalid. No data element at "
									+ currentRule.getPosition(ci));
				}
			}
			currentSegment.addElementRule(currentRule);
			return;
		}

		throw new SAXException("logic error: unknown type " + nodeName + " "
				+ currentID + " for element: " + _iElement);

	}

	/**
	 * @param path
	 * @return TemplateSegment segment
	 */

	/**
	 * Method called by the SAX parser at the </
	 *
	 * @param uri       URI of incoming file
	 * @param localName String of element's local name
	 * @param rawName   String of element's raw name
	 * @throws SAXException many possible *
	 */
	@Override
	public void endElement(java.lang.String uri, java.lang.String localName,
			java.lang.String rawName) throws SAXException {

		reportPosition();

		String name = rawName;
		if (name.equals("transactionSet")) {
			// currentTemplateContainer = templateContainerStack.pop();
			// stack should be empty

			return;
		}
		if (name.equals("table")) {
			currentTemplateContainer = templateContainerStack.pop();
			currentTransactionSet = (TemplateTransactionSet) currentTemplateContainer;
			return;
		}
		if (name.equals("loop")) {
			currentTemplateContainer = templateContainerStack.pop();
			if (currentTemplateContainer instanceof TemplateLoop) {
				currentLoop = (TemplateLoop) currentTemplateContainer;
			} else if (currentTemplateContainer instanceof TemplateTable) {
				currentTable = (TemplateTable) currentTemplateContainer;
			} else {
				throw new SAXException(
						"bad OBOE logic, what is this doing in the stack "
								+ currentTemplateContainer.getID());
			}
			return;
		}

		if (name.equals("segment")) {
			currentSegment = null;
			return;
		}

		if (name.equals("compositeDE")) {
			currentCompositeElement = null;
			return;
		}

		if (name.equals("dataElement")) {
			currentDataElement = null;
			return;
		}

		if (nodeName.equals("idListProcess")) {
			return;
		}
		if (nodeName.equals("idListFile")) {
			return;
		}
		if (rawName.equals("idList")) {
			currentDataElement.setIDList(currentIDList);
			return;
		}

		if (rawName.equals("idCode")) {
			idCode = contents.toString(); // code comes before value so save it
			return;
		}

		if (rawName.equals("idValue")) {
			currentIDList.add(idCode, contents.toString());
			return;
		}

		if (name.equals("default")) {
			String s = contents.toString();
			if (defaultFrom.equals("constant")) {
				currentDataElement.setLoadFromConstant(s);
			} else if (defaultFrom.equals("property")) {
				currentDataElement.setLoadFromProperty(s);
			} else if (defaultFrom.equals("method")) {
				currentDataElement.setLoadFromClassMethod(s);
			}
			return;
		}

		// throw new SAXException("logic error: unknown type " + name +
		// " for element: " + _iElement);
	}

	/**
	 * Method handles #PCDATA
	 *
	 * @param ch     array
	 * @param start  position in array where next has been placed
	 * @param length int
	 *
	 *
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		reportPosition();

		contents.write(ch, start, length);
		for (int i = start; i < (start + length); i++) {
			if (ch[i] == '\n') {
				_iLine++;
			}
		}

	}

	/**
	 * help method to build a template composite
	 *
	 * @return TemplateComposite template composite
	 * @param pos        int position within segment
	 * @param attributes SAX2 attributes
	 * @throws SAXException SAX errors
	 */
	public TemplateCompositeElement setTemplateComposite(int pos,
			Attributes attributes) throws SAXException {
		int i;
		String id = "", thisName = "", description = "";
		int occurs = 1;
		int sequence = pos + 1;
		String sPosition;
		String shortName = null;
		char required = 'O';
		boolean used = true;

		String validatingMethod = null;

		for (i = 0; i < attributes.getLength(); i++) {
			if (attributes.getQName(i).equals("id")) {
				id = attributes.getValue(i);
			}
			if (attributes.getQName(i).equals("description")) {
				description = attributes.getValue(i);
			}
			if (attributes.getQName(i).equals("name")) {
				thisName = attributes.getValue(i);
			}
			if (attributes.getQName(i).equals("required")) {
				required = attributes.getValue(i).charAt(0);
			}
			if (attributes.getQName(i).equals("sequence")) {
				try {
					sequence = Integer.parseInt(attributes.getValue(i));
				} catch (NumberFormatException nfe) {
					throw new OBOEException("sequence not numeric. "
							+ " look for sequence=\"" + attributes.getValue(i)
							+ "\"" + " nodeName: CompositeElement" + " name: "
							+ thisName + " id:" + id);
				}
			}
			if (attributes.getQName(i).equals("shortName")) {
				shortName = attributes.getValue(i);
			}
			if (attributes.getQName(i).equals("occurs")) {
				String sOccurs = attributes.getValue(i).trim();
				if (sOccurs.length() > 0) {
					try {
						occurs = Integer.parseInt(sOccurs);
						if (occurs < 1) {
							throw new OBOEException("occurs less than 1. "
									+ " look for occurs=\""
									+ attributes.getValue(i) + "\""
									+ " nodeName: CompositeElement" + " name: "
									+ thisName + " id:" + id);
						}
					} catch (NumberFormatException nfe) {
						throw new OBOEException("occurs not numeric. "
								+ " look for occurs=\"" + attributes.getValue(i)
								+ "\"" + " nodeName: CompositeElement"
								+ " name: " + thisName + " id:" + id);
					}
				}
			}
			if (attributes.getQName(i).equals("used")) {
				used = attributes.getValue(i).trim().charAt(0) == 'Y';
			}

			if (attributes.getQName(i).equals("validatingMethod")) {
				validatingMethod = attributes.getValue(i);
			}

		}
		DecimalFormat df = new DecimalFormat("00");
		sPosition = df.format(sequence);
		if (shortName == null) {
			shortName = elementID + sPosition;
		}

		elementID = elementID + sPosition + "_";
		TemplateCompositeElement currentComposite = new TemplateCompositeElement(
				id, thisName, required, sequence, description, shortName,
				currentSegment, occurs, used);

		if (validatingMethod != null) {
			currentComposite.setValidatingMethod(
					getValidatingMethod(validatingMethod, currentComposite));
		}

		return currentComposite;
	}

	/**
	 * help method to build a template data element
	 *
	 * @return TemplateDE template data element
	 * @param pos        position within segment or composite
	 * @param attributes SAX2 attributes
	 * @throws SAXException SAX errors
	 */
	public TemplateDataElement setDataElement(int pos, Attributes attributes)
			throws SAXException {
		int i;

		idListFile = null;

		String id = "", thisName = "", type = "", description = "";
		int minLength = 0, maxLength = 0;
		String shortName = null, required = "O";
		int position = pos + 1;
		String sPosition = "" + position;
		int occurs = 1;
		boolean used = true;

		String validatingMethod = null;

		for (i = 0; i < attributes.getLength(); i++) {
			if (attributes.getQName(i).equals("type")) {
				type = attributes.getValue(i);
			}
			if (attributes.getQName(i).equals("required")) {
				required = attributes.getValue(i);
			}
			if (attributes.getQName(i).equals("id")) {
				id = attributes.getValue(i);
			}
			if (attributes.getQName(i).equals("name")) {
				thisName = attributes.getValue(i);
			}
			if (attributes.getQName(i).equals("description")) {
				description = attributes.getValue(i);
			}
			if (attributes.getQName(i).equals("sequence")) {
				try {
					sPosition = attributes.getValue(i);
					position = Integer.parseInt(sPosition);
				} catch (NumberFormatException nfe) {
					throw new OBOEException("position not numeric. "
							+ " look for position=\"" + attributes.getValue(i)
							+ "\"" + " nodeName: dataElement" + " name: "
							+ thisName + " id:" + id);
				}
			}
			if (attributes.getQName(i).equals("minLength")) {
				try {
					minLength = Integer.parseInt(attributes.getValue(i));
				} catch (NumberFormatException nfe) {
					throw new OBOEException("minLength not numeric. "
							+ " look for minLength=\"" + attributes.getValue(i)
							+ "\"" + " nodeName: dataElement" + " name: "
							+ thisName + " id:" + id);
				}
			}
			if (attributes.getQName(i).equals("maxLength")) {
				try {
					maxLength = Integer.parseInt(attributes.getValue(i));
				} catch (NumberFormatException nfe) {
					throw new OBOEException("maxLength not numeric. "
							+ " look for maxLength=\"" + attributes.getValue(i)
							+ "\"" + " nodeName: dataElement" + " name: "
							+ thisName + " id:" + id);
				}
			}
			if (attributes.getQName(i).equals("shortName")) {
				shortName = attributes.getValue(i);
			}

			if (attributes.getQName(i).equals("used")) {
				used = attributes.getValue(i).trim().charAt(0) == 'Y';
			}

			if (attributes.getQName(i).equals("occurs")) {
				String sOccurs = attributes.getValue(i).trim();
				if (sOccurs.length() > 0) {
					try {
						occurs = Integer.parseInt(sOccurs);
						if (occurs < 1) {
							throw new OBOEException("occurs less than 1. "
									+ " look for occurs=\""
									+ attributes.getValue(i) + "\""
									+ " nodeName: dataElement" + " name: "
									+ thisName + " id:" + id);
						}
					} catch (NumberFormatException nfe) {
						throw new OBOEException("occurs not numeric. "
								+ " look for occurs=\"" + attributes.getValue(i)
								+ "\"" + " nodeName: dataElement" + " name: "
								+ thisName + " id:" + id);
					}
				}
			}

			if (attributes.getQName(i).equals("validatingMethod")) {
				validatingMethod = attributes.getValue(i);
			}

		}

		if (elementID.endsWith("_") == false) { // not a composite componenet
			DecimalFormat df = new DecimalFormat("00");
			sPosition = df.format(position);
		}
		if (shortName == null) {
			shortName = elementID + sPosition;
		}

		TemplateDataElement currentDE = new TemplateDataElement(id, thisName,
				position, type, required.charAt(0), description, minLength,
				maxLength, shortName, null, null, occurs, used);

		if (validatingMethod != null) {
			currentDE.setValidatingMethod(
					getValidatingMethod(validatingMethod, currentDE));
		}

		return currentDE;
	}

	/**
	 * method returns the validating method,
	 *
	 * @param inValidatingMethodName name from transaction set message
	 *                               description <br>
	 *                               format is class name + '.' + method
	 *                               name (e.g.
	 *                               "com.americancders.edi.validatingClass.validatingMethod")
	 *                               <br>
	 *                               getValidatingMethod checks
	 *                               <li>the class exists.
	 *                               <li>the method exits.
	 *                               <li>the method is public.
	 *                               <li>the method is static.
	 *                               <li>the method takes two objects ,
	 *                               class being validated (as defined by
	 *                               the message description file and
	 *                               <li>the method returns a boolean.
	 * @param inObject               object to be validated
	 * @return Method
	 */
	public static Method getValidatingMethod(String inValidatingMethodName,
			Object inObject) throws OBOEException {
		if (inValidatingMethodName == null) {
			return null;
		}
		int pos = inValidatingMethodName.lastIndexOf(".");
		if (pos < 0) {
			throw new OBOEException(
					"Validating method name format incorrect, use className+\".\"+methodName");
		}
		Class<?> testClass = null;

		String vClass = inValidatingMethodName.substring(0, pos);
		String vMethod = inValidatingMethodName.substring(pos + 1);
		Class<?> validatingClass;
		Class<?> classes[] = { null, null };
		try {

			validatingClass = Class.forName(vClass);
			if (inObject instanceof TemplateTransactionSet) {
				testClass = Class
						.forName("io.github.EDIandXML.OBOE.TransactionSet");
			}
			if (inObject instanceof TemplateTable) {
				testClass = Class.forName("io.github.EDIandXML.OBOE.Table");
			}
			if (inObject instanceof TemplateLoop) {
				testClass = Class.forName("io.github.EDIandXML.OBOELoop");
			}
			if (inObject instanceof TemplateSegment) {
				testClass = Class.forName("io.github.EDIandXML.OBOE.Segment");
			}
			if (inObject instanceof TemplateCompositeElement) {
				testClass = Class
						.forName("io.github.EDIandXML.OBOE.CompositeElement");
			}
			if (inObject instanceof TemplateDataElement) {
				testClass = Class
						.forName("io.github.EDIandXML.OBOE.DataElement");
			}
			classes[0] = testClass;
			classes[1] = Class
					.forName("io.github.EDIandXML.OBOE.DocumentErrors");
		} catch (java.lang.ClassNotFoundException cfne) {
			logr.error(cfne.getMessage(), cfne);
			throw new OBOEException("Validating class not found " + vClass
					+ " see " + inValidatingMethodName);
		}

		// test to see if method exists
		// thanks to Java's reflectability

		Method testValidatingMethod;
		try {
			testValidatingMethod = validatingClass.getMethod(vMethod, classes);
		} catch (java.lang.NoSuchMethodException nsme) {
			logr.error(nsme.getMessage(), nsme);
			throw new OBOEException(vMethod + "(" + testClass.getName()
					+ ", DocumentErrors) method not found in Class=" + vClass
					+ " see " + inValidatingMethodName);
		} catch (java.lang.SecurityException se) {
			logr.error(se.getMessage(), se);
			throw new OBOEException(
					"Security Exception while looking for validate method in Class="
							+ inValidatingMethodName + " see "
							+ inValidatingMethodName);
		}

		if (java.lang.reflect.Modifier
				.isStatic(testValidatingMethod.getModifiers())) {
			;
		} else {
			throw new OBOEException(vMethod
					+ "(object, DocumentErrors) method in Class=" + vClass
					+ " must be defined with static modifier; see "
					+ inValidatingMethodName);
		}

		if (java.lang.reflect.Modifier
				.isPublic(testValidatingMethod.getModifiers())) {
			;
		} else {
			throw new OBOEException(vMethod
					+ "(object, DocumentErrors) method in Class=" + vClass
					+ " must be defined with public modifier; see "
					+ inValidatingMethodName);
		}

		if (testValidatingMethod.getReturnType().getName()
				.equals("boolean") == false) {
			throw new OBOEException(vMethod
					+ "(object, DocumentErrors) method in Class=" + vClass
					+ " must return boolean; see " + inValidatingMethodName);
		}

		return testValidatingMethod;

	}

	/**
	 * I use this to keep track of line #s
	 *
	 * @param ch     char array of found whitespaces
	 * @param start  int start position in array
	 * @param length int length of what's been found
	 */
	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) {
		reportPosition();

		for (int i = start; i < (start + length); i++) {
			if (ch[i] == '\n') {
				_iLine++;
			}
		}
	}

	/**
	 * catches warning SAXParseExceptions this code sends exception to stdio
	 * and allows public classto continue
	 *
	 * @param e SaxException object
	 * @throws SAXException exception
	 */
	@Override
	public void warning(SAXParseException e) throws SAXException {
		logr.error("SAX Warning at (file "
				+ (knownSystemID == null ? e.getSystemId() : knownSystemID)
				+ "{" + e.getPublicId() + "}," + ", line " + e.getLineNumber()
				+ ", char " + e.getColumnNumber() + "): " + e.getMessage());
	}

	/**
	 * catches error SAXParseExceptions this code causes exception to
	 * continue
	 *
	 * @param e SaxException object
	 * @throws SAXException thrown
	 */
	@Override
	public void error(SAXParseException e) throws SAXException {
		throw new SAXException("Error at (file "
				+ (knownSystemID == null ? e.getSystemId() : knownSystemID)
				+ "{" + e.getPublicId() + "}," + ", line " + e.getLineNumber()
				+ ", char " + e.getColumnNumber() + "): " + e.getMessage());
	}

	/**
	 * catches fatal SAXParseExceptions this code causes exception to
	 * continue
	 *
	 * @param e SAXException object
	 * @throws SAXException thrown
	 */
	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		throw new SAXException("SAX Fatal Error at (file "
				+ (knownSystemID == null ? e.getSystemId() : knownSystemID)
				+ "{" + e.getPublicId() + "}," + ", line " + e.getLineNumber()
				+ ", char " + e.getColumnNumber() + "): " + e.getMessage());
	}

	private void setDirectoryPaths(String inPath, String inFoundPath) {
		definedMessageFolder = inPath;
		foundMessageFolder = inFoundPath;
	}

	/** store read template ts in a hash table for quick look up */
	static private Hashtable<String, TemplateTransactionSet> tsBuilt = new Hashtable<String, TemplateTransactionSet>();

	/**
	 * @return the tsBuilt
	 */
	public static TemplateTransactionSet getTemplateTransactionSet(String path,
			String id) {
		return tsBuilt.get(path + id);
	}

	/**
	 * static method to clear built hash table, created for test purposes
	 */
	static public void clearTable() {
		tsBuilt = new Hashtable<String, TemplateTransactionSet>();
	}

	/**
	 * static class method will build a transaction set based on input
	 * string and OBOE.properties defintion see OBOE.properties file to
	 * define the directory path
	 *
	 * @return TransactionSet
	 * @param inTSID String trransaction set id
	 * @throws OBOEException io error most likely
	 */
	public static TransactionSet buildTransactionSet(String inTSID)
			throws OBOEException {

		return buildTransactionSet(inTSID, null, null, null, null, null, true);

	}

	/**
	 * static class method will build a transaction set based on input
	 * string, the searchDirective, the four search directories and
	 * OBOE.properties definition
	 *
	 * @return TransactionSet
	 * @param inTSID                      String TransactionSet id
	 * @param inSearchDirective           String - any combination of V, S,
	 *                                    R, T. These provide the search
	 *                                    path for the message descriptions
	 *                                    through a directory structure as
	 *                                    specified by the next four
	 *                                    parameters. See full method for
	 *                                    more details about this and the
	 *                                    other parameters.
	 * @param inVersionDirectory          - specify directory as defined by
	 *                                    the Version value.
	 * @param inReceiverIDDirectory       - specify directory as defined by
	 * @param inSenderIDDirectory         - specify directory as defined by
	 * @param inTestOrProductionDirectory - specify directory as defined by
	 * @throws OBOEException io exception
	 */
	public static TransactionSet buildTransactionSet(String inTSID,
			String inSearchDirective, String inVersionDirectory,
			String inReceiverIDDirectory, String inSenderIDDirectory,
			String inTestOrProductionDirectory) throws OBOEException {

		return buildTransactionSet(inTSID, inSearchDirective,
				inVersionDirectory, inReceiverIDDirectory, inSenderIDDirectory,
				inTestOrProductionDirectory, true);
	}

	/**
	 * static class method will build a transaction set based on input
	 * string, the searchDirective, the four search directories and
	 * OBOE.properties definition see OBOE.properties file to define the
	 * directory path and optional searchDirective. At the very least the
	 * message description must reside in the directory specified by the
	 * messageDescriptionFolder. The message description name is appended
	 * with ".xml" <br>
	 * example #1 no search directive <br>
	 * OBOE.properties file contains: messageDescriptionFolder =
	 * c:/xmlDefinitions/ <br>
	 * input String is 840 <br>
	 * method will read file named: c:/xmlDefinitions/840.xml <br>
	 * example #2 full search directive <br>
	 * OBOE.properties file contains: messageDescriptionFolder =
	 * c:/xmlDefinitions/ <br>
	 * input String is 840 <br>
	 * searchDirective is VTRS <br>
	 * inVersion is 004010 <br>
	 * inTestProduction is P <br>
	 * inReceiverID is 000001 <br>
	 * inSenderID is AAAAA <br>
	 * method will search for file named:
	 * c:/xmlDefinitions/004010/P/000001/AAAAA/840.xml <br>
	 * if not found then it will search for file named:
	 * c:/xmlDefinitions/004010/P/000001/840.xml <br>
	 * if not found then it will search for file named:
	 * c:/xmlDefinitions/004010/P/840.xml <br>
	 * if not found then it will search for file named:
	 * c:/xmlDefinitions/004010/840.xml <br>
	 * if not found then it will search for file named:
	 * c:/xmlDefinitions/840.xml <br>
	 * if not found then it will throw OBOEException <br>
	 * example #2 partial search directive <br>
	 * OBOE.properties file contains: messageDescriptionFolder =
	 * c:/xmlDefinitions/ <br>
	 * input String is 840 <br>
	 * searchDirective is RV <br>
	 * inVersion is 004010 <br>
	 * inTestProduction is P <br>
	 * inReceiverID is 000001 <br>
	 * inSenderID is AAAAA <br>
	 * method will search for file named:
	 * c:/xmlDefinitions/000001/004010/840.xml <br>
	 * if not found then it will search for file named:
	 * c:/xmlDefinitions/000001/840.xml <br>
	 * if not found then it will search for file named:
	 * c:/xmlDefinitions/840.xml <br>
	 * if not found then it will throw OBOEException <br>
	 * <i>Note</i>If any directory parameter is null or zero length that
	 * directory is ignored even if the searchDirective specifies its usage.
	 * 
	 * <i>Note</i>Name the directories with the values a possible specified
	 * by your standard. This will be the only way the incoming document
	 * parser can find them. For instance in X12 specify a Version directory
	 * with the possible values of GS 480 value such as "004010" or
	 * "003031". In EDIFACT concatenate the UNH 0052 and 0054 fields such as
	 * "D93A" or "D99B".
	 *
	 * @return TransactionSet
	 * @param inTSID                      String TransactionSet id
	 * @param inSearchDirective           String - any combination of V, S,
	 *                                    R, T. These provide the search
	 *                                    path for the message descriptions
	 *                                    through a directory structure as
	 *                                    specified by the next four
	 *                                    parameters. The base directory is
	 *                                    defined by the
	 *                                    messageDescriptionFolder property
	 *                                    in OBOE.properties. Any
	 *                                    combination is possible to provide
	 *                                    for the search directive. The
	 *                                    directive value of "STV" -
	 *                                    indicates to search using the
	 *                                    SendId parameter value as a
	 *                                    directory, Test Indicator as as
	 *                                    Directory, and Version parameter
	 *                                    value as a directory. While "VR"
	 *                                    indicates to use the Version
	 *                                    number as a directory and Receiver
	 *                                    id. The package starts at the
	 *                                    lowest directory with its search.
	 *                                    If the message description files
	 *                                    is not found there it will go up
	 *                                    to the next directory and repeat
	 *                                    this process until the directory
	 *                                    as specified by the
	 *                                    messageDescriptionFolder property.
	 *                                    <br>
	 *                                    <i><b>Note</b></i>If a null is
	 *                                    passed, the method will look in
	 *                                    the OBOE.properties file for a
	 *                                    property named
	 *                                    <i>searchDirective</i>using the
	 *                                    same values. Use this property for
	 *                                    incoming documents.
	 * @param inVersionDirectory          - specify directory as defined by
	 *                                    the Version value. For this and
	 *                                    the other directory parameters do
	 *                                    not specify a directory separator.
	 *                                    If used specify the names as the
	 *                                    same as the the incoming process.
	 *                                    <ul>
	 *                                    For incoming processing the
	 *                                    directory name will be passed from
	 *                                    <li>X12: "480 - Version Release
	 *                                    Industry Identifier Code" from GS
	 *                                    - Functional Group Header segment
	 *                                    <li>EDIFACT: the concatenation of
	 *                                    "0052 - Message type version" and
	 *                                    "0054 Message type release" from
	 *                                    UNH - Message Header
	 *                                    </ul>
	 * @param inReceiverIDDirectory       - specify directory as defined by
	 *                                    For this and the other directory
	 *                                    parameters do not specify a
	 *                                    directory separator. If used
	 *                                    specify the names as the same as
	 *                                    the the incoming process.
	 *                                    <ul>
	 *                                    For incoming processing the
	 *                                    directory name will be passed from
	 *                                    <li>X12: "I07 - Interchange
	 *                                    Receiver ID"
	 *                                    <li>EDIFACT: "0010 - Recipient
	 *                                    Identification"
	 *                                    </ul>
	 * @param inSenderIDDirectory         - specify directory as defined by
	 *                                    For this and the other directory
	 *                                    parameters do not specify a
	 *                                    directory separator. If used
	 *                                    specify the names as the same as
	 *                                    the the incoming process.
	 *                                    <ul>
	 *                                    For incoming processing the
	 *                                    directory name will be passed from
	 *                                    <li>X12: "I06 - Interchange Sender
	 *                                    ID"
	 *                                    <li>EDIFACT: "0004 - Sender
	 *                                    identification",
	 *                                    </ul>
	 * @param inTestOrProductionDirectory - specify directory as defined by
	 *                                    For this and the other directory
	 *                                    parameters do not specify a
	 *                                    directory separator. If used
	 *                                    specify the names as the same as
	 *                                    the the incoming process.
	 *                                    <ul>
	 *                                    For incoming processing the
	 *                                    directory name will be passed from
	 *                                    <li>X12: "I14 - Test Indicator"
	 *                                    values of P or T.
	 *                                    <li>EDIFACT: "0035 - TEST
	 *                                    INDICATOR"
	 *                                    </ul>
	 * @param inSaveInArrayListIndicator  boolean to save EDI object in the
	 *                                    tsBuilt ArrayList
	 * @throws OBOEException io exception
	 */
	public static TransactionSet buildTransactionSet(String inTSID,
			String inSearchDirective, String inVersionDirectory,
			String inReceiverIDDirectory, String inSenderIDDirectory,
			String inTestOrProductionDirectory,
			boolean inSaveInArrayListIndicator) throws OBOEException {
		// messageDescriptionFolder - OBOE.properties value
		// messageDescriptionSearchPath - directory path to found message
		// description
		// messageDescriptionFolder - full path name to message description

		String messageDescriptionFolder = "", messageDescriptionSearchPath = "",
				messageDescriptionFolderPath = "";

		TemplateTransactionSet currentTransactionSet;

		File xmlFile = null;

		int i;

		String searchDirective = inSearchDirective;

		try {
			messageDescriptionFolder = Util.getMessageDescriptionFolder();

			if (messageDescriptionFolder == null) {
				messageDescriptionFolder = "";
			}
			if (searchDirective == null) {
				searchDirective = Util.getOBOEProperty(Util.SEARCH_DIRECTIVE);
			}

		} catch (Exception ex) {
			logr.error(ex.getMessage(), ex);
			throw new OBOEException(ex.getMessage());
		}

		String searchPaths[] = null;
		boolean foundXMLFile = false;

		if (searchDirective != null) {
			{
				String lilDirectives[] = searchDirective.split(",");
				searchLoop: for (int idirs = 0; idirs < lilDirectives.length; idirs++) {

					searchPaths = new String[lilDirectives[idirs].length() + 1];
					StringBuilder searchPath = new StringBuilder(
							messageDescriptionFolder);
					searchPaths[0] = messageDescriptionFolder;

					for (i = 0; i < lilDirectives[idirs].length(); i++) {
						switch (lilDirectives[idirs].charAt(i)) {
						case 'V':
							if ((inVersionDirectory != null)
									&& (inVersionDirectory.length() > 0)) {
								searchPath.append(inVersionDirectory.trim());
								searchPath.append(File.separatorChar);
							}

							break;
						case 'X': // gets a substring of the version
							// e.g. 004010X097 becomes 004010
							if ((inVersionDirectory != null)
									&& (inVersionDirectory.length() > 0)) {
								logr.debug("X directive: using "
										+ inVersionDirectory.trim()
										+ " to search directory with name of "
										+ inVersionDirectory.trim().substring(0,
												6));
								searchPath.append(inVersionDirectory.trim()
										.substring(0, 6));
								searchPath.append(File.separatorChar);
							}
							break;
						case 'R':
							if ((inReceiverIDDirectory != null)
									&& (inReceiverIDDirectory.length() > 0)) {
								searchPath.append(inReceiverIDDirectory.trim());
								searchPath.append(File.separatorChar);
							}

							break;
						case 'S':
							if ((inSenderIDDirectory != null)
									&& (inSenderIDDirectory.length() > 0)) {
								searchPath.append(inSenderIDDirectory.trim());
								searchPath.append(File.separatorChar);
							}

							break;
						case 'T':
							if ((inTestOrProductionDirectory != null)
									&& (inTestOrProductionDirectory
											.length() > 0)) {
								searchPath.append(
										inTestOrProductionDirectory.trim());
								searchPath.append(File.separatorChar);
							}

							break;

						default:
							throw new OBOEException(
									"searchDirective (" + lilDirectives[idirs]
											+ ") contains illegal character");
						} // switch
						searchPaths[i + 1] = new String(searchPath);
					} // for int i
					for (i = lilDirectives[idirs].length(); i > 0; i--) // there's
																		// at
																		// least
																		// one
					{
						messageDescriptionFolderPath = searchPaths[i] + inTSID
								+ ".xml";

						xmlFile = new File(messageDescriptionFolderPath);

						logr.debug(
								"looking for " + messageDescriptionFolderPath);
						if (xmlFile.exists()) {
							messageDescriptionSearchPath = searchPaths[i];
							foundXMLFile = true;
							break searchLoop;
						}
					}
				}

			}
		}
		if (foundXMLFile == false) {
			messageDescriptionFolderPath = messageDescriptionFolder + inTSID
					+ ".xml";
			logr.debug("looking for " + messageDescriptionFolderPath);
			messageDescriptionSearchPath = messageDescriptionFolder;
			xmlFile = new File(messageDescriptionFolderPath);
		}

		if (inSaveInArrayListIndicator) {
			TemplateTransactionSet tts = tsBuilt
					.get(messageDescriptionFolderPath);
			if (tts != null) {
				return new TransactionSet(tts, null);
			}
		}

		if ((xmlFile.exists() == false)) {
			throw new OBOEException("XML Message Description not found "
					+ messageDescriptionFolderPath);
		}

		try {

			synchronized (tsBuilt) {

				TransactionSetFactory tsf = new TransactionSetFactory();

				tsf.knownSystemID = messageDescriptionFolderPath;

				logr.debug("using ts description file"
						+ xmlFile.getAbsolutePath());

				tsf.setDirectoryPaths(messageDescriptionFolder,
						messageDescriptionSearchPath);

				InputSource is;
				if (Util.findMessageDefinitionFilesInClassPath()) {
					logr.debug(" using classpath xml file " + xmlFile);
					is = new InputSource(TransactionSetFactory.class
							.getClassLoader()
							.getResourceAsStream(messageDescriptionFolderPath));
				} else {
					logr.debug("idlistparser using xml file " + xmlFile);
					is = new InputSource(
							new FileReader(messageDescriptionFolderPath));
				}

				tsf.parse(is);
				currentTransactionSet = tsf.currentTransactionSet;
				tsBuilt.put(messageDescriptionFolderPath,
						currentTransactionSet);
			}

			return new TransactionSet(currentTransactionSet, null);

		} catch (Exception ex) {
			logr.error(ex.getMessage(), ex);

			throw new OBOEException(ex.getMessage());
		}

	}

	public static TransactionSet buildTransactionSet(File inFile) {
		try {
			InputSource is = new InputSource(new FileReader(inFile));
			TransactionSetFactory tsf = new TransactionSetFactory();
			String pth = inFile.getAbsolutePath();
			int i = pth.lastIndexOf(File.separatorChar);
			pth = pth.substring(0, i + 1);
			tsf.setDirectoryPaths(Util.getMessageDescriptionFolder(), pth);
			tsf.parse(is);
			TemplateTransactionSet currentTransactionSet = tsf.currentTransactionSet;
			return new TransactionSet(currentTransactionSet, null);
		} catch (Exception ex) {
			logr.error(ex.getMessage(), ex);
			throw new OBOEException(ex.getMessage());
		}

	}

	public static TransactionSet buildTransactionSetFromString(String inTS) {
		try {
			InputSource is = new InputSource(new StringReader(inTS));
			TransactionSetFactory tsf = new TransactionSetFactory();
			tsf.parse(is);
			TemplateTransactionSet currentTransactionSet = tsf.currentTransactionSet;
			return new TransactionSet(currentTransactionSet, null);
		} catch (Exception ex) {
			logr.error(ex.getMessage(), ex);
			throw new OBOEException(ex.getMessage());
		}

	}

	public TemplateTransactionSet getTemplateTransactionSet() {
		return currentTransactionSet;
	}

}
