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

import java.io.IOException;
import java.io.PushbackInputStream;
import java.io.Writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.DataElements.Element;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateTransactionSet;
import io.github.EDIandXML.OBOE.Tokenizers.ITokenizer;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for all EDI Transaction Sets
 *
 * 
 */

public class TransactionSet extends MetaContainer implements IContainedObject {

	/**
	 * TransactioSet format
	 */
	Format format = Format.UNDEFINED;
	/**
	 * TransactionSet id (840, ORDER...)
	 */
	String id = "";
	/**
	 * TransactionSet name
	 */
	String name = "";
	/**
	 * TransactionSet revision (3040, D99A...
	 */
	String revision = "";
	/**
	 * TransactionSet functional group
	 */
	String functionalGroup = "";
	/**
	 * TransactionSet description
	 */
	String shortDescription = "";
	/**
	 * TransactionSet XML tag
	 */
	String shortName = "";

	/**
	 * TransactionSet tables three of them.
	 */

	Table headerTable = null, detailTable = null, summaryTable = null;

	private Segment headerSegment = null; // ST or UNH
	private Segment trailerSegment = null; // SE or UNT
	private boolean typeX12 = false;

	static Logger logr = LogManager.getLogger(TransactionSet.class);

	/**
	 * creates a transactionset object from a template
	 *
	 * @param inTemplateTransactionSet
	 * @param inParent                 owning Object
	 */
	public TransactionSet(TemplateTransactionSet inTemplateTransactionSet,
			IContainedObject inParent) {

		super(inTemplateTransactionSet);

		setParent(inParent);
		setFormat(((TemplateTransactionSet) getMyTemplate()).getFormat());
		setID(getMyTemplate().getID());

		if (getID().length() > 0) {
			typeX12 = Character.isDigit(getID().charAt(0));
		}

		setName(((TemplateTransactionSet) getMyTemplate()).getName());
		setRevision(((TemplateTransactionSet) getMyTemplate()).getRevision());
		setShortDescription(((TemplateTransactionSet) getMyTemplate())
				.getShortDescription());
		setShortName(((TemplateTransactionSet) getMyTemplate()).getShortName());
		setFunctionalGroup(((TemplateTransactionSet) getMyTemplate())
				.getFunctionalGroup());

		if (((TemplateTransactionSet) getMyTemplate())
				.getHeaderTemplateTable() != null) {
			headerTable = new Table(((TemplateTransactionSet) getMyTemplate())
					.getHeaderTemplateTable(), this);
		}
		if (((TemplateTransactionSet) getMyTemplate())
				.getDetailTemplateTable() != null) {
			detailTable = new Table(((TemplateTransactionSet) getMyTemplate())
					.getDetailTemplateTable(), this);
		}
		if (((TemplateTransactionSet) getMyTemplate())
				.getSummaryTemplateTable() != null) {
			summaryTable = new Table(((TemplateTransactionSet) getMyTemplate())
					.getSummaryTemplateTable(), this);
		}
	}

	/**
	 * parses an EDI Document from tokenized string
	 *
	 * @param TransactionTokenizedString input string containing all of the
	 *                                   transaction data pretokened by
	 *                                   OBOE.Tokenizer
	 *
	 * @exception OBOEException thrown when the transaction id string is
	 *                          incorrect
	 * @exception OBOEException thrown when an unknown segment id string is
	 *                          found
	 */
	// @Override
	@Override
	public boolean parse(ITokenizer TransactionTokenizedString)
			throws OBOEException {

		boolean used = false;

		if (headerTable != null) {
			used |= headerTable.parse(TransactionTokenizedString);
		}
		if (detailTable != null) {
			used |= detailTable.parse(TransactionTokenizedString);
		}
		if (summaryTable != null) {
			used |= summaryTable.parse(TransactionTokenizedString);
		}

		return used;
	}

