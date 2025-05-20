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

import java.io.BufferedReader;
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
 * Tradacoms dependent
 *
 */

public class TradacomsDocumentHandler implements EDIDocumentHandler {

	static Logger logr = LogManager.getLogger(TradacomsDocumentHandler.class);

	static boolean log4JConfigured = false;

	TradacomsDocumentParser parser = null;
	TradacomsEnvelope envelope = null;
	// Segment saveServiceString = null;
	// serviceString is parsed well before the envelope is created
	FunctionalGroup functionalGroup = null;

	// TransactionSet TransactionSet = null;

	/**
	 * create a parser for transaction set and parser what is coming from
	 * Reader object
	 */

	public TradacomsDocumentHandler() {
		parser = new TradacomsDocumentParser();
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

	public TradacomsDocumentHandler(Reader inReader) throws OBOEException {
		parser = new TradacomsDocumentParser();
		parser.registerHandler(this);
		parser.parseDocument(inReader, true);
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
		parser.parseDocument(inReader, true);
	}

	/**
	 * called when an Envelope object is created
	 *
	 * @param inEnv Envelope found
	 */
	@Override
	public void startEnvelope(Envelope inEnv) {
		envelope = (TradacomsEnvelope) inEnv;
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

		if (inSeg.getID()
				.compareTo(TradacomsEnvelope.idInterchangeHeader) == 0) {
			envelope.addSegment(inSeg);
		} else if (inSeg.getID()
				.compareTo(TradacomsFunctionalGroup.idHeader) == 0) {
			functionalGroup.addSegment(inSeg);
		} else if (inSeg.getID()
				.compareTo(TradacomsFunctionalGroup.idTrailer) == 0) {
			functionalGroup.addSegment(inSeg);
		} else if (inSeg.getID()
				.compareTo(TradacomsEnvelope.idInterchangeTrailer) == 0) {
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
		TradacomsDocumentHandler dh = null;
		for (String arg : args) {
			try {

				System.out.println(arg);
				logr.debug(arg);
				BufferedReader br = new BufferedReader(new FileReader(arg));
				logr.debug(br.readLine());
				br.close();

				FileReader fr = new FileReader(arg);
				dh = new TradacomsDocumentHandler();
				dh.startParsing(fr);
				fr.close();
				Envelope x = dh.getEnvelope();
				// System.out.println(x.getFormattedText(Format.VALID_XML_FORMAT));
				System.out.println("xml results");
				x.writeFormattedText(new PrintWriter(System.out),
						Format.VALID_XML_FORMAT);

			} catch (OBOEException oe) {
				logr.debug(oe.getMessage(), oe);
				if (oe.getDocumentErrors() != null) {
					String err[] = oe.getDocumentErrors().getError();
					for (String element : err) {
						System.out.println(element);
					}
					Envelope x = dh.getEnvelope();
					System.out.println("xml results");
					System.out.println(
							x.getFormattedText(Format.VALID_XML_FORMAT));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public DocumentErrors getDocumentErrors() {

		return parser.getDocumentErrors();
	}
}
