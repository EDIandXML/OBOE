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

package io.github.EDIandXML.OBOE.EDIFACT;

/**
 * OBOE - Open Business Objects for EDI
 * 
 * 
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PushbackInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.TransactionSetFactory;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.DataElements.CompositeElement;
import io.github.EDIandXML.OBOE.DataElements.Element;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors.ERROR_TYPE;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Parsers.SegmentParser;
import io.github.EDIandXML.OBOE.Templates.TemplateEnvelope;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for wrapping a EDI transaction set within an EDI Envelope
 *
 */

public class EDIFACTEnvelope extends Envelope {

	/** constants for segments */

	/** segment constants */
	public static String idServiceString = "UNA";
	public static String idInterchangeHeader = "UNB";
	public static String idInterchangeTrailer = "UNZ";

	/**
	 * instantiates the class from a TemplateEnvelope, creates mandatory
	 * segments UNB and UNZ and creates one emtpy functional group
	 *
	 * @param inTempEnv TemplateEnvelope to build this class with
	 * @exception OBOEException missing segment definition in envelope xml.
	 */

	public EDIFACTEnvelope(TemplateEnvelope inTempEnv) throws OBOEException {
		super(inTempEnv);
		setFormat(Format.EDIFACT_FORMAT);
		segmentDelimiter = Envelope.EDIFACT_SEGMENT_DELIMITER;
		fieldDelimiter = Envelope.EDIFACT_FIELD_DELIMITER;
		groupDelimiter = Envelope.EDIFACT_GROUP_DELIMITER;
		repeatDelimiter = Envelope.EDIFACT_REPEAT_DELIMITER;
		escapeCharacter = Envelope.EDIFACT_ESCAPE_CHARACTER;
		// createInterchange_Header();
		// createFunctionalGroup();
		// createInterchange_Trailer();

	}

	/*
	 * method for parsing well formed edi xml files
	 *
	 * @param node a DOM node object
	 *
	 * @throws OBOException...
	 *
	 * @throws FileNotFoundException...
	 *
	 * @throws IOException...
	 */

