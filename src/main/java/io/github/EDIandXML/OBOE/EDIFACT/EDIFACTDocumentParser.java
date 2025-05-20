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

import java.io.Reader;
import java.io.StringReader;

import io.github.EDIandXML.OBOE.EnvelopeFactory;
import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.TransactionSetFactory;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.Table;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.DataElements.CompositeElement;
import io.github.EDIandXML.OBOE.DataElements.Element;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors.ERROR_TYPE;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Parsers.EDIDocumentParser;
import io.github.EDIandXML.OBOE.Parsers.SegmentParser;
import io.github.EDIandXML.OBOE.Templates.TemplateEnvelope;
import io.github.EDIandXML.OBOE.Tokenizers.Tokenizer;

/**
 * class defining methods for parsing EDI Documents EDIFACT2 dependent
 * Document handlers will register with this class to be notified when
 * specific edi objects are created or finished Unlike the old parser
 * these parsers will not contain the objects, the process of adding
 * objects to owning parents (such as adding functional groups to an
 * envelope) is left up to the document handler.
 */

public class EDIFACTDocumentParser extends EDIDocumentParser {

	/**
	 * parses an EDIFACT Document and passes results to EDIDocumentHandlers
	 */
	public EDIFACTDocumentParser() {
		super();
	}

	/**
	 * method that controls the parsing
	 *
	 * @param inString String edi document
	 * @return boolean - true - continue or false - halted
	 * @exception OBOEException - most likely unknown segment
	 */
	@Override
	public boolean parseDocument(String inString) throws OBOEException {

		int posStart = inString.indexOf("UNA");
		int posStartUNB = inString.indexOf("UNB");
		if ((posStart > -1) && (posStart < posStartUNB)) {
			;
		} else {
			posStart = posStartUNB;
			if (posStart < 0) {
				throw new OBOEException("UNB (and UNA) segment missing");
			}
		}
		int posStop = inString.indexOf("UNZ");
		if (posStop < 0) {
			throw new OBOEException("UNZ segment missing");
		}

		return parseDocument(new StringReader(inString), true);
	}

	/**
	 * method that controls the parsing
	 *
	 * @param inReader   inReader reader object containing edi data
	 * @param inValidate boolean - call validation logic on envelope if true
	 * @return boolean - true - continue or false - halted if false is used
	 *         then don't forget to call the validation method on the
	 *         envelope object pass it the documenterrors object created
	 *         here and getable using the getDocumentErrors method.
	 *
	 * @exception OBOEException - most likely unknown segment
	 */

