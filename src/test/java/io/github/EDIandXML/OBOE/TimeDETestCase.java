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

import io.github.EDIandXML.OBOE.DataElements.TimeDE;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.util.Util;

public class TimeDETestCase {

	private TimeDE de, de6, de8;

	@BeforeEach
	protected void setUp() {

		de = new TimeDE(new TemplateDataElement("ID", "Name", 1, "TM", 'R',
				"Description", 4, 4, "xmltag", null, null, 1, true), null);
		de6 = new TimeDE(new TemplateDataElement("ID", "Name", 1, "TM", 'R',
				"Description", 4, 6, "xmltag", null, null, 1, true), null);
		de8 = new TimeDE(new TemplateDataElement("ID", "Name", 1, "TM", 'R',
				"Description", 4, 8, "xmltag", null, null, 1, true), null);

	}

	@AfterEach
	protected void tearDown() {
		de = null;
	}

	@Test
	public void testParms() {

		assertEquals("ID", de.getID());
		assertEquals("Name", de.getName());
		assertEquals(1, de.getPosition());
		assertEquals("Description", de.getDescription());
		assertEquals('R', de.getRequired());
		assertEquals(4, de.getMinLength());
		assertEquals(4, de.getMaxLength());
		assertEquals("xmltag", de.getShortName());
	}

	@Test
	public void testFormattedText() {
		 Calendar calendar = Calendar.getInstance();
		 SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
		 String value = sdf.format(calendar.getTime());

		de.set(value);

		assertEquals(
				"\tName: " + sdf.format(calendar.getTime()) + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals(
				"<xmltag>" + sdf.format(calendar.getTime()) + "</xmltag>"
						+ Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"> <value>"
						+ sdf.format(calendar.getTime()) + "</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals(value, de.getFormattedText(Format.X12_FORMAT));
		assertEquals(value, de.getFormattedText(Format.EDIFACT_FORMAT));
	}

	@Test
	public void testFormattedText6() {
		 Calendar calendar = Calendar.getInstance();
		 SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
		 String value = sdf.format(calendar.getTime());
		de6.set(value);

		assertEquals(
				"\tName: " + sdf.format(calendar.getTime()) + Util.lineFeed,
				de6.getFormattedText(Format.UNDEFINED));
		assertEquals(
				"<xmltag>" + sdf.format(calendar.getTime()) + "</xmltag>"
						+ Util.lineFeed,
				de6.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"> <value>"
						+ sdf.format(calendar.getTime()) + "</value></element>"
						+ Util.lineFeed,
				de6.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals(value, de6.getFormattedText(Format.X12_FORMAT));
		assertEquals(value, de6.getFormattedText(Format.EDIFACT_FORMAT));
	}

	@Test
	public void testFormattedText8() {
		 Calendar calendar = Calendar.getInstance();
		 SimpleDateFormat sdf = new SimpleDateFormat("HHmmssSSS");
		 String value = sdf.format(calendar.getTime()).substring(0, 8);
		de8.set(value);

		assertEquals("\tName: " + sdf.format(calendar.getTime()).substring(0, 8)
				+ Util.lineFeed, de8.getFormattedText(Format.UNDEFINED));
		assertEquals(
				"<xmltag>" + sdf.format(calendar.getTime()).substring(0, 8)
						+ "</xmltag>" + Util.lineFeed,
				de8.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"> <value>"
						+ sdf.format(calendar.getTime()).substring(0, 8)
						+ "</value></element>" + Util.lineFeed,
				de8.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals(value, de8.getFormattedText(Format.X12_FORMAT));
		assertEquals(value, de8.getFormattedText(Format.EDIFACT_FORMAT));
	}

	/*
	 * public void testSetTimes() { de.set(Calendar.HOUR_OF_DAY, 20);
	 * assertEquals(20, de.get(Calendar.HOUR_OF_DAY));
	 * de.set(Calendar.MINUTE, 3); assertEquals(3, de.get(Calendar.MINUTE));
	 * assertEquals("2003", de.get());
	 *
	 * }
	 */
	@Test
	public void testValidate() {
		try {
			de.set("123");
			fail("should throw exception");
		} catch ( OBOEException oe) {
			;
		}
		try {
			de.set("12345");
			fail("should throw exception");
		} catch ( OBOEException oe) {
			;
		}
		try {
			de8.set("1234");
		} catch ( OBOEException oe) {
			fail(oe.getLocalizedMessage());
		}
		try {
			de8.set("123456");
		} catch ( OBOEException oe) {
			fail(oe.getLocalizedMessage());
		}
		try {
			de8.set("1234567");
		} catch ( OBOEException oe) {
			fail(oe.getLocalizedMessage());
		}
		try {
			de8.set("12345678");
		} catch ( OBOEException oe) {
			fail(oe.getLocalizedMessage());
		}
		try {
			de8.set("3730");
			fail("should throw exception");
		} catch ( OBOEException oe) {
			;
		}
		try {
			de8.set("1269");
			fail("should throw exception");
		} catch ( OBOEException oe) {
			;
		}
		try {
			de8.set("123079");
			fail("should throw exception");
		} catch ( OBOEException oe) {
			;
		}
	}

}