	@Override
	public void parse(Node node) throws OBOEException {
		Node cnode = null;
		NodeList nl = node.getChildNodes();
		Segment seg;
		int i;
		for (i = 0; i < nl.getLength(); i++) {
			cnode = nl.item(i);
			if (cnode.getNodeType() == Node.ELEMENT_NODE) {
				break;
			}
		}

		if (i == nl.getLength()) {
			throw new OBOEException("No element nodes found");
		}

		if (cnode.getNodeName().equals("Service_String")) {
			seg = new Segment(
					getMyTemplate().getTemplateSegment(idServiceString), this);
			addSegment(seg);
			SegmentParser.parse(seg, cnode);

			for (i++; i < nl.getLength(); i++) {
				cnode = nl.item(i);
				if (cnode.getNodeType() == Node.ELEMENT_NODE) {
					break;
				}
			}

			if (i == nl.getLength()) {
				throw new OBOEException("Envelope terminated too soon");
			}
		}

		if (cnode.getNodeName().compareTo(getMyTemplate()
				.getTemplateSegment(idInterchangeHeader).getShortName()) != 0) {
			throw new OBOEException("Expected " + getMyTemplate()
					.getTemplateSegment(idInterchangeHeader).getShortName()
					+ " got " + cnode.getNodeName());
		}

		seg = new Segment(
				getMyTemplate().getTemplateSegment(idInterchangeHeader), this);
		addSegment(seg);
		SegmentParser.parse(seg, cnode);

		for (i++; i < nl.getLength(); i++) {
			cnode = nl.item(i);
			if (cnode.getNodeType() == Node.ELEMENT_NODE) {
				break;
			}
		}

		if (i == nl.getLength()) {
			throw new OBOEException("Envelope terminated too soon");
		}

		if (cnode.getNodeName()
				.compareTo(getMyTemplate().getTemplateFunctionalGroup()
						.getTemplateSegment(EDIFACTFunctionalGroup.idHeader)
						.getShortName()) != 0) {

			FunctionalGroup functionalGroup = new EDIFACTFunctionalGroup(
					getMyTemplate().getTemplateFunctionalGroup(), this);
			addFunctionalGroup(functionalGroup);
			do {
				String tsID = null;
				Node tsIDNode = cnode.getAttributes().getNamedItem("id");
				if (tsIDNode == null) {
					try {
						tsID = Util.getOBOEProperty(cnode.getNodeName());
					} catch (IOException e) {
						throw new OBOEException(cnode.getNodeName()
								+ " not defined in oboe.properties.");
					}
					if (tsID == null) {
						throw new OBOEException(cnode.getNodeName()
								+ " not defined in oboe.properties.");
					}
				} else {
					tsID = tsIDNode.getNodeValue();
				}

				if ((tsID == null) || (tsID.length() < 1)) {
					throw new OBOEException(
							"Can't find TransactionSet id value in "
									+ cnode.getNodeName());
				}

				String testProduction = "";
				Element tde = getSegment("UNB").getElement("0035"); // do
				// this because 0035 is not required
				if (tde != null) {
					testProduction = tde.get();
				}

				String version = "";
				tde = null;
				CompositeElement messageVersion = (CompositeElement) getSegment(
						"UNB").getElement("S008");
				if (messageVersion != null) {
					tde = messageVersion.getElement("0054");
					if (tde != null) {
						version = tde.get().trim();
					}
				}

				CompositeElement interchangeRecipient = (CompositeElement) getSegment(
						"UNB").getElement("S003");

				TransactionSet parsedTransactionSet = TransactionSetFactory
						.buildTransactionSet(tsID, null, version,
								interchangeRecipient.getElement("0010").get(),
								interchangeRecipient.getElement("0007").get(),
								testProduction);

				parsedTransactionSet.setFormat(Format.EDIFACT_FORMAT);
				parsedTransactionSet.parse(nl.item(i));
				functionalGroup.addTransactionSet(parsedTransactionSet);
				parsedTransactionSet.setParent(functionalGroup);

				for (i++; i < nl.getLength(); i++) {
					cnode = nl.item(i);
					if (cnode.getNodeType() == Node.ELEMENT_NODE) {
						break;
					}
				}
				if (i == nl.getLength()) {
					throw new OBOEException("Envelope terminated too soon");
				}
			} while (!cnode.getNodeName().equals(getMyTemplate()
					.getTemplateSegment(idInterchangeTrailer).getShortName()));

		} else {
			while (cnode.getNodeName()
					.compareTo(getMyTemplate().getTemplateFunctionalGroup()
							.getTemplateSegment(EDIFACTFunctionalGroup.idHeader)
							.getShortName()) == 0) {
				FunctionalGroup functionalGroup = new EDIFACTFunctionalGroup(
						getMyTemplate().getTemplateFunctionalGroup(), this);
				addFunctionalGroup(functionalGroup);
				seg = functionalGroup
						.createAndAddSegment(EDIFACTFunctionalGroup.idHeader);
				SegmentParser.parse(seg, cnode);

				for (i++; i < nl.getLength(); i++) {
					cnode = nl.item(i);
					if (cnode.getNodeType() == Node.ELEMENT_NODE) {
						break;
					}
				}

				if (i == nl.getLength()) {
					throw new OBOEException("Envelope terminated too soon");
				}

				while (cnode.getNodeName()
						.compareTo(getMyTemplate().getTemplateFunctionalGroup()
								.getTemplateSegment(
										EDIFACTFunctionalGroup.idTrailer)
								.getShortName()) != 0) {

					String tsID;
					try {
						tsID = Util.getOBOEProperty(cnode.getNodeName());
					} catch (IOException e) {
						throw new OBOEException(cnode.getNodeName()
								+ " not defined in oboe.properties.");
					}
					if (tsID == null) {
						throw new OBOEException(cnode.getNodeName()
								+ " not defined in OBOE.properties.");
					}

					TransactionSet parsedTransactionSet = TransactionSetFactory
							.buildTransactionSet(tsID);
					parsedTransactionSet.setFormat(Format.EDIFACT_FORMAT);
					parsedTransactionSet.parse(nl.item(i));
					functionalGroup.addTransactionSet(parsedTransactionSet);
					parsedTransactionSet.setParent(functionalGroup);

					for (i++; i < nl.getLength(); i++) {
						cnode = nl.item(i);
						if (cnode.getNodeType() == Node.ELEMENT_NODE) {
							break;
						}
					}

					if (i == nl.getLength()) {
						throw new OBOEException("Envelope terminated too soon");
					}
				}
				seg = functionalGroup
						.createAndAddSegment(EDIFACTFunctionalGroup.idTrailer);
				SegmentParser.parse(seg, cnode);

				for (i++; i < nl.getLength(); i++) {
					cnode = nl.item(i);
					if (cnode.getNodeType() == Node.ELEMENT_NODE) {
						break;
					}
				}

				if (i == nl.getLength()) {
					throw new OBOEException("Envelope terminated too soon");
				}

			}
		}

		if (cnode.getNodeName().compareTo(
				getMyTemplate().getTemplateSegment(idInterchangeTrailer)
						.getShortName()) != 0) {
			throw new OBOEException("Expecting " + getMyTemplate()
					.getTemplateSegment(idInterchangeTrailer).getShortName());
		}

		seg = new Segment(
				getMyTemplate().getTemplateSegment(idInterchangeTrailer), this);
		SegmentParser.parse(seg, cnode);
		addSegment(seg);

	}

