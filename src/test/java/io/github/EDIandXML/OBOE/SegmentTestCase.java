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

import io.github.EDIandXML.OBOE.Containers.Loop;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.DataElements.CharDE;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.DataElements.IDList;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateCompositeElement;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.Templates.TemplateLoop;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;
import io.github.EDIandXML.OBOE.util.Util;

public class SegmentTestCase {

	private Segment seg;
	private TemplateSegment tseg;

	@BeforeEach
	protected void setUp() {

		tseg = new TemplateSegment("segID", "segName", 0, "Short Description",
				10, 'M', "segtag", true, null);

	}

	@AfterEach

	protected void tearDown() {
		seg = null;
	}

	@Test
	public void testTrim() {
		TemplateLoop tl = new TemplateLoop();
		tl.addContainer(tseg);

		Loop l = new Loop(tl, null);
		tseg.addElement(new TemplateDataElement("cID", "cName", 1, "AN", 'R',
				"Char Field", 1, 2, "detag", null, null, 1, true));
		seg = new Segment(tseg, null);
		seg.buildElement(1);
		l.addSegment(seg);
		assertEquals("segID: ", seg.getFormattedText(Format.UNDEFINED));
		assertEquals(1, l.getSegmentCount("segID"));

		seg = new Segment(tseg, null);
		seg.buildElement(1);
		seg.setDataElementValue(1, "2");

		l.addSegment(seg);
		assertEquals(2, l.getSegmentCount("segID"));
		seg.trim();
		assertEquals(2, l.getSegmentCount("segID"));

		assertEquals("segID: \tcName: 2",
				seg.getFormattedText(Format.UNDEFINED).trim());
		assertEquals(2, l.getSegmentCount("segID"));

		l.trim();

		assertEquals(1, l.getSegmentCount("segID"));

	}

	public void testParms() {
		seg = new Segment(tseg, null);
		assertEquals("segID", seg.getID());
		assertEquals("segName", seg.getName());
		assertEquals(0, seg.getPosition());
		assertEquals("segtag", seg.getShortName());
	}

	@Test
	public void testAddingDE() {

		tseg.addElement(new TemplateDataElement("cID", "cName", 1, "AN", 'R',
				"Char Field", 1, 2, "detag", null, null, 1, true));
		seg = new Segment(tseg, null);

		DataElement de = (DataElement) seg.buildElement(1);

		de.set("Value");

		assertEquals("segID: \tcName: Value" + Util.lineFeed,
				seg.getFormattedText(Format.UNDEFINED));
		assertEquals(
				"<segtag>" + Util.lineFeed + "<detag>Value</detag>"
						+ Util.lineFeed + "</segtag>" + Util.lineFeed,
				seg.getFormattedText(Format.XML_FORMAT));
		assertEquals("<segment code=\"segID\" name=\"segName\">" + Util.lineFeed
				+ "<element code=\"cID\" name=\"cName\"> <value>Value</value></element>"
				+ Util.lineFeed + "</segment>" + Util.lineFeed,
				seg.getFormattedText(Format.VALID_XML_FORMAT));

		assertEquals("segID*Value\n", seg.getFormattedText(Format.X12_FORMAT));
		assertEquals("segID+Value'",
				seg.getFormattedText(Format.EDIFACT_FORMAT));

		assertEquals("Value".length(), seg.getElementCount());

	}

