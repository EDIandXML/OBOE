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

package io.github.EDIandXML.OBOE.Templates;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Errors.OBOEException;

/**
 * class for Template Data Elements <br>
 * template de's are dynamic definitions for de's
 * <p>
 * OBOE - Open Business Objects for EDI An EDI and XML Translator
 *
 *
 * 
 */

public abstract class TemplateElement implements IContainedObject {

	protected String type = "";
	/**
	 * position within segment or composite
	 */
	protected Integer position;
	/**
	 * element id
	 */
	protected String id = "";
	/**
	 * element name
	 */
	protected String name = "";

	/**
	 * occurs some many times
	 */
	protected int occurs = 1;

	/**
	 * required indicator
	 */
	protected char required = ' ';
	/**
	 * referenceid as defined by standard
	 */
	protected String referenceId = "";

	protected String description;
	/**
	 * xml tag
	 */
	protected String shortName;

	/**
	 * returns used attribute
	 *
	 * @return boolean
	 */
	public boolean isUsed() {
		return used;
	}

	/**
	 * sets used attribute
	 *
	 * @param used boolean used indicator
	 */

	public void setUsed(boolean used) {
		this.used = used;
	}

	protected boolean used = true;

	static Logger logr = LogManager.getLogger(TemplateElement.class);

	/**
	 * constructs the Data Element type used for serialization
	 */

	public TemplateElement() {
	}

	/**
	 * sets the Data Element type
	 *
	 * @param inType String
	 *
	 */
	public void setType(String inType) {
		type = inType;
	}

	/**
	 * gets the Data Element type
	 *
	 * @return String
	 *
	 */
	public String getType() {
		return type;
	}

	/**
	 * sets the Data Element required
	 *
	 * @param inRequired char required indicator
	 */
	public void setRequired(char inRequired) {
		required = inRequired;
	}

	/**
	 * gets the Data Element required
	 *
	 * @return char
	 *
	 */
	public char getRequired() {
		return required;
	}

	/**
	 * sets the Data Element id
	 *
	 * @param inID String id
	 */
	public void setID(String inID) {
		id = inID;
	}

	/**
	 * gets the Data Element id
	 *
	 * @return String
	 *
	 */
	@Override
	public String getID() {
		return id;
	}

	/**
	 * sets the Data Element name
	 *
	 * @param inname String name
	 */
	public void setName(String inname) {
		name = inname;
	}

	/**
	 * gets the Data Element name
	 *
	 * @return String
	 *
	 */
	public String getName() {
		return name;
	}

	/**
	 * sets the Data Element position
	 *
	 * @param inPosition int position within seg or comp
	 * @exception - invalid position - # < 1
	 */
	public void setPosition(int inPosition) throws OBOEException {
		if (inPosition < 1) {
			throw new OBOEException(
					"Invalid position specified for " + getID());
		}
		position = inPosition;
	}

	/**
	 * gets the Data Element position
	 *
	 * @return position in segment or composite
	 *
	 */
	public Integer getPosition() {
		return position;
	}

	/**
	 * sets Description for the Data Element
	 *
	 * @param inDesc String description
	 */
	public void setDescription(String inDesc) {
		description = inDesc;
	}

	/**
	 * returns the Description for the Data Element
	 *
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 *
	 * @param inShortName String
	 */

	public void setShortName(String inShortName) {
		shortName = inShortName;
	}

	/**
	 * returns the xml tag field
	 *
	 * @return String tag value
	 */

	@Override
	public String getShortName() {
		if (shortName == null) {
			return getID();
		}
		return shortName;
	}

	/**
	 * sets the occurs value
	 *
	 * @param inOccurs
	 */
	public void setOccurs(int inOccurs) {
		occurs = inOccurs;
	}

	/**
	 * gets the occurs value
	 *
	 * @return int
	 */
	public int getOccurs() {
		return occurs;
	}

	protected IContainedObject parent;

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

	public boolean isRequired() {
		return getRequired() == 'M';
	}

	public abstract boolean IAmATemplateComposite();

	public abstract boolean IAmATemplateDE();

	public abstract String getEmptyData();

}
