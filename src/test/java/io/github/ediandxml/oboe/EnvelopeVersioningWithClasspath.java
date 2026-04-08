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
package io.github.ediandxml.oboe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.github.ediandxml.oboe.Containers.Envelope;
import io.github.ediandxml.oboe.Containers.Segment;
import io.github.ediandxml.oboe.DataElements.IDDE;
import io.github.ediandxml.oboe.Errors.DocumentErrors;
import io.github.ediandxml.oboe.util.Util;
import io.github.ediandxml.oboe.x12.X12DocumentHandler;

public class EnvelopeVersioningWithClasspath {

	static Logger logr = LogManager.getLogger(EnvelopeVersioningWithClasspath.class);

	@Test
	public void testVersioning() {

		try {
			Util.setOBOEProperty(Util.THROW_PARSING_EXCEPTION, "false");
			Util.setOBOEProperty(Util.SEARCH_DIRECTIVE, "V");
			FileReader fr = new FileReader("testFiles/sample.output.840.VS.forEnvelopeVersioning");
			X12DocumentHandler p1 = new X12DocumentHandler();

			System.out.println(
					"This test logs an error message, it may be ignored if the assert below complete successfully");
			logr.debug("This test logs an error message, it may be ignored if the assert below complete successfully");
			p1.startParsing(fr);
			Envelope env = p1.getEnvelope();
			Segment sg = env.getInterchange_Header();

			IDDE de = (IDDE) sg.getElement("I01");

			assertTrue(de.get().equals("JJ"));
			assertTrue(de.isCodeValid("JJ"));
			assertFalse(de.isCodeValid("00"));
			DocumentErrors derr = new DocumentErrors();
			env.validate(derr);
			if (derr.getErrorCount() != 1)
				derr.logErrors();
			assertEquals(0, derr.getErrorCount());
			sg = env.getFunctionalGroup(0).getHeader();
			de = (IDDE) sg.getElement(8);
			assertTrue(de.isCodeValid("00401X"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}

	}

	@AfterEach
	protected void tearDown() {
		Util.setOBOEProperty(Util.SEARCH_DIRECTIVE, "");
	}

}
