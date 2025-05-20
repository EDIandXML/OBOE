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

package io.github.EDIandXML.OBOE.x12;

/**
 * OBOE - Open Business Objects for EDI
 * 
 * java access to EDI
 *
 * @author Joe McVerry
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
import io.github.EDIandXML.OBOE.Containers.MetaTemplateContainer;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.DataElements.Element;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Parsers.SegmentParser;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for wrapping a X12 EDI transaction set within an EDI Envelope
 *
 */

public class X12Envelope extends Envelope {

	/** segment constants */
	public static String idInterchangeHeader = "ISA";
	public static String idGradeofServiceRequest = "ISB";
	public static String idInterchangeAcknowledgment = "TA1";
	public static String idDeferredDeliveryRequest = "ISE";
	public static String idInterchangeTrailer = "IEA";

	/**
	 * instantiates the class from a TemplateEnvelope, creates mandatory
	 * segments ISA and IEA and creates one emtpy functional group
	 *
	 * @param inTempEnv TemplateEnvelope to build this class with
	 * @exception OBOEException missing segment definition in envelope xml.
	 */

	public X12Envelope(MetaTemplateContainer template) throws OBOEException {
		super(template);
		setFormat(Format.X12_FORMAT);

		segmentDelimiter = Envelope.X12_SEGMENT_DELIMITER;
		fieldDelimiter = Envelope.X12_FIELD_DELIMITER;
		groupDelimiter = Envelope.X12_GROUP_DELIMITER;
		repeatDelimiter = Envelope.X12_REPEAT_DELIMITER;
		escapeCharacter = Envelope.X12_ESCAPE_CHARACTER;

	}

