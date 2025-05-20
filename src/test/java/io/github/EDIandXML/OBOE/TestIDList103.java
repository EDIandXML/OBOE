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

import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.x12.IDList103Processor;

/**
 * @author Joe McVerry
 *
 */
public class TestIDList103 {

	/**
	 * Constructor for TestIDList103.
	 */
	@Test
	public void testIDList103() {
		try {
			IDList103Processor idl103 = new IDList103Processor();
			assertFalse(idl103.isCodeValid(""));
			assertFalse(idl103.isCodeValid("AMMAMM"));
			assertTrue(idl103.isCodeValid("AMM"));
			assertFalse(idl103.isCodeValid("10"));
			assertTrue(idl103.isCodeValid("AMM10"));
			assertFalse(idl103.isCodeValid("10AAM"));
			assertEquals("Ammo Pack Chemically Hardened Fibre",
					idl103.describe("AMM10"));
			assertEquals("AXB10", idl103.describe("AXB10"));
		} catch (Exception e1) {
			fail(e1.getMessage());
		}
	}

}
