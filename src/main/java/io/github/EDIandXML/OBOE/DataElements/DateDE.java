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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for Data Elements defined as Date
 *
 * 
 */

public class DateDE extends DataElement implements IContainedObject {
	protected String value[];
	protected int cursor = -1;
	private Calendar calendar;

	private boolean setLengthIs6 = true;
	static Logger logr = LogManager.getLogger(DateDE.class);

	/**
	 * constructs from its template
	 *
	 * @param inTDE    TemplateDE
	 * @param inParent owning Object
	 */
	public DateDE(TemplateDataElement inTDE, IContainedObject inParent) {
		super(inTDE, inParent);
		value = new String[inTDE.getOccurs()];
	}

	/**
	 * returns the date stored in YYYYMMDD format where
	 * <UL>
	 * <LI>YY is year
	 * <LI>MM is month,
	 * <LI>DD is day of month
	 * </UL>
	 *
	 * @return String in format YYYYMMDD
	 */
	@Override
	public String get() {
		return get(0);
	}

	/**
	 * returns the value for the Data Element
	 *
	 * @param inPos int position in array of repeating elements.
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

		// testAndSetValue(inPos);
		return value[inPos];
	}

	private void testAndSetValue(int inPos) {
		if (calendar == null) {
			calendar = Calendar.getInstance();
		}

		SimpleDateFormat sdf;
		if (((getMinLength() == 6) && setLengthIs6) || (getMaxLength() == 6)) {
			sdf = new SimpleDateFormat("yyMMdd");
		} else {
			sdf = new SimpleDateFormat("yyyyMMdd");
		}

		value[inPos] = sdf.format(calendar.getTime());

	}

	/**
	 * sets the next field value at first field, cursor set to zero stored
	 * using an input string in the format YYYYMMDD where
	 *
	 * <UL>
	 * <LI>YY is year or YYYY is year.
	 * <LI>MM is month,
	 * <LI>DD is day of month
	 * </UL>
	 *
	 * @param inDate String date
	 */
	@Override
	public void set(String inDate) throws OBOEException {
		cursor = -1;
		setNext(inDate);
	}

	/**
	 * sets the next field value by moving cursor, cursor will wrap around
	 * stored using an input string in the format YYYYMMDD where
	 *
	 * <UL>
	 * <LI>YY is year or YYYY is year.
	 * <LI>MM is month,
	 * <LI>DD is day of month
	 * </UL>
	 *
	 * @param inDate String date
	 * @throws ParseException
	 */
	@Override
	public void setNext(String inDate) throws OBOEException {
		cursor++;
		if (cursor >= getOccurs()) {
			cursor = 0;
		}

		setLengthIs6 = (inDate.length() == 6);
		if (inDate.length() == 0) {
			value[cursor] = "";
			return;
		}

		if (inDate.length() == 10) {
			inDate = inDate.substring(0, 4) + inDate.substring(5, 7)
					+ inDate.substring(8, 10);

		}
		testAndSetValue(cursor);
		String vtest = validate(inDate);
		if ((vtest != null) && (vtest.length() > 0)) {
			value[cursor] = inDate;
			throw new OBOEException(vtest);
		}

		if (inDate.length() == 6) {
			if (Integer.parseInt(inDate.substring(0, 2)) < 50) {
				if (getMinLength() == 8) {
					logr.error("Date not Y2K compliant '20' will be prepended");
				}
				calendar.set(Calendar.YEAR,
						2000 + Integer.parseInt(inDate.substring(0, 2)));
			} else {
				if (getMinLength() == 8) {
					logr.error("Date not Y2K compliant '19' will be prepended");
				}
				calendar.set(Calendar.YEAR,
						1900 + Integer.parseInt(inDate.substring(0, 2)));
			}
			calendar.set(Calendar.MONTH,
					Integer.parseInt(inDate.substring(2, 4)) - 1);
			calendar.set(Calendar.DAY_OF_MONTH,
					Integer.parseInt(inDate.substring(4, 6)));
		} else if (inDate.length() == 8) {
			calendar.set(Calendar.YEAR,
					Integer.parseInt(inDate.substring(0, 4)));
			calendar.set(Calendar.MONTH,
					Integer.parseInt(inDate.substring(4, 6)) - 1);
			calendar.set(Calendar.DAY_OF_MONTH,
					Integer.parseInt(inDate.substring(6, 8)));
		}

		testAndSetValue(cursor);

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
	 * @param format int formatting type
	 * @return String formatted content
	 */

	@Override
	public String getFormattedText(Format format) {

		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

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
				got = "";
			}

			switch (format) {
			case CSV_FORMAT:
				sb.append("Date DE," + getID() + ",\"" + getName() + "\",\""
						+ sdf.format(calendar.getTime()) + "\""
						+ Util.lineFeed);
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
					sb.append(Envelope.TRADACOMS_REPEAT_DELIMITER.charAt(0));
				}
				break;
			default:
				if ((got != null) && (got.length() > 0)) {
					got = sdf.format(calendar.getTime());
				}
				sb.append("\t" + getName() + ": " + got + Util.lineFeed);
			}
		}
		return sb.toString();
	}

	/**
	 * returns error responses of contents
	 *
	 * @param inText String text
	 * @return String - null if no error
	 */
	@Override
	public String validate(String inText) {
		String returnMessage = ((TemplateDataElement) myTemplate)
				.validate(inText);
		if (returnMessage != null) {
			return returnMessage;
		}

		returnMessage = "";

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

		int offset = 2;

		if (inText.length() == 8) {
			dd = inText.substring(0, 4);
			i = Integer.parseInt(dd);
			if ((i < 1850) || (i > 2150)) {
				return "invalid year " + dd;
			}
			offset = 4;
		} else {
			dd = inText.substring(0, 2);
			i = Integer.parseInt(dd);
		}

		dd = inText.substring(offset, offset + 2);
		int m = Integer.parseInt(dd);
		if ((m < 1) || (m > 12)) {
			return "invalid month " + dd;
		}

		offset += 2;
		dd = inText.substring(offset, offset + 2);
		int d = Integer.parseInt(dd);
		if (d < 1) {
			return "invalid day " + dd;
		}

		if ((i % 4) == 0) {
			if (m == 2) {
				if (d > 29) {
					return "invalid day " + dd;
				}
			}
		} else {
			if (m == 2) {
				if (d > 28) {
					return "invalid day " + dd;
				}
			}
		}

		if ((m == 9) || (m == 4) || (m == 6) || (m == 11)) {
			if (d > 30) {
				return "invalid day " + dd;
			}
		} else if (d > 31) {
			return "invalid day " + dd;
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
