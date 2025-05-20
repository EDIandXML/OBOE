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

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.DataElements.IDDE;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.util.Util;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;

public class SearchForMessageFilesBasedOnPropertiesDirective {

	static Logger logr = LogManager
			.getLogger(SearchForMessageFilesBasedOnPropertiesDirective.class);

	@BeforeAll
	protected static void preSetup() {
		TransactionSetFactory.clearTable();
	}

	@BeforeEach
	protected void setUp() {

		try {
			Util.closeOBOEProperty();
			Files.copy(Paths.get("testfiles/TVRS.oboe.properties.txt"),
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
	public void testT() throws IOException {
		FileReader fr = new FileReader("testFiles/sample.output.840.T");

		var dh = new X12DocumentHandler(fr);

		assertEquals(0, dh.getDocumentErrors().getErrorCount());

		var idfield = (IDDE) dh.getEnvelope().getFunctionalGroup(0)
				.getTransactionSet(0).getHeaderTable().getSegment("BQT")
				.getElement(1);
		assertEquals("T Test", idfield.describe());

	}

	@Test
	public void testTBuildTS() throws IOException {

		var ts = TransactionSetFactory.buildTransactionSet("840", "T", "1", "2",
				"3", "p");

		TemplateDataElement idfield = (TemplateDataElement) ts.getHeaderTable()
				.getMyTemplate().getTemplateSegment("BQT")
				.getTemplateElement(1);
		assertEquals("TEST FOUND IN PRODUCTION FOLDER",
				idfield.getIDList().describe("00"));

	}

	@Test
	public void testTV() throws IOException {
		FileReader fr = new FileReader("testFiles/sample.output.840.TV");

		var dh = new X12DocumentHandler(fr);
		if (dh.getDocumentErrors().getErrorCount() > 0) {
			dh.getDocumentErrors().logErrors();
		}
		assertEquals(0, dh.getDocumentErrors().getErrorCount());

		var idfield = (IDDE) dh.getEnvelope().getFunctionalGroup(0)
				.getTransactionSet(0).getHeaderTable().getSegment("BQT")
				.getElement(1);
		assertEquals("TV Test", idfield.describe());

	}

	@Test
	public void testTVBuildTS() throws IOException {
		// tvr but r will not be found but t (p)v(4020) will
		var ts = TransactionSetFactory.buildTransactionSet("840", "TVR",
				"004020", "2", "3", "p");

		TemplateDataElement idfield = (TemplateDataElement) ts.getHeaderTable()
				.getMyTemplate().getTemplateSegment("BQT")
				.getTemplateElement(1);
		assertEquals("P 4020 TEST", idfield.getIDList().describe("00"));

	}

	@Test
	public void testR() throws IOException {
		FileReader fr = new FileReader("testFiles/sample.output.840.R");

		try {
			Util.closeOBOEProperty();
			Files.copy(Paths.get("testfiles/receiver.oboe.properties.txt"),
					Paths.get("OBOE.properties"),
					StandardCopyOption.REPLACE_EXISTING);

		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(0);
		}

		var dh = new X12DocumentHandler(fr);
		if (dh.getDocumentErrors().getErrorCount() > 0) {
			dh.getDocumentErrors().logErrors();
		}
		assertEquals(0, dh.getDocumentErrors().getErrorCount());

		var idfield = (IDDE) dh.getEnvelope().getFunctionalGroup(0)
				.getTransactionSet(0).getHeaderTable().getSegment("BQT")
				.getElement(1);
		assertEquals("TEST RECEIVER FOLDER", idfield.describe());

	}

	@Test
	public void testRBuildTS() throws IOException {
		// rtv but tv will not be found but r will
		var ts = TransactionSetFactory.buildTransactionSet("840", "RTV", "1",
				"receiverII", "3", "p");
		var idfield = (TemplateDataElement) ts.getHeaderTable().getMyTemplate()
				.getTemplateSegment("BQT").getTemplateElement(1);
		assertEquals("TEST RECEIVER II FOLDER",
				idfield.getIDList().describe("00"));

	}

	@Test
	public void testVS() throws IOException {
		try {
			Files.copy(Paths.get("testfiles/versionsender.oboe.properties.txt"),
					Paths.get("OBOE.properties"),
					StandardCopyOption.REPLACE_EXISTING);

		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(0);
		}

		FileReader fr = new FileReader("testFiles/sample.output.840.VS");

		TransactionSetFactory.clearTable();
		try {
			new X12DocumentHandler(fr);
		} catch (OBOEException oe) {
			oe.getDocumentErrors().logErrors();

			fail(oe.getMessage());
		} catch (Exception e) {
			logr.error(e.getMessage(), e);
			fail(e.getMessage());
		}

	}

	@Test
	public void testVSBuildTS() throws IOException {

		try {
			Files.copy(Paths.get("testfiles/versionsender.oboe.properties.txt"),
					Paths.get("OBOE.properties"),
					StandardCopyOption.REPLACE_EXISTING);

		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(0);
		}

		TransactionSetFactory.clearTable();
		TransactionSetFactory.buildTransactionSet("840", "VS", "004031", "2",
				"sender", "P");

	}

	@Test
	public void testXS() throws IOException {

		try {
			Files.copy(Paths.get("testFiles/UseVersionX.oboe.properties.txt"),
					Paths.get("OBOE.properties"),
					StandardCopyOption.REPLACE_EXISTING);

		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(0);
		}

		FileReader fr = new FileReader("testFiles/sample.output.840.X"); // has
		// 004031X999
		// but uses
		// 004031
		// directory.

		Util.setOBOEProperty(Util.SEARCH_DIRECTIVE, "XS");
		TransactionSetFactory.clearTable();
		try {
			new X12DocumentHandler(fr);
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

			fail(oe.getMessage());
		} catch (Exception e) {
			logr.error(e.getMessage(), e);
			fail(e.getMessage());
		}

	}

}