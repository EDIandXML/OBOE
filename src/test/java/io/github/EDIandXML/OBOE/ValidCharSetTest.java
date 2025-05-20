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
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.util.Util;

/**
 * @author joe mcverry
 *
 */
public class ValidCharSetTest {

	@AfterEach
	public void tearDown() {
		Util.resetValids();

	}

	@Test
	public void testNoProperty() {
		assertEquals(-1, Util.isValidForCharacterSet("blahblah"));
		assertEquals(-1, Util.isValidForCharacterSet("\uffff"));
	}

	@Test
	public void testX12Basic() {
		Util.setOBOEProperty(Util.VALID_CHARACTER_SET,
				"A...Z0...9 !=\"&'()*+,-./:';?=");
		assertEquals(-1, Util.isValidForCharacterSet("A8G&"));
		assertEquals(0, Util.isValidForCharacterSet("a8G&"));
		assertEquals(2, Util.isValidForCharacterSet("9-\uffff"));
		Util.resetValids();
	}

	@Test
	public void testX12Extended() {
		Util.setOBOEProperty(Util.VALID_CHARACTER_SET,
				"A...Za...z0...9 !=\"&'()*+,-./:';?=");
		assertEquals(-1, Util.isValidForCharacterSet("A8G&"));
		assertEquals(-1, Util.isValidForCharacterSet("a8G&"));
		assertEquals(2, Util.isValidForCharacterSet("8y\uffff"));
		Util.resetValids();
	}

	@Test
	public void testEDIFACTLevelA() {
		Util.setOBOEProperty(Util.VALID_CHARACTER_SET,
				"A...Z0...9 .,-()/='+:?!\"%&*;<>");
		assertEquals(-1, Util.isValidForCharacterSet("A8G&"));
		assertEquals(1, Util.isValidForCharacterSet("Ba8G&"));
		assertEquals(4, Util.isValidForCharacterSet("8%<>\uffff"));
		Util.resetValids();
	}

	@Test
	public void testEDIFACTLevelB() {
		Util.setOBOEProperty(Util.VALID_CHARACTER_SET,
				"A...Za...z0...9 .,-()/'+:=?!\"%&*;<>");
		assertEquals(-1, Util.isValidForCharacterSet("A8G&"));
		assertEquals(-1, Util.isValidForCharacterSet("a8G&"));
		assertEquals(5, Util.isValidForCharacterSet("8,-(y\uffff"));
		Util.resetValids();
	}

}
