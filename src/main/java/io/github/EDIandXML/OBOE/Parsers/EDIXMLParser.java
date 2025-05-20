
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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.github.EDIandXML.OBOE.EnvelopeFactory;
import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.EDIFACT.EDIFACTEnvelope;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.TRADACOMS.TradacomsEnvelope;
import io.github.EDIandXML.OBOE.x12.X12Envelope;

/**
 * class builds OBOE objects by parsing input string in well-formed xml
 * edi format
 *
 */

public class EDIXMLParser {
	protected URL url = null;
	protected Envelope envelope;

	/**
	 * create a transaction set from input string
	 */
	static Logger logr = LogManager.getLogger(EDIXMLParser.class);

	public EDIXMLParser() {
	}

	/**
	 * builds a transaction set from input string
	 *
	 * @param inString the edi document
	 * @exception OBOEException - unknown transaction set, this transaction
	 *                          set is undefined to OBOE
	 */

	public void parse(String inString) {
		ByteArrayInputStream bais = new ByteArrayInputStream(
				inString.getBytes());
		parse(bais);
	}

	/**
	 * builds a transaction set from input file
	 *
	 * @param inString Filename of the edi document file name
	 * @exception FileNotFoundException
	 * @exception IOException
	 * @exception SAXException
	 */

	public void parseFile(String inString)
			throws FileNotFoundException, IOException, SAXException {
		FileInputStream fis = new FileInputStream(inString);
		parse(fis);
	}

	/**
	 * builds a transaction set by reading a URL <i>NOT USED</i>
	 *
	 * @param inURL URL of the edi file
	 * @param text  String of document
	 * @exception OBOEException - unknown transaction set, this transaction
	 *                          set is undefined to OBOE
	 */

	public void parse(URL inURL, String text) throws OBOEException {
		url = inURL;
		parse(text);
	}

	/**
	 * builds a transaction set from input stream <br>
	 * all the work is really done here, the other parse methods call this
	 *
	 * @param inStream InputStream the edi document in a stream
	 * @exception OBOEException <br>
	 *                          - continues to throw existing exception -
	 *                          unknown transaction set, this transaction
	 *                          set is undefined to OBOE
	 */

	public void parse(InputStream inStream) throws OBOEException {

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document document = db.parse(new InputSource(inStream));

			if (document.getNodeType() != Node.DOCUMENT_NODE) {
				throw new OBOEException(
						"unknown node type found, should be DOCUMENT_NODE");
			}

			NodeList nl = document.getChildNodes();
			String nName = "";
			int i;
			for (i = 0; i < nl.getLength(); i++) {
				nName = nl.item(i).getNodeName();

				if (nName.compareTo("#comment") != 0) {
					break;
				}
			}

			Format format = Envelope.getFormat(nl.item(i));

			if (format == Format.X12_FORMAT) {
				NodeList nlChild = nl.item(i).getChildNodes();
				String version = "";
				String name;
				for (int j = 0; j < nlChild.getLength(); j++) {
					name = nlChild.item(j).getNodeName();
					if (name.compareTo(
							"interchangeControlVersionNumber") == 0) {
						version = nl.item(j).getNodeValue();
					}
				}
				envelope = new X12Envelope(
						EnvelopeFactory.buildEnvelope("x12.envelope", version));
			} else if (format == Format.EDIFACT_FORMAT) {
				envelope = new EDIFACTEnvelope(EnvelopeFactory
						.buildEnvelope("EDIFACT.envelope", "notSetYet"));
			} else if (format == Format.TRADACOMS_FORMAT) {
				envelope = new TradacomsEnvelope(EnvelopeFactory
						.buildEnvelope("Tradacoms.envelope", "notSetYet"));
			} else {
				throw new OBOEException("Unknown envelope type");
			}

			envelope.parse(nl.item(i));

		} catch (Exception e) {
			e.printStackTrace();
			throw new OBOEException(e.getMessage());
		}

	}

	/**
	 * returns the Envelope that was parsed
	 *
	 * @return envelope object either X12Envelope or EDIFACTEnvelope
	 */
	public Envelope getEnvelope() {
		return envelope;
	}

}
