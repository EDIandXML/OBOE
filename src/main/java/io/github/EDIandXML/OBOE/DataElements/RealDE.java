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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * class for Data Elements defined as Real
 * 
 */

public class RealDE extends DataElement {

	protected String value[];
	protected int savePrecision[];
	protected int cursor = -1;

	/**
	 * constructs from its template
	 *
	 * @param inTDE    TemplateDE
	 * @param inParent owning Object
	 */
	public RealDE(TemplateDataElement inTDE, IContainedObject inParent) {
		super(inTDE, inParent);
		value = new String[inTDE.getOccurs()];
		savePrecision = new int[inTDE.getOccurs()];
		cursor++;
		if (cursor >= getOccurs()) {
			cursor = 0;
		}

	}

	/**
	 * sets the fields contents, cursor set to zero
	 *
	 * @param inValue String contents
	 * @throws OBOEException
	 */
	@Override
	public void set(String inValue) {
		cursor = -1;
		setNext(inValue);
	}

	/**
	 * sets the fields contents by moving cursor, cursor will wrap around;
	 * not formatted
	 *
	 * @param inValue String contents
	 */
	@Override
	public void setNext(String inValue) throws OBOEException {

		cursor++;
		if (cursor >= getOccurs()) {
			cursor = 0;
		}

		inValue = inValue.trim();

		if (inValue.length() == 0) {
			value[cursor] = "";
			return;
		}

		DecimalFormatSymbols dfs = new DecimalFormatSymbols();

		boolean foundDecimal = false;
		savePrecision[cursor] = -1;
		int lastNonZeroFraction = 0;

		for (int i = 0; i < inValue.length(); i++) {
			if (Character.isDigit(inValue.charAt(i))) {
				if (foundDecimal) {
					savePrecision[cursor]++;
					if (inValue.charAt(i) != '0') {
						lastNonZeroFraction = savePrecision[cursor];
					}
				}
				continue;
			}

			if (inValue.charAt(i) == '.') {
				if (foundDecimal) {
					throw new OBOEException("numeric parsing error " + inValue
							+ " contains multiple decimal points");
				}
				foundDecimal = true;
				savePrecision[cursor] = 0;
				continue;

			}
			if (inValue.charAt(i) == dfs.getGroupingSeparator()) {
				continue;
			}
			if (inValue.charAt(i) == dfs.getMinusSign()) {
				continue;
			}

			throw new OBOEException("numeric parsing error " + inValue);

		}

		DecimalFormat df = new DecimalFormat();

		try {
			double d = df.parse(inValue).doubleValue();
			String fmt = "#0";
			if (savePrecision[cursor] > 0) {
				if (Util.propertyFileIndicatesRealNumbersRetainPrecision() == true) {
					fmt += ".";
					for (int di = 0; di < savePrecision[cursor]; di++) {
						fmt += "0";
					}
				} else if (lastNonZeroFraction > 0) {
					fmt += ".";
					for (int di = 0; di < lastNonZeroFraction; di++) {
						fmt += "0";
					}
				}
			}
			NumberFormat fmtr = new DecimalFormat(fmt);
			value[cursor] = fmtr.format(d);
			if ((value[cursor].charAt(0) == '0')
					&& (value[cursor].length() > 1)) {
				if (dfs.getDecimalSeparator() == value[cursor].charAt(1)) {
					if (value[cursor].length() > getMinLength()) {
						value[cursor] = value[cursor].substring(1);
					}
				}
			}

		} catch (ParseException p1) {
			throw new OBOEException("java parse exception: " + p1.getMessage());
		}

		value[cursor] = get(cursor);

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

		int len = 0;
		for (int i = 0; i < getOccurs(); i++) {
			if (value[i] != null) {
				len += get(i).length();
			}
		}
		return len;
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
			return "";
		}
		StringBuilder sb = new StringBuilder(value[inPos]);

		int l = value[inPos].length();
		if ((l < getMinLength())
				|| ((l == getMinLength()) && (value[inPos].indexOf('.') > -1))) // ignore
		// decimal
		// point
		{
			if (value[inPos].indexOf('.') < 0) {
				sb.append('.');
			} else {
				l--;
			}
			for (int i = l; i < getMinLength(); i++) {
				sb.append('0');
			}

		}

