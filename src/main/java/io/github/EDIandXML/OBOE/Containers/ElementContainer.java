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

import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.DataElements.CharDE;
import io.github.EDIandXML.OBOE.DataElements.CompositeElement;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.DataElements.DateDE;
import io.github.EDIandXML.OBOE.DataElements.Element;
import io.github.EDIandXML.OBOE.DataElements.IDDE;
import io.github.EDIandXML.OBOE.DataElements.IDListProcessor;
import io.github.EDIandXML.OBOE.DataElements.NumericDE;
import io.github.EDIandXML.OBOE.DataElements.RealDE;
import io.github.EDIandXML.OBOE.DataElements.TimeDE;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.ITemplateElementContainer;
import io.github.EDIandXML.OBOE.Templates.TemplateCompositeElement;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.Templates.TemplateElement;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;

public class ElementContainer implements IElementContainer {
	/** log4j object */
	static Logger logr = LogManager.getLogger(CompositeElement.class);

	protected TreeMap<Integer, Element> elementList;

	ITemplateElementContainer myTemplate;

	IContainedObject containedBy;

	public ElementContainer(ITemplateElementContainer template,
			IContainedObject containedBy) {
		myTemplate = template;
		this.containedBy = containedBy;
		elementList = new TreeMap<Integer, Element>();
	}

	/**
	 * @return the elementList
	 */
	public TreeMap<Integer, Element> getElementList() {
		return elementList;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (var elm : elementList.values()) {
			sb.append(elm.get());
		}

		return sb.toString();

	}

	/**
	 * defines an element by the predefined template
	 *
	 * @param pos field to build is identified by its templates position
	 *            <br>
	 *            <b>position is relative to 1.</b>
	 * @return DataElement
	 */
	@Override
	public Element buildElement(int pos) {

		if (pos < 1) {
			throw new OBOEException("Invalid position to build an element");
		}
		// all element slots must be filled in the arraylist.
		// if the pos is higher than the (total number of elements less one)
		// already
		// built then we need to build the unused elements.

		if (elementList.get(pos) != null) { // it looks this elements already
											// built
			return elementList.get(pos);
		}

		// if (pos > getContainerSize() - 1) {
		// for (var pos1 = getContainerSize() + 1; pos1 < pos; pos1++) {
		// buildElement(pos1);
		// }
		// }

		if (myTemplate.isTemplateDE(pos) == false) {
			elementList
					.put(pos,
							new CompositeElement(
									(TemplateCompositeElement) myTemplate
											.getTemplateElement(pos),
									containedBy));
		} else {
			TemplateDataElement tde;

			tde = (TemplateDataElement) myTemplate.getTemplateElement(pos);

			switch (tde.getType().charAt(0)) {
			case 'A':
				if (tde.getIDList() != null) {
					elementList.put(pos, new IDDE(tde, containedBy));
				} else {
					elementList.put(pos, new CharDE(tde, containedBy));
				}
				break;
			case 'C':
				if (tde.getIDList() != null) {
					elementList.put(pos, new IDDE(tde, containedBy));
				} else {
					elementList.put(pos, new CharDE(tde, containedBy));
				}
				break;
			case 'I':
				elementList.put(pos, new IDDE(tde, containedBy));
				break;
			case 'D':
				elementList.put(pos, new DateDE(tde, containedBy));
				break;
			case 'T':
				elementList.put(pos, new TimeDE(tde, containedBy));
				break;
			case 'N':
				elementList.put(pos, new NumericDE(tde, containedBy));
				break;
			case 'R':
				elementList.put(pos, new RealDE(tde, containedBy));
				break;
			default:
				throw new OBOEException("Unknown dataElement type");
			}
		}
		return elementList.get(pos);
	}

	@Override
	public Element buildElement(String ID) {

		for (var elms : myTemplate.getAllTemplateElements().entrySet()) { // always

			TemplateElement te = elms.getValue();

			if (te.getID().equals(ID)) {

				if (elementList.containsKey(te.getPosition())) {

					logr.info("data element already built with id " + ID
							+ " for " + toString()
							+ ". checking for more with same id within segment");
					return elementList.get(te.getPosition());

				}

				return buildElement(te.getPosition());
			}

		}
		logr.error("No data element found with id " + ID + " in " + toString());
		return null;
	}

	@Override
	public Element buildElement(String ID, int offset) {
		for (var elms : myTemplate.getAllTemplateElements().entrySet()) { // always

			if (elms.getKey() <= offset) {
				continue;
			}
			TemplateElement te = elms.getValue();

			if (te.getID().equals(ID)) {

				if (elementList.containsKey(te.getPosition())) {

					logr.info("data element already built with id " + ID
							+ " for " + toString()
							+ ". checking for more with same id within segment");
					return elementList.get(te.getPosition());

				}

				return buildElement(te.getPosition());
			}

		}

		logr.error("No data element found with id " + ID + " in " + toString());
		return null;
	}

