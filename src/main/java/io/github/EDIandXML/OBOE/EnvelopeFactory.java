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
import io.github.EDIandXML.OBOE.DataElements.IDList;
import io.github.EDIandXML.OBOE.DataElements.IDListProcessor;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Parsers.IDListParser;
import io.github.EDIandXML.OBOE.Templates.TemplateCompositeElement;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.Templates.TemplateEnvelope;
import io.github.EDIandXML.OBOE.Templates.TemplateFunctionalGroup;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for building an Envelope from an edi xml file this is a
 * subclass of the SAX2 handler <br>
 * Class contains a main method to allow it to be invoked as an
 * application. <br>
 * 
 * OBOE - Open Business Objects for EDI <br>
 * An EDI and XML Translator Written In Java <br>
 *
 * 
 * 
 */

public class EnvelopeFactory extends DefaultHandler implements ContentHandler {
	/**
	 * current element number
	 */
	protected int _iElement = 0;
	/**
	 * current line number
	 */
	protected int _iLine = 0;
	/**
	 * current envelope
	 */
	protected TemplateEnvelope currentEnvelope = null;
	/**
	 * current functionalGroup
	 */
	protected TemplateFunctionalGroup currentFG = null;
	/**
	 * current segment
	 */
	protected TemplateSegment currentTemplateSegment = null;
	/**
	 * current composite
	 */
	protected TemplateCompositeElement currentCompositeElement = null;
	/**
	 * current element
	 */
	protected TemplateDataElement currentDataElement = null;
	/**
	 * loop id - may not be used in future
	 */
	protected String currentLoopID = "";
	/**
	 * current id string
	 */
	protected String currentID = "";

	/** simple string processor */
	protected CharArrayWriter contents = new CharArrayWriter();

	/**
	 * current idListFile name
	 */
	protected String idListFile = null;

	/** idList Processor */
	protected IDListProcessor currentIDListProcessor = null;

	/**
	 * default value setup
	 */
	protected String defaultFrom = null;

	/**
	 * current Rule
	 */
	protected ElementRules currentRule;

	/**
	 * current IDList
	 */

	protected IDList idlist;

	/**
	 * idcode value since idcode appears before idvalue save it
	 */

	protected String idCode = null;

	/**
	 * current IDList parser object
	 */
	protected IDListParser idListParser;
	/**
	 * list of IDs
	 */
	protected Hashtable<String, IDList> idLists;
	/**
	 * current list of ids
	 */
	protected IDList currentIDList;
	/**
	 * directory path for xml file
	 */
	protected String xmlDirectoryPath = "."
			+ System.getProperty("file.separator") + ".";
	/**
	 * directory path for xml file as specified in OBOE.properties
	 */
	protected String xmlFoundDirectoryPath;

	/**
	 * SAX2 parser
	 */
	SAXParser parser;

	/** default name */
	protected String nameOrID = "no set yet";

	/** file name being parsed */
	protected String knownSystemID = null;

	static Logger logr = LogManager.getLogger(EnvelopeFactory.class);

	/**
	 * construct the factory with a xml parser
	 *
	 * @throws Exception an xml parser exception
	 */
	MetaTemplateContainer currentContainer;
	Stack<MetaTemplateContainer> templateContainerStack = new Stack<>();

