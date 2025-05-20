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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.Containers.Table;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * @author Joe McVerry
 *
 */
public class TestSegmentsByPrimaryID {

	@Test
	public void testUse003() {

		Util.setOBOEProperty("doPrevalidate", "true");
		TransactionSet ts = TransactionSetFactory.buildTransactionSet("003", "",
				"", "", "", "");

		try {
			Table t = ts.getHeaderTable();
			t.createAndAddSegment("DTP", "938");
			t.createAndAddSegment("DTP", "96638");

		} catch (OBOEException oe) {
			assertTrue(oe.getMessage().equals(
					"Segment DTP with primaryIDValue of 96638 unknown to container."));

		}

	}

	public void testUse004() {
		Util.setOBOEProperty("doPrevalidate", "false");
		TransactionSet ts = TransactionSetFactory.buildTransactionSet("004", "",
				"", "", "", "");

		try {
			Table t = ts.getHeaderTable();
			t.createAndAddSegment("HrI", "BrK");

		} catch (OBOEException oe) {
			assertTrue(oe.getMessage().equals(
					"Segment HrI with primaryIDValue of BrK unknown to container."));

		}

	}

}
