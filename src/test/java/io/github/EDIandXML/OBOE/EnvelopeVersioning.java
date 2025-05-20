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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.DataElements.IDDE;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.util.Util;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;

public class EnvelopeVersioning {

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
	public void testVersioning() {
		try {
			Util.setOBOEProperty(Util.THROW_PARSING_EXCEPTION, "false");
			FileReader fr = new FileReader(
					"testFiles/sample.output.840.VS.forEnvelopeVersioning");
			X12DocumentHandler p1 = new X12DocumentHandler();
			p1.startParsing(fr);
			Envelope env = p1.getEnvelope();
			Segment sg = env.getInterchange_Header();
			IDDE de = (IDDE) sg.getElement("I01");
			assertEquals("JJ", de.get());
			assertTrue(de.isCodeValid("JJ"));
			assertFalse(de.isCodeValid("00"));
			DocumentErrors derr = new DocumentErrors();
			env.validate(derr);
			assertEquals(0, derr.getErrorCount());

			sg = env.getFunctionalGroup(0).getHeader();
			de = (IDDE) sg.getElement(8);
			assertEquals("00401X", de.get());
			assertTrue(de.isCodeValid("00401X"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
