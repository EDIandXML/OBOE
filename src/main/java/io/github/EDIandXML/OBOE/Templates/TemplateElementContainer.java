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

package io.github.EDIandXML.OBOE.Templates;

import java.util.Collection;
import java.util.TreeMap;

import io.github.EDIandXML.OBOE.Identifier;
import io.github.EDIandXML.OBOE.Containers.MetaContainer;
import io.github.EDIandXML.OBOE.DataElements.IDListProcessor;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Tokenizers.IDataTokenizer;
import io.github.EDIandXML.OBOE.Tokenizers.ITokenizer;
import io.github.EDIandXML.OBOE.Tokenizers.Tokenizer;
import io.github.EDIandXML.OBOE.util.Util;

public class TemplateElementContainer implements ITemplateElementContainer {
	/**
	 * segment ArrayList for elements
	 */
	public TreeMap<Integer, TemplateElement> templateDataElementList;

	public Identifier who;

	protected TemplateElementContainer(Identifier who) {
		this.who = who;
		templateDataElementList = new TreeMap<Integer, TemplateElement>();
	}

	@Override
	public int getContainerSize() {
		return templateDataElementList.size();
	}

	@Override
	public void addElement(TemplateElement inTemplateElement)
			throws OBOEException {
		if (inTemplateElement.getPosition() < 1) {
			throw new OBOEException(
					"Template Element Container position must be positive.");
		}
		// if (inTemplateElement.getPosition() > templateDataElementList.size()
		// + 1) {
		// throw new OBOEException(
		// "Template Element Container can not be sparse.");
		// }
		if (templateDataElementList
				.containsKey(inTemplateElement.getPosition())) {
			throw new OBOEException(
					"Template Element Container already populated.");
		}
		templateDataElementList.put(inTemplateElement.getPosition(),
				inTemplateElement);

	}

	@Override

	public Collection<TemplateElement> getAllTemplateElementsValues() {
		return templateDataElementList.values();

	}

	@Override
	public TemplateElement getTemplateElement(int pos) {
		return templateDataElementList.get(pos);
	}

	@Override
	public TemplateElement getTemplateElement(String id) {

		for (var tce : templateDataElementList.values()) {
			if (!tce.IAmATemplateDE()) {
				continue;
			}
			TemplateDataElement tde = (TemplateDataElement) tce;
			if (tde.getID().compareTo(id) == 0) {
				return tde;
			}
		}
		return null;
	}

	@Override
	public boolean isTemplateDE(int at) {
		if (!templateDataElementList.containsKey(at)) {
			throw new OBOEException("Invalid or unknown position for Element");
		}
		return templateDataElementList.get(at).IAmATemplateDE();

	}

	@Override
	public boolean isTemplateComposite(int at) {
		if (!templateDataElementList.containsKey(at)) {
			throw new OBOEException("Invalid or unknown position for Element");
		}
		return templateDataElementList.get(at).IAmATemplateComposite();
	}

	@Override
	public int doYouUseThisElement(String inID, int startAt) {

		for (var pos = startAt; pos < templateDataElementList.size(); pos++) {
			if (templateDataElementList.get(pos).getID().equals(inID)) {
				return pos;
			}
		}
		return -1;
	}

	@Override
	public int doYouUseThisXMLElement(String inXML, int startAt) {

		for (var te : templateDataElementList.entrySet()) {
			if (te.getKey() <= startAt) {
				continue;
			}
			if (te.getValue().getShortName().equals(inXML)) {
				return te.getKey();
			}
		}

		return -1;
	}

