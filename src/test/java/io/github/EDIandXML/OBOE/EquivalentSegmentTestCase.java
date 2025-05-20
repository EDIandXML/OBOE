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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileReader;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.util.Util;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;

public class EquivalentSegmentTestCase {

	@BeforeAll
	public static void setup() {
		EnvelopeFactory.reloadbuiltTable();
	}

	@Test
	public void testOnlyOneGoodOne() {

		X12DocumentHandler xdh = new X12DocumentHandler();
		try {
			// two segments with unique primary keys
			Util.setOBOEProperty(Util.DO_PREVALIDATE, "true");
			Util.setOBOEProperty(Util.THROW_PARSING_EXCEPTION, "false");
			xdh.getParser()
					.parseDocument(new FileReader(
							"testfiles/EquivalentSegmentTestMessage.001"),
							true);
			assertTrue(xdh.getDocumentErrors().getErrorCount() == 0);
			xdh.getDocumentErrors().logErrors();
		} catch (OBOEException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testShouldOnlyBeOne() {
		// 1. this issue is in the EDI/X12 message parser.
		// For example, if I have two successive DTP segments in the X12 message
		// with the same primary data element,
		// the message parser will not catch this error.
		// It should catch it when the message description defines that DTP
		// segment with
		// occurs="1"
		// (this means that there could be at most one DTP segment with that
		// primary data
		// element in the X12 message)

		X12DocumentHandler xdh = new X12DocumentHandler();
		try {
			Util.setOBOEProperty(Util.DO_PREVALIDATE, "true");
			Util.setOBOEProperty(Util.THROW_PARSING_EXCEPTION, "false");
			// 2 segments with same key but only one should exist
			xdh.getParser()
					.parseDocument(new FileReader(
							"testfiles/EquivalentSegmentTestMessage.002"),
							true);
			assertEquals(5, xdh.getDocumentErrors().getErrorCount());
			assertEquals("DTP", xdh.getDocumentErrors().getErrorID(0));
			assertTrue(xdh.getDocumentErrors().getErrorDescription(0)
					.contains("value of 938 is invalid"));
		} catch (OBOEException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testOneWithABadKeyValue() {

		// 2. The EDI/X12 parser is not catching segments with invalid primary
		// data element values.
		// That's correct. This is happening with the DTP segments, I haven't
		// checked other types of segments.

		X12DocumentHandler xdh = new X12DocumentHandler();
		try {
			Util.setOBOEProperty(Util.DO_PREVALIDATE, "true");
			Util.setOBOEProperty(Util.THROW_PARSING_EXCEPTION, "false");
			// 1 segments with bogus primary key
			xdh.getParser()
					.parseDocument(new FileReader(
							"testfiles/EquivalentSegmentTestMessage.003"),
							true);
			assertEquals(7, xdh.getDocumentErrors().getErrorCount());
			assertEquals("DTP", xdh.getDocumentErrors().getErrorID(0));
			assertTrue(xdh.getDocumentErrors().getErrorDescription(0)
					.contains("value of JOE is invalid"));

			// xdh.getDocumentErrors().logErrors();
		} catch (OBOEException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

}