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

package io.github.EDIandXML.OBOE.ach;

/**
 * OBOE - Open Business Objects for EDI
 * 
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.io.Writer;

import org.w3c.dom.Node;

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
import io.github.EDIandXML.OBOE.Templates.TemplateFunctionalGroup;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * @author Joe McVerry class for wrapping a ACH messages within an EDI
 *         Envelope
 *
 */
public class ACHEnvelope extends Envelope {

	public static String idHeader = "1"; // identifies the ACH header
	// record
	public static String idTrailer = "9"; // identifies the ACH trailer
	private String transID = ""; // indicates if CBR/PBR format.

	/**
	 * instantiates the class from a TemplateEnvelope,
	 *
	 * @param inTempEnv TemplateEnvelope to build this class with
	 * @exception OBOEException missing segment definition in envelope xml.
	 */

	public ACHEnvelope(TemplateEnvelope inTempEnv) throws OBOEException {
		super(inTempEnv);
		setFormat(Format.ACH_FORMAT);

		resetBatchHeader();

	}

	/**
	 * parses an xml formated file
	 *
	 * @see io.github.EDIandXML.OBOE.Containers.Envelope#parse(org.w3c.dom.Node)
	 */
	@Override
	public void parse(Node node) throws OBOEException {
		throw new OBOEException("method not available for ACH processing");
	}

	/**
	 * method for parsing fixed format edi files
	 *
	 * @param inpis PushbackInputStream
	 * @throws OBOException          ...
	 * @throws FileNotFoundException ...
	 * @throws IOException           ...
	 * @return boolean success
	 */
	@Override
	public void parse(PushbackInputStream inpis)
			throws OBOEException, FileNotFoundException, IOException {
		Segment seg;
		int i;

		// we need a bigger buffer to do this, so create another
		// pushbackinputstream using the original
		PushbackInputStream pis = new PushbackInputStream(inpis, 94 * 5);

		byte me[] = new byte[94 * 5];
		if (pis.read(me) != (94 * 5)) {
			throw new OBOEException("expected data not read");
		}
		for (i = 0; i < (94 * 5); i += 94) {
			if (me[i] != '5') {
				continue;
			}
			transID = new String(me, i + 50, 3);
			break;
		}

		if ((transID.equals("CBR")) || (transID.equals("PBR"))) {
			rebuildBatchHeader();
		}

		pis.unread(me);

		me = new byte[1];
		if (pis.read(me) != 1) {
			throw new OBOEException("expected data not read");
		}
		String id = new String(me, 0, 1);

		if (Util.rightTrim(id).compareTo(
				getMyTemplate().getTemplateSegment(idHeader).getID()) != 0) {
			throw new OBOEException("Expecting "
					+ getMyTemplate().getTemplateSegment(idHeader).getID()
					+ " got " + id);
		}

		seg = new Segment(getMyTemplate().getTemplateSegment(idHeader), this);
		addSegment(seg);
		SegmentParser.parse(seg, pis);

		if (pis.read(me) != 1) {
			throw new OBOEException("expected data not read");
		}

		id = new String(me, 0, 1);

		while ((getMyTemplate().getTemplateFunctionalGroup()
				.getTemplateSegment(ACHFunctionalGroup.idHeader) != null)
				&& (id.compareTo(getMyTemplate().getTemplateFunctionalGroup()
						.getTemplateSegment(ACHFunctionalGroup.idHeader)
						.getID()) == 0)) {
			FunctionalGroup functionalGroup = new ACHFunctionalGroup(
					getMyTemplate().getTemplateFunctionalGroup(), this);
			addFunctionalGroup(functionalGroup);
			seg = functionalGroup
					.createAndAddSegment(ACHFunctionalGroup.idHeader);
			SegmentParser.parse(seg, pis);
			if (pis.read(me) != 1) {
				throw new OBOEException("expected data not read");
			}
			id = Util.rightTrim(new String(me));

			while (id.compareTo("6") == 0) {
				pis.unread(me);

				TransactionSet parsedTransactionSet = TransactionSetFactory
						.buildTransactionSet("ACH");
				parsedTransactionSet.setFormat(Format.ACH_FORMAT);
				parsedTransactionSet.parse(pis);
				functionalGroup.addTransactionSet(parsedTransactionSet);
				parsedTransactionSet.setParent(functionalGroup);

				if (pis.read(me) != 1) {
					throw new OBOEException("expected data not read");
				}
				id = Util.rightTrim(new String(me));
			}

			// pis.unread(me);
			seg = functionalGroup
					.createAndAddSegment(ACHFunctionalGroup.idTrailer);
			SegmentParser.parse(seg, pis);

			if (pis.read(me) != 1) {
				throw new OBOEException("expected data not read");
			}
			id = Util.rightTrim(new String(me));

		}

		if (id.compareTo(
				getMyTemplate().getTemplateSegment(idTrailer).getID()) != 0) {
			throw new OBOEException("Expecting "
					+ getMyTemplate().getTemplateSegment(idTrailer).getID()
					+ " got " + id);
		}

		seg = new Segment(getMyTemplate().getTemplateSegment(idTrailer), this);
		SegmentParser.parse(seg, pis);
		addSegment(seg);

	}

