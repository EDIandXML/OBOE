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
import io.github.EDIandXML.OBOE.DataElements.CompositeElement;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors.ERROR_TYPE;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Parsers.EDIDocumentParser;
import io.github.EDIandXML.OBOE.Parsers.SegmentParser;
import io.github.EDIandXML.OBOE.Templates.TemplateEnvelope;
import io.github.EDIandXML.OBOE.Tokenizers.Tokenizer;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class defining methods for parsing EDI Documents Tradacoms2 dependent
 * Document handlers will register with this class to be notified when
 * specific edi objects are created or finished Unlike the old parser
 * these parsers will not contain the objects, the process of adding
 * objects to owning parents (such as adding functional groups to an
 * envelope) is left up to the document handler.
 */

public class TradacomsDocumentParser extends EDIDocumentParser {
	static Logger logr = LogManager.getLogger(TradacomsDocumentParser.class);

	/**
	 * parses an Tradacoms Document and passes results to
	 * EDIDocumentHandlers
	 *
	 * @exception OBOEException - unknown transaction set, this transaction
	 *                          set is undefined to OBOE
	 */
	public TradacomsDocumentParser() {
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

		int posStart;
		posStart = inString.indexOf(TradacomsEnvelope.idInterchangeHeader);
		if (posStart < 0) {
			throw new OBOEException(
					TradacomsEnvelope.idInterchangeHeader + " segment missing");
		}
		int posStop = inString.indexOf(TradacomsEnvelope.idInterchangeTrailer);
		if (posStop < 0) {
			throw new OBOEException(TradacomsEnvelope.idInterchangeTrailer
					+ " segment missing");
		}

		return parseDocument(new StringReader(inString), true);
	}

