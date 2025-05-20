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

import io.github.EDIandXML.OBOE.DataElements.NumericDE;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.util.Util;

public class NumericDETestCase {

	private NumericDE de, de2, de3, de4, de5, de6;
	static Logger logr = LogManager.getLogger(NumericDETestCase.class);

	@BeforeEach
	protected void setUp() {

		de = new NumericDE(
				new TemplateDataElement("ID1", "Name", 1, "N0", 'R',
						"Description", 1, 5, "xmltag", null, null, 1, true),
				null);
		de2 = new NumericDE(
				new TemplateDataElement("ID2", "Name", 2, "N2", 'R',
						"Description", 5, 5, "xmltag", null, null, 1, true),
				null);
		de3 = new NumericDE(
				new TemplateDataElement("ID3", "Name", 3, "N", 'R',
						"Description", 1, 6, "xmltag", null, null, 1, true),
				null);
		de4 = new NumericDE(
				new TemplateDataElement("ID4", "Name", 4, "N2", 'R',
						"Description", 1, 5, "xmltag", null, null, 1, true),
				null);
		de5 = new NumericDE(
				new TemplateDataElement("ID5", "Name", 5, "N2", 'R',
						"Description", 1, 10, "xmltag", null, null, 1, true),
				null);
		de6 = new NumericDE(
				new TemplateDataElement("ID6", "Name", 6, "N2", 'R',
						"Description", 1, 18, "xmltag", null, null, 1, true),
				null);

	}

	@AfterEach
	protected void tearDown() {
		de = null;
	}

	@Test
	public void testSet() {

		try {
			de.set("1,000");
			fail("should cause exception");
		} catch (Exception e1) {
			;
		}
		try {
			de2.set("21q5.30");
			fail("should cause exception");
		} catch (Exception e1) {
			logr.info(e1.getMessage());
		}
		try {
			de.set("10000.");
			fail("should cause exception");
		} catch (Exception e1) {
			;
		}
		try {
			de.set("100.00");
			fail("should cause exception");
		} catch (Exception e1) {
			;
		}
		try {
			de.set("102");
		} catch (Exception e1) {
			fail("should not cause exception");
			;
		}
		try {
			de.set("00000");
		} catch (Exception e1) {
			fail("should not cause exception");
			;
		}
		try {
			de.set(".11000");
			fail("should cause exception");
		} catch (Exception e1) {
			;
		}
		try {
			de.set("-.100000");
			fail("should cause exception");
		} catch (Exception e1) {
			;
		}
		try {
			de.set("-0101");
		} catch (Exception e1) {
			fail("should not cause exception");
			;
		}
		try {
			de.set("-0100");
		} catch (Exception e1) {
			fail("should not cause exception");
			;
		}

		try {
			de2.set("1,000");
			fail("should cause exception");
		} catch (Exception e1) {
			;
		}
		try {
			de2.set("10000.");
			fail("should cause exception");
		} catch (Exception e1) {
			;
		}
		try {
			de2.set("100.00");
			fail("should cause exception");
		} catch (Exception e1) {
			;
		}
		try {
			de2.set("102");
		} catch (Exception e1) {
			fail("should not cause exception");
			;
		}
		try {
			de2.set("00000");
		} catch (Exception e1) {
			fail("should not cause exception");
			;
		}
		try {
			de2.set(".11000");
			fail("should cause exception");
		} catch (Exception e1) {
			;
		}
		try {
			de2.set("-.100000");
			fail("should cause exception");
		} catch (Exception e1) {
			;
		}
		try {
			de2.set("-0101");
		} catch (Exception e1) {
			fail("should not cause exception");
			;
		}
		try {
			de2.set("-21010");
		} catch (Exception e1) {
			fail("should not cause exception");
			;
		}
		try {
			de2.set("-0100");
		} catch (Exception e1) {
			fail("should not cause exception");
			;
		}

		de3.set("1000");
		assertEquals(null, de3.validate(de3.get()));
		assertEquals("1000", de3.get());
		de3.set("1.000");
		assertEquals(null, de3.validate(de3.get()));
		assertEquals("1.000", de3.get());
		de3.set("100.00");
		assertEquals(null, de3.validate(de3.get()));
		assertEquals("100.00", de3.get());
		de3.set("102");
		assertEquals(null, de3.validate(de3.get()));
		assertEquals("102", de3.get());
		de3.set("00000");
		assertEquals(null, de3.validate(de3.get()));
		assertEquals("00000", de3.get());
		de3.set(".11000");
		assertEquals(null, de3.validate(de3.get()));
		assertEquals(".11000", de3.get());
		de3.set("-.100000");
		assertEquals("field at position 3 id=ID3 field value too long.",
				de3.validate(de3.get()));
		assertEquals("-.100000", de3.get());
		de3.set("-0101000");
		assertEquals("field at position 3 id=ID3 field value too long.",
				de3.validate(de3.get()));
		assertEquals("-0101000", de3.get());
		de3.set("-0100000");
		assertEquals("field at position 3 id=ID3 field value too long.",
				de3.validate(de3.get()));
		assertEquals("-0100000", de3.get());

		de4.set("-0700");
		assertEquals(null, de4.validate(de4.get()));
		assertEquals("-700", de4.get());

	}

