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
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateLoop;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for Loops OBOE - Open Business Objects for EDI
 *
 */

public class Loop extends MetaContainer implements ITemplatedObject {

	/**
	 * owning object
	 */

	protected IContainedObject parent = null;

	/**
	 * create a Loop based on its template
	 *
	 * @param inTemplateLoop predefined Templatoop
	 * @param inParent       owning Object
	 * @throws OBOEException unknown Segment
	 */

	public Loop(TemplateLoop inTemplateLoop, IContainedObject inParent)
			throws OBOEException {
		super(inTemplateLoop);

		setParent(inParent);
	}

	/**
	 * gets the id
	 *
	 * @return String id
	 */

	@Override
	public String getID() {
		return getMyTemplate().getID();
	}

	/**
	 * gets the Segment Name
	 *
	 * @return String Name
	 */

	public String getName() {
		return ((TemplateLoop) getMyTemplate()).getName();
	}

	/**
	 * returns the occurs value
	 *
	 * @return int occurance value
	 *
	 */

	public int getOccurs() {
		return ((TemplateLoop) getMyTemplate()).getOccurs();
	}

	/**
	 * gets required flag
	 *
	 * @return char required
	 */
	public char getRequired() {
		return ((TemplateLoop) getMyTemplate()).getRequired();
	}

	/**
	 * returns the xml tag field
	 *
	 * @return String tag value
	 */

	@Override
	public String getShortName() {
		return getMyTemplate().getShortName();
	}

	/**
	 * returns the used indicator
	 *
	 * @return boolean
	 */
	public boolean isUsed() {
		return ((TemplateLoop) getMyTemplate()).isUsed();

	}

	/**
	 * returns the formatted text
	 *
	 * @param format int indicating x12, edificact...
	 * @return String
	 */

	public String getFormattedText(Format format) {

		StringBuilder sbFormattedText = new StringBuilder();

		switch (format) {
		case CSV_FORMAT:
			sbFormattedText.append("Loop," + getID() + ",\"" + getName() + "\""
					+ Util.lineFeed);
			break;
		case XML_FORMAT:
			sbFormattedText.append('<' + getShortName() + ">" + Util.lineFeed);
			break;
		case VALID_XML_FORMAT:
		case VALID_XML_FORMAT_WITH_POSITION:
			sbFormattedText.append("<loop code=\"" + getID() + "\"");
			sbFormattedText
					.append(" name=\"" + getName() + "\">" + Util.lineFeed);
			break;
		case PREBUILD_FORMAT:
			break;
		case X12_FORMAT:
			break;
		case EDIFACT_FORMAT:
			break;
		case TRADACOMS_FORMAT:
			break;
		default:
			sbFormattedText.append("Loop: " + getID() + Util.lineFeed);
		}

		getContainerFormattedText(sbFormattedText, format);

		switch (format) {
		case CSV_FORMAT:
			break;
		case XML_FORMAT:
			sbFormattedText.append("</" + getShortName() + ">" + Util.lineFeed);
			break;
		case VALID_XML_FORMAT:
		case VALID_XML_FORMAT_WITH_POSITION:

			sbFormattedText.append("</loop>" + Util.lineFeed);
			break;
		case X12_FORMAT:
		case EDIFACT_FORMAT:
		case TRADACOMS_FORMAT:
		default:
		}

		return new String(sbFormattedText);

	}

	public void writeFormattedText(Writer inWriter, Format format)
			throws IOException {

		switch (format) {
		case CSV_FORMAT:
			inWriter.write("Loop," + getID() + ",\"" + getName() + "\""
					+ Util.lineFeed);
			break;
		case XML_FORMAT:
			inWriter.write('<' + getShortName() + ">" + Util.lineFeed);
			break;
		case VALID_XML_FORMAT:
		case VALID_XML_FORMAT_WITH_POSITION:
			inWriter.write("<loop code=\"" + getID() + "\"");
			inWriter.write(" name=\"" + getName() + "\">" + Util.lineFeed);
			break;

		case PREBUILD_FORMAT:
		case X12_FORMAT:
		case EDIFACT_FORMAT:
		case TRADACOMS_FORMAT:
			break;
		default:
			inWriter.write("Loop: " + getID() + Util.lineFeed);
		}

		writeContainerFormattedText(inWriter, format);

		switch (format) {
		case CSV_FORMAT:
			break;
		case XML_FORMAT:
			inWriter.write("</" + getShortName() + ">" + Util.lineFeed);
			break;
		case VALID_XML_FORMAT:
		case VALID_XML_FORMAT_WITH_POSITION:

			inWriter.write("</loop>" + Util.lineFeed);
			break;
		case PREBUILD_FORMAT:
			break;
		case X12_FORMAT:
			break;
		case EDIFACT_FORMAT:
			break;
		case TRADACOMS_FORMAT:
			break;
		default:
		}

		inWriter.flush();

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

	@Override
	public void validate(DocumentErrors inDErr) {

		testMissing(inDErr);

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
	 * the toString method
	 */
	@Override
	public String toString() {
		return "loop  id:" + getID() + " name:" + getName();

	}

	@Override
	public ContainerType getContainerType() {
		return ContainerType.Loop;
	}

	@Override
	public int trim() {

		return trimTheContainer();
	}

}
