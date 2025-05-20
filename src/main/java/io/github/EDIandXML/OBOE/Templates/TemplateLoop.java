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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.ContainerType;
import io.github.EDIandXML.OBOE.Containers.Loop;
import io.github.EDIandXML.OBOE.Containers.MetaTemplateContainer;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Tokenizers.ITokenizer;
import io.github.EDIandXML.OBOE.Tokenizers.Tokenizer;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for Template Loops a general class for the segment loops.
 *
 * <p>
 * OBOE - Open Business Objects for EDI
 * <p>
 * 
 * An EDI and XML Translator Written In Java <br>
 *
 * 
 */
public class TemplateLoop extends MetaTemplateContainer
		implements ITemplate, IContainedObject {

	/**
	 * String id
	 */
	public String id = "";
	/**
	 * String name
	 */
	protected String name = "";
	/**
	 * how many times it can occur
	 */
	public int occurs;
	/**
	 * required indicator
	 */
	protected char required;
	/**
	 * String XML tag
	 */
	protected String shortName = "";

	static Logger logr = LogManager.getLogger(TemplateLoop.class);

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	protected boolean used = true;

	/**
	 * constructor takes no parameters
	 */
	public TemplateLoop() {
		super();
		setID("");
		setShortName("Unknown");
	}

	/**
	 * Constructor
	 *
	 * @param inID       loop id
	 * @param inName     loop name
	 * @param inOccurs   int occursance count
	 * @param inRequired char required indicator
	 * @param shortName     String XML tag
	 * @param inUsed     boolean
	 * @param inParent   IContainedObject parent container
	 */
	public TemplateLoop(String inID, String inName, int inOccurs,
			char inRequired, String shortName, boolean inUsed,
			IContainedObject inParent) {
		super();
		setID(inID);
		setName(inName);
		setOccurs(inOccurs);
		setRequired(inRequired);
		setShortName(shortName);
		used = inUsed;
		this.setParent(inParent);
	}

	/**
	 * sets the id
	 *
	 * @param inID String id
	 */

	public void setID(String inID) {
		id = inID;
	}

	/**
	 * returns the Loop id
	 *
	 * @return String
	 */
	@Override
	public String getID() {
		return id;
	}

	/**
	 * sets the name
	 *
	 * @param inName String name
	 */

	public void setName(String inName) {
		name = inName;
	}

	/**
	 * returns the Loop name
	 *
	 * @return String
	 */
	public String getName() {
		return name;
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
	 * returns true if the template loop can prevalidate an incoming edi
	 * transaction segment. If yes then this segment is the first segment
	 * for this loop. It actually defines the loop. <br>
	 * implemented for HIPAA
	 * <ul>
	 * prevalidation requirement
	 * <li>the first segment contains at least one mandatory ID field
	 * </ul>
	 *
	 * @return boolean
	 */

	public boolean canYouPrevalidate() {
		if (Util.propertyFileIndicatesDoPrevalidate() == false) {
			return false;
		}
		return ((TemplateSegment) theContainer.get(0)).canYouPrevalidate();

	}

	/**
	 * looking into the tokenized string we ask the first segment's first
	 * idde field if the data in the same position is one of its codes
	 *
	 * @return boolean
	 */

	public boolean isThisYou(ITokenizer inToken) {

		return ((TemplateSegment) theContainer.get(0)).isThisYou(inToken);
	}

	/**
	 * asking template what parsing id field it doesn't like so that it
	 * would create a prevalidated segment.
	 *
	 * @return string
	 */

	public void whyNotYou(Tokenizer et) {
		((TemplateSegment) theContainer.get(0)).whyNotYou(et, null);
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
	 * @param inLoop
	 * @param inDocErrs document errors
	 * @return boolean
	 * @exception OBOEException with message from caught exception
	 */

	public boolean runValidatingMethod(Loop inLoop, DocumentErrors inDocErrs)
			throws OBOEException {
		if (validatingMethod == null) {
			return true;
		}

		Object objArray[] = new Object[2];
		objArray[0] = inLoop;
		objArray[1] = inDocErrs;
		Boolean b;
		try {
			b = (Boolean) validatingMethod.invoke(null, objArray);
		} catch (Exception e1) {
			logr.error(e1.getMessage(), e1);
			throw new OBOEException(e1.getMessage());
		}

		return b.booleanValue();
	}

	@Override
	public ContainerType getContainerType() {
		return ContainerType.Loop;
	}

}
