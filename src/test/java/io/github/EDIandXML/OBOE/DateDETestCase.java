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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.DataElements.DateDE;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.util.Util;

public class DateDETestCase {

	private DateDE de, de6, de8;
	private String value8;
	private String value6;
	private String prettyDate;

	@BeforeEach
	protected void setUp() {

		de = new DateDE(
				new TemplateDataElement("ID", "Name", 1, "DT", 'R',
						"Description", 6, 8, "xmltag", null, null, 1, true),
				null);
		de6 = new DateDE(
				new TemplateDataElement("ID", "Name", 1, "DT", 'R',
						"Description", 6, 6, "xmltag", null, null, 1, true),
				null);
		de8 = new DateDE(
				new TemplateDataElement("ID", "Name", 1, "DT", 'R',
						"Description", 8, 8, "xmltag", null, null, 1, true),
				null);

		de.set(Util.currentDate());
		de6.set(Util.currentDate().substring(2));
		de8.set(Util.currentDate());

		SimpleDateFormat sdf;

		sdf = new SimpleDateFormat("MMM dd, yyyy");

		prettyDate = sdf.format(Calendar.getInstance().getTime());

	}

	@Test
	public void testReset() {
		// it seems that the application is resetting the date
		// if it's invalid. user wants the original bad data

		try {
			de.set("20041");
		} catch (OBOEException oe) {
			;
		}
		// de.validate(err);
		// if (err.getErrorCount() != 1)
		// fail("validate method failed to find error");
		if (de.get().compareTo("20041") != 0) {
			fail("set method altered data " + de.get());
		}

	}

	@AfterEach
	protected void tearDown() {
		de = null;
	}

	@Test
	public void testAll3Gets() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf6 = new SimpleDateFormat("yyMMdd");
		SimpleDateFormat sdf8 = new SimpleDateFormat("yyyyMMdd");
		value8 = sdf8.format(calendar.getTime());
		value6 = sdf6.format(calendar.getTime());

		assertEquals(value8, de.getFormattedText(Format.X12_FORMAT));
		assertEquals(value6, de6.getFormattedText(Format.X12_FORMAT));
		assertEquals(value8, de.getFormattedText(Format.X12_FORMAT));

	}

	@Test
	public void testParms() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf8 = new SimpleDateFormat("yyyyMMdd");
		value8 = sdf8.format(calendar.getTime());

		assertEquals(value8, de.get());
		assertEquals("ID", de.getID());
		assertEquals("Name", de.getName());
		assertEquals(1, de.getPosition());
		assertEquals("Description", de.getDescription());
		assertEquals('R', de.getRequired());
		assertEquals(6, de.getMinLength());
		assertEquals(8, de.getMaxLength());
		assertEquals("xmltag", de.getShortName());
	}

	@Test

	public void test6() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf6 = new SimpleDateFormat("yyMMdd");
		value6 = sdf6.format(calendar.getTime());
		assertEquals("\tName: " + prettyDate + Util.lineFeed,
				de6.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>" + value6 + "</xmltag>" + Util.lineFeed,
				de6.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"> <value>" + value6
						+ "</value></element>" + Util.lineFeed,
				de6.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals(value6, de6.getFormattedText(Format.X12_FORMAT));
		assertEquals(value6, de6.getFormattedText(Format.EDIFACT_FORMAT));
	}

	@Test
	public void testFormattedText() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf8 = new SimpleDateFormat("yyyyMMdd");
		value8 = sdf8.format(calendar.getTime());

		assertEquals("\tName: " + prettyDate + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>" + value8 + "</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"> <value>" + value8
						+ "</value></element>" + Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals(value8, de.getFormattedText(Format.X12_FORMAT));
		assertEquals(value8, de.getFormattedText(Format.EDIFACT_FORMAT));
	}

	@Test

	public void testValidate() {
		assertEquals(
				" invalid character at 0 invalid character at 1 invalid character at 2 invalid character at 3 invalid character at 4 invalid character at 5 invalid character at 6 invalid character at 7",
				de.validate("abcdefgh"));
		assertEquals(" invalid character at 6 invalid character at 7",
				de.validate("123456gh"));
		assertEquals("invalid year 1800", de.validate("18000000"));
		assertEquals("invalid year 2211", de.validate("22110000"));
		assertEquals("invalid month 00", de.validate("20000000"));
		assertEquals("invalid month 13", de.validate("20001300"));

		assertEquals("invalid day 00", de.validate("20000100"));
		assertEquals("invalid day 32", de.validate("20000132"));

		assertEquals("invalid day 31", de.validate("20000931"));
		assertEquals("invalid day 31", de.validate("20000431"));
		assertEquals("invalid day 31", de.validate("20000631"));
		assertEquals("invalid day 31", de.validate("20001131"));

		assertEquals("invalid day 32", de.validate("20000332"));
		assertEquals("invalid day 32", de.validate("20000532"));
		assertEquals("invalid day 32", de.validate("20000732"));
		assertEquals("invalid day 32", de.validate("20000832"));
		assertEquals("invalid day 32", de.validate("20001032"));
		assertEquals("invalid day 32", de.validate("20001232"));

		assertEquals("invalid day 29", de.validate("20010229"));
		assertEquals("invalid day 30", de.validate("20040230"));
		assertEquals("invalid day 30", de.validate("040230"));
		assertEquals("invalid day 30", de.validate("20000230"));

		assertEquals("invalid day 32", de.validate("20000132"));
		assertEquals(null, de.validate("20000101"));
		assertEquals(null, de.validate("20000229"));
		assertEquals(null, de.validate("20080229"));
		assertEquals(null, de.validate("20040229"));
		assertEquals(null, de.validate("040229"));
		assertEquals(null, de.validate("20010228"));

		try {
			DocumentErrors derr = new DocumentErrors();
			de.set("20080229");
			de.validate(derr);
			assertEquals(0, derr.getErrorCount());
			de6.set("080229");
			de6.validate(derr);
			assertEquals(0, derr.getErrorCount());
			de8.set("20080229");
			de8.validate(derr);
			assertEquals(0, derr.getErrorCount());

		} catch (OBOEException oe) {
			fail("should not throw exception");
		}

		try {
			de.set("123");
			fail("should throw exception");
		} catch (OBOEException oe) {
			;
		}
		try {
			de.set("00000000");
			fail("should throw exception");
		} catch (OBOEException oe) {
			;
		}
		try {
			de.set("abcdefgh");
			fail("should throw exception");
		} catch (OBOEException oe) {
			;
		}

	}

}