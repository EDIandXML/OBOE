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
 * Works with X12 Docs
 *
 * @author Joe McVerry
 * 
 */

import java.io.File;
import java.io.Reader;
import java.io.StringReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.EnvelopeFactory;
import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.TransactionSetFactory;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Parsers.EDIDocumentParser;
import io.github.EDIandXML.OBOE.Parsers.SegmentParser;
import io.github.EDIandXML.OBOE.Templates.TemplateEnvelope;
import io.github.EDIandXML.OBOE.Tokenizers.Tokenizer;

/**
 * class to parse input string for all defined OBOE Transaction Sets
 * 
 * x12 dependent
 *
 */

public class X12DocumentParser extends EDIDocumentParser {

	static Logger logr = LogManager.getLogger(X12DocumentHandler.class);

	/**
	 * parses an X12 Document and passes results to EDIDocumentHandlers
	 */
	public X12DocumentParser() {
		super();
	}

	/**
	 * parses a String containing an X12 Document and passes results to
	 * EDIDocumentHandlers
	 *
	 * @param inString the edi document
	 * @return boolean - true - continue or false - halted
	 * @exception OBOEException - unknown transaction set, this transaction
	 *                          set is undefined to OBOE
	 */
	@Override
	public boolean parseDocument(String inString) throws OBOEException {

		int posStart;
		posStart = inString.indexOf(X12Envelope.idInterchangeHeader);
		if (posStart < 0) {
			throw new OBOEException(
					X12Envelope.idInterchangeHeader + " segment missing");
		}
		int posStop = inString.indexOf(X12Envelope.idInterchangeTrailer);
		if (posStop < 0) {
			throw new OBOEException(
					X12Envelope.idInterchangeTrailer + " segment missing");
		}

		return parseDocument(new StringReader(inString), true);
	}

	String redefineVersion = null;

	public void setVersion(String inV) {
		redefineVersion = inV;
	}

	public String getVersion() {
		return redefineVersion;
	}

	/**
	 * parses an X12 Document and passes results to EDIDocumentHandlers
	 *
	 * @param inReader   the edi document passed through by a reader object
	 * @param inValidate - if true call validation method of the envelope
	 *                   after parsing
	 * @return boolean - true - continue or false - halted if you pass false
	 *         don't forget to call the validation method on the envelope
	 *         object pass to the validation method the documenterrors
	 *         object created here, use the getDocumentErrors method of the
	 *         parser object.
	 * @exception OBOEException - unknown transaction set, this transaction
	 *                          set is undefined to OBOE
	 */

	@Override
	public boolean parseDocument(Reader inReader, boolean inValidate)
			throws OBOEException {

		if (redefineVersion != null) {
			dErr = new DocumentErrors();
		}
		X12Envelope envelope;
		Tokenizer tokenizer = new X12Tokenizer(inReader, dErr); // ,
																// posStop-1));
		logr.debug("env ver = " + tokenizer.getVersion());
		try {
			TemplateEnvelope te = EnvelopeFactory.buildEnvelope("x12.envelope",
					tokenizer.getVersion() + File.separator);
			envelope = new X12Envelope(te);
		} catch (OBOEException oe) {
			// if (oe.getMessage().startsWith("File not found"))
			// envelope = new X12Envelope();
			// else
			throw oe;
		}

		notifyStartEnvelope(envelope);
		if (this.halted()) {
			return false;
		}

		envelope.setDelimiters(tokenizer.getSeparators());

		tokenizer.getNextSegment(null);
		String findID = tokenizer.getNextDataElement();

		if (findID.compareTo(X12Envelope.idInterchangeHeader) != 0) {
			throw new OBOEException(
					"Segment ID \"" + findID + "\" found, was expecting "
							+ X12Envelope.idInterchangeHeader + "\"");
		}

		Segment ISA_Interchange_Control_Header = envelope
				.createInterchange_Header();
		ISA_Interchange_Control_Header
				.setByteOffset(tokenizer.getInputByteCount());
		// should be 0
		notifyStartSegment(ISA_Interchange_Control_Header);
		if (this.halted()) {
			return false;
		}
		SegmentParser.parse(ISA_Interchange_Control_Header, tokenizer);

		notifyEndSegment(ISA_Interchange_Control_Header);
		if (this.halted()) {
			return false;
		}

		findID = tokenizer.getCurrentDataElement();
		if (findID.compareTo(X12Envelope.idGradeofServiceRequest) == 0) {
			Segment ISB_Grade_of_Service_Request = envelope
					.createGrade_of_Service_Request();
			ISB_Grade_of_Service_Request
					.setByteOffset(tokenizer.getInputByteCount());
			notifyStartSegment(ISB_Grade_of_Service_Request);
			if (this.halted()) {
				return false;
			}
			SegmentParser.parse(ISB_Grade_of_Service_Request, tokenizer);
			notifyEndSegment(ISB_Grade_of_Service_Request);
			if (this.halted()) {
				return false;
			}
			findID = tokenizer.getCurrentDataElement();
		}
		if (findID.compareTo(X12Envelope.idDeferredDeliveryRequest) == 0) {
			Segment ISE_Deferred_Delivery_Request = envelope
					.createDeferred_Delivery_Request();
			ISE_Deferred_Delivery_Request
					.setByteOffset(tokenizer.getInputByteCount());
			notifyStartSegment(ISE_Deferred_Delivery_Request);
			if (this.halted()) {
				return false;
			}
			SegmentParser.parse(ISE_Deferred_Delivery_Request, tokenizer);
			notifyStartSegment(ISE_Deferred_Delivery_Request);
			if (this.halted()) {
				return false;
			}
			findID = tokenizer.getCurrentDataElement();
		}
		boolean ta1Found = false;
		if (findID.compareTo(X12Envelope.idInterchangeAcknowledgment) == 0) {
			// /ArrayList v_Interchange_Acknowledgment = new ArrayList();
			do {
				Segment Temp_Interchange_Acknowledgment = envelope
						.createInterchange_Acknowledgment();
				Temp_Interchange_Acknowledgment
						.setByteOffset(tokenizer.getInputByteCount());
				notifyStartSegment(Temp_Interchange_Acknowledgment);
				if (this.halted()) {
					return false;
				}
				SegmentParser.parse(Temp_Interchange_Acknowledgment, tokenizer);
				notifyEndSegment(Temp_Interchange_Acknowledgment);
				if (this.halted()) {
					return false;
				}
				findID = tokenizer.getCurrentDataElement();
				ta1Found = true;
			} while (findID.compareTo("TA1") == 0);
		}

		findID = tokenizer.getCurrentDataElement();
		if (findID.compareTo(X12FunctionalGroup.idHeader) != 0) {
			if (!ta1Found) {
				throw new OBOEException(
						"Segment ID \"" + findID + "\" found, was expecting \""
								+ X12FunctionalGroup.idHeader + "\"");
			}
		}

		while ((findID != null)
				&& (findID.compareTo(X12FunctionalGroup.idHeader) == 0)) {

			FunctionalGroup functionalGroup = envelope.createFunctionalGroup();
			notifyStartFunctionalGroup(functionalGroup);
			if (this.halted()) {
				return false;
			}
			Segment GS_Functional_Group_Header = functionalGroup
					.createAndAddSegment(X12FunctionalGroup.idHeader);
			GS_Functional_Group_Header
					.setByteOffset(tokenizer.getInputByteCount());
			notifyStartSegment(GS_Functional_Group_Header);
			if (this.halted()) {
				return false;
			}
			SegmentParser.parse(GS_Functional_Group_Header, tokenizer);
			notifyEndSegment(GS_Functional_Group_Header);
			if (this.halted()) {
				return false;
			}
			findID = tokenizer.getCurrentDataElement();
			TransactionSet parsedTransactionSet = null;

			while ((findID != null) && (findID.equals("ST"))) {
				findID = tokenizer.getNextDataElement();
				String getImplementationConventionReference = tokenizer
						.getDataElementAt(3);
				if (getImplementationConventionReference == null) {
					getImplementationConventionReference = GS_Functional_Group_Header
							.getElement("480").get();
				} else {
					// it's an error if they don't match
					if (getImplementationConventionReference
							.equals(GS_Functional_Group_Header.getElement("480")
									.get())) {
						;
					} else {
						dErr.addError(tokenizer.getSegmentPos(), "ST",
								"GS08 and ST03 do not match", functionalGroup,
								"?", tokenizer,
								DocumentErrors.ERROR_TYPE.Integrity);
					}

				}
				if (redefineVersion != null) {
					getImplementationConventionReference = redefineVersion;
				} else {
					redefineVersion = getImplementationConventionReference;
				}

				tokenizer.resetSegment();

				parsedTransactionSet = TransactionSetFactory
						.buildTransactionSet(findID, null,
								// use oboe.properties searchDirective
								getImplementationConventionReference, // either
								// ST03
								// or
								// GS09
								ISA_Interchange_Control_Header.getElement("I07")
										.get(), // receiver
								ISA_Interchange_Control_Header.getElement("I06")
										.get(), // sender
								ISA_Interchange_Control_Header.getElement("I14")
										.get()); // test

				// or
				// production
				parsedTransactionSet.setParent(functionalGroup);
				parsedTransactionSet.setFormat(Format.X12_FORMAT);
				notifyStartTransactionSet(parsedTransactionSet);
				if (this.halted()) {
					return false;
				}
				parsedTransactionSet.parse(tokenizer);
				while (true) {
					findID = tokenizer.getCurrentDataElement();
					if (findID == null) {
						return false;
					}
					if (findID.equals("ST")) {
						notifyEndTransactionSet(parsedTransactionSet);
						if (this.halted()) {
							return false;
						}
						break;
					} else if (findID
							.compareTo(X12FunctionalGroup.idTrailer) == 0) {
						notifyEndTransactionSet(parsedTransactionSet);
						if (this.halted()) {
							return false;
						}
						break;
					} else if (findID
							.compareTo(X12Envelope.idInterchangeTrailer) == 0) {
						tokenizer.reportError(
								"Should not appear before end of functional group",
								"2");
						notifyEndTransactionSet(parsedTransactionSet);
						if (this.halted()) {
							return false;
						}
						break;
					} else if (findID.length() > 0) {

						// et.reportError("Unknown or out of place segment",
						// "2");

						tokenizer.getLastMetaContainer().whyNotUsed(tokenizer);
						// here is where we put logic to try and get back into
						// sync.
						tokenizer.getNextSegment(
								tokenizer.getLastMetaContainer());
						tokenizer.getNextDataElement();
						if (parsedTransactionSet.continueParse(
								tokenizer.getLastMetaContainer(),
								tokenizer) == false) {
							tokenizer.reportError(
									"May not be able to restart parser", "?");
							// break;
						}
					} else {
						tokenizer.reportError("Empty Data Line Error", "?");
						// here is where we put logic to try and get back into
						// sync.
						tokenizer.getNextSegment(
								tokenizer.getLastMetaContainer());
						tokenizer.getNextDataElement();
						if (parsedTransactionSet.continueParse(
								tokenizer.getLastMetaContainer(),
								tokenizer) == false) {
							tokenizer.reportError("Can not restart parser",
									"?");
							// break;
						}
					}
				}
			}
			if (findID == null) {
				tokenizer.reportError("Envelope ended too soon", "2");
			} else if (findID.compareTo(X12FunctionalGroup.idTrailer) != 0) {
				tokenizer.reportError(
						"Unknown or out of place segment (" + findID + ")",
						"2");

			} else {
				Segment GE_Functional_Group_Trailer = functionalGroup
						.createAndAddSegment(X12FunctionalGroup.idTrailer);
				GE_Functional_Group_Trailer
						.setByteOffset(tokenizer.getInputByteCount());
				notifyStartSegment(GE_Functional_Group_Trailer);
				if (this.halted()) {
					return false;
				}
				SegmentParser.parse(GE_Functional_Group_Trailer, tokenizer);
				notifyEndSegment(GE_Functional_Group_Trailer);
				if (this.halted()) {
					return false;
				}
				notifyEndFunctionalGroup(functionalGroup);
				if (this.halted()) {
					return false;
				}
				findID = tokenizer.getCurrentDataElement();
			}

		}

		if (findID == null) {
			tokenizer.reportError("Envelope ended too soon", "2");
		} else if (findID.compareTo(X12Envelope.idInterchangeTrailer) != 0) {
			tokenizer.reportError(
					"Unknown or out of place segment (" + findID + ")", "2");
		} else {
			Segment IEA_Interchange_Control_Trailer = envelope
					.createInterchange_Trailer();
			IEA_Interchange_Control_Trailer
					.setByteOffset(tokenizer.getInputByteCount());
			notifyStartSegment(IEA_Interchange_Control_Trailer);
			if (this.halted()) {
				return false;
			}
			SegmentParser.parse(IEA_Interchange_Control_Trailer, tokenizer);
			notifyEndSegment(IEA_Interchange_Control_Trailer);
			if (this.halted()) {
				return false;
			}
		}

		notifyEndEnvelope(envelope);
		if (this.halted()) {
			return false;
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
