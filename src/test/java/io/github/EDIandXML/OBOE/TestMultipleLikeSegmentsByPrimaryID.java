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

import io.github.EDIandXML.OBOE.Containers.Table;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * @author Joe McVerry
 *
 */
public class TestMultipleLikeSegmentsByPrimaryID {

	@Test
	public void testUse003() {

		Util.setOBOEProperty("doPrevalidate", "true");
		TransactionSet ts = TransactionSetFactory.buildTransactionSet("003", "",
				"", "", "", "");

		Table t = ts.getHeaderTable();

		t.createAndAddSegmentWithPrimaryIDValue("DTP", "938");
		t.createAndAddSegmentWithPrimaryIDValue("DTP", "454");

		String test = t.getFormattedText(Format.UNDEFINED).trim();
		test = test.replaceAll("(\r\n|\n)", "|");
		assertEquals(
				"DTP: \tDate Time Qualifier: Order : 938|\tDate Time Period Format Qualifier: Date Expressed in Format CCYYMMDD : D8|\tOrder Date:  |DTP: \tDate Time Qualifier: Initial Treatment : 454|\tDate Time Period Format Qualifier: Date Expressed in Format CCYYMMDD : D8|\tInitial Treatment Date:"
						.trim(),
				test);

	}

}
