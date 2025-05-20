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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Loop;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.Table;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.EDIFACT.EDIFACTDocumentHandler;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Parsers.EDIXMLParser;
import io.github.EDIandXML.OBOE.Parsers.ValidXMLEDIParser;
import io.github.EDIandXML.OBOE.TRADACOMS.TradacomsDocumentHandler;
import io.github.EDIandXML.OBOE.util.Util;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;
import io.github.EDIandXML.OBOE.x12.X12Envelope;

public class DocumentHandlerTestCase {
	static Logger logr = LogManager.getLogger(DocumentHandlerTestCase.class);

	@BeforeEach
	protected void setUp() {
		EnvelopeFactory.reloadbuiltTable();
		try {
			Files.copy(Paths.get("testFiles/reset.oboe.properties.txt"),
					Paths.get("OBOE.properties"),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (OBOEException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	@AfterEach
	protected void tearDown() {

		try {
			Files.copy(Paths.get("testFiles/reset.oboe.properties.txt"),
					Paths.get("OBOE.properties"),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (OBOEException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	@Test
	@Order(1)
	public void testX12() throws IOException {
		FileReader fr = new FileReader("testFiles/sample.output.840.1");

		BufferedReader br = new BufferedReader(fr);
		String instring = null;
		StringBuilder sb = new StringBuilder();
		do {
			instring = br.readLine();
			if (instring != null) {
				sb.append(instring + '\n');
			}

		} while (instring != null);

		String expected = sb.toString();
		fr.close();

		fr = new FileReader("testFiles/sample.output.840.1");

		X12DocumentHandler p1;

		try {
			p1 = new X12DocumentHandler(fr);
		} catch (OBOEException oe) {
			if (oe.getDocumentErrors() != null) {
				oe.getDocumentErrors().logErrors();
			} else {
				logr.error(oe.getMessage(), oe);
				fail(oe.getMessage());
			}

			return;
		}

		fr.close();

		Envelope x = p1.getEnvelope();

		assertEquals(expected,
				new String(x.getFormattedText(Format.X12_FORMAT).getBytes()));

	}

	@Test
	@Order(2)
	public void testX12ToXMLToX12() throws IOException {
		FileReader fr = new FileReader("testFiles/sample.output.840.1");

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

		fr = new FileReader("testFiles/sample.output.840.1");

		X12DocumentHandler p1 = new X12DocumentHandler(fr);
		fr.close();

		Envelope x = p1.getEnvelope();
		String workString = new String(
				x.getFormattedText(Format.VALID_XML_FORMAT).getBytes());

		DataOutputStream dos = new DataOutputStream(
				new FileOutputStream("testFiles/test.X12.xml"));
		dos.writeBytes(workString);
		dos.close();
		ValidXMLEDIParser xmp = new ValidXMLEDIParser();

		try {
			xmp.parseFile("testFiles/test.X12.xml");
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

		assertEquals(expected,
				new String(x.getFormattedText(Format.X12_FORMAT).getBytes()));

	}

	@Test
	@Order(3)
	public void testX12LFToXMLToX12LF() throws IOException {
		FileReader fr = new FileReader("testFiles/lfTestCase.edi");

		BufferedReader br = new BufferedReader(fr);
		String instring = null;
		StringBuilder sb = new StringBuilder();
		do {
			instring = br.readLine();
			if (instring != null) {
				sb.append(instring + "");
			}

		} while (instring != null);

		String expected = sb.toString();
		fr.close();

		fr = new FileReader("testFiles/lfTestCase.edi");

		X12DocumentHandler p1 = new X12DocumentHandler(fr);
		fr.close();

		X12Envelope x = (X12Envelope) p1.getEnvelope();
		String workString = new String(
				x.getFormattedText(Format.VALID_XML_FORMAT).getBytes());

		DataOutputStream dos = new DataOutputStream(
				new FileOutputStream("testFiles/test.X12.xml"));
		dos.writeBytes(workString);
		dos.close();
		ValidXMLEDIParser xmp = new ValidXMLEDIParser();

		try {
			xmp.parseFile("testFiles/test.X12.xml");
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

		x = (X12Envelope) xmp.getEnvelope();
		x.validate();

		x.setDelimiters("~>");

		StringWriter sw = new StringWriter();
		x.writeFormattedText(sw, Format.X12_FORMAT);
		sw.close();

		assertEquals(expected, sw.toString());

	}

	@Test
	@Order(4)

	public void testX12ToWellFormedXMLToX12() throws IOException {
		FileReader fr = new FileReader("testFiles/sample.output.840.1");

		String instring = null;
		StringBuilder sb = new StringBuilder();

		BufferedReader br = new BufferedReader(fr);
		do {
			instring = br.readLine();
			if (instring != null) {
				sb.append(instring + '\n');
			}

		} while (instring != null);

		String expected = sb.toString();
		fr.close();

		fr = new FileReader("testFiles/sample.output.840.1");

		X12DocumentHandler p1 = new X12DocumentHandler(fr);

		fr.close();

		Envelope x = p1.getEnvelope();
		String workString = new String(
				x.getFormattedText(Format.XML_FORMAT).getBytes());

		DataOutputStream dos = new DataOutputStream(
				new FileOutputStream("testFiles/test.X12.wf.xml"));
		dos.writeBytes(workString.trim());
		dos.close();
		EDIXMLParser xmp = new EDIXMLParser();

		try {
			xmp.parseFile("testFiles/test.X12.wf.xml");
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

		assertEquals(expected,
				new String(x.getFormattedText(Format.X12_FORMAT).getBytes()));

	}

	@Test
	@Order(6)
	public void testX12JustTA1ToValidXMLToX12JustTA1() throws IOException {
		FileReader fr = new FileReader("testFiles/justTA1TestCase.edi");

		BufferedReader br = new BufferedReader(fr);
		String instring = null;
		StringBuilder sb = new StringBuilder();
		do {
			instring = br.readLine();
			if (instring != null) {
				sb.append(instring + "");
			}

		} while (instring != null);

		String expected = sb.toString();
		fr.close();

		fr = new FileReader("testFiles/justTA1TestCase.edi");

		X12DocumentHandler p1 = new X12DocumentHandler(fr);
		fr.close();

		X12Envelope x = (X12Envelope) p1.getEnvelope();

		String dlm = "" + x.fieldDelimiter + x.groupDelimiter;

		String workString = new String(
				x.getFormattedText(Format.VALID_XML_FORMAT).getBytes());

		DataOutputStream dos = new DataOutputStream(
				new FileOutputStream("testFiles/testJustTA1.X12.xml"));
		dos.writeBytes(workString);
		dos.close();
		ValidXMLEDIParser xmp = new ValidXMLEDIParser();

		try {
			xmp.parseFile("testFiles/testJustTA1.X12.xml");
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

		x = (X12Envelope) xmp.getEnvelope();
		x.validate();
		x.setDelimiters(dlm);

		StringWriter sw = new StringWriter();
		x.writeFormattedText(sw, Format.X12_FORMAT);
		sw.close();

		assertEquals(expected, sw.toString());

	}

	@Test
	@Order(7)
	public void testX12WithTA1ToValidXMLToX12WithTA1() throws IOException {
		FileReader fr = new FileReader("testFiles/withTA1TestCase.edi");

		BufferedReader br = new BufferedReader(fr);
		String instring = null;
		StringBuilder sb = new StringBuilder();
		do {
			instring = br.readLine();
			if (instring != null) {
				sb.append(instring + "");
			}

		} while (instring != null);

		String expected = sb.toString();
		fr.close();

		fr = new FileReader("testFiles/withTA1TestCase.edi");

		X12DocumentHandler p1 = new X12DocumentHandler(fr);
		fr.close();

		X12Envelope x = (X12Envelope) p1.getEnvelope();

		String dlm = "" + x.fieldDelimiter + x.groupDelimiter;

		String workString = new String(
				x.getFormattedText(Format.VALID_XML_FORMAT).getBytes());

		DataOutputStream dos = new DataOutputStream(
				new FileOutputStream("testFiles/testWithTA1.X12.xml"));
		dos.writeBytes(workString);
		dos.close();
		ValidXMLEDIParser xmp = new ValidXMLEDIParser();

		try {
			xmp.parseFile("testFiles/testWithTA1.X12.xml");
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

		x = (X12Envelope) xmp.getEnvelope();
		x.validate();

		StringWriter sw = new StringWriter();

		x.setDelimiters(dlm);

		x.writeFormattedText(sw, Format.X12_FORMAT);
		sw.close();

		assertEquals(expected, sw.toString());

	}

	@Test
	@Order(8)

	public void testX12JustTA1ToWellFormedXMLToX12JustTA1() throws IOException {
		FileReader fr = new FileReader("testFiles/justTA1TestCase.edi");

		BufferedReader br = new BufferedReader(fr);
		String instring = null;
		StringBuilder sb = new StringBuilder();
		do {
			instring = br.readLine();
			if (instring != null) {
				sb.append(instring + "");
			}

		} while (instring != null);

		String exptected = sb.toString();
		fr.close();

		fr = new FileReader("testFiles/justTA1TestCase.edi");

		X12DocumentHandler p1 = new X12DocumentHandler(fr);
		fr.close();

		X12Envelope x = (X12Envelope) p1.getEnvelope();

		String dlm = "" + x.fieldDelimiter + x.groupDelimiter;

		String workString = new String(
				x.getFormattedText(Format.XML_FORMAT).getBytes());

		DataOutputStream dos = new DataOutputStream(
				new FileOutputStream("testFiles/testJustTA1.wfX12.xml"));
		dos.writeBytes(workString);
		dos.close();
		EDIXMLParser xmp = new EDIXMLParser();

		try {
			xmp.parseFile("testFiles/testJustTA1.wfX12.xml");
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

		x = (X12Envelope) xmp.getEnvelope();
		x.validate();
		x.setDelimiters(dlm);

		StringWriter sw = new StringWriter();
		x.writeFormattedText(sw, Format.X12_FORMAT);
		sw.close();

		assertEquals(exptected, sw.toString());

	}

	@Test
	@Order(9)

	public void testX12WithTA1ToWellFormedXMLToX12WithTA1() throws IOException {
		FileReader fr = new FileReader("testFiles/withTA1TestCase.edi");

		BufferedReader br = new BufferedReader(fr);
		String instring = null;
		StringBuilder sb = new StringBuilder();
		do {
			instring = br.readLine();
			if (instring != null) {
				sb.append(instring + "");
			}

		} while (instring != null);

		String expected = sb.toString();
		fr.close();

		fr = new FileReader("testFiles/withTA1TestCase.edi");

		X12DocumentHandler p1 = new X12DocumentHandler(fr);
		fr.close();

		X12Envelope x = (X12Envelope) p1.getEnvelope();

		String dlm = "" + x.fieldDelimiter + x.groupDelimiter;

		String workString = new String(
				x.getFormattedText(Format.XML_FORMAT).getBytes());

		DataOutputStream dos = new DataOutputStream(
				new FileOutputStream("testFiles/testWithTA1.wfX12.xml"));
		dos.writeBytes(workString);
		dos.close();
		EDIXMLParser xmp = new EDIXMLParser();

		try {
			xmp.parseFile("testFiles/testWithTA1.wfX12.xml");
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

		x = (X12Envelope) xmp.getEnvelope();
		x.validate();

		StringWriter sw = new StringWriter();

		x.setDelimiters(dlm);

		x.writeFormattedText(sw, Format.X12_FORMAT);
		sw.close();

		assertEquals(expected, sw.toString());

	}

	@Test
	@Order(10)

	public void testValidXMLParser() throws IOException {

		try {
			ValidXMLEDIParser xmp = new ValidXMLEDIParser();
			xmp.parseFile("testFiles/test.X12.xml");
			String t = xmp.getEnvelope().getFormattedText(Format.X12_FORMAT);

			xmp = null;

			StringReader sr = new StringReader(t);

			X12DocumentHandler p1 = new X12DocumentHandler(sr);

			String s = p1.getEnvelope().getFormattedText(Format.X12_FORMAT);

			assertEquals(s, t);
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

	}

	@Test
	@Order(11)

	public void testX12ValidXMLParser() throws IOException {
		FileReader fr = new FileReader("testFiles/sample.output.840.1");
		X12DocumentHandler p1 = new X12DocumentHandler(fr);

		fr.close();

		String expected = p1.getEnvelope()
				.getFormattedText(Format.VALID_XML_FORMAT);

		DataOutputStream dos = new DataOutputStream(
				new FileOutputStream("testFiles/test.X12.ts.xml"));
		dos.writeBytes(expected);
		dos.close();
		ValidXMLEDIParser xmp = new ValidXMLEDIParser();

		try {
			xmp.parseFile("testFiles/test.X12.ts.xml");
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

		assertEquals(expected, new String(xmp.getEnvelope()
				.getFormattedText(Format.VALID_XML_FORMAT).getBytes()));

	}

	@Test
	@Order(12)

	public void testX12WellFormedXMLParser() throws IOException {
		FileReader fr = new FileReader("testFiles/sample.output.840.1");

		X12DocumentHandler p1 = new X12DocumentHandler(fr);
		fr.close();

		Envelope x = p1.getEnvelope();
		String expected = new String(
				x.getFormattedText(Format.XML_FORMAT).getBytes());

		DataOutputStream dos = new DataOutputStream(
				new FileOutputStream("testFiles/test.X12.tswf.xml"));
		dos.writeBytes(expected);
		dos.close();

		try {
			EDIXMLParser exmp = new EDIXMLParser();
			exmp.parseFile("testFiles/test.X12.tswf.xml");
			x = exmp.getEnvelope();
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

		String workString = new String(
				x.getFormattedText(Format.XML_FORMAT).getBytes());
		dos = new DataOutputStream(
				new FileOutputStream("testFiles/test.X12.tswfII.xml"));
		dos.writeBytes(workString);
		dos.close();

		assertEquals(expected, workString);

	}

	@Test
	@Order(13)

	public void testX12WellFormedSpacesISAXMLParser() throws IOException {

		Envelope x = null, y = null;

		try {
			EDIXMLParser exmp = new EDIXMLParser();
			exmp.parseFile("testFiles/test.X12.tswf.xml");
			x = exmp.getEnvelope();
			exmp.parseFile("testFiles/test.X12.wf.withSpacesInISA.xml");
			y = exmp.getEnvelope();
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

		String tx = new String(
				x.getFormattedText(Format.X12_FORMAT).getBytes());
		String ty = new String(
				y.getFormattedText(Format.X12_FORMAT).getBytes());

		assertEquals(tx, ty);

	}

	@Test
	@Order(14)

	public void testEDIFACT() throws IOException {
		FileReader fr = new FileReader("testFiles/orders");

		BufferedReader br = new BufferedReader(fr);

		String exptected = br.readLine();

		fr.close();

		fr = new FileReader("testFiles/orders");

		EDIFACTDocumentHandler p1 = null;
		try {
			p1 = new EDIFACTDocumentHandler(fr);
		} catch (OBOEException oe) {
			logr.info(oe.getMessage());
			oe.getDocumentErrors().logErrors();
			fail("FAILED " + oe.getMessage());
		}
		fr.close();

		Envelope x = p1.getEnvelope();

		assertEquals(exptected, new String(
				x.getFormattedText(Format.EDIFACT_FORMAT).getBytes()));

	}

	@Test
	@Order(15)

	public void testEDIFACTWithBadUNBSegment() throws IOException {
		Util.setOBOEProperty(Util.THROW_PARSING_EXCEPTION, "true");
		FileReader fr = new FileReader("testFiles/orders.withbadunbsegment");

		try {
			new EDIFACTDocumentHandler(fr);
		} catch (OBOEException oe) {
			StringWriter sw = new StringWriter();
			try {
				oe.getDocumentErrors().writeErrors(sw);
				sw.flush();
				assertTrue(
						sw.toString().contains("UNB 4 2 Invalid token length"));
			} catch (IOException e) {
				fail(e.getLocalizedMessage());
				e.printStackTrace();
			}

			return;
		}
		fr.close();
		fail("FAILED: expecting failure");

	}

	@Test
	@Order(16)

	public void testEDIFACTToXMLToEDIFACT() throws IOException {
		FileReader fr = new FileReader("testFiles/orders");

		BufferedReader br = new BufferedReader(fr);

		String expected = br.readLine();

		fr.close();

		fr = new FileReader("testFiles/orders");

		EDIFACTDocumentHandler p1 = new EDIFACTDocumentHandler(fr);
		Envelope x = p1.getEnvelope();
		fr.close();

		String workString = new String(
				x.getFormattedText(Format.VALID_XML_FORMAT).getBytes());

		DataOutputStream dos = new DataOutputStream(
				new FileOutputStream("testFiles/test.EDIFACT.xml"));
		dos.writeBytes(workString);
		dos.close();
		ValidXMLEDIParser xmp = new ValidXMLEDIParser();

		try {
			xmp.parseFile("testFiles/test.EDIFACT.xml");
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

		assertEquals(expected, new String(
				x.getFormattedText(Format.EDIFACT_FORMAT).getBytes()));

	}

	@Test
	@Order(17)

	public void testEDIFACTToWellFormedXMLToEDIFACT() throws IOException {
		FileReader fr = new FileReader("testFiles/orders");

		BufferedReader br = new BufferedReader(fr);

		String workString = br.readLine();

		fr.close();

		fr = new FileReader("testFiles/orders");

		EDIFACTDocumentHandler p1 = new EDIFACTDocumentHandler(fr);
		Envelope x = p1.getEnvelope();
		fr.close();

		String expected = new String(
				x.getFormattedText(Format.XML_FORMAT).getBytes());

		DataOutputStream dos = new DataOutputStream(
				new FileOutputStream("testFiles/test.EDIFACT.wf.xml"));
		dos.writeBytes(expected);
		dos.close();
		EDIXMLParser xmp = new EDIXMLParser();

		try {
			xmp.parseFile("testFiles/test.EDIFACT.wf.xml");
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

		expected = new String(
				x.getFormattedText(Format.EDIFACT_FORMAT).getBytes());

		assertEquals(workString, expected);

	}

	@Test
	@Order(18)

	public void testEscapeEDIFACTToXMLToEscapeEDIFACT() throws IOException {
		FileReader fr = new FileReader("testFiles/orders.escape");

		BufferedReader br = new BufferedReader(fr);

		String expected = br.readLine();

		fr.close();

		fr = new FileReader("testFiles/orders.escape");

		EDIFACTDocumentHandler p1 = null;

		try {
			p1 = new EDIFACTDocumentHandler(fr);
		} catch (OBOEException oe) {
			if (oe.getDocumentErrors() != null) {
				oe.getDocumentErrors().logErrors();
			} else {
				logr.error(oe.getMessage(), oe);
			}

			return;
		}
		Envelope x = p1.getEnvelope();
		fr.close();

		String workString = new String(
				x.getFormattedText(Format.VALID_XML_FORMAT).getBytes());

		DataOutputStream dos = new DataOutputStream(
				new FileOutputStream("testFiles/test.EDIFACT.xml"));
		dos.writeBytes(workString);
		dos.close();
		ValidXMLEDIParser xmp = new ValidXMLEDIParser();

		try {
			xmp.parseFile("testFiles/test.EDIFACT.xml");
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

		assertEquals(expected, new String(
				x.getFormattedText(Format.EDIFACT_FORMAT).getBytes()));

	}

	@Test
	@Order(19)

	public void testX12Build() throws OBOEException {

		StringBuilder sb = new StringBuilder();
		sb.append(
				"ISA*Y1*AUTH      *Y3*SEC       *Y5*SENDERID       *Y7*RECEIVERID     *010101*1300*Y*Y12  *000000001*x*z*<\n");
		sb.append("GS*fg0*fg1*fg2*260203****\n");
		sb.append("ST*840*00001\n");
		sb.append("BQT*363*586*20260103*200\n");
		sb.append("PO1*PO1350*1*12*1232\n");
		sb.append("LIN*LIN350*2 *2-350\n");
		sb.append("G53*875\n");
		sb.append("CTT*1*1\n");
		sb.append("SE*1*00001\n");
		sb.append("GE*1*1\n");
		sb.append("IEA*1*000000001\n");

		assertEquals(sb.toString(), new String(
				b840(null).getFormattedText(Format.X12_FORMAT).getBytes()));
	}

	@Test
	@Order(20)

	public void testX12BuildWithDelimiters() throws OBOEException {

		StringBuilder sb = new StringBuilder();
		sb.append(
				"ISA|Y1|AUTH      |Y3|SEC       |Y5|SENDERID       |Y7|RECEIVERID     |010101|1300|Y|Y12  |000000001|x|z|<$");
		sb.append("GS|fg0|fg1|fg2|260203||||$");
		sb.append("ST|840|00001$");
		sb.append("BQT|363|586|20260103|200$");
		sb.append("PO1|PO1350|1|12|1232$");
		sb.append("LIN|LIN350|2 |2-350$");
		sb.append("G53|875$");
		sb.append("CTT|1|1$");
		sb.append("SE|1|00001$");
		sb.append("GE|1|1$");
		sb.append("IEA|1|000000001$");

		assertEquals(sb.toString(), new String(
				b840("$|<^%").getFormattedText(Format.X12_FORMAT).getBytes()));
	}

	@Test
	@Order(21)

	public void testWellFormedXMLToWellFormedXML() throws IOException {
		FileReader fr = new FileReader("testFiles/test.X12.wf.xml");

		String instring = null;
		StringBuilder sb = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();

		BufferedReader br = new BufferedReader(fr);
		do {
			instring = br.readLine();
			if (instring != null) {
				sb.append(instring);
				sb2.append(instring + Util.lineFeed);
			}

		} while (instring != null);

		String expected = sb.toString();
		fr.close();

		EDIXMLParser xmp = new EDIXMLParser();

		// try {

		xmp.parse(expected);
		expected = new String(sb2);
		/*
		 * } catch(SAXException se) { logr.error(se.getMessage(), se);
		 * fail(se.getMessage()); } catch(FileNotFoundException fe) {
		 * logr.error(fe.getMessage(), fe); fail(fe.getMessage()); }
		 * catch(IOException ie)
		 *
		 * { logr.error(ie.getMessage(), ie); fail(ie.getMessage()); }
		 */
		Envelope x = xmp.getEnvelope();

		assertEquals(expected,
				new String(x.getFormattedText(Format.XML_FORMAT).getBytes()));

	}

	public Envelope b840(String delimiters) throws OBOEException {
		X12Envelope env = new X12Envelope(
				EnvelopeFactory.buildEnvelope("x12.envelope", "not set yet"));

		/**
		 * add code here to work with the headers and other envelope control
		 * segments
		 */
		Segment interchange_Control_Header = env.createInterchange_Header();
		env.addSegment(interchange_Control_Header);
		// env.setInterchange_Header(interchange_Control_Header);
		interchange_Control_Header.setDataElementValue(1, "Y1");// auth. info.
		// qual 2-2
		interchange_Control_Header.setDataElementValue(2, "AUTH");// auth info
		// 10-10
		interchange_Control_Header.setDataElementValue(3, "Y3");// sec info qual
		// 2-2
		interchange_Control_Header.setDataElementValue(4, "SEC");// sec info
		// 10-10
		interchange_Control_Header.setDataElementValue(5, "Y5");// int sender
		// info qual 2-2
		interchange_Control_Header.setDataElementValue(6, "SENDERID");// int
		// sender
		// info
		// 15-15
		interchange_Control_Header.setDataElementValue(7, "Y7");// int id qual
		// 2-2
		interchange_Control_Header.setDataElementValue(8, "RECEIVERID");// int
																		// id
		// 15-15
		interchange_Control_Header.setDataElementValue(9, "010101");// int dt
																	// 6-6
		interchange_Control_Header.setDataElementValue(10, "1300");// int tm 4-4
		interchange_Control_Header.setDataElementValue(11, "Y");// int cntr std
		// id 1-1
		interchange_Control_Header.setDataElementValue(12, "Y12");// int cntrl
		// ver 5-5
		interchange_Control_Header.setDataElementValue(13, "000000001");// int
		// cntrl number 9-9
		interchange_Control_Header.setDataElementValue(14, "x");// ack req 1-1
		interchange_Control_Header.setDataElementValue(15, "z");// test ind 1-1
		interchange_Control_Header.setDataElementValue(16, "<");// composite
		// delimiter

		env.setDelimitersInHeader();

		FunctionalGroup fg = env.createFunctionalGroup();
		TransactionSet ts = TransactionSetFactory.buildTransactionSet("840");

		Segment fgHeader = fg.buildHeaderSegment();

		fgHeader.setDataElementValue(1, "fg0");
		fgHeader.setDataElementValue(2, "fg1");
		fgHeader.setDataElementValue(3, "fg2");
		fgHeader.setDataElementValue(4, "260203");

		env.addFunctionalGroup(fg);
		fg.addTransactionSet(ts);

		Table table;
		table = ts.getHeaderTable();
		buildHeaderST(table);
		buildHeaderBQT(table);
		table = ts.getDetailTable();
		buildDetailPO1(table);
		table = ts.getSummaryTable();
		buildSummaryCTT(table);
		buildSummarySE(table);

		Segment fgTrailer = fg.buildTrailerSegment();

		fg.setCountInTrailer();
		fgTrailer.setDataElementValue(2, "1");

		Segment interchange_Control_Trailer = env.createInterchange_Trailer();
		env.addSegment(interchange_Control_Trailer);
		env.setFGCountInTrailer();
		interchange_Control_Trailer.setDataElementValue(2, "000000001");

		if (delimiters != null) {
			env.setDelimiters(delimiters);
		}

		return env;

	}

	public void buildHeaderST(Table inTable) throws OBOEException {
		Segment segment = inTable.createSegment("ST");
		inTable.addSegment(segment);
		DataElement de;

		de = (DataElement) segment.buildElement("143");
		// 143 Transaction Set Identifier Code
		de.set("840");
		de = (DataElement) segment.buildElement("329"); // 329 Transaction Set
														// Control Number
		de.set("00001");
	}

	/**
	 * builds segment BQT that is part of the Header <br>
	 * Beginning Segment for Request for Quotation used <br>
	 * To indicate the beginning of a Request for Quotation Transaction Set
	 * and transmit identifying numbers and dates param inTable table
	 * containing this segment throws OBOEException - most likely segment
	 * not found
	 */
	public void buildHeaderBQT(Table inTable) throws OBOEException {
		Segment segment = inTable.createSegment("BQT");
		inTable.addSegment(segment);
		DataElement de;
		de = (DataElement) segment.buildElement("353"); // 353 Transaction Set
														// Purpose Code
		de.set("363");
		de = (DataElement) segment.buildElement("586"); // 586 Request for Quote
		// Reference Number
		de.set("586");
		de = (DataElement) segment.buildElement("373"); // 373 Date
		de.set("20260103");
		de = (DataElement) segment.buildElement("374"); // 374 Date/Time
														// Qualifier
		de.set("200");
	}

	public void buildDetailPO1(Table inTable) throws OBOEException {

		Loop l = inTable.createAndAddLoop("PO1");
		Segment segment = l.createAndAddSegment("PO1");

		buildDetailPO1LIN(l);
		buildDetailPO1G53(l);
		DataElement de;
		de = (DataElement) segment.buildElement("350"); // 350 Assigned
		// Identification
		de.set("PO1350");
		de = (DataElement) segment.buildElement("330"); // 330 Quantity Ordered
		de.set("1");
		de = (DataElement) segment.buildElement("355"); // 355 Unit or Basis for
		// Measurement Code
		de.set("12");
		de = (DataElement) segment.buildElement("212"); // 212 Unit Price
		de.set("1232");
	}

	public void buildDetailPO1LIN(Loop inLoop) throws OBOEException {

		Segment segment = inLoop.createSegment("LIN");
		inLoop.addSegment(segment);
		DataElement de;
		de = (DataElement) segment.buildElement("350");
		// 350 Assigned Identification
		de.set("LIN350");
		de = (DataElement) segment.buildElement("235");
		// 235 Product/Service ID Qualifier
		de.set("2");
		de = (DataElement) segment.buildElement("234");
		// 234 Product/Service ID
		de.set("2-350");
	}

	/**
	 * builds segment G53 that is part of the DetailPO1 <br>
	 * Maintenance Type used <br>
	 * To identify the specific type of item maintenance param inSegment
	 * segment containing this subsegment throws OBOEException - most likely
	 * segment not found
	 */
	public void buildDetailPO1G53(Loop inLoop) throws OBOEException {
		Segment segment = inLoop.createSegment("G53");
		inLoop.addSegment(segment);
		DataElement de;
		de = (DataElement) segment.buildElement("875"); // 875 Maintenance Type
														// Code
		de.set("875");
	}

	public void buildSummaryCTT(Table inTable) throws OBOEException {
		Segment segment = inTable.createSegment("CTT");
		inTable.addSegment(segment);
		DataElement de;
		de = (DataElement) segment.buildElement("354"); // 354 Number of Line
														// Items
		de.set("1");
		de = (DataElement) segment.buildElement("347"); // 347 Hash Total
		de.set("1");
	}

	/**
	 * builds segment SE that is part of the Summary <br>
	 * Transaction Set Trailer used <br>
	 * To indicate the end of the transaction set and provide the count of
	 * the transmitted segments (including the beginning (ST) and ending
	 * (SE) segments) param inTable table containing this segment throws
	 * OBOEException - most likely segment not found
	 */
	public void buildSummarySE(Table inTable) throws OBOEException {
		Segment segment = inTable.createSegment("SE");
		inTable.addSegment(segment);
		DataElement de;
		de = (DataElement) segment.buildElement("96"); // 96 Number of Included
														// Segments
		de.set("1");
		de = (DataElement) segment.buildElement("329"); // 329 Transaction Set
		// Control
		// Number
		de.set("00001");
	}

	@Test
	public void testTRADACOMS() throws IOException {
		FileReader fr = new FileReader("testFiles/Tradacoms.ORDER");

		BufferedReader br = new BufferedReader(fr);
		String instring = null;
		instring = br.readLine();
		if (instring == null) {
			fail("missing all data from Tradacoms.ORDER");
		}

		String expected = new String(instring);
		fr.close();

		fr = new FileReader("testFiles/Tradacoms.ORDER");

		TradacomsDocumentHandler p1;

		try {
			p1 = new TradacomsDocumentHandler(fr);
		} catch (OBOEException oe) {
			if (oe.getDocumentErrors() != null) {
				oe.getDocumentErrors().logErrors();
			} else {
				logr.error(oe.getMessage(), oe);
			}

			return;
		}

		fr.close();

		Envelope x = p1.getEnvelope();
		assertEquals(expected, new String(
				x.getFormattedText(Format.TRADACOMS_FORMAT).getBytes()));

	}

	@Test
	public void testTRADACOMSToXMLToTRADACOMS() throws IOException {
		FileReader fr = new FileReader("testFiles/Tradacoms.ORDER");

		BufferedReader br = new BufferedReader(fr);
		String instring = null;
		instring = br.readLine();
		if (instring == null) {
			fail("missing all data from Tradacoms.ORDER");
		}

		String expected = new String(instring);

		fr.close();

		fr = new FileReader("testFiles/Tradacoms.ORDER");

		TradacomsDocumentHandler p1 = new TradacomsDocumentHandler(fr);
		fr.close();

		Envelope x = p1.getEnvelope();
		String workString = new String(
				x.getFormattedText(Format.VALID_XML_FORMAT).getBytes());

		DataOutputStream dos = new DataOutputStream(
				new FileOutputStream("testFiles/test.TRADACOMS.xml"));
		dos.writeBytes(workString);
		dos.close();
		ValidXMLEDIParser xmp = new ValidXMLEDIParser();

		try {
			xmp.parseFile("testFiles/test.TRADACOMS.xml");
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

		logr.info(x.getFormattedText(Format.TRADACOMS_FORMAT));

		assertEquals(expected, new String(
				x.getFormattedText(Format.TRADACOMS_FORMAT).getBytes()));

	}

	@Test
	public void testTRADACOMSToWellFormedXMLToTRADACOMS() throws IOException {
		FileReader fr = new FileReader("testFiles/Tradacoms.ORDER");

		String instring = null;

		BufferedReader br = new BufferedReader(fr);
		instring = br.readLine();
		if (instring == null) {
			fail("missing all data from Tradacoms.ORDER");
		}

		String expected = new String(instring);

		fr.close();

		fr = new FileReader("testFiles/Tradacoms.ORDER");

		TradacomsDocumentHandler p1 = new TradacomsDocumentHandler(fr);

		fr.close();

		Envelope x = p1.getEnvelope();
		String workString = new String(
				x.getFormattedText(Format.XML_FORMAT).getBytes());

		DataOutputStream dos = new DataOutputStream(
				new FileOutputStream("testFiles/test.TRADACOMS.wf.xml"));
		dos.writeBytes(workString);
		dos.close();
		EDIXMLParser xmp = new EDIXMLParser();

		try {
			xmp.parseFile("testFiles/test.TRADACOMS.wf.xml");
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

		assertEquals(expected, new String(
				x.getFormattedText(Format.TRADACOMS_FORMAT).getBytes()));

	}

}