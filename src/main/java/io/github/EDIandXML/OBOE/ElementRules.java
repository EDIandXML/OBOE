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

package io.github.EDIandXML.OBOE;

import java.util.StringTokenizer;

import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.DataElements.CompositeElement;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.DataElements.Element;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateCompositeElement;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;

public class ElementRules {
	/**
	 * Static text field, one or more of the fields in the rule must have
	 * content
	 */
	public static String oneOrMoreMustExist = "oneOrMoreMustExist";
	/**
	 * Static text field, if the first field has content so must all of the
	 * other fields
	 */
	public static String ifFirstExistsThenAllMustExist = "ifFirstExistsThenAllMustExist";
	/**
	 * Static text field, only one of these fields must exist
	 */
	public static String oneAndOnlyOneMayExist = "oneAndOnlyOneMayExist";
	/**
	 * Static text field, if the first field has content then at least one
	 * more field has content also.
	 */
	public static String ifFirstExistsThenAtLeastOneMoreMustExist = "ifFirstExistsThenAtLeastOneMoreMustExist";
	/**
	 * Static text field, if one exists they all must exist
	 */
	public static String allOrNoneMayExist = "allOrNoneMayExist";
	/**
	 * Static text field, if first has content then no others may have
	 * content
	 */
	public static String ifFirstExistsThenNoOthersMayExist = "ifFirstExistsThenNoOthersMayExist";

	private int ruleNumber = -1;
	private int positions[];
	private int positionCount;
	private String ruleText;
	private String rulePositions;

	/**
	 * Constructor
	 */
	public ElementRules() {
	}

	/**
	 * Constructor
	 *
	 * @param inRule      String contianing rule
	 * @param inPositions field positions seperated by commas
	 * @throws OBOEException probably one of the field ids is wrong
	 */
	public ElementRules(String inRule, String inPositions)
			throws OBOEException {
		ruleText = inRule;
		rulePositions = inPositions;
		if (inRule.compareTo(oneOrMoreMustExist) == 0) {
			ruleNumber = 1;
		} else if (inRule.compareTo(ifFirstExistsThenAllMustExist) == 0) {
			ruleNumber = 2;
		} else if (inRule.compareTo(oneAndOnlyOneMayExist) == 0) {
			ruleNumber = 3;
		} else if (inRule
				.compareTo(ifFirstExistsThenAtLeastOneMoreMustExist) == 0) {
			ruleNumber = 4;
		} else if (inRule.compareTo(allOrNoneMayExist) == 0) {
			ruleNumber = 5;
		} else if (inRule.compareTo(ifFirstExistsThenNoOthersMayExist) == 0) {
			ruleNumber = 6;
		} else {
			throw new OBOEException("Unknown rule " + inRule);
		}

		positionCount = 0;
		int i;
		for (i = 0; i < inPositions.length(); i++) {
			if (inPositions.charAt(i) == ' ') {
				continue;
			}
			if (inPositions.charAt(i) == ',') {
				positionCount++;
				continue;
			}
			if ((inPositions.charAt(i) < '0')
					|| (inPositions.charAt(i) > '9')) {
				throw new OBOEException(
						"Invalid character in positions, at position " + (i + 1)
								+ " in string " + inPositions);
			}
		}
		if (positionCount == 0) {
			throw new OBOEException(
					"Not enough positions specified in string " + inPositions);
		}

		positionCount++;
		positions = new int[positionCount];

		StringTokenizer st = new StringTokenizer(inPositions, ",");

		if (st.countTokens() != positionCount) {
			throw new OBOEException(
					"Based on count there are not enough positions specified in string "
							+ inPositions);
		}

		i = 0;
		int j = -1;
		String sPos;
		while (st.hasMoreElements()) {
			sPos = (String) st.nextElement();
			if (sPos.trim().length() == 0) {
				throw new OBOEException("Missing position at element " + i);
			}
			try {
				positions[i] = Integer.parseInt(sPos.trim());
			} catch (NumberFormatException nfe) {
				throw new OBOEException("Invalid position " + sPos
						+ " specified string " + inPositions);
			}
			if (positions[i] < 1) {
				for (j = 0; j < i; j++) {
					if (positions[j] == positions[i]) {
						throw new OBOEException("Position " + positions[j]
								+ " specified more than once in string "
								+ inPositions);
					}
				}
			}
			i++;
		}

		if (i != positionCount) {
			throw new OBOEException(
					"Based on tokenizer there is not an exact match for positions specified in string "
							+ inPositions);
		}

	}

