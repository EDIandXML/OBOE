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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.Containers.Loop;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.Table;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.util.Util;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;

public class SegmentFetchTestCase {

	static Logger logr = LogManager.getLogger(SegmentFetchTestCase.class);

	@BeforeEach
	public void setup() {
		EnvelopeFactory.reloadbuiltTable();
	}

	@Test
	public void testFetch() {
		X12DocumentHandler dh = new X12DocumentHandler();
		try {

			dh.startParsing(new FileReader("testFiles/5-850.x12"));
		} catch (OBOEException oe) {
			if (oe.getMessage().compareTo("Parsing Errors.") == 0) {
				DocumentErrors de = oe.getDocumentErrors();
				for (int i = 0; i < de.getErrorCount(); i++) {
					logr.info(de.getErrorID(i) + " ");
					logr.info(de.getErrorPosition(i) + " ");
					logr.info(de.getErrorDescription(i) + " ");
					logr.info(de.getContainer(i).getID());
				}
			}

			fail(oe.getMessage());
		} catch (Exception e) {
			logr.error(e.getMessage(), e);
			fail(e.getMessage());
		}

		Table dt = dh.getEnvelope().getFunctionalGroup(0).getTransactionSet(0)
				.getDetailTable();
		Loop lp = dt.getLoop("PO1", 0);
		if (lp == null) {
			fail("Can't find Loop PO1");
		}

		Segment seg = lp.getSegment("PO1");
		if (seg == null) {
			fail("Can't find PO1");
		}

		seg = lp.getLoop("SAC", 0).getSegment("SAC");
		if (seg == null) {
			fail("Can't find SAC");
		}

		Segment nte = lp.getLoop("SAC", 0).getSegment("NTE", 0);
		if (nte == null) {
			fail("Can't find first NTE");
		}

		nte = lp.getLoop("SAC", 0).getSegment("NTE", 1);
		if (nte == null) {
			fail("Can't find second NTE");
		}

		nte = lp.getLoop("SAC", 0).getSegment("NTE", 2);
		if (nte == null) {
			fail("Can't find third NTE");
		}

	}

	@Test
	public void testEmptyDEFetchAndGet() {
		X12DocumentHandler dh = new X12DocumentHandler();
		try {
			dh.startParsing(new FileReader("testFiles/sample.001"));
		} catch (OBOEException oe) {
			if (oe.getMessage().equals("Parsing Errors.")) {
				DocumentErrors de = oe.getDocumentErrors();
				for (int i = 0; i < de.getErrorCount(); i++) {
					logr.info(de.getErrorID(i) + " ");
					logr.info(de.getErrorPosition(i) + " ");
					logr.info(de.getErrorDescription(i) + " ");
					logr.info(de.getContainer(i).getID());
				}
			}

		} catch (Exception e) {
			logr.error(e.getMessage(), e);
			fail(e.getMessage());
		}

		String expected = """
				ISA*00*          *00*          *01*001234567      *01*007654321      *990605*1325*U*00401*000000250*0*P*:
				GS*ZZ*001234567*007654321*19990605*1325*250*X*004010
				ST*001*0001
				A1*1*2
				A2*1**3
				SE*4*0001
				GE*1*250
				IEA*1*000000250
								""";

		assertEquals(expected,
				dh.getEnvelope().getFormattedText(Format.X12_FORMAT));

	}

	@Test

	public void testBadNumericDE() {
		X12DocumentHandler dh = new X12DocumentHandler();
		try {

			dh.startParsing(new FileReader("testFiles/sample.001.badnum"));
		} catch (OBOEException oe) {
			if (oe.getMessage()
					.startsWith("Invalid or unknown position for Element")) {
				;
			} else {
				fail(oe.getMessage());
			}

		} catch (Exception e) {
			logr.error(e.getMessage(), e);
			fail(e.getMessage());
		} finally {
			StringBuilder sb = new StringBuilder(
					"ISA*00*          *00*          *01*001234567      *01*007654321      *990605*1325*U*00401*000000250*0*P*:\n");
			sb.append("GS*ZZ*001234567*007654321*19990605*1325*250*X*004010\n");
			sb.append("ST*001*0001\n");
			sb.append("A1*1*2\n");
			sb.append("A2*1**\n\r\n" + "missing IEA segment"); // the second
																// field
																// should
																// be missing
			assertEquals(sb.toString(), dh.getEnvelope()
					.getFormattedText(Format.X12_FORMAT).trim());
		}

	}

	@Test
	public void testDoPrevalidateProperty() {
		X12DocumentHandler dh = new X12DocumentHandler();
		try {
			dh.startParsing(new FileReader("testFiles/sample.output.840.1"));
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}

		try {
			Util.setOBOEProperty("doPrevalidate", "false");
			Table th = dh.getEnvelope().getFunctionalGroup(0)
					.getTransactionSet(0).getHeaderTable();
			th.getSegment("BQT", "00");
			fail("should fail with \"Method does not work with...");
		} catch (OBOEException oe) {
			if (oe.getMessage()
					.startsWith("Method does not work with this type of")) {
				logr.debug("exception here is okay");
			} else {
				fail(oe.getMessage());
			}
		}

		try {
			Util.setOBOEProperty("doPrevalidate", "true");
			Table th = dh.getEnvelope().getFunctionalGroup(0)
					.getTransactionSet(0).getHeaderTable();
			th.getSegment("BQT", "00");
		} catch (OBOEException oe) {
			fail(oe.getMessage());
		}

	}

}