	/*
	 * method for parsing well formed edi xml files
	 *
	 * @param node a DOM node object
	 *
	 * @throws OBOException...
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
			throw new OBOEException("Expecting " + getMyTemplate()
					.getTemplateSegment(idInterchangeHeader).getShortName());
		}

		seg = createAndAddSegment(idInterchangeHeader);

		SegmentParser.parse(seg, cnode);

		String gd = seg.getElement("I15").get();
		if (gd.compareTo(Envelope.X12_GROUP_DELIMITER) != 0) {

			String t = Envelope.X12_SEGMENT_DELIMITER
					+ Envelope.X12_FIELD_DELIMITER + gd
					+ Envelope.X12_REPEAT_DELIMITER
					+ Envelope.X12_ESCAPE_CHARACTER;
			this.setDelimiters(t);
		}

		for (i++; i < nl.getLength(); i++) {
			cnode = nl.item(i);
			if (cnode.getNodeType() == Node.ELEMENT_NODE) {
				break;
			}
		}

		if (i == nl.getLength()) {
			throw new OBOEException("Envelope terminated too soon");
		}

		if ((getMyTemplate()
				.getTemplateSegment(idGradeofServiceRequest) != null)
				&& (cnode.getNodeName()
						.compareTo(getMyTemplate()
								.getTemplateSegment(idGradeofServiceRequest)
								.getShortName()) == 0)) {
			seg = createAndAddSegment(idGradeofServiceRequest);
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

		if ((getMyTemplate()
				.getTemplateSegment(idDeferredDeliveryRequest) != null)
				&& (cnode.getNodeName()
						.compareTo(getMyTemplate()
								.getTemplateSegment(idGradeofServiceRequest)
								.getShortName()) == 0)) {
			seg = new Segment(getMyTemplate()
					.getTemplateSegment(idDeferredDeliveryRequest), this);
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

		if ((getMyTemplate()
				.getTemplateSegment(idInterchangeAcknowledgment) != null)
				&& (cnode.getNodeName()
						.compareTo(getMyTemplate()
								.getTemplateSegment(idInterchangeAcknowledgment)
								.getShortName()) == 0)) {

			do {
				seg = new Segment(getMyTemplate()
						.getTemplateSegment(idInterchangeAcknowledgment), this);
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

			} while (cnode.getNodeName()
					.compareTo(getMyTemplate()
							.getTemplateSegment(idInterchangeAcknowledgment)
							.getShortName()) == 0);
		}

		while ((getMyTemplate().getTemplateFunctionalGroup()
				.getTemplateSegment(X12FunctionalGroup.idHeader) != null)
				&& (cnode.getNodeName()
						.compareTo(getMyTemplate().getTemplateFunctionalGroup()
								.getTemplateSegment(X12FunctionalGroup.idHeader)
								.getShortName()) == 0)) {
			FunctionalGroup functionalGroup = new X12FunctionalGroup(
					getMyTemplate().getTemplateFunctionalGroup(), this);
			addFunctionalGroup(functionalGroup);
			seg = functionalGroup.buildHeaderSegment();

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

			while ((getMyTemplate().getTemplateFunctionalGroup()
					.getTemplateSegment(X12FunctionalGroup.idTrailer) != null)
					&& (cnode.getNodeName()
							.compareTo(getMyTemplate()
									.getTemplateFunctionalGroup()
									.getTemplateSegment(
											X12FunctionalGroup.idTrailer)
									.getShortName()) != 0)) {
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
						.buildTransactionSet(tsID, null,
								functionalGroup
										.getSegment(X12FunctionalGroup.idHeader)
										.getElement("480").get(),
								getSegment(X12Envelope.idInterchangeHeader)
										.getElement("I07").get(),
								getSegment(X12Envelope.idInterchangeHeader)
										.getElement("I06").get(),
								getSegment(X12Envelope.idInterchangeHeader)
										.getElement("I14").get());

				parsedTransactionSet.setFormat(Format.X12_FORMAT);
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

			seg = functionalGroup.buildTrailerSegment();
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
		int i;

		byte me[] = new byte[3];
		i = pis.read(me);
		if (i != 3) {
			throw new OBOEException("parser fail to read segment id");
		}

		String id = new String(me);

		if (id.compareTo(getMyTemplate().getTemplateSegment(idInterchangeHeader)
				.getID()) != 0) {
			throw new OBOEException("Expecting " + getMyTemplate()
					.getTemplateSegment(idInterchangeHeader).getID() + " got "
					+ id);
		}

		seg = new Segment(
				getMyTemplate().getTemplateSegment(idInterchangeHeader), this);
		addSegment(seg);
		SegmentParser.parse(seg, pis);

		me = new byte[3];
		i = pis.read(me);
		if (i != 3) {
			throw new OBOEException("parser fail to read segment id");
		}

		id = Util.rightTrim(new String(me));

		if ((getMyTemplate()
				.getTemplateSegment(idGradeofServiceRequest) != null)
				&& (id.compareTo(getMyTemplate()
						.getTemplateSegment(idGradeofServiceRequest)
						.getID()) == 0)) {
			seg = new Segment(
					getMyTemplate().getTemplateSegment(idGradeofServiceRequest),
					this);
			addSegment(seg);
			SegmentParser.parse(seg, pis);
			if (pis.read(me) != 2) {
				throw new OBOEException(
						"parsing failed to read requested data");
			}
			id = Util.rightTrim(new String(me));
		}

		if ((getMyTemplate()
				.getTemplateSegment(idDeferredDeliveryRequest) != null)
				&& (id.compareTo(getMyTemplate()
						.getTemplateSegment(idGradeofServiceRequest)
						.getID()) == 0)) {
			seg = new Segment(getMyTemplate()
					.getTemplateSegment(idDeferredDeliveryRequest), this);
			addSegment(seg);
			SegmentParser.parse(seg, pis);
			if (pis.read(me) != 2) {
				throw new OBOEException(
						"parsing failed to read requested data");
			}
			id = Util.rightTrim(new String(me));
		}

		if ((getMyTemplate()
				.getTemplateSegment(idInterchangeAcknowledgment) != null)
				&& (id.compareTo(getMyTemplate()
						.getTemplateSegment(idInterchangeAcknowledgment)
						.getID()) == 0)) {

			do {
				seg = new Segment(getMyTemplate()
						.getTemplateSegment(idInterchangeAcknowledgment), this);
				addSegment(seg);
				SegmentParser.parse(seg, pis);
				if (pis.read(me) != 2) {
					throw new OBOEException(
							"parsing failed to read requested data");
				}

				id = Util.rightTrim(new String(me));

			} while (id.compareTo(getMyTemplate()
					.getTemplateSegment(idInterchangeAcknowledgment)
					.getID()) == 0);
		}

		pis.unread(me);
		me = new byte[2];
		if (pis.read(me) != 2) {
			throw new OBOEException("parsing failed to read requested data");
		}
		id = Util.rightTrim(new String(me));

		while ((getMyTemplate().getTemplateFunctionalGroup()
				.getTemplateSegment(X12FunctionalGroup.idHeader) != null)
				&& (id.compareTo(getMyTemplate().getTemplateFunctionalGroup()
						.getTemplateSegment(X12FunctionalGroup.idHeader)
						.getID()) == 0)) {
			FunctionalGroup functionalGroup = new X12FunctionalGroup(
					getMyTemplate().getTemplateFunctionalGroup(), this);
			addFunctionalGroup(functionalGroup);
			seg = new Segment(getMyTemplate().getTemplateSegment(
					X12FunctionalGroup.idHeader), functionalGroup);
			functionalGroup.addSegment(seg);

			SegmentParser.parse(seg, pis);
			if (pis.read(me) != 2) {
				throw new OBOEException(
						"parsing failed to read requested data");
			}

			id = Util.rightTrim(new String(me));
			functionalGroup.addSegment(seg);

			while (id.equals("ST")) {

				byte tsme[] = new byte[3];
				if (pis.read(tsme) != 3) {
					throw new OBOEException(
							"parsing failed to read requested data");
				}
				pis.unread(tsme);
				pis.unread(me);

				String tsID = Util.rightTrim(new String(tsme));

				TransactionSet parsedTransactionSet = TransactionSetFactory
						.buildTransactionSet(tsID);
				parsedTransactionSet.setFormat(Format.X12_FORMAT);
				parsedTransactionSet.parse(pis);
				functionalGroup.addTransactionSet(parsedTransactionSet);
				parsedTransactionSet.setParent(functionalGroup);

				if (pis.read(me) != 2) {
					throw new OBOEException(
							"parsing failed to read requested data");
				}

				id = Util.rightTrim(new String(me));
			}

			seg = new Segment(getMyTemplate().getTemplateSegment(
					X12FunctionalGroup.idHeader), functionalGroup);
			functionalGroup.addSegment(seg);
			SegmentParser.parse(seg, pis);
			functionalGroup.addSegment(seg);
			if (pis.read(me) != 2) {
				throw new OBOEException(
						"parsing failed to read requested data");
			}
			id = Util.rightTrim(new String(me));

		}
		pis.unread(me);
		me = new byte[3];
		if (pis.read(me) != 3) {
			throw new OBOEException("parsing failed to read requested data");
		}

		id = new String(me);

		if (id.compareTo(getMyTemplate()
				.getTemplateSegment(idInterchangeTrailer).getID()) != 0) {
			throw new OBOEException("Expecting " + getMyTemplate()
					.getTemplateSegment(idInterchangeTrailer).getID() + " got "
					+ id);
		}

		seg = new Segment(
				getMyTemplate().getTemplateSegment(idInterchangeTrailer), this);
		SegmentParser.parse(seg, pis);

		addSegment(seg);

		return;
	}

	/**
	 * creates a functional group object
	 *
	 * @return X12FunctionalGroup
	 */
	@Override
	public FunctionalGroup createFunctionalGroup() {
		return new X12FunctionalGroup(
				getMyTemplate().getTemplateFunctionalGroup(), this);
	}

