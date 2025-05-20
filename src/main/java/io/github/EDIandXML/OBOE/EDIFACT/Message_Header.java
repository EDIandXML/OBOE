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

package io.github.EDIandXML.OBOE.EDIFACT;

import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateCompositeElement;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;

/**
 * static defined class for EDIFACT Segment Message_Header
 *
 */
public class Message_Header {

	public static String id = "UNH";
	public static String shortName = "MessageHeader";

	/**
	 * Factory method to create an Message Header
	 *
	 * @return Segment Message Header
	 * @exception OBOEException can be thrown
	 */

	public static Segment getInstance() throws OBOEException {
		TemplateCompositeElement tcde;

		TemplateSegment returnSeg = new TemplateSegment(id, "Message Header", 0,
				"Message Header", 1, 'M', shortName, true, null);
		returnSeg.addElement(
				new TemplateDataElement("0062", "Message reference number", 1,
						"AN", 'O', "Message reference number", 1, 14,
						"messageReferenceNumber", null, null, 1, true));

		tcde = new TemplateCompositeElement("S009", "messageIdentifier", 'O', 2,
				"messageIdentifier", "messageIdentifier", null, 1, true);
		tcde.addElement(
				new TemplateDataElement("0065", "Message type identifier", 1,
						"AN", 'O', "Message type identifier", 1, 6,
						"messageTypeIdentifier", null, null, 1, true));
		tcde.addElement(new TemplateDataElement("0052", "Message type version",
				2, "AN", 'O', "Message type version number", 1, 3,
				"messageTypeVersionNumber", null, null, 1, true));
		tcde.addElement(new TemplateDataElement("0054", "Message type release",
				3, "AN", 'O', "Message type release number", 1, 3,
				"messageTypeReleaseNumber", null, null, 1, true));
		tcde.addElement(new TemplateDataElement("0051", "Controlling agency", 4,
				"AN", 'O', "Controlling agency", 1, 2, "controllingAgency",
				null, null, 1, true));
		tcde.addElement(
				new TemplateDataElement("0057", "Association assigned code", 5,
						"AN", 'O', "Association assigned code", 1, 6,
						"associationAssignedCode", null, null, 1, true));
		returnSeg.addElement(tcde);

		returnSeg.addElement(
				new TemplateDataElement("0068", "Common access reference", 3,
						"AN", 'O', "Common access reference", 1, 35,
						"commonAccessReference", null, null, 1, true));

		tcde = new TemplateCompositeElement("S009", "messageIdentifier", 'O', 4,
				"messageIdentifier", "messageIdentifier", null, 1, true);
		tcde.addElement(
				new TemplateDataElement("0070", "Position message transfer", 1,
						"AN", 'O', "Position message transfer number", 1, 2,
						"positionMessageTransferNumber", null, null, 1, true));
		tcde.addElement(new TemplateDataElement("0073", "First/last", 2, "AN",
				'O', "First/last position message transfer indication", 1, 1,
				"firstLastPositionMessageTransferIndication", null, null, 1,
				true));
		returnSeg.addElement(tcde);

		return new Segment(returnSeg, null);
	}
}
