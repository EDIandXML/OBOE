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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.DataElements.Element;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for Segments
 * 
 * OBOE - Open Business Objects for EDI
 *
 */

public class Segment implements IElementContainer, IContainedObject {

	/**
	 * TemplateSegment that can build this segment
	 */
	public TemplateSegment myTemplate = null;

	/**
	 * position within the X12 or EDIFACT document
	 */
	protected int positionInIncomingDocument = -1;

	protected int byteOffsetPosition = -1;

	/** log4j object */
	static Logger logr = LogManager.getLogger(Segment.class);

	public ElementContainer myElementContainer;

	/**
	 * create a Segment based on its template
	 *
	 * @param inTemplateSegment predefined TemplateSegment
	 * @param inParent          owning Object
	 */

	public Segment(TemplateSegment inTemplateSegment,
			IContainedObject inParent) {
		super();
		myTemplate = inTemplateSegment;
		myElementContainer = new ElementContainer(inTemplateSegment, this);

		for (var tesEntries : myTemplate.getAllTemplateElementsValues()) {
			if (!tesEntries.isRequired()) {
				continue;
			}

			buildElement(tesEntries.getPosition());

		}

		setParent(inParent);
	}

	public TemplateSegment getMyTemplate() {
		return myTemplate;
	}

	/**
	 * returns the TemplateSegment used to build the Segment
	 *
	 * @return TemplateSegment
	 */
	public TemplateSegment getTemplate() {
		return myTemplate;
	}

	/**
	 * gets the segment id
	 *
	 * @return String id
	 */

	@Override
	public String getID() {
		return myTemplate.getID();
	}

	/**
	 * gets the segment Name
	 *
	 * @return String Name
	 */

	public String getName() {
		return myTemplate.getName();
	}

	/**
	 * returns the occurs value
	 *
	 * @return int occurs value
	 *
	 */

	public int getOccurs() {
		return myTemplate.getOccurs();
	}

	/**
	 * returns the required flag
	 *
	 * @return char required
	 *
	 */

	public char getRequired() {
		return myTemplate.getRequired();
	}

	/**
	 * returns the xml tag field
	 *
	 * @return String tag value
	 */

	@Override
	public String getShortName() {
		return myTemplate.getShortName();
	}

	/**
	 * returns the used indicator
	 *
	 * @return boolean
	 */
	public boolean isUsed() {
		return myTemplate.isUsed();

	}

	/**
	 * gets the segment position
	 *
	 * @return int position number
	 */
	public int getPosition() {
		return myTemplate.getPosition();
	}

	/**
	 * this is here as a stub because of the segment container interface
	 *
	 * @return Segment
	 * @param ID String
	 * @throws OBOEException as thrown
	 */

	/**
	 * returns the Short Description for the Segment
	 *
	 * @return String
	 */
	public String getDescription() {
		return myTemplate.getDescription();
	}

	/**
	 * defines an element by the predefined map
	 *
	 * @param pos int <br>
	 *            <b>position is relative to 1.</b>
	 * @return DataElement or Composite
	 */
	@Override
	public Element buildElement(int pos) {

		return myElementContainer.buildElement(pos);
	}

	/**
	 * used to verify if the segment is built correctly.
	 *
	 * @return boolean true if segment built correctly <br>
	 *         Basic Edition always returns true
	 */
	@Override
	public boolean validate() {

		DocumentErrors dErr = new DocumentErrors();
		if (isUsed() == false) {
			dErr.addError(0, getID(), "Segment Not Used", this.getParent(), "6",
					this, DocumentErrors.ERROR_TYPE.Integrity);
		}
		return myElementContainer.validate();

	}

	/**
	 * used to verify if the segment is built correctly
	 *
	 * @return boolean true if segment built correctly
	 * @param inDErr DocumentErrorsObject
	 */
	@Override
	public boolean validate(DocumentErrors inDErr) {

		if (isUsed() == false) {
			inDErr.addError(0, getID(), "Segment Not Used", this.getParent(),
					"6", this, DocumentErrors.ERROR_TYPE.Integrity);
			return false;
		}
		return myElementContainer.validate(inDErr);

	}

