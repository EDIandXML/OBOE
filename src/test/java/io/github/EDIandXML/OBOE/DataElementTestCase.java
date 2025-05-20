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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.DataElements.CharDE;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.util.Util;

public class DataElementTestCase {

	private DataElement de, deMult;

	@BeforeEach
	protected void setUp() {

		de = new CharDE(
				new TemplateDataElement("ID", "Name", 1, "AN", 'M',
						"Description", 1, 2, "xmltag", null, null, 1, true),
				null);
		de.set("Value");
		deMult = new CharDE(
				new TemplateDataElement("ID", "Name", 1, "AN", 'M',
						"Description", 1, 2, "xmltag", null, null, 3, true),
				null);
		deMult.set("Value");

	}

	@AfterEach
	protected void tearDown() {
		de = null;
	}

	@Test
	public void testBadPosition() {
		try {
			new TemplateDataElement("ID", "Name", -1, "AN", 'M', "Description",
					1, 2, "xmltag", null, null, 1, true);
			fail("Didn't catch bad position");
		} catch (OBOEException oe) {
			if (oe.getMessage().startsWith("Invalid position specified")) {
				;
			} else {
				fail("Didn't catch bad position - caught " + oe.getMessage());
			}
		}
	}

	@Test
	public void testParms() {
		assertEquals("Value", de.get());
		assertEquals("ID", de.getID());
		assertEquals("Name", de.getName());
		assertEquals(1, de.getPosition());
		assertEquals("Description", de.getDescription());
		assertEquals('M', de.getRequired());
		assertEquals(1, de.getMinLength());
		assertEquals(2, de.getMaxLength());
		assertEquals("xmltag", de.getShortName());
		assertEquals(1, de.getOccurs());
		assertEquals(3, deMult.getOccurs());
	}

	@Test
	public void testFormattedText() {
		assertEquals("\tName: Value" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>Value</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"> <value>Value</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		// assertEquals("Value", de.getFormattedText(Format.X12_FORMAT));
		// assertEquals("Value", de.getFormattedText(Format.EDIFACT_FORMAT));
	}

	@Test
	public void testMultFormattedText() {
		assertEquals("\tName: Value" + Util.lineFeed,
				deMult.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>Value</xmltag>" + Util.lineFeed,
				deMult.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"> <value>Value</value></element>"
						+ Util.lineFeed,
				deMult.getFormattedText(Format.VALID_XML_FORMAT));
	}

	@Test
	public void testOccurs() {
		assertEquals(1, de.getTemplate().getOccurs());
		assertEquals(1, de.getOccurs());
		assertEquals(1, de.getRepeatCount());
	}
}