	public void testBlankSegments() {
		TemplateLoop tl = new TemplateLoop("l1", "loop 1", 1, 'M', "loop1",
				true, null);

		TemplateSegment ts1 = new TemplateSegment("ts1", "ts1", 10, "ts1", 1,
				'O', "ts1", true, null);
		ts1.addElement(new TemplateDataElement("de1", "de1", 1, "AN", 'R',
				"Char Field", 1, 2, "de1", null, null, 1, true));
		ts1.addElement(new TemplateDataElement("de2", "de2", 2, "AN", 'R',
				"Char Field", 1, 2, "de2", null, null, 1, true));
		tl.addContainer(ts1);

		TemplateSegment ts2 = new TemplateSegment("ts2", "ts2", 20, "ts2", 1,
				'O', "ts2", true, null);
		ts2.addElement(new TemplateDataElement("de1", "de1", 1, "AN", 'R',
				"Char Field", 1, 2, "de1", null, null, 1, true));
		ts2.addElement(new TemplateDataElement("de2", "de2", 2, "AN", 'R',
				"Char Field", 1, 2, "de2", null, null, 1, true));
		tl.addContainer(ts2);

		TemplateSegment ts3 = new TemplateSegment("ts3", "ts3", 30, "ts3", 1,
				'O', "ts3", true, null);
		ts3.addElement(new TemplateDataElement("de1", "de1", 1, "AN", 'R',
				"Char Field", 1, 2, "de1", null, null, 1, true));
		ts3.addElement(new TemplateDataElement("de2", "de2", 2, "AN", 'R',
				"Char Field", 1, 2, "de2", null, null, 1, true));
		tl.addContainer(ts3);

		Loop l1 = new Loop(tl, null);
		Segment s1 = new Segment(ts1, null);
		l1.addSegment(s1);
		CharDE de1 = (CharDE) s1.buildElement(1);
		CharDE de2 = (CharDE) s1.buildElement(2);
		de1.set("D1");
		de2.set("D2");

		Segment s2 = new Segment(ts2, null);
		de1 = (CharDE) s2.buildElement(1);
		de2 = (CharDE) s2.buildElement(2);
		l1.addSegment(s2);

		Segment s3 = new Segment(ts3, null);
		de1 = (CharDE) s3.buildElement(1);
		de2 = (CharDE) s3.buildElement(2);
		l1.addSegment(s3);

		assertEquals("ts1*D1*D2\n", l1.getFormattedText(Format.X12_FORMAT));
		assertEquals(3, l1.getSegmentCount());

	}

	@Test
	public void testTooManySegments() {
		TemplateLoop tl = new TemplateLoop("l1", "loop 1", 1, 'M', "loop1",
				true, null);

		TemplateSegment ts1 = new TemplateSegment("ts1", "ts1", 10, "ts1", -1,
				'O', "ts1", true, null);
		ts1.addElement(new TemplateDataElement("de1", "de1", 1, "AN", 'R',
				"Char Field", 1, 2, "de1", null, null, 1, true));
		ts1.addElement(new TemplateDataElement("de2", "de2", 2, "AN", 'R',
				"Char Field", 1, 2, "de2", null, null, 1, true));
		tl.addContainer(ts1);

		Loop l1 = new Loop(tl, null);
		Segment s1 = new Segment(ts1, null);
		l1.addSegment(s1);

		Segment s2 = new Segment(ts1, null);
		l1.addSegment(s2);

		TemplateLoop t3 = new TemplateLoop("l1", "loop 1", 1, 'M', "loop1",
				true, null);

		ts1 = new TemplateSegment("ts1", "ts1", 10, "ts1", 2, 'O', "ts1", true,
				null);
		t3.addContainer(ts1);

		l1 = new Loop(t3, null);
		s2 = new Segment(ts1, null);
		l1.addSegment(s1);
		l1.addSegment(s1);

		s2 = new Segment(ts1, null);
		try {
			l1.addSegment(s2);
			fail("should have caught too many segment exception");
		} catch (OBOEException oe) {
			assertEquals("Can't add segment, occurs size will be exceeded",
					oe.getLocalizedMessage());
		}

	}

	@Test
	public void testEmpty() {
		TemplateSegment tseg = new TemplateSegment("tsId", "tsName", 0,
				"tsDescription", 1, 'M', "tsShortName", true, null);
		tseg.addElement(new TemplateDataElement("ID1", "Name1", 1, "C", 'O',
				"Desc1", 1, 2, "shortName1", null, null, 1, true));
		tseg.addElement(new TemplateDataElement("ID2", "Name2", 2, "DT", 'O',
				"Desc2", 6, 8, "shortName2", null, null, 1, true));
		tseg.addElement(new TemplateDataElement("ID3", "Name3", 3, "ID", 'O',
				"Desc31", 1, 2, "ShortName3", new IDList(), null, 1, true));
		tseg.addElement(new TemplateDataElement("ID4", "Name4", 4, "N0", 'O',
				"Desc4", 1, 4, "ShortName4", null, null, 1, true));
		tseg.addElement(new TemplateDataElement("ID5", "Name5", 5, "R", 'O',
				"Desc5", 1, 5, "ShortName5", null, null, 1, true));
		tseg.addElement(new TemplateDataElement("ID6", "Name1", 6, "TM", 'O',
				"Desc6", 4, 6, "ShortName6", null, null, 1, true));
		tseg.addElement(new TemplateDataElement("ID7", "Name1", 7, "C", 'O',
				"Desc7", 1, 2, "ShortName7", null, null, 1, true));

		Segment seg = new Segment(tseg, null);
		for (int i = 1; i <= 7; i++) {
			seg.buildElement(i);
		}

		seg.setDataElementValue(7, "s");

		assertEquals("tsId*******s\n", seg.getFormattedText(Format.X12_FORMAT));
	}

