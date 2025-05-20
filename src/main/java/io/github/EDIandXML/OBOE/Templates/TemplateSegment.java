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
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.ElementRules;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.ContainerType;
import io.github.EDIandXML.OBOE.Containers.IContainer;
import io.github.EDIandXML.OBOE.Containers.MetaContainer;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.DataElements.IDListProcessor;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Tokenizers.ITokenizer;
import io.github.EDIandXML.OBOE.Tokenizers.Tokenizer;

/**
 * TemplateSegment holds preliminary segment structure
 * <p>
 * OBOE - Open Business Objects for EDI
 * <p>
 * 
 * An EDI and XML Translator Written In Java <br>
 *
 * 
 */
public class TemplateSegment
		implements ITemplate, IContainedObject, ITemplateElementContainer {

	/** log4j object */
	static Logger logr = LogManager.getLogger(TemplateSegment.class);

	/**
	 * segment id
	 */
	public String id;
	/**
	 * segment name
	 */
	protected String name;
	/**
	 * segment position within table or parent segment
	 */
	protected int position = 0;
	/**
	 * segment description
	 */
	protected String description;
	/**
	 * segment XML tag
	 */
	protected String shortName = "";
	/**
	 * segment how many times it can occur
	 */
	public int occurs;
	/**
	 * segment required indicator
	 */
	protected char required;

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
	 * @param used boolean
	 */
	public void setUsed(boolean used) {
		this.used = used;
	}

	protected boolean used = true;

	/**
	 * segment ArrayList of rules not used in Basic package
	 */
	private ArrayList<ElementRules> elementRulesList;

	public TemplateElementContainer myElementContainer;

	/**
	 * templateSegment constructor
	 */
	public TemplateSegment() {
	}

	/**
	 * templateSegment, there are two flavors of segments Templates and
	 * regular
	 * <P>
	 * templates are used to define a segment dynamically
	 * <p>
	 * and are used to build the static form of segments
	 * <p>
	 * contains template data elements and secondary segments
	 *
	 * @param inId          String id
	 * @param inName        String name
	 * @param inPosition    int position within seg or comp
	 * @param inDescription String description
	 * @param inOccurs      int for multiple occurring segments
	 * @param inRequired    char required indicator
	 * @param inShortName   String XML tag
	 * @param inUsed        boolean
	 * @param inParent      owning Object
	 */

	public TemplateSegment(String inId, String inName, int inPosition,
			String inDescription, int inOccurs, char inRequired,
			String inShortName, boolean inUsed, IContainer inParent) {
		setID(inId);
		setName(inName);
		setPosition(inPosition);
		setDescription(inDescription);
		myElementContainer = new TemplateElementContainer(this);
		setOccurs(inOccurs);
		setRequired(inRequired);
		setShortName(inShortName);

		elementRulesList = new ArrayList<ElementRules>();

		used = inUsed;
		setParent(inParent);
	}

	/**
	 * returns the number of templateDEs
	 *
	 * @return int
	 */
	@Override
	public int getContainerSize() {
		return myElementContainer.getContainerSize();
	}

	/**
	 * sets TemplateSegment id
	 *
	 * @param inID String id
	 */
	public void setID(String inID) {
		id = inID;
	}

	/**
	 * sets TemplateSegment name
	 *
	 * @param inName String name
	 */
	public void setName(String inName) {
		name = inName;
	}

	/**
	 * gets TemplateSegment id
	 *
	 * @return String
	 */
	@Override
	public String getID() {
		return id;
	}

	/**
	 * gets TemplateSegment name
	 *
	 * @return string name
	 */
	public String getName() {
		return name;
	}

	/**
	 * sets position id
	 *
	 * @param inPosition int position within table or parent segment
	 */
	public void setPosition(int inPosition) {
		position = inPosition;
	}

	/**
	 * gets position id
	 *
	 * @return position
	 */
	public Integer getPosition() {
		return position;
	}

	/**
	 * sets Description for the Segmemt
	 *
	 * @param inDesc String description
	 */
	public void setDescription(String inDesc) {
		description = inDesc;
	}

	/**
	 * returns the Description for the Segment
	 *
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * sets occurance value
	 *
	 * @param inOccurs int
	 */
	public void setOccurs(int inOccurs) {
		occurs = inOccurs;
	}

	/**
	 * gets occurance value
	 *
	 * @return int occurs
	 */
	public int getOccurs() {
		return occurs;
	}

	/**
	 * sets required value
	 *
	 * @param inRequired char
	 */
	public void setRequired(char inRequired) {
		required = inRequired;
	}

	/**
	 * gets required flag
	 *
	 * @return char required
	 */
	public char getRequired() {
		return required;
	}

	public boolean isRequired() {
		return getRequired() == 'M';
	}

	/**
	 * sets the shortName field
	 *
	 * @param inShortName String XML tag
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

	/**
	 * adds TemplateDE to container <br>
	 * checks for duplicate entry at position position
	 *
	 * @param inTemplateDE TemplateDE to add
	 * @exception OBOEException -Position position already filled
	 */
	@Override
	public void addElement(TemplateElement inTemplateElement) {
		myElementContainer.addElement(inTemplateElement);
	}

	/**
	 * tests if the element at a position is a TemplateDE
	 *
	 * @return boolean
	 * @param at int position <br>
	 *           <b>position is relative to 1.</b>
	 */
	@Override
	public boolean isTemplateDE(int at) {
		return myElementContainer.isTemplateDE(at);
	}

	/**
	 * tests if the element at a position is a TemplateComposite
	 *
	 * @return boolean
	 * @param at int position <br>
	 *           <b>position is relative to 1.</b>
	 */
	@Override
	public boolean isTemplateComposite(int at) {

		return myElementContainer.isTemplateComposite(at);
	}

	@Override
	public Collection<TemplateElement> getAllTemplateElementsValues() {

		return myElementContainer.getAllTemplateElementsValues();

	}

	/**
	 * 
	 * /** gets TemplateDE from TemplateDE container
	 *
	 * @return TemplateDE
	 * @param at int position <br>
	 *           <b>position is relative to 1.</b>
	 */
	@Override
	public TemplateElement getTemplateElement(int at) {
		return myElementContainer.getTemplateElement(at);
	}

	/**
	 * gets TemplateDE from TemplateDE container with the first ID, there
	 * can be multiple
	 * 
	 *
	 * @return TemplateDE
	 * @param id String id
	 */
	@Override
	public TemplateElement getTemplateElement(String id) {
		return myElementContainer.getTemplateElement(id);
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
	 * routine to ask if it uses a dataelement
	 *
	 * @return boolen true the segment id is part of this group
	 * @param inXML   String xml tag
	 * @param startAt int starting position
	 */
	@Override
	public int doYouUseThisXMLElement(String inXML, int startAt) {
		return myElementContainer.doYouUseThisXMLElement(inXML, startAt);
	}

	/**
	 * adds element rule to elementrule ArrayList <br>
	 * not part of Basic package
	 *
	 * @param inElementRule ElementRule
	 */
	public void addElementRule(ElementRules inElementRule) {
		elementRulesList.add(inElementRule);
	}

	/**
	 * returns entire elementrule ArrayList
	 *
	 * @return ArrayList of element rules <br>
	 *         or null in Basic Edition
	 */
	public ArrayList<ElementRules> getElementRules() {
		return elementRulesList;
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
	 * returns true if the template segment can prevalidate an incoming edi
	 * transaction segment. If yes then this incoming segment is the
	 * segment. <br>
	 * implemented for HIPAA
	 * <ul>
	 * prevalidation requirement
	 * <li>at least one mandatory ID field
	 * </ul>
	 *
	 * @return boolean
	 */

	@Override
	public boolean canYouPrevalidate() {
		return myElementContainer.canYouPrevalidate();

	}

	/**
	 * returns the idlist that allows for prevalidation used by
	 * OBOECodeGenerator
	 *
	 * @return IDListProcessor
	 */
	@Override
	public IDListProcessor getIDListThatPrevalidates() {
		return myElementContainer.getIDListThatPrevalidates();
	}

	/**
	 * looking into the tokenized string we ask the first idde field if the
	 * data in the same position is one of its codes
	 *
	 * @return boolean
	 */

	@Override
	public boolean isThisYou(ITokenizer inToken) {
		return myElementContainer.isThisYou(inToken);
	}

	/**
	 * ask the first idde field if the data in the same position is one of
	 * its codes
	 *
	 * @param primaryIDValue String to search on
	 * @return boolean
	 */

	@Override
	public boolean isThisYou(String primaryIDValue) {
		return myElementContainer.isThisYou(primaryIDValue);
	}

	/**
	 * asking template what parsing id field it doesn't like so that it
	 * would create a prevalidated segment.
	 *
	 */

	@Override
	public void whyNotYou(Tokenizer et, MetaContainer errContainer) {
		myElementContainer.whyNotYou(et, errContainer);

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
	 * @param inSeg     segment to run on
	 * @param inDocErrs document errors
	 * @return boolean
	 * @exception OBOEException with message from caught exception
	 */

	public boolean runValidatingMethod(Segment inSeg, DocumentErrors inDocErrs)
			throws OBOEException {

		if (validatingMethod == null) {
			return true;
		}

		Object objArray[] = new Object[2];
		objArray[0] = inSeg;
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
	public String toString() {
		return getID() + " " + getName() + " " + getOccurs() + " "
				+ getRequired() + " " + getPosition() + " "
				+ getParent().getID() + " ";
	}

	@Override
	public ContainerType getContainerType() {
		return ContainerType.Segment;
	}

	@Override
	public TreeMap<Integer, TemplateElement> getAllTemplateElements() {

		return myElementContainer.getAllTemplateElements();
	}

	public int getLastPosition() {
		return myElementContainer.templateDataElementList.lastEntry()
				.getValue().position;

	}

}