		if (Util.propertyFileIndicatesRealNumbersRetainPrecision()) {

			int decPos = sb.indexOf(".");
			if ((decPos < 0) && (savePrecision[inPos] > -1)) {
				sb.append('.');
				decPos = sb.indexOf(".");
			}
			int currentPrecision = sb.length() - decPos - 1;
			if ((decPos == -1) && (savePrecision[inPos] > 0)) {
				sb.append('.');
				currentPrecision = 0;
			}
			while ((sb.length() < getMaxLength())
					&& (currentPrecision < savePrecision[inPos])) {
				sb.append("0");
				currentPrecision++;
			}

		}
		value[inPos] = sb.toString();
		return value[inPos];
	}

	/**
	 * gets the number of decimal positions
	 *
	 * @return int number of decimal positions
	 *
	 */

	public int getDecimalPositions() {
		return getMinLength();
	}

	/**
	 * builds a formatted String of the object
	 *
	 * @return String of formatted text
	 * @param format int x12, EDIFACT...
	 */
	@Override
	public String getFormattedText(Format format) {
		int i;
		String got;
		StringBuilder sb = new StringBuilder();

		int repeatCnt = -1;
		for (repeatCnt = value.length - 1; (repeatCnt > -1)
				&& (value[repeatCnt] == null); repeatCnt--) {
			;
		}

		for (i = 0; i < value.length; i++) {
			if ((value[i] == null) && (i > 0)) {
				break;
			}
			got = get(i);
			if (got == null) {
				got = "";
			}

			switch (format) {
			case PREBUILD_FORMAT:
				sb.append(got);
				if ((getOccurs() > 1) && (i < repeatCnt)) {
					sb.append(Envelope.PREBUILD_REPEAT_DELIMITER.charAt(0));
				}
				break;

			case X12_FORMAT:
				sb.append(got);
				if ((getOccurs() > 1) && (i < repeatCnt)
						&& (Envelope.X12_REPEAT_DELIMITER
								.charAt(0) != '\u0000')) {
					sb.append(Envelope.X12_REPEAT_DELIMITER.charAt(0));
				}
				break;

			case EDIFACT_FORMAT:
				got = get(i);
				sb.append(got);
				if ((getOccurs() > 1) && (i < repeatCnt)) {
					sb.append(Envelope.EDIFACT_REPEAT_DELIMITER.charAt(0));
				}
				break;
			case TRADACOMS_FORMAT:
				got = get(i);
				sb.append(got);
				if ((getOccurs() > 1) && (i < repeatCnt)) {
					sb.append(Envelope.TRADACOMS_REPEAT_DELIMITER.charAt(0));
				}
				break;

			case CSV_FORMAT:
				sb.append("DE," + getID() + ",\"" + getName() + "\",");
				sb.append(got);
				sb.append("\t" + getName() + ": ");
				sb.append("\"" + Util.lineFeed);
				break;

			case XML_FORMAT:
				sb.append("<" + getShortName());
				sb.append(">");
				sb.append(got);
				sb.append("</" + getShortName() + ">" + Util.lineFeed);
				break;

			case VALID_XML_FORMAT:
			case VALID_XML_FORMAT_WITH_POSITION:

				sb.append("<element code=\"" + getID() + "\"");
				sb.append(" name=\"" + getName() + "\"");
				if (format == Format.VALID_XML_FORMAT_WITH_POSITION) {
					sb.append(" docPosition=\"" + this.getPosition() + "\"");
				}

				sb.append(">");
				sb.append("<value>");
				sb.append(got);
				sb.append("</value>");
				sb.append("</element>" + Util.lineFeed);
				break;

			default:
				sb.append("\t" + getName() + ": ");
				sb.append(value[i]);
				sb.append(Util.lineFeed);
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
		if (returnMessage == null) {
			return returnMessage;
		}

		if ((inText == null) || (inText.trim().length() == 0)) {
			return null;
			// if it was required the super call above catches that
		}

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(getMinLength());
		nf.setMaximumFractionDigits(getMinLength());
		nf.setMinimumIntegerDigits(1);
		nf.setMaximumIntegerDigits(getMaxLength());
		try {
			Double.valueOf(inText.trim()).doubleValue();
		} catch (java.lang.NumberFormatException e1) {
			returnMessage = "Invalid Real Number Format";
		}

		return null;
	}

	/**
	 * sets error in DocumentErrors
	 *
	 * @param inDErr DocumentErrors object
	 * @return boolean true if it's okay
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
						"value[0] Required, see " + getName() + " at position "
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
