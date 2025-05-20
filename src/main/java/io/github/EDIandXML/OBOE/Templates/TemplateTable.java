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

import java.lang.reflect.Method;

import org.w3c.dom.Node;

import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.ContainerType;
import io.github.EDIandXML.OBOE.Containers.MetaTemplateContainer;
import io.github.EDIandXML.OBOE.Containers.Table;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;

/**
 * class for Template Tables a general class for the transaction set's
 * heading detail and summary.
 *
 * <p>
 * OBOE - Open Business Objects for EDI
 * <p>
 * 
 * An EDI and XML Translator Written In Java <br>
 *
 * 
 */
public class TemplateTable extends MetaTemplateContainer
		implements ITemplate, IContainedObject {
	/**
	 * String XML tag
	 */
	protected String shortName = "";

	/**
	 * constructor takes no parameters
	 */
	public TemplateTable() {
		super();
		setShortName("Unknown");
	}

	/**
	 * Constructor
	 *
	 * @param shortName   String XML tag
	 * @param inParent owning Object
	 */
	public TemplateTable(String shortName, IContainedObject inParent) {
		super();
		setShortName(shortName);
		setParent(inParent);
	}

	/**
	 * tests if the node is part of this table
	 *
	 * @return boolean true it is part of this table
	 * @param node DOM node of transaction data
	 * @exception OBOEException thrown when the transaction id string is
	 *                          incorrect
	 * @exception OBOEException thrown when an unknown segment id string is
	 *                          found
	 */
	public boolean doYouWantThisNode(Node node) throws OBOEException {

		for (var conElements : theContainer) {
			if (conElements.getContainerType() != ContainerType.Segment) {
				throw new OBOEException(
						"unexpected type " + conElements.getContainerType());
			}
			if (node.getNodeName().equals(conElements.getID())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * returns the table id, since there are no tables id it returns a
	 * zero-length string
	 *
	 * @return String
	 */
	@Override
	public String getID() {
		return shortName;
	}

	/**
	 * sets the xml tag field
	 *
	 * @param inShortName String tag
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
		return shortName;
	}

	IContainedObject parent = null;

	/**
	 * sets parent attribute
	 *
	 * @param inParent templateContainer
	 */
	@Override
	public void setParent(IContainedObject inParent) {
		parent = inParent;
	}

	/**
	 * gets parent attribute
	 *
	 * @return templateContainer
	 */
	@Override
	public IContainedObject getParent() {
		return parent;
	}

	/** method used to validate with */
	private Method validatingMethod = null;

	/**
	 * gets the validating Method as specified in message description
	 *
	 * @return String
	 */

	public String getValidatingMethod() {
		if (validatingMethod == null) {
			return null;
		}
		return validatingMethod.getDeclaringClass().getName() + "."
				+ validatingMethod.getName();
	}

	/**
	 * sets the validating method as specified in message description
	 *
	 * @param inValidatingMethod
	 */

	public void setValidatingMethod(Method inValidatingMethod) {
		validatingMethod = inValidatingMethod;
	}

	/**
	 * runs the validating class as specified in message description
	 *
	 * @param inTable
	 * @param inDocErrs document errors
	 * @return boolean
	 * @exception OBOEException with message from caught exception
	 */

	public boolean runValidatingMethod(Table inTable, DocumentErrors inDocErrs)
			throws OBOEException {
		if (validatingMethod == null) {
			return true;
		}

		Object objArray[] = new Object[2];
		objArray[0] = inTable;
		objArray[1] = inDocErrs;
		Boolean b;
		try {
			b = (Boolean) validatingMethod.invoke(null, objArray);
		} catch (Exception e1) {
			throw new OBOEException(e1.getMessage());
		}

		return b.booleanValue();
	}

	@Override
	public ContainerType getContainerType() {
		return ContainerType.Table;
	}

}
