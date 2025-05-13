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

package io.github.EDIandXML.OBOE.Containers;

import java.io.IOException;
import java.io.Writer;

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Templates.TemplateTable;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for Tables a general class for the transaction set's heading
 * detail and summary.
 *
 */
public class Table extends MetaContainer implements IContainedObject {
	/**
	 * String XML tag
	 */
	protected String shortName = "";

	/**
	 * constructor
	 *
	 * @param inTemplateTable
	 * @param inParent        owning Object
	 */
	public Table(TemplateTable inTemplateTable, IContainedObject inParent) {
		super(inTemplateTable);

		setShortName(inTemplateTable.getShortName());
		setParent(inParent);
	}

	/**
	 * returns a preformatted string
	 *
	 * @return String formatted text
	 * @param format int - x12, EDIFACT...
	 */

	public String getFormattedText(Format format) {

		StringBuilder sbFormattedText = new StringBuilder();

		if (format == Format.CSV_FORMAT) {
			sbFormattedText.append("Table," + getShortName() + Util.lineFeed);
		}
		if (format == Format.XML_FORMAT) {
			sbFormattedText.append("<" + getShortName() + ">" + Util.lineFeed);
		}
		if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			sbFormattedText.append("<table section=\"" + getShortName() + "\">"
					+ Util.lineFeed);
		}

		getContainerFormattedText(sbFormattedText, format);

		if (format == Format.XML_FORMAT) {
			sbFormattedText.append("</" + getShortName() + ">" + Util.lineFeed);
		}
		if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			sbFormattedText.append("</table>" + Util.lineFeed);
		}

		return new String(sbFormattedText);

	}

	/**
	 * writes a preformatted string
	 *
	 * @param inWriter writer object
	 * @param format   int - x12, EDIFACT...
	 */

	public void writeFormattedText(Writer inWriter, Format format)
			throws IOException {

		if (format == Format.CSV_FORMAT) {
			inWriter.write("Table," + getShortName() + Util.lineFeed);
		}
		if (format == Format.XML_FORMAT) {
			inWriter.write("<" + getShortName() + ">" + Util.lineFeed);
		}
		if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			inWriter.write("<table section=\"" + getShortName() + "\">"
					+ Util.lineFeed);
		}

		writeContainerFormattedText(inWriter, format);

		if (format == Format.XML_FORMAT) {
			inWriter.write("</" + getShortName() + ">" + Util.lineFeed);
		}
		if ((format == Format.VALID_XML_FORMAT)
				|| (format == Format.VALID_XML_FORMAT_WITH_POSITION)) {
			inWriter.write("</table>" + Util.lineFeed);
		}

		inWriter.flush();

	}

	/**
	 * returns the table id, since there are no table ids method returns a
	 * zero-length string
	 *
	 * @return String
	 */
	@Override
	public String getID() {
		return getShortName();
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

	/**
	 * validates <br>
	 * doesn't throw exception but places error message in DocumentErrors
	 * object
	 */

	@Override
	public void validate(DocumentErrors inDErr) {
		testMissing(inDErr);
		validateTheContainer(inDErr);

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

	@Override
	public String toString() {

		return "Table: " + getShortName();
	}

	@Override
	public ContainerType getContainerType() {
		return ContainerType.Table;
	}

	@Override
	public int trim() {

		return trimTheContainer();
	}

}