	/*
	 * method for parsing fixed format edi files
	 *
	 * @param DataInputStream
	 *
	 * @throws OBOException...
	 *
	 * @throws FileNotFoundException...
	 *
	 * @throws IOException...
	 */

	@Override
	public void parse(PushbackInputStream pis)
			throws OBOEException, FileNotFoundException, IOException {
		Segment seg;

		StringBuilder sb = new StringBuilder(); // first char was already
												// read by
		// parser class
		byte me[] = new byte[4];
		if (pis.read(me) != 4) {
			throw new OBOEException("expected data not read");
		}

		sb.append(new String(me));
		String id = Util.rightTrim(sb.toString());

		if (id.equals("Service_String")) {
			seg = new Segment(
					getMyTemplate().getTemplateSegment(idServiceString), this);
			addSegment(seg);
			SegmentParser.parse(seg, pis);

			if (pis.read(me) != 4) {
				throw new OBOEException("expected data not read");
			}
			id = Util.rightTrim(new String(me));
		}

		if (id.compareTo(getMyTemplate().getTemplateSegment(idInterchangeHeader)
				.getID()) != 0) {
			throw new OBOEException("Expected " + getMyTemplate()
					.getTemplateSegment(idInterchangeHeader).getID() + " got "
					+ id);
		}

		seg = new Segment(
				getMyTemplate().getTemplateSegment(idInterchangeHeader), this);
		addSegment(seg);
		SegmentParser.parse(seg, pis);

		if (id.compareTo(getMyTemplate().getTemplateFunctionalGroup()
				.getTemplateSegment(EDIFACTFunctionalGroup.idHeader)
				.getID()) != 0) {

			FunctionalGroup functionalGroup = new EDIFACTFunctionalGroup(
					getMyTemplate().getTemplateFunctionalGroup(), this);
			addFunctionalGroup(functionalGroup);
			String tsID = Util.getOBOEProperty(id);
			if (tsID == null) {
				throw new OBOEException(
						id + " not defined in OBOE.properties file");
			}

			TransactionSet parsedTransactionSet = TransactionSetFactory
					.buildTransactionSet(tsID);
			parsedTransactionSet.setFormat(Format.EDIFACT_FORMAT);
			parsedTransactionSet.parse(pis);
			functionalGroup.addTransactionSet(parsedTransactionSet);
			parsedTransactionSet.setParent(functionalGroup);

			if (pis.read(me) != 4) {
				throw new OBOEException("expected data not read");
			}
			id = Util.rightTrim(new String(me));

		} else {
			while (id.compareTo(getMyTemplate()
					.getTemplateSegment(EDIFACTFunctionalGroup.idHeader)
					.getID()) == 0) {
				FunctionalGroup functionalGroup = new EDIFACTFunctionalGroup(
						getMyTemplate().getTemplateFunctionalGroup(), this);
				addFunctionalGroup(functionalGroup);
				seg = new Segment(getMyTemplate().getTemplateFunctionalGroup()
						.getTemplateSegment(EDIFACTFunctionalGroup.idHeader),
						functionalGroup);
				SegmentParser.parse(seg, pis);
				if (pis.read(me) != 4) {
					throw new OBOEException("expected data not read");
				}
				id = Util.rightTrim(new String(me));

				while (id.compareTo(getMyTemplate()
						.getTemplateSegment(EDIFACTFunctionalGroup.idTrailer)
						.getID()) != 0) {

					String tsID = Util.getOBOEProperty(id);
					if (tsID == null) {
						throw new OBOEException(
								id + " not defined in OBOE.properties file");
					}

					TransactionSet parsedTransactionSet = TransactionSetFactory
							.buildTransactionSet(tsID);
					parsedTransactionSet.setFormat(Format.EDIFACT_FORMAT);
					parsedTransactionSet.parse(pis);
					functionalGroup.addTransactionSet(parsedTransactionSet);
					parsedTransactionSet.setParent(functionalGroup);

					if (pis.read(me) != 4) {
						throw new OBOEException("expected data not read");
					}
					id = Util.rightTrim(new String(me));
				}
				seg = new Segment(getMyTemplate().getTemplateFunctionalGroup()
						.getTemplateSegment(EDIFACTFunctionalGroup.idTrailer),
						functionalGroup);
				SegmentParser.parse(seg, pis);
				if (pis.read(me) != 4) {
					throw new OBOEException("expected data not read");
				}
				id = Util.rightTrim(new String(me));
			}
		}

		if (id.compareTo(getMyTemplate()
				.getTemplateSegment(idInterchangeTrailer).getID()) != 0) {
			throw new OBOEException("Expecting " + getMyTemplate()
					.getTemplateSegment(idInterchangeTrailer).getID());
		}

		seg = new Segment(
				getMyTemplate().getTemplateSegment(idInterchangeTrailer), this);
		SegmentParser.parse(seg, pis);
		addSegment(seg);

	}

