
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

/**
 * OBOE - Open Business Objects for EDI
 */

import java.io.IOException;
import java.io.PushbackInputStream;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.github.EDIandXML.OBOE.Containers.MetaContainer;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.DataElements.CompositeElement;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.DataElements.NumericDE;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.Tokenizers.DataTokenizer;
import io.github.EDIandXML.OBOE.Tokenizers.ITokenizer;
import io.github.EDIandXML.OBOE.util.Util;

public class SegmentParser {
	/**
	 * parses an EDI Document adds to datalement ArrayList and adds to
	 * secondary segment ArrayList
	 *
	 *
	 * @param transactionSetTokenizer tokenizer hold string data
	 * @throws OBOEException as thrown, can't process subsegment maybe
	 */

	public static boolean parse(Segment segment,
			ITokenizer transactionSetTokenizer) throws OBOEException {

		DataElement currentDE;
		CompositeElement currentComposite;

		segment.setPosition(transactionSetTokenizer.getSegmentPos());

		transactionSetTokenizer.getNextDataElement();
		int elementsToParse = transactionSetTokenizer.countDataElements() - 1;
		// skip the id

		if ((segment.myTemplate != null)
				&& (elementsToParse > segment.myTemplate.getLastPosition())) {
			transactionSetTokenizer.reportError("Too many data elements "
					+ elementsToParse + " is greater than template count of "
					+ segment.getTemplate().getContainerSize()
					+ "; at byte offset("
					+ transactionSetTokenizer.getInputByteCount()
					+ ") near segment ["
					+ transactionSetTokenizer.getSegmentPos() + "]");
		} else if ((segment.myTemplate == null)
				&& (elementsToParse > segment.myTemplate.getLastPosition())) {
			transactionSetTokenizer
					.reportError("Too many data elements " + elementsToParse
							+ " is greater than built segment count of "
							+ segment.getContainerSize() + "; at byte offset("
							+ transactionSetTokenizer.getInputByteCount()
							+ ")near segment ["
							+ transactionSetTokenizer.getSegmentPos() + "]");
			return false;
		}

		var cnt = 1;
		// skip over segment id token

		while (/* cnt <= segment.myTemplate.getContainerSize() */
		/* && */ transactionSetTokenizer.hasMoreDataElements()) {
			String currentTokenString = transactionSetTokenizer
					.getCurrentDataElement();
			if (currentTokenString == null) {
				return false;
			}

			if (currentTokenString.length() == 0) {
				transactionSetTokenizer.getNextDataElement();
				cnt++;
				continue;
			}

			if (segment.getElement(cnt) == null) {
				segment.buildElement(cnt);
			}
			if (segment.isCompositeElement(cnt)) {
				currentComposite = (CompositeElement) segment.getElement(cnt);
				if (currentComposite.getOccurs() > 1) {
					DataTokenizer dataTokenizer = new DataTokenizer(
							currentTokenString,
							transactionSetTokenizer.getRepeaterCharacter(),
							transactionSetTokenizer.getEscapeCharacters());
					for (int dti = 0; dti < dataTokenizer
							.countTokens(); dti++) {
						if (dti > 0) {
							currentComposite.createNewGroup();
						}
						CompositeElementParser.parse(currentComposite,
								dataTokenizer.getTokenAt(dti),
								transactionSetTokenizer);
					}

				} else {
					if (CompositeElementParser.parse(currentComposite,
							transactionSetTokenizer.getCurrentDataElement(),
							transactionSetTokenizer) == false) {
						cnt++;
						continue;
					}
				}

			} else if (segment.isDataElement(cnt)) {
				currentDE = (DataElement) segment.getElement(cnt);
				try {
					if (currentDE.getOccurs() > 1) {
						DataTokenizer dataTokenizer = new DataTokenizer(
								currentTokenString,
								transactionSetTokenizer.getRepeaterCharacter(),
								transactionSetTokenizer.getEscapeCharacters());
						if (dataTokenizer.countTokens() == 0) {
							// need an empty string
							currentDE.setNext("");
						} else {
							for (int dti = 0; dti < dataTokenizer
									.countTokens(); dti++) {
								currentDE
										.setNext(dataTokenizer.getTokenAt(dti));
							}
						}
					} else {
						currentDE.set(Util.unEscape(
								transactionSetTokenizer.getCurrentDataElement(),
								transactionSetTokenizer.getEscapeCharacters()));
					}
				} catch (Exception exc) {
					transactionSetTokenizer.reportError("Parsing Error: "
							+ exc.getMessage() + "; at byte offset("
							+ transactionSetTokenizer.getInputByteCount()
							+ ")");
				}

			} else if (currentTokenString.length() > 0) {
				transactionSetTokenizer.reportError("Parsing Error: element (\""
						+ currentTokenString + "\") at position " + (cnt)
						+ " is not used in segment " + segment.getID()
						+ "; at byte offset("
						+ transactionSetTokenizer.getInputByteCount()
						+ ")near segment ["
						+ transactionSetTokenizer.getSegmentPos() + "]");
			}
			transactionSetTokenizer.getNextDataElement();
			cnt++;
		}
		if (transactionSetTokenizer
				.getNextSegment((MetaContainer) segment.getParent()) == null) {
			return true;
		}

		transactionSetTokenizer.getNextDataElement();
		return true;

	}