	/**
	 * tests the fields according to the rules objects can be OBOE fields or
	 * JTextFields.
	 *
	 * @param elementsToTest fields to check
	 * @param parentTemplate TemplateSegment owning the fields
	 * @param throwError     if true then the error is thrown and
	 *                       application must catch it, otherwise error is
	 *                       reported in returned object
	 * @throws OBOEException thrown if throwError is true
	 * @return String of errors
	 */
	public String testRules(Object elementsToTest[],
			TemplateSegment parentTemplate, boolean throwError)
			throws OBOEException {
		int i;
		DataElement de;
		TemplateDataElement tde;
		CompositeElement cde;
		TemplateCompositeElement tc;
		StringBuilder sb = new StringBuilder();
		String testString;
		int pos;
		int cnt;
		switch (ruleNumber) {
		case 1: // one or more must exist

			for (i = 0; i < positionCount; i++) {
				pos = positions[i];
				if (pos > elementsToTest.length) {
					continue;
				}
				if (elementsToTest[pos - 1] == null) {
					continue;
				}
				if ((elementsToTest[pos - 1] instanceof String)) {
					testString = getObjectText(parentTemplate, pos - 1,
							elementsToTest[pos - 1]);
					if (testString.length() > 0) {
						return null;
					}
					if (parentTemplate.isTemplateComposite(pos)) {
						tc = (TemplateCompositeElement) parentTemplate
								.getTemplateElement(pos);
						sb.append(tc.getShortName() + " ");
					} else {
						tde = (TemplateDataElement) parentTemplate
								.getTemplateElement(pos);
						sb.append(tde.getShortName() + " ");
					}
				} else if (elementsToTest[pos - 1] instanceof Element) {
					de = (DataElement) elementsToTest[pos - 1];
					if (de != null) {
						if (de.getLength() > 0) {
							return null; // one does exist
						}
					}
					tde = (TemplateDataElement) parentTemplate
							.getTemplateElement(pos);
					sb.append(tde.getShortName() + " ");
				} else // better be a composite
				{
					cde = (CompositeElement) elementsToTest[pos - 1];
					if (cde != null) {
						if (cde.getElementCount() > 0) {
							return null; // one does exist
						}
					}
					tc = (TemplateCompositeElement) parentTemplate
							.getTemplateElement(pos);
					sb.append(tc.getShortName() + " ");
				}

			}
			if (throwError) {
				throw new OBOEException("rule failure: one or more must exist");
			} else {
				return ("rule failure: at least one of these data element fields must be used: "
						+ sb.toString());
			}

		case 2: // if First Exists Then All Must Exist

			pos = positions[0];
			if (pos > elementsToTest.length) {
				return null; // first doesn't exists
			}
			if (elementsToTest[pos - 1] == null) {
				return null;
			}
			String name;
			if (elementsToTest[pos - 1] instanceof String) {
				testString = getObjectText(parentTemplate, pos - 1,
						elementsToTest[pos - 1]);
				if (testString.length() == 0) {
					return null;
				}
				name = parentTemplate.getTemplateElement(pos).getShortName();
			} else if (elementsToTest[pos - 1] instanceof Element) {
				de = (DataElement) elementsToTest[pos - 1];
				if (de == null) {
					return null; // don't care
				}
				if (de.getLength() == 0) {
					return null; // don't care
				}
				tde = (TemplateDataElement) parentTemplate
						.getTemplateElement(pos);
				name = tde.getShortName();
			} else // better be a composite
			{
				cde = (CompositeElement) elementsToTest[pos - 1];
				if (cde == null) {
					return null;
				}
				if (cde.getElementCount() == 0) {
					return null; // don't care
				}
				tc = (TemplateCompositeElement) parentTemplate
						.getTemplateElement(pos);
				name = tc.getShortName();
			}

			for (i = 1; i < positionCount; i++) {
				pos = positions[i];
				if (pos > elementsToTest.length) {
					if (throwError) {
						throw new OBOEException(
								"rule failure: if First Exists Then All Must Exist at position "
										+ positions[i]);
					} else {
						return ("if " + name + " is used then you must fill in "
								+ parentTemplate.getTemplateElement(pos)
										.getShortName());
					}
				}
				if ((elementsToTest[pos - 1] instanceof String)) {
					testString = getObjectText(parentTemplate, pos - 1,
							elementsToTest[pos - 1]);
					if (testString.length() > 0) {
						continue;
					}
				} else if (elementsToTest[pos - 1] instanceof Element) {
					de = (DataElement) elementsToTest[pos - 1];
					if (de != null) {
						if (de.getLength() > 0) {
							continue;
						}
					}
				} else {
					cde = (CompositeElement) elementsToTest[pos - 1];
					if (cde != null) {
						if (cde.getElementCount() > 0) {
							continue;
						}
					}
				}
				if (parentTemplate.isTemplateComposite(pos)) {
					if (throwError) {
						throw new OBOEException(
								"rule failure: if First Exists Then All Must Exist at position "
										+ positions[i]);
					} else {
						return ("if " + name + " is used then you must fill in "
								+ parentTemplate.getTemplateElement(pos)
										.getShortName());
					}
				} else {
					if (throwError) {
						throw new OBOEException(
								"rule failure: if First Exists Then All Must Exist at position "
										+ positions[i]);
					} else {
						return ("if " + name + " is used then you must fill in "
								+ parentTemplate.getTemplateElement(pos)
										.getShortName());
					}
				}
			}
			return null;

		case 3: // oneAndOnlyOneMayExist

			cnt = 0;
			if (!throwError) {
				sb = new StringBuilder(
						"enter data into only ONE of these fields: ");
			}
			for (i = 0; i < positionCount; i++) {
				pos = positions[i];
				if (pos > elementsToTest.length) {
					continue;
				}
				if (elementsToTest[pos - 1] == null) {
					continue;
				}
				if ((elementsToTest[pos - 1] instanceof String)) {
					testString = getObjectText(parentTemplate, pos - 1,
							elementsToTest[pos - 1]);
					if (testString.length() == 0) {
						continue;
					}
				} else if (elementsToTest[pos - 1] instanceof Element) {
					de = (DataElement) elementsToTest[pos - 1];
					if (de == null) {
						continue;
					}
					if (de.getLength() == 0) {
						continue;
					}
				} else {
					cde = (CompositeElement) elementsToTest[pos - 1];
					if (cde == null) {
						continue;
					}
					if (cde.getElementCount() == 0) {
						continue;
					}
				}
				if (!throwError) {

					sb.append(parentTemplate.getTemplateElement(pos).getShortName()
							+ " ");

				}

				cnt++;
			}
			if (cnt > 1) {
				if (throwError) {
					throw new OBOEException(
							"rule failure: one and only one may exist");
				} else {
					return (sb.toString());
				}
			}
			return null;

		case 4: // ifFirstExistsThenAtLeastOneMoreMustExist

			if (elementsToTest.length == 0) {
				return null; // first doesn't exists
			}

			cnt = 0;
			pos = positions[0];
			if (elementsToTest[pos - 1] == null) {
				return null;
			}

			if (elementsToTest[pos - 1] instanceof String) {
				testString = getObjectText(parentTemplate, pos - 1,
						elementsToTest[pos - 1]);
				if (testString.length() == 0) {
					return null;
				}
				name = parentTemplate.getTemplateElement(pos).getShortName();
			} else if (elementsToTest[pos - 1] instanceof Element) {
				de = (DataElement) elementsToTest[pos - 1];
				if (de == null) {
					return null; // don't care
				}
				if (de.getLength() == 0) {
					return null; // don't care
				}
				tde = (TemplateDataElement) parentTemplate
						.getTemplateElement(pos);
				name = tde.getShortName();
			} else // better be a composite
			{
				cde = (CompositeElement) elementsToTest[pos - 1];
				if (cde == null) {
					return null;
				}
				if (cde.getElementCount() == 0) {
					return null;
				}
				tc = (TemplateCompositeElement) parentTemplate
						.getTemplateElement(pos);
				name = tc.getShortName();
			}
			for (i = 1; i < positionCount; i++) {
				pos = positions[i];
				if (pos > elementsToTest.length) {
					continue;
				}
				if (elementsToTest[pos - 1] == null) {
					continue;
				}

				sb.append(parentTemplate.getTemplateElement(pos).getShortName()
						+ " ");

				if ((elementsToTest[pos - 1] instanceof String)) {
					testString = getObjectText(parentTemplate, pos - 1,
							elementsToTest[pos - 1]);
					if (testString.length() == 0) {
						continue;
					}
				} else if (elementsToTest[pos - 1] instanceof Element) {
					de = (DataElement) elementsToTest[pos - 1];
					if (de == null) {
						continue;
					}
					if (de.getLength() == 0) {
						continue;
					}
				} else {
					cde = (CompositeElement) elementsToTest[pos - 1];
					if (cde == null) {
						continue;
					}
					if (cde.getElementCount() == 0) {
						continue;
					}
				}
				cnt++;
			}
			if (cnt == 0) {
				if (throwError) {
					throw new OBOEException(
							"rule failure: if first exists then at least one more must exist");
				} else {
					return ("if " + name
							+ " is used then use least one of the following "
							+ sb.toString());
				}
			}
			return null;

		case 5: // allOrNoneMayExist
			cnt = 0;
			if (elementsToTest.length == 0) {
				return null; // first doesn't exists
			}

			sb.append("enter data into none or all of the following fields: ");
			for (i = 0; i < positionCount; i++) {
				pos = positions[i];

				sb.append(parentTemplate.getTemplateElement(pos).getShortName()
						+ " ");

				if (elementsToTest[pos - 1] == null) {
					continue;
				}
				if ((elementsToTest[pos - 1] instanceof String)) {
					testString = getObjectText(parentTemplate, pos - 1,
							elementsToTest[pos - 1]);
					if (testString.length() == 0) {
						return null;
					}
				} else if (elementsToTest[pos - 1] instanceof Element) {
					if (pos > elementsToTest.length) {
						continue;
					}
					de = (DataElement) elementsToTest[pos - 1];
					if (de == null) {
						continue;
					}
					if (de.getLength() == 0) {
						continue;
					}
				} else {
					if (pos > elementsToTest.length) {
						continue;
					}
					cde = (CompositeElement) elementsToTest[pos - 1];
					if (cde == null) {
						continue;
					}
					if (cde.getElementCount() == 0) {
						continue;
					}
				}
				cnt++;
			}
			if ((cnt == 0) || (cnt == positionCount)) {
				return null;
			}
			if (throwError) {
				throw new OBOEException("rule failure: all or none may exists");
			} else {
				return (sb.toString());
			}

		case 6: // ifFirstExistsThenNoOthersMayExist

			if (elementsToTest.length == 0) {
				return null; // first doesn't exists
			}

			pos = positions[0];
			if (elementsToTest[pos - 1] instanceof String) {
				testString = getObjectText(parentTemplate, pos - 1,
						elementsToTest[pos - 1]);
				if (testString.length() == 0) {
					return null;
				}
				sb.append("if "
						+ parentTemplate.getTemplateElement(pos).getShortName()
						+ " is used then don't use the following: ");
			} else if (elementsToTest[pos - 1] instanceof Element) {
				de = (DataElement) elementsToTest[pos - 1];
				if (de == null) {
					return null; // don't care
				}
				if (de.getLength() == 0) {
					return null; // don't care
				}
				tde = (TemplateDataElement) parentTemplate
						.getTemplateElement(pos);
				sb.append("if "
						+ parentTemplate.getTemplateElement(pos).getShortName()
						+ " is used then don't use the following: ");
			} else // better be a composite
			{
				cde = (CompositeElement) elementsToTest[pos - 1];
				// if (cde == null) return null;
				if (cde.getElementCount() == 0) {
					return null;
				}

				sb.append("if "
						+ parentTemplate.getTemplateElement(pos).getShortName()
						+ " is used then don't use the following: ");
			}
			for (i = 1; i < positionCount; i++) {
				pos = positions[i];
				if (pos > elementsToTest.length) {
					continue;
				}
				if ((elementsToTest[pos - 1] instanceof String)) {
					testString = getObjectText(parentTemplate, pos - 1,
							elementsToTest[pos - 1]);
					if (testString.length() == 0) {
						continue;
					}
				} else if (elementsToTest[pos - 1] instanceof Element) {
					de = (DataElement) elementsToTest[pos - 1];
					if (de == null) {
						continue;
					}
					if (de.getLength() == 0) {
						continue;
					}
				} else {
					cde = (CompositeElement) elementsToTest[pos - 1];
					if (cde == null) {
						continue;
					}
					if (cde.getElementCount() == 0) {
						continue;
					}
				}
				sb.append(parentTemplate.getTemplateElement(pos).getShortName());
				if (throwError) {
					throw new OBOEException(
							"rule failure: if first exists then no others may exist");
				} else {
					return ("rule failure: if first exists then no others may exist"
							+ sb.toString());
				}
			}
			return null;

		default:
			if (throwError) {
				throw new OBOEException("Logic error in testRules");
			} else {
				return ("Logic error in testRules");
			}
		}

	}

