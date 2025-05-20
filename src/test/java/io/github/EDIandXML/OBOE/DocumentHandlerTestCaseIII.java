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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Parsers.ValidXMLEDIParser;
import io.github.EDIandXML.OBOE.util.Util;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;
import junit.framework.TestCase;

public class DocumentHandlerTestCaseIII {
	static Logger logr = LogManager.getLogger(DocumentHandlerTestCaseIII.class);

	/**
	 * @see TestCase#setUp()
	 */
	@BeforeEach
	protected void setUp() throws Exception {
		EnvelopeFactory.reloadbuiltTable();
		try {
			Util.closeOBOEProperty();
			Files.copy(Paths.get("testFiles/UseVersion.oboe.properties.txt"),
					Paths.get("OBOE.properties"),
					StandardCopyOption.REPLACE_EXISTING);

		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(0);
		}
	}

	@AfterEach
	protected void tearDown() {
		try {
			Util.closeOBOEProperty();
			Files.copy(Paths.get("testFiles/reset.oboe.properties.txt"),
					Paths.get("OBOE.properties"),
					StandardCopyOption.REPLACE_EXISTING);

		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(0);
		}
	}

	@Test
	public void testX12ToXMLToX12() throws IOException {
		FileReader fr = new FileReader("testFiles/5-850.x12");

		BufferedReader br = new BufferedReader(fr);
		String instring = null;
		StringBuilder sb = new StringBuilder();
		do {
			instring = br.readLine();

			if (instring != null) {
				sb.append(instring + Envelope.X12_SEGMENT_DELIMITER);
			}

		} while (instring != null);

		String expected = sb.toString();
		fr.close();

		fr = new FileReader("testFiles/5-850.x12");
		X12DocumentHandler p1 = new X12DocumentHandler();
		try {
			p1.startParsing(fr);
		} catch (OBOEException oe1) {
			if (oe1.getDocumentErrors() == null) {
				oe1.printStackTrace();
			} else {
				DocumentErrors de = oe1.getDocumentErrors();
				for (int i = 0; i < de.getErrorCount(); i++) {
					logr.info(de.getErrorID(i) + " ");
					logr.info(de.getErrorCode(i) + " ");
					logr.info(de.getErrorPosition(i) + " ");
					logr.info(de.getErrorDescription(i) + " ");
					if (de.getContainer(i) instanceof Segment) {
						logr.info(((Segment) de.getContainer(i)).getID() + " "
								+ ((Segment) de.getContainer(i)).getName()
								+ Util.lineFeed);
					} else {
						logr.info(de.getContainer(i) + Util.lineFeed);
					}

				}
			}
		}
		fr.close();

		Envelope x = p1.getEnvelope();
		String actual = new String(
				x.getFormattedText(Format.VALID_XML_FORMAT).getBytes());

		DataOutputStream dos = new DataOutputStream(
				new FileOutputStream("testFiles/test.5-850.xml"));
		dos.writeBytes(actual);
		dos.close();
		ValidXMLEDIParser xmp = new ValidXMLEDIParser();

		try {
			xmp.parseFile("testFiles/test.5-850.xml");
		} catch (SAXException se) {
			logr.error(se.getMessage(), se);
			fail(se.getMessage());
		} catch (FileNotFoundException fe) {
			logr.error(fe.getMessage(), fe);
			fail(fe.getMessage());
		} catch (IOException ie)

		{
			logr.error(ie.getMessage(), ie);
			fail(ie.getMessage());
		}

		x = xmp.getEnvelope();
		x.validate();
		x.setDelimiters("\n~@");

		actual = new String(x.getFormattedText(Format.X12_FORMAT).getBytes());
		char fld = 29;
		char seg = 28;
		char grp = 31;
		expected = expected.trim();
		expected = expected.replaceAll(fld + "", "~");
		expected = expected.replaceAll(grp + "", "@");
		expected = expected.replaceAll(seg + "", "\n");
		assertEquals(expected, actual.trim());

	}

}