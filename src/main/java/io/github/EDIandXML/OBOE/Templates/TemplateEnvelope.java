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

import java.io.File;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.ContainerType;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.MetaTemplateContainer;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;

/**
 * class for Template Envelopes
 * <p>
 * OBOE - Open Business Objects for EDI
 * <p>
 * 
 * An EDI and XML Translator Written In Java <br>
 *
 * 
 */

public class TemplateEnvelope extends MetaTemplateContainer
		implements ITemplate {

	private String type;

	/** log4j object */

	static Logger logr = LogManager.getLogger(TemplateEnvelope.class);

	/** store idListFile datetime stamp */
	protected Hashtable<String, Long> idListFileList = new Hashtable<String, Long>();

	/**
	 * creates a TemplateEnvelope object
	 */
	public TemplateEnvelope() {
		super();

	}

	/**
	 * returns the template functional group used by this envelope
	 *
	 * @return templatefunctionalgroup
	 */
	@Override
	public TemplateFunctionalGroup getTemplateFunctionalGroup() {
		for (var conElements : theContainer) {
			if (conElements
					.getContainerType() == ContainerType.FunctionalGroup) {
				return (TemplateFunctionalGroup) conElements;
			}
		}
		return null;
	}

	/**
	 * sets the template functional group used by this envelope
	 *
	 * @param inTFG templatefunctionalgroup
	 */
	public void setTemplateFunctionalGroup(TemplateFunctionalGroup inTFG) {
		addContainer(inTFG);
	}

	protected IContainedObject parent = null;

	/**
	 * sets parent attribute
	 *
	 * @param inParent TemplateSegmentContainer
	 */
	@Override
	public void setParent(IContainedObject inParent) {
		throw new OBOEException("Envelopes don't have parents");
	}

	/**
	 * gets parent attribute
	 *
	 * @return TemplateSegmentContainer
	 */
	@Override
	public IContainedObject getParent() {
		throw new OBOEException("Envelopes don't have parents");
	}

	@Override
	public String getID() {
		return "template env";
	}

	@Override
	public String getShortName() {
		return "templateEnv";
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
	 * @param inEnv     envelope
	 * @param inDocErrs document errors
	 * @return boolean
	 * @exception OBOEException with message from caught exception
	 */

	public boolean runValidatingMethod(Envelope inEnv, DocumentErrors inDocErrs)
			throws OBOEException {
		if (validatingMethod == null) {
			return true;
		}

		Object objArray[] = new Object[2];
		objArray[0] = inEnv;
		objArray[1] = inDocErrs;
		Boolean b;
		try {
			b = (Boolean) validatingMethod.invoke(null, objArray);
		} catch (Exception e1) {
			throw new OBOEException(e1.getMessage());
		}

		return b.booleanValue();
	}

	/**
	 * returns type of envelope
	 *
	 * @return string
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param string
	 */
	public void setType(String string) {
		type = string;
	}

	public void addIDListFile(File inFile) {
		this.idListFileList.put(inFile.getPath(), inFile.lastModified());
	}

	public boolean checkIDListFiles() {
		int i;
		Enumeration<String> enumer = idListFileList.keys();
		for (i = 0; i < idListFileList.size(); i++) {
			String pth = enumer.nextElement();
			File f = new File(pth);
			if (f.exists() == false) {
				return false;
			}
			Long l = idListFileList.get(pth);
			if (l.longValue() != f.lastModified()) {
				return false;
			}

		}

		return true;
	}

	@Override
	public ContainerType getContainerType() {
		return ContainerType.Envelope;
	}

}