	public void resetBatchHeader() { // reset fg batch header record, if
		// necessary
		TemplateFunctionalGroup tfg = getMyTemplate()
				.getTemplateFunctionalGroup();
		TemplateSegment tseg = tfg.getTemplateSegment("+");
		if (tseg == null) {
			return;
		}
		tseg = tfg.getTemplateSegment(ACHFunctionalGroup.idHeader);
		tseg.setID("-");
		tseg = tfg.getTemplateSegment("+");
		tseg.setID(ACHFunctionalGroup.idHeader);

	}

	public void rebuildBatchHeader() { // rebuild fg batch header record
		TemplateFunctionalGroup tfg = getMyTemplate()
				.getTemplateFunctionalGroup();
		TemplateSegment tseg = tfg
				.getTemplateSegment(ACHFunctionalGroup.idHeader);
		tseg.setID("+");
		tseg = tfg.getTemplateSegment("-");
		tseg.setID(ACHFunctionalGroup.idHeader);

	}

	/**
	 * @see io.github.EDIandXML.OBOE.Containers.Envelope#getFormattedText(int)
	 *
	 */
	@Override
	public String getFormattedText(Format format) throws OBOEException {

		StringBuilder sb = new StringBuilder();
		if (format == Format.CSV_FORMAT) {
			sb.append("Envelope" + io.github.EDIandXML.OBOE.util.Util.lineFeed);
		} else if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			sb.append("<!DOCTYPE envelope PUBLIC \"envelope\" "
					+ "\"https://raw.githubusercontent.com/EDIandXML/OBOE/main/envelope.dtd\">"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			sb.append("<envelope format=\""
					+ ((transID.equals("CBR")) || (transID.equals("PBR"))
							? "ACH-CBR-PBR"
							: "ACH")
					+ "\"");
			sb.append(">" + io.github.EDIandXML.OBOE.util.Util.lineFeed);

			sb.append("<?xml version=\"1.0\"?>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);

			sb.append("<envelope format=\"");
		} else if (format == Format.XML_FORMAT) {
			sb.append("<?xml version=\"1.0\"?>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			sb.append("<Envelope");
			if ((transID.equals("CBR")) || (transID.equals("PBR"))) {
				sb.append(" format=\"ACH-CBR-PBR\"");
			}
			sb.append("\"" + io.github.EDIandXML.OBOE.util.Util.lineFeed);
			sb.append(
					"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
							+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			sb.append(
					"  xsi:noNamespaceSchemaLocation=\"https://raw.githubusercontent.com/EDIandXML/OBOE/main/EDIRules.xsd\"");
			sb.append(">" + io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}

		int i;
		Segment s = getSegment(idHeader);

		sb.append(s.getFormattedText(format));
		FunctionalGroup fg;
		for (i = 0; i < getFunctionalGroupCount(); i++) {
			fg = getFunctionalGroup(i);
			sb.append(fg.getFormattedText(format));
		}
		s = getSegment(idTrailer);
		sb.append(s.getFormattedText(format));

		if (format == Format.XML_FORMAT) {
			sb.append("</Envelope>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}
		if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			sb.append("</envelope>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}
		return sb.toString();
	}

	/**
	 * @see io.github.EDIandXML.OBOE.Containers.Envelope#writeFormattedText(Writer,
	 *      int)
	 */
	@Override
	public void writeFormattedText(Writer inWriter, Format format)
			throws OBOEException, IOException {

		if (format == Format.CSV_FORMAT) {
			inWriter.write(
					"Envelope" + io.github.EDIandXML.OBOE.util.Util.lineFeed);
		} else if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			inWriter.write("<?xml version=\"1.0\"?>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);

			if ((transID.equals("CBR")) || (transID.equals("PBR"))) {
				inWriter.write("<envelope format=\"ACH-CBR-PBR\""
						+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			} else {
				inWriter.write("<envelope format=\"ACH\""
						+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			}
			inWriter.write(
					"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
							+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			inWriter.write(
					"  xsi:noNamespaceSchemaLocation=\"https://raw.githubusercontent.com/EDIandXML/OBOE/main/EDIRules.xsd\"");
			inWriter.write(">" + io.github.EDIandXML.OBOE.util.Util.lineFeed);

		} else if (format == Format.XML_FORMAT) {
			inWriter.write("<?xml version=\"1.0\"?>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
			inWriter.write(
					"<Envelope>" + io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}

		int i;
		Segment s = getSegment(idHeader);
		inWriter.write(s.getFormattedText(format));
		FunctionalGroup fg;
		for (i = 0; i < getFunctionalGroupCount(); i++) {
			fg = getFunctionalGroup(i);
			fg.writeFormattedText(inWriter, format);
		}
		s = getSegment(idTrailer);
		inWriter.write(s.getFormattedText(format));

		if (format == Format.XML_FORMAT) {
			inWriter.write("</Envelope>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}
		if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			inWriter.write("</envelope>"
					+ io.github.EDIandXML.OBOE.util.Util.lineFeed);
		}

		inWriter.flush();
	}

	/**
	 * @see io.github.EDIandXML.OBOE.Containers.Envelope#validate()
	 */
	@Override
	public boolean validate() throws OBOEException {

		Segment s = getSegment(idHeader);
		if (s != null) {
			s.validate();
		}

		FunctionalGroup fg;
		for (int i = 0; i < getFunctionalGroupCount(); i++) {
			fg = getFunctionalGroup(i);
			fg.validate();
		}

		s = getSegment(idTrailer);

		if (s != null) {
			s.validate();
		}
		return true;
	}

	@Override
	public void validate(DocumentErrors inDErr) {

		testMissing(inDErr);

		Segment s = getSegment(idHeader);
		if (s != null) {
			s.validate(inDErr);
		}

		FunctionalGroup fg;
		for (int i = 0; i < getFunctionalGroupCount(); i++) {
			fg = getFunctionalGroup(i);
			fg.validate(inDErr);
		}

		s = getSegment(idTrailer);

		if (s != null) {
			s.validate(inDErr);
		}

	}

	/**
	 * @see io.github.EDIandXML.OBOE.Containers.Envelope#setDelimitersInHeader()
	 */
	@Override
	public void setDelimitersInHeader() throws OBOEException {
		throw new OBOEException("Method not available for ACH processing");
	}

	/**
	 * @see io.github.EDIandXML.OBOE.Containers.Envelope#setFGCountInTrailer()
	 */
	@Override
	public void setFGCountInTrailer() throws OBOEException {
		throw new OBOEException("ask developement to add code for this method");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.EDIandXML.OBOE.Envelope#getInterchange_Header()
	 */
	@Override
	public Segment getInterchange_Header() {

		return getSegment(idHeader);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.EDIandXML.OBOE.Envelope#getInterchange_Trailer()
	 */
	@Override
	public Segment getInterchange_Trailer() {
		return getSegment(idTrailer);
	}

	/**
	 * creates a functional group object
	 *
	 * @return X12FunctionalGroup
	 */
	@Override
	public FunctionalGroup createFunctionalGroup() {
		return new ACHFunctionalGroup(
				getMyTemplate().getTemplateFunctionalGroup(), this);
	}

}