	/**
	 * continues to parse an EDI Document after an error. Searches through
	 * the containers until last one container to make a good request is
	 * found. So the process can continue on.
	 *
	 * @param inContainer                last container used.
	 *
	 * @param TransactionTokenizedString input string containing all of the
	 *                                   transaction data pretokened by
	 *                                   OBOE.Tokenizer
	 *
	 * @return boolean - reparse started
	 *
	 * @exception OBOEException thrown when the transaction id string is
	 *                          incorrect
	 * @exception OBOEException thrown when an unknown segment id string is
	 *                          found
	 */
	// @Override
	// public boolean continueParse(SegmentContainer inContainer,
	// ITokenizer TransactionTokenizedString) throws OBOEException {
	// boolean restartSucessful = false;
	// if ((headerTable != null) && (restartSucessful == false)) {
	// restartSucessful = headerTable.continueParse(inContainer,
	// TransactionTokenizedString);
	// if (restartSucessful) {
	// /*
	// * when we come out of here the header table should be done so
	// * finish the work in the other tables
	// */
	// TransactionTokenizedString.resetSegment();
	// if (detailTable != null) {
	// detailTable.parse(TransactionTokenizedString);
	// }
	// if (summaryTable != null) {
	// summaryTable.parse(TransactionTokenizedString);
	// }
	// return true;
	// }
	// }
	// if ((detailTable != null) && (restartSucessful == false)) {
	// restartSucessful = detailTable.continueParse(inContainer,
	// TransactionTokenizedString);
	// if (restartSucessful) {
	// /*
	// * when we come out of here the header table should be done so
	// * finish the work in the summary tables
	// */
	// TransactionTokenizedString.resetSegment();
	// if (summaryTable != null) {
	// summaryTable.parse(TransactionTokenizedString);
	// }
	// return true;
	// }
	// }
	// if ((summaryTable != null) && (restartSucessful == false)) {
	// restartSucessful = summaryTable.continueParse(inContainer,
	// TransactionTokenizedString);
	// }
	//
	// return restartSucessful;
	// }

