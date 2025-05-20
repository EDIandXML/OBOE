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

package io.github.EDIandXML.OBOE.DataElements;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.ContainerType;
import io.github.EDIandXML.OBOE.Containers.ElementContainer;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.IElementContainer;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.ITemplateElementContainer;
import io.github.EDIandXML.OBOE.Templates.TemplateCompositeElement;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for all Composite Data Elements
 *
 * 
 */

public class CompositeElement extends Element
		implements IElementContainer, IContainedObject {
	/** log4j object */
	static Logger logr = LogManager.getLogger(CompositeElement.class);

	/**
	 * cursor to current Element Container Group
	 */
	protected int cursor = -1;
	protected ElementContainer currentElementContainer;

	protected ArrayList<ElementContainer> groupOfMyElements;
	// composites can occur multiple times.

	/**
	 * @return the groupOfMyElements
	 */
	public ArrayList<ElementContainer> getGroupOfMyElements() {
		return groupOfMyElements;
	}

	/**
	 * creates a composite using a template
	 *
	 * @param inTemplateComposite TemplateComposite used to build this
	 *                            composite
	 * @param inParent            owning Object
	 */
	public CompositeElement(TemplateCompositeElement inTemplateComposite,
			IContainedObject inParent) {

		myTemplate = inTemplateComposite;
		setParent(inParent);
		groupOfMyElements = new ArrayList<>();

		currentElementContainer = createNewElementContainer();

		cursor = 0;
	}

	/**
	 * returns the templateComposite object used to build this CompoisteDE
	 *
	 * @return TemplateComposite
	 */
	@Override
	public TemplateCompositeElement getTemplate() {
		return (TemplateCompositeElement) myTemplate;
	}

	/**
	 * returns the length of the data in the data elements
	 *
	 * @return int
	 */
	@Override
	public int getElementCount() {

		int size = 0;
		for (var ec : groupOfMyElements) {
			size += ec.getElementCount();
		}
		return size;

	}

	/**
	 * returns the formatted text
	 *
	 * @param format x12, EDIFACT...
	 * @return String of formatted text
	 */

	@Override
	public String getFormattedText(Format format) {

		if (getOccurs() > 1) {
			resetCursor();
		}
		if (currentElementContainer.hasText() == false) {
			return "";
		}

		if (groupOfMyElements.size() == 0) {
			if ((format == Format.X12_FORMAT)
					|| (format == Format.EDIFACT_FORMAT)
					|| (format == Format.TRADACOMS_FORMAT)) {
				return "";
			}
		}

		StringBuilder sbFormattedText = new StringBuilder();

		for (var ec : groupOfMyElements) {

			switch (format) {
			case CSV_FORMAT:
				sbFormattedText.append("Composite DE," + getID() + ","
						+ getName() + "\"" + Util.lineFeed);
				break;
			case XML_FORMAT:
				if (getShortName() != null) {
					sbFormattedText.append('<' + getShortName());
					sbFormattedText.append(">" + Util.lineFeed);
				} else {
					sbFormattedText.append('<' + getID());
					sbFormattedText.append(">" + Util.lineFeed);
				}
				break;
			case VALID_XML_FORMAT:
			case VALID_XML_FORMAT_WITH_POSITION:
				sbFormattedText.append("<composite code=\"" + getID() + "\"");
				sbFormattedText.append(" name=\"" + getName() + "\"");
				if (format == Format.VALID_XML_FORMAT_WITH_POSITION) {
					sbFormattedText.append(
							" docPosition=\"" + this.getPosition() + "\"");
				}
				sbFormattedText.append(">" + Util.lineFeed);
				break;

			case PREBUILD_FORMAT:
				break;
			case X12_FORMAT:
				break;
			case EDIFACT_FORMAT:
				break;
			case TRADACOMS_FORMAT:
				break;
			default:
				sbFormattedText.append(getID() + ": ");
			}

			sbFormattedText.append(ec.getFormattedText(format));

			switch (format) {
			case XML_FORMAT:
				if (getShortName() != null) {
					sbFormattedText.append(
							"</" + getShortName() + ">" + Util.lineFeed);
				} else {
					sbFormattedText
							.append("</" + getID() + ">" + Util.lineFeed);
				}
				break;
			case VALID_XML_FORMAT:
			case VALID_XML_FORMAT_WITH_POSITION:

				sbFormattedText.append("</composite>" + Util.lineFeed);
				break;
			case X12_FORMAT:
				break;
			case EDIFACT_FORMAT:
				break;
			default:
				break;
			}
			if (getOccurs() > 1) {
				if (nextGroup() == null) {
					break;
				}
				switch (format) {
				case PREBUILD_FORMAT:
					sbFormattedText.append(
							Envelope.PREBUILD_REPEAT_DELIMITER.charAt(0));
					break;
				case X12_FORMAT:
					if (Envelope.X12_REPEAT_DELIMITER.charAt(0) != '\u0000') {
						sbFormattedText.append(
								Envelope.X12_REPEAT_DELIMITER.charAt(0));
					}
					break;
				case EDIFACT_FORMAT:
					sbFormattedText.append(
							Envelope.EDIFACT_REPEAT_DELIMITER.charAt(0));
					break;
				case TRADACOMS_FORMAT:
					sbFormattedText.append(
							Envelope.TRADACOMS_REPEAT_DELIMITER.charAt(0));
					break;
				default:
					break;
				}
			}
		}

		return new String(sbFormattedText);

	}

	private int resetCursor() {
		cursor = 0;
		currentElementContainer = groupOfMyElements.get(cursor);
		return cursor;
	}

	private ElementContainer createNewElementContainer() {
		currentElementContainer = new ElementContainer(
				(ITemplateElementContainer) myTemplate, this);
		groupOfMyElements.add(currentElementContainer);
		cursor = 0;
		return currentElementContainer;
	}

	public int gotoGroup(int inPos) throws OBOEException {

		if (inPos < 0) {
			throw new OBOEException("inPos too small.");
		}
		if (inPos >= groupOfMyElements.size()) {
			throw new OBOEException("inPos too large for number of groups.");
		}
		cursor = inPos;
		currentElementContainer = groupOfMyElements.get(cursor);
		return cursor;
	}

	private ElementContainer nextGroup() {

		cursor++;

		if (cursor >= groupOfMyElements.size()) {
			return null;
		}

		currentElementContainer = groupOfMyElements.get(cursor);
		return currentElementContainer;
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
		boolean ret = true;
		for (var ec : groupOfMyElements) {
			ret &= ec.validate(inDErr);
		}

		return ret;

	}

	/**
	 * removes empty trailing data elements and returns the number of used
	 * dataelements
	 *
	 * @return int
	 */

	@Override
	public int trim() {
		int ret = 0;
		for (var ec : groupOfMyElements) {
			ret += ec.trim();
		}
		return ret;
	}

	/**
	 * routine to ask if it uses a dataelement
	 *
	 * @return boolen true the segment id is part of this group
	 * @param inTag   String id
	 * @param startAt int position to start at
	 */
	@Override
	public int doIUseThisXMLElement(String inTag, int startAt) {
		return currentElementContainer.doIUseThisXMLElement(inTag, startAt);

	}

	/**
	 * sets the default value for the data elements <br>
	 * Not part of Basic Package
	 */
	@Override
	public void useDefault() {
		currentElementContainer.useDefault();
	}

	/**
	 * owning object
	 */

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
	 * the toString method
	 */
	@Override
	public String toString() {
		return "composite  id:" + getID() + " name:" + getName() + " seq: "
				+ getPosition();

	}

	@Override
	public boolean IAmACompositeElement() {

		return true;
	}

	@Override
	public boolean IAmADataElement() {

		return false;
	}

	@Override
	public Element getElement(String ID) throws OBOEException {

		return currentElementContainer.getElement(ID);
	}

	@Override
	public Element getElement(int inPosition) throws OBOEException {

		return currentElementContainer.getElement(inPosition);
	}

	@Override
	public int getContainerSize() {
		return currentElementContainer.getContainerSize();
	}

	@Override
	public Element buildElement(int pos) {
		return currentElementContainer.buildElement(pos);
	}

	@Override
	public Element buildElement(String ID) {

		return currentElementContainer.buildElement(ID);
	}

	@Override
	public Element buildElement(String ID, int offset) {

		return currentElementContainer.buildElement(ID, offset);
	}

	@Override
	public String getDataElementValue(String ID) throws OBOEException {

		return currentElementContainer.getDataElementValue(ID);
	}

	@Override
	public String getDataElementValue(int pos) throws OBOEException {
		return currentElementContainer.getDataElementValue(pos);
	}

	@Override
	public Element setDataElementValue(String ID, String inValue)
			throws OBOEException {
		return currentElementContainer.setDataElementValue(ID, inValue);
	}

	@Override
	public Element setDataElementValue(String ID, int offset, String inValue)
			throws OBOEException {
		return currentElementContainer.setDataElementValue(ID, offset, inValue);
	}

	@Override
	public Element setDataElementValue(int pos, String inValue)
			throws OBOEException {

		return currentElementContainer.setDataElementValue(pos, inValue);
	}

	@Override
	public DataElement getDataElementByName(String inName, int inoffset) {
		return currentElementContainer.getDataElementByName(inName, inoffset);
	}

	@Override
	public Element getElement(String inID, int inoffset) {
		return currentElementContainer.getElement(inID, inoffset);
	}

	@Override
	public boolean isDataElement(int inPos) {
		return currentElementContainer.isDataElement(inPos);
	}

	@Override
	public boolean isCompositeElement(int inPos) {
		return currentElementContainer.isCompositeElement(inPos);
	}

	@Override
	public boolean validate() {
		return currentElementContainer.validate();
	}

	@Override
	public Element getPrimaryIDDE() {
		return currentElementContainer.getPrimaryIDDE();
	}

	@Override
	public String validate(String inText) {
		// not used by this class
		return null;
	}

	@Override
	public String get() {

		return currentElementContainer.toString();
	}

	@Override
	public String getName() {
		return myTemplate.getName();
	}

	/**
	 * returns the Description
	 *
	 * @return String description
	 */
	@Override
	public String getDescription() {
		return myTemplate.getDescription();
	}

	/**
	 * returns the occurs value
	 *
	 * @return int
	 */
	@Override
	public int getOccurs() {
		return myTemplate.getOccurs();

	}

	/**
	 * returns the used indicator
	 *
	 * @return boolean indicator
	 */
	@Override
	public boolean isUsed() {
		return myTemplate.isUsed();

	}

	/**
	 * gets the required indicator
	 *
	 * @return char the required indicator
	 *
	 */
	@Override
	public int getRequired() {
		return myTemplate.getRequired();
	}

	@Override
	public boolean isRequired() {
		return myTemplate.isRequired();
	}

	@Override
	public String getID() {
		return myTemplate.getID();
	}

	@Override
	public Integer getPosition() {
		return myTemplate.getPosition();
	}

	@Override
	public ContainerType getContainerType() {

		return null;
	}

	public void createNewGroup() {
		currentElementContainer = new ElementContainer(getTemplate(), this);
		groupOfMyElements.add(currentElementContainer);
		cursor++;

	}

}
