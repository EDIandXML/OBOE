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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;
import io.github.EDIandXML.OBOE.x12.X12Envelope;

public class OBOEValidateTestCase {

	ElementRules er;
	X12Envelope xenv;
	static Logger logr = LogManager.getLogger(OBOEValidateTestCase.class);

	@BeforeEach
	protected void setUp() {
		EnvelopeFactory.reloadbuiltTable();
		xenv = new X12Envelope(
				EnvelopeFactory.buildEnvelope("x12.envelope", "notSetYet"));
		testValidate();
	}

	@Test
	public void testValidate() {
		try {
			er = new ElementRules("anything but", "");
		} catch (OBOEException e1) {
			if (e1.getMessage().startsWith("Unknown rule") == false) {
				fail("unknown rule test " + e1.getMessage());
			}
		}

		try {
			er = new ElementRules(ElementRules.oneOrMoreMustExist, "");
		} catch (OBOEException e1) {
			if (e1.getMessage().startsWith(
					"Not enough positions specified in string") == false) {
				fail("position length check " + e1.getMessage());
			}
		}
		try {
			er = new ElementRules(ElementRules.oneOrMoreMustExist, "x,x");
		} catch (OBOEException e1) {
			if (e1.getMessage()
					.startsWith("Invalid character in positions") == false) {
				fail("Invalid character test " + e1.getMessage());
			}
		}
		try {
			er = new ElementRules(ElementRules.oneOrMoreMustExist, "1x,x");
		} catch (OBOEException e1) {
			if (e1.getMessage()
					.startsWith("Invalid character in positions") == false) {
				fail("Invalid character test 2 " + e1.getMessage());
			}
		}
		try {
			er = new ElementRules(ElementRules.oneOrMoreMustExist, "1,2");
		} catch (OBOEException e1) {
			if (e1.getMessage()
					.startsWith("Invalid character in positions") == false) {
				fail("Invalid character test 3 " + e1.getMessage());
			}
		}
		try {
			er = new ElementRules(ElementRules.oneOrMoreMustExist, "1;2");
		} catch (OBOEException e1) {
			if (e1.getMessage()
					.startsWith("Invalid character in positions") == false) {
				fail("Invalid character test 4 " + e1.getMessage());
			}
		}

		try {
			er = new ElementRules(ElementRules.oneOrMoreMustExist, "1,1");
		} catch (OBOEException e1) {
			if (e1.getMessage()
					.indexOf("specified more than once in string") < 0) {
				fail("Invalid character test 5 " + e1.getMessage());
			}
		}

		try {
			er = new ElementRules(ElementRules.oneOrMoreMustExist, "1,2,");
		} catch (OBOEException e1) {
			if (e1.getMessage().startsWith(
					"Based on count there are not enough") == false) {
				fail("Invalid character test 6 " + e1.getMessage());
			}
		}

		try {
			er = new ElementRules(ElementRules.oneOrMoreMustExist,
					"1 2, 3, 5 6");
		} catch (OBOEException e1) {
			if (e1.getMessage().startsWith("Invalid position") == false) {
				fail("Invalid character test 7 " + e1.getMessage());
			}
		}
	}

	@Test
	public void testoneOrMoreMustExist() {
		Object obj[] = new Object[0];
		Segment seg = xenv.createSegment(X12Envelope.idInterchangeHeader);
		er = new ElementRules(ElementRules.oneOrMoreMustExist, "1, 2, 3, 5, 6");
		assertTrue(er.testRules(obj, seg, false).indexOf(
				"at least one of these data element fields must be used:") > -1);
		String testStrings[] = new String[2];
		testStrings[0] = "1";
		testStrings[1] = "2";
		assertEquals(null, er.testRules(testStrings, seg, false));
	}

	@Test
	public void testifFirstExistsThenAllMustExist() {
		Object obj[] = new Object[0];
		Segment seg = xenv.createSegment(X12Envelope.idInterchangeHeader);
		er = new ElementRules(ElementRules.ifFirstExistsThenAllMustExist,
				"1,2,3");
		assertEquals(null, er.testRules(obj, seg, false));
		String testStrings[] = new String[2];
		testStrings[0] = "1";
		testStrings[1] = "2";
		assertTrue(er.testRules(testStrings, seg, false)
				.indexOf("is used then you must fill in") > -1);
		testStrings = new String[3];
		testStrings[0] = "1";
		testStrings[1] = "2";
		testStrings[2] = "3";
		assertEquals(null, er.testRules(testStrings, seg, false));
	}