	@Override
	public boolean canYouPrevalidate() {
		if (Util.propertyFileIndicatesDoPrevalidate() == false) {
			return false;
		}
		for (var tce : templateDataElementList.values()) {
			if (!tce.isRequired()) {
				continue;
			}
			if (tce.IAmATemplateComposite()) {
				TemplateCompositeElement tc = (TemplateCompositeElement) tce;
				TemplateDataElement tcde = (TemplateDataElement) tc
						.getTemplateElement(1);
				if (tcde.isRequired() & tcde.getType().equals("ID")) {
					if (tcde.getIDList() == null) {
						return false;
					}
					return true;

				}
			} else {
				TemplateDataElement tde = (TemplateDataElement) tce;
				if (tde.getType().equals("ID")) {
					if (tde.getIDList() == null) {
						return false;
					}
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public IDListProcessor getIDListThatPrevalidates() {
		for (var tce : templateDataElementList.values()) {
			if (!tce.isRequired()) {
				continue;
			}
			if (tce.IAmATemplateComposite()) {
				TemplateCompositeElement tc = (TemplateCompositeElement) tce;
				TemplateDataElement tde = (TemplateDataElement) tc
						.getTemplateElement(1);
				if (tde.isRequired() & tde.getType().equals("ID")) {
					return tde.getIDList();
				}
				continue;
			} else { // data element
				TemplateDataElement tde = (TemplateDataElement) tce;
				if (tde.getType().equals("ID")) {
					return tde.getIDList();
				}
			}
		}

		return null;

	}

	@Override
	public boolean isThisYou(ITokenizer inToken) {
		TemplateCompositeElement tce;
		TemplateDataElement tde;
		int tokenPos = 0;
		for (var tdeme : templateDataElementList.values()) {
			tokenPos++;
			inToken.getNextDataElement();
			if (!tdeme.isRequired()) {
				continue;
			}
			if (tdeme.IAmATemplateComposite()) {
				tce = (TemplateCompositeElement) tdeme;
				tde = (TemplateDataElement) tce.myElementContainer.templateDataElementList
						.get(1);

				if ((tde.isRequired()) && (tde.getType().equals("ID"))
						&& (inToken.getCurrentDataElement().length() > 0)) {
					IDataTokenizer dt = inToken.makeSubfieldTokenizer();
					String s = Util.unEscape(dt.nextToken(),
							inToken.getEscapeCharacters());
					inToken.resetSegment();
					return tde.getIDList().isCodeValid(s);
				}
				continue;
			} else if (tdeme.IAmATemplateDE()) {
				tde = (TemplateDataElement) tdeme;
				if ((tde.isRequired()) && (tde.getType().equals("ID"))) {
					inToken.resetSegment();
					return tde.getIDList()
							.isCodeValid(inToken.getDataElementAt(tokenPos));
				}
			}
		}

		inToken.resetSegment();
		return false;

	}

	@Override
	public boolean isThisYou(String primaryIDValue) {

		for (var tdes : templateDataElementList.values()) {
			if (tdes.IAmATemplateComposite()) {
				TemplateCompositeElement tc = (TemplateCompositeElement) tdes;

				if (tc.isRequired()) {
					TemplateDataElement tde = (TemplateDataElement) tc
							.getTemplateElement(1);
					if ((tde.isRequired()) && (tde.getType().equals("ID"))) {
						return tde.getIDList().isCodeValid(primaryIDValue);
					}
					continue;
				}
			} else // its a regular data element
			{
				var tde = (TemplateDataElement) tdes;
				if ((tde.isRequired()) && (tde.getType().equals("ID"))) {
					return tde.getIDList().isCodeValid(primaryIDValue);
				}
			}
		}
		return false;

	}

	@Override
	public void whyNotYou(Tokenizer et, MetaContainer errContainer) {
		for (int i = 0; (i < getContainerSize())
				&& (i < (et.countDataElements() - 1)); i++) {
			et.getNextDataElement();
			if (templateDataElementList.get(i + 1).IAmATemplateComposite()
					&& (templateDataElementList.get(i + 1).isRequired())) {
				TemplateCompositeElement tce = (TemplateCompositeElement) templateDataElementList
						.get(i + 1);
				TemplateDataElement tde = (TemplateDataElement) tce
						.getTemplateElement(1);
				if ((tde.isRequired()) && (tde.getType().equals("ID"))) {
					IDataTokenizer dt = et.makeSubfieldTokenizer();
					String s = Util.unEscape(dt.nextToken(),
							et.getEscapeCharacters());
					et.resetSegment();
					et.reportError("ID Field " + who.getID() + " with value of "
							+ s
							+ " is invalid or segment is out of place at byte offset("
							+ et.getInputByteCount() + ") near segment ["
							+ et.getSegmentPos() + "]", errContainer, "7", this,
							DocumentErrors.ERROR_TYPE.Integrity);
				}
				continue;
			}
			if (isTemplateDE(i + 1) == false) {
				continue;
			}
			TemplateDataElement tde = (TemplateDataElement) templateDataElementList
					.get(i + 1);
			if (tde.getType().equals("ID")) {
				et.reportError("ID Field " + who.getID() + " with value of "
						+ et.getDataElementAt(i + 1)
						+ " is invalid or segment is out of place at byte offset("
						+ et.getInputByteCount() + ") near segment ["
						+ et.getSegmentPos() + "]", errContainer, "7", this,
						DocumentErrors.ERROR_TYPE.Integrity);
				et.resetSegment();
				return;
			}
		}
		et.reportError(
				"ID Field " + who.getID()
						+ " value can not be found near or at byte offset("
						+ et.getInputByteCount() + ") near segment ["
						+ et.getSegmentPos() + "]",
				errContainer, "7", this, DocumentErrors.ERROR_TYPE.Integrity);
		et.resetSegment();
		return;
	}

	@Override
	public String getID() {

		return who.getID();
	}

	@Override
	public TreeMap<Integer, TemplateElement> getAllTemplateElements() {
		return templateDataElementList;
	}

}
