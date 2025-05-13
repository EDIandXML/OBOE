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

package io.github.EDIandXML.OBOE.Containers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.github.EDIandXML.OBOE.EDIDocumentHandler;
import io.github.EDIandXML.OBOE.EnvelopeFactory;
import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.EDIFACT.EDIFACTDocumentHandler;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.TRADACOMS.TradacomsEnvelope;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;
import io.github.EDIandXML.OBOE.ach.ACHEnvelope;
import io.github.EDIandXML.OBOE.util.Util;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;

/**
 * class for wrapping an EDI transaction set within an EDI Envelope
 */

public abstract class Envelope extends MetaContainer
		implements IContainedObject {

	protected Format format = Format.UNDEFINED;

	/**
	 * revision (3040, D99A...
	 */
	protected String revision = "";

	/**
	 * response envelope if this is outgoing then it's the receiver response
	 * if it's not outgoing then it is response to this incoming envelope
	 */
	protected Envelope response = null;

	/** constant for segment delimiter */

	public static String PREBUILD_SEGMENT_DELIMITER = "\ufff0";
	public static String X12_SEGMENT_DELIMITER = "\n";
	public static String EDIFACT_SEGMENT_DELIMITER = "'";
	public static String TRADACOMS_SEGMENT_DELIMITER = "'";

	public String segmentDelimiter = "";

	/** constant for field delimiter */
	public static String PREBUILD_FIELD_DELIMITER = "\ufff1";
	public static String X12_FIELD_DELIMITER = "*";
	public static String EDIFACT_FIELD_DELIMITER = "+";
	public static String TRADACOMS_FIELD_DELIMITER = "+";
	public String fieldDelimiter = "";

	/** constant for group delimiter */
	public static String PREBUILD_GROUP_DELIMITER = "\ufff2";
	public static String X12_GROUP_DELIMITER = "<";
	public static String EDIFACT_GROUP_DELIMITER = ":";
	public static String TRADACOMS_GROUP_DELIMITER = ":";
	public String groupDelimiter = "";

	/** constant for element repeat character */
	public static String PREBUILD_REPEAT_DELIMITER = "\ufff3";
	public static String X12_REPEAT_DELIMITER = "\u0000";
	public static String EDIFACT_REPEAT_DELIMITER = "?";
	public static String TRADACOMS_REPEAT_DELIMITER = "?";
	public String repeatDelimiter = "";

	/** constant for element escape character */
	public static String PREBUILD_ESCAPE_DELIMITER = "\ufff4";
	public static String X12_ESCAPE_CHARACTER = "";
	public static String EDIFACT_ESCAPE_CHARACTER = "?";
	public static String TRADACOMS_ESCAPE_CHARACTER = "?";
	public String escapeCharacter = "";

	/** TRADACOMS has one more */
	public static String PREBUILD_SEGID_DELIMITER = "\ufff5";
	public static String TRADACOMS_SEGID_DELIMITER = "=";

	/** log4j object */

	static Logger logr = LogManager.getLogger(Envelope.class);

	/**
	 * instantiates the class from all related OBOE classes
	 */

	protected Envelope(MetaTemplateContainer template) {
		super(template);

	}

	/**
	 * sets status to deliveredPendingReply and the statusDate for
	 * deliveredPendingReply to current Calendar and class
	 * setLastProcessDate
	 */

	private String delimiterType = "unknown";

	/**
	 * sets the segment, field, group (CompositeElement) delimiters from the
	 * properties file. <br>
	 * if you need to use different delimiters, call this method prior to
	 * the first getFormattedText call. You only need to call it once since
	 * all delimiters are defined as class variables and as such the method
	 * contains logic so that it only works on the first method call. Uses
	 * property x12Delimiters. x12Delimiters format is 3 characters
	 * <ol>
	 * <li>segment
	 * <li>field
	 * <li>group
	 * </ol>
	 * If property is not found it uses the default
	 * <p>
	 * in OBOE.properties files add the following <br>
	 * &nbps; x12Delimiters="!@#" <br>
	 * &nbps; EDIFACTDelimiters="..."
	 * <p>
	 * at this time no exceptions are thrown, all exceptions are caught and
	 * written to the System.err file.
	 */
	public void setDelimitersFromProperties() {

		try {
			String delimiters = Util.getOBOEProperty(delimiterType);

			if (delimiters == null) {
				return;
			}
			setDelimiters(delimiters);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * sets the segment, field, group (CompositeElement) delimiters <br>
	 * if you need to use different delimiters, call this method prior to
	 * the first getFormattedText call. You only need to call it once since
	 * all delimiters are defined as class variables
	 *
	 * @param inDelimiters String format is 3 or 4 or 5 characters
	 *                     <ol>
	 *                     <li>segment
	 *                     <li>field
	 *                     <li>group
	 *                     <li>repeat
	 *                     <li>escape
	 *                     </ol>
	 */
	public void setDelimiters(String inDelimiters) {

		if ((inDelimiters.length() != 3) && (inDelimiters.length() != 4)
				&& (inDelimiters.length() != 5)) {
			logr.error("set delimiters (" + inDelimiters + ") length ("
					+ inDelimiters.length()
					+ ") is incorrect. Using defaults.");
			return;
		}

		/** constant for segment delimiter */
		segmentDelimiter = inDelimiters.substring(0, 1);

		/** constant for field delimiter */
		fieldDelimiter = inDelimiters.substring(1, 2);

		/** constant for group delimiter */
		groupDelimiter = inDelimiters.substring(2, 3);

		if (inDelimiters.length() == 4) {
			repeatDelimiter = inDelimiters.substring(3, 4);
		}

		if (inDelimiters.length() == 5) {
			escapeCharacter = inDelimiters.substring(4, 5);
		}

	}

	/*
	 * method for parsing well formed edi xml files
	 *
	 * @param node a DOM node object
	 *
	 * @throws OBOException...
	 *
	 * 
	 */

	@Override
	public abstract void parse(Node node) throws OBOEException;

	/*
	 * method for parsing fixed format edi files
	 *
	 * @param DataInputStream
	 *
	 * @throws OBOException...
	 *
	 */

	@Override
	public abstract void parse(PushbackInputStream pis)
			throws OBOEException, FileNotFoundException, IOException;

	/**
	 * @return a formatted document in a String
	 * @param format int - format type see TransactionSet
	 * @return String the formatted document
	 * @exception OBOEException
	 *
	 */

	public abstract String getFormattedText(Format format) throws OBOEException;

	/**
	 * like getFormattedText; writes to a Writer object instead of building
	 * a string.
	 *
	 * @param inWriter writer - object written to
	 * @param format   int - format type see TransactionSet
	 * @exception OBOEException
	 * @exception IOException
	 */

	public abstract void writeFormattedText(Writer inWriter, Format format)
			throws OBOEException, IOException;

	/**
	 * validate contents of envelope
	 * 
	 * @return
	 *
	 * @exception OBOEException
	 */
	@Override
	public abstract boolean validate() throws OBOEException;

	/**
	 * validate contents of envelope <br>
	 * doesn't throw exceptions but places errors in DocumentErrors object
	 */
	@Override
	public abstract void validate(DocumentErrors inDErr);

	/**
	 * gets the Interchange Header
	 *
	 * @return Segment
	 */
	public abstract Segment getInterchange_Header();

	/**
	 * gets the Interchange Trailer
	 *
	 * @return Segment
	 */
	public abstract Segment getInterchange_Trailer();

	/**
	 * creates a basic functionalgroup object
	 *
	 * @return FunctionalGroup
	 */
	public abstract FunctionalGroup createFunctionalGroup();

	/**
	 * adds a Functional Group object to the ArrayList (container);
	 *
	 * @param inFunctionalGroup a functional Group object
	 */
	public void addFunctionalGroup(FunctionalGroup inFunctionalGroup) {
		addContainerDontUseID(inFunctionalGroup);
	}

	/**
	 * gets the count of Functional Group object in the ArrayList
	 * (container);
	 *
	 * @return int count
	 */

	public int getFunctionalGroupCount() {
		var fgs = getFunctionalGroups();
		if (fgs == null) {
			return 0;
		}
		return fgs.size();
	}

	/**
	 * gets a Functional Group object from the ArrayList (container); check
	 * for runtime array out of bounds exception
	 *
	 * @return a FunctionalGroup object
	 */

	public FunctionalGroup getFunctionalGroup(int pos) {
		return (FunctionalGroup) getFunctionalGroups().get(pos);
	}

	/**
	 * sets the format attribute
	 *
	 * @param inFormat - int
	 */
	public void setFormat(Format inFormat) {
		format = inFormat;

		// let's set the delimiters while we are here

		switch (format) {
		case X12_FORMAT:
			delimiterType = "x12Delimiters";
			break;
		case EDIFACT_FORMAT:
			delimiterType = "EDIFACTDelimiters";
			break;
		case TRADACOMS_FORMAT:
			delimiterType = "TradacomsDelimiters";
			break;
		default:

		}

	}

	/**
	 * gets the format attribute
	 *
	 * @return int
	 */
	public Format getFormat() {
		return format;
	}

	/**
	 * gets the format of an well formed xml envelope by peeking into the
	 * envelope
	 *
	 * @param node DOM Node
	 * @return int
	 * @exception OBOEException an envelope node not passed
	 * @exception OBOEException incorrect interchange header node.
	 */
	public static Format getFormat(Node node) throws OBOEException {
		if (node.getNodeName().equals("Envelope") == false) {
			throw new OBOEException("Envelope node not passed");
		}

		NodeList nl = node.getChildNodes();
		int i = 0;
		for (i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeName().compareTo("#text") == 0) {
				continue;
			}
			if (nl.item(i).getNodeName().compareTo("#comment") == 0) {
				continue;
			}
			break;
		}
		String firstNodeName = nl.item(i).getNodeName();

		switch (firstNodeName) {
		case "InterchangeControlHeader":
			return Format.X12_FORMAT;
		case "Service_String":
		case "InterchangeHeader":
			// EDIFACT can have different type of headers
			return Format.EDIFACT_FORMAT;
		case "StartTransmission":
			return Format.TRADACOMS_FORMAT;

		case "FileHeaderRecord":
			return Format.ACH_FORMAT;

		default:
			throw new OBOEException(
					"Incorrect Interchange Header Node, what is "
							+ firstNodeName);
		}

	}

	/**
	 * sets the revision attribute
	 *
	 * @param inRevision - String
	 */

	public void setRevision(String inRevision) {
		revision = inRevision;
	}

	/**
	 * gets the revision attribute
	 *
	 * @return String
	 */
	public String getRevision() {
		return revision;
	}

	/**
	 * sets the respons attribute
	 *
	 * @param inResponse - Envelope
	 */
	public void setResponse(Envelope inResponse) {
		response = inResponse;
	}

	/**
	 * gets the response attribute
	 *
	 * @return Envelope
	 */

	public Envelope getResponse() {
		return response;
	}

	/** sets the Delimiter fields in the header */

	public abstract void setDelimitersInHeader() throws OBOEException;

	/** sets the functional group count in the trailer */

	public abstract void setFGCountInTrailer() throws OBOEException;

	/**
	 * method of SegmentContainer interface <br>
	 * Envelope's segments built from instance methods so we return a null
	 * and hope the call can figure it out
	 *
	 * @param inID - template segment to get
	 * @return TemplateSegment - in this case a null
	 */
	public TemplateSegment getTemplateSegment(String inID) {
		return null;
	}

	/**
	 * returns the ID which is "envelope". <br>
	 * required for SegmentContainer interface
	 */

	@Override
	public String getID() {
		return "envelope";
	}

	@Override
	public void setParent(IContainedObject inParent) {
		;
	}

	@Override
	public IContainedObject getParent() {
		return null;
	}

	@Override
	public String getShortName() {
		return "Envelope";
	}

	public static Envelope processEDIEnvelope(InputStream inStream)
			throws OBOEException, IOException {
		EDIDocumentHandler dh = null;
		Envelope env = null;
		PushbackInputStream pbis = new PushbackInputStream(inStream, 5 * 94);
		byte me[] = new byte[94];
		if (pbis.read(me) != 94) {
			throw new OBOEException("expected data not found");
		}
		switch (me[0]) {
		case 'I':
			pbis.unread(me);
			dh = new X12DocumentHandler();
			dh.startParsing(new InputStreamReader(pbis));
			return ((X12DocumentHandler) dh).getEnvelope();
		case 'U':
			pbis.unread(me);
			dh = new EDIFACTDocumentHandler();
			dh.startParsing(new InputStreamReader(pbis));
			return ((EDIFACTDocumentHandler) dh).getEnvelope();
		case 'S':
			pbis.unread(me);
			env = new TradacomsEnvelope(EnvelopeFactory
					.buildEnvelope("tradacoms.envelope", "notSetYet"));
			env.parse(pbis);
			return env;
		case '1':
			byte me2[] = new byte[94];
			if (pbis.read(me2) != 94) {
				throw new OBOEException("expected data not read");
			}
			pbis.unread(me2);
			if (me2[0] == '5') {
				pbis.unread(me);
				String transID = new String(me, 50, 3);
				env = new ACHEnvelope(EnvelopeFactory.buildEnvelope(
						"ach." + transID + ".envelope", "notSetYet"));
				env.parse(pbis);
				return env;
			}

		default:
			break;
		}

		return null;
	}

	/**
	 * the toString method
	 */
	@Override
	public String toString() {
		return "envelope  format:" + getFormat() + " id:" + getID() + " rev:"
				+ getRevision();

	}

	@Override
	public int trim() {
		int cnt = 0;
		for (int i = 0; i < this.getFunctionalGroupCount(); i++) {
			cnt += this.getFunctionalGroup(i).trim();
		}
		return cnt;
	}

	protected class PreDelimiter extends Thread {
		Reader r;
		Writer w;

		public PreDelimiter(Reader inPR, Writer inW) {
			r = inPR;
			w = inW;
		}

		@Override
		public void run() {
			try {
				int i;
				char c;
				boolean escTest = false;
				while ((i = r.read()) > -1) {
					c = (char) i;

					if ((escTest == true) && (escapeCharacter.length() > 0))
					// don't escape before first segment
					// only need to do this if we need to
					// add escape character most likely tradacoms or EDIFACT
					{
						if ((c == segmentDelimiter.charAt(0))
								|| (c == groupDelimiter.charAt(0))
								|| (c == fieldDelimiter.charAt(0))
								|| (c == escapeCharacter.charAt(0))
								|| ((repeatDelimiter.length() > 0)
										&& (c == repeatDelimiter.charAt(0)))) {
							w.write(escapeCharacter.charAt(0));
						}
					}

					if (c == Envelope.PREBUILD_SEGMENT_DELIMITER.charAt(0)) {
						w.write(segmentDelimiter.charAt(0));
						escTest = true;
					} else if (c == Envelope.PREBUILD_GROUP_DELIMITER
							.charAt(0)) {
						w.write(groupDelimiter.charAt(0));
					} else if (c == Envelope.PREBUILD_FIELD_DELIMITER
							.charAt(0)) {
						w.write(fieldDelimiter.charAt(0));
					} else if ((escapeCharacter.length() > 0)
							&& (c == escapeCharacter.charAt(0))) {
						w.write(repeatDelimiter.charAt(0));
					} else if ((repeatDelimiter.length() > 0)
							&& (c == Envelope.PREBUILD_REPEAT_DELIMITER
									.charAt(0))) {
						w.write(repeatDelimiter.charAt(0));
					} else {
						if (c == Envelope.PREBUILD_REPEAT_DELIMITER.charAt(0)) {
							logr.error(
									"found prebuild repeat delimiter but not enveleope repeat delimiter was set or found");
						}
						w.write(i);
					}
				}
				w.flush();
				w.close();
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

		}

	}

	@Override
	public ContainerType getContainerType() {
		return ContainerType.Envelope;
	}
}