	@Test
	public void testoneAndOnlyOneMayExist() {
		Object obj[] = new Object[0];
		Segment seg = xenv.createSegment(X12Envelope.idInterchangeHeader);
		er = new ElementRules(ElementRules.oneAndOnlyOneMayExist, "1,2,3");
		assertEquals(null, er.testRules(obj, seg, false));
		String testStrings[] = new String[2];
		testStrings[0] = "1";
		testStrings[1] = "2";
		assertTrue(er.testRules(testStrings, seg, false)
				.indexOf("enter data into only ONE of these fields") > -1);
		testStrings = new String[3];
		testStrings[2] = "3";
		assertEquals(null, er.testRules(testStrings, seg, false));
	}

	@Test
	public void testifFirstExistsThenAtLeastOneMoreMustExist() {
		Object obj[] = new Object[0];
		Segment seg = xenv.createSegment(X12Envelope.idInterchangeHeader);
		er = new ElementRules(
				ElementRules.ifFirstExistsThenAtLeastOneMoreMustExist, "3,2,1");
		assertEquals(null, er.testRules(obj, seg, false));
		String testStrings[] = new String[3];
		testStrings[2] = "3";
		assertTrue(er.testRules(testStrings, seg, false)
				.indexOf("is used then use least one o") > -1);
		testStrings = new String[3];
		testStrings[0] = "1";
		testStrings[2] = "3";
		assertEquals(null, er.testRules(testStrings, seg, false));
		testStrings[1] = "2";
		assertEquals(null, er.testRules(testStrings, seg, false));

	}

	@Test
	public void testallOrNoneMayExist() {
		Object obj[] = new Object[0];
		Segment seg = xenv.createSegment(X12Envelope.idInterchangeHeader);
		er = new ElementRules(ElementRules.allOrNoneMayExist, "1,2,3");
		assertEquals(null, er.testRules(obj, seg, false));
		String testStrings[] = new String[3];
		testStrings[0] = "1";
		testStrings[2] = "3";
		assertTrue(er.testRules(testStrings, seg, false)
				.indexOf("data into none or all of the") > -1);
		testStrings[1] = "2";
		assertEquals(null, er.testRules(testStrings, seg, false));
	}

	@Test
	public void testifFirstExistsThenNoOthersMayExist() {
		Object obj[] = new Object[0];
		Segment seg = xenv.createSegment(X12Envelope.idInterchangeHeader);
		er = new ElementRules(ElementRules.ifFirstExistsThenNoOthersMayExist,
				"1,2,3");
		assertEquals(null, er.testRules(obj, seg, false));
		String testStrings[] = new String[3];
		testStrings[0] = "1";
		testStrings[2] = "3";
		assertTrue(er.testRules(testStrings, seg, false)
				.indexOf("is used then don't use the following") > -1);
		testStrings[1] = "2";
		assertTrue(er.testRules(testStrings, seg, false)
				.indexOf("is used then don't use the following") > -1);

		testStrings[2] = "";
		testStrings[1] = "";
		assertEquals(null, er.testRules(testStrings, seg, false));
	}

	@Test
	public void testDataRemoval() throws IOException {
		FileReader fr = new FileReader("testFiles/sample.output.840.1");
		X12DocumentHandler p1;
		String de[];

		try {
			p1 = new X12DocumentHandler(fr);
		} catch (OBOEException oe) {
			if (oe.getDocumentErrors() != null) {
				oe.getDocumentErrors().logErrors();
			} else {
				logr.error(oe.getMessage(), oe);
			}

			return;
		}
		fr.close();
		Envelope x = p1.getEnvelope();

		x.getSegment("ISA").setDataElementValue(2, "  ");
		DocumentErrors des = new DocumentErrors();
		x.validate(des);
		de = des.getError();
		if (de != null) {
			if (de.length != 1) {
				des.logErrors();
				fail("check error messages");
			}
		}
		x.getSegment("ISA").setDataElementValue(2, "00");
		des = new DocumentErrors();
		x.validate(des);
		de = des.getError();
		if (de != null) {
			fail("check error messages");
		}

	}

}