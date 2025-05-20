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
import io.github.EDIandXML.OBOE.DataElements.Element;
import io.github.EDIandXML.OBOE.DataElements.IDListProcessor;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for Template Data Elements <br>
 * template de's are dynamic definitions for de's
 * <p>
 * OBOE - Open Business Objects for EDI An EDI and XML Translator
 *
 *
 * 
 */

public class TemplateDataElement extends TemplateElement {

	/**
	 * template de are not sub classed so store their type here
	 */

	/**
	 * minimum and maximum lengths allowed
	 */
	protected int minLength, maxLength;

	/**
	 * id list if available
	 */
	private IDListProcessor idList = null;

	private String loadFromConstant = null;
	private String loadFromProperty = null;
	private String loadFromClassMethod = null;

	static Logger logr = LogManager.getLogger(TemplateDataElement.class);

	/**
	 * constructs the Data Element type used for serialization
	 */

	public TemplateDataElement() {
	}

	/**
	 * constructs the Data Element type
	 *
	 * @param inID        String id
	 * @param inName      String name
	 * @param inPosition  int position within seg or comp
	 * @param inType      String de type
	 * @param inRequired  char required indicator
	 * @param inDesc      String description
	 * @param inMinLength int mimimum length
	 * @param inMaxLength int maximum length
	 * @param inShortName    String XML tag
	 * @param inIDList    IDListProcessor if available
	 * @param inParent    owning Object
	 * @param inOccurs    int
	 * @param inUsed      boolean
	 */
	public TemplateDataElement(String inID, String inName, int inPosition,
			String inType, char inRequired, String inDesc, int inMinLength,
			int inMaxLength, String inShortName, IDListProcessor inIDList,
			IContainedObject inParent, int inOccurs, boolean inUsed) {

		setID(inID);
		setName(inName);
		setPosition(inPosition);
		setType(inType);
		setRequired(inRequired);
		setDescription(inDesc);
		setMinLength(inMinLength);
		setMaxLength(inMaxLength);
		setShortName(inShortName);
		setIDList(inIDList);
		setParent(inParent);
		setOccurs(inOccurs);
		setUsed(inUsed);
	}

	/**
	 * sets the Data Element type
	 *
	 * @param inType String
	 *
	 */
	@Override
	public void setType(String inType) {
		type = inType;
	}

	/**
	 * gets the Data Element type
	 *
	 * @return String
	 *
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * sets the minimum length for the Data Element
	 *
	 * @param inMinLength int min length
	 */
	public void setMinLength(int inMinLength) {
		minLength = inMinLength;
	}

	/**
	 * sets the maximum length for the Data Element
	 *
	 * @param inMaxLength int max length
	 */
	public void setMaxLength(int inMaxLength) {
		maxLength = inMaxLength;
	}

	/**
	 * gets the minimum length for the Data Element ;
	 * 
	 * @return int
	 *
	 */
	public int getMinLength() {
		return minLength;
	}

	/**
	 * gets the maximum length for the Data Element
	 *
	 * @return int
	 *
	 */
	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * sets the idList Object
	 *
	 * @param inIdList IDListProcessor object
	 */

	public void setIDList(IDListProcessor inIdList) {
		idList = inIdList;
	}

	/**
	 * gets the idListProcessor Object
	 *
	 * @return IDListProcessor
	 */
	public IDListProcessor getIDList() {
		return idList;
	}

	/**
	 * returns error responses of contents
	 *
	 * @param inText String text
	 * @return String
	 */
	public String validate(String inText) {
		String testText = "";

		if (inText != null) {
			testText = inText;// ;.trim();
		}

		if (isUsed() == false) {
			if ((inText != null) || (testText.length() > 0)) {
				return "field at position " + getPosition() + " id=" + getID()
						+ " data element is not used.";
			}
		}

		if (isRequired()) {
			if ((inText == null) || (testText.length() == 0)) {
				return "field at position " + getPosition() + " id=" + getID()
						+ " value required.";
			}
		} else // not required
		if ((inText == null) || (testText.length() == 0)) {
			return null;
		}

		if (inText.length() < getMinLength()) {
			return "field at position " + getPosition() + " id=" + getID()
					+ " field value too short.";
		}

		if ((getType().equals("DT")) && (getMaxLength() == 6)
				&& (testText.length() == 8)) {
			; // let's not do this here
		} else if (testText.length() > getMaxLength()) {
			return "field at position " + getPosition() + " id=" + getID()
					+ " field value too long.";
		}

		for (int i = 0; i < testText.length(); i++) {
			char ch = testText.charAt(i);
			if (((ch >= 'A') && (ch <= 'Z')) || ((ch >= '0') && (ch <= '9'))
					|| (ch == '!') || (ch == '"') || (ch == '&') || (ch == '\'')
					|| (ch == '(') || (ch == ')') || (ch == '*') || (ch == '+')
					|| (ch == '\'') || (ch == '-') || (ch == '.') || (ch == '/')
					|| (ch == ':') || (ch == ';') || (ch == '?') || (ch == '=')
					|| (ch == ' ') || ((ch >= 'a') && (ch <= 'z'))
					|| (ch == '%') || (ch == '~') || (ch == '@') || (ch == '[')
					|| (ch == ']') || (ch == '_') || (ch == '{') || (ch == '}')
					|| (ch == '\\') || (ch == '|') || (ch == '<') || (ch == '>')
					|| (ch == '#') || (ch == '$')) {
				;
			} else {
				return "field at position " + getPosition() + " id=" + getID()
						+ " invalid character.";
			}
		}

		return null;
	}

