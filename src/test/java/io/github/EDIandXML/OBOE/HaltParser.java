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
package io.github.EDIandXML.OBOE;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.x12.X12DocumentParser;

/**
 * @author joe mcverry
 *
 */
public class HaltParser implements EDIDocumentHandler {

	static Logger logr = LogManager.getLogger(HaltParser.class);

	@BeforeAll
	public static void setup() {
		EnvelopeFactory.reloadbuiltTable();
	}

	/**
	 * @param name
	 */

	public boolean testHalt = false;
	X12DocumentParser p;

	@Test
	public void testNotHaltingParser() {
		p = new X12DocumentParser();
		p.registerHandler(this);
		try {
			assertTrue(p.parseDocument(new FileReader("testFiles/sample.output.840.1"),
					false));
		} catch (OBOEException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}

	}

	@Test
	public void testHaltingParser() {
		p = new X12DocumentParser();
		p.registerHandler(this);
		testHalt = true;
		try {
			assertFalse(p.parseDocument(new FileReader("testFiles/sample.output.840.1"),
					false));
			assertTrue(p.halted());
			assertTrue(p.whyHaltParser.equals("halted bad GS08"));
			logr.info(p.whyHaltParser);
		} catch (OBOEException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}

	}

	@Override
	public void endEnvelope(Envelope inEnv) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endFunctionalGroup(FunctionalGroup inFG) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endSegment(Segment inSeg) {
		if (testHalt) {
			if (inSeg.getID().equals("GS")) {
				if (inSeg.getDataElementValue(8).equals("004010")) {
					p.setWhyHaltParser("halted bad GS08");
				}

			}
		}

	}

	@Override
	public void endTransactionSet(TransactionSet inTS) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see EDIDocumentHandler#getDocumentErrors()
	 */
	@Override
	public DocumentErrors getDocumentErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startEnvelope(Envelope inEnv) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startFunctionalGroup(FunctionalGroup inFG) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startParsing(Reader inReader) throws OBOEException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startSegment(Segment inSeg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startTransactionSet(TransactionSet inTS) {
		// TODO Auto-generated method stub

	}

}