	/**
	 * returns the formatted text
	 *
	 * @param format indicating x12, edificact...
	 * @return String
	 */

	@Override
	public String getFormattedText(Format format) {

		if (myElementContainer.getContainerSize() == 0) {
			return "";
		}
		StringBuilder sbFormattedText = new StringBuilder();

		switch (format) {
		case CSV_FORMAT:
			sbFormattedText.append("Segment," + getID() + ",\"" + getName()
					+ "\"" + Util.lineFeed);
			break;
		case XML_FORMAT:
			sbFormattedText.append('<' + getShortName() + ">" + Util.lineFeed);
			break;
		case VALID_XML_FORMAT:
		case VALID_XML_FORMAT_WITH_POSITION:
			sbFormattedText.append("<segment code=\"" + getID() + "\"");
			sbFormattedText.append(" name=\"" + getName() + "\"");
			if (format == Format.VALID_XML_FORMAT_WITH_POSITION) {
				sbFormattedText.append(" docPosition=\""
						+ this.getPositionInIncomingDocument() + "\"");
			}
			sbFormattedText.append(">" + Util.lineFeed);
			break;

		case PREBUILD_FORMAT:
			sbFormattedText.append(getID());
			break;
		case X12_FORMAT:
			sbFormattedText.append(getID());
			break;
		case EDIFACT_FORMAT:
			sbFormattedText.append(getID());
			break;
		case TRADACOMS_FORMAT:
			sbFormattedText.append(getID());
			break;
		default:
			sbFormattedText.append(getID() + ": ");
		}

		sbFormattedText.append(myElementContainer.getFormattedText(format));

		switch (format) {
		case PREBUILD_FORMAT:
			sbFormattedText
					.append(Envelope.PREBUILD_SEGMENT_DELIMITER.charAt(0));
			break;
		case X12_FORMAT:
			sbFormattedText.append(Envelope.X12_SEGMENT_DELIMITER.charAt(0));
			break;
		case EDIFACT_FORMAT:
			sbFormattedText
					.append(Envelope.EDIFACT_SEGMENT_DELIMITER.charAt(0));
			break;
		case TRADACOMS_FORMAT:
			sbFormattedText
					.append(Envelope.TRADACOMS_SEGMENT_DELIMITER.charAt(0));
			break;

		case XML_FORMAT:
			sbFormattedText.append("</" + getShortName() + ">" + Util.lineFeed);
			break;
		case VALID_XML_FORMAT:
		case VALID_XML_FORMAT_WITH_POSITION:

			sbFormattedText.append("</segment>" + Util.lineFeed);
			break;
		default:
			break;
		}

		return new String(sbFormattedText);

	}

	/**
	 * returns the number of defined data element
	 *
	 * @return int DataElement count
	 */

	@Override
	public int getContainerSize() {
		return myElementContainer.getContainerSize();
	}

	/**
	 * returns the length of the data in the data elements
	 *
	 * @return int
	 */
	@Override
	public int getElementCount() {
		return myElementContainer.getElementCount();
	}

	/**
	 * returns a data element by its id
	 *
	 * @return DataElement
	 * @param inID ID of dataelement to find
	 */

	@Override
	public Element getElement(String inID) {

		return myElementContainer.getElement(inID);
	}

	/**
	 * returns a data element by its id, from an offset
	 *
	 * @return DataElement
	 * @param inID     ID of dataelement to find
	 * @param inoffset int
	 */

	@Override
	public Element getElement(String inID, int inoffset) {
		return myElementContainer.getElement(inID, inoffset);
	}

	/**
	 * returns a data element by its id off of an offset
	 *
	 * @param inName   name of dataelement to find
	 * @param inoffset int
	 * @return DataElement
	 */

	@Override
	public DataElement getDataElementByName(String inName, int inoffset) {

		return myElementContainer.getDataElementByName(inName, inoffset);
	}

