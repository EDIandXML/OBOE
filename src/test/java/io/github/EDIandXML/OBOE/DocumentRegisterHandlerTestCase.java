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

import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.EDIFACT.EDIFACTDocumentHandler;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;

/**
 * @author joe mcverry
 *
 */
public class DocumentRegisterHandlerTestCase implements EDIDocumentHandler

{

	@Override
	public void startParsing(Reader inReader) throws OBOEException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startEnvelope(Envelope inEnv) {
		all[0] = true;

	}

	@Override
	public void startFunctionalGroup(FunctionalGroup inFG) {
		all[1] = true;

	}

	@Override
	public void startTransactionSet(TransactionSet inTS) {
		all[2] = true;

	}

	@Override
	public void startSegment(Segment inSeg) {
		all[3] = true;

	}

	@Override
	public void endEnvelope(Envelope inEnv) {
		all[4] = true;

	}

	@Override
	public void endFunctionalGroup(FunctionalGroup inFG) {
		all[5] = true;

	}

	@Override
	public void endTransactionSet(TransactionSet inTS) {
		all[6] = true;

	}

	@Override
	public void endSegment(Segment inSeg) {
		all[7] = true;

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

	boolean all[];

	@Test
	public void testX12() {
		all = new boolean[8];
		X12DocumentHandler x = new X12DocumentHandler();
		x.getParser().registerHandler(this);
		FileReader fr;
		try {
			fr = new FileReader("testFiles/sample.output.840.1");
			x.startParsing(fr);
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}

		for (int i = 0; i < 8; i++) {
			if (all[i] == false) {
				fail("on number " + i);
			}
		}

	}

	@Test
	public void testEDIFACT() {
		all = new boolean[8];
		EDIFACTDocumentHandler e = new EDIFACTDocumentHandler();
		e.getParser().registerHandler(this);
		FileReader fr;
		try {
			fr = new FileReader("testFiles/orders");
			e.startParsing(fr);
		} catch (FileNotFoundException ex) {
			fail(ex.getMessage());
		}

		for (int i = 0; i < 8; i++) {
			if (all[i] == false) {
				fail("on number " + i);
			}
		}

	}

}
