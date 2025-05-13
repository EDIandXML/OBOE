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
import java.util.Collection;
import java.util.TreeMap;

import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.ContainerType;
import io.github.EDIandXML.OBOE.Containers.MetaContainer;
import io.github.EDIandXML.OBOE.DataElements.CompositeElement;
import io.github.EDIandXML.OBOE.DataElements.IDListProcessor;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Tokenizers.ITokenizer;
import io.github.EDIandXML.OBOE.Tokenizers.Tokenizer;

/**
 * Template Composite holds preliminary structure of Composites
 * <p>
 * OBOE - Open Business Objects for EDI
 * <p>
 * 
 * An EDI and XML Translator Written In Java <br>
 *
 * 
 */
public class TemplateCompositeElement extends TemplateElement
		implements ITemplateElementContainer {

	/**
	 * where its de's go.
	 */
	public TemplateElementContainer myElementContainer;

	/**
	 * Constructor
	 */
	public TemplateCompositeElement() {
	}

	/**
	 * TemplateComposite, there are two flavors of composites Templates and
	 * regular
	 * <P>
	 * templates are used to define a composite dynamically
	 * <p>
	 * and are used to build the static form of composites
	 * <p>
	 * contains template data element
	 *
	 * @param inId          String id of composite
	 * @param inName        composite name
	 * @param inRequired    required indicator
	 * @param inPosition    position within segment
	 * @param inDescription String description
	 * @param inShortName      String xml tag
	 * @param inParent      owning Object
	 * @param inOccurs      int
	 * @param inUsed        boolean
	 */

	public TemplateCompositeElement(String inId, String inName, char inRequired,
			int inPosition, String inDescription, String inShortName,
			IContainedObject inParent, int inOccurs, boolean inUsed) {
		setID(inId);
		setName(inName);
		setRequired(inRequired);
		setPosition(inPosition);
		setDescription(inDescription);
		myElementContainer = new TemplateElementContainer(this);
		setShortName(inShortName);
		setParent(inParent);
		setOccurs(inOccurs);
		setUsed(inUsed);
	}

	/**
	 * returns the number of elements in DEArrayList
	 *
	 * @return int count
	 */

	@Override
	public int getContainerSize() {
		return myElementContainer.getContainerSize();
	}

	/**
	 * adds TemplateDE to container <br>
	 * checks for duplicate entry at position position
	 *
	 * @param inTemplateDE TemplateDE to add
	 * @exception OBOEException -Position position already filled
	 */
	@Override
	public void addElement(TemplateElement inTemplateElement) {

		if (inTemplateElement.IAmATemplateComposite()) {
			throw new OBOEException("Can't add composite to composite");
		}

		if (inTemplateElement.getOccurs() != 1) {
			throw new OBOEException(
					"Data elements within Composite Elements can only have occur values of 1(one).");
		}
		myElementContainer.addElement(inTemplateElement);

	}

	/**
	 * gets TemplateDE from TemplateDE container by position
	 *
	 * @return TemplateDE at the specified position
	 * @param at int position of dataelement <br>
	 * 
	 */
	@Override
	public TemplateElement getTemplateElement(int at) {
		return myElementContainer.getTemplateElement(at);
	}

	/**
	 * gets TemplateDE from TemplateDE container by ID
	 *
	 * @param id of TemplateDE in container
	 * @return TemplateDE null if not found
	 */
	@Override
	public TemplateElement getTemplateElement(String id) {

		return myElementContainer.getTemplateElement(id);
	}

	/**
	 * tests if the element at a position is a TemplateDE
	 *
	 * @return boolean
	 * @param at int position <br>
	 */
	@Override
	public boolean isTemplateDE(int at) {
		return myElementContainer.isTemplateDE(at);
	}

	/**
	 * routine to ask if it uses a dataelement
	 *
	 * @return boolen true the segment id is part of this group
	 * @param inID String id
	 */
	@Override
	public int doYouUseThisElement(String inID, int startAt) {
		return myElementContainer.doYouUseThisElement(inID, startAt);
	}

	/**
	 * routine to ask if it uses a dataelement by its XML Tag
	 *
	 * @return int position , -1 not found
	 * @param inXML   tag of dataelement
	 * @param startAt int position
	 */
	@Override
	public int doYouUseThisXMLElement(String inXML, int startAt) {
		return myElementContainer.doYouUseThisXMLElement(inXML, startAt);
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
	 * @return parent container
	 */
	@Override
	public IContainedObject getParent() {
		return parent;
	}

	/**
	 * sets the occurs value
	 *
	 * @param inOccurs
	 */
	@Override
	public void setOccurs(int inOccurs) {
		occurs = inOccurs;
	}

	/**
	 * gets the occurs value
	 *
	 * @return int
	 */
	@Override
	public int getOccurs() {
		return occurs;
	}

	/**
	 * helper routine to get fields that are not built
	 *
	 * @return String
	 */
	@Override
	public String getEmptyData() {
		StringBuilder sb = new StringBuilder();
		TemplateDataElement tDE;
		for (int i = 1; i <= myElementContainer.getContainerSize(); i++) {
			tDE = (TemplateDataElement) getTemplateElement(i);
			sb.append(tDE.getEmptyData());
		}
		return sb.toString();
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
	 * @param inCDE     composite
	 * @param inDocErrs document errors
	 * @return boolean
	 * @exception OBOEException with message from caught exception
	 */

	public boolean runValidatingMethod(CompositeElement inCDE,
			DocumentErrors inDocErrs) throws OBOEException {
		if (validatingMethod == null) {
			return true;
		}

		Object objArray[] = new Object[2];
		objArray[0] = inCDE;
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
	public boolean isTemplateComposite(int at) {
		// composites can't contain composites
		return false;
	}

	@Override
	public boolean IAmATemplateComposite() {
		return true;
	}

	@Override
	public boolean IAmATemplateDE() {
		return false;
	}

	@Override
	public boolean isRequired() {
		return getRequired() == 'M';
	}

	@Override
	public boolean canYouPrevalidate() {
		return myElementContainer.canYouPrevalidate();
	}

	@Override
	public boolean isThisYou(ITokenizer inToken) {
		return myElementContainer.isThisYou(inToken);
	}

	@Override
	public boolean isThisYou(String primaryIDValue) {
		return myElementContainer.isThisYou(primaryIDValue);
	}

	@Override
	public void whyNotYou(Tokenizer et, MetaContainer errContainer) {
		myElementContainer.whyNotYou(et, errContainer);

	}

	@Override
	public IDListProcessor getIDListThatPrevalidates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<TemplateElement> getAllTemplateElementsValues() {
		return myElementContainer.getAllTemplateElementsValues();
	}

	@Override
	public ContainerType getContainerType() {
		return ContainerType.CompositeElement;
	}

	@Override
	public String toString() {
		return getType() + ";" + getID() + ";" + getName() + ";"
				+ getPosition();
	}

	@Override
	public TreeMap<Integer, TemplateElement> getAllTemplateElements() {
		return myElementContainer.getAllTemplateElements();
	}
}
