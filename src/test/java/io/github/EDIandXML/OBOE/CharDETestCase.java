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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.DataElements.CharDE;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.util.Util;

public class CharDETestCase {

	private CharDE de;

	@BeforeEach
	protected void setUp() {
		de = new CharDE(
				new TemplateDataElement("ID", "Name", 1, "AN", 'M',
						"Description", 1, 42, "xmltag", null, null, 1, true),
				null);
		de.set("Value");

	}

	@AfterEach
	protected void tearDown() {
		de = null;
	}

	@Test
	public void testParms() {
		assertEquals("Value", de.get());
		assertEquals("ID", de.getID());
		assertEquals("Name", de.getName());
		assertEquals(1, de.getPosition().intValue());
		assertEquals("Description", de.getDescription());
		assertEquals('M', de.getRequired());
		assertEquals(1, de.getMinLength());
		assertEquals(42, de.getMaxLength());
		assertEquals("xmltag", de.getShortName());
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
		assertEquals("Value", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("Value", de.getFormattedText(Format.EDIFACT_FORMAT));
	}

	public void testSetNull() {
		de.set(getIn());
		assertEquals(null, de.get());
	}

	public static String getIn() {
		String in = null;
		return in;
	}

}