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

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for all Data Elements defined as character and alphanumeric
 *
 * OBOE - Open Business Objects for EDI
 */

public class CharDE extends DataElement {

	protected String value[];

	protected int cursor = -1;

	/**
	 * constructs from its template
	 *
	 * @param inTDE    TemplateDE
	 * @param inParent owning Object
	 */
	public CharDE(TemplateDataElement inTDE, IContainedObject inParent) {
		super(inTDE, inParent);
		value = new String[inTDE.getOccurs()];
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
		int len = 0;
		for (int i = 0; i < getOccurs(); i++) {
			if (value[i] != null) {
				len += value[i].length();
			}
		}
		return len;
	}

	/**
	 * sets the fields contents, cursor set to zero
	 *
	 * @param inValue String contents
	 * @throws OBOEException
	 */
	@Override
	public void set(String inValue) {

		cursor = 0;
		value[cursor] = inValue;
	}

	/**
	 * sets the next field contents by moving cursor, cursor will wrap
	 * around
	 *
	 * @param inValue String contents
	 * @throws OBOEException
	 */
	@Override
	public void setNext(String inValue) {
		cursor++;
		if (cursor >= getOccurs()) {
			cursor = 0;
		}
		if (this.isRequired()) {
			for (int i = inValue.length(); i < getMinLength(); i++) {
				inValue += ' ';
			}
		}
		value[cursor] = inValue;
	}

	/**
	 * sets the fields contents, not formatted
	 *
	 * @param inValue byte array, converted to string and set(String) is
	 *                called
	 * @exception OBOEException inValue contains format characters if field
	 *                          defined with type of N#
	 *
	 */
	@Override
	public void set(byte inValue[]) throws OBOEException {
		set(new String(inValue));
	}

	/**
	 * returns the value for the Data Element
	 *
	 * @return String
	 */

	@Override
	public String get() {
		return get(0);
	}

	/**
	 * returns the value for the Data Element
	 *
	 * @param inPos int position in array
	 * @return String
	 */

	@Override
	public String get(int inPos) {

		if (value[inPos] == null) {
			return null;
		}
		if (value[inPos].length() == 0) {
			return value[inPos];
		}
		StringBuilder sb = new StringBuilder(value[inPos]);
		for (int i = value[inPos].length(); i < getMinLength(); i++) {
			sb.append(' ');
		}
		value[inPos] = sb.toString();
		return value[inPos];
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
		String got;
		int repeatCnt = -1;
		for (repeatCnt = value.length - 1; (repeatCnt > -1)
				&& (value[repeatCnt] == null); repeatCnt--) {
			;
		}
		for (int ii = 0; ii < value.length; ii++) {
			if (value[ii] == null) {
				break;
			}
			got = get(ii);
			if (got == null) {
				got = new String();
			}

			switch (format) {
			case CSV_FORMAT:
				sb.append("DE," + getID() + ",\"" + getName() + "\",\"" + got
						+ "\"" + Util.lineFeed);
				break;
			case XML_FORMAT:
				sb.append("<" + getShortName());
				sb.append(">" + Util.normalize(got) + "</" + getShortName()
						+ ">" + Util.lineFeed);
				break;
			case VALID_XML_FORMAT:
			case VALID_XML_FORMAT_WITH_POSITION:
				sb.append("<element code=\"" + getID() + "\"");
				sb.append(" name=\"" + getName() + "\"");
				if (format == Format.VALID_XML_FORMAT_WITH_POSITION) {
					sb.append(" docPosition=\"" + this.getPosition() + "\"");
				}
				sb.append(">");
				sb.append(" <value>" + Util.normalize(got)
						+ "</value></element>" + Util.lineFeed);
				break;

			case PREBUILD_FORMAT:

				sb.append(got);
				if ((getOccurs() > 1) && (ii < repeatCnt)) {
					sb.append(Envelope.PREBUILD_REPEAT_DELIMITER.charAt(0));
				}
				break;

			case X12_FORMAT:
				sb.append(got);
				if ((getOccurs() > 1) && (ii < repeatCnt)
						&& (Envelope.X12_REPEAT_DELIMITER
								.charAt(0) != '\u0000')) {
					sb.append(Envelope.X12_REPEAT_DELIMITER.charAt(0));
				}
				break;

			case EDIFACT_FORMAT:
				sb.append(got);
				if ((getOccurs() > 1) && (ii < repeatCnt)) {
					sb.append(Envelope.EDIFACT_REPEAT_DELIMITER.charAt(0));
				}
				break;

			case TRADACOMS_FORMAT:
				sb.append(got);
				if ((getOccurs() > 1) && (ii < repeatCnt)) {
					sb.append(Envelope.EDIFACT_REPEAT_DELIMITER.charAt(0));
				}
				break;

			default:
				sb.append("\t" + getName() + ": " + value[0] + Util.lineFeed);
				break;
			}
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

		return ((TemplateDataElement) myTemplate).validate(inText);

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
			if ((value[0] != null) && (value[0].length() > 0)) {
				inDErr.addError(0, getID(),
						"field is not used, see " + getName() + " at position "
								+ getPosition(),
						getParent(), "10", this,
						DocumentErrors.ERROR_TYPE.Integrity);
				return false;
			} else {
				return true;
			}
		}

		if (isRequired()) {
			if ((value[0] == null) || (value[0].length() == 0)) {
				inDErr.addError(0, getID(),
						"Value Required, see " + getName() + " at position "
								+ getPosition(),
						getParent(), "1", this,
						DocumentErrors.ERROR_TYPE.Integrity);
				return false;
			}
		} else // not required
		if ((value[0] == null) || (value[0].length() == 0)) {
			return true;
		}

		if (value[0].length() < getMinLength()) {
			inDErr.addError(getPosition(), getID(),
					"Data element value (" + value[0] + ") Too Short, see "
							+ getName() + " at position " + getPosition(),
					getParent(), "4", this,
					DocumentErrors.ERROR_TYPE.Integrity);
			return false;
		}

		if (value[0].length() > getMaxLength()) {
			inDErr.addError(getPosition(), getID(),
					"Data element value (" + value[0] + ") Too Long, see "
							+ getName() + " at position " + getPosition(),
					getParent(), "5", this,
					DocumentErrors.ERROR_TYPE.Integrity);
			return false;
		}

		int pos = Util.isValidForCharacterSet(value[0]);
		if (pos > -1) {
			inDErr.addError(getPosition(), getID(),
					"Invalid character data at index " + pos
							+ " for element with value (" + value[0] + ") "
							+ getName() + " at position " + getPosition(),
					getParent(), "5", this,
					DocumentErrors.ERROR_TYPE.Integrity);
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
			value[0] = ((TemplateDataElement) myTemplate).getDefault();
		} else {
			char set[] = new char[getMinLength()];

			for (int pos = 0; pos < getMinLength(); pos++) {
				set[pos] = ' ';
			}
			value[0] = new String(set);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 */
	@Override
	public int getRepeatCount() {
		for (int i = 0; i < value.length; i++) {
			if (value[i] == null) {
				return i;
			}
		}

		return value.length;
	}

}
