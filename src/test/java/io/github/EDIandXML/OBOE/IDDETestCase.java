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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.DataElements.IDDE;
import io.github.EDIandXML.OBOE.DataElements.IDList;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.util.Util;

public class IDDETestCase {

	private IDDE de;

	@BeforeEach
	protected void setUp() {

		 IDList idl = new IDList();
		idl.add("Code1", "Meaning1");
		idl.add("Code2", "Meaning2");

		de = new IDDE(new TemplateDataElement("ID", "Name", 1, "ID", 'R', "Description",
				1, 2, "xmltag", idl, null, 1, true), null);
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
		assertEquals(1, de.getPosition());
		assertEquals("Description", de.getDescription());
		assertEquals('R', de.getRequired());
		assertEquals(1, de.getMinLength());
		assertEquals(2, de.getMaxLength());
		assertEquals("xmltag", de.getShortName());
	}

	@Test

	public void testFormattedText() {
		assertEquals("\tName: Value : Value" + Util.lineFeed,
				de.getFormattedText(Format.UNDEFINED));
		assertEquals("<xmltag>Value</xmltag>" + Util.lineFeed,
				de.getFormattedText(Format.XML_FORMAT));
		assertEquals(
				"<element code=\"ID\" name=\"Name\"><value description=\"Value\">Value</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("Value", de.getFormattedText(Format.X12_FORMAT));
		assertEquals("Value", de.getFormattedText(Format.EDIFACT_FORMAT));
	}

	@Test

	public void testCodeSetter() {
		assertTrue(de.isCodeValid("code1") == false);
		assertTrue(de.isCodeValid("Code1") == true);
		assertTrue(de.isCodeValid("Code2") == true);
		assertEquals("Meaning1", de.describe("Code1"));
		assertEquals("Code2", de.getCode("Meaning2"));
		de.set("Code1");
		assertEquals(
				"<element code=\"ID\" name=\"Name\"><value description=\"Meaning1\">Code1</value></element>"
						+ Util.lineFeed,
				de.getFormattedText(Format.VALID_XML_FORMAT));
		assertEquals("Code1", de.getFormattedText(Format.X12_FORMAT));
	}

	@Test

	public void testExclude() {
		IDList idl = new IDList();
		idl.add("a", "Meaning1");
		idl.add("b", "Meaning2");
		idl.add("c", "Meaning3");
		try {
			 IDList idl2 = idl.idListWork('x', "a,c");
			assertFalse(idl2.isCodeValid("a"));
			assertTrue(idl2.isCodeValid("b"));
			assertFalse(idl2.isCodeValid("a"));
		} catch ( OBOEException e) {
			fail("shouldn't fail");
		}

		try {
			idl.idListWork('x', "-0,zz");
			fail("should fail -0,zz");
		} catch ( OBOEException e) {
		}

		try {
			idl.idListWork('x', "c,a");
			fail("should fail c,a");
		} catch ( OBOEException e) {
		}

		idl = new IDList();
		idl.add("1", "Meaning1");
		idl.add("10", "Meaning2");
		idl.add("20", "Meaning3");
		try {
			idl.idListWork('x', "1,11");
		} catch ( OBOEException e) {
			fail("shouldn't fail but did with " + e.getMessage());
		}

		try {
			idl.idListWork('x', "11,1");
			fail("should fail 11,1");
		} catch ( OBOEException e) {
		}

		try {
			idl.idListWork('x', "999,0");
			fail("should fail 999,0");
		} catch ( OBOEException e) {
		}

	}

	@Test

	public void testInclude() {
		IDList idl = new IDList();
		idl.add("a", "Meaning1");
		idl.add("b", "Meaning2");
		idl.add("c", "Meaning3");
		try {
			 IDList idl2 = idl.idListWork('i', "a,c");
			assertTrue(idl2.isCodeValid("a"));
			assertFalse(idl2.isCodeValid("b"));
			assertTrue(idl2.isCodeValid("a"));

		} catch ( OBOEException e) {
			fail("shouldn't fail");
		}

		try {
			idl.idListWork('i', "-0,zz");
			fail("should fail -0,zz");
		} catch ( OBOEException e) {
		}

		try {
			idl.idListWork('i', "c,a");
			fail("should fail c,a");
		} catch ( OBOEException e) {
		}

		idl = new IDList();
		idl.add("1", "Meaning1");
		idl.add("10", "Meaning2");
		idl.add("20", "Meaning3");
		try {
			 IDList idl2 = idl.idListWork('i', "1,11");
			assertTrue(idl2.isCodeValid("1"));
		} catch ( OBOEException e) {
			fail("shouldn't fail but did with " + e.getMessage());
		}

		try {
			idl.idListWork('i', "11,1");
			fail("should fail 11,1");
		} catch ( OBOEException e) {
		}

		try {
			idl.idListWork('i', "999,0");
			fail("should fail 999,0");
		} catch ( OBOEException e) {
		}

	}
}