	/**
	 * returns the number of defined data element
	 *
	 * @return int DataElement count
	 */

	@Override
	public int getContainerSize() {
		return elementList.size();
	}

	/**
	 * returns a data element by its id,
	 *
	 * @return DataElement
	 * @param inID String id of element to return first one, there can be
	 *             multiples
	 * 
	 */

	@Override
	public Element getElement(String inID) {

		for (var dent : elementList.values()) {
			if (dent.getID().compareTo(inID) == 0) {
				return dent;
			}
		}

		return null;
	}

	@Override
	public Element getElement(String inID, int inoffset) {

		for (var elms : elementList.entrySet()) {
			if (elms.getKey() <= inoffset) {
				continue;
			}
			if (elms.getValue().getID().equals(inID)) {
				return elms.getValue();
			}
		}
		return null;

	}

	/**
	 * returns a data element by its location
	 *
	 * @return DataElement
	 * @param inPos int position of data element to return <br>
	 *              <b>position is relative to 1.</b>
	 */

	@Override
	public Element getElement(int inPos) {
		return elementList.get(inPos);
	}

	@Override
	public DataElement getDataElementByName(String inName, int inoffset) {
		for (var elms : elementList.entrySet()) {
			if (elms.getKey() <= inoffset) {
				continue;
			}
			if (elms.getValue() instanceof DataElement) {
				if (elms.getValue().getName().equals(inName)) {
					return (DataElement) elms.getValue();
				}
			}
		}

		return null;
	}

	/**
	 * returns a boolean if ArrayList position held by a data element
	 *
	 * @return boolean
	 * @param inPos is object in the array a dataelement <br>
	 *              <b>position is relative to 1.</b>
	 */
	@Override
	public boolean isDataElement(int inPos) {

		return elementList.get(inPos).IAmADataElement();
	}

	@Override
	public boolean isCompositeElement(int inPos) {
		return elementList.get(inPos).IAmACompositeElement();
	}

	@Override
	public String getDataElementValue(String ID) throws OBOEException {
		DataElement de = (DataElement) getElement(ID);
		if (de == null) {
			return "";
		}
		return de.get();
	}

	@Override
	public String getDataElementValue(int pos) throws OBOEException {
		DataElement de = (DataElement) getElement(pos);
		if (de == null) {
			return null;
		}
		return de.get();
	}

	@Override
	public DataElement setDataElementValue(String ID, String inValue)
			throws OBOEException {

		DataElement de;
		de = (DataElement) getElement(ID);
		if (de == null) {
			de = (DataElement) buildElement(ID);
		}
		if (de == null) {
			throw new OBOEException("Unknown Data Element with ID =  " + ID);
		}
		de.set(inValue);
		return de;
	}

	@Override
	public DataElement setDataElementValue(String ID, int offset,
			String inValue) throws OBOEException {

		DataElement de;
		de = (DataElement) getElement(ID, offset);
		if (de == null) {
			de = (DataElement) buildElement(ID, offset);
		}
		if (de == null) {
			throw new OBOEException("Unknown Data Element with ID =  " + ID);
		}
		de.set(inValue);
		return de;
	}

	@Override
	public DataElement setDataElementValue(int pos, String inValue)
			throws OBOEException {
		if (pos < 0) {
			throw new OBOEException("Invalid position for data element " + pos);
		}
		DataElement de = (DataElement) buildElement(pos);
		if (de == null) {
			throw new OBOEException(
					"Unknown position when setting data element " + pos);
		}
		de.set(inValue);
		return de;
	}

	@Override
	public boolean validate() {
		boolean returnValidation = true;

		DocumentErrors dErr = new DocumentErrors();
		for (var deme : elementList.values()) {
			if (deme.IAmACompositeElement()) {
				CompositeElement cd = (CompositeElement) deme;
				if (cd.isRequired() & cd.get().isEmpty()) {
					dErr.addError(cd.getPosition(),
							containedBy.getID() + cd.getPosition(),
							"Required CompositeElement missing, see "
									+ cd.getName() + " at position "
									+ cd.getPosition(),
							containedBy, "1", cd,
							DocumentErrors.ERROR_TYPE.Integrity);
				} else {
					cd.validate(dErr);
				}
			} else { // data element
				DataElement de = (DataElement) deme;
				if (de.isRequired()) {
					if (de.get() == null) {
						dErr.addError(de.getPosition(),
								containedBy.getID() + de.getPosition(),
								"Required DataElement missing, see "
										+ de.getName() + " at position "
										+ de.getPosition(),
								containedBy, "1", de,
								DocumentErrors.ERROR_TYPE.Integrity);
					} else if (de.get().length() == 0) {
						dErr.addError(de.getPosition(),
								containedBy.getID() + de.getPosition(),
								"Required DataElement missing, see "
										+ de.getName() + " at position "
										+ de.getPosition(),
								containedBy, "1", de,
								DocumentErrors.ERROR_TYPE.Integrity);
					} else {
						de.validate(dErr);
					}
				}
			}
		}

		returnValidation &= (dErr.getErrorCount() == 0);
		final String errs[] = dErr.getError();
		for (int dei = 0; dei < dErr.getErrorCount(); dei++) {
			logr.error(errs[dei]);

		}

		return returnValidation;

	}

