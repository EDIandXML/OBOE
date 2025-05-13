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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for Data Elements defined as Time
 * 
 */
public class TimeDE extends DataElement implements IContainedObject {
	protected String value[];
	protected int cursor = -1;

	private Calendar calendar;

	/**
	 * constructs from its template
	 *
	 * @param inTDE    TemplateDE
	 * @param inParent owning Object
	 */
	public TimeDE(TemplateDataElement inTDE, IContainedObject inParent) {
		super(inTDE, inParent);
		value = new String[inTDE.getOccurs()];

	}

	private void testAndSetValue(int inTimeLength) {
		if (calendar == null) {
			calendar = Calendar.getInstance();
		}

		SimpleDateFormat sdf;
		if ((getMaxLength() == 4) || (inTimeLength == 4)) {
			sdf = new SimpleDateFormat("HHmm");
		} else if ((getMaxLength() == 6) || (inTimeLength == 6)) {
			sdf = new SimpleDateFormat("HHmmss");
		} else {
			sdf = new SimpleDateFormat("HHmmssSS");
		}

		value[cursor] = sdf.format(calendar.getTime());

	}

	/**
	 * returns the date stored in HHMMSSmmm format where
	 * <UL>
	 * <LI>HH is hour
	 * <LI>MM is minutes
	 * <LI>SS is seconds
	 * <LI>mmm is milliseconds
	 * </UL>
	 *
	 * @return String in format HHMMSSmmm
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

		cursor = inPos;

		testAndSetValue(value[inPos].length());
		return value[inPos].substring(0, getMinLength());
	}

	/**
	 * sets the value (sets cursor to zero) stored using an input string in
	 * the format HHMMSS where
	 * <UL>
	 * <LI>HH is hour
	 * <LI>MM is minutes
	 * <LI>SS is seconds,
	 * </UL>
	 *
	 * @param inTime String field in format HHMMSS
	 */
	@Override
	public void set(String inTime) {
		cursor = -1;
		setNext(inTime);
	}

	/**
	 * sets the value (by moving cursor, cursor will wrap around) stored
	 * using an input string in the format HHMMSS where
	 * <UL>
	 * <LI>HH is hour
	 * <LI>MM is minutes
	 * <LI>SS is seconds,
	 * </UL>
	 *
	 * @param inTime String field in format HHMMSS
	 */
	@Override
	public void setNext(String inTime) {
		cursor++;
		if (cursor >= getOccurs()) {
			cursor = 0;
		}

		if (inTime.length() == 0) {
			value[cursor] = "";
			return;
		}

		String vtest = validate(inTime);
		if ((vtest != null) && (vtest.length() > 0)) {
			throw new OBOEException(vtest);
		}

		testAndSetValue(inTime.length());
		calendar.set(Calendar.HOUR_OF_DAY,
				Integer.parseInt(inTime.substring(0, 2)));
		calendar.set(Calendar.MINUTE, Integer.parseInt(inTime.substring(2, 4)));
		if (inTime.length() < 6) {
			calendar.set(Calendar.SECOND, 0);
		}

		if (inTime.length() > 5) {
			calendar.set(Calendar.SECOND,
					Integer.parseInt(inTime.substring(4, 6)));
		}

		if (inTime.length() < 8) {
			calendar.set(Calendar.MILLISECOND, 0);
		} else {
			calendar.set(Calendar.MILLISECOND,
					Integer.parseInt(inTime.substring(6, 8)));
		}

		value[cursor] = inTime;
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
	 * formats text of data element <br>
	 * Short Description of DataElement is defined in the class <br>
	 * value is the current value set in the object
	 *
	 * @return String formatted string
	 * @param format int x12, EDIFACT...
	 */

	@Override
	public String getFormattedText(Format format) {

		SimpleDateFormat sdf;

		StringBuilder sb = new StringBuilder();
		String got;
		int repeatCnt = -1;
		for (repeatCnt = value.length - 1; (repeatCnt > -1)
				&& (value[repeatCnt] == null); repeatCnt--) {
			;
		}
		for (int ii = 0; ii < value.length; ii++) {
			if ((value[ii] == null) || (value[ii].length() == 0)) {
				break;
			}
			if ((getMaxLength() == 4) || (value[ii].length() == 4)) {
				sdf = new SimpleDateFormat("HHmm");
			} else if ((getMaxLength() == 6) || (value[ii].length() == 6)) {
				sdf = new SimpleDateFormat("HHmmss");
			} else {
				sdf = new SimpleDateFormat("HHmmssSS");
			}
			got = sdf.format(calendar.getTime());

			switch (format) {
			case CSV_FORMAT:
				sb.append("Time DE," + getID() + ",\"" + getName() + "\",\""
						+ sdf.format(calendar.getTime()) + "\""
						+ Util.lineFeed);
				break;
			case XML_FORMAT:
				sb.append("<" + getShortName());
				sb.append(">" + sdf.format(calendar.getTime()) + "</"
						+ getShortName() + ">" + Util.lineFeed);
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
					sb.append(Envelope.TRADACOMS_REPEAT_DELIMITER.charAt(0));
				}
				break;
			default:
				sb.append("\t" + getName() + ": "
						+ sdf.format(calendar.getTime()) + Util.lineFeed);
			}
		}
		return sb.toString();
	}

	/**
	 * returns error responses of contents
	 *
	 * @param inText String text
	 * @return String
	 */
	@Override
	public String validate(String inText) {
		String returnMessage = ((TemplateDataElement) myTemplate)
				.validate(inText);
		if (returnMessage != null) {
			return returnMessage;
		}

		if ((inText.length() == 4) || (inText.length() == 6)
				|| (inText.length() == 7) || (inText.length() == 8)) {
			;
		} else {
			return "Time field format must be HHMM (length 4) HHMMSS (length 6) or HHMMSSmm (length 7 or 8, mm - decimal seconds may be 1 or 2 digits)";
		}

		String dd;
		int i;
		for (i = 0; i < inText.length(); i++) {
			if (Character.isDigit(inText.charAt(i)) == false) {
				returnMessage = returnMessage + " invalid character at " + i;
			}
		}

		if ((returnMessage != null) && (returnMessage.length() > 0)) {
			return returnMessage;
		}

		dd = inText.substring(0, 2);
		int m = Integer.parseInt(dd);
		if (m > 23) {
			return "invalid hour " + dd;
		}

		dd = inText.substring(2, 4);
		m = Integer.parseInt(dd);
		if (m > 59) {
			return "invalid minute " + dd;
		}

		if (inText.length() > 5) {
			dd = inText.substring(4, 6);
			m = Integer.parseInt(dd);
			if (m > 59) {
				return "invalid second " + dd;
			}
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
						"value Required, see " + getName() + " at position "
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