	/**
	 * parses a XML EDI Document from a DOM node.
	 *
	 * @param node XML Node element
	 *
	 * @exception OBOEException thrown when the transaction id string is
	 *                          incorrect
	 * @exception OBOEException thrown when an unknown segment id string is
	 *                          foundi
	 */
	@Override
	public void parse(Node node) throws OBOEException {
		Node cnode;
		NodeList nl = node.getChildNodes();
		int i;
		for (i = 0; i < nl.getLength(); i++) {

			cnode = nl.item(i);
			if (cnode.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			if (cnode.getNodeName().equals("header")) {
				headerTable.parse(cnode);
			} else if (cnode.getNodeName().equals("detail")) {
				detailTable.parse(cnode);
			} else if (cnode.getNodeName().equals("summary")) {
				summaryTable.parse(cnode);
			}
		}
	}

	/**
	 * 
	 * parses a FixedLength EDI Document from a Data Input Stream.**
	 * 
	 * @param pis PushbackInputStream**
	 * @exception OBOEException thrown when the transaction id string is
	 *                          incorrect
	 * @exception OBOEException thrown when an unknown segment id string is
	 *                          found
	 */

	@Override
	public void parse(PushbackInputStream pis)
			throws OBOEException, IOException {

		int idLen = 0;

		if (headerTable != null) {
			idLen = headerTable.getMyTemplate().getContainer().get(0).getID()
					.length();
			byte me[] = new byte[idLen];
			if (pis.read(me) != idLen) {
				throw new OBOEException("expected data not read");
			}
			String id = Util.rightTrim(new String(me));
			pis.unread(me);
			while (headerTable.getMyTemplate()
					.doYouWantThisSegment(id) == true) {
				headerTable.parse(pis);
				if (pis.read(me) != idLen) {
					throw new OBOEException("expected data not read");
				}
				id = Util.rightTrim(new String(me));
				pis.unread(me);
			}
		}
		if (detailTable != null) {
			idLen = detailTable.getMyTemplate().getContainer().get(0).getID()
					.length();

			byte me[] = new byte[idLen];
			if (pis.read(me) != idLen) {
				throw new OBOEException("expected data not read");
			}
			String id = Util.rightTrim(new String(me));
			pis.unread(me);
			while (detailTable.getMyTemplate()
					.doYouWantThisSegment(id) == true) {
				detailTable.parse(pis);
				if (pis.read(me) != idLen) {
					throw new OBOEException("expected data not read");
				}
				id = Util.rightTrim(new String(me));
				pis.unread(me);
			}
		}
		if (summaryTable != null) {
			idLen = summaryTable.getMyTemplate().getContainer().get(0).getID()
					.length();
			byte me[] = new byte[idLen];
			if (pis.read(me) != idLen) {
				throw new OBOEException("expected data not read");
			}
			String id = Util.rightTrim(new String(me));
			pis.unread(me);
			while (summaryTable.getMyTemplate()
					.doYouWantThisSegment(id) == true) {
				summaryTable.parse(pis);
				if (pis.read(me) != idLen) {
					throw new OBOEException("expected data not read");
				}
				id = Util.rightTrim(new String(me));
				pis.unread(me);
			}
		}
	}

	/**
	 * sets format for the Transaction Set <br>
	 * XML_FORMAT = 1; <br>
	 * X12_FORMAT = 2; <br>
	 * EDIFACT_FORMAT = 3; <br>
	 * VALID_XML_FORMAT = 4;
	 *
	 * @param inFormat int format
	 */
	public void setFormat(Format inFormat) {
		if ((inFormat == Format.XML_FORMAT) || (inFormat == Format.X12_FORMAT)
				|| (inFormat == Format.EDIFACT_FORMAT)
				|| (inFormat == Format.VALID_XML_FORMAT)
				|| (inFormat == Format.TRADACOMS_FORMAT)
				|| (inFormat == Format.ACH_FORMAT)) {
			format = inFormat;
		} else {
			if (inFormat != Format.UNDEFINED) {
				logr.error("Invalid transaction set format " + inFormat
						+ ".  Value set to undefined");
				Throwable t = new Throwable();
				StackTraceElement[] es = t.getStackTrace();
				for (StackTraceElement e : es) {
					logr.info(" in class:" + e.getClassName()
							+ " in source file:" + e.getFileName()
							+ " in method:" + e.getMethodName() + " at line:"
							+ e.getLineNumber() + " "
							+ (e.isNativeMethod() ? "native" : ""));
				}
			}
		}
	}

	/**
	 * sets id for the Transaction Set
	 *
	 * @param inId String transation set id
	 */
	public void setID(String inId) {
		id = inId;
		if (id.length() > 0) {
			typeX12 = Character.isDigit(id.charAt(0));
		}
	}

	/**
	 * sets name for the Transaction Set
	 *
	 * @param inName String transaction set name
	 */
	public void setName(String inName) {
		name = inName;
	}

	/**
	 * sets Revision for the Transaction Set
	 *
	 * @param inRevision String revision or version
	 */
	public void setRevision(String inRevision) {
		revision = inRevision;
	}

	/**
	 * sets Function Group for the Transaction Set
	 *
	 * @param inFunctionalGroup String functional group
	 */
	public void setFunctionalGroup(String inFunctionalGroup) {
		functionalGroup = inFunctionalGroup;
	}

	/**
	 * sets Short Description for the Transaction Set
	 *
	 * @param inDesc String description
	 */
	public void setShortDescription(String inDesc) {
		shortDescription = inDesc;
	}

	/**
	 * sets header table for the Transaction Set
	 *
	 * @param inTable Table
	 */
	public void setHeaderTable(Table inTable) {
		headerTable = inTable;
	}

	/**
	 * sets detail table for the Transaction Set
	 *
	 * @param inTable Table
	 */
	public void setDetailTable(Table inTable) {
		detailTable = inTable;
	}

	/**
	 * sets summary table for the Transaction Set
	 *
	 * @param inTable Table
	 */
	public void setSummaryTable(Table inTable) {
		summaryTable = inTable;
	}

	/**
	 * returns the Transaction Set format
	 *
	 * @return Format
	 */

	public Format getFormat() {
		return format;
	}

	/**
	 * returns the Transaction Set id
	 *
	 * @return String
	 */

	@Override
	public String getID() {
		return id;
	}

	/**
	 * returns name for the Transaction Set
	 *
	 * @return String
	 *
	 */

	public String getName() {
		return name;
	}

	/**
	 * returns revision value for the Transaction Set
	 *
	 * @return String
	 *
	 */

	public String getRevision() {
		return revision;
	}

	/**
	 * return Functional Group for the Transaction Set
	 *
	 * @return String
	 *
	 */

	public String getFunctionalGroup() {
		return functionalGroup;
	}

	/**
	 * returns the Short Description for the Transaction Set
	 *
	 * @return String
	 */
	public String getShortDescription() {
		if ((shortDescription == null) || (shortDescription.length() == 0)) {
			return id;
		}
		return shortDescription;
	}

	/**
	 * returns header table for the Transaction Set
	 *
	 * @return Table
	 *
	 */

	public Table getHeaderTable() {
		return headerTable;
	}

	/**
	 * returns detail table for the Transaction Set
	 *
	 * @return Table
	 *
	 */

	public Table getDetailTable() {
		return detailTable;
	}

	/**
	 * returns summary table for the Transaction Set
	 *
	 * @return Table
	 *
	 */

	public Table getSummaryTable() {
		return summaryTable;
	}

	/**
	 * sets the xml tag field
	 *
	 * @param inShortName String xml tag id
	 */

	public void setShortName(String inShortName) {
		shortName = inShortName;
	}

	/**
	 * returns the xml tag field
	 *
	 * @return String tag value
	 */

	@Override
	public String getShortName() {

		return shortName;
	}

	/**
	 * validates segment syntax for correct DataElements
	 *
	 * @exception OBOEException indicates why segment is invalid
	 */
	@Override
	public boolean validate() throws OBOEException {
		boolean validateResponse = true;
		if (headerTable != null) {
			validateResponse &= headerTable.validate();
		}

		if (detailTable != null) {
			validateResponse &= detailTable.validate();
		}

		if (summaryTable != null) {
			validateResponse &= summaryTable.validate();
		}

		setHeaderTrailer();

		if (typeX12) {
			Element de1 = headerSegment.getElement("329");
			if (trailerSegment == null) {
				throw new OBOEException("missing  SE segment");
			}
			Element de2 = trailerSegment.getElement("329");
			if ((de1 == null) || (de2 == null)) {
				throw new OBOEException(
						"missing data element 329 in either ST or SE");
			}

			if (de1.get().compareTo(de2.get()) != 0) {
				throw new OBOEException("Control Number Mismatch (329)");
			}
		}

		if (typeX12 && (trailerSegment != null)) {

			int saidCount = Integer
					.parseInt(trailerSegment.getElement("96").get());

			int readCount = getSegmentCount();
			if (saidCount != readCount) {
				throw new OBOEException(
						"Segment Count Mismatch.  Should Be " + readCount);
			}
		}

		return validateResponse;
	}

	/**
	 * validates segment syntax for correct DataElements <br>
	 * doesn't throw exception, places error text in DocumentErrors object
	 */
	@Override
	public void validate(DocumentErrors inDErr) throws OBOEException {

		if (headerTable != null) {
			headerTable.validate(inDErr);
		}

		if (detailTable != null) {
			detailTable.validate(inDErr);
		}

		if (summaryTable != null) {
			summaryTable.validate(inDErr);
		}

		setHeaderTrailer();

		if (typeX12) {
			if ((headerSegment != null) && (trailerSegment != null)) {
				Element de1 = headerSegment.getElement("329");
				if (de1 == null) {
					inDErr.addError(0, "ST", "Control Number Mismatch (329)",
							headerTable, "3", headerSegment,
							DocumentErrors.ERROR_TYPE.Integrity);
					return;
				}
				Element de2 = trailerSegment.getElement("329");
				if (de2 == null) {
					inDErr.addError(0, "ST", "Control Number Mismatch (329)",
							headerTable, "3", headerSegment,
							DocumentErrors.ERROR_TYPE.Integrity);
					return;
				}
				if ((de1.get().isEmpty()) || (de2.get().isEmpty())) {
					inDErr.addError(0, "ST", "Control Number Mismatch (329)",
							headerTable, "3", headerSegment,
							DocumentErrors.ERROR_TYPE.Integrity);
					return;
				}
				if (de1.get().compareTo(de2.get()) != 0) {
					inDErr.addError(0, "ST", "Control Number Mismatch (329)",
							headerTable, "3", headerSegment,
							DocumentErrors.ERROR_TYPE.Integrity);
				}
			}
		}

		if (typeX12 && (trailerSegment != null)) {

			int saidCount;
			try {
				saidCount = Integer
						.parseInt(trailerSegment.getElement("96").get());
			} catch (NumberFormatException nfe) {
				saidCount = 0;
			}

			int readCount = getSegmentCount();
			if (saidCount != readCount) {
				inDErr.addError(0, "96",
						"Segment Count Mismatch.  Should Be " + readCount, this,
						"7", trailerSegment.getElement("96"),
						DocumentErrors.ERROR_TYPE.Integrity);
			}
		}
		// if (getMyTemplate() != null) {
		// getMyTemplate().runValidatingMethod(this, inDErr);
		// }

	}

	/**
	 * returns the number of segments
	 *
	 * @return int count
	 */

	@Override
	public int getSegmentCount() {
		int segCount = 0;

		if (headerTable != null) {
			segCount += headerTable.getSegmentCount();
		}

		if (detailTable != null) {
			segCount += detailTable.getSegmentCount();
		}

		if (summaryTable != null) {
			segCount += summaryTable.getSegmentCount();
		}

		return segCount;
	}

	/**
	 * returns a formatted string of the transaction set
	 *
	 * @return String formattedOutput
	 * @param format int format type, x12, EDIFACT, xml...
	 *
	 */

	public String getFormattedText(Format format) {

		StringBuilder sbFormattedText = new StringBuilder();
		switch (format) {
		case XML_FORMAT:
			sbFormattedText.append('<' + getShortName() + " code=\"" + getID()
					+ "\">" + Util.lineFeed);
			break;
		case VALID_XML_FORMAT:
		case VALID_XML_FORMAT_WITH_POSITION:
			sbFormattedText.append("<transactionset code=\"" + getID() + "\"");
			sbFormattedText.append(" name=\"" + getName() + "\"");
			/*
			 * if (format == Format.EDIFACT_FORMAT) { Segment unh =
			 * this.getSegment("UNH"); String v =
			 * unh.getCompositeElement("S009").getDataElement("0052").get().trim
			 * ()+
			 * unh.getCompositeElement("S009").getDataElement("0054").get().trim
			 * (); { sbFormattedText.append(" version=\"" + v + "\""); } }
			 */
			sbFormattedText.append(">" + Util.lineFeed);
			break;

		case PREBUILD_FORMAT:
		case X12_FORMAT:
		case EDIFACT_FORMAT:
		case TRADACOMS_FORMAT:
			break;

		default:
			sbFormattedText
					.append("Transaction Set: " + getName() + Util.lineFeed);
		}
		if (headerTable != null) {
			sbFormattedText.append(headerTable.getFormattedText(format));
		}

		if (detailTable != null) {
			sbFormattedText.append(detailTable.getFormattedText(format));
		}

		if (summaryTable != null) {
			sbFormattedText.append(summaryTable.getFormattedText(format));
		}

		switch (format) {
		case XML_FORMAT:
			sbFormattedText.append("</" + getShortName() + ">" + Util.lineFeed);
			break;
		case VALID_XML_FORMAT:
		case VALID_XML_FORMAT_WITH_POSITION:

			sbFormattedText.append("</transactionset>" + Util.lineFeed);
			break;
		case X12_FORMAT:
		case EDIFACT_FORMAT:
		case TRADACOMS_FORMAT:
			break;

		default:
			break;
		}

		return new String(sbFormattedText);
	}

	/**
	 * like getFormattedText; writes to a Writer object instead of building
	 * a string.
	 *
	 * @param inWriter writer - object written to
	 * @param format   - format type see TransactionSet
	 * @exception OBOEException
	 * @exception IOException
	 */

	public void writeFormattedText(Writer inWriter, Format format)
			throws IOException {

		switch (format) {
		case XML_FORMAT:
			inWriter.write('<' + getShortName() + " code=\"" + getID() + "\">"
					+ Util.lineFeed);
			break;
		case VALID_XML_FORMAT:
		case VALID_XML_FORMAT_WITH_POSITION:
			inWriter.write("<transactionset code=\"" + getID() + "\"");
			inWriter.write(" name=\"" + getName() + "\">" + Util.lineFeed);
			break;

		case PREBUILD_FORMAT:
		case X12_FORMAT:
		case EDIFACT_FORMAT:
		case TRADACOMS_FORMAT:
			break;

		default:
			inWriter.write("Transaction Set: " + getName() + Util.lineFeed);
		}
		if (headerTable != null) {
			headerTable.writeFormattedText(inWriter, format);
		}

		if (detailTable != null) {
			detailTable.writeFormattedText(inWriter, format);
		}

		if (summaryTable != null) {
			summaryTable.writeFormattedText(inWriter, format);
		}

		switch (format) {
		case XML_FORMAT:
			inWriter.write("</" + getShortName() + ">" + Util.lineFeed);
			break;
		case VALID_XML_FORMAT:
		case VALID_XML_FORMAT_WITH_POSITION:

			inWriter.write("</transactionset>" + Util.lineFeed);
			break;
		case PREBUILD_FORMAT:
		case X12_FORMAT:
		case EDIFACT_FORMAT:
		case TRADACOMS_FORMAT:
			break;
		default:
			break;
		}

		inWriter.flush();
	}

	/**
	 * sets the SE (for X12) or UNT (for EDIFACT) control number and count
	 * fields
	 */

	public void setTrailerFields() {
		setHeaderTrailer();

		// if you don't have either header or trailer blow up
		if (headerSegment == null) {
			throw new OBOEException("Transaction set header segment not found");
		}
		if (trailerSegment == null) {
			throw new OBOEException(
					"Transaction set trailer segment not found");
		}

		if (typeX12) {
			trailerSegment.setDataElementValue("329",
					headerSegment.getElement("329").get());
			trailerSegment.setDataElementValue("96",
					Integer.toString(getSegmentCount()));
		} else {
			trailerSegment.setDataElementValue("0062",
					headerSegment.getElement("0062").get());
			trailerSegment.setDataElementValue("0074",
					Integer.toString(getSegmentCount()));
		}
	}

	/**
	 * sets the default value for the data elements <br>
	 * does nothing in Basic Edition, just a stub <br>
	 * will call tables useDefault methods, tables will create mandatory
	 * subsegments
	 */
	public void useDefault() {

		// if (getHeaderTable() != null) {
		// getHeaderTable().useDefault();
		// }
		// if (getDetailTable() != null) {
		// getDetailTable().useDefault();
		// }
		// if (getSummaryTable() != null) {
		// getSummaryTable().useDefault();
		// }

	}

	/**
	 * helper routine to setup segments for validation and default value
	 * setting
	 */

	private void setHeaderTrailer() {
		headerSegment = null;
		trailerSegment = null;
		if (getHeaderTable() != null) {
			try {
				if (typeX12) {
					headerSegment = getHeaderTable().getSegment("ST");
				} else {
					headerSegment = getHeaderTable().getSegment("UNH");
				}
			} catch (OBOEException oe) {
				;
			}

			try {
				if (typeX12) {
					trailerSegment = getHeaderTable().getSegment("SE");
				} else {
					trailerSegment = getHeaderTable().getSegment("UNT");
				}
			} catch (OBOEException oe) {
				;
			}
		}
		if (getDetailTable() != null) {

			try {
				if (typeX12) {
					trailerSegment = getDetailTable().getSegment("SE");
				} else {
					trailerSegment = getDetailTable().getSegment("UNT");
				}
			} catch (OBOEException oe) {
				;
			}
		}
		if (getSummaryTable() != null) {

			try {
				if (typeX12) {
					trailerSegment = getSummaryTable().getSegment("SE");
				} else {
					trailerSegment = getSummaryTable().getSegment("UNT");
				}
			} catch (OBOEException oe) {
				;
			}
		}

	}

	/**
	 * trims out unused segments and returns number of used segments
	 *
	 * @return int
	 */
	@Override
	public int trim() {
		int count = 0;

		if (headerTable != null) {
			count += headerTable.trim();
		}

		if (detailTable != null) {
			count += detailTable.trim();
		}

		if (summaryTable != null) {
			count += summaryTable.trim();
		}

		return count;

	}

	protected IContainedObject parent = null;

	/**
	 * sets parent attribute
	 *
	 * @param inParent TemplateSegmentContainer
	 */
	@Override
	public void setParent(IContainedObject inParent) {
		parent = inParent;
	}

	/**
	 * gets parent attribute
	 *
	 * @return TemplateSegmentContainer
	 */
	@Override
	public IContainedObject getParent() {
		return parent;
	}

	@Override
	public ContainerType getContainerType() {
		return ContainerType.TransactionSet;
	}

}
