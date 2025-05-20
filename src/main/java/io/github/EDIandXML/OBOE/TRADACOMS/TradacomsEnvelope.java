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


package io.github.EDIandXML.OBOE.TRADACOMS;

/**
 * OBOE - Open Business Objects for EDI
 * 
 * java access to EDI
 *
 * @author Joe McVerry
 * 
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.io.Writer;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.TransactionSetFactory;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Parsers.SegmentParser;
import io.github.EDIandXML.OBOE.Templates.TemplateEnvelope;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for wrapping a EDI transaction set within an EDI Envelope
 *
 */

public class TradacomsEnvelope extends Envelope {

	/** constants for segments */

	/** segment constants */
	public static String idInterchangeHeader = "STX";
	public static String idInterchangeTrailer = "END";

	/**
	 * instantiates the class from a TemplateEnvelope, creates mandatory
	 * segments STX and END and creates one emtpy functional group
	 *
	 * @param inTempEnv TemplateEnvelope to build this class with
	 * @exception OBOEException missing segment definition in envelope xml.
	 */

	public TradacomsEnvelope(TemplateEnvelope inTempEnv) throws OBOEException {
		super(inTempEnv);
		setFormat(Format.TRADACOMS_FORMAT);
		segmentDelimiter = Envelope.TRADACOMS_SEGMENT_DELIMITER;
		fieldDelimiter = Envelope.TRADACOMS_FIELD_DELIMITER;
		groupDelimiter = Envelope.TRADACOMS_GROUP_DELIMITER;
		repeatDelimiter = Envelope.TRADACOMS_REPEAT_DELIMITER;
		escapeCharacter = Envelope.TRADACOMS_ESCAPE_CHARACTER;

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
						.getTemplateSegment(TradacomsFunctionalGroup.idHeader)
						.getShortName()) != 0) {

			FunctionalGroup functionalGroup = new TradacomsFunctionalGroup(
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

				TransactionSet parsedTransactionSet = TransactionSetFactory
						.buildTransactionSet(tsID);
				parsedTransactionSet.setFormat(Format.TRADACOMS_FORMAT);
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
			} while (cnode.getNodeName()
					.compareTo(getMyTemplate()
							.getTemplateSegment(idInterchangeTrailer)
							.getShortName()) != 0);

		} else {
			while (cnode.getNodeName()
					.compareTo(getMyTemplate().getTemplateFunctionalGroup()
							.getTemplateSegment(
									TradacomsFunctionalGroup.idHeader)
							.getShortName()) == 0) {
				FunctionalGroup functionalGroup = new TradacomsFunctionalGroup(
						getMyTemplate().getTemplateFunctionalGroup(), this);
				addFunctionalGroup(functionalGroup);
				seg = new Segment(
						functionalGroup.getTemplateSegment(
								TradacomsFunctionalGroup.idHeader),
						functionalGroup);
				SegmentParser.parse(seg, cnode);
				functionalGroup.addSegment(seg);
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
										TradacomsFunctionalGroup.idTrailer)
								.getShortName()) != 0) {

					String tsID;
					try {
						tsID = Util.getOBOEProperty(cnode.getNodeName());
					} catch (IOException e) {
						throw new OBOEException(cnode.getNodeName()
								+ " not defined in OBOE.properties.");
					}

					TransactionSet parsedTransactionSet = TransactionSetFactory
							.buildTransactionSet(tsID);
					parsedTransactionSet.setFormat(Format.TRADACOMS_FORMAT);
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
				seg = new Segment(
						functionalGroup.getTemplateSegment(
								TradacomsFunctionalGroup.idTrailer),
						functionalGroup);
				SegmentParser.parse(seg, cnode);
				functionalGroup.addSegment(seg);
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

		if (cnode.getNodeName().compareTo(getMyTemplate()
				.getTemplateSegment(idInterchangeTrailer).getShortName()) != 0) {
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
				.getTemplateSegment(TradacomsFunctionalGroup.idHeader)
				.getID()) != 0) {

			FunctionalGroup functionalGroup = new TradacomsFunctionalGroup(
					getMyTemplate().getTemplateFunctionalGroup(), this);
			addFunctionalGroup(functionalGroup);
			String tsID = Util.getOBOEProperty(id);
			if (tsID == null) {
				throw new OBOEException(
						id + " not defined in OBOE.properties file");
			}

			TransactionSet parsedTransactionSet = TransactionSetFactory
					.buildTransactionSet(tsID);
			parsedTransactionSet.setFormat(Format.TRADACOMS_FORMAT);
			parsedTransactionSet.parse(pis);
			functionalGroup.addTransactionSet(parsedTransactionSet);
			parsedTransactionSet.setParent(functionalGroup);

			if (pis.read(me) != 4) {
				throw new OBOEException("expected data not read");
			}
			id = Util.rightTrim(new String(me));

		} else {
			while (id.compareTo(getMyTemplate()
					.getTemplateSegment(TradacomsFunctionalGroup.idHeader)
					.getID()) == 0) {
				FunctionalGroup functionalGroup = new TradacomsFunctionalGroup(
						getMyTemplate().getTemplateFunctionalGroup(), this);
				addFunctionalGroup(functionalGroup);
				seg = new Segment(getMyTemplate().getTemplateFunctionalGroup()
						.getTemplateSegment(TradacomsFunctionalGroup.idHeader),
						functionalGroup);
				SegmentParser.parse(seg, pis);
				if (pis.read(me) != 4) {
					throw new OBOEException("expected data not read");
				}
				id = Util.rightTrim(new String(me));

				while (id.compareTo(getMyTemplate()
						.getTemplateSegment(TradacomsFunctionalGroup.idTrailer)
						.getID()) != 0) {

					String tsID = Util.getOBOEProperty(id);
					if (tsID == null) {
						throw new OBOEException(
								id + " not defined in OBOE.properties file");
					}

					TransactionSet parsedTransactionSet = TransactionSetFactory
							.buildTransactionSet(tsID);
					parsedTransactionSet.setFormat(Format.TRADACOMS_FORMAT);
					parsedTransactionSet.parse(pis);
					functionalGroup.addTransactionSet(parsedTransactionSet);
					parsedTransactionSet.setParent(functionalGroup);

					if (pis.read(me) != 4) {
						throw new OBOEException("expected data not read");
					}
					id = Util.rightTrim(new String(me));
				}
				seg = new Segment(getMyTemplate().getTemplateFunctionalGroup()
						.getTemplateSegment(TradacomsFunctionalGroup.idTrailer),
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
	 * @return TradacomsFunctionalGroup
	 */
	@Override
	public FunctionalGroup createFunctionalGroup() {
		return new TradacomsFunctionalGroup(
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
	 * returns the Interchange Control Trailer built for the envelope
	 *
	 * @return Segment
	 */

	@Override
	public Segment getInterchange_Trailer() {
		return getSegment(idInterchangeTrailer);
	}

	/** sets the functional group count in the trailer */

	/** sets the Delimter fields in the header */

	@Override
	public void setDelimitersInHeader() throws OBOEException {

		if (getInterchange_Header() == null) {
			throw new OBOEException("header not set yet");
		}

	}

	@Override
	public void setFGCountInTrailer() {

		getInterchange_Trailer().setDataElementValue(2,
				Integer.toString((getFunctionalGroupCount())));
	}

	/**
	 * returns the EDI (Tradacoms) formatted document in a String
	 *
	 * @param format int - format type see TransactionSet
	 * @return String the formatted document
	 *
	 */

	@Override
	public String getFormattedText(Format format) {
		StringBuilder sb = new StringBuilder();
		if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			sb.append("<!DOCTYPE envelope PUBLIC \"envelope\" "
					+ "\"https://raw.githubusercontent.com/EDIandXML/OBOE/main/envelope.dtd\">"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			sb.append("<envelope format=\"Tradacoms\"");
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

		sb.append(getSegment(idInterchangeHeader).getFormattedText(format));

		FunctionalGroup fg;
		for (int i = 0; i < getFunctionalGroupCount(); i++) {
			fg = getFunctionalGroup(i);
			sb.append(fg.getFormattedText(format));
		}

		sb.append(getSegment(idInterchangeTrailer).getFormattedText(format));
		if (format == Format.XML_FORMAT) {
			sb.append("</Envelope>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}
		if (format == Format.VALID_XML_FORMAT) {
			sb.append("</envelope>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}
		return sb.toString();
	}

	/**
	 * @see io.github.EDIandXML.OBOE.Containers.Envelope#writeFormattedText(Writer,
	 *      Format)
	 */

	@Override
	public void writeFormattedText(Writer inWriter, Format format)
			throws OBOEException, IOException {

		if (format == Format.VALID_XML_FORMAT) {
			inWriter.write("<?xml version=\"1.0\"?>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);

			inWriter.write("<envelope format=\"Tradacoms\""
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			inWriter.write(
					"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
							+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			inWriter.write(
					"  xsi:noNamespaceSchemaLocation=\"https://raw.githubusercontent.com/EDIandXML/OBOE/main/EDIRules.xsd\"");
			inWriter.write(">" + io.github.EDIandXML.OBOE.util.Util.lineFeed);

		}
		if (format == Format.XML_FORMAT) {
			inWriter.write("<?xml version=\"1.0\"?>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			inWriter.write(
					"<Envelope>" + io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}

		if (format == Format.CSV_FORMAT) {
			inWriter.write(
					"Envelope" + io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}

		inWriter.write(
				getSegment(idInterchangeHeader).getFormattedText(format));

		FunctionalGroup fg;
		for (int i = 0; i < getFunctionalGroupCount(); i++) {
			fg = getFunctionalGroup(i);
			fg.writeFormattedText(inWriter, format);
		}

		inWriter.write(
				getSegment(idInterchangeTrailer).getFormattedText(format));
		if (format == Format.XML_FORMAT) {
			inWriter.write("</Envelope>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}
		if (format == Format.VALID_XML_FORMAT) {
			inWriter.write("</envelope>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}

		inWriter.flush();

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

		Segment seg = getSegment(idInterchangeHeader);
		if (seg != null) {
			seg.validate();
		}

		FunctionalGroup fg;
		for (int i = 0; i < getFunctionalGroupCount(); i++) {
			fg = getFunctionalGroup(i);
			fg.validate();
		}
		seg = getSegment(idInterchangeTrailer);
		if (seg != null) {
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

		Segment seg = getSegment(idInterchangeHeader);
		if (seg != null) {
			seg.validate(inDErr);
		}

		FunctionalGroup fg;
		for (int i = 0; i < getFunctionalGroupCount(); i++) {
			fg = getFunctionalGroup(i);
			fg.validate(inDErr);
		}

		seg = getSegment(idInterchangeTrailer);
		if (seg != null) {
			seg.validate(inDErr);
		}
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
				.getTemplateSegment(TradacomsEnvelope.idInterchangeHeader);

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
				.getTemplateSegment(TradacomsEnvelope.idInterchangeTrailer);
		Segment seg = new Segment(tsg, this);
		addSegment(seg);
		return seg;
	}

}
