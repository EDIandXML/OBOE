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

import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.ContainerType;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;

/**
 * class for all Data Elements
 *
 * 
 */

public abstract class DataElement extends Element implements IContainedObject {
	/**
	 * templateDE representing this DataELement
	 */

	public DataElement(TemplateDataElement templateDataELement,
			IContainedObject inParent) {
		myTemplate = templateDataELement;
		setParent(inParent);

	}

	/**
	 * gets the minimum length for the Data Element
	 *
	 * @return int the defined minimum length
	 *
	 */
	public int getMinLength() {
		return ((TemplateDataElement) myTemplate).getMinLength();
	}

	/**
	 * gets the maximum length for the Data Element
	 *
	 * @return int the defined maximum length
	 *
	 */
	public int getMaxLength() {
		return ((TemplateDataElement) myTemplate).getMaxLength();
	}

	/**
	 * returns the Description for the Data Element
	 *
	 * @return String description
	 */
	@Override
	public String getDescription() {
		return myTemplate.getDescription();
	}

	/**
	 * returns the occurs value for the Data Element
	 *
	 * @return int
	 */
	@Override
	public int getOccurs() {
		return myTemplate.getOccurs();

	}

	/**
	 * returns the number of data elements in a repeating data element.
	 *
	 * @return int
	 */

	public abstract int getRepeatCount();

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
	 * sets the fields contents
	 *
	 * @param inValue String contents
	 */
	public abstract void set(String inValue);

	/**
	 * sets the fields contents for multiple occurring elements
	 *
	 * @param inValue String contents
	 */
	public abstract void setNext(String inValue);

	/**
	 * sets the fields contents
	 *
	 * @param inValue byte array
	 */

	public abstract void set(byte inValue[]);

	/**
	 * returns the value for the Data Element
	 *
	 * @return String
	 */

	/**
	 * returns the value for the Data Element
	 *
	 * @return String
	 */
	@Override
	abstract public String get();

	/**
	 * returns the value for the Data Element
	 *
	 * @param inPos int position in array of repeating elements.
	 * @return String
	 */

	public abstract String get(int inPos);

	/**
	 * gets the current length for the Data Element
	 *
	 * @return int returns length of set value, can have a null exception if
	 *         value is not set.
	 *
	 */
	public abstract int getLength();

	@Override
	public boolean IAmACompositeElement() {

		return false;
	}

	@Override
	public boolean IAmADataElement() {

		return true;
	}

	/**
	 * returns the dataElement Type
	 *
	 * @return String type
	 *
	 */

	@Override
	public String getType() {
		return myTemplate.getType();
	}

	/**
	 * 
	 * gets the Data Element id**@return String edi id
	 **/

	@Override
	public String getID() {
		return myTemplate.getID();
	}

	/**
	 * gets the Data Element Name
	 *
	 * @return String edi Name
	 *
	 */
	@Override
	public String getName() {
		return myTemplate.getName();
	}

	/**
	 * gets the Data Element position
	 *
	 * @return int position with in the segment
	 *
	 */
	@Override
	public Integer getPosition() {
		return myTemplate.getPosition();
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
	public ContainerType getContainerType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return getType() + ";" + getID() + ";" + getName() + ";"
				+ getPosition();
	}
}