	/**
	 * returns the Interchange Control Header built for the envelope
	 *
	 * @return Segment
	 */

	@Override
	public Segment getInterchange_Header() {
		return getSegment(idInterchangeHeader);
	}

	/**
	 * gets the Grade_of_Service_Request
	 *
	 * @return Interchange_Control_Header segment
	 */
	public Segment getGrade_of_Service_Request() {
		return getSegment(idGradeofServiceRequest);
	}

	/**
	 * gets the Deferred_Delivery_Request
	 *
	 * @return Interchange_Control_Header segment
	 */
	public Segment getDeferred_Delivery_Request() {
		return getSegment(idDeferredDeliveryRequest);
	}

	/**
	 * gets the count of Interchange_Acknowledgment in the
	 * ArrayList(container)
	 *
	 * @return int count
	 */

	public int getInterchange_AcknowledgmentCount() {
		return getSegmentCount(idInterchangeAcknowledgment);
	}

	/**
	 * gets a Interchange_Acknowledgment from the ArrayList(container) check
	 * for runtime array out of bounds exception
	 *
	 * @return Interchange_Acknowledgment Segment
	 */

	public Segment getInterchange_Acknowledgment(int pos) {
		return getSegment(idInterchangeAcknowledgment, pos);
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
				.getTemplateSegment(idInterchangeHeader);
		if (tsg == null) {
			return null;
		}
		Segment seg = new Segment(tsg, this);
		this.addSegment(seg);
		return seg;
	}

