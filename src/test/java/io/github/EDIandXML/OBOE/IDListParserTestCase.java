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

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.DataElements.IDList;
import io.github.EDIandXML.OBOE.Parsers.IDListParser;

/**
 * @author joe mcverry
 *
 */
public class IDListParserTestCase {

	@Test
	public void testAllIdCodeIdValues() {
		IDListParser idlp = new IDListParser();
		ArrayList<String> vCodes = new ArrayList<String>(),
				vValues = new ArrayList<String>();
		idlp.parse("xml/idList1.xml", "", vCodes, vValues);
		assertEquals(vCodes.size(), vValues.size());
		assertEquals(3, vCodes.size());
		assertEquals("A", vCodes.get(0));
		assertEquals("B", vCodes.get(1));
		assertEquals("C", vCodes.get(2));
		assertEquals("Urgent", vValues.get(0));
		assertEquals("Normal", vValues.get(1));
		assertEquals("Low", vValues.get(2));

	}

	@Test
	public void testSomeMissingIdValues() {
		// B is missing so B should comme back with vValues(2)
		IDListParser idlp = new IDListParser();
		ArrayList<String> vCodes = new ArrayList<String>(),
				vValues = new ArrayList<String>();
		idlp.parse("xml/idList1Missing.xml", "", vCodes, vValues);
		assertEquals(vValues.size(), vCodes.size());
		assertEquals(3, vCodes.size());
		assertEquals("A", vCodes.get(0));
		assertEquals("B", vCodes.get(1));
		assertEquals("C", vCodes.get(2));
		assertEquals("Urgent", vValues.get(0));
		assertEquals("B", vValues.get(1));
		assertEquals("Low", vValues.get(2));

	}

	@Test
	public void testIDListMissingIDValues() {
		IDListParser idlp = new IDListParser();
		IDList idl = new IDList("xml/idList1Missing.xml", "", idlp);
		assertEquals(idl.getValues().size(), idl.getCodes().size());
		assertEquals(3, idl.getValues().size());

		assertEquals("A", idl.getCodes().get(0));
		assertEquals("B", idl.getCodes().get(1));
		assertEquals("C", idl.getCodes().get(2));
		assertEquals("Urgent", idl.describe(idl.getCodes().get(0)));
		assertEquals("B", idl.describe(idl.getCodes().get(1)));
		assertEquals("Low", idl.describe(idl.getCodes().get(2)));

	}

	@Test
	public void testIDList() {
		IDListParser idlp = new IDListParser();
		IDList idl = new IDList("xml/idList1.xml", "", idlp);
		assertEquals(idl.getValues().size(), idl.getCodes().size());
		assertEquals(3, idl.getValues().size());

		assertEquals("A", idl.getCodes().get(0));
		assertEquals("B", idl.getCodes().get(1));
		assertEquals("C", idl.getCodes().get(2));
		assertEquals("Urgent", idl.describe(idl.getCodes().get(0)));
		assertEquals("Normal", idl.describe(idl.getCodes().get(1)));
		assertEquals("Low", idl.describe(idl.getCodes().get(2)));

	}

}