	public EnvelopeFactory() throws Exception {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		xmlDirectoryPath = Util.getMessageDescriptionFolder();
		xmlFoundDirectoryPath = xmlDirectoryPath;

		spf.setNamespaceAware(true);

		parser = spf.newSAXParser();

		currentEnvelope = null;

		idLists = new Hashtable<String, IDList>();
		idListParser = new IDListParser();
		// idListParser.parser.setEntityResolver(this);

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
	 * @throws Exception an xml parser exception
	 */

	public void parse(InputSource is) throws Exception {
		is.setSystemId("");
		parser.parse(is, this);
	}

	/**
	 * method to call parser with a String argument
	 *
	 * @param is String of xml data
	 * @throws Exception an xml parser exception
	 */

	public void parse(String is) throws Exception {
		parser.parse(is, this);
	}

	/**
	 * method called for each xml element found. <br>
	 * process logic
	 * <ul>
	 * <li>test each name found for edi type: Envelope, table, segment, data
	 * element
	 * <li>for each type pull appropriate attributes and construct object
	 * <li>for envelopes
	 * <li>for functional groups
	 * <li>for segments build a template segment
	 * <li>for data element build a template data element</li>
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
		reportPosition();

		int i;
		String name = rawName;
		_iElement++;
		contents.reset();

		String validatingMethod = null;

		if (name.equals("envelope")) {
			currentEnvelope = new TemplateEnvelope();
			currentEnvelope.setType(attributes.getValue("type"));
			currentContainer = currentEnvelope;

			return;
		}
		if (name.equals("functionalGroup")) {
			currentFG = new TemplateFunctionalGroup();
			currentEnvelope.setTemplateFunctionalGroup(currentFG);
			templateContainerStack.push(currentContainer);
			currentContainer = currentFG;
			return;
		}
		if (name.equals("transactionSet")) {
			return;
		}
		if (name.equals("segment")) {
			String shortName = null;
			currentCompositeElement = null;
			int sequence = 0;
			String description = "";
			String currentName = "";
			char required = 'O';
			int occurs = 1;
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
						// let it numeric parsing exception occur on its own
						occurs = Integer.parseInt(sOccurs);
					}
				}
				if (attributes.getQName(i).equals("required")) {
					required = attributes.getValue(i).charAt(0);
				}

				if (attributes.getQName(i).equals("validatingMethod")) {
					validatingMethod = attributes.getValue(i);
				}

			}

			if (shortName == null) {
				shortName = currentID;
			}
			currentTemplateSegment = new TemplateSegment(currentID, currentName,
					sequence, description, occurs, required, shortName, true,
					currentContainer);

			if (validatingMethod != null) {
				currentTemplateSegment.setValidatingMethod(
						TransactionSetFactory.getValidatingMethod(
								validatingMethod, currentTemplateSegment));
			}

			try {

				currentContainer.addContainer(currentTemplateSegment);
			} catch (Exception e1) {
				throw new SAXException(e1.getMessage());
			}
			return;
		}

		if (name.equals("compositeDE")) {
			currentCompositeElement = setTemplateComposite(
					currentTemplateSegment.getContainerSize(), attributes);
			try {
				currentTemplateSegment.addElement(currentCompositeElement);
			} catch (OBOEException oe) {
				oe.printStackTrace();
				throw new SAXException(oe.getMessage());
			}

			return;
		}
		if (name.equals("dataElement")) {
			int pos;
			if (currentCompositeElement != null) {
				pos = currentCompositeElement.getContainerSize();
			} else {
				pos = currentTemplateSegment.getContainerSize();
			}
			currentDataElement = setDataElement(pos, attributes);
			if (currentCompositeElement != null) {
				currentCompositeElement.addElement(currentDataElement);
			} else {
				try {
					currentTemplateSegment.addElement(currentDataElement);
				} catch (OBOEException oe) {
					oe.printStackTrace();
					throw new SAXException(oe.getMessage());
				}
			}

			return;
		}

		if (name.equals("idListClass")) {

			if ((currentDataElement.getType().equals("A"))
					|| (currentDataElement.getType().equals("AN"))) {
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
		if (name.equals("idListFile")) {
			if (currentDataElement.getType().equals("AN")) {
				currentDataElement.setType("ID");
				logr.info("changed dataelement id =\""
						+ currentDataElement.getID()
						+ "\" from type AN to ID because of idListFile node");
			}

			if (currentDataElement.getType().equals("ID") == false) {
				logr.error("cannot use idListFile node with dataelement id =\""
						+ currentDataElement.getID() + "\"");
			}

			currentIDList = idLists.get(
					xmlFoundDirectoryPath + attributes.getValue("fileName"));
			if (currentIDList == null) {

				currentIDList = new IDList(
						xmlFoundDirectoryPath + attributes.getValue("fileName"),
						xmlDirectoryPath, idListParser);
				idLists.put(
						xmlFoundDirectoryPath + attributes.getValue("fileName"),
						currentIDList);
				currentEnvelope.addIDListFile(
						new File(attributes.getValue("fileName")));
				// idLists.add(currentIDList);
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
		if (name.equals("idList")) {

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

		if (name.equals("default")) {
			defaultFrom = attributes.getValue(0); // only one attribute
			nameOrID = defaultFrom;
			return;
		}

		if (name.equals("elementRule")) {
			String rule = "", positions = "";
			for (i = 0; i < attributes.getLength(); i++) {
				if (attributes.getQName(i).equals("rule")) {
					rule = attributes.getValue(i);
				}
				if (attributes.getQName(i).equals("positions")) {
					positions = attributes.getValue(i);
				}
			}
			currentRule = new ElementRules(rule, positions);
			currentTemplateSegment.addElementRule(currentRule);
			return;
		}

		throw new SAXException(
				"logic error: unknown type " + name + ". Last item was "
						+ currentID + " at element # " + _iElement);

	}

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
		if (name.equals("envelope")) {
			currentContainer = null;
			return;
		}
		if (name.equals("functionalGroup")) {
			currentContainer = templateContainerStack.pop();
			return;
		}
		if (name.equals("segment")) {
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

		if (name.equals("idListProcess")) {
			return;
		}
		if (name.equals("idListFile")) {
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
	 * method to build a template composite <br>
	 * made static in release 2.5.0
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
		int position = pos + 1;
		String shortName = null;
		char required = 'O';

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
				position = Integer.parseInt(attributes.getValue(i));
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

			if (attributes.getQName(i).equals("validatingMethod")) {
				validatingMethod = attributes.getValue(i);
			}

		}
		if (shortName == null) {
			shortName = id;
		}

		TemplateCompositeElement currentTemplateComposite = new TemplateCompositeElement(
				id, thisName, required, position, description, shortName,
				currentTemplateSegment, occurs, true);

		if (validatingMethod != null) {
			currentTemplateComposite.setValidatingMethod(
					TransactionSetFactory.getValidatingMethod(validatingMethod,
							currentTemplateSegment));
		}

		return currentTemplateComposite;
	}

	/**
	 * method to build a template data element <br>
	 * made static in release 2.5.0
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
		int occurs = 1;

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
				position = Integer.parseInt(attributes.getValue(i));
			}
			if (attributes.getQName(i).equals("minLength")) {
				minLength = Integer.parseInt(attributes.getValue(i));
			}
			if (attributes.getQName(i).equals("maxLength")) {
				maxLength = Integer.parseInt(attributes.getValue(i));
			}
			if (attributes.getQName(i).equals("shortName")) {
				shortName = attributes.getValue(i);
			}

			if (attributes.getQName(i).equals("idListFile")) {
				idListFile = attributes.getValue(i);
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

		if (shortName == null) {
			shortName = id;
		}

		TemplateDataElement currentTemplateDE = new TemplateDataElement(id,
				thisName, position, type, required.charAt(0), description,
				minLength, maxLength, shortName, null, null, occurs, true);

		if (validatingMethod != null) {
			currentTemplateDE.setValidatingMethod(
					TransactionSetFactory.getValidatingMethod(validatingMethod,
							currentTemplateSegment));
		}

		return currentTemplateDE;
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
		logr.error("Warning at (file "
				+ (knownSystemID == null ? e.getSystemId() : knownSystemID)
				+ ", line " + e.getLineNumber() + ", char "
				+ e.getColumnNumber() + "): " + e.getMessage());
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
				+ ", line " + e.getLineNumber() + ", char "
				+ e.getColumnNumber() + "): " + e.getMessage());
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
		throw new SAXException("Fatal Error at (file "
				+ (knownSystemID == null ? e.getSystemId() : knownSystemID)
				+ ", line " + e.getLineNumber() + ", char "
				+ e.getColumnNumber() + "): " + e.getMessage());
	}

	private void setDirectoryPath(String inPath, String inFoundPath) {
		xmlDirectoryPath = inPath;
		xmlFoundDirectoryPath = inFoundPath;
	}

	static private Hashtable<String, TemplateEnvelope> envBuilt = new Hashtable<String, TemplateEnvelope>();

	/**
	 * static class method will build a envelope based on input string and
	 * OBOE.properties definition see OBOE.properties file to define the
	 * directory path
	 *
	 * @param inFileName String without the path or xml file extension
	 * @param version    String specifies subpath to find version specific
	 *                   envelope message descriptions
	 * @return TemplateEnvelope
	 * @throws OBOEException io error most likely
	 */
	public static TemplateEnvelope buildEnvelope(String inFileName,
			String version) {

		String path;
		String msgDescriptionFolder;
		String msgDescriptionSearchPath;
		File xmlFile;
		TemplateEnvelope currentEnvelope;
		String searchDirective = null;
		try {
			searchDirective = Util.getOBOEProperty(Util.SEARCH_DIRECTIVE);
		} catch (IOException e) {
			;
		}
		if (searchDirective != null) {
			if (searchDirective.indexOf("V") > -1) {
				if (version.endsWith(File.separatorChar + "") == false) {
					version += File.separatorChar;
				}
			} else {
				version = "";
			}
		} else {
			version = "";
		}

		TemplateEnvelope gotBuilt = envBuilt.get(version + inFileName);
		if (gotBuilt != null) {
			return gotBuilt;
		}

		try {
			EnvelopeFactory ef = new EnvelopeFactory();
			ef.knownSystemID = inFileName;
			InputSource is = null;
			if (Util.findMessageDefinitionFilesInClassPath()) {
				path = Util.getOBOEProperty("xmlPath");

				msgDescriptionFolder = path + version + inFileName + ".xml";
				msgDescriptionSearchPath = path + version;
				logr.debug(" searching for envelope xml in classpath as: "
						+ msgDescriptionSearchPath + inFileName + ".xml");
				is = new InputSource(EnvelopeFactory.class.getClassLoader()
						.getResourceAsStream(msgDescriptionSearchPath
								+ inFileName + ".xml"));
				if (is.getByteStream() == null) {
					logr.debug(" envelope xml not found in classpath as: "
							+ msgDescriptionSearchPath + inFileName + ".xml");
					logr.debug(" searching for envelope xml in classpath as: "
							+ path + inFileName + ".xml");
					is = new InputSource(EnvelopeFactory.class.getClassLoader()
							.getResourceAsStream(path + inFileName + ".xml"));
					if (is.getByteStream() == null) {
						throw new OBOEException("Envelope Message Description ("
								+ version + inFileName
								+ ".xml) not found in classpath as specified in OBOE.properties file.)");
					}
					msgDescriptionSearchPath = path;
				}
				logr.debug(" using envelope xml in classpath as: " + path
						+ inFileName + ".xml");
				ef.setDirectoryPath(path, msgDescriptionSearchPath);

			}

			else {
				path = Util.getMessageDescriptionFolder();

				msgDescriptionFolder = path + version + inFileName + ".xml";
				msgDescriptionSearchPath = path + version;
				xmlFile = new File(msgDescriptionFolder);
				logr.debug("xml file path is " + xmlFile.getAbsolutePath());
				logr.debug(
						"envelope rules xml file test is " + xmlFile.exists());
				if (xmlFile.exists() == false) {
					msgDescriptionFolder = path + inFileName + ".xml";
					xmlFile = new File(msgDescriptionFolder);
					logr.debug("envelope rules xml file is " + xmlFile.exists()
							+ " for " + xmlFile.getAbsolutePath());
				}
				if (xmlFile.exists() == false) {
					throw new OBOEException(
							"Envelope Message Description not found: ("
									+ Util.getMessageDescriptionFolder()
									+ "/)");
				}

				ef.setDirectoryPath(path, msgDescriptionSearchPath);

				is = new InputSource(new FileReader(msgDescriptionFolder));
			}
			ef.parse(is);
			currentEnvelope = ef.currentEnvelope;

			envBuilt.put(version + inFileName, currentEnvelope);
			return currentEnvelope;

		}

		catch (Exception ex) {
			logr.error(ex.getMessage(), ex);
			throw new OBOEException(ex.getMessage());
		}

	}

	/* written for test classes */
	public static void reloadbuiltTable() {
		envBuilt = new Hashtable<String, TemplateEnvelope>();
	}

}
