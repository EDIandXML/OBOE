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

import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateFunctionalGroup;

/**
 * class for container Functional_Group
 * 
 *
 */
public class EDIFACTFunctionalGroup extends FunctionalGroup
		implements IContainedObject {

	/** static segment ids */
	public static String idHeader = "UNG";
	public static String idTrailer = "UNE";

	/**
	 * instantiates a functional group from the definition in an envelope
	 * message description file.
	 *
	 * @param inParent owning Object
	 */

	public EDIFACTFunctionalGroup(TemplateFunctionalGroup inTFG,
			IContainedObject inParent) {
		super(inTFG, inParent, idHeader, idTrailer);
	}

	/**
	 * set the Transaction Count in the trailer object
	 */
	@Override
	public void setCountInTrailer() throws OBOEException {
		Segment header = getHeader();
		Segment trailer = getTrailer();
		if (trailer == null) {
			throw new OBOEException("trailer not defined yet");
		}
		trailer.setDataElementValue("0048", header.getElement("0048").get());
		trailer.setDataElementValue("0060",
				Integer.toString(getTransactionSetCount()));
	}

	@Override
	public Segment buildHeaderSegment() {
		Segment seg = new Segment(getMyTemplate().getTemplateSegment(idHeader),
				this);
		addContainer(seg);

		return seg;
	}

	@Override
	public Segment buildTrailerSegment() {
		Segment seg = new Segment(getMyTemplate().getTemplateSegment(idTrailer),
				this);
		addContainer(seg);

		return seg;
	}

}