	@Test
	public void testSetFormatted() {

		assertEquals("1000", de.setFormatted("1,000"));
		assertEquals("10000", de.setFormatted("10000."));
		assertEquals("100", de.setFormatted("100.00"));
		assertEquals("102", de.setFormatted("102"));
		assertEquals("0", de.setFormatted("00000"));
		assertEquals("0", de.setFormatted(".11000"));
		assertEquals("0", de.setFormatted("-.100000"));
		assertEquals("-1001", de.setFormatted("-1001"));
		assertEquals("-1", de.setFormatted("-0001"));

		assertEquals("00000", de2.setFormatted("1,000"));
		assertEquals("00000", de2.setFormatted("10000."));
		assertEquals("10000", de2.setFormatted("100.00"));
		assertEquals("10200", de2.setFormatted("102"));
		assertEquals("-10200", de2.setFormatted("-102"));
		assertEquals("00000", de2.setFormatted("00000"));
		assertEquals("00011", de2.setFormatted(".11000"));
		assertEquals("-00010", de2.setFormatted("-.100000"));
		assertEquals("-10100", de2.setFormatted("-0101"));
		assertEquals("-10000", de2.setFormatted("-0100"));

		assertEquals("1,000", de3.setFormatted("1,000"));
		assertEquals("10000.", de3.setFormatted("10000."));
		assertEquals("100.00", de3.setFormatted("100.00"));
		assertEquals("102", de3.setFormatted("102"));
		assertEquals("00000", de3.setFormatted("00000"));
		assertEquals(".11000", de3.setFormatted(".11000"));
		assertEquals("-.100000", de3.setFormatted("-.100000"));
		assertEquals("-0101", de3.setFormatted("-0101"));
		assertEquals("-0100", de3.setFormatted("-0100"));

		assertEquals("-2456949020", de5.setFormatted("-24569490.20"));
		assertEquals("2456949020", de5.setFormatted("24569490.20"));
		assertEquals("24569490.20", de3.setFormatted("24569490.20"));
	}

	@Test
	public void testParms() {
		assertEquals("ID1", de.getID());
		assertEquals("Name", de.getName());
		assertEquals(1, de.getPosition());
		assertEquals("Description", de.getDescription());
		assertEquals('R', de.getRequired());
		assertEquals(1, de.getMinLength());
		assertEquals(5, de.getMaxLength());
		assertEquals("xmltag", de.getShortName());
	}

	@Test
	public void testUnformatted() {
		de.set("10");
		assertEquals("\tName: 10" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>10</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID1\" name=\"Name\"><value>10</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("10", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("10", de.getFormattedText(Format.EDIFACT_FORMAT));
		de.set("1000");
		assertEquals("\tName: 1000" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>1000</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID1\" name=\"Name\"><value>1000</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("1000", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("1000", de.getFormattedText(Format.EDIFACT_FORMAT));

		de2.set("01010");
		assertEquals("\tName: 10.10" + Util.lineFeed,
				de2.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>10.10</xmltag>" + Util.lineFeed,
				de2.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID2\" name=\"Name\"><value>10.10</value></element>"
						+ Util.lineFeed,
				de2.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("01010", de2.getFormattedText(Format.X12_FORMAT));
		assertEquals("01010", de2.getFormattedText(Format.EDIFACT_FORMAT));
		de2.set("01000");
		assertEquals("\tName: 10.00" + Util.lineFeed,
				de2.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>10.00</xmltag>" + Util.lineFeed,
				de2.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID2\" name=\"Name\"><value>10.00</value></element>"
						+ Util.lineFeed,
				de2.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("01000", de2.getFormattedText(Format.X12_FORMAT));
		assertEquals("01000", de2.getFormattedText(Format.EDIFACT_FORMAT));

		de4.set("-0700");
		assertEquals("\tName: -7.00" + Util.lineFeed,
				de4.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>-7.00</xmltag>" + Util.lineFeed,
				de4.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID4\" name=\"Name\"><value>-7.00</value></element>"
						+ Util.lineFeed,
				de4.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("-700", de4.getFormattedText(Format.X12_FORMAT));
		assertEquals("-700", de4.getFormattedText(Format.EDIFACT_FORMAT));
	}

	@Test
	public void testFormattedText() {
		de.setFormatted("10");
		assertEquals("\tName: 10" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>10</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID1\" name=\"Name\"><value>10</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("10", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("10", de.getFormattedText(Format.EDIFACT_FORMAT));
		de.setFormatted("1000");
		assertEquals("\tName: 1000" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>1000</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID1\" name=\"Name\"><value>1000</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("1000", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("1000", de.getFormattedText(Format.EDIFACT_FORMAT));
		de.setFormatted("1,000");
		assertEquals("\tName: 1000" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>1000</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID1\" name=\"Name\"><value>1000</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("1000", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("1000", de.getFormattedText(Format.EDIFACT_FORMAT));
		de.setFormatted("100.00");
		assertEquals("\tName: 100" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>100</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID1\" name=\"Name\"><value>100</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("100", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("100", de.getFormattedText(Format.EDIFACT_FORMAT));

	}

	@Test
	public void testLargeNumbers() {
		Util.setOBOEProperty(Util.REAL_NUMBERS_RETAIN_PRECISION, "true");
		de6.set("1149606746");
		DocumentErrors derr = new DocumentErrors();
		assertTrue(de6.validate(derr));
		assertEquals(0, derr.getErrorCount());
		assertEquals(null, de6.validate("1149606746"));
		assertEquals("1149606746", de6.get());

	}

}