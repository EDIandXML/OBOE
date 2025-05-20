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

package io.github.EDIandXML.OBOE.Parsers;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import io.github.EDIandXML.OBOE.EnvelopeFactory;
import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.TransactionSetFactory;
import io.github.EDIandXML.OBOE.Containers.ContainerType;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.IElementContainer;
import io.github.EDIandXML.OBOE.Containers.Loop;
import io.github.EDIandXML.OBOE.Containers.MetaContainer;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.Table;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.DataElements.CompositeElement;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.DataElements.Element;
import io.github.EDIandXML.OBOE.DataElements.NumericDE;
import io.github.EDIandXML.OBOE.EDIFACT.EDIFACTEnvelope;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.TRADACOMS.TradacomsEnvelope;
import io.github.EDIandXML.OBOE.Templates.TemplateCompositeElement;
import io.github.EDIandXML.OBOE.Templates.TemplateLoop;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;
import io.github.EDIandXML.OBOE.Templates.TemplateTable;
import io.github.EDIandXML.OBOE.ach.ACHEnvelope;
import io.github.EDIandXML.OBOE.util.Util;
import io.github.EDIandXML.OBOE.x12.X12Envelope;
import io.github.EDIandXML.OBOE.x12.X12FunctionalGroup;

/**
 * class builds OBOE objects by parsing input string in valid xml edi
 * format
 *
 * OBOE - Open Business Objects for EDI
 * 
 */