	@Test
	public void testAdding2DEs() {

		tseg.addElement(new TemplateDataElement("cID", "cName", 1, "AN", 'R',
				"Char Field", 1, 2, "detag", null, null, 1, true));
		tseg.addElement(new TemplateDataElement("c2ID", "c2Name", 2, "AN", 'R',
				"Char 2 Field", 1, 2, "de2tag", null, null, 1, true));

		seg = new Segment(tseg, null);

		CharDE de = (CharDE) seg.buildElement(1);
		CharDE de2 = (CharDE) seg.buildElement(2);

		de.set("Value");
		de2.set("222");

		assertEquals(
				"segID: \tcName: Value" + Util.lineFeed + "\tc2Name: 222"
						+ Util.lineFeed,
				seg.getFormattedText(Format.UNDEFINED));
		assertEquals(
				"<segtag>" + Util.lineFeed + "<detag>Value</detag>"
						+ Util.lineFeed + "<de2tag>222</de2tag>" + Util.lineFeed
						+ "</segtag>" + Util.lineFeed,
				seg.getFormattedText(Format.XML_FORMAT));
		assertEquals("<segment code=\"segID\" name=\"segName\">" + Util.lineFeed
				+ "<element code=\"cID\" name=\"cName\"> <value>Value</value></element>"
				+ Util.lineFeed
				+ "<element code=\"c2ID\" name=\"c2Name\"> <value>222</value></element>"
				+ Util.lineFeed + "</segment>" + Util.lineFeed,
				seg.getFormattedText(Format.VALID_XML_FORMAT));

		assertEquals("segID*Value*222\n",
				seg.getFormattedText(Format.X12_FORMAT));
		assertEquals("segID+Value+222'",
				seg.getFormattedText(Format.EDIFACT_FORMAT));

		assertEquals("Value222".length(), seg.getElementCount());

	}

	@Test
	public void testLoop() {

		TemplateLoop tl = new TemplateLoop("i", "name", 1, 'M', "xml", true,
				null);
		assertEquals("i", tl.getID());
		assertEquals("name", tl.getName());
		assertEquals(1, tl.getOccurs());
		assertEquals('M', tl.getRequired());
		assertEquals("xml", tl.getShortName());

	}

	@Test
	public void testUseDefault() {
		tseg.setRequired('O');
		tseg.setOccurs(2);
		TemplateCompositeElement tcde = new TemplateCompositeElement("compID",
				"compName", 'M', 1, "description", "comptag", null, 1, true);
		tcde.setPosition(1);
		tseg.addElement(tcde);
		TemplateDataElement tde = new TemplateDataElement("cID", "cName", 1,
				"AN", 'M', "Char Field", 1, 2, "detag", null, null, 1, true);
		tcde.addElement(tde);
		tde.setLoadFromConstant("BE");

		tcde = new TemplateCompositeElement("compID", "compName", 'O', 2,
				"description", "comptag", null, 1, true);
		tcde.setPosition(2);
		tseg.addElement(tcde);
		tde = new TemplateDataElement("cID", "cName", 1, "AN", 'M',
				"Char Field", 1, 2, "detag", null, null, 1, true);
		tcde.addElement(tde);
		tde.setLoadFromConstant("BE");

		Segment seg = new Segment(tseg, null);

		seg.useDefault();
	}

	@Test
	public void testUseDefault2() {
		tseg.setRequired('M');
		TemplateCompositeElement tcde = new TemplateCompositeElement("compID",
				"compName", 'M', 1, "description", "comptag", null, 1, true);
		tcde.setPosition(1);
		tseg.addElement(tcde);
		TemplateDataElement tde = new TemplateDataElement("cID", "cName", 1,
				"AN", 'M', "Char Field", 1, 2, "detag", null, null, 1, true);
		tcde.addElement(tde);
		tde.setLoadFromConstant("BE");

		tcde = new TemplateCompositeElement("compID", "compName", 'O', 2,
				"description", "comptag", null, 1, true);
		tcde.setPosition(2);
		tseg.addElement(tcde);
		tde = new TemplateDataElement("cID", "cName", 1, "AN", 'O',
				"Char Field", 1, 2, "detag", null, null, 1, true);
		tcde.addElement(tde);
		tde.setLoadFromConstant("BE");

		TemplateLoop tl = new TemplateLoop("id", "name", 1, 'M', "tag", true,
				null);
		tl.addContainer(tseg);

		Loop l = new Loop(tl, tl);

		Segment seg = l.createAndAddSegment("segID");

		seg.useDefault();
	}

}