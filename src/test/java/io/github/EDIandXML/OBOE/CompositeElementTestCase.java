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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.DataElements.CompositeElement;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateCompositeElement;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.util.Util;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;

public class CompositeElementTestCase {

	private static TemplateCompositeElement tcde;
	private static CompositeElement cde;

	@BeforeAll
	protected static void setUp() {

		tcde = new TemplateCompositeElement("compID", "compName", 'M', 1,
				"description", "comptag", null, 1, true);

	}

	@AfterAll
	protected static void tearDown() {
		cde = null;
	}

	@Test
	@Order(0)
	public void testParms() {
		cde = new CompositeElement(tcde, null);
		assertEquals("compID", cde.getID());
		assertEquals("compName", cde.getName());
		assertEquals(1, cde.getPosition());
		// assertEquals("description", cde.getDescription());
		assertEquals("comptag", cde.getShortName());
	}

	@Test
	@Order(1)
	public void testAddingDE() {

		tcde.addElement(new TemplateDataElement("cID", "cName", 1, "AN", 'R',
				"Char Field", 1, 2, "detag", null, null, 1, true));

		cde = new CompositeElement(tcde, null);
		cde.buildElement(1);

		DataElement de = (DataElement) cde.getElement("cID");

		de.set("Value");

		assertEquals("compID: \tcName: Value" + Util.lineFeed,
				cde.getFormattedText(Format.UNDEFINED));
		assertEquals(
				"<comptag>" + Util.lineFeed + "<detag>Value</detag>"
						+ Util.lineFeed + "</comptag>" + Util.lineFeed,
				cde.getFormattedText(Format.XML_FORMAT));
		assertEquals("<composite code=\"compID\" name=\"compName\">"
				+ Util.lineFeed
				+ "<element code=\"cID\" name=\"cName\"> <value>Value</value></element>"
				+ Util.lineFeed + "</composite>" + Util.lineFeed,
				cde.getFormattedText(Format.VALID_XML_FORMAT));

		assertEquals("Value", cde.getFormattedText(Format.X12_FORMAT));
		assertEquals("Value", cde.getFormattedText(Format.EDIFACT_FORMAT));

		assertEquals("Value".length(), cde.getElementCount());

	}

	@Test
	@Order(2)

	public void testAdding2DEs() {

		tcde.addElement(new TemplateDataElement("cID", "cName", 2, "AN", 'R',
				"Char Field", 1, 2, "detag", null, null, 1, true));
		tcde.addElement(new TemplateDataElement("c2ID", "c2Name", 3, "AN", 'R',
				"Char 2 Field", 1, 2, "de2tag", null, null, 1, true));

		cde = new CompositeElement(tcde, null);
		cde.buildElement(2);
		cde.buildElement(3);

		DataElement de = (DataElement) cde.getElement("cID");
		DataElement de2 = (DataElement) cde.getElement("c2ID");

		de.set("Value");
		de2.set("222");

		assertEquals(
				"compID: \tcName: Value" + Util.lineFeed + "\tc2Name: 222"
						+ Util.lineFeed,
				cde.getFormattedText(Format.UNDEFINED));
		assertEquals(
				"<comptag>" + Util.lineFeed + "<detag>Value</detag>"
						+ Util.lineFeed + "<de2tag>222</de2tag>" + Util.lineFeed
						+ "</comptag>" + Util.lineFeed,
				cde.getFormattedText(Format.XML_FORMAT));
		assertEquals("<composite code=\"compID\" name=\"compName\">"
				+ Util.lineFeed
				+ "<element code=\"cID\" name=\"cName\"> <value>Value</value></element>"
				+ Util.lineFeed
				+ "<element code=\"c2ID\" name=\"c2Name\"> <value>222</value></element>"
				+ Util.lineFeed + "</composite>" + Util.lineFeed,
				cde.getFormattedText(Format.VALID_XML_FORMAT));

		assertEquals("<Value<222", cde.getFormattedText(Format.X12_FORMAT));
		assertEquals(":Value:222", cde.getFormattedText(Format.EDIFACT_FORMAT));

		assertEquals("Value222".length(), cde.getElementCount());

	}

	@Test
	@Order(3)

	public void testAddingLateDE() {

		tcde.addElement(new TemplateDataElement("c2ID", "c2Name", 5, "AN", 'R',
				"Char 2 Field", 1, 2, "de2tag", null, null, 1, true));

		cde = new CompositeElement(tcde, null);
		var f = cde.buildElement(5);
		assertNotNull(f);

		DataElement de2 = (DataElement) cde.getElement("c2ID");

		de2.set("222");

		assertEquals("compID: \tc2Name: 222" + Util.lineFeed,
				cde.getFormattedText(Format.UNDEFINED));
		assertEquals(
				"<comptag>" + Util.lineFeed + "<de2tag>222</de2tag>"
						+ Util.lineFeed + "</comptag>" + Util.lineFeed,
				cde.getFormattedText(Format.XML_FORMAT));
		assertEquals("<composite code=\"compID\" name=\"compName\">"
				+ Util.lineFeed
				+ "<element code=\"c2ID\" name=\"c2Name\"> <value>222</value></element>"
				+ Util.lineFeed + "</composite>" + Util.lineFeed,
				cde.getFormattedText(Format.VALID_XML_FORMAT));

		assertEquals("<<<<222", cde.getFormattedText(Format.X12_FORMAT));
		assertEquals("::::222", cde.getFormattedText(Format.EDIFACT_FORMAT));

		assertEquals("222".length(), cde.getElementCount());

	}

	@Test
	@Order(4)

	public void testParserNotCatchingTooManyDEsInComposite() {
		try {
			// LogManager.getLogger(AllTests.class);
			// Util.setOBOEProperty(Util.searchDirective, "VS");
			Util.setOBOEProperty(Util.THROW_PARSING_EXCEPTION, "false");
			X12DocumentHandler xdh = new X12DocumentHandler(new FileReader(
					"testFiles/test278WithTooManyDEsInCompositeField"));
			xdh.getDocumentErrors().logErrors();
			assertEquals(1, xdh.getDocumentErrors().getErrorCount());

		} catch (OBOEException e) {
			if (e.getMessage().startsWith("Invalid or unknown")) {

			} else {
				fail(e.getMessage());
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	@Test
	@Order(5)

	public void testOccurs() {
		assertEquals(1, tcde.getOccurs());
		cde = new CompositeElement(tcde, null);
		assertEquals(1, cde.getOccurs());
	}

	@Test
	@Order(5)

	public void testDefault() {
		TemplateDataElement tde = new TemplateDataElement("cID", "cName", 4,
				"AN", 'R', "Char Field", 1, 2, "detag", null, null, 1, true);
		tcde.addElement(tde);
		tde.setLoadFromConstant("BE");

		cde = new CompositeElement(tcde, null);
		cde.buildElement(4);
		cde.useDefault();

	}

}