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

package io.github.EDIandXML.OBOE.Parsers;

import java.io.IOException;
import java.io.PushbackInputStream;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.github.EDIandXML.OBOE.DataElements.CompositeElement;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.DataElements.NumericDE;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateCompositeElement;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.Tokenizers.DataTokenizer;
import io.github.EDIandXML.OBOE.Tokenizers.ITokenizer;
import io.github.EDIandXML.OBOE.util.Util;

public class CompositeElementParser {
	/**
	 * parsing logic for CompositeElement
	 *
	 * @param parseThisString
	 * @param transactionSetTokenizer
	 * @throws OBOEException most likely the composite item can't find any
	 *                       of the fields coming in.
	 * @return boolean true if tokenized string was used
	 */

	public static boolean parse(CompositeElement compositeElement,
			String parseThisString, ITokenizer transactionSetTokenizer)
			throws OBOEException {

		DataElement currentDE;

		DataTokenizer dataTokenizer = new DataTokenizer(parseThisString,
				transactionSetTokenizer.getElementSeparator(),
				transactionSetTokenizer.getEscapeCharacters());

		int elementsToParse = dataTokenizer.countTokens();

		var allTempElements = ((TemplateCompositeElement) compositeElement.myTemplate)
				.getAllTemplateElements();

		var cnt = 0;

		var $token = Util.unEscape(dataTokenizer.nextToken(),
				transactionSetTokenizer.getEscapeCharacters());
		while (elementsToParse > 0) {
			cnt++;
			if ($token.length() > 0) {
				if (!allTempElements.containsKey(cnt)) {
					throw new OBOEException(
							"No data element defined at position " + cnt
									+ " for composite "
									+ compositeElement.getID());
				}
			}
			if (compositeElement.getElement(cnt) == null) {
				compositeElement.buildElement(cnt);
			}
			currentDE = (DataElement) compositeElement.getElement(cnt);

			currentDE.set($token);
			elementsToParse--;
			$token = Util.unEscape(dataTokenizer.nextToken(),
					transactionSetTokenizer.getEscapeCharacters());

		}
		return true;

	}

	/**
	 * parses a Fixed Length EDI Document adds to datalement ArrayList or
	 * CompositeElement and adds to secondary segment ArrayList
	 *
	 * @param pis PushbackInputStream
	 * @throws OBOEException as thrown
	 */

	public static void parse(CompositeElement compositeElement,
			PushbackInputStream pis) throws OBOEException, IOException {
		int i = 0;

		DataElement currentDE;
		var template = compositeElement.getTemplate();
		for (i = 1; i <= template.getContainerSize(); i++) {

			if (template.isTemplateComposite(i + 1)) {
				throw new OBOEException(
						"Can't have composite element within a composite element.");
			} else if (template.isTemplateDE(i)) {
				TemplateDataElement tde = (TemplateDataElement) template
						.getTemplateElement(i);
				var me = new byte[tde.getMaxLength()];
				pis.read(me);
				if (me[0] == 0) {
					continue;
				}
				String s = Util.rightTrim(new String(me));

				currentDE = (DataElement) compositeElement.getElement(i);
				if (currentDE == null) {
					currentDE = (DataElement) compositeElement.buildElement(i);
				}
				StringBuilder sb = new StringBuilder(s);
				if (currentDE.isRequired()) {
					for (int j = s.length(); j < currentDE
							.getMinLength(); j++) {
						sb.append(' ');
					}
					s = sb.toString();
				} else if (s.length() > 0) {
					for (int j = s.length(); j < currentDE
							.getMinLength(); j++) {
						sb.append(' ');
					}
					s = sb.toString();
				}
				currentDE.set(s);
			}
		}

	}

	/**
	 * parses a XML EDI Document adds to datalement ArrayList
	 *
	 * @param node Node
	 * @throws OBOEException Node is unknown to composite
	 */

	public static void parse(CompositeElement compositeElement, Node node)
			throws OBOEException {
		int currentPosition = 0, n;

		NodeList nl = node.getChildNodes();
		NodeList cnl;
		DataElement currentDE = null;

		Node currentNode;
		String lastSubNodeName = null;

		Node cnode;
		nodeLoop: for (n = 0; n < nl.getLength(); n++) {
			currentNode = nl.item(n);
			if (currentNode.getNodeType() != Node.ELEMENT_NODE) {
				continue nodeLoop;
			}
			cnl = currentNode.getChildNodes();
			String currentSubNodeName = currentNode.getNodeName();
			if ((lastSubNodeName != null)
					&& lastSubNodeName.equals(currentSubNodeName)) {

				if (currentDE != null) {
					if (currentDE.getOccurs() != 1) {
						boolean textNodeFound = false;
						for (int nn = 0; nn < cnl.getLength(); nn++) {
							cnode = cnl.item(nn);
							if (cnode.getNodeType() == Node.TEXT_NODE) {
								textNodeFound = true;
								if (currentDE instanceof NumericDE) {
									((NumericDE) currentDE)
											.setFormatted(cnode.getNodeValue()); // why
																					// not
																					// set
																					// next?
								} else {
									currentDE.setNext(cnode.getNodeValue());
								}
								continue nodeLoop;
							}
						}

						if (textNodeFound == false) {
							currentDE.setNext(""); // set fields that have
													// no
													// values
						}
						continue nodeLoop;
					}
				}

			}
			int i;
			if (compositeElement.getTemplate() != null) {
				i = compositeElement.getTemplate().doYouUseThisXMLElement(
						currentSubNodeName, currentPosition);
			} else {
				i = compositeElement.doIUseThisXMLElement(currentSubNodeName,
						currentPosition);
			}

			currentPosition = i;
			var tc = (TemplateCompositeElement) compositeElement.myTemplate;
			if (tc.myElementContainer.templateDataElementList.get(i)
					.IAmATemplateComposite()) {
				throw new OBOEException(
						"Can't have composite element within a composite element.");
			}

			currentDE = (DataElement) compositeElement
					.getElement(currentPosition);
			if (currentDE == null) {
				currentDE = (DataElement) compositeElement
						.buildElement(currentPosition);
			}
			lastSubNodeName = currentDE.getShortName();
			boolean textNodeFound = false;
			if (currentDE.getShortName().compareTo(currentSubNodeName) == 0) {

				for (int nn = 0; nn < cnl.getLength(); nn++) {
					cnode = cnl.item(nn);
					if (cnode.getNodeType() == Node.TEXT_NODE) {
						textNodeFound = true;
						if (currentDE instanceof NumericDE) {
							((NumericDE) currentDE)
									.setFormatted(cnode.getNodeValue());
						} else if (currentDE.getOccurs() != 1) {
							currentDE.setNext(cnode.getNodeValue());
						} else {
							currentDE.set(cnode.getNodeValue());
						}
						continue nodeLoop;
					}
				}

				if (textNodeFound == false) {
					currentDE.set(""); // set fields that have no values
				}
				continue nodeLoop;
			} else if (textNodeFound == false) {
				currentDE.set(""); // set fields that have no values
			}
		}

	}

}