	@Override
	public boolean parseDocument(Reader inReader, boolean inValidate)
			throws OBOEException {

		resetWhyHaltParser();
		TransactionSet parsedTransactionSet = null;
		EDIFACTEnvelope envelope;

		TemplateEnvelope te = EnvelopeFactory.buildEnvelope("EDIFACT.envelope",
				"notSetYet");
		envelope = new EDIFACTEnvelope(te);

		notifyStartEnvelope(envelope);
		if (halted()) {
			return false;
		}

		Tokenizer et = new EDIFACTTokenizer(inReader, dErr);
		// , posStop-1));
		envelope.setDelimiters(et.getSeparators());

		et.getNextSegment(null);
		String findID = et.getNextDataElement();
		if (findID.startsWith("UNA")) {
			Segment UNA_Service_String = envelope.createService_String();
			UNA_Service_String.setByteOffset(et.getInputByteCount());
			notifyStartSegment(UNA_Service_String);
			if (halted()) {
				return false;
			}
			UNA_Service_String.setDataElementValue(2, et.getRestOfSegment());
			notifyEndSegment(UNA_Service_String);
			if (halted()) {
				return false;
			}
			// UNA_Service_String.parse(et);
			et.getNextSegment(null);

			findID = et.getNextDataElement();
		}

		if (findID.compareTo(EDIFACTEnvelope.idInterchangeHeader) != 0) {
			throw new OBOEException(
					"Segment ID " + findID + " found, was expecting "
							+ EDIFACTEnvelope.idInterchangeHeader + ".");
		}
		Segment UNB_Interchange_Header = envelope.createInterchange_Header();
		UNB_Interchange_Header.setByteOffset(et.getInputByteCount());
		notifyStartSegment(UNB_Interchange_Header);
		if (halted()) {
			return false;
		}
		SegmentParser.parse(UNB_Interchange_Header, et);

		notifyEndSegment(UNB_Interchange_Header);
		if (halted()) {
			return false;
		}

		findID = et.getCurrentDataElement();
		boolean iterateOnce = (findID
				.compareTo(EDIFACTFunctionalGroup.idHeader) != 0);
		if (iterateOnce) {
			findID = EDIFACTFunctionalGroup.idHeader;
		}

		while ((findID != null)
				&& (findID.compareTo(EDIFACTFunctionalGroup.idHeader) == 0)) {
			FunctionalGroup fg = envelope.createFunctionalGroup();
			notifyStartFunctionalGroup(fg);
			if (halted()) {
				return false;
			}
			if (iterateOnce) {
				;
			} else {
				Segment UNG_Functional_Group_Header = fg
						.createAndAddSegment(EDIFACTFunctionalGroup.idHeader);
				UNG_Functional_Group_Header
						.setByteOffset(et.getInputByteCount());
				notifyStartSegment(UNG_Functional_Group_Header);
				if (halted()) {
					return false;
				}
				SegmentParser.parse(UNG_Functional_Group_Header, et);

				notifyEndSegment(UNG_Functional_Group_Header);
				if (halted()) {
					return false;
				}
			}
			findID = et.getCurrentDataElement();

			while ((findID != null) && (findID.equals("UNH"))) {
				Segment UNH_Message_Header = Message_Header.getInstance();

				UNH_Message_Header.setByteOffset(et.getInputByteCount());

				SegmentParser.parse(UNH_Message_Header, et);

				CompositeElement msgID = (CompositeElement) UNH_Message_Header
						.getElement("S009");

				findID = msgID.getElement("0065").get();

				String testProduction = "";

				Element tde = UNB_Interchange_Header.getElement("0035"); // do
				// this because 0035 is not required
				if (tde != null) {
					testProduction = tde.get();
				}

				CompositeElement interChangeSender = (CompositeElement) UNB_Interchange_Header
						.getElement("S002");
				CompositeElement interRecipient = (CompositeElement) UNB_Interchange_Header
						.getElement("S003");
				parsedTransactionSet = TransactionSetFactory
						.buildTransactionSet(findID, null,
								// use OBOE.properties searchDirective
								msgID.getElement("0052").get()
										+ msgID.getElement("0054").get(), // version
								interRecipient.getElement("0010").get(), // receiver
								interChangeSender.getElement("0004").get(), // sender
								testProduction); // test/production

				UNH_Message_Header.setParent(parsedTransactionSet);

				notifyStartTransactionSet(parsedTransactionSet);
				if (halted()) {
					return false;
				}

				parsedTransactionSet.setFormat(Format.EDIFACT_FORMAT);
				parsedTransactionSet.setParent(fg);

				Table hdr = parsedTransactionSet.getHeaderTable();

				hdr.addContainer(UNH_Message_Header);

				parsedTransactionSet.parse(et);
				findID = et.getCurrentDataElement();

				while ((findID != null) && (findID.equals("UNH"))) {
					findID = et.getCurrentDataElement();
					if (findID.equals("UNH")) {
						notifyEndTransactionSet(parsedTransactionSet);
						if (halted()) {
							return false;
						}

						break;
					} else if (findID
							.compareTo(EDIFACTFunctionalGroup.idHeader) == 0) {
						notifyEndTransactionSet(parsedTransactionSet);
						if (halted()) {
							return false;
						}

						break;
					} else if (findID
							.compareTo(EDIFACTFunctionalGroup.idTrailer) == 0) {
						notifyEndTransactionSet(parsedTransactionSet);
						if (halted()) {
							return false;
						}

						if (iterateOnce) {
							// header.
							et.reportError(EDIFACTFunctionalGroup.idHeader
									+ " appears without "
									+ EDIFACTFunctionalGroup.idTrailer);
						}
						break;
					} else if (findID.compareTo(
							EDIFACTEnvelope.idInterchangeTrailer) == 0) {
						if (!iterateOnce) {
							et.reportError(
									"Should not appear before end of functional group");
						}
						notifyEndTransactionSet(parsedTransactionSet);
						if (halted()) {
							return false;
						}

						break;
					} else if (findID.length() > 0) {
						et.reportError("Unknown or out of place segment ("
								+ findID + ") byte offset ("
								+ et.getInputByteCount() + ")", "2");
						// here is where we put logic to try and get back into
						// sync.
						et.getNextSegment(et.getLastMetaContainer());
						et.getNextDataElement();
						parsedTransactionSet
								.continueParse(et.getLastMetaContainer(), et);
					} else {
						et.reportError("Empty Data Line Error", "?");
						// here is where we put logic to try and get back into
						// sync.
						et.getNextSegment(et.getLastMetaContainer());
						et.getNextDataElement();
						parsedTransactionSet
								.continueParse(et.getLastMetaContainer(), et);
					}
				}
			}

			if (iterateOnce) {
				notifyEndTransactionSet(parsedTransactionSet);
				if (halted()) {
					return false;
				}
				notifyEndFunctionalGroup(fg);
				if (halted()) {
					return false;
				}
			} else if (findID
					.compareTo(EDIFACTFunctionalGroup.idTrailer) != 0) {
				dErr.addError(0, EDIFACTFunctionalGroup.idTrailer,
						"More segments to process starting at segment position "
								+ et.getSegmentPos() + ". Problem "
								+ et.getCurrentDataElement()
								+ " is not defined. Expecting "
								+ EDIFACTFunctionalGroup.idTrailer,
						envelope, "0", null, ERROR_TYPE.Integrity);
				/*
				 * throw new OBOEException(
				 * "More segments to process starting at position " +
				 * et.getSegmentPos() + ". Problem " +
				 * et.getCurrentDataElement() + " is not defined. Expecting " +
				 * EDIFACTFunctionalGroup.idTrailer);
				 */
			} else {
				Segment UNE_Functional_Group_Trailer = fg
						.createAndAddSegment(EDIFACTFunctionalGroup.idTrailer);
				UNE_Functional_Group_Trailer
						.setByteOffset(et.getInputByteCount());
				notifyStartSegment(UNE_Functional_Group_Trailer);
				if (halted()) {
					return false;
				}
				SegmentParser.parse(UNE_Functional_Group_Trailer, et);

				notifyEndSegment(UNE_Functional_Group_Trailer);
				if (halted()) {
					return false;
				}
				notifyEndFunctionalGroup(fg);
				if (halted()) {
					return false;
				}
				findID = et.getCurrentDataElement();
			}

		}

		if (findID == null) {
			et.reportError("Envelope ended too soon", "2");
		} else

		if (findID.compareTo(EDIFACTEnvelope.idInterchangeTrailer) != 0) {
			dErr.addError(0, EDIFACTEnvelope.idInterchangeTrailer,
					"More segments to process starting at segment position "
							+ et.getSegmentPos() + ". Problem "
							+ et.getCurrentDataElement()
							+ " is not defined. Expecting "
							+ EDIFACTEnvelope.idInterchangeTrailer,
					envelope, "0", null, ERROR_TYPE.Integrity);
			/*
			 * throw new OBOEException(dErr); throw new OBOEException(
			 * "More segments to process starting at segment position " +
			 * et.getSegmentPos() + ". Problem " + et.getCurrentDataElement() +
			 * " is not defined. Expecting " +
			 * EDIFACTEnvelope.idInterchangeTrailer);
			 */
		} else {
			Segment UNZ_Interchange_Trailer = envelope
					.createInterchange_Trailer();
			UNZ_Interchange_Trailer.setByteOffset(et.getInputByteCount());
			notifyStartSegment(UNZ_Interchange_Trailer);
			if (halted()) {
				return false;
			}
			SegmentParser.parse(UNZ_Interchange_Trailer, et);

			notifyEndSegment(UNZ_Interchange_Trailer);
			if (halted()) {
				return false;
			}
			notifyEndEnvelope(envelope);
			if (halted()) {
				return false;
			}

		}

		if (inValidate) {
			envelope.validate(dErr);
		}

		if (io.github.EDIandXML.OBOE.util.Util
				.propertyFileIndicatesTHROW_PARSING_EXCEPTION()
				&& (dErr.getErrorCount() > 0)) {
			// dErr.logErrors();
			throw new OBOEException(dErr);
		}

		return true;
	}

}