	/**
	 * sets the load from constant attribute <br>
	 * part of Extended Edition package
	 */
	public void setLoadFromConstant(String inString) {
		loadFromConstant = inString;
		loadFromProperty = null;
		loadFromClassMethod = null;
	}

	/**
	 * sets the load from property attribute <br>
	 * part of Extended Edition package
	 */
	public void setLoadFromProperty(String inString) {
		loadFromConstant = null;
		loadFromProperty = inString;
		loadFromClassMethod = null;
	}

	/**
	 * sets the load from class&method attribute <br>
	 * part of Extended Edition package
	 */
	public void setLoadFromClassMethod(String inString) {
		loadFromConstant = null;
		loadFromProperty = null;
		loadFromClassMethod = inString;
	}

	/**
	 * ressets the load attributes <br>
	 * part of Extended Edition package
	 */
	public void resetDefault() {
		loadFromConstant = null;
		loadFromProperty = null;
		loadFromClassMethod = null;
	}

	/**
	 * returns character indicating where default load comes from <br>
	 * part of Extended Edition package
	 */
	public char defaultFromWhere() {
		if (loadFromConstant != null) {
			return 'C';
		}
		if (loadFromProperty != null) {
			return 'P';
		}
		if (loadFromClassMethod != null) {
			return 'M';
		}
		return ' ';
	}

	/**
	 * returns default load value <br>
	 * part of Extended Edition package
	 */
	public String getDefaultKey() {
		if (loadFromConstant != null) {
			return loadFromConstant;
		}
		if (loadFromProperty != null) {
			return loadFromProperty;
		}
		if (loadFromClassMethod != null) {
			return loadFromClassMethod;
		}
		return "?";
	}

	/**
	 * gets the default value for a data element <br>
	 * not part of Basic edition
	 *
	 * @return String value as ste
	 */
	public String getDefault() {

		if (loadFromConstant != null) {
			return loadFromConstant;
		}

		if (loadFromProperty != null) {
			try {
				return Util.getOBOEProperty(loadFromProperty);
			} catch (Exception e1) {
				;
			}
		}

		if (loadFromClassMethod != null) {
			int pos = loadFromClassMethod.lastIndexOf('.');
			if (pos <= 0) {
				logr.error("Can't find method " + loadFromClassMethod
						+ " for DE " + this.getID() + " in "
						+ this.getParent().getID());
				return null;
			}
			String classname = loadFromClassMethod.substring(0, pos);
			if (pos == loadFromClassMethod.length()) {
				return null;
			}
			String methodname = loadFromClassMethod.substring(pos + 1);
			try {
				Class<?> loadClass = Class.forName(classname);
				Class<?> parms[] = new Class[0];
				Method loadMethod = loadClass.getMethod(methodname, parms);
				if (loadMethod == null) {
					return null;
				}
				return (String) loadMethod.invoke(null, (Object[]) parms);

			} catch (Exception e1) {
				e1.printStackTrace();
				return null;
			}

		}
		if ((isRequired()) && (getMinLength() > 0)) {
			char set[] = new char[getMinLength()];
			for (int pos = 0; pos < getMinLength(); pos++) {
				set[pos] = getType().startsWith("N") ? '0' : ' ';
			}
			return new String(set);
		}

		return null;
	}

	@Override
	public String getEmptyData() {
		char cArray[] = new char[getMaxLength()];
		for (int i = 0; i < getMaxLength(); i++) {
			cArray[i] = '\0';
		}
		return new String(cArray);
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
	 * @param inDE      Datalement
	 * @param inDocErrs document errors
	 * @return boolean
	 * @exception OBOEException with message from caught exception
	 */

	public boolean runValidatingMethod(Element inDE, DocumentErrors inDocErrs)
			throws OBOEException {
		if (validatingMethod == null) {
			return true;
		}

		Object objArray[] = new Object[2];
		objArray[0] = inDE;
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
	public boolean IAmATemplateComposite() {
		return false;
	}

	@Override
	public boolean IAmATemplateDE() {
		return true;
	}

	@Override
	public ContainerType getContainerType() {
		return ContainerType.DataElement;
	}

	@Override
	public String toString() {
		return getType() + ";" + getID() + ";" + getName() + ";"
				+ getPosition();
	}

}