	/**
	 * tests the fields according to the rules objects can be OBOE fields or
	 * JTextFields.
	 *
	 * @param elementsToTest fields to check
	 * @param parentSegment  Segment owning the fields
	 * @param throwError     if true then the error is thrown and
	 *                       application must catch it, otherwise error is
	 *                       reported in returned object
	 * @throws OBOEException thrown if throwError is true
	 * @return String of errors
	 */
	public String testRules(Object elementsToTest[], Segment parentSegment,
			boolean throwError) throws OBOEException {
		int i;
		DataElement de;
		CompositeElement cde;
		StringBuilder sb = new StringBuilder();
		String testString;
		int pos;
		int cnt;
		String name;

		switch (ruleNumber) {
		case 1: // one or more must exist

			for (i = 0; i < positionCount; i++) {
				pos = positions[i];
				if (pos > elementsToTest.length) {
					continue;
				}
				if (elementsToTest[pos - 1] == null) {
					continue;
				}
				if ((elementsToTest[pos - 1] instanceof String)) {
					testString = getObjectText(parentSegment, pos - 1,
							elementsToTest[pos - 1]);
					if (testString.length() > 0) {
						return null;
					}

					sb.append(parentSegment.getElement(pos).getShortName() + " ");

				} else if (elementsToTest[pos - 1] instanceof Element) {
					de = (DataElement) elementsToTest[pos - 1];
					if (de != null) {
						if (de.getLength() > 0) {
							return null; // one does exist
						}
					}
					de = (DataElement) parentSegment.getElement(pos);
					sb.append(de.getShortName() + " ");
				} else // better be a composite
				{
					cde = (CompositeElement) elementsToTest[pos - 1];
					if (cde != null) {
						if (cde.getElementCount() > 0) {
							return null; // one does exist
						}
					}
					sb.append(parentSegment.getElement(pos).getShortName() + " ");
				}

			}
			if (throwError) {
				throw new OBOEException("rule failure: one or more must exist");
			} else {
				return ("rule failure: at least one of these data element fields must be used: "
						+ sb.toString());
			}

		case 2: // if First Exists Then All Must Exist

			pos = positions[0];
			if (pos > elementsToTest.length) {
				return null; // first doesn't exists
			}
			name = "no data element defined";

			if (elementsToTest[pos - 1] == null) {
				return null;
			}

			if (elementsToTest[pos - 1] instanceof String) {
				testString = getObjectText(parentSegment, pos - 1,
						elementsToTest[pos - 1]);
				if (testString.length() == 0) {
					return null;
				}
				name = parentSegment.getElement(pos).getShortName();
			} else if (elementsToTest[pos - 1] instanceof Element) {
				de = (DataElement) elementsToTest[pos - 1];
				if (de == null) {
					return null; // don't care
				}
				if (de.getLength() == 0) {
					return null; // don't care
				}
				de = (DataElement) parentSegment.getElement(pos);
				name = de.getShortName();
			} else // better be a composite
			if (elementsToTest[pos - 1] instanceof CompositeElement) {
				cde = (CompositeElement) elementsToTest[pos - 1];
				if (cde == null) {
					return null;
				}
				if (cde.getElementCount() == 0) {
					return null; // don't care
				}
				cde = (CompositeElement) parentSegment.getElement(pos);
				name = cde.getShortName();
			} else {
				throw new OBOEException(
						"no data element defined at position " + pos);
			}

			for (i = 1; i < positionCount; i++) {
				pos = positions[i];
				if (pos > elementsToTest.length) {
					if (throwError) {
						throw new OBOEException(
								"rule failure: if First Exists Then All Must Exist at position "
										+ positions[i]);
					} else {
						return ("if " + name + " is used then you must fill in "
								+ parentSegment.getElement(pos).getShortName());
					}
				}
				if ((elementsToTest[pos - 1] instanceof String)) {
					testString = getObjectText(parentSegment, pos - 1,
							elementsToTest[pos - 1]);
					if (testString.length() > 0) {
						continue;
					}
				} else if (elementsToTest[pos - 1] instanceof Element) {
					de = (DataElement) elementsToTest[pos - 1];
					if (de != null) {
						if (de.getLength() > 0) {
							continue;
						}
					}
				} else {
					cde = (CompositeElement) elementsToTest[pos - 1];
					if (cde != null) {
						if (cde.getElementCount() > 0) {
							continue;
						}
					}
				}
				if (throwError) {
					throw new OBOEException(
							"rule failure: if First Exists Then All Must Exist at position "
									+ positions[i]);
				} else {
					return ("if " + name + " is used then you must fill in "
							+ parentSegment.getElement(pos).getShortName());
				}
			}
			return null;

		case 3: // oneAndOnlyOneMayExist

			cnt = 0;
			if (!throwError) {
				sb = new StringBuilder(
						"enter data into only ONE of these fields: ");
			}
			for (i = 0; i < positionCount; i++) {
				pos = positions[i];
				if (pos > elementsToTest.length) {
					continue;
				}
				if (elementsToTest[pos - 1] == null) {
					continue;
				}
				if ((elementsToTest[pos - 1] instanceof String)) {
					testString = getObjectText(parentSegment, pos - 1,
							elementsToTest[pos - 1]);
					if (testString.length() == 0) {
						continue;
					}
				} else if (elementsToTest[pos - 1] instanceof Element) {
					de = (DataElement) elementsToTest[pos - 1];
					if (de == null) {
						continue;
					}
					if (de.getLength() == 0) {
						continue;
					}
				} else {
					cde = (CompositeElement) elementsToTest[pos - 1];
					if (cde == null) {
						continue;
					}
					if (cde.getElementCount() == 0) {
						continue;
					}
				}
				if (!throwError) {

					sb.append(parentSegment.getElement(pos).getShortName() + " ");

				}

				cnt++;
			}
			if (cnt > 1) {
				if (throwError) {
					throw new OBOEException(
							"rule failure: one and only one may exist");
				} else {
					return (sb.toString());
				}
			}
			return null;

		case 4: // ifFirstExistsThenAtLeastOneMoreMustExist

			if (elementsToTest.length == 0) {
				return null; // first doesn't exists
			}

			cnt = 0;
			pos = positions[0];
			name = "no data element defined";
			if (elementsToTest[pos - 1] == null) {
				return null;
			}
			if (elementsToTest[pos - 1] instanceof String) {
				testString = getObjectText(parentSegment, pos - 1,
						elementsToTest[pos - 1]);
				if (testString.length() == 0) {
					return null;
				}
				name = parentSegment.getElement(pos).getShortName();
			} else if (elementsToTest[pos - 1] instanceof Element) {
				de = (DataElement) elementsToTest[pos - 1];
				if (de == null) {
					return null; // don't care
				}
				if (de.getLength() == 0) {
					return null; // don't care
				}
				de = (DataElement) parentSegment.getElement(pos);
				name = de.getShortName();
			} else // better be a composite
			{
				cde = (CompositeElement) elementsToTest[pos - 1];
				if (cde == null) {
					return null;
				}
				if (cde.getElementCount() == 0) {
					return null;
				}
				cde = (CompositeElement) parentSegment.getElement(pos);
				name = parentSegment.getElement(pos).getShortName();
			}
			for (i = 1; i < positionCount; i++) {
				pos = positions[i];
				if (pos > elementsToTest.length) {
					continue;
				}

				sb.append(parentSegment.getElement(pos).getShortName() + " ");

				if ((elementsToTest[pos - 1] instanceof String)) {
					testString = getObjectText(parentSegment, pos - 1,
							elementsToTest[pos - 1]);
					if (testString.length() == 0) {
						continue;
					}
				} else if (elementsToTest[pos - 1] instanceof Element) {
					de = (DataElement) elementsToTest[pos - 1];
					if (de == null) {
						continue;
					}
					if (de.getLength() == 0) {
						continue;
					}
				} else {
					cde = (CompositeElement) elementsToTest[pos - 1];
					if (cde == null) {
						continue;
					}
					if (cde.getElementCount() == 0) {
						continue;
					}
				}
				cnt++;
			}
			if (cnt == 0) {
				if (throwError) {
					throw new OBOEException(
							"rule failure: if first exists then at least one more must exist");
				} else {
					return ("if " + name
							+ " is used then use least one of the following "
							+ sb.toString());
				}
			}
			return null;

		case 5: // allOrNoneMayExist

			if (elementsToTest.length == 0) {
				return null; // first doesn't exists
			}
			cnt = 0;
			sb.append("enter data into none or all of the following fields: ");
			for (i = 0; i < positionCount; i++) {
				pos = positions[i];

				sb.append(parentSegment.getElement(pos).getShortName() + " ");
				if (elementsToTest[pos - 1] == null) {
					continue;
				}
				if ((elementsToTest[pos - 1] instanceof String)) {
					testString = getObjectText(parentSegment, pos - 1,
							elementsToTest[pos - 1]);
					if (testString.length() == 0) {
						return null;
					}
				} else if (elementsToTest[pos - 1] instanceof Element) {
					if (pos > elementsToTest.length) {
						continue;
					}
					de = (DataElement) elementsToTest[pos - 1];
					if (de == null) {
						continue;
					}
					if (de.getLength() == 0) {
						continue;
					}
				} else {
					if (pos > elementsToTest.length) {
						continue;
					}
					cde = (CompositeElement) elementsToTest[pos - 1];
					if (cde == null) {
						continue;
					}
					if (cde.getElementCount() == 0) {
						continue;
					}
				}
				cnt++;
			}
			if ((cnt == 0) || (cnt == positionCount)) {
				return null;
			}
			if (throwError) {
				throw new OBOEException("rule failure: all or none may exists");
			} else {
				return (sb.toString());
			}

		case 6: // ifFirstExistsThenNoOthersMayExist

			if (elementsToTest.length == 0) {
				return null; // first doesn't exists
			}

			pos = positions[0];
			if (elementsToTest[pos - 1] == null) {
				return null;
			}

			if (elementsToTest[pos - 1] instanceof String) {
				testString = getObjectText(parentSegment, pos - 1,
						elementsToTest[pos - 1]);
				if (testString.length() == 0) {
					return null;
				}

				sb.append("if " + parentSegment.getElement(pos).getShortName()
						+ " is used then don't use the following: ");

			} else if (elementsToTest[pos - 1] instanceof Element) {
				de = (DataElement) elementsToTest[pos - 1];
				if (de == null) {
					return null; // don't care
				}
				if (de.getLength() == 0) {
					return null; // don't care
				}
				de = (DataElement) parentSegment.getElement(pos);
				sb.append("if " + parentSegment.getElement(pos).getShortName()
						+ " is used then don't use the following: ");
			} else // better be a composite
			{
				cde = (CompositeElement) elementsToTest[pos - 1];
				// if (cde == null) return null;
				if (cde.getElementCount() == 0) {
					return null;
				}
				cde = (CompositeElement) parentSegment.getElement(pos);
				sb.append("if " + cde.getShortName()
						+ " is used then don't use the following: ");
			}
			for (i = 1; i < positionCount; i++) {
				pos = positions[i];
				if (pos > elementsToTest.length) {
					continue;
				}
				if (elementsToTest[pos - 1] == null) {
					continue;
				}
				if ((elementsToTest[pos - 1] instanceof String)) {
					testString = getObjectText(parentSegment, pos - 1,
							elementsToTest[pos - 1]);
					if (testString.length() == 0) {
						continue;
					}
				} else if (elementsToTest[pos - 1] instanceof Element) {
					de = (DataElement) elementsToTest[pos - 1];
					if (de == null) {
						continue;
					}
					if (de.getLength() == 0) {
						continue;
					}
				} else {
					cde = (CompositeElement) elementsToTest[pos - 1];
					if (cde == null) {
						continue;
					}
					if (cde.getElementCount() == 0) {
						continue;
					}
				}
				sb.append(parentSegment.getElement(pos).getShortName() + " ");
				if (throwError) {
					throw new OBOEException(
							"rule failure: if first exists then no others may exist");
				} else {
					return ("rule failure: if first exists then no others may exist"
							+ sb.toString());
				}
			}
			return null;

		default:
			if (throwError) {
				throw new OBOEException("Logic error in testRules");
			} else {
				return ("Logic error in testRules");
			}
		}

	}

