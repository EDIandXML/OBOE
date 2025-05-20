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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.DataElements.BinaryDE;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.util.Util;

public class BinDETestCase {

	private BinaryDE de;

	@BeforeEach
	protected void setUp() {
		// new TemplateDE(inID, inName, inPosition, inType, inRequired, inDesc,
		// inMinLength, inMaxLength, inShortName, inIDList, inParent, inOccurs,
		// inUsed)
		de = new BinaryDE(new TemplateDataElement("BIN", "Name", 1, "BIN", 'M',
				"Binary Desc", 1, 42000, "binaryField", null, null, 1, true),
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
		assertEquals("BIN", de.getID());
		assertEquals("Name", de.getName());
		assertEquals(1, de.getPosition().intValue());
		assertEquals("Binary Desc", de.getDescription());
		assertEquals('M', de.getRequired());
		assertEquals(1, de.getMinLength());
		assertEquals(42000, de.getMaxLength());
		assertEquals("binaryField", de.getShortName());
	}

	@Test
	public void testFormattedText() {
		assertEquals("\tName: Value" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<binaryField>Value</binaryField>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"BIN\" name=\"Name\"> <value>Value</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("Value", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("Value", de.getFormattedText(Format.EDIFACT_FORMAT));
	}

	@Test
	public void testValid() {
		assertEquals(null, de.validate(""));
		assertEquals(null, de.validate("01FF"));
		assertNotNull(de.validate("HE"));
		DocumentErrors derr = new DocumentErrors();
		de.validate(derr);
		assertEquals(1, derr.getErrorCount());
		de.set("AAA0");
		derr = new DocumentErrors();
		de.validate(derr);
		assertEquals(0, derr.getErrorCount());
	}

	public static String getIn() {
		String in = null;
		return in;
	}

}