	@Override
	public boolean validate(DocumentErrors dErr) {

		boolean returnValidation = true;

		for (var deme : elementList.values()) {
			if (deme.IAmACompositeElement()) {
				CompositeElement cd = (CompositeElement) deme;
				if (cd.isRequired() & cd.get() == null) {
					dErr.addError(cd.getPosition(),
							containedBy.getID() + cd.getPosition(),
							"Required CompositeElement missing, see "
									+ cd.getName() + " at position "
									+ cd.getPosition(),
							containedBy, "1", cd,
							DocumentErrors.ERROR_TYPE.Integrity);
				} else {
					cd.validate(dErr);
				}
			} else { // data element
				DataElement de = (DataElement) deme;
				var got = de.get();
				if (de.isRequired()) {
					if (got == null) {
						dErr.addError(de.getPosition(),
								containedBy.getID() + de.getPosition(),
								"Required DataElement missing, see "
										+ de.getName() + " at position "
										+ de.getPosition(),
								containedBy, "1", de,
								DocumentErrors.ERROR_TYPE.Integrity);
					} else if (got.length() < 1) {
						dErr.addError(de.getPosition(),
								containedBy.getID() + de.getPosition(),
								"Required DataElement missing, see "
										+ de.getName() + " at position "
										+ de.getPosition(),
								containedBy, "1", de,
								DocumentErrors.ERROR_TYPE.Integrity);
					} else {
						de.validate(dErr);
					}
				}
			}
		}

		returnValidation &= (dErr.getErrorCount() == 0);
		// String errs[] = dErr.getError();
		// for (int dei = 0; dei < dErr.getErrorCount(); dei++) {
		// logr.error(errs[dei]);
		//
		// }

		return returnValidation;

	}

	@Override
	public String getFormattedText(Format format) {

		StringBuilder sbFormattedText = new StringBuilder();

		int fieldToProcess = 1;

		if (myTemplate instanceof TemplateSegment) {
			switch (format) {
			case PREBUILD_FORMAT:
				sbFormattedText
						.append(myTemplate instanceof TemplateCompositeElement
								? Envelope.PREBUILD_GROUP_DELIMITER.charAt(0)
								: Envelope.PREBUILD_FIELD_DELIMITER.charAt(0));
				break;
			case X12_FORMAT:
				sbFormattedText
						.append(myTemplate instanceof TemplateCompositeElement
								? Envelope.X12_GROUP_DELIMITER.charAt(0)
								: Envelope.X12_FIELD_DELIMITER.charAt(0));
				break;
			case EDIFACT_FORMAT:
				sbFormattedText
						.append(myTemplate instanceof TemplateCompositeElement
								? Envelope.EDIFACT_GROUP_DELIMITER.charAt(0)
								: Envelope.EDIFACT_FIELD_DELIMITER.charAt(0));
				break;
			case TRADACOMS_FORMAT:
				if (fieldToProcess == 1) {
					sbFormattedText.append(
							Envelope.TRADACOMS_SEGID_DELIMITER.charAt(0));
				} else {
					sbFormattedText.append(
							myTemplate instanceof TemplateCompositeElement
									? Envelope.TRADACOMS_GROUP_DELIMITER
											.charAt(0)
									: Envelope.TRADACOMS_FIELD_DELIMITER
											.charAt(0));
				}
				break;

			default:
				break;
			}
		}

		for (int cnt = 1; cnt <= elementList.lastKey(); cnt++) {
			if (elementList.containsKey(cnt)) {
				var de = elementList.get(cnt);
				if (de.getID().equals("UNA") == false
						|| (format != Format.EDIFACT_FORMAT)) {
					// treat UNA differently
					sbFormattedText.append(de.getFormattedText(format));
				} else {
					sbFormattedText.append(de.get());
				}
			}

			if (cnt != elementList.lastKey()) {
				switch (format) {
				case PREBUILD_FORMAT:
					sbFormattedText.append(
							myTemplate instanceof TemplateCompositeElement
									? Envelope.PREBUILD_GROUP_DELIMITER
											.charAt(0)
									: Envelope.PREBUILD_FIELD_DELIMITER
											.charAt(0));
					break;
				case X12_FORMAT:
					sbFormattedText.append(
							myTemplate instanceof TemplateCompositeElement
									? Envelope.X12_GROUP_DELIMITER.charAt(0)
									: Envelope.X12_FIELD_DELIMITER.charAt(0));
					break;
				case EDIFACT_FORMAT:
					sbFormattedText.append(
							myTemplate instanceof TemplateCompositeElement
									? Envelope.EDIFACT_GROUP_DELIMITER.charAt(0)
									: Envelope.EDIFACT_FIELD_DELIMITER
											.charAt(0));
					break;
				case TRADACOMS_FORMAT:
					sbFormattedText.append(
							myTemplate instanceof TemplateCompositeElement
									? Envelope.TRADACOMS_GROUP_DELIMITER
											.charAt(0)
									: Envelope.TRADACOMS_FIELD_DELIMITER
											.charAt(0));
					break;
				default:
					break;
				}
			}

		}
		return sbFormattedText.toString();

	}