	/**
	 * creates, sets and returns the Grade_of_Service_Request
	 *
	 * @return Segment - can return null if templateenvelope is null or
	 *         segment not defined in envelope message description file
	 */
	public Segment createGrade_of_Service_Request() {
		if (getMyTemplate() == null) {
			return null;
		}
		TemplateSegment tsg = getMyTemplate()
				.getTemplateSegment(idGradeofServiceRequest);
		if (tsg == null) {
			return null;
		}
		Segment seg = new Segment(tsg, this);

		return seg;
	}

	/**
	 * creates, sets and returns the Deferred_Delivery_Request
	 *
	 * @return Segment - can return null if templateenvelope is null or
	 *         segment not defined in envelope message description file
	 */
	public Segment createDeferred_Delivery_Request() {
		if (getMyTemplate() == null) {
			return null;
		}
		TemplateSegment tsg = getMyTemplate()
				.getTemplateSegment(idDeferredDeliveryRequest);
		if (tsg == null) {
			return null;
		}
		Segment seg = new Segment(tsg, this);

		return seg;
	}

	/**
	 * creates, adds and returns a Interchange_Acknowledgment from the
	 * ArrayList(container) check for runtime array out of bounds exception
	 *
	 * @return Segment - can return null if templateenvelope is null or
	 *         segment not defined in envelope message description file
	 */

	public Segment createInterchange_Acknowledgment() {
		if (getMyTemplate() == null) {
			return null;
		}
		TemplateSegment tsg = getMyTemplate()
				.getTemplateSegment(idInterchangeAcknowledgment);
		if (tsg == null) {
			return null;
		}
		Segment seg = new Segment(tsg, this);

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
				.getTemplateSegment(idInterchangeTrailer);
		if (tsg == null) {
			return null;
		}
		Segment seg = new Segment(tsg, this);
		this.addSegment(seg);

		return seg;
	}

	/** sets the Delimiter fields in the header */

	@Override
	public void setDelimitersInHeader() throws OBOEException {

		if (getInterchange_Header() == null) {
			throw new OBOEException("header not set yet");
		}

		if ((repeatDelimiter.charAt(0) != '\u0000') && (repeatDelimiter
				.equals(Envelope.PREBUILD_REPEAT_DELIMITER) == false)) {
			getInterchange_Header().setDataElementValue("I10", repeatDelimiter);

		}

		getInterchange_Header().setDataElementValue("I15", this.groupDelimiter);

	}

	/**
	 * sets the functional group count in the trailer. also sets the control
	 * number from header
	 */

	@Override
	public void setFGCountInTrailer() throws OBOEException {

		if (getInterchange_Trailer() == null) {
			throw new OBOEException("trailer not set yet");
		}

		getInterchange_Trailer().setDataElementValue("I12",
				getInterchange_Header().getElement("I12").get());

		getInterchange_Trailer().setDataElementValue("I16",
				Integer.toString(getFunctionalGroupCount()));
	}

	/**
	 * returns a formatted document as a String
	 *
	 * @param inFormat int - format type see TransactionSet
	 * @return String the formatted document
	 *
	 */

