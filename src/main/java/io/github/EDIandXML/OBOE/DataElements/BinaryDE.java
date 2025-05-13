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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for all Data Elements defined as character and alphanumeric
 *
 * OBOE - Open Business Objects for EDI
 * 
 */

public class BinaryDE extends DataElement {

	public static void main(String args[]) {
		BinaryDE bde = new BinaryDE(null, null);
		bde.getBytes();

	}

	private byte[] value;

	/**
	 * constructs from its template
	 *
	 * @param inTDE    TemplateDE
	 * @param inParent owning Object
	 */
	public BinaryDE(TemplateDataElement inTDE, IContainedObject inParent) {
		super(inTDE, inParent);
	}

	/**
	 * sets the value for the Data Element
	 *
	 * @param inValue String value
	 *
	 */
	public void setValue(String inValue) {
		value = inValue.getBytes();
	}

	/**
	 * sets the next but not implemented
	 *
	 * @param inValue String value
	 */
	@Override
	public void setNext(String inValue) {
		set(inValue);
	}

	/**
	 * sets the fields contents
	 *
	 * @param inValue byte array contents
	 */
	@Override
	public void set(String inValue) {
		value = inValue.getBytes();
	}

	/**
	 * gets the current length for the Data Element
	 *
	 * @return int returns length of set value, can have a null exception if
	 *         value is not set.
	 *
	 */
	@Override
	public int getLength() {
		if (value == null) {
			return 0;
		}
		return value.length;
	}

	/**
	 * sets the fields contents
	 *
	 * @param inValue byte array contents
	 */
	@Override
	public void set(byte inValue[]) {
		value = inValue.clone();
	}

	/**
	 * returns the value for the Data Element
	 *
	 * @return String
	 */

	@Override
	public String get() {
		if (value == null) {
			return null;
		}
		if (value.length == 0) {
			return "";
		}
		return new String(value);
	}

	/**
	 * returns the value for the Binary Data Element
	 *
	 * @return byte array
	 */

	public byte[] getBytes() {
		return value.clone();
	}

	/**
	 * formats text of data element <br>
	 * Description of DataElement is defined in the class <br>
	 * value is the current value set in the object
	 *
	 * @param format int format type x12, EDIFACT...
	 * @return String of formatted text
	 */

	@Override
	public String getFormattedText(Format format) {
		StringBuilder sb = new StringBuilder();
		String got = "";
		if (getBytes() != null) {
			got = new String(getBytes());
		}

		switch (format) {
		case CSV_FORMAT:
			sb.append("Binary DE," + getID() + ",\"" + getName() + "," + got
					+ '"' + Util.lineFeed);
			break;
		case XML_FORMAT:
			sb.append('<' + getShortName() + '>' + Util.normalize(got) + "</"
					+ getShortName() + ">" + Util.lineFeed);
			break;
		case VALID_XML_FORMAT:
		case VALID_XML_FORMAT_WITH_POSITION:
			sb.append("<element code=\"" + getID() + '"');
			sb.append(" name=\"" + getName() + '"');
			if (format == Format.VALID_XML_FORMAT_WITH_POSITION) {
				sb.append(" docPosition=\"" + this.getPosition() + '"');
			}
			sb.append('>');
			sb.append(" <value>" + Util.normalize(got) + "</value></element>"
					+ Util.lineFeed);
			break;

		case X12_FORMAT:
		case EDIFACT_FORMAT:
		case TRADACOMS_FORMAT:
		case PREBUILD_FORMAT:
			return got;
		default:
			sb.append('\t' + getName() + ": " + got + Util.lineFeed); //$NON-NLS-1$
			break;
		}
		return sb.toString();
	}

	/**
	 * returns error responses of contents
	 *
	 * @param inText String text
	 * @return String, null if no error
	 */
	@Override
	public String validate(String inText) {

		Pattern pat = Pattern.compile("^\\p{XDigit}*$");

		Matcher mat = pat.matcher(inText);
		if (mat.find() == false) {
			return "BINary field: " + getName() + " at position "
					+ getPosition() + " does not contain a valid HEX string.";

		}
		return null;
	}

	/**
	 * sets error in DocumentErrors
	 *
	 * @param inDErr DocumentErrors object
	 * @return boolean false = error.
	 */
	@Override
	public boolean validate(DocumentErrors inDErr) {
		if (isUsed() == false) {
			if ((get() != null) && (get().length() > 0)) {
				inDErr.addError(0, getID(),
						"BIN field is not used, see " + getName()
								+ " at position " + getPosition(),
						getParent(), "10", this,
						DocumentErrors.ERROR_TYPE.Requirement);
				return false;
			} else {
				return true;
			}
		}

		Pattern pat = Pattern.compile("^(0|1|2|3|4|5|6|7|8|9|A|B|C|D|E|F)*$");

		Matcher mat = pat.matcher(get());
		if (mat.find() == false) {
			inDErr.addError(0, getID(),
					"binary field" + getName() + " at position " + getPosition()
							+ " does not contain a valid HEX string.",
					getParent(), "10", this,
					DocumentErrors.ERROR_TYPE.Requirement);
			return false;
		}

		if (myTemplate != null) {
			return ((TemplateDataElement) myTemplate).runValidatingMethod(this,
					inDErr);
		}

		return true;
	}

	/**
	 * sets the default data in the dataelement <br>
	 * Not part of Basic Package
	 */

	@Override
	public void useDefault() {
		if (myTemplate != null) {
			value = ((TemplateDataElement) myTemplate).getDefault().getBytes();
		} else {
			char set[] = new char[getMinLength()];

			for (int pos = 0; pos < getMinLength(); pos++) {
				set[pos] = ' ';
			}
			value = new String(set).getBytes();
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * repeat
	 */
	@Override
	public String get(int inPos) {
		if (inPos > 1) {
			return null;
		}
		return get();
	}

	/*
	 * (non-Javadoc)
	 *
	 */
	@Override
	public int getRepeatCount() {
		if (value == null) {
			return 0;
		}
		return 1;
	}

}
