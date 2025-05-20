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

package io.github.EDIandXML.OBOE.Containers;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateFunctionalGroup;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for container Functional_Group
 */
public abstract class FunctionalGroup extends MetaContainer
		implements IContainedObject

{
	String headerId;
	Segment headerSegment;

	/**
	 * @param headerSegment the headerSegment to set
	 */
	public abstract Segment buildHeaderSegment();

	/**
	 * @param trailerSegment the trailerSegment to set
	 */
	public abstract Segment buildTrailerSegment();

	String trailerId;
	Segment trailerSegment;

	/**
	 * instantiates a functional group
	 */

	public FunctionalGroup(TemplateFunctionalGroup inTFG,
			IContainedObject inParent, String inHeaderID, String inTrailerID) {
		super(inTFG);
		setParent(inParent);
		headerId = inHeaderID;
		trailerId = inTrailerID;
	}

	public void setHeaderId(String headerId) {
		this.headerId = headerId;
	}

	public void setTrailerId(String trailerId) {
		this.trailerId = trailerId;
	}

	/**
	 * add a transaction set to the ArrayList (container)
	 *
	 * @param inTransactionSet
	 * @exception used for X12FunctionalGroup
	 */
	public void addTransactionSet(TransactionSet inTransactionSet)
			throws OBOEException {
		addContainerDontUseID(inTransactionSet);
		inTransactionSet.setParent(this);

	}

	/**
	 * get a transaction set from the ArrayList <br>
	 * can throw runtime exception array out of bounds
	 *
	 * @param pos position in ArrayList
	 * @return TransactionSet
	 */

	public TransactionSet getTransactionSet(int pos) {
		return (TransactionSet) getContainer(ContainerType.TransactionSet, "",
				pos);
	}

	/**
	 * get the transaction set ArrayList
	 *
	 * @return ArrayList of transaction sets
	 */
	public ArrayList<IContainedObject> getTransactionSets() {
		return getAllContainers(ContainerType.TransactionSet, "");
	}

	/**
	 * helper routine to get header segment
	 *
	 *
	 * @return segment
	 */

	public Segment getHeader() {
		return getSegment(headerId);
	}

	/**
	 * helper routine to get trailer segment
	 *
	 *
	 * @return segment
	 */

	public Segment getTrailer() {
		return getSegment(trailerId);
	}

	/**
	 * returns the EDI (EDIFACT) formatted document in a String
	 *
	 * @param format int - format type see TransactionSet
	 * @return String the formatted document
	 *
	 */

	public String getFormattedText(Format format) {

		StringBuilder sb = new StringBuilder();
		if (format == Format.CSV_FORMAT) {
			sb.append("Functional Group" + Util.lineFeed);
		} else if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			sb.append("<functionalgroup>" + Util.lineFeed);
		}

		Segment seg = getSegment(headerId);
		if (seg != null) {
			sb.append(seg.getFormattedText(format));
		}

		for (var tsStream : getKeysByContainerType(
				ContainerType.TransactionSet)) {
			for (var eachTS : getAllContainers(ContainerType.TransactionSet,
					tsStream.ID())) {
				sb.append(((TransactionSet) eachTS).getFormattedText(format));
			}
		}

		seg = getSegment(trailerId);
		if (seg != null) {
			sb.append(seg.getFormattedText(format));
		}

		if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			sb.append("</functionalgroup>" + Util.lineFeed);
		}

		return sb.toString();
	}

	/**
	 * like getFormattedText; writes to a Writer object instead of building
	 * a string.
	 *
	 * @param inWriter writer - object written to
	 * @param format   int - format type see TransactionSet
	 * @exception OBOEException
	 * @exception IOException
	 */

	public void writeFormattedText(Writer inWriter, Format format)
			throws IOException {
		if (format == Format.CSV_FORMAT) {
			inWriter.write("Functional Group" + Util.lineFeed);
		} else if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			inWriter.write("<functionalgroup>" + Util.lineFeed);
		}

		Segment seg = getSegment(headerId);
		if (seg != null) {
			inWriter.write(seg.getFormattedText(format));
		}

		for (var tsStream : getKeysByContainerType(
				ContainerType.TransactionSet)) {
			for (var eachTS : getAllContainers(ContainerType.TransactionSet,
					tsStream.ID())) {
				((TransactionSet) eachTS).writeFormattedText(inWriter, format);
			}
		}

		seg = getSegment(trailerId);
		if (seg != null) {
			inWriter.write(seg.getFormattedText(format));
		}

		if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			inWriter.write("</functionalgroup>" + Util.lineFeed);
		}

		inWriter.flush();

	}

	/**
	 * validates
	 *
	 * @exception OBOEException indicates why envelope is invalid
	 */

	@Override
	public boolean validate() throws OBOEException {
		boolean validateResponse = true;
		Segment seg = getSegment(headerId);
		if (seg != null) {
			validateResponse &= seg.validate();
		}
		int i;
		TransactionSet ts;
		for (i = 0; i < getTransactionSetCount(); i++) {
			ts = getTransactionSet(i);
			validateResponse &= ts.validate();
		}

		seg = getSegment(trailerId);
		if (seg != null) {
			validateResponse &= seg.validate();
		}
		return validateResponse;
	}

	/**
	 * validates <br>
	 * doesn't throw exception but placess error message in DocumentErrors
	 * object
	 */

	@Override
	public void validate(DocumentErrors inDErr) {
		Segment seg = getSegment(headerId);
		if (seg != null) {
			seg.validate(inDErr);
		}

		if (getTransactionSetCount() == 0) {
			inDErr.addError(0, "FG", "No transaction sets", this, "5", this,
					DocumentErrors.ERROR_TYPE.Integrity);
		} else {
			for (var tsStream : getKeysByContainerType(
					ContainerType.TransactionSet)) {
				for (var eachTS : getAllContainers(ContainerType.TransactionSet,
						tsStream.ID())) {
					((TransactionSet) eachTS).validate(inDErr);
				}
			}

		}
		seg = getSegment(trailerId);
		if (seg != null) {
			seg.validate(inDErr);
		}

	}

	/**
	 * set the Transaction Count in the trailer object
	 */
	public abstract void setCountInTrailer() throws OBOEException;

	/**
	 * method of SegmentContainer interface <br>
	 * Functional Group's segments built from instance methods so we return
	 * a null and hope the call can figure it out
	 *
	 * @param inID - template segment to get
	 * @return TemplateSegment - in this case a null
	 */
	public TemplateSegment getTemplateSegment(String inID)

	{
		return (TemplateSegment) getMyTemplate()
				.getContainer(ContainerType.Segment, inID);

	}

	/**
	 * returns the ID which is "envelope". <br>
	 * required for SegmentContainer interface
	 */

	@Override
	public String getID() {
		return "envelope";
	}

	protected IContainedObject parent = null;

	/**
	 * sets parent attribute
	 *
	 * @param inParent TemplateSegmentContainer
	 */
	@Override
	public void setParent(IContainedObject inParent) {
		parent = inParent;
	}

	/**
	 * gets parent attribute
	 *
	 * @return TemplateSegmentContainer
	 */
	@Override
	public IContainedObject getParent() {
		return parent;
	}

	@Override
	public String getShortName() {
		return "FunctionalGroup";
	}

	/**
	 * the toString method
	 */
	@Override
	public String toString() {
		return "functional group  format:" + getID() + " id:" + getID();

	}

	@Override
	public int trim() {
		int cnt = 0;
		for (int i = 0; i < this.getTransactionSetCount(); i++) {
			cnt += this.getTransactionSet(i).trim();
		}
		return cnt;
	}

	@Override
	public ContainerType getContainerType() {
		return ContainerType.FunctionalGroup;
	}
}
