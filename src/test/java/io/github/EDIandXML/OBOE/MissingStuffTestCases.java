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
/*
 * I've noticed that when parsing an incoming EDIFACT message, there is
 * no error when a mandatory element is missing. The same when I
 * generate an outgoing EDIFACT message. tim snyder mercurygate
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.EDIFACT.EDIFACTDocumentHandler;
import io.github.EDIandXML.OBOE.Errors.OBOEException;

public class MissingStuffTestCases {
	static Logger logr = LogManager.getLogger(MissingStuffTestCases.class);

	String xmlPath = "";

	@Test
	public void testMissingElements() throws IOException {
		FileReader fr = new FileReader(
				"testFiles/orders.missingMandatoryElements");

		EDIFACTDocumentHandler p1 = null;
		try {
			p1 = new EDIFACTDocumentHandler(fr);
			assertEquals(1, p1.getDocumentErrors().getErrorCount());
			assertEquals("C5071", p1.getDocumentErrors().getErrorID(0));
			assertTrue(p1.getDocumentErrors().getErrorDescription(0)
					.contains("Required DataElement missing"));

		} catch (OBOEException oe) {
			fail("FAILED " + oe.getMessage());
		}
		fr.close();

	}

	@Test
	public void testMissingSegments() throws IOException {
		FileReader fr = new FileReader(
				"testFiles/orders.missingMandatorySegments");

		EDIFACTDocumentHandler p1 = null;
		try {
			p1 = new EDIFACTDocumentHandler(fr);
			p1.getEnvelope().validate();
			assertEquals(2, p1.getDocumentErrors().getErrorCount());
			assertEquals("header", p1.getDocumentErrors().getErrorID(0));
			assertTrue(p1.getDocumentErrors().getErrorDescription(0)
					.contains("Required Segment (BGM)"));
			assertEquals("header", p1.getDocumentErrors().getErrorID(1));
			assertTrue(p1.getDocumentErrors().getErrorDescription(1)
					.contains("Required Segment (DTM)"));

		} catch (OBOEException oe) {
			fail("FAILED " + oe.getMessage());
		}
	}

}