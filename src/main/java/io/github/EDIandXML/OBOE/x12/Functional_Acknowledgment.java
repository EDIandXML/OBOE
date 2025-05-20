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

package io.github.EDIandXML.OBOE.x12;

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.TransactionSetFactory;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.Table;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.DataElements.Element;
import io.github.EDIandXML.OBOE.Errors.OBOEException;

public class Functional_Acknowledgment {

	/**
	 * Static class method used to generate a 997 TransactionSet. It always
	 * returns a positive acknowledgment and assumes it is reponding to 1
	 * transaction set.
	 *
	 * @param tsRespondingTo TransactionSet ts we're responding to
	 * @param ISASegment     Segment ISA segment used for control numbers to
	 *                       indicate what we are responding to
	 * @return TransactionSet - functional acknowledgement transaction set
	 * @exception OBOEException - for instance using FA to respond to a FA
	 *                          is not correct.
	 */

	public static TransactionSet postiveAcknowledgment(
			TransactionSet tsRespondingTo, Segment ISASegment)
			throws OBOEException {

		if (tsRespondingTo.getID().compareTo("997") == 0) {
			throw new OBOEException(
					"Functional Acknowledgment should not be used to respond to another Functional Acknowledgment");
		}

		Table responseTable, currentTable;
		Segment responseSegment, currentSegment;
		Element currentDE;

		responseTable = tsRespondingTo.getHeaderTable();
		responseSegment = responseTable.getSegment("ST");

		TransactionSet TransactionSet = TransactionSetFactory
				.buildTransactionSet("997");
		TransactionSet.setFormat(Format.X12_FORMAT);

		currentTable = TransactionSet.getHeaderTable();

		currentSegment = currentTable.createAndAddSegment("ST");

		currentDE = currentSegment.buildElement(1);
		((DataElement) currentDE).set("997");
		currentDE = currentSegment.buildElement(2);
		((DataElement) currentDE).set(responseSegment.getElement(3).get());

		currentSegment = currentTable.createAndAddSegment("AK1");

		currentDE = currentSegment.buildElement(3);
		((DataElement) currentDE).set(tsRespondingTo.getFunctionalGroup());
		currentDE = currentSegment.buildElement(4);
		((DataElement) currentDE).set(ISASegment.getElement("ISA13").get());

		currentSegment = currentTable.createAndAddSegment("AK9");

		currentDE = currentSegment.buildElement(5);
		((DataElement) currentDE).set("A");
		currentDE = currentSegment.buildElement(6);
		((DataElement) currentDE).set("1");
		currentDE = currentSegment.buildElement(7);
		((DataElement) currentDE).set("1");

		currentSegment = currentTable.createAndAddSegment("SE");

		currentDE = currentSegment.buildElement(8);
		((DataElement) currentDE).set("4");
		currentDE = currentSegment.buildElement(9);
		((DataElement) currentDE).set(responseSegment.getElement(3).get());

		return TransactionSet;
	}

}
