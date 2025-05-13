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

import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.DataElements.Element;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateFunctionalGroup;

/**
 * class for container Functional_Group
 *
 */
public class X12FunctionalGroup extends FunctionalGroup {
	/** static segment ids */

	public static String idHeader = "GS";
	public static String idTrailer = "GE";

	/**
	 * instantiates a functional group from the definition in an envelope
	 * message description file.
	 */

	public X12FunctionalGroup(TemplateFunctionalGroup inTFG,
			IContainedObject inParent) {
		super(inTFG, inParent, idHeader, idTrailer);

	}

	/**
	 * set the Transaction Count in the trailer object also sets the trailer
	 * "28" field from the headers "28" field
	 *
	 * @exception trailer not defined yet
	 */
	@Override
	public void setCountInTrailer() throws OBOEException {

		Segment header = getHeader();
		Segment trailer = getTrailer();
		if (trailer == null) {
			throw new OBOEException("trailer not defined yet");
		}

		trailer.setDataElementValue("28", header.getElement("28").get());

		String l = Integer.toString(getTransactionSetCount());
		StringBuilder sb = new StringBuilder();
		DataElement de = (DataElement) trailer.getElement("97");
		int testL = de.getMinLength() - l.length();
		for (int i = 0; i < testL; i++) {
			sb.append('0');
		}
		sb.append(l);
		trailer.setDataElementValue("97", sb.toString());
	}

	/**
	 * validates
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
		String fggrp = header.getElement(1).get();
		for (i = 0; i < getTransactionSetCount(); i++) {
			ts = getTransactionSet(i);
			if (fggrp.compareTo(ts.getFunctionalGroup()) != 0) {
				throw new OBOEException(
						"Functional Identifier Code (GS01-479) value is "
								+ fggrp
								+ " does not match for Transaction Set ID "
								+ ts.getID() + "-" + ts.getFunctionalGroup());
			}
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
		if ((header != null) && (trailer != null)) {
			Element de1 = header.getElement("28");
			Element de2 = trailer.getElement("28");
			if ((de1 == null) || (de2 == null)) {
				throw new OBOEException("Missing GE01 and or GE02");
			}
			if (de1.get().compareTo(de2.get()) != 0) {
				throw new OBOEException("Control number mismatch (28)");
			}
			de1 = trailer.getElement("97");
			int saidCount = Integer.parseInt(de1.get());
			int readCount = getTransactionSetCount();
			if (saidCount != readCount) {
				throw new OBOEException("Transaction Set Count Mismatch. Said "
						+ saidCount + ".  Should Be " + readCount);
			}
		}
		return true;
	}

	/**
	 * validates doesn't throw exception but placess error message in
	 * DocumentErrors object
	 */

	@Override
	public void validate(DocumentErrors inDErr) {
		String fggrp = "";
		boolean hNoErr = false, tNoErr = false;
		Segment header = getHeader();
		Segment trailer = getTrailer();
		if (header != null) {
			hNoErr = header.validate(inDErr);
			// if (hNoErr == false) {
			// return;
			// }
			fggrp = header.getElement(1).get();
		} else {
			inDErr.addError(0, "Envelope", "Missing FunctionalGroup Header",
					this, "3", this, DocumentErrors.ERROR_TYPE.Integrity);
		}

		int i;
		TransactionSet ts;
		if (getTransactionSetCount() == 0) {
			inDErr.addError(0, "FG", "No transaction sets", this, "4", this,
					DocumentErrors.ERROR_TYPE.Integrity);
		} else {
			for (i = 0; i < getTransactionSetCount(); i++) {
				ts = getTransactionSet(i);
				if (fggrp.compareTo(ts.getFunctionalGroup()) != 0) {
					inDErr.addError(0, "FG",
							"Functional Identifier Code (GS01-479) value is "
									+ fggrp
									+ " does not match for Transaction Set ID "
									+ ts.getID() + "-"
									+ ts.getFunctionalGroup(),
							this, "6", this,
							DocumentErrors.ERROR_TYPE.Requirement);
				}
				ts.validate(inDErr);
			}
		}

		if (getTransactionSetCount() < 1) {
			inDErr.addError(0, "FG", "No Transaction Sets Defined", this, "4",
					this, DocumentErrors.ERROR_TYPE.Integrity);
		}

		if (trailer != null) {
			tNoErr = trailer.validate(inDErr);
		} else {
			inDErr.addError(0, "FG", "Missing FunctionalGroup Trailer", this,
					"4", this, DocumentErrors.ERROR_TYPE.Integrity);
		}

		if ((header != null) && (trailer != null) && (hNoErr == true)
				&& (tNoErr == true)) {
			Element de1 = header.getElement("28");
			Element de2 = trailer.getElement("28");
			if ((de1 != null) && (de2 != null)) {
				if ((de1.get() != null) && (de2.get() != null)) {
					if (de1.get().compareTo(de2.get()) != 0) {
						inDErr.addError(0, "FG", "Control number mismatch (28)",
								this, "4", header,
								DocumentErrors.ERROR_TYPE.Integrity);
					}
				}
			}
			de1 = trailer.getElement("97");
			int saidCount = Integer.parseInt(de1.get());
			int readCount = this.getTransactionSetCount();
			if (saidCount != readCount) {
				inDErr.addError(0, "97",
						"Transaction Set Count Mismatch.  Should Be "
								+ readCount,
						this, "7", trailer.getElement("97"),
						DocumentErrors.ERROR_TYPE.Integrity);
			}

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
