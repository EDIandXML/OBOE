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

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

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
 * class for Data Elements defined as Numeric (Nx - where x is an
 * integer representing number of decimal places)
 *
 * 
 */

public class NumericDE extends DataElement implements IContainedObject {

	protected String value[];
	protected int cursor = -1;

	/**
	 * stores fraction of digits
	 */
	protected int fractionDigits = -1;

	/* useful for internationalization */
	protected static DecimalFormatSymbols dfs = new DecimalFormatSymbols();

	static Logger logr = LogManager.getLogger(NumericDE.class);

	/**
	 * constructs from its template
	 *
	 * @param inTDE    TemplateDE
	 * @param inParent owning Object
	 */
	public NumericDE(TemplateDataElement inTDE, IContainedObject inParent) {
		super(inTDE, inParent);
		value = new String[inTDE.getOccurs()];
		if (inTDE.getType().length() == 1) {
			fractionDigits = -1;
		} else {
			fractionDigits = Integer.parseInt(inTDE.getType().substring(1));
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
		cursor = 1;
		setNext(inValue);
	}

	String preformattedError = "";

	/**
	 * sets the fields contents by moving cursor, cursor will wrap around
	 *
	 * @param inValue String contents
	 * @exception OBOEException inValue contains format characters if field
	 *                          defined with type of N#
	 *
	 */
	@Override
	public void setNext(String inValue) throws OBOEException {

		if (inValue == null) {
			return;
		}

		if ((inValue.length() == 0) && (getRequired() != 'M')) {
			return;
		}

		cursor++;

		if (cursor == 0) {
			preformattedError = "";
		}

		if (inValue.length() < getMinLength()) {
			preformattedError = "field at position " + (cursor + 1) + " id="
					+ getID() + " field value too short.";

		}

		if (inValue.length() > getMaxLength()) {
			preformattedError = "field at position " + (cursor + 1) + " id="
					+ getID() + " field value too long.";
		}
		if (cursor >= getOccurs()) {
			cursor = 0;
		}

		int i;

		boolean negative = false;

		for (i = 0; i < inValue.length(); i++) {
			if (inValue.charAt(i) == dfs.getMinusSign()) {
				negative = true;
				continue;
			}
			if (inValue.charAt(i) == dfs.getDecimalSeparator()) {
				if (fractionDigits == -1) {
					continue;
				} else {
					throw new OBOEException(getID()
							+ ": value contains decimal separator character("
							+ inValue.charAt(i) + "), use setFormatted method");
				}
			}
			if (Character.isDigit(inValue.charAt(i)) == false) {
				if (fractionDigits > -1) {
					throw new OBOEException(getID() + ": value " + inValue
							+ " contains format character(" + inValue.charAt(i)
							+ "), use setFormatted method");
				}
			}
		}

		if (fractionDigits == -1) {
			value[cursor] = inValue;
			return;
		}

		char array[] = new char[getMaxLength()];

		for (i = 0; i < getMaxLength(); i++) {
			array[i] = '0';
		}

		int j = getMaxLength() - 1;
		for (i = inValue.length() - 1; (i > -1) && (j > -1); i--, j--) {
			if (inValue.charAt(i) == dfs.getMinusSign()) {
				break;
			} else {
				array[j] = inValue.charAt(i);
			}
		}

		if (negative) {
			value[cursor] = dfs.getMinusSign() + new String(array);
		} else {
			value[cursor] = new String(array);
		}

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

		if (fractionDigits == -1) {
			return value[inPos];
		}

		int i;

		int stop = value[inPos].length() - getMinLength();
		boolean negative = (value[inPos].charAt(0) == dfs.getMinusSign());
		if (negative) {
			i = 1;
		} else {
			i = 0;
		}
		for (; i < stop; i++) {
			if ((value[inPos].charAt(i) > '0')
					&& (value[inPos].charAt(i) <= '9')) {
				break;
			}
		}

		value[inPos] = value[inPos].substring(i);
		if (negative) {
			value[inPos] = dfs.getMinusSign() + value[inPos];
		}
		return value[inPos];

	}

	/**
	 * gets the number of decimal positions
	 *
	 * @return int number of decimal positions
	 *
	 */

	public int getDecimalPositions() {
		return fractionDigits;
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
		StringBuilder sb = new StringBuilder();
		String got;
		int repeatCnt = -1;
		for (repeatCnt = value.length - 1; (repeatCnt > -1)
				&& (value[repeatCnt] == null); repeatCnt--) {
			;
		}
		for (int ii = 0; ii < value.length; ii++) {
			if ((value[ii] == null) && (ii > 0)) {
				break;
			}
			got = get(ii);
			if (got == null) {
				got = "";
			}

			if ((fractionDigits > -1) && (got.length() > 0)) {
				NumberFormat nf = NumberFormat.getNumberInstance();
				nf.setMinimumFractionDigits(fractionDigits);
				nf.setMaximumFractionDigits(fractionDigits);
				nf.setGroupingUsed(false);
				if (fractionDigits > 0) {
					nf.setMinimumIntegerDigits(
							getMinLength() - fractionDigits - 1);
					// subtract 1 for decimal
					nf.setMaximumIntegerDigits(
							getMaxLength() - fractionDigits - 1);
				} else {
					nf.setMinimumIntegerDigits(getMinLength() - fractionDigits);
					nf.setMaximumIntegerDigits(getMaxLength() - fractionDigits);
				}

				double d;
				try {
					d = Double.valueOf(got.trim()).doubleValue();
					for (i = 0; i < fractionDigits; i++) {
						d /= 10;
					}
					got = nf.format(d);
				} catch (java.lang.NumberFormatException e1) {
				}

			}

			switch (format) {
			case PREBUILD_FORMAT:
				got = get(ii);
				if (got == null) {
					got = "";
				}
				sb.append(got);
				if ((getOccurs() > 1) && (ii < repeatCnt)) {
					sb.append(Envelope.PREBUILD_REPEAT_DELIMITER.charAt(0));
				}
				break;

			case X12_FORMAT:
				got = get(ii);
				if (got == null) {
					got = "";
				}
				sb.append(got);
				if ((getOccurs() > 1) && (ii < repeatCnt)
						&& (Envelope.X12_REPEAT_DELIMITER
								.charAt(0) != '\u0000')) {
					sb.append(Envelope.X12_REPEAT_DELIMITER.charAt(0));
				}
				break;

			case EDIFACT_FORMAT:
				got = get(ii);
				if (got == null) {
					got = "";
				}
				sb.append(got);
				if ((getOccurs() > 1) && (ii < repeatCnt)) {
					sb.append(Envelope.EDIFACT_REPEAT_DELIMITER.charAt(0));
				}
				break;

			case TRADACOMS_FORMAT:
				got = get(ii);
				if (got == null) {
					got = "";
				}
				sb.append(got);
				if ((getOccurs() > 1) && (ii < repeatCnt)) {
					sb.append(Envelope.TRADACOMS_REPEAT_DELIMITER.charAt(0));
				}
				break;

			case XML_FORMAT:
				sb.append("<" + getShortName());

				sb.append(">" + got + "</" + getShortName() + ">"
						+ Util.lineFeed);
				break;
			case VALID_XML_FORMAT:
			case VALID_XML_FORMAT_WITH_POSITION:
				sb.append("<element code=\"" + getID() + "\"");
				sb.append(" name=\"" + getName() + "\"");
				if (format == Format.VALID_XML_FORMAT_WITH_POSITION) {
					sb.append(" docPosition=\"" + this.getPosition() + "\"");
				}

				sb.append(">");
				sb.append(
						"<value>" + got + "</value></element>" + Util.lineFeed);
				break;

			case CSV_FORMAT:
				sb.append("DE," + getID() + ",\"" + getName() + "\",\"" + got
						+ "\"" + Util.lineFeed);
				break;
			default:
				sb.append("\t" + getName() + ": " + got + Util.lineFeed);
			}
		}
		return sb.toString();

	}

	/**
	 * sets NumericDE object using an input string <br>
	 * use class set method to set values with assumed decimal
	 *
	 * @param input set contents
	 * @exception OBOEException invalid input
	 * @return String input formatted
	 */

	public String setFormatted(String input) throws OBOEException {

		if (input.length() == 0) {
			setNext(input);
			return input;
		}
		boolean nodigityet = true;
		boolean negative = false;
		boolean nowdecimal = false;

		char outnum[] = new char[input.length()];
		int outpos = -1;
		int i, il;

		char decnum[] = new char[input.length()];
		decnum[0] = '0';

		int decpos = -1;

		for (i = 0; i < input.length(); i++) {
			if (Character.isDigit(input.charAt(i))) {
				nodigityet = false;
				if (nowdecimal) {
					decpos++;
					decnum[decpos] = input.charAt(i);
				} else {
					outpos++;
					outnum[outpos] = input.charAt(i);
				}
			} else if (input.charAt(i) == dfs.getMinusSign()) {
				if (negative) {
					throw new OBOEException(getID() + ":invalid character at "
							+ i + " for " + input + " second negative ");
				}
				if (nodigityet == false) {
					throw new OBOEException(getID() + ":invalid character at "
							+ i + " for " + input + "  negative after number");
				}
				if (nowdecimal) {
					throw new OBOEException(getID() + ":invalid character at "
							+ i + " for " + input + " negative after decimal");
				}
				negative = true;
			} else if (input.charAt(i) == dfs.getDecimalSeparator()) {
				if (nowdecimal) {
					throw new OBOEException(getID() + ":invalid character at "
							+ i + " for " + input + " second decimal");
				}
				nowdecimal = true;
			} else if (input.charAt(i) == dfs.getGroupingSeparator()) {
				if (nowdecimal) {
					throw new OBOEException(getID() + ":invalid character at "
							+ i + " for " + input
							+ " group separator after decimal separator");
				}
				if (nodigityet) {
					throw new OBOEException(getID() + ":invalid character at "
							+ i + " for " + input
							+ " group separator before first digit");
				}
				if ((i + 3) > input.length()) {
					throw new OBOEException(getID() + ":invalid character at "
							+ i + " for " + input
							+ " not enough digits after group separator");
				}
				if (!Character.isDigit(input.charAt(i + 1))
						|| !Character.isDigit(input.charAt(i + 2))) {
					// || !Character.isDigit(input.charAt(i + 3)))
					throw new OBOEException(input.charAt(i)
							+ " invalid character at " + i + " for " + input
							+ " not all digits after group separator");
				}
				if (((i + 4) < input.length())
						&& Character.isDigit(input.charAt(i + 4))) {
					throw new OBOEException(getID() + ":invalid character at "
							+ i + " for " + input
							+ " too many  digits after group separator");
				}
			} else if (nodigityet == false) {
				throw new OBOEException(getID() + ":invalid character at " + i
						+ " for " + input);
			}

		}
		if (nowdecimal == false) {
			for (i = 0; i < input.length(); i++) {
				decnum[i] = '0';
			}
		}

		if (fractionDigits < 0) {
			setNext(input);
			return input;
		}

		il = getMaxLength() - fractionDigits;

		char outn[] = new char[getMaxLength()];

		for (i = 0; i < getMaxLength(); i++) {
			outn[i] = '0';
		}

		if ((il - 1) >= getMaxLength()) {
			throw new OBOEException(getID() + ":logic error");
		}

		for (i = il - 1; i > -1; i--) {
			if (outpos < 0) {
				break;
			}
			outn[i] = outnum[outpos];
			outpos--;
		}

		int repos = 0;
		for (i = il; i < getMaxLength(); i++) {
			outn[i] = decnum[repos];
			repos++;
			if (repos > decpos) {
				break;
			}
		}
		/*
		 * if (negative) { for (i = 1; i<getMaxLength(); i++) { if (outn[i] !=
		 * '0') { outn[0] = dfs.getMinusSign(); break; } }
		 */
		boolean allzeros = true;
		for (i = 0; i < getMaxLength(); i++) {
			if (outn[i] != '0') {
				allzeros = false;
			}
		}
		if (allzeros) {
			negative = false;
		}

		if (negative) {
			setNext(dfs.getMinusSign() + new String(outn));
		} else {
			setNext(new String(outn));
		}

		return get(cursor);

	}

	/**
	 * returns error responses of contents
	 *
	 * @param inText String text
	 * @return String - null if no error
	 */
	@Override
	public String validate(String inText) {

		if (preformattedError.length() > 0) {
			return preformattedError;
		}

		String returnMessage = ((TemplateDataElement) myTemplate)
				.validate(inText);
		if (returnMessage != null) {
			return returnMessage;
		}

		returnMessage = "";

		int countDigits = 0;
		for (int i = 0; i < inText.length(); i++) {
			if (Character.isDigit(inText.charAt(i))) {
				countDigits++;
			}
		}

		if (countDigits > getMaxLength()) {
			return getID() + ": Invalid Number, too many digits";
		}

		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMinimumFractionDigits(fractionDigits);
		nf.setMaximumFractionDigits(fractionDigits);
		nf.setMinimumIntegerDigits(getMinLength());
		nf.setMaximumIntegerDigits(getMaxLength());
		String testText = inText.trim();
		try {
			double d = Double.valueOf(testText).doubleValue();
			for (int i = 0; i < fractionDigits; i++) {
				d /= 10;
			}
			returnMessage = nf.format(d);
		} catch (java.lang.NumberFormatException e1) {
			e1.printStackTrace();
			return getID() + ":Invalid Number Format";
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

		if (preformattedError.length() > 0) {
			inDErr.addError(0, getID(), preformattedError, getParent(), "10",
					this, DocumentErrors.ERROR_TYPE.Integrity);
			return false;
		}

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

		if ((value[0].charAt(0) == '-')
				&& ((value[0].length() - 1) == getMaxLength())) {
			;
		} else if (value[0].length() > getMaxLength()) {
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
				set[pos] = '0';
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