	/**
	 * parses a XML EDI Document adds to datalement ArrayList or
	 * CompositeElement and adds to secondary segment ArrayList
	 *
	 * @param node Incoming DOM node
	 * @throws OBOEException as thrown
	 */

	public static int parse(Segment segment, Node node) throws OBOEException {
		int currentPosition = 0, n;

		NodeList nl = node.getChildNodes();
		NodeList cnl;
		DataElement currentDE = null;
		CompositeElement currentComposite = null;
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

				if (segment.isCompositeElement(currentPosition)) {
					if (currentComposite.getOccurs() != 1) {
						currentComposite.createNewGroup();
						CompositeElementParser.parse(currentComposite,
								currentNode);
						continue nodeLoop;
					}
				} else {

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
							currentDE.setNext(""); // set fields that have no
													// values
						}
						continue nodeLoop;
					}

				}
			}

			if (segment.myTemplate != null) {
				currentPosition = segment.myTemplate.doYouUseThisXMLElement(
						currentSubNodeName, currentPosition);
			} else {
				currentPosition = segment.doIUseThisXMLElement(
						currentSubNodeName, currentPosition);
			}

			if (currentPosition >= 0) {

				segment.buildElement(currentPosition);

				if (segment.isCompositeElement(currentPosition)) {
					currentComposite = (CompositeElement) segment
							.getElement(currentPosition);
					if (currentComposite.getShortName()
							.compareTo(currentSubNodeName) == 0) {
						CompositeElementParser.parse(currentComposite,
								currentNode);
						lastSubNodeName = currentComposite.getShortName();
					}
					continue nodeLoop;
				} else // its a data element
				{
					currentDE = (DataElement) segment
							.getElement(currentPosition);
					lastSubNodeName = currentDE.getShortName();
					boolean textNodeFound = false;
					if (currentDE.getShortName().equals(currentSubNodeName)) {

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
			// virtual else

			throw new OBOEException(
					"Unknown node found " + currentNode.getNodeName());
		}

		return n;

	}

	/**
	 * parses a Fixed Length EDI Document adds to datalement ArrayList or
	 * CompositeElement and adds to secondary segment ArrayList
	 *
	 * @param pis PushbackInputStream
	 * @throws OBOEException as thrown
	 */

	public static boolean parse(Segment segment, PushbackInputStream pis)
			throws OBOEException, IOException {
		int i = 0;

		byte me[] = new byte[segment.getID().length()];
		DataElement currentDE;
		CompositeElement currentComposite;

		for (i = 1; i <= segment.myTemplate.getContainerSize(); i++) {

			if (segment.myTemplate.isTemplateComposite(i + 1)) {
				segment.buildElement(i + 1);
				currentComposite = (CompositeElement) segment.getElement(i);
				CompositeElementParser.parse(currentComposite, pis);

			} else if (segment.myTemplate.isTemplateDE(i)) {
				TemplateDataElement tde = (TemplateDataElement) segment.myTemplate
						.getTemplateElement(i);
				me = new byte[tde.getMaxLength()];
				pis.read(me);
				if (me[0] == 0) {
					continue;
				}
				String s = Util.rightTrim(new String(me));

				currentDE = (DataElement) segment.getElement(i);
				if (currentDE == null) {
					currentDE = (DataElement) segment.buildElement(i);
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

		return true;
	}

}
