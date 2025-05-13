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
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.EDIDocumentHandler;
import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;

/**
 * class to parse input string for all defined OBOE Transaction Sets
 * 
 * EDIFACT dependent
 *
 */

public class EDIFACTDocumentHandler implements EDIDocumentHandler {
	EDIFACTDocumentParser parser = null;
	EDIFACTEnvelope envelope = null;
	// Segment saveServiceString = null;
	// serviceString is parsed well before the envelope is created
	FunctionalGroup functionalGroup = null;
	// TransactionSet TransactionSet = null;

	static Logger logr = LogManager.getLogger(EDIFACTDocumentHandler.class);

	/**
	 * create a parser for transaction set and parser what is coming from
	 * Reader object
	 */

	public EDIFACTDocumentHandler() {
		parser = new EDIFACTDocumentParser();
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

	public EDIFACTDocumentHandler(Reader inReader) throws OBOEException {
		parser = new EDIFACTDocumentParser();
		parser.registerHandler(this);
		parser.parseDocument(inReader, false);
		envelope.validate(parser.getDocumentErrors());
	}

	/**
	 * create a parser for transaction set and parser what is coming from
	 * Reader object if you use this constructor and there are document
	 * errors the method will not make the envelope object available
	 *
	 * @param inString file name of the edi document
	 * @exception OBOEException - unknown transaction set, this transaction
	 *                          set is undefined to OBOE - parsing erros
	 */

	public EDIFACTDocumentHandler(String inString) throws OBOEException {
		try {

			parser = new EDIFACTDocumentParser();

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
	 *                          set is undefined to OBOE - parsing erros
	 */

	@Override
	public void startParsing(Reader inReader) throws OBOEException {
		parser.parseDocument(inReader, false);
		envelope.validate(parser.getDocumentErrors());
	}

	/**
	 * called when an Envelope object is created
	 *
	 * @param inEnv Envelope found
	 */
	@Override
	public void startEnvelope(Envelope inEnv) {
		envelope = (EDIFACTEnvelope) inEnv;
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

		if (inSeg.getID().compareTo(EDIFACTEnvelope.idServiceString) == 0) {
			envelope.addSegment(inSeg);
		} else if (inSeg.getID()
				.compareTo(EDIFACTEnvelope.idInterchangeHeader) == 0) {
			envelope.addSegment(inSeg);
		} else if (inSeg.getID()
				.compareTo(EDIFACTFunctionalGroup.idHeader) == 0) {
			functionalGroup.addSegment(inSeg);
		} else if (inSeg.getID()
				.compareTo(EDIFACTFunctionalGroup.idTrailer) == 0) {
			functionalGroup.addSegment(inSeg);
		} else if (inSeg.getID()
				.compareTo(EDIFACTEnvelope.idInterchangeTrailer) == 0) {
			envelope.addSegment(inSeg);
		}

	}

	/**
	 * called when an Evelope is finished
	 *
	 * @param inEnv envelope found
	 */
	@Override
	public void endEnvelope(Envelope inEnv) {
		;
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
	 * gets the DocumentErrors object created by the DocumentParser class
	 *
	 * @return DocumentErrors
	 */

	/**
	 * main method used for testing purposes format: java filetoparse writes
	 * valid xml format of file to System.out
	 *
	 * @param args String array - only one arg accepted - file to parse
	 */

	public static void main(String args[]) {
		EDIFACTDocumentHandler dh = null;
		try {
			FileReader fr = new FileReader(args[0]);

			dh = new EDIFACTDocumentHandler();

			dh.startParsing(fr);

			dh.parser.getDocumentErrors().logErrors();
			fr.close();
			Envelope x = dh.getEnvelope();
			FileOutputStream fos = new FileOutputStream(args[0] + ".xml");
			x.writeFormattedText(new PrintWriter(fos), Format.VALID_XML_FORMAT);
		} catch (OBOEException oe) {
			logr.error(oe.getMessage(), oe);
			oe.getDocumentErrors().logErrors();
			Envelope x = dh.getEnvelope();
			System.out.println(x.getFormattedText(Format.VALID_XML_FORMAT));
			// x.writeFormattedText(new PrintWriter(System.out),
			// Format.VALID_XML_FORMAT);

		} catch (Exception e) {
			logr.error(e.getMessage(), e);
		}

	}

	@Override
	public DocumentErrors getDocumentErrors() {

		return parser.getDocumentErrors();
	}

	public EDIFACTDocumentParser getParser() {
		return parser;
	}
}
