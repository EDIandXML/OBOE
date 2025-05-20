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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.util.Util;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;

public class BadDocumentTestCase {
	static Logger logr = LogManager.getLogger();

	/**
	 *
	 * @throws OBOEException
	 */
	@Test
	public void testWeirdSegment() throws OBOEException {

		Util.setOBOEProperty(Util.THROW_PARSING_EXCEPTION, "false");
		StringBuilder sb = new StringBuilder();
		sb.append(
				"ISA*Y1*AUTH      *Y3*SEC       *Y5*SENDERID       *Y7*RECEIVERID     *010101*1300*Y*Y12  *000000001*x*z*!\n");
		sb.append("GS*fg0*fg1*fg2*010203\n");
		sb.append("ST*840*00001\n");
		sb.append("BQT*363*586*20010103*200\n");
		sb.append("PO1*PO1350*1*12*1232\n");
		sb.append("@@@@*LIN350*2 *LINE3|- Christina <3\n");
		sb.append("G53*875\n");
		sb.append("ju8*1*1\n");
		sb.append("SE*1*00001\n");
		sb.append("GE*1*1\n");
		sb.append("IEA*1*000000001\n");

		X12DocumentHandler xdh = new X12DocumentHandler(
				new StringReader(sb.toString()));
		assertTrue(xdh.getDocumentErrors().getErrorCount() > 0);

		StringWriter sw = new StringWriter();
		try {
			xdh.getDocumentErrors().writeErrors(sw);
			sw.flush();
			assertTrue(sw.toString().contains("@@@@"));
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
			e.printStackTrace();
		}

	}

	/**
	 *
	 * @throws OBOEException
	 */
	@Test
	public void testDoesntEndWell() throws OBOEException {

		Util.setOBOEProperty(Util.THROW_PARSING_EXCEPTION, "false");
		StringBuilder sb = new StringBuilder();
		sb.append(
				"ISA*Y1*AUTH      *Y3*SEC       *Y5*SENDERID       *Y7*RECEIVERID     *010101*1300*Y*Y12  *000000001*x*z*!\n");
		sb.append("GS*fg0*fg1*fg2*010203\n");
		sb.append("ST*840*00001\n");
		sb.append("BQT*363*586*20010103*200\n");
		sb.append("PO1*PO1350*1*12*1232\n");
		sb.append("@@@@*LIN350*2 *LINE3|- Christina <3\n");
		sb.append("G53*875\n");
		sb.append("ju8*1*1\n");
		sb.append("GE*1*1\n");
		sb.append("IEA*1*000000001\n");

		X12DocumentHandler xdh = new X12DocumentHandler(
				new StringReader(sb.toString()));
		assertTrue(xdh.getDocumentErrors().getErrorCount() > 0);

		StringWriter sw = new StringWriter();
		try {
			xdh.getDocumentErrors().writeErrors(sw);
			sw.flush();
			assertTrue(sw.toString().contains("@@@@"));
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
			e.printStackTrace();
		}

	}

	@Test
	public void testDoesntEndWellatFunctionalGroup() throws OBOEException {

		Util.setOBOEProperty(Util.THROW_PARSING_EXCEPTION, "false");
		StringBuilder sb = new StringBuilder();
		sb.append(
				"ISA*Y1*AUTH      *Y3*SEC       *Y5*SENDERID       *Y7*RECEIVERID     *010101*1300*Y*Y12  *000000001*x*z*!\n");
		sb.append("GS*fg0*fg1*fg2*010203\n");
		sb.append("ST*840*00001\n");
		sb.append("BQT*363*586*20010103*200\n");
		sb.append("PO1*PO1350*1*12*1232\n");
		sb.append("@@@@*LIN350*2 *LINE3|- Christina <3\n");
		sb.append("G53*875\n");
		sb.append("ju8*1*1\n");
		sb.append("IEA*1*000000001\n");

		X12DocumentHandler xdh = new X12DocumentHandler(
				new StringReader(sb.toString()));
		assertTrue(xdh.getDocumentErrors().getErrorCount() > 0);

		StringWriter sw = new StringWriter();
		try {
			xdh.getDocumentErrors().writeErrors(sw);
			sw.flush();
			assertTrue(sw.toString().contains("@@@@"));
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
			e.printStackTrace();
		}

	}

	@Test
	public void testDoesntEndWellNoEnding() throws OBOEException {

		Util.setOBOEProperty(Util.THROW_PARSING_EXCEPTION, "false");
		StringBuilder sb = new StringBuilder();
		sb.append(
				"ISA*Y1*AUTH      *Y3*SEC       *Y5*SENDERID       *Y7*RECEIVERID     *010101*1300*Y*Y12  *000000001*x*z*!\n");
		sb.append("GS*fg0*fg1*fg2*010203\n");
		sb.append("ST*840*00001\n");
		sb.append("BQT*363*586*20010103*200\n");
		sb.append("PO1*PO1350*1*12*1232\n");

		X12DocumentHandler xdh = new X12DocumentHandler(
				new StringReader(sb.toString()));
		assertTrue(xdh.getDocumentErrors().getErrorCount() > 0);

		StringWriter sw = new StringWriter();
		try {
			xdh.getDocumentErrors().writeErrors(sw);
			sw.flush();
			assertTrue(sw.toString().contains("Missing "));
		} catch (IOException e) {
			fail(e.getLocalizedMessage());
			e.printStackTrace();
		}

	}

}