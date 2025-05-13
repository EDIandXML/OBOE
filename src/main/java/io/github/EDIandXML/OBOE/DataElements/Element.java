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

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Templates.TemplateElement;

/**
 * class for all Elements
 *
 * <P>
 * OBOE - Open Business Objects for EDI An EDI and XML Translator
 *
 * 
 * 
 */

public abstract class Element implements IContainedObject {
	/**
	 * template representing this ELement
	 */
	public TemplateElement myTemplate;

	/**
	 * returns the Element Type
	 *
	 * @return String type
	 *
	 */

	public String getType() {
		return myTemplate.getType();
	} // is there a default type?

	/**
	 * gets the Element id
	 *
	 * @return String
	 *
	 */
	@Override
	public abstract String getID();

	/**
	 * gets the Element Name
	 *
	 * @return String edi Name
	 *
	 */

	public abstract String getName();

	/**
	 * gets the Element position
	 *
	 * @return int position with in the segment or composite
	 *
	 */

	public abstract Integer getPosition();

	/**
	 * gets the required indicator
	 *
	 * @return char the required indicator
	 *
	 */

	public abstract int getRequired();

	public abstract boolean isRequired();

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
	 * returns the Description for the Element
	 *
	 * @return String description
	 */
	public abstract String getDescription();

	/**
	 * returns the occurs value for Data Element
	 *
	 * @return int
	 */
	public abstract int getOccurs();

	/**
	 * returns the used indicator
	 *
	 * @return boolean indicator
	 */
	public abstract boolean isUsed();

	public abstract boolean IAmACompositeElement();

	public abstract boolean IAmADataElement();

	public abstract String get();

	public abstract String getFormattedText(Format format);

	/**
	 * returns error responses of contents
	 *
	 * @param inText String text
	 * @return String, null if no error
	 */
	public abstract String validate(String inText);

	/**
	 * sets error in DocumentErrors
	 *
	 * @param inDErr DocumentErrors object
	 */
	public abstract boolean validate(DocumentErrors inDErr);

	/**
	 * gets the template that built this object
	 */
	public TemplateElement getTemplate() {
		return myTemplate;
	}

	/**
	 * sets the default data in the dataelement <br>
	 * Not part of Basic Package
	 */

	public abstract void useDefault();

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

}