	/**
	 * creates a FunctionalGroup object
	 *
	 * @return EDIFACTFunctionalGroup
	 */
	@Override
	public FunctionalGroup createFunctionalGroup() {
		return new EDIFACTFunctionalGroup(
				getMyTemplate().getTemplateFunctionalGroup(), this);
	}

	/**
	 * gets the Interchange_Header
	 *
	 * @return Interchange_Header segment
	 */
	@Override
	public Segment getInterchange_Header() {
		return getSegment(idInterchangeHeader);
	}

	/**
	 * returns the Service String Advice Segment
	 *
	 * @return Segment
	 */

	public Segment getService_String() {
		return getSegment(idServiceString);
	}

	/**
	 * returns the Interchange Control Trailer built for the envelope
	 *
	 * @return Segment
	 */

	@Override
	public Segment getInterchange_Trailer() {
		return getSegment(idInterchangeTrailer);
	}

	/** sets the functional group count in the trailer */

	/** sets the Delimiter fields in the header */

	@Override
	public void setDelimitersInHeader() throws OBOEException {

		if (getInterchange_Header() == null) {
			throw new OBOEException("header not set yet");
		}

	}

	@Override
	public void setFGCountInTrailer() {

		getInterchange_Trailer().setDataElementValue(2,
				Integer.toString(getFunctionalGroupCount()));
	}

	/**
	 * returns the EDI (EDIFACT) formatted document in a String
	 *
	 * @param format - format type see TransactionSet
	 * @return String the formatted document
	 */

