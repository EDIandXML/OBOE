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

import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.DataElements.CompositeElement;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.Templates.TemplateCompositeElement;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;

public class TrimTest {

	@Test
	public void testNullPointerInCompDETrim() {

		TemplateSegment ts = new TemplateSegment("t", "t", 10, "n/a", 1, 'M',
				"inShortName", true, null);
		TemplateCompositeElement tc = new TemplateCompositeElement("tc", "tc",
				'M', 10, "n/a", "inShortName", null, 1, true);
		ts.addElement(tc);
		TemplateDataElement tde1 = new TemplateDataElement("td1", "td1", 1,
				"AN", 'M', "n/a", 1, 10, "inShortName", null, null, 1, true);
		TemplateDataElement tde2 = new TemplateDataElement("td2", "td2", 2,
				"AN", 'M', "n/a", 1, 10, "inShortName", null, null, 1, true);
		TemplateDataElement tde4 = new TemplateDataElement("td4", "td4", 4,
				"AN", 'M', "n/a", 1, 10, "inShortName", null, null, 1, true);
		TemplateDataElement tde5 = new TemplateDataElement("td5", "td5", 5,
				"AN", 'M', "n/a", 1, 10, "inShortName", null, null, 1, true);

		tc.addElement(tde1);
		tc.addElement(tde2);
		tc.addElement(tde4);
		tc.addElement(tde5);

		CompositeElement cde = new CompositeElement(tc, null);
		cde.trim();

	}

	@Test
	public void testInLastButNotFirst() {

		TemplateSegment ts = new TemplateSegment("t", "t", 10, "n/a", 1, 'M',
				"inShortName", true, null);
		TemplateCompositeElement tc1 = new TemplateCompositeElement("tc", "tc",
				'M', 1, "n/a", "inShortName", null, 1, true);
		TemplateCompositeElement tc2 = new TemplateCompositeElement("tc", "tc",
				'M', 2, "n/a", "inShortName", null, 1, true);
		TemplateCompositeElement tc3 = new TemplateCompositeElement("tc", "tc",
				'M', 3, "n/a", "inShortName", null, 1, true);
		ts.addElement(tc1);
		ts.addElement(tc2);
		ts.addElement(tc3);
		TemplateDataElement tde1 = new TemplateDataElement("td1", "td1", 1,
				"AN", 'M', "n/a", 1, 10, "inShortName", null, null, 1, true);
		TemplateDataElement tde2 = new TemplateDataElement("td2", "td2", 2,
				"AN", 'M', "n/a", 1, 10, "inShortName", null, null, 1, true);
		TemplateDataElement tde4 = new TemplateDataElement("td4", "td4", 4,
				"AN", 'M', "n/a", 1, 10, "inShortName", null, null, 1, true);
		TemplateDataElement tde5 = new TemplateDataElement("td5", "td5", 5,
				"AN", 'M', "n/a", 1, 10, "inShortName", null, null, 1, true);
		ts.addElement(tde4);

		tc1.addElement(tde1);
		tc1.addElement(tde2);
		tc1.addElement(tde4);
		tc1.addElement(tde5);

		tc2.addElement(tde1);
		tc2.addElement(tde2);
		tc2.addElement(tde4);
		tc2.addElement(tde5);

		tc3.addElement(tde1);
		tc3.addElement(tde2);
		tc3.addElement(tde4);
		tc3.addElement(tde5);

		Segment s = new Segment(ts, null);

		s.buildElement(1);
		CompositeElement cde = (CompositeElement) s.getElement(1);
		cde.setDataElementValue(1, "de1 in 1");
		cde.setDataElementValue(2, "de2 in 1");

		s.buildElement(4);
		DataElement de = (DataElement) s.getElement(4);

		de.set("de4 in seg");

		s.trim();

		assertEquals("t*de1 in 1<de2 in 1***de4 in seg\n",
				s.getFormattedText(Format.X12_FORMAT));

		cde.setDataElementValue(5, "de5 in 1");
		s.trim();

		assertEquals("t*de1 in 1<de2 in 1<<<de5 in 1***de4 in seg\n",
				s.getFormattedText(Format.X12_FORMAT));

	}
}
