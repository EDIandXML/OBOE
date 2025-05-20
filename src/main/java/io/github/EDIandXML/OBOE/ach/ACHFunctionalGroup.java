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

package io.github.EDIandXML.OBOE.ach;

import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors.ERROR_TYPE;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateFunctionalGroup;

/**
 * class for container Functional_Group
 *
 */
public class ACHFunctionalGroup extends FunctionalGroup {
	/** static segment ids */

	public static String idHeader = "5";
	public static String idTrailer = "8";

	/**
	 * instantiates a functional group from the definition in an envelope
	 * message description file.
	 */

	public ACHFunctionalGroup(TemplateFunctionalGroup inTFG,
			IContainedObject inParent) {
		super(inTFG, inParent, idHeader, idTrailer);
		createAndAddSegment(idHeader);
		createAndAddSegment(idTrailer);
	}

	/**
	 * set the Transaction Count in the trailer object also sets the trailer
	 * "28" field from the headers "28" field
	 *
	 * @exception trailer not defined yet
	 */
	@Override
	public void setCountInTrailer() throws OBOEException {
		if (getTrailer() == null) {
			throw new OBOEException("trailer not defined yet");
		}

		getTrailer().setDataElementValue("28",
				getHeader().getElement("28").get());

		String l = Integer.toString(getTransactionSetCount());
		StringBuilder sb = new StringBuilder();
		int testL = ((DataElement) getTrailer().getElement("97")).getMinLength()
				- l.length();
		for (int i = 0; i < testL; i++) {
			sb.append('0');
		}
		sb.append(l);
		getTrailer().setDataElementValue("97", sb.toString());
	}

	/**
	 * validates
	 * 
	 * @return
	 *
	 * @exception OBOEException indicates why envelope is invalid
	 */

	@Override
	public boolean validate() throws OBOEException {
		Segment header = getHeader();
		Segment trailer = getTrailer();
		if (header != null) {
			header.validate();
		} else {
			throw new OBOEException("Missing FunctionalGroup Header");
		}
		int i;
		TransactionSet ts;

		for (i = 0; i < getTransactionSetCount(); i++) {
			ts = getTransactionSet(i);
			ts.validate();
		}
		if (getTransactionSetCount() < 1) {
			throw new OBOEException("No Transaction Sets Defined");
		}

		if (trailer != null) {
			trailer.validate();
		} else {
			throw new OBOEException("Missing FunctionalGroup Trailer");
		}
		return true;
	}

	/**
	 * validates doesn't throw exception but placess error message in
	 * DocumentErrors object
	 */

	@Override
	public void validate(DocumentErrors inDErr) {

		testMissing(inDErr);

		Segment header = getHeader();
		Segment trailer = getTrailer();

		if (header != null) {
			header.validate(inDErr);

		} else {
			inDErr.addError(0, "Envelope", "Missing FunctionalGroup Header",
					this, "3", this, ERROR_TYPE.Integrity);
		}

		int i;
		TransactionSet ts;
		if (getTransactionSetCount() == 0) {
			inDErr.addError(0, "FG", "No transaction sets", this, "4", this,
					ERROR_TYPE.Integrity);
		} else {
			for (i = 0; i < getTransactionSetCount(); i++) {
				ts = getTransactionSet(i);
				ts.validate(inDErr);
			}
		}

		if (getTransactionSetCount() < 1) {
			inDErr.addError(0, "FG", "No Transaction Sets Defined", this, "4",
					this, ERROR_TYPE.Integrity);
		}

		if (trailer != null) {
			trailer.validate(inDErr);
		} else {
			inDErr.addError(0, "FG", "Missing FunctionalGroup Trailer", this,
					"4", this, ERROR_TYPE.Integrity);
		}

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
