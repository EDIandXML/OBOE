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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.DataElements.RealDE;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.util.Util;

public class RealDETestCase {

	private RealDE de, de2, de3, de4, de5;
	static Logger logr = LogManager.getLogger(RealDETestCase.class);

	@BeforeEach
	protected void setUp() {

		de = new RealDE(
				new TemplateDataElement("ID", "Name", 1, "R", 'R',
						"Description", 5, 5, "xmltag", null, null, 1, true),
				null);
		de2 = new RealDE(
				new TemplateDataElement("ID", "Name", 2, "R", 'R',
						"Description", 5, 5, "xmltag", null, null, 1, true),
				null);
		de3 = new RealDE(
				new TemplateDataElement("ID", "Name", 2, "R", 'R',
						"Description", 4, 10, "xmltag", null, null, 1, true),
				null);
		de4 = new RealDE(
				new TemplateDataElement("ID", "Name", 4, "R", 'R',
						"Description", 1, 10, "xmltag", null, null, 1, true),
				null);
		de5 = new RealDE(
				new TemplateDataElement("ID", "Name", 4, "R", 'R',
						"Description", 1, 17, "xmltag", null, null, 1, true),
				null);
		Util.setOBOEProperty(Util.REAL_NUMBERS_RETAIN_PRECISION, "false");

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
		assertEquals(5, de.getMinLength());
		assertEquals(5, de.getMaxLength());
		assertEquals("xmltag", de.getShortName());
	}

	@Test

