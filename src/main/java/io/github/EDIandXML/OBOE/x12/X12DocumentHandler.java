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
 * Works with X12 Docs.
 *
 * @author Joe McVerry
 * 
 */

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.EDIDocumentHandler;
import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.util.Util;

public class X12DocumentHandler implements EDIDocumentHandler {
	X12DocumentParser parser;
	X12Envelope envelope = null;
	FunctionalGroup functionalGroup = null;

	static Logger logr = LogManager.getLogger(X12DocumentHandler.class);

	/**
	 * create a parser for transaction set and parser what is coming from
	 * Reader object
	 */

	public X12DocumentHandler() {
		parser = new X12DocumentParser();
		parser.registerHandler(this);
	}

	/**
	 * create a parser for transaction set and parser what is coming from
	 * Reader object if you use this constructor and there are document
	 * errors the method will not make the envelope object available
	 * 
	 * @param inReader the edi document in a java io reader object
	 * @exception OBOEException - unknown transaction set, this transaction
	 *                          set is undefined to OBOE - parsing errors
	 */

	public X12DocumentHandler(Reader inReader) throws OBOEException {
		parser = new X12DocumentParser();
		parser.registerHandler(this);
		parser.parseDocument(inReader, false);
		envelope.validate(parser.getDocumentErrors());
	}

	/**
	 * create a parser for transaction set and parser what is coming from
	 * Reader object if you use this constructor and there are document
	 * errors the method will not make the envelope object available
	 * 
	 * @param inString filename to parse
	 * @exception OBOEException - unknown transaction set, this transaction
	 *                          set is undefined to OBOE - parsing errors
	 */

	public X12DocumentHandler(String inString) throws OBOEException {
		try {

			parser = new X12DocumentParser();

			parser.registerHandler(this);

			startParsing(new FileReader(inString));

			envelope.validate(parser.getDocumentErrors());
		} catch (OBOEException e) {

			logr.error(e.getMessage(), e);
		} catch (FileNotFoundException e) {
			// catch block Sep 9, 2005
			logr.error(e.getMessage(), e);
		}
	}

	/**
	 * starts the parser with the passed Reader object
	 *
	 * @param inReader the edi document in a java io reader object
	 * @exception OBOEException - unknown transaction set, this transaction
	 *                          set is undefined to OBOE - parsing errors
	 */

	@Override
	public void startParsing(Reader inReader) throws OBOEException {

		parser.parseDocument(inReader, false);

		envelope.validate(parser.getDocumentErrors());

		if (parser.getDocumentErrors().getErrorCount() > 0) {
			if (Util.propertyFileIndicatesTHROW_PARSING_EXCEPTION()) {
				PrintWriter pw = new PrintWriter(System.out);
				try {
					parser.getDocumentErrors().writeErrors(pw);
				} catch (IOException e) {
					e.printStackTrace();
					throw new OBOEException(e.getLocalizedMessage());
				}
				throw new OBOEException(parser.getDocumentErrors());
			} else {
				logr.error("Validation failed, check DocumentErrors object");
			}
		}
	}

	/**
	 * called when an Envelope object is created
	 * 
	 * @param inEnv Envelope found
	 */
	@Override
	public void startEnvelope(Envelope inEnv) {
		envelope = (X12Envelope) inEnv;
	}

	/**
	 * called when an FunctionalGroup object is created
	 * 
	 * @param inFG FunctionalGroup found
	 */
	@Override
	public void startFunctionalGroup(FunctionalGroup inFG) {
		functionalGroup = inFG;
		envelope.addFunctionalGroup(inFG);
	}

	/**
	 * called when an TransactionSet object is created
	 * 
	 * @param inTS TransactionSet found
	 */
	@Override
	public void startTransactionSet(TransactionSet inTS) {
		functionalGroup.addTransactionSet(inTS);
	}