	/**
	 * returns a data element by its position number, not location within
	 * ArrayList
	 *
	 * @return DataElement
	 * @param inPosition int position of dataelement <br>
	 *                   <b>position is relative to 1.</b>
	 */

	@Override
	public Element getElement(int inPosition) {

		return myElementContainer.getElement(inPosition);
	}

	/**
	 * returns a boolean if ArrayList position held by a data element
	 *
	 * @return boolean
	 * @param inPosition is object in the array a dataelement <br>
	 *                   <b>position is relative to 1.</b>
	 */

	@Override
	public boolean isDataElement(int inPosition) {

		return myElementContainer.isDataElement(inPosition);
	}

	/**
	 * returns a boolean if ArrayList position held by a composite
	 *
	 * @return boolean
	 * @param inPosition int is object in array at this position a composite
	 *                   <br>
	 *                   <b>position is relative to 1.</b>
	 */

	@Override
	public boolean isCompositeElement(int inPosition) {
		return myElementContainer.isCompositeElement(inPosition);
	}

	/**
	 * removes empty trailing data elements and returns the number of used
	 * dataelements
	 *
	 * @return int
	 */

	@Override
	public int trim() {
		return myElementContainer.trim();
	}

	/**
	 * routine to ask if it uses a dataelement
	 *
	 * @return int position of data element
	 * @param inTag   String id
	 * @param startAt prepositioning
	 */
	@Override
	public int doIUseThisXMLElement(String inTag, int startAt) {

		return myElementContainer.doIUseThisXMLElement(inTag, startAt);

	}

	/**
	 * sets the default value for the data elements <br>
	 * will create mandatory subsegments. <br>
	 * if mandatory subsegment is part of the collection it will create the
	 * first one
	 */
	@Override
	public void useDefault() {

		myElementContainer.useDefault();

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

	/**
	 * returns the data element associated with being known as making this
	 * segment unique
	 *
	 * @return DataElement
	 */

	@Override
	public Element getPrimaryIDDE() {
		return myElementContainer.getPrimaryIDDE();
	}

	/**
	 * sets the x12 or EDIFACT position for incoming documnents
	 *
	 * @param inPos
	 */
	public void setPosition(int inPos) {
		positionInIncomingDocument = inPos;
	}

	public int getByteOffset() {
		return byteOffsetPosition;
	}

	public void setByteOffset(int byteOffsetPosition) {
		this.byteOffsetPosition = byteOffsetPosition;
	}

	/**
	 * gets the x12 or EDIFACT position for incoming documnents
	 *
	 * @return int
	 */
	public int getPositionInIncomingDocument() {
		return positionInIncomingDocument;
	}

	/**
	 * the toString method
	 */
	@Override
	public String toString() {
		return "segment id:" + getID() + " name:" + getName() + " seq:"
				+ getPosition();

	}

	@Override
	public Element buildElement(String ID) {

		return myElementContainer.buildElement(ID);
	}

	@Override
	public Element buildElement(String ID, int offset) {

		return myElementContainer.buildElement(ID, offset);
	}

	@Override
	public String getDataElementValue(String ID) throws OBOEException {
		Element de = getElement(ID);
		if (de == null) {
			return "";
		}
		return de.get();
	}

	@Override
	public String getDataElementValue(int pos) throws OBOEException {
		Element de = getElement(pos);
		if (de == null) {
			return "";
		}
		return de.get();
	}

	@Override
	public Element setDataElementValue(String ID, String inValue)
			throws OBOEException {
		return myElementContainer.setDataElementValue(ID, inValue);

	}

	@Override
	public Element setDataElementValue(String ID, int offset, String inValue)
			throws OBOEException {
		return myElementContainer.setDataElementValue(ID, offset, inValue);

	}

	@Override
	public Element setDataElementValue(int pos, String inValue)
			throws OBOEException {
		return myElementContainer.setDataElementValue(pos, inValue);
	}

	@Override
	public ContainerType getContainerType() {

		return ContainerType.Segment;
	}

}