	@Override
	public int getElementCount() {
		int len = 0;
		for (var elm : elementList.values()) {
			if (elm.IAmACompositeElement()) {
				CompositeElement ce = (CompositeElement) elm;
				len += ce.getElementCount();

			} else {
				DataElement de = (DataElement) elm;
				len += de.getLength();
			}

		}
		return len;
	}

	@Override
	public int trim() {

		DataElement currentDE = null;
		CompositeElement currentComp = null;

		for (var elm : elementList.reversed().entrySet()) {
			if (elm.getValue().IAmADataElement()) {
				currentDE = (DataElement) elm.getValue();
				if (currentDE.getLength() > 0) {
					return elm.getKey();
				}
				elementList.remove(elm.getKey());
			} else if (elm.getValue().IAmACompositeElement()) {
				currentComp = (CompositeElement) elm.getValue();
				if (currentComp.trim() > 0) {
					return elm.getKey();
				}
				elementList.remove(elm.getKey());
			}
		}

		return -1;
	}

	@Override

	public int doIUseThisXMLElement(String inTag, int startAt) {

		for (var pos = startAt; pos < elementList.size(); pos++) {
			if (elementList.get(pos).getShortName().compareTo(inTag) == 0) {
				return pos;
			}
		}
		return -1;
	}

	@Override
	public void useDefault() {

		initializeToPrevalidateFields();

		for (var te : myTemplate.getAllTemplateElementsValues()) {
			if (te.isRequired()) {
				buildElement(te.getPosition());
				elementList.get(te.getPosition()).useDefault();
			}

		}

	}

	/**
	 * sets the default value for the data elements <br>
	 * will create mandatory subsegments. <br>
	 * if mandatory subsegment is part of ArrayList (collection) will create
	 * the first one
	 */

	private void initializeToPrevalidateFields() {

		if (myTemplate.canYouPrevalidate() == false) {
			return;
		}
		for (var te : myTemplate.getAllTemplateElementsValues()) {
			if (te.isRequired()) {
				buildElement(te.getPosition());
				elementList.get(te.getPosition()).useDefault();
			}

		}

		IDListProcessor idl = myTemplate.getIDListThatPrevalidates();

		for (var elm : elementList.values()) {
			if (elm.IAmADataElement()) {
				DataElement de = (DataElement) elm;
				if (de.getType().equals("ID")) {
					de.set(idl.getCodeByPos(0));
				}

			}
		}

	}

	@Override
	public Element getPrimaryIDDE() {

		for (var elm : elementList.values()) {
			if (!elm.isRequired()) {
				continue;
			}
			if (elm.IAmACompositeElement()) {
				CompositeElement ce = (CompositeElement) elm;
				if (ce.getElement(1).isRequired()) {
					DataElement ded = (DataElement) ce.getElement(1);
					if (ded.getType().equals("ID")) {
						return ded;
					}
				}
			} else {
				DataElement ded = (DataElement) elm;
				if (ded.getType().equals("ID")) {
					return ded;
				}
			}
		}

		return null;
	}

	public boolean hasText() {
		for (var elm : elementList.values()) {
			if (elm.IAmADataElement()) {
				DataElement de = (DataElement) elm;
				if (de.get() == null) {
					continue;
				}
				if (de.get().length() > 0) {
					return true;
				}
			}
		}

		return false;
	}

}
