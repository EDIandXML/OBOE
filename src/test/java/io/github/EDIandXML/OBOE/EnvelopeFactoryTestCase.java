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

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.EDIFACT.EDIFACTDocumentHandler;
import io.github.EDIandXML.OBOE.EDIFACT.EDIFACTEnvelope;
import io.github.EDIandXML.OBOE.TRADACOMS.TradacomsEnvelope;
import io.github.EDIandXML.OBOE.util.Util;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;
import io.github.EDIandXML.OBOE.x12.X12Envelope;

public class EnvelopeFactoryTestCase {

	@Test
	public void testDelimiters() {
		assertEquals("\n", Envelope.X12_SEGMENT_DELIMITER);
		assertEquals("*", Envelope.X12_FIELD_DELIMITER);
		assertEquals("<", Envelope.X12_GROUP_DELIMITER);

		X12Envelope x = new X12Envelope(
				EnvelopeFactory.buildEnvelope("x12.envelope", ""));
		x.setDelimiters("abc");

		assertEquals("a", x.segmentDelimiter);
		assertEquals("b", x.fieldDelimiter);
		assertEquals("c", x.groupDelimiter);

		assertEquals("'", Envelope.EDIFACT_SEGMENT_DELIMITER);
		assertEquals("+", Envelope.EDIFACT_FIELD_DELIMITER);
		assertEquals(":", Envelope.EDIFACT_GROUP_DELIMITER);

		EDIFACTEnvelope e = new EDIFACTEnvelope(
				EnvelopeFactory.buildEnvelope("EDIFACT.envelope", ""));
		e.setDelimiters("abc");

		assertEquals("a", e.segmentDelimiter);
		assertEquals("b", e.fieldDelimiter);
		assertEquals("c", e.groupDelimiter);

		assertEquals("'", Envelope.EDIFACT_SEGMENT_DELIMITER);
		assertEquals("+", Envelope.EDIFACT_FIELD_DELIMITER);
		assertEquals(":", Envelope.EDIFACT_GROUP_DELIMITER);

		TradacomsEnvelope t = new TradacomsEnvelope(
				EnvelopeFactory.buildEnvelope("Tradacoms.envelope", ""));
		t.setDelimiters("abc");

		assertEquals("a", t.segmentDelimiter);
		assertEquals("b", t.fieldDelimiter);
		assertEquals("c", t.groupDelimiter);

		try {
			Util.closeOBOEProperty();
			Files.copy(
					Paths.get("testFiles/testDelimiters.oboe.properties.txt"),
					Paths.get("OBOE.properties"),
					StandardCopyOption.REPLACE_EXISTING);

		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(0);
		}

		x.setDelimitersFromProperties(); // |~:
		e.setDelimitersFromProperties(); // ^&*
		t.setDelimitersFromProperties(); // ^&*

		assertEquals("|", x.segmentDelimiter);
		assertEquals("~", x.fieldDelimiter);
		assertEquals(":", x.groupDelimiter);

		// x.setDelimiters("\n*<");

		assertEquals("^", e.segmentDelimiter);
		assertEquals("&", e.fieldDelimiter);
		assertEquals("*", e.groupDelimiter);

		// e.setDelimiters("'+:");

		assertEquals("^", t.segmentDelimiter);
		assertEquals("&", t.fieldDelimiter);
		assertEquals("*", t.groupDelimiter);

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
	public void testDelimitersInX12() throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream("testFiles/sample.output.840.1")));
		StringBuilder sb = new StringBuilder();
		String s;
		try {
			while (true) {
				s = br.readLine();
				if (s == null) {
					break;
				}
				sb.append(s + Envelope.X12_SEGMENT_DELIMITER);
			}
		} catch (EOFException e) {
			;
		}
		br.close();
		s = sb.toString();

		X12DocumentHandler dh = new X12DocumentHandler(new StringReader(s));

		X12Envelope x = (X12Envelope) dh.getEnvelope();

		x.setDelimiters("|~<");

		String t = new String(x.getFormattedText(Format.X12_FORMAT).getBytes());

		br = new BufferedReader(new InputStreamReader(new FileInputStream(
				"testFiles/sample.output.840.newDelimiters")));
		String s2 = br.readLine();

		assertEquals(s2, t);

		StringWriter sw = new StringWriter(t.length());

		x.writeFormattedText(sw, Format.X12_FORMAT);

		assertEquals(sw.toString(), t);

		x.setDelimiters("\n*<");
		t = new String(x.getFormattedText(Format.X12_FORMAT).getBytes());
		assertEquals(s, t);
		br.close();

	}

	@Test
	public void testDelimitersInEDIFACT() throws IOException {

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream("testFiles/orders")));
		String s = null;
		s = br.readLine();
		if (s == null) {
			fail("missing all data from orders");
		}

		br.close();

		EDIFACTDocumentHandler dh = new EDIFACTDocumentHandler(
				new StringReader(s));

		EDIFACTEnvelope x = (EDIFACTEnvelope) dh.getEnvelope();

		x.setDelimiters(";\"=");

		String t = new String(
				x.getFormattedText(Format.EDIFACT_FORMAT).getBytes());

		br = new BufferedReader(new InputStreamReader(
				new FileInputStream("testFiles/orders.newdelimiters")));
		String s2 = br.readLine();

		assertEquals(s2, t);

		StringWriter sw = new StringWriter(t.length());

		x.writeFormattedText(sw, Format.EDIFACT_FORMAT);

		s2 = sw.toString();
		assertEquals(s2, t);

		x.setDelimiters("'+:");
		t = new String(x.getFormattedText(Format.EDIFACT_FORMAT).getBytes());
		assertEquals(s, t);
		br.close();

	}

	@Test
	public void testBuildingFromClasspath() {

		ClassLoader.getSystemClassLoader();

		Util.setOBOEProperty("xmlPath", "xml/");
		Util.setOBOEProperty(Util.SEARCH_CLASSPATH, "true");

		EnvelopeFactory.reloadbuiltTable();
		assertTrue(Util.findMessageDefinitionFilesInClassPath());

		EnvelopeFactory.buildEnvelope("x12.envelope", "4010");

	}

	public static Collection<String> getResources(Pattern pattern) {
		ArrayList<String> retval = new ArrayList<String>();
		String classPath = System.getProperty("java.class.path", ".");
		String[] classPathElements = classPath.split(":");
		for (String element : classPathElements) {
			retval.addAll(getResources(element, pattern));
		}
		return retval;
	}

	private static Collection<String> getResources(String element,
			Pattern pattern) {
		ArrayList<String> retval = new ArrayList<String>();
		File file = new File(element);
		if (file.isDirectory()) {
			retval.addAll(getResourcesFromDirectory(file, pattern));
		} else {
			retval.addAll(getResourcesFromJarFile(file, pattern));
		}
		return retval;
	}

	private static Collection<String> getResourcesFromJarFile(File file,
			Pattern pattern) {
		ArrayList<String> retval = new ArrayList<String>();
		ZipFile zf;
		System.out.println(file.getAbsolutePath());
		try {
			zf = new ZipFile(file);
		} catch (ZipException e) {
			throw new Error(e);
		} catch (IOException e) {
			throw new Error(e);
		}
		Enumeration<ZipEntry> e = (Enumeration<ZipEntry>) zf.entries();
		while (e.hasMoreElements()) {
			ZipEntry ze = e.nextElement();
			String fileName = ze.getName();
			boolean accept = pattern.matcher(fileName).matches();
			if (accept) {
				retval.add(fileName);
			}
		}
		try {
			zf.close();
		} catch (IOException e1) {
			throw new Error(e1);
		}
		return retval;
	}

	private static Collection<String> getResourcesFromDirectory(File directory,
			Pattern pattern) {
		ArrayList<String> retval = new ArrayList<String>();
		File[] fileList = directory.listFiles();
		if (fileList != null) {
			for (File file : fileList) {
				if (file.isDirectory()) {
					retval.addAll(getResourcesFromDirectory(file, pattern));
				} else {
					try {
						String fileName = file.getCanonicalPath();
						boolean accept = pattern.matcher(fileName).matches();
						if (accept) {
							retval.add(fileName);
						}
					} catch (IOException e) {
						throw new Error(e);
					}
				}
			}
		}
		return retval;
	}

}
