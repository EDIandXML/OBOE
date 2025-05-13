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

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.ContainerKey;
import io.github.EDIandXML.OBOE.Containers.ContainerType;
import io.github.EDIandXML.OBOE.Containers.MetaTemplateContainer;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;

/**
 * class for Template Transaction Sets
 * <p>
 * OBOE - Open Business Objects for EDI
 * <p>
 * 
 * An EDI and XML Translator Written In Java <br>
 *
 * 
 */

public class TemplateTransactionSet extends MetaTemplateContainer
		implements ITemplate, IContainedObject {

	/**
	 * Template TransactionSet format
	 */
	Format format = Format.UNDEFINED;
	/**
	 * TransactionSet id (840, ORDER...)
	 */
	String id = "";
	/**
	 * TransactionSet name
	 */
	String name = "";
	/**
	 * TransactionSet revision (3040, D99A...
	 */
	String revision = "";
	/**
	 * TransactionSet functional group
	 */
	String functionalGroup = "";
	/**
	 * TransactionSet description
	 */
	String shortDescription = "";
	/**
	 * TransactionSet XML tag
	 */
	String shortName = "";

	/** store idListFile datetime stamp */
	protected Hashtable<String, Long> idListFileList = new Hashtable<String, Long>();

	/**
	 * parent container
	 */
	protected IContainedObject parent = null;

	static Logger logr = LogManager.getLogger(TemplateTransactionSet.class);

	/**
	 * creates a Template transactionset object
	 */
	public TemplateTransactionSet() {
		super();
	}

	/**
	 * constructs a Template Transaction Set
	 *
	 * @param inFormat           format
	 * @param inId               String ts id
	 * @param inName             String name
	 * @param inRevision         String revision or version
	 * @param inFunctionalGroup  String functional group
	 * @param inShortDescription String short description
	 * @param inShortName           String xml tag
	 * @param inParent           owning Object
	 */

	public TemplateTransactionSet(Format inFormat, String inId, String inName,
			String inRevision, String inFunctionalGroup,
			String inShortDescription, String inShortName,
			IContainedObject inParent) {

		setFormat(inFormat);
		setID(inId);
		setName(inName);
		setRevision(inRevision);
		setFunctionalGroup(inFunctionalGroup);
		setShortDescription(inShortDescription);
		setShortName(inShortName);
		setParent(inParent);
	}

	/**
	 * sets format for the Transaction Set <br>
	 * XML_FORMAT = 1; <br>
	 * X12_FORMAT = 2; <br>
	 * EDIFACT_FORMAT = 3; <br>
	 * VALID_XML_FORMAT = 4;
	 *
	 * @param inFormat int format
	 */
	public void setFormat(Format inFormat) {
		if ((inFormat == Format.XML_FORMAT) || (inFormat == Format.X12_FORMAT)
				|| (inFormat == Format.EDIFACT_FORMAT)
				|| (inFormat == Format.VALID_XML_FORMAT)) {
			format = inFormat;
		} else {
			logr.error("Invalid transaction set format " + inFormat
					+ ".  Value set to -1");
		}
	}

	/**
	 * sets id for the Transaction Set
	 *
	 * @param inId String transation set id
	 */
	public void setID(String inId) {
		id = inId;
	}

	/**
	 * sets name for the Transaction Set
	 *
	 * @param inName String transaction set name
	 */
	public void setName(String inName) {
		name = inName;
	}

	/**
	 * sets Revision for the Transaction Set
	 *
	 * @param inRevision String revision or version
	 */
	public void setRevision(String inRevision) {
		revision = inRevision;
	}

	/**
	 * sets Function Group for the Transaction Set
	 *
	 * @param inFunctionalGroup String functional group
	 */
	public void setFunctionalGroup(String inFunctionalGroup) {
		functionalGroup = inFunctionalGroup;
	}

	/**
	 * sets Short Description for the Transaction Set
	 *
	 * @param inDesc String description
	 */
	public void setShortDescription(String inDesc) {
		shortDescription = inDesc;
	}

	/**
	 * sets header TemplateTable for the Transaction Set
	 *
	 * @param inTemplateTable TemplateTable
	 */
	public void addTemplateTable(TemplateTable inTemplateTable) {
		addContainer(inTemplateTable);
	}

	/**
	 * returns the Transaction Set format
	 *
	 * @return int
	 */

	public Format getFormat() {
		return format;
	}

	/**
	 * returns the Transaction Set id
	 *
	 * @return String
	 */

	@Override
	public String getID() {
		return id;
	}

	/**
	 * returns name for the Transaction Set
	 *
	 * @return String
	 *
	 */

	public String getName() {
		return name;
	}

	/**
	 * returns revision value for the Transaction Set
	 *
	 * @return String
	 *
	 */

	public String getRevision() {
		return revision;
	}

	/**
	 * returns functional group string
	 *
	 * @return String
	 *
	 */

	public String getFunctionalGroup() {
		return functionalGroup;
	}

	/**
	 * returns the Short Description for the Transaction Set
	 *
	 * @return String
	 */
	public String getShortDescription() {
		if ((shortDescription == null) || (shortDescription.length() == 0)) {
			return id;
		}
		return shortDescription;
	}

	/**
	 * returns header TemplateTable for the Transaction Set
	 *
	 * @return TemplateTable
	 *
	 */

	public TemplateTable getHeaderTemplateTable() {
		ContainerKey key = new ContainerKey(ContainerType.Table, "header");
		var gotwhere = keyContainer.get(key);
		if (gotwhere == null) {
			return null;
		}
		return (TemplateTable) theContainer.get(gotwhere);
	}

	/**
	 * returns detail TemplateTable for the Transaction Set
	 *
	 * @return TemplateTable
	 *
	 */

	public TemplateTable getDetailTemplateTable() {
		ContainerKey key = new ContainerKey(ContainerType.Table, "detail");
		var gotwhere = keyContainer.get(key);
		if (gotwhere == null) {
			return null;
		}
		return (TemplateTable) theContainer.get(gotwhere);

	}

	/**
	 * returns summary TemplateTable for the Transaction Set
	 *
	 * @return TemplateTable
	 *
	 */

	public TemplateTable getSummaryTemplateTable() {
		ContainerKey key = new ContainerKey(ContainerType.Table, "summary");
		var gotwhere = keyContainer.get(key);
		if (gotwhere == null) {
			return null;
		}
		return (TemplateTable) theContainer.get(gotwhere);

	}

	/**
	 * sets the xml tag field
	 *
	 * @param inShortName String xml tag id
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
	 * @param inTS      transaction set
	 * @param inDocErrs document errors
	 * @return boolean
	 * @exception OBOEException with message from caught exception
	 */

	public boolean runValidatingMethod(TransactionSet inTS,
			DocumentErrors inDocErrs) throws OBOEException {
		if (validatingMethod == null) {
			return true;
		}

		Object objArray[] = new Object[2];
		objArray[0] = inTS;
		objArray[1] = inDocErrs;
		Boolean b;
		try {
			b = (Boolean) validatingMethod.invoke(null, objArray);
		} catch (Exception e1) {
			throw new OBOEException(e1.getMessage());
		}

		return b.booleanValue();
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
		return ContainerType.TransactionSet;
	}

}