	/**
	 * depending if it's a field get the objects text context
	 *
	 * @param parent     Object owning parent
	 * @param elementPos position of field or JTextField
	 * @param inObject   object containing the text
	 * @return String contents
	 */
	public static String getObjectText(Object parent, int elementPos,
			Object inObject) {
		if (inObject instanceof String) {
			return (String) inObject;
		}
		return ""; // inObject.toString();
	}

	/**
	 * get the rule number for this element rule
	 *
	 * @return int position of rule number
	 */
	public int getRuleNumber() {
		return ruleNumber;
	}

	/**
	 * get number of fields
	 *
	 * @return int count
	 */
	public int getPositionCount() {
		return positionCount;
	}

	/**
	 * returns actual field location
	 *
	 * @param pos int position with in position String.
	 * @return int position within object
	 */
	public int getPosition(int pos) {
		return positions[pos - 1];
	}

	/**
	 * returns the rules text contents
	 *
	 * @return String contents
	 */
	public String getRuleText() {
		return ruleText;
	}

	/**
	 * returns the rules positions as text
	 *
	 * @return String contents
	 */
	public String getRulePositions() {
		return rulePositions;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("<elementRule rule=\"");

		switch (ruleNumber) {
		case 1:
			sb.append(oneOrMoreMustExist);
			break;
		case 2:
			sb.append(ifFirstExistsThenAllMustExist);
			break;
		case 3:
			sb.append(oneAndOnlyOneMayExist);
			break;
		case 4:
			sb.append(ifFirstExistsThenAtLeastOneMoreMustExist);
			break;
		case 5:
			sb.append(allOrNoneMayExist);
			break;
		case 6:
			sb.append(ifFirstExistsThenNoOthersMayExist);
			break;
		default:
			break;
		}
		sb.append("\" positions=\"" + this.getRulePositions() + "\"/>");
		return sb.toString();
	}
}
