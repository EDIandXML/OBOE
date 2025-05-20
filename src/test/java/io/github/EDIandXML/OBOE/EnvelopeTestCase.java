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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.EDIFACT.EDIFACTEnvelope;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.TRADACOMS.TradacomsEnvelope;
import io.github.EDIandXML.OBOE.util.Util;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;
import io.github.EDIandXML.OBOE.x12.X12Envelope;

public class EnvelopeTestCase {

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
			Files.copy(Paths.get("testFiles/testDelimiters.oboe.properties.txt"),
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

	public void testDelimitersInX12() throws IOException {

		try (BufferedReader br = new BufferedReader(
				new FileReader("testFiles/sample.output.840.1"))) {
			String instring = null;
			StringBuilder sb = new StringBuilder();
			do {
				instring = br.readLine();
				if (instring != null) {
					sb.append(instring + '\n');
				}

			} while (instring != null);

			String s = sb.toString();

			X12DocumentHandler dh = new X12DocumentHandler(
					new FileReader("testFiles/sample.output.840.1"));

			X12Envelope x = (X12Envelope) dh.getEnvelope();

			x.setDelimiters("|~<");

			String t = new String(
					x.getFormattedText(Format.X12_FORMAT).getBytes());

			FileInputStream fis = new FileInputStream(
					"testFiles/sample.output.840.newDelimiters");
			try (var br2 = new BufferedReader(new InputStreamReader(fis))) {
				String s2 = br2.readLine();

				assertEquals(s2, t);
			}
			x.setDelimiters("\n*<");
			t = new String(x.getFormattedText(Format.X12_FORMAT).getBytes());
			assertEquals(s, t);
		} catch (OBOEException e) {

			e.printStackTrace();
		}

	}

	public void testDelimitersInX12WithGrouping() throws IOException {

		try (BufferedReader br = new BufferedReader(
				new FileReader("testFiles/sample.output.840.5010"))) {
			String instring = null;
			StringBuilder sb = new StringBuilder();
			do {
				instring = br.readLine();
				if (instring != null) {
					sb.append(instring + '\n');
				}

			} while (instring != null);

			String s = sb.toString();
			br.close();

			X12DocumentHandler dh = new X12DocumentHandler(
					new FileReader("testFiles/sample.output.840.5010"));

			X12Envelope x = (X12Envelope) dh.getEnvelope();

			x.setDelimiters("|~<\\");

			String t = new String(
					x.getFormattedText(Format.X12_FORMAT).getBytes());

			FileInputStream fis = new FileInputStream(
					"testFiles/sample.output.840.newDelimiters.5010");
			try (var br2 = new BufferedReader(new InputStreamReader(fis))) {
				String s2 = br2.readLine();

				assertEquals(s2, t);
			}
			x.setDelimiters("\n*<:");
			t = new String(x.getFormattedText(Format.X12_FORMAT).getBytes());
			assertEquals(s, t);
		} catch (OBOEException e) {

			e.printStackTrace();
		}

	}

	public void testDelimitersForISAGroupDelimiter()
			throws OBOEException, FileNotFoundException, IOException {

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

		String s = sb.toString();
		fr.close();

		X12DocumentHandler x12DocHandler = new X12DocumentHandler(
				new FileReader("testFiles/sample.output.840.1"));
		X12Envelope fileEnv = (X12Envelope) x12DocHandler.getEnvelope();
		// setDelimiters 3, 4, or 5 characters: segment, field, group, repeat,
		// escape
		s = s.replaceAll("\n", "~");
		s = s.replaceAll("<", "&");
		s = s.replaceAll("\\*", "|");
		fileEnv.setDelimiters("~|&");
		String temp2 = fileEnv.getFormattedText(Format.X12_FORMAT);
		assertEquals(s, temp2);
	}

}