	@Override
	public String getFormattedText(Format inFormat) {

		Format format = inFormat;

		if (inFormat == Format.X12_FORMAT) {
			// temporarily set, will reset at end.
			format = Format.PREBUILD_FORMAT;
		}

		StringBuilder sb = new StringBuilder();
		if (format == Format.CSV_FORMAT) {
			sb.append("Envelope" + io.github.EDIandXML.OBOE.util.Util.lineFeed);
		} else if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			sb.append("<?xml version=\"1.0\"?>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			sb.append("<!DOCTYPE envelope PUBLIC \"envelope\" "
					+ "\"https://raw.githubusercontent.com/EDIandXML/OBOE/main/envelope.dtd\">"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			sb.append("<envelope format=\"X12\"");
			sb.append(">" + io.github.EDIandXML.OBOE.util.Util.lineFeed);

		} else if (format == Format.XML_FORMAT) {
			sb.append("<?xml version=\"1.0\"?>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			sb.append(
					"<Envelope>" + io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}
		Segment seg = getSegment(idInterchangeHeader);
		if (format == Format.PREBUILD_FORMAT) {
			// don't forget to set the group (Component Element Separator)
			// delimiter to the prebuild format
			seg.setDataElementValue("I15", Envelope.PREBUILD_GROUP_DELIMITER);
			if (repeatDelimiter.charAt(0) != '\u0000') {
				seg.setDataElementValue("I10",
						Envelope.PREBUILD_REPEAT_DELIMITER);
			}

		}

		sb.append(seg.getFormattedText(format));

		seg = getSegment(idGradeofServiceRequest);
		if (seg != null) {
			sb.append(seg.getFormattedText(format));
		}

		seg = getSegment(idDeferredDeliveryRequest);
		if (seg != null) {
			sb.append(seg.getFormattedText(format));
		}

		int i;
		for (i = 0; i < getInterchange_AcknowledgmentCount(); i++) {
			seg = getSegment(idInterchangeAcknowledgment, i);
			sb.append(seg.getFormattedText(format));
		}

		FunctionalGroup fg;
		for (i = 0; i < getFunctionalGroupCount(); i++) {
			fg = getFunctionalGroup(i);
			sb.append(fg.getFormattedText(format));
		}
		seg = getSegment(idInterchangeTrailer);
		if (seg == null) {
			sb.append(io.github.EDIandXML.OBOE.util.Util.lineFeed
					+ "missing IEA segment"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		} else {
			sb.append(seg.getFormattedText(format));
		}
		if (format == Format.XML_FORMAT) {
			sb.append("</Envelope>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}
		if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			sb.append("</envelope>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}

		if (inFormat == Format.X12_FORMAT) {
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
	 *      int)
	 */

	@Override
	public void writeFormattedText(Writer inWriter, Format inFormat)
			throws OBOEException, IOException {

		Writer writer = inWriter;
		Format format = inFormat;
		PreDelimiter pd = null;

		if (inFormat == Format.X12_FORMAT) {
			// temporarily set, will reset at end.
			format = Format.PREBUILD_FORMAT;
			PipedReader pr = new PipedReader();
			@SuppressWarnings("resource")
			PipedWriter pw = new PipedWriter(pr);
			writer = pw;
			pd = new Envelope.PreDelimiter(pr, inWriter);
			pd.start();

		}

		if (format == Format.CSV_FORMAT) {
			writer.write(
					"Envelope" + io.github.EDIandXML.OBOE.util.Util.lineFeed);
		} else if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			writer.write("<?xml version=\"1.0\"?>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			writer.write("<envelope format=\"X12\""
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			writer.write(
					"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
							+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			writer.write(
					"  xsi:noNamespaceSchemaLocation=\"https://raw.githubusercontent.com/EDIandXML/OBOE/main/EDIRules.xsd\"");
			writer.write(">" + io.github.EDIandXML.OBOE.util.Util.lineFeed);

		} else if (format == Format.XML_FORMAT) {
			writer.write("<?xml version=\"1.0\"?>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			writer.write(
					"<Envelope>" + io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}
		Segment seg = getSegment(idInterchangeHeader);
		writer.write(seg.getFormattedText(format));

		seg = getSegment(idGradeofServiceRequest);
		if (seg != null) {
			writer.write(seg.getFormattedText(format));
		}

		seg = getSegment(idDeferredDeliveryRequest);
		if (seg != null) {
			writer.write(seg.getFormattedText(format));
		}

		int i;

		for (i = 0; i < getInterchange_AcknowledgmentCount(); i++) {
			seg = getSegment(idInterchangeAcknowledgment, i);
			writer.write(seg.getFormattedText(format));
		}

		FunctionalGroup fg;
		for (i = 0; i < getFunctionalGroupCount(); i++) {
			fg = getFunctionalGroup(i);
			fg.writeFormattedText(writer, format);
		}
		seg = getSegment(idInterchangeTrailer);
		if (seg == null) {
			writer.write(io.github.EDIandXML.OBOE.util.Util.lineFeed
					+ "missing IEA segment"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		} else {
			writer.write(seg.getFormattedText(format));
		}

		if (format == Format.XML_FORMAT) {
			writer.write("</Envelope>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}

		if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
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
		Segment seg, seghdr;

		seg = getSegment(idInterchangeHeader);
		if (seg == null) {
			throw new OBOEException("Missing ISA Segment");
		} else {
			seg.validate();
		}

		seghdr = seg;

		seg = getSegment(idGradeofServiceRequest);
		if (seg != null) {
			seg.validate();
		}

		seg = getSegment(idDeferredDeliveryRequest);
		if (seg != null) {
			seg.validate();
		}

		int i;

		boolean ta1Found = false;
		for (i = 0; i < getInterchange_AcknowledgmentCount(); i++) {
			ta1Found = true;
			seg = getSegment(idInterchangeAcknowledgment, i);
			seg.validate();
		}

		FunctionalGroup fg;
		if ((getFunctionalGroupCount() == 0) && (ta1Found == false)) {
			throw new OBOEException("No functional groups");
		} else {
			for (i = 0; i < getFunctionalGroupCount(); i++) {
				fg = getFunctionalGroup(i);
				fg.validate();
			}
		}

		seg = getSegment(idInterchangeTrailer);
		if (seg == null) {
			throw new OBOEException("Missing IEA Segment");
		} else {
			seg.validate();
			if (seghdr != null) {
				Element de1 = seghdr.getElement("I12");
				Element de2 = seg.getElement("I12");
				// if either is null then it's been reported by previous
				// validate calls
				if ((de1 != null) && (de2 != null)) {
					if (de1.get().compareTo(de2.get()) != 0) {
						throw new OBOEException(
								"Control number mismatch (I12)");
					}

				}
			}
		}

		return true;

	}

	/**
	 * validate contents of envelope doesn't throw exception but places
	 * errors in DocumentErrors object.
	 *
	 * @param inDErr DocumentErrors object
	 */
	@Override
	public void validate(DocumentErrors inDErr) {

		boolean hNoErr = false, tNoErr = false;
		Segment seg, seghdr;

		seg = getSegment(idInterchangeHeader);
		if (seg == null) {
			inDErr.addError(0, "Envelope", "Missing ISA Segment", this, "1",
					this, DocumentErrors.ERROR_TYPE.Integrity);
		} else {
			hNoErr = seg.validate(inDErr);
		}

		seghdr = seg;

		seg = getSegment(idGradeofServiceRequest);
		if (seg != null) {
			seg.validate(inDErr);
		}

		seg = getSegment(idDeferredDeliveryRequest);
		if (seg != null) {
			seg.validate(inDErr);
		}

		int i;

		boolean ta1Found = false;
		for (i = 0; i < getInterchange_AcknowledgmentCount(); i++) {
			seg = getSegment(idInterchangeAcknowledgment, i);
			seg.validate(inDErr);
			ta1Found = true;
		}

		FunctionalGroup fg;
		if ((getFunctionalGroupCount() == 0) && (ta1Found == false)) {
			inDErr.addError(0, "Envelope", "No functional groups", this, "3",
					this, DocumentErrors.ERROR_TYPE.Integrity);
		} else {
			for (i = 0; i < getFunctionalGroupCount(); i++) {
				fg = getFunctionalGroup(i);
				fg.validate(inDErr);
			}
		}

		seg = getSegment(idInterchangeTrailer);
		if (seg == null) {
			inDErr.addError(0, "Envelope", "Missing IEA Segment", this, "3",
					this, DocumentErrors.ERROR_TYPE.Integrity);
		} else {
			tNoErr = seg.validate(inDErr);
			if ((hNoErr == true) && (tNoErr == true) && (seghdr != null)) {
				Element de1 = seghdr.getElement("I12");
				Element de2 = seg.getElement("I12");
				// if either is null then it's been reported by previous
				// validate calls
				if ((de1 != null) && (de2 != null)) {
					if ((de1.get() != null) && (de2.get() != null)) {
						if (de1.get().compareTo(de2.get()) != 0) {
							inDErr.addError(0, idInterchangeHeader,
									"Control number mismatch (I12)", this, "3",
									seghdr,
									DocumentErrors.ERROR_TYPE.Integrity);
						}
					}

				}
			}
		}

	}

}
