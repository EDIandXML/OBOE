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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.DataElements.CompositeElement;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.DataElements.Element;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateCompositeElement;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;

/**
 * @author joe mcverry
 * 
 *         1 test getting values from de when de exists and does not
 *         exist 2.test range (position or suquence) exceptions 3. test
 *         putting values to de when de exists and does not exist
 *
 */
public class GetSetDEValueTestCase {

	@Test
	public void testSegmentGetDEValue() {

		TemplateSegment ts = new TemplateSegment("id", "name", 0, "desc", 1,
				'n', "xml", false, null);
		TemplateDataElement tde = new TemplateDataElement();
		tde.setType("AN");
		tde.setPosition(1);
		ts.addElement(tde);
		Segment seg = new Segment(ts, null);
		seg.buildElement(1);
		seg.setDataElementValue(1, "1seg");

		assertEquals("", seg.getDataElementValue(-1));
		assertEquals("", seg.getDataElementValue(100));
		assertEquals("1seg", seg.getDataElementValue(1));

	}

	@Test

	public void testCompositeGetDEValue() {
		TemplateCompositeElement tc = new TemplateCompositeElement("id", "name",
				'n', 1, "desc", "xml", null, 0, false);

		TemplateDataElement tde = new TemplateDataElement();
		tde.setPosition(1);
		tde.setType("AN");
		tc.addElement(tde);
		CompositeElement cd = new CompositeElement(tc, null);
		cd.buildElement(1);
		cd.setDataElementValue(1, "1cd");

		try {
			assertEquals(null, cd.getDataElementValue(-1));
		} catch (OBOEException e) {

		} catch (Exception e) {
			fail(e.getMessage());
		}
		try {
			assertEquals(null, cd.getDataElementValue(100));
		} catch (OBOEException e) {

		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertEquals("1cd", cd.getDataElementValue(1));

	}

	@Test

	public void testSegmentPutDEValue() {
		TemplateSegment ts = new TemplateSegment("id", "name", 0, "desc", 1,
				'n', "xml", false, null);
		TemplateDataElement tde = new TemplateDataElement();
		tde.setPosition(1);
		tde.setType("AN");
		ts.addElement(tde);
		TemplateDataElement tde2 = new TemplateDataElement();
		tde2.setPosition(2);
		tde2.setType("AN");
		tde2.setID("ID2");
		ts.addElement(tde2);
		Segment seg = new Segment(ts, null);
		try {
			seg.setDataElementValue(-1, "test-1");
		} catch (

		OBOEException e) {

		} catch (Exception e) {
			fail(e.getMessage());
		}
		seg.setDataElementValue(1, "test1");
		seg.setDataElementValue("ID2", "testwithID2");
		try {
			seg.setDataElementValue(100, "test100");
		} catch (OBOEException e) {

		} catch (Exception e) {
			fail(e.getMessage());
		}
		try {
			assertEquals("", seg.getDataElementValue(-1));
		} catch (OBOEException e) {

		} catch (Exception e) {
			fail(e.getMessage());
		}
		try {
			assertEquals("", seg.getDataElementValue(100));
		} catch (OBOEException e) {

		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertEquals("test1", seg.getDataElementValue(1));
		assertEquals("testwithID2", seg.getDataElementValue(2));
		assertEquals("testwithID2", seg.getDataElementValue("ID2"));

	}

	@Test

	public void testCompositePutDEValue() {

		TemplateCompositeElement tc = new TemplateCompositeElement("id", "name",
				'n', 1, "desc", "xml", null, 0, false);
		TemplateDataElement tde = new TemplateDataElement();
		tde.setPosition(1);
		tde.setType("AN");
		tc.addElement(tde);
		TemplateDataElement tde2 = new TemplateDataElement();
		tde2.setPosition(2);
		tde2.setType("AN");
		tde2.setID("ID2");
		tc.addElement(tde2);
		CompositeElement cd = new CompositeElement(tc, null);
		try {
			cd.setDataElementValue(-1, "test-1");
			fail("can't set a negative position");
		} catch (OBOEException e) {

		} catch (Exception e) {
			fail(e.getMessage());
		}
		cd.setDataElementValue(1, "test1");
		cd.setDataElementValue("ID2", "testwithID2");
		try {
			cd.setDataElementValue(100, "test100");
		} catch (OBOEException e) {

		} catch (Exception e) {
			fail(e.getMessage());
		}
		try {
			assertEquals(null, cd.getDataElementValue(-1));
		} catch (OBOEException e) {

		} catch (Exception e) {
			fail(e.getMessage());
		}
		try {
			assertEquals(null, cd.getDataElementValue(100));
		} catch (OBOEException e) {

		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertEquals("test1", cd.getDataElementValue(1));
		assertEquals("testwithID2", cd.getDataElementValue(2));
		assertEquals("testwithID2", cd.getDataElementValue("ID2"));

	}

	@Test

	public void testSegmentPutDEValueWithSameID() {
		TemplateSegment ts = new TemplateSegment("id", "name", 0, "desc", 1,
				'n', "xml", false, null);
		TemplateDataElement tde = new TemplateDataElement();
		tde.setPosition(1);
		tde.setType("AN");
		tde.setID("ID");
		ts.addElement(tde);
		TemplateDataElement tde2 = new TemplateDataElement();
		tde2.setPosition(2);
		tde2.setType("AN");
		tde2.setID("ID");
		ts.addElement(tde2);
		Segment seg = new Segment(ts, null);
		Element de = null;
		try {
			de = seg.setDataElementValue(-1, "test-1");
		} catch (OBOEException e) {

		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertNull(de);
		de = seg.setDataElementValue("ID", "testwithID");
		assertEquals(1, de.getPosition());
		de = seg.setDataElementValue("ID", 1, "testwithID2");
		assertEquals(2, de.getPosition());
		try {
			de = seg.setDataElementValue(100, "test100");
		} catch (OBOEException e) {

		} catch (Exception e) {
			fail(e.getMessage());
		}

		assertEquals("", seg.getDataElementValue(-1));
		assertEquals("", seg.getDataElementValue(100));
		assertEquals("testwithID", seg.getDataElementValue("ID"));
		assertEquals("testwithID2", seg.getDataElementValue(2));
		assertEquals("testwithID", seg.getDataElementValue(1));

	}

	@Test

	public void testCompositePutDEValueWithSameID() {

		TemplateCompositeElement tc = new TemplateCompositeElement("id", "name",
				'n', 1, "desc", "xml", null, 0, false);
		TemplateDataElement tde = new TemplateDataElement();
		tde.setPosition(1);
		tde.setType("AN");
		tde.setID("ID");
		tc.addElement(tde);
		TemplateDataElement tde2 = new TemplateDataElement();
		tde2.setPosition(2);
		tde2.setType("AN");
		tde2.setID("ID");
		tc.addElement(tde2);
		CompositeElement cd = new CompositeElement(tc, null);
		DataElement de = null;
		try {
			de = (DataElement) cd.setDataElementValue(-1, "test-1");
		} catch (OBOEException e) {

		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertNull(de);
		de = (DataElement) cd.setDataElementValue("ID", "testwithID");
		assertEquals(1, de.getPosition());
		de = (DataElement) cd.setDataElementValue("ID", 1, "testwithID2");
		assertEquals(2, de.getPosition());
		try {
			de = (DataElement) cd.setDataElementValue(100, "test100");
			fail("should not get");
		} catch (

		OBOEException e) {

		}

		try {
			var denull = cd.getDataElementValue(-1);
			if (denull != null) {
				fail("should not get");
			}
		} catch (OBOEException e) {

		}

		assertEquals("testwithID", cd.getDataElementValue(1));
		assertEquals("testwithID2", cd.getDataElementValue(2));
		assertEquals("testwithID", cd.getDataElementValue("ID"));

	}

}
