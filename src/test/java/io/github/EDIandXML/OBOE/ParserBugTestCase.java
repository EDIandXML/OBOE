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

import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;

public class ParserBugTestCase {
	static Logger logr = LogManager.getLogger(ParserBugTestCase.class);

	@Test
	public void testEmpyDataLine() {

		String sEDI810Invoice = "ISA*00*          *00*          *ZZ*KOH1   KOH1174 *ZZ*NATIONSBANK    *011015*1350*U*00200*000000047*0*P*>|GS*ZZ*KOH1174*IPPINV*20011015*1350*47*X*004010|ST*4RepeatingNumericTest*47001|TDS*63264**63264||SE*3*47001|GE*1137*47|IEA*1*000000047|";

		try {
			new X12DocumentHandler(new StringReader(sEDI810Invoice));
		} catch (OBOEException oe) {

			if (oe.getDocumentErrors().getErrorDescription(0)
					.compareTo("Invalid token length") != 0) {
				fail(oe.getMessage());
			}
		} catch (Exception e) {
			logr.info(e.getMessage());
			logr.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}

}