	/**
	 * called when an Segment object is created only called for segments at
	 * the Envelope and functionalGroup level does not get called for
	 * segments within TransactionSet
	 * 
	 * @param inSeg Segment found
	 */
	@Override
	public void startSegment(Segment inSeg) {

		if (inSeg.getID().compareTo(X12Envelope.idInterchangeHeader) == 0) {
			envelope.addSegment(inSeg);
		} else if (inSeg.getID()
				.compareTo(X12Envelope.idInterchangeTrailer) == 0) {
			envelope.addSegment(inSeg);
		} else if (inSeg.getID()
				.compareTo(X12Envelope.idGradeofServiceRequest) == 0) {
			envelope.addSegment(inSeg);
		} else if (inSeg.getID()
				.compareTo(X12Envelope.idDeferredDeliveryRequest) == 0) {
			envelope.addSegment(inSeg);
		} else if (inSeg.getID()
				.compareTo(X12Envelope.idInterchangeAcknowledgment) == 0) {
			envelope.addSegment(inSeg);
		} else if (inSeg.getID().compareTo(X12FunctionalGroup.idHeader) == 0) {
			functionalGroup.addSegment(inSeg);
		} else if (inSeg.getID().compareTo(X12FunctionalGroup.idTrailer) == 0) {
			functionalGroup.addSegment(inSeg);
		}

	}

	/**
	 * called when an Evelope is finished
	 * 
	 * @param inEnv envelope found
	 */
	@Override
	public void endEnvelope(Envelope inEnv) {
		envelope = (X12Envelope) inEnv;
	}

	/**
	 * called when an FunctionalGroup object is finished
	 * 
	 * @param inFG FunctionalGroup found
	 */
	@Override
	public void endFunctionalGroup(FunctionalGroup inFG) {
		;
	}

	/**
	 * called when an TransactionSet object is finished
	 * 
	 * @param inTS TransactionSet found
	 */
	@Override
	public void endTransactionSet(TransactionSet inTS) {
		;
	}

	/**
	 * called when an Segment object is finished only called for segments at
	 * the Envelope and functionalGroup level does not get called for
	 * segments within TransactionSet
	 * 
	 * @param inSeg Segment found
	 */
	@Override
	public void endSegment(Segment inSeg) {
		;
	}

	/**
	 * returns the envelope that was parsed
	 * 
	 * @return Envelope - the envelope when object was built.
	 */
	public Envelope getEnvelope() {
		return envelope;
	}

	/**
	 * main to test in application mode args[0] String - an input file name
	 * - contains an X12 message args[1] String - optional integer value for
	 * output format XML_FORMAT = 1; X12_FORMAT = 2; EDIFACT_FORMAT = 3 not
	 * logical VALID_XML_FORMAT = 4; CSV_FORMAT = 5; FIXED_LENGTH_FORMAT =
	 * 6; TRADACOMS_FORMAT = 7 not logical VALID_XML_FORMAT_WITH_POSITION =
	 * 8; ACH_FORMAT = 9 not logical ACH_FORMAT_FOR_CBR_PBR = 10 not logical
	 * must contain an x12 edi document
	 */

	public static void main(String args[]) {

		try {
			FileReader fr = new FileReader(args[0]);
			X12DocumentHandler dh = new X12DocumentHandler();
			dh.startParsing(fr);

			fr.close();
			Envelope env = dh.getEnvelope();
			// TransactionSet ts =
			// env.getFunctionalGroup(0).getTransactionSet(0);

			env.writeFormattedText(new PrintWriter(System.out),
					Format.VALID_XML_FORMAT);

		} catch (OBOEException oe1) {
			if (oe1.getDocumentErrors() == null) {
				oe1.printStackTrace();
			} else {
				oe1.getDocumentErrors().logErrors();
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	static void goUp(IContainedObject inICO, StringBuilder inSB) {
		inSB.append(" in " + inICO.getID());
		if (inICO.getParent() != null) {
			goUp(inICO.getParent(), inSB);
		}

	}

	@Override
	public DocumentErrors getDocumentErrors() {

		return parser.getDocumentErrors();
	}

	public X12DocumentParser getParser() {
		return parser;
	}

}