	public void testNotSet() {
		assertEquals("\tName: null" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag></xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
	}

	@Test

	public void testUnformatted() {
		de.set("10");
		assertEquals("\tName: 10.000" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>10.000</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"><value>10.000</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("10.000", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("10.000", de.getFormattedText(Format.EDIFACT_FORMAT));
		de.set("1000");
		assertEquals("\tName: 1000.0" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>1000.0</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"><value>1000.0</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("1000.0", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("1000.0", de.getFormattedText(Format.EDIFACT_FORMAT));

		de2.set("1010");
		assertEquals("\tName: 1010.0" + Util.lineFeed,
				de2.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>1010.0</xmltag>" + Util.lineFeed,
				de2.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"><value>1010.0</value></element>"
						+ Util.lineFeed,
				de2.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("1010.0", de2.getFormattedText(Format.X12_FORMAT));
		assertEquals("1010.0", de2.getFormattedText(Format.EDIFACT_FORMAT));

		de2.set("10.1");
		assertEquals("\tName: 10.100" + Util.lineFeed,
				de2.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>10.100</xmltag>" + Util.lineFeed,
				de2.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"><value>10.100</value></element>"
						+ Util.lineFeed,
				de2.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("10.100", de2.getFormattedText(Format.X12_FORMAT));
		assertEquals("10.100", de2.getFormattedText(Format.EDIFACT_FORMAT));

		de2.set("6249.9998");
		assertEquals("\tName: 6249.9998" + Util.lineFeed,
				de2.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>6249.9998</xmltag>" + Util.lineFeed,
				de2.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"><value>6249.9998</value></element>"
						+ Util.lineFeed,
				de2.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("6249.9998", de2.getFormattedText(Format.X12_FORMAT));
		assertEquals("6249.9998", de2.getFormattedText(Format.EDIFACT_FORMAT));
	}

	@Test
	public void testFormattedText() {
		de.set("10");
		assertEquals("\tName: 10.000" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>10.000</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"><value>10.000</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("10.000", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("10.000", de.getFormattedText(Format.EDIFACT_FORMAT));
		de.set("1000");
		assertEquals("\tName: 1000.0" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>1000.0</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"><value>1000.0</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("1000.0", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("1000.0", de.getFormattedText(Format.EDIFACT_FORMAT));
		de.set("1,000");
		assertEquals("\tName: 1000.0" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>1000.0</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"><value>1000.0</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("1000.0", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("1000.0", de.getFormattedText(Format.EDIFACT_FORMAT));
		de.set("100.00");
		assertEquals("\tName: 100.00" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>100.00</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"><value>100.00</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("100.00", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("100.00", de.getFormattedText(Format.EDIFACT_FORMAT));
		de.set("100.10");
		assertEquals("\tName: 100.10" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>100.10</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"><value>100.10</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("100.10", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("100.10", de.getFormattedText(Format.EDIFACT_FORMAT));
		de.set("6249.9998");
		assertEquals("\tName: 6249.9998" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>6249.9998</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"><value>6249.9998</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("6249.9998", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("6249.9998", de.getFormattedText(Format.EDIFACT_FORMAT));
	}

	@Test
	public void testFloater() {
		de3.set("1.0000");
		assertEquals("\tName: 1.000" + Util.lineFeed,
				de3.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>1.000</xmltag>" + Util.lineFeed,
				de3.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"><value>1.000</value></element>"
						+ Util.lineFeed,
				de3.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("1.000", de3.getFormattedText(Format.X12_FORMAT));
		assertEquals("1.000", de3.getFormattedText(Format.EDIFACT_FORMAT));

	}

	@Test
	public void testNegatives() {
		de3.set("-1.00");
		assertEquals("-1.00", de3.getFormattedText(Format.X12_FORMAT));
		de4.set("-1");
		assertEquals("-1", de4.getFormattedText(Format.X12_FORMAT));
		de4.set("-18.02");
		assertEquals("-18.02", de4.getFormattedText(Format.X12_FORMAT));
	}

	@Test
	public void testLeadingZero() {
		de3.set("1.0000");
		assertEquals("\tName: 1.000" + Util.lineFeed,
				de3.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>1.000</xmltag>" + Util.lineFeed,
				de3.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"><value>1.000</value></element>"
						+ Util.lineFeed,
				de3.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("1.000", de3.getFormattedText(Format.X12_FORMAT));
		assertEquals("1.000", de3.getFormattedText(Format.EDIFACT_FORMAT));

		de4.set(".80");
		assertEquals(".8", de4.getFormattedText(Format.X12_FORMAT));
		de4.set("0.80");
		assertEquals(".8", de4.getFormattedText(Format.X12_FORMAT));
		de4.set("1.80");
		assertEquals("1.8", de4.getFormattedText(Format.X12_FORMAT));
		de4.set("1");
		assertEquals("1", de4.getFormattedText(Format.X12_FORMAT));
		de3.set("1");
		assertEquals("1.000", de3.getFormattedText(Format.X12_FORMAT));
	}

	@Test
	public void testNotForPrecision() {
		de5.set("15.00");
		assertEquals("\tName: 15" + Util.lineFeed,
				de5.getFormattedText(Format.UNDEFINED));
		de5.set("15.10");
		assertEquals("\tName: 15.1" + Util.lineFeed,
				de5.getFormattedText(Format.UNDEFINED));
		de5.set("15.");
		assertEquals("\tName: 15" + Util.lineFeed,
				de5.getFormattedText(Format.UNDEFINED));
		de5.set("15");
		assertEquals("\tName: 15" + Util.lineFeed,
				de5.getFormattedText(Format.UNDEFINED));
	}

	@Test
	public void testForPrecision() {
		Util.setOBOEProperty("realNumbersRetainPrecision", "true");
		de5.set("15.00");
		assertEquals("\tName: 15.00" + Util.lineFeed,
				de5.getFormattedText(Format.UNDEFINED));
		de5.set("15.10");
		assertEquals("\tName: 15.10" + Util.lineFeed,
				de5.getFormattedText(Format.UNDEFINED));
		de5.set("15.");
		assertEquals("\tName: 15." + Util.lineFeed,
				de5.getFormattedText(Format.UNDEFINED));
		de5.set("15");
		assertEquals("\tName: 15" + Util.lineFeed,
				de5.getFormattedText(Format.UNDEFINED));
	}

	@Test
	public void testBadData() {

		try {
			de.set("21Addd");
			fail("should throw exception");
		} catch (Exception e1) // assume exception is thrown
		{
			logr.info(e1.getMessage());
		}
		try {
			de2.set("a12");
			fail("should throw exception");
		} catch (Exception e2) // assume exception is thrown
		{
			logr.info(e2.getMessage());
		}
		try {
			de3.set("###");
			fail("should throw exception");
		} catch (Exception e3) // assume exception is thrown
		{
			logr.info(e3.getMessage());
		}
		try {
			de4.set("aaa");
			fail("should throw exception");
		} catch (Exception e4) // assume exception is thrown
		{
			logr.info(e4.getMessage());
		}
	}

	@Test
	public void testLargeValue() {
		Util.setOBOEProperty(Util.REAL_NUMBERS_RETAIN_PRECISION, "true");
		de5.set("11496067.46");
		DocumentErrors derr = new DocumentErrors();
		assertTrue(de5.validate(derr));
		assertEquals(0, derr.getErrorCount());
		assertEquals(null, de5.validate("11496067.46"));
		assertEquals("11496067.46", de5.get());
	}

}