	@Override
	public String getFormattedText(Format inFormat) {

		Format format = inFormat;
		if (inFormat == Format.EDIFACT_FORMAT) {
			format = Format.PREBUILD_FORMAT;
		}

		StringBuilder sb = new StringBuilder();
		if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			sb.append("<!DOCTYPE envelope PUBLIC \"envelope\" "
					+ "\"https://raw.githubusercontent.com/EDIandXML/OBOE/main/envelope.dtd\">"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			sb.append("<envelope format=\"EDIFACT\"");
			sb.append(">" + io.github.EDIandXML.OBOE.util.Util.lineFeed);

		}
		if (format == Format.XML_FORMAT) {
			sb.append("<?xml version=\"1.0\"?>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			sb.append(
					"<Envelope>" + io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}

		if (format == Format.CSV_FORMAT) {
			sb.append("Envelope" + io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}

		Segment seg = getSegment(idServiceString);
		if (seg != null) {// not required
			sb.append(seg.getFormattedText(format));

		}

		seg = getSegment(idInterchangeHeader);
		sb.append(seg.getFormattedText(format));

		FunctionalGroup fg;
		for (int i = 0; i < getFunctionalGroupCount(); i++) {
			fg = getFunctionalGroup(i);
			sb.append(fg.getFormattedText(format));
		}

		seg = getSegment(idInterchangeTrailer);
		sb.append(seg.getFormattedText(format));
		if (format == Format.XML_FORMAT) {
			sb.append("</Envelope>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}
		if (format == Format.VALID_XML_FORMAT) {
			sb.append("</envelope>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}

		if (inFormat == Format.EDIFACT_FORMAT) {
			StringReader sr = new StringReader(sb.toString());
			StringWriter sw = new StringWriter();
			PreDelimiter pd = new PreDelimiter(sr, sw);
			pd.start();
			try {
				pd.join();
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
			return sw.toString();
		}

		return sb.toString();
	}

	/**
	 * @see io.github.EDIandXML.OBOE.Containers.Envelope#writeFormattedText(Writer,
	 *      Format)
	 */

	@Override
	public void writeFormattedText(Writer inWriter, Format inFormat)
			throws OBOEException, IOException {

		Writer writer = inWriter;
		PreDelimiter pd = null;
		Format format = inFormat;

		if (inFormat == Format.EDIFACT_FORMAT) {
			// temporarily set, will reset at end.
			format = Format.PREBUILD_FORMAT;
			PipedReader pr = new PipedReader();
			@SuppressWarnings("resource")
			PipedWriter pw = new PipedWriter(pr);
			writer = pw;
			pd = new Envelope.PreDelimiter(pr, inWriter);
			pd.start();

		}

		if (format == Format.VALID_XML_FORMAT) {
			writer.write("<?xml version=\"1.0\"?>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);

			writer.write("<envelope format=\"EDIFACT\""
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			writer.write(
					"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
							+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			writer.write(
					"  xsi:noNamespaceSchemaLocation=\"https://raw.githubusercontent.com/EDIandXML/OBOE/main/EDIRules.xsd\"");
			writer.write(">" + io.github.EDIandXML.OBOE.util.Util.lineFeed);

		}
		if (format == Format.XML_FORMAT) {
			writer.write("<?xml version=\"1.0\"?>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			writer.write(
					"<Envelope>" + io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}

		if (format == Format.CSV_FORMAT) {
			writer.write(
					"Envelope" + io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}
		Segment seg = getSegment(idServiceString);
		if (seg != null) {
			writer.write(seg.getFormattedText(format));
		}

		seg = getSegment(idInterchangeHeader);
		writer.write(seg.getFormattedText(format));

		FunctionalGroup fg;
		for (int i = 0; i < getFunctionalGroupCount(); i++) {
			fg = getFunctionalGroup(i);
			fg.writeFormattedText(writer, format);
		}

		seg = getSegment(idInterchangeTrailer);
		writer.write(seg.getFormattedText(format));
		if (format == Format.XML_FORMAT) {
			writer.write("</Envelope>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}
		if (format == Format.VALID_XML_FORMAT) {
			writer.write("</envelope>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}

		writer.flush();
		writer.close();

		if (pd != null) {
			try {
				pd.join();
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}

	}

	/**
	 * validates
	 * 
	 * @return
	 *
	 * @exception OBOEException indicates why envelope is invalid
	 */

	@Override
	public boolean validate() throws OBOEException {
		Segment seg;
		seg = getSegment(idServiceString);
		if (seg != null) {
			seg.validate();
		}

		seg = getSegment(idInterchangeHeader);
		if (seg == null) {
			throw new OBOEException(
					"Missing " + idInterchangeHeader + " Segment");
		} else {
			seg.validate();
		}

		seg.validate();

		FunctionalGroup fg;
		for (int i = 0; i < getFunctionalGroupCount(); i++) {
			fg = getFunctionalGroup(i);
			fg.validate();
		}

		seg = getSegment(idInterchangeTrailer);
		if (seg == null) {
			throw new OBOEException(
					"Missing " + idInterchangeTrailer + " Segment");
		} else {
			seg.validate();
		}
		return true;
	}

	/**
	 * validates and places errors in DocumentErrors object
	 *
	 * @param inDErr DocumentErrors
	 */

	@Override
	public void validate(DocumentErrors inDErr) throws OBOEException {
		testMissing(inDErr);

		Segment seg;
		seg = getSegment(idServiceString);
		if (seg != null) {
			seg.validate();
		}

		seg = getSegment(idInterchangeHeader);

		if (seg == null) {
			inDErr.addError(0, "Envelope",
					"Missing " + idInterchangeHeader + "  Segment", this, "1",
					this, ERROR_TYPE.Integrity);
		} else {
			seg.validate(inDErr);
		}

		FunctionalGroup fg;
		for (int i = 0; i < getFunctionalGroupCount(); i++) {
			fg = getFunctionalGroup(i);
			fg.validate(inDErr);
		}
		seg = getSegment(idInterchangeTrailer);
		if (seg == null) {
			inDErr.addError(0, "Envelope",
					"Missing " + idInterchangeTrailer + "  Segment", this, "1",
					this, ERROR_TYPE.Integrity);
		} else {
			seg.validate(inDErr);
		}
	}

	/**
	 * creates, sets and returns the Service String built for the envelope
	 *
	 * @return Segment - can return null if templateenvelope is null or
	 *         segment not defined in envelope message description file
	 */

	public Segment createService_String() {
		if (getMyTemplate() == null) {
			return null;
		}
		TemplateSegment tsg = getMyTemplate()
				.getTemplateSegment(EDIFACTEnvelope.idServiceString);
		if (tsg == null) {
			return null;
		}
		Segment seg = new Segment(tsg, this);
		addSegment(seg);
		return seg;
	}

	/**
	 * creates, sets and returns the Interchange Control Header built for
	 * the envelope
	 *
	 * @return Segment - can return null if templateenvelope is null or
	 *         segment not defined in envelope message description file
	 */

	public Segment createInterchange_Header() {

		if (getMyTemplate() == null) {
			return null;
		}
		TemplateSegment tsg = getMyTemplate()
				.getTemplateSegment(EDIFACTEnvelope.idInterchangeHeader);

		if (tsg == null) {
			return null;
		}
		Segment seg = new Segment(tsg, this);
		addSegment(seg);
		return seg;
	}

	/**
	 * creates, sets and returns the Interchange Control Trailer built for
	 * the envelope
	 *
	 * @return Segment - can return null if templateenvelope is null or
	 *         segment not defined in envelope message description file
	 */

	public Segment createInterchange_Trailer() {
		if (getMyTemplate() == null) {
			return null;
		}
		TemplateSegment tsg = getMyTemplate()
				.getTemplateSegment(EDIFACTEnvelope.idInterchangeTrailer);
		if (tsg == null) {
			return null;
		}
		Segment seg = new Segment(tsg, this);
		addSegment(seg);
		return seg;
	}

	public char getDelimiter(char inOriginal) {

		if (inOriginal == Envelope.EDIFACT_SEGMENT_DELIMITER.charAt(0)) {
			return segmentDelimiter.charAt(0);
		}

		if (inOriginal == Envelope.EDIFACT_GROUP_DELIMITER.charAt(0)) {
			return groupDelimiter.charAt(0);
		}

		if (inOriginal == Envelope.EDIFACT_FIELD_DELIMITER.charAt(0)) {
			return fieldDelimiter.charAt(0);
		}

		if (inOriginal == Envelope.EDIFACT_REPEAT_DELIMITER.charAt(0)) {
			return repeatDelimiter.charAt(0);
		}

		if (inOriginal == Envelope.EDIFACT_ESCAPE_CHARACTER.charAt(0)) {
			return escapeCharacter.charAt(0);
		}

		return inOriginal;
	}

}