public class ValidXMLEDIParser extends DefaultHandler
		implements LexicalHandler {

	/** envelope */
	protected Envelope envelope;

	/**
	 * @return the envelope
	 */
	public Envelope getEnvelope() {
		return envelope;
	}

	/** functionalGroup */
	protected FunctionalGroup functionalGroup;
	/** transactionset */
	protected TransactionSet transactionSet;
	/** current element position */
	protected int _iElement = 0;
	/** current line number */
	protected int _iLine = 0;
	/** current table */
	protected Table table = null;
	/** current template table */
	protected TemplateTable templateTable = null;
	/** current loop */
	protected Loop loop = null;
	/** current seg */
	protected Segment segment = null;
	/** current comp */
	protected CompositeElement composite = null;
	/** current element */
	protected Element element = null;
	/** current de container */
	protected IElementContainer elementContainer = null;
	/** simple string processor */
	protected CharArrayWriter contents = new CharArrayWriter();
	/** parser object */
	protected SAXParser parser;

	Integer elementPosition;

	Integer saveElementPosition; // elements codes can repeat within a container
	// not to be confused with the fact that elements can have repeating
	// values
	// we need to save the element count because segments and composites are
	// element containers

	MetaContainer currentContainer;
	Stack<MetaContainer> containerStack = new Stack<>();

	protected TemplateSegment templateSegment = null;
	protected TemplateCompositeElement templateComposite = null;

	protected Boolean headerTableDefined = false, detailTableDefined = false,
			summaryTableDefined = false;

	protected String xmlDirectoryPath = "." + File.separator + ".";

	static Logger logr = LogManager.getLogger(ValidXMLEDIParser.class);

	/**
	 * create a XML EDI parser for valid xml files
	 *
	 */

	public ValidXMLEDIParser() {

		logr.debug("start validxmlediparser");

		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		spf.setValidating(true);

		try {
			parser = spf.newSAXParser();
			parser.getXMLReader().setProperty(
					"http://xml.org/sax/properties/lexical-handler", this);
			xmlDirectoryPath = Util.getMessageDescriptionFolder();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
			System.exit(0);
		} catch (SAXException saxe) {
			saxe.printStackTrace();
			System.exit(0);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			System.exit(0);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * parse an xml document coming in as a String
	 *
	 * @param text String xml data
	 * @exception SAXException
	 * @exception FileNotFoundException
	 * @exception IOException
	 */
	public void parse(String text)
			throws SAXException, FileNotFoundException, IOException {
		CharArrayReader car = new CharArrayReader(text.toCharArray());
		InputSource is = new InputSource(car);
		is.setSystemId("");
		parser.parse(is, this);
	}

	/**
	 * parse an xml
	 *
	 * @param filename String xml file name
	 * @exception SAXException
	 * @exception FileNotFoundException
	 * @exception IOException
	 */
	public void parseFile(String filename)
			throws SAXException, FileNotFoundException, IOException {
		InputSource is = new InputSource(new FileReader(filename));
		is.setSystemId("");
		parser.parse(is, this);
	}

	/**
	 * method called for each xml element found. <br>
	 * process logic
	 * <ul>
	 * <li>based on the name of the element found
	 * <li>for each pull appropriate attributes and construct object
	 * <li>if owned by another class, and all are except for Standard, add
	 * it to its parents object
	 * </ul>
	 *
	 * @param uri        URI of incoming file
	 * @param localName  String of element's local name
	 * @param rawName    String of element's raw name
	 * @param attributes ArrayList of the elements attributes
	 * @throws SAXException many possible exceptions
	 *
	 */
	@Override
	public void startElement(java.lang.String uri, java.lang.String localName,
			java.lang.String rawName, Attributes attributes)
			throws SAXException {

		_iElement++;
		contents.reset();

		switch (rawName) {
		case "envelope":
			doEnvelope(attributes);
			break;
		case "functionalgroup":
			containerStack.push(currentContainer);
			currentContainer = doFunctionalGroup(attributes);
			break;
		case "transactionset":
			containerStack.push(currentContainer);
			currentContainer = doTransactionSet(attributes);
			headerTableDefined = false;
			detailTableDefined = false;
			summaryTableDefined = false;
			break;
		case "table":
			containerStack.push(currentContainer);
			currentContainer = doTable(attributes);
			break;
		case "loop":
			containerStack.push(currentContainer);
			currentContainer = doLoop(attributes);
			break;
		case "segment":
			doSegment(attributes);
			elementPosition = 0;
			break;
		case "composite":
			doComposite(attributes);
			saveElementPosition = elementPosition;
			elementPosition = 0;
			break;
		case "element":
			doElement(attributes);

			break;
		case "value":
			break;
		default:
			logr.error("Logic error: Unknown element name \"" + rawName
					+ "\" found at element position " + _iElement + " line: "
					+ _iLine);
			throw new SAXException("Unknown element " + rawName);

		}

	}

	private void doElement(Attributes attributes) throws SAXException {
		String code = attributes.getValue("code");

		if (element != null) {
			if (code.equals(element.getID()) && element.getOccurs() != 1) {
				return;
			}
		}
		if (elementContainer instanceof CompositeElement) {

			element = composite.buildElement(code, elementPosition);

		} else {
			element = segment.getElement(code, elementPosition);
			if (element == null) {
				element = segment.buildElement(code, elementPosition);
			}

		}

		elementPosition = element.getPosition();

	}

	private void doComposite(Attributes attributes) throws SAXException {
		String code = attributes.getValue("code");

		if (composite != null) { // working on a composite, it is the same one?
			if ((code.equals(composite.getID()) & composite.getOccurs() > 1)) {
				// composite's occurs can never be -1 like segments and loops
				composite.createNewGroup();
				elementContainer = composite;
				return;
			}
			if (code.equals(composite.getID())) {
				elementPosition = composite.getPosition();
			}
		}

		composite = (CompositeElement) segment.getElement(code,
				elementPosition);
		if (composite == null) {
			composite = (CompositeElement) segment.buildElement(code,
					elementPosition);
		}

		elementContainer = composite;

	}

	private void doSegment(Attributes attributes) throws SAXException {
		String code = attributes.getValue("code");
		templateSegment = null;
		if (Util.propertyFileIndicatesDoPrevalidate()) {
			String sgname = attributes.getValue("name");
			templateSegment = currentContainer.getMyTemplate()
					.getTemplateSegmentByIDAndName(code, sgname);
			if (templateSegment == null) {
				throw new SAXException("segment code =" + code + " name = "
						+ sgname + " unknown to " + currentContainer + " "
						+ currentContainer.getID());
			}

			segment = currentContainer.createSegmentByIDAndName(code, sgname);
		} else {
			if (currentContainer.getMyTemplate()
					.getTemplateSegment(code) == null) {
				throw new SAXException("segment " + code + " unknown to "
						+ currentContainer + " " + currentContainer.getID());
			}

			segment = currentContainer.createSegment(code);
			templateSegment = segment.myTemplate;
		}

		elementContainer = segment;

	}

	private MetaContainer doLoop(Attributes attributes) {
		String code = attributes.getValue("code");
		var template = currentContainer.getMyTemplate()
				.getContainer(ContainerType.Loop, code);
		loop = new Loop((TemplateLoop) template, currentContainer);
		currentContainer.addLoop(loop);
		return loop;

	}

	private MetaContainer doTable(Attributes attributes) throws SAXException {
		String section = attributes.getValue("section");
		if (section.equals("header")) {
			if (headerTableDefined) {
				throw new SAXException("header table already defined");
			}
			headerTableDefined = true;
			table = transactionSet.getHeaderTable();
			currentContainer = table;

		} else if (section.equals("detail")) {
			if (detailTableDefined) {
				throw new SAXException("detail table already defined");
			}
			detailTableDefined = true;
			table = transactionSet.getDetailTable();
			currentContainer = table;

		} else if (section.equals("summary")) {
			if (summaryTableDefined) {
				throw new SAXException("summary table already defined");
			}
			summaryTableDefined = true;
			table = transactionSet.getSummaryTable();
			currentContainer = table;

		} else {
			throw new SAXException("table section=" + section + " unknown");
		}
		return currentContainer;

	}

	private MetaContainer doTransactionSet(Attributes attributes)
			throws SAXException {
		String tsID = attributes.getValue("code");
		String version = attributes.getValue("version"); // for EDIFACT only
		if (version != null && version.length() == 0) {
			version = null;
		}
		if (envelope instanceof X12Envelope) {

			transactionSet = TransactionSetFactory.buildTransactionSet(tsID,
					null,
					functionalGroup.getSegment(X12FunctionalGroup.idHeader)
							.getElement("480").get(),
					envelope.getSegment(X12Envelope.idInterchangeHeader)
							.getElement("I07").get(),
					envelope.getSegment(X12Envelope.idInterchangeHeader)
							.getElement("I06").get(),
					envelope.getSegment(X12Envelope.idInterchangeHeader)
							.getElement("I14").get());

		} else if (envelope instanceof EDIFACTEnvelope) {
			String testProduction = "";
			Element tde = envelope.getSegment("UNB").getElement("0035"); // do
																			// this
																			// becuase
																			// 0035
																			// is
																			// not
			// required
			if (tde != null) {
				testProduction = tde.get();
			}

			CompositeElement rlsNumber = (CompositeElement) envelope
					.getSegment("UNB").getElement("S003");

			transactionSet = TransactionSetFactory.buildTransactionSet(tsID,
					null, version, rlsNumber.getDataElementValue("0010"),
					envelope.getSegment("UNB").getDataElementValue("0004"),
					testProduction);

		} else if (envelope instanceof TradacomsEnvelope) {
			Element de = ((CompositeElement) envelope.getSegment("STX")
					.getElement("FROM")).getElement(2);
			String from = "";
			if (de != null) {
				from = de.get();
			}
			de = ((CompositeElement) envelope.getSegment("STX")
					.getElement("UNTO")).getElement(2);
			String unto = "";
			if (de != null) {
				unto = de.get();
			}

			transactionSet = TransactionSetFactory.buildTransactionSet(tsID,
					null,
					((CompositeElement) envelope.getSegment("STX")
							.getElement("STDS")).getElement(2).get(),
					from, unto, ""); // no

		}

		else if (envelope instanceof ACHEnvelope) {

			transactionSet = TransactionSetFactory.buildTransactionSet(tsID,
					null, null, null, null, null);

		} else {
			throw new SAXException("unknown envelope type");
		}

		transactionSet.setFormat(Format.VALID_XML_FORMAT);
		transactionSet.setParent(functionalGroup);
		if (functionalGroup != null) {
			functionalGroup.addTransactionSet(transactionSet);
		}

		return transactionSet;

	}

	private MetaContainer doFunctionalGroup(Attributes attributes) {
		functionalGroup = envelope.createFunctionalGroup();
		envelope.addFunctionalGroup(functionalGroup);
		return functionalGroup;
	}

	private void doEnvelope(Attributes attributes) throws SAXException {
		String format = attributes.getValue("format");
		switch (format) {
		case "X12":
			envelope = new X12Envelope(
					EnvelopeFactory.buildEnvelope("x12.envelope", "notSetYet"));
			break;
		case "EDIFACT":
			envelope = new EDIFACTEnvelope(EnvelopeFactory
					.buildEnvelope("EDIFACT.envelope", "notSetYet"));
			break;
		case "Tradacoms":
			envelope = new TradacomsEnvelope(EnvelopeFactory
					.buildEnvelope("Tradacoms.envelope", "notSetYet"));
			break;
		case "ACH":
		case "ACH-CBR-PBR":
			envelope = new ACHEnvelope(
					EnvelopeFactory.buildEnvelope("ACH.envelope", "notSetYet"));
			break;

		default:
			throw new SAXException("Envelope format " + format + " is unknown");
		}
		currentContainer = envelope;

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

		switch (rawName) {
		case "envelope":
			// functionalGroup = null;
			// segment = null;
			break;
		case "functionalgroup":
			functionalGroup = (FunctionalGroup) currentContainer;
			currentContainer = containerStack.pop();
			// transactionSet = null;
			// segment = null;

			break;
		case "transactionset":
			transactionSet = (TransactionSet) currentContainer;
			currentContainer = containerStack.pop();
			// table = null;
			// segment = null;
			break;
		case "table":
			table = (Table) currentContainer;
			currentContainer = containerStack.pop();
			// loop = null;
			// segment = null;
			break;
		case "loop":
			loop = (Loop) currentContainer;
			currentContainer = containerStack.pop();
			// segment = null;
			break;
		case "segment":
			if (elementContainer instanceof Segment == false) {
				throw new OBOEException("logic error");

			}

			currentContainer.addSegment(segment);
			if (segment.getID().equals("ISA")) {
				String v = segment.getDataElementValue("I15");
				if (v.length() == 1) {
					envelope.groupDelimiter = v;
				}
			}
			composite = null;
			element = null;

			break;
		case "composite":

			elementContainer = segment;

			// if (composite.myTemplate.getOccurs() == 1) {
			// composite = null;
			// }
			// element = null;
			elementPosition = saveElementPosition;
			break;
		case "element":
			// if (element.myTemplate.getOccurs() == 1) {
			// element = null;
			// }
			break;
		case "value":
			if (segment.getID().equals("ISA")) // may have to rebuild
			// envelope object
			{
				if (element.getID().compareTo("I11") == 0) {
					X12Envelope newenvelope = new X12Envelope(
							EnvelopeFactory.buildEnvelope("x12.envelope",
									contents.toString()));
					envelope = newenvelope;
					currentContainer = envelope;
					segment.setParent(envelope);

				}

			}
			if (element instanceof NumericDE) {
				((NumericDE) element).setFormatted(contents.toString());
			} else {
				((DataElement) element).setNext(contents.toString());
			}
			break;
		default:
			logr.error("Logic error: Unknown element name \"" + rawName
					+ "\" found at element position " + _iElement + " line: "
					+ _iLine);
			throw new SAXException("unknown element name " + rawName);
		}
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
		contents.write(ch, start, length);
		for (int i = start; i < start + length; i++) {
			if (ch[i] == '\n') {
				_iLine++;
			}
		}
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
		for (int i = start; i < start + length; i++) {
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
		logr.error("Warning at (file " + e.getSystemId() + ", line "
				+ e.getLineNumber() + ", char " + e.getColumnNumber() + "): "
				+ e.getMessage());
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
		throw new SAXException("Error at (file " + e.getSystemId() + ", line "
				+ e.getLineNumber() + ", char " + e.getColumnNumber() + "): "
				+ e.getMessage());
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
		throw new SAXException("Fatal Error at (file " + e.getSystemId()
				+ ", line " + e.getLineNumber() + ", char "
				+ e.getColumnNumber() + "): " + e.getMessage());
	}

	@Override
	public void startDTD(String name, String publicId, String systemId)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endDTD() throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startEntity(String name) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endEntity(String name) throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startCDATA() throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void endCDATA() throws SAXException {
		// TODO Auto-generated method stub

	}

	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub

	}

}
