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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.DataElements.CharDE;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Parsers.EDIXMLParser;
import io.github.EDIandXML.OBOE.Parsers.ValidXMLEDIParser;
import io.github.EDIandXML.OBOE.util.Util;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;

/**
 * @author Joe McVerry
 *
 */
public class TestMultipleElements {

	static Logger logr = LogManager.getLogger(TestMultipleElements.class);

	@Test
	public void testTransactionSetFactory() {

		assertTrue(null != TransactionSetFactory
				.buildTransactionSet("4MultipleElementsTest"));
	}

	@Test
	public void testBuildingMulitples() {

		TransactionSet ts = TransactionSetFactory
				.buildTransactionSet("4MultipleElementsTest");
		Segment seg = ts.getHeaderTable().createSegment("MDE");
		ts.getHeaderTable().addSegment(seg);
		CharDE de = (CharDE) seg.buildElement(1);
		de.setNext("one");
		de.setNext("two");
		de.setNext("three");
		de.setNext("four");
		assertEquals(
				"MDE" + Envelope.PREBUILD_FIELD_DELIMITER + "one"
						+ Envelope.PREBUILD_REPEAT_DELIMITER + "two"
						+ Envelope.PREBUILD_REPEAT_DELIMITER + "three"
						+ Envelope.PREBUILD_REPEAT_DELIMITER + "four"
						+ Envelope.PREBUILD_SEGMENT_DELIMITER,
				seg.getFormattedText(Format.PREBUILD_FORMAT));
		assertEquals("one", de.get(0));
		assertEquals("two", de.get(1));
		assertEquals("three", de.get(2));
		assertEquals("four", de.get(3));
		assertEquals(4, de.getRepeatCount());

	}

	@Test
	public void testRepeatingNumericDEs() {

		String sEDI810Invoice = "ISA*00*          *00*          *ZZ*KOH1   KOH1174 *ZZ*NATIONSBANK    *011015*1350*%*00200*000000047*0*P*>\nGS*ZZ*KOH1174*IPPINV*20011015*1350*47*X*004010\nST*4MultipleElementsTest*47001\nMDE*63264%73264\nMCE*13264>23264%33245>43425\nSE*4*47001\nGE*1*47\nIEA*1*000000047\n";
		String sXmlInvoice;

		try {
			X12DocumentHandler p1 = new X12DocumentHandler(
					new StringReader(sEDI810Invoice));
			var env = p1.getEnvelope();
			env.setDelimiters("\n*>%");
			env.validate();
			sXmlInvoice = env.getFormattedText(Format.VALID_XML_FORMAT);

			TransactionSet ts = env.getFunctionalGroup(0).getTransactionSet(0);

			Segment seg = ts.getHeaderTable().getSegment("MDE");

			assertNotNull("MDE Missing");

			ValidXMLEDIParser xmp = new ValidXMLEDIParser();

			xmp.parse(sXmlInvoice);
			env = xmp.getEnvelope();
			env.setDelimiters("\n*>%");
			ts = env.getFunctionalGroup(0).getTransactionSet(0);
			seg = ts.getHeaderTable().getSegment("MDE");
			assertNotNull("MDE Missing");

			assertEquals(
					"MDE" + Envelope.PREBUILD_FIELD_DELIMITER + "63264"
							+ Envelope.PREBUILD_REPEAT_DELIMITER + "73264"
							+ Envelope.PREBUILD_SEGMENT_DELIMITER,
					seg.getFormattedText(Format.PREBUILD_FORMAT));

			seg = ts.getHeaderTable().getSegment("MCE");
			assertNotNull("MCE Missing");
			var got = seg.getFormattedText(Format.PREBUILD_FORMAT);
			assertEquals("MCE" + Envelope.PREBUILD_FIELD_DELIMITER + "13264"
					+ Envelope.PREBUILD_GROUP_DELIMITER + "23264"
					+ Envelope.PREBUILD_REPEAT_DELIMITER + "33245"
					+ Envelope.PREBUILD_GROUP_DELIMITER + "43425"
					+ Envelope.PREBUILD_SEGMENT_DELIMITER, got);

			assertEquals(sEDI810Invoice,
					env.getFormattedText(Format.X12_FORMAT));

			/* do the wf xml format */

			sXmlInvoice = env.getFormattedText(Format.XML_FORMAT);

			EDIXMLParser exp = new EDIXMLParser();
			Util.setOBOEProperty("Invoice", "4MultipleElementsTest");
			exp.parse(sXmlInvoice);
			env = exp.getEnvelope();
			ts = env.getFunctionalGroup(0).getTransactionSet(0);
			seg = ts.getHeaderTable().getSegment("MDE");
			assertNotNull("MDE Missing");
			assertEquals(
					"MDE" + Envelope.PREBUILD_FIELD_DELIMITER + "63264"
							+ Envelope.PREBUILD_REPEAT_DELIMITER + "73264"
							+ Envelope.PREBUILD_SEGMENT_DELIMITER,
					seg.getFormattedText(Format.PREBUILD_FORMAT));

			seg = ts.getHeaderTable().getSegment("MCE");
			assertNotNull("MCE Missing");
			assertEquals(
					"MCE" + Envelope.PREBUILD_FIELD_DELIMITER + "13264"
							+ Envelope.PREBUILD_GROUP_DELIMITER + "23264"
							+ Envelope.PREBUILD_REPEAT_DELIMITER + "33245"
							+ Envelope.PREBUILD_GROUP_DELIMITER + "43425"
							+ Envelope.PREBUILD_SEGMENT_DELIMITER,
					seg.getFormattedText(Format.PREBUILD_FORMAT));

			env.setDelimiters("\n*>%");
			assertEquals(sEDI810Invoice,
					env.getFormattedText(Format.X12_FORMAT));

		} catch (SAXException se) {
			logr.info(se.getMessage());
			logr.error(se.getMessage(), se);
			fail(se.getMessage());
		} catch (FileNotFoundException fe) {
			logr.info(fe.getMessage());
			logr.error(fe.getMessage(), fe);
			fail(fe.getMessage());
		} catch (OBOEException oe) {
			if (oe.getMessage().compareTo("Parsing Errors.") == 0) {
				DocumentErrors de = oe.getDocumentErrors();
				for (int i = 0; i < de.getErrorCount(); i++) {
					logr.info(de.getErrorID(i) + " ");
					logr.info(de.getErrorPosition(i) + " ");
					logr.info(de.getErrorDescription(i) + " ");
					logr.info(de.getContainer(i).getID());
				}
			}

			logr.error(oe.getMessage(), oe);
			fail(oe.getMessage());
		} catch (IOException ie) {
			logr.info(ie.getMessage());
			logr.error(ie.getMessage(), ie);
			fail(ie.getMessage());
		} catch (Exception e) {
			logr.info(e.getMessage());
			logr.error(e.getMessage(), e);
			fail(e.getMessage());
		}

	}

}