	/**
	 * method that controls the parsing
	 *
	 * @param inReader   reader object containing edi data
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
		TransactionSet parsedTransactionSet;
		TradacomsEnvelope envelope;
		Util.setOBOEProperty("doPrevalidate", "true");

		TemplateEnvelope te = EnvelopeFactory
				.buildEnvelope("Tradacoms.envelope", "notSetYet");
		envelope = new TradacomsEnvelope(te);

		notifyStartEnvelope(envelope);
		if (halted()) {
			return false;
		}

		Tokenizer et = new TradacomsTokenizer(inReader, dErr);
		// , posStop-1));
		envelope.setDelimiters(et.getSeparators());

		et.getNextSegment(null);
		String findID = et.getNextDataElement();

		if (findID.compareTo(TradacomsEnvelope.idInterchangeHeader) != 0) {
			throw new OBOEException(
					"Segment ID " + findID + " found, was expecting "
							+ TradacomsEnvelope.idInterchangeHeader + ".");
		}
		Segment STX_Start_of_Transmission = envelope.createInterchange_Header();
		STX_Start_of_Transmission.setByteOffset(et.getInputByteCount());
		notifyStartSegment(STX_Start_of_Transmission);
		if (halted()) {
			return false;
		}
		SegmentParser.parse(STX_Start_of_Transmission, et);
		notifyEndSegment(STX_Start_of_Transmission);
		if (halted()) {
			return false;
		}

		findID = et.getCurrentDataElement();
		boolean iterateOnce = (findID
				.compareTo(TradacomsFunctionalGroup.idHeader) != 0);
		if (iterateOnce) {
			findID = TradacomsFunctionalGroup.idHeader;
		}

		while (findID.compareTo(TradacomsFunctionalGroup.idHeader) == 0) {
			FunctionalGroup fg = envelope.createFunctionalGroup();
			notifyStartFunctionalGroup(fg);
			if (halted()) {
				return false;
			}
			if (iterateOnce) {
				;
			} else {
				Segment BAT_Functional_Group_Header = fg
						.createAndAddSegment(TradacomsFunctionalGroup.idHeader);
				BAT_Functional_Group_Header
						.setByteOffset(et.getInputByteCount());
				notifyStartSegment(BAT_Functional_Group_Header);
				if (halted()) {
					return false;
				}
				SegmentParser.parse(BAT_Functional_Group_Header, et);
				notifyEndSegment(BAT_Functional_Group_Header);
				if (halted()) {
					return false;
				}
			}
			findID = et.getCurrentDataElement();

			while (findID.equals("MHD")) {
				et.getNextDataElement();
				findID = et.getNextDataElement();
				findID = findID.substring(0, 6);
				et.resetSegment();
				DataElement de = (DataElement) ((CompositeElement) STX_Start_of_Transmission
						.getElement("FROM")).getElement(2);
				String from = "";
				if (de != null) {
					from = de.get();
				}
				de = (DataElement) ((CompositeElement) STX_Start_of_Transmission
						.getElement("UNTO")).getElement(2);
				String unto = "";
				if (de != null) {
					unto = de.get();
				}

				parsedTransactionSet = TransactionSetFactory
						.buildTransactionSet(findID, null,
								// use OBOE.properties searchDirective
								((CompositeElement) STX_Start_of_Transmission
										.getElement("STDS")).getElement(1)
										.get(), // version
								from, // receiver
								unto, // sender
								"");// test/production does not exist

				parsedTransactionSet.setParent(fg);
				parsedTransactionSet.setFormat(Format.TRADACOMS_FORMAT);
				notifyStartTransactionSet(parsedTransactionSet);
				if (halted()) {
					return false;
				}
				parsedTransactionSet.parse(et);
				while (true) {
					findID = et.getCurrentDataElement();
					logr.debug("finding " + findID);
					if (findID.equals("MHD")) {
						notifyEndTransactionSet(parsedTransactionSet);
						if (halted()) {
							return false;
						}
						break;
					} else if (findID.compareTo(
							TradacomsFunctionalGroup.idTrailer) == 0) {
						notifyEndTransactionSet(parsedTransactionSet);
						if (halted()) {
							return false;
						}
						break;
					} else if (findID.compareTo(
							TradacomsEnvelope.idInterchangeTrailer) == 0) {
						if (!iterateOnce) {
							et.reportError(
									"Should not appear before end of functional group");
						}
						notifyEndTransactionSet(parsedTransactionSet);
						if (halted()) {
							return false;
						}
						break;
					} else if (findID.compareTo(
							TradacomsEnvelope.idInterchangeTrailer) == 0) {
						et.reportError(
								"Should not appears before end of functional group",
								"2");
						notifyEndTransactionSet(parsedTransactionSet);
						if (halted()) {
							return false;
						}

						break;
					} else if (findID.length() > 0) {

						// et.reportError("Unknown or out of place segment",
						// "2");

						et.getLastMetaContainer().whyNotUsed(et);
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
				;
			} else if (findID
					.compareTo(TradacomsFunctionalGroup.idTrailer) != 0) {
				dErr.addError(0, TradacomsFunctionalGroup.idTrailer,
						"More segments to process starting at segment position "
								+ et.getSegmentPos() + ". Problem "
								+ et.getCurrentDataElement()
								+ " is not defined. Expecting "
								+ TradacomsFunctionalGroup.idTrailer,
						envelope, "0", null, ERROR_TYPE.Integrity);
				/*
				 * throw new OBOEException(
				 * "More segments to process starting at position " +
				 * et.getSegmentPos() + ". Problem " +
				 * et.getCurrentDataElement() + " is not defined. Expecting " +
				 * TradacomsFunctionalGroup.idTrailer);
				 */
			} else {
				Segment UNE_Functional_Group_Trailer = fg.createAndAddSegment(
						TradacomsFunctionalGroup.idTrailer);
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
				findID = et.getCurrentDataElement();
			}
			notifyEndFunctionalGroup(fg);
			if (halted()) {
				return false;
			}
		}

		if (findID.compareTo(TradacomsEnvelope.idInterchangeTrailer) != 0) {
			dErr.addError(0, TradacomsEnvelope.idInterchangeTrailer,
					"More segments to process starting at segment position "
							+ et.getSegmentPos() + ". Problem "
							+ et.getCurrentDataElement()
							+ " is not defined. Expecting "
							+ TradacomsEnvelope.idInterchangeTrailer,
					envelope, "0", null, ERROR_TYPE.Integrity);
			/*
			 * throw new OBOEException(
			 * "More segments to process starting at segment position " +
			 * et.getSegmentPos() + ". Problem " + et.getCurrentDataElement() +
			 * " is not defined. Expecting " +
			 * TradacomsEnvelope.idInterchangeTrailer);
			 */
		} else {
			Segment END_Interchange_Trailer = envelope
					.createInterchange_Trailer();
			END_Interchange_Trailer.setByteOffset(et.getInputByteCount());
			notifyStartSegment(END_Interchange_Trailer);
			if (halted()) {
				return false;
			}
			SegmentParser.parse(END_Interchange_Trailer, et);
			notifyEndSegment(END_Interchange_Trailer);
			if (halted()) {
				return false;
			}
			notifyEndEnvelope(envelope);
			if (halted()) {
				return false;
			}
			if (inValidate) {
				envelope.validate(dErr);
			}

			if (io.github.EDIandXML.OBOE.util.Util
					.propertyFileIndicatesTHROW_PARSING_EXCEPTION()
					&& (dErr.getErrorCount() > 0)) {
				dErr.logErrors();
				logr.info("doprevalidate="
						+ Util.propertyFileIndicatesDoPrevalidate());
				throw new OBOEException(dErr);
			}

		}

		return true;
	}

}
