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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;

/**
 * @author Joe McVerry
 *
 */
public class TestTransactionSets {

	Envelope envRUNa, envRUNb, envRUNc, envRUNd;

	static Logger logr = LogManager.getLogger(TestTransactionSets.class);

	@BeforeAll
	public static void setup() {
		EnvelopeFactory.reloadbuiltTable();
	}

	@Test
	public void testTransactionSetFactory() {

		Thread ta = new Thread(new Runnable() {
			@Override
			public void run() {
				FileReader fr = null;
				try {
					fr = new FileReader("testFiles/sample.output.840.1");
				} catch (Exception e) {
					logr.error(e.getMessage(), e);
					System.exit(0);
				}

				X12DocumentHandler p1;

				try {
					p1 = new X12DocumentHandler(fr);
				} catch (OBOEException oe) {
					if (oe.getDocumentErrors() != null) {
						oe.getDocumentErrors().logErrors();
					} else {
						logr.error(oe.getMessage(), oe);
					}

					return;
				}

				try {
					fr.close();
					envRUNa = p1.getEnvelope();
				} catch (Exception e) {
					logr.error(e.getMessage(), e);
					System.exit(0);
				}

				logr.info("A done");
			}
		});

		Thread tb = new Thread(new Runnable() {
			@Override
			public void run() {

				FileReader fr = null;
				try {
					fr = new FileReader("testFiles/sample.output.840.1");
				} catch (Exception e) {
					logr.error(e.getMessage(), e);
					System.exit(0);
				}

				X12DocumentHandler p1;

				try {
					p1 = new X12DocumentHandler(fr);
				} catch (OBOEException oe) {
					if (oe.getDocumentErrors() != null) {
						oe.getDocumentErrors().logErrors();
					} else {
						logr.error(oe.getMessage(), oe);
					}

					return;
				}

				try {
					fr.close();
					envRUNb = p1.getEnvelope();
				} catch (Exception e) {
					logr.error(e.getMessage(), e);
					System.exit(0);
				}

				logr.info("B done");
			}
		});

		Thread tc = new Thread(new Runnable() {
			@Override
			public void run() {
				FileReader fr = null;
				try {
					fr = new FileReader("testFiles/sample.output.840.1");
				} catch (Exception e) {
					logr.error(e.getMessage(), e);
					System.exit(0);
				}

				X12DocumentHandler p1;

				try {
					p1 = new X12DocumentHandler(fr);
				} catch (OBOEException oe) {
					if (oe.getDocumentErrors() != null) {
						oe.getDocumentErrors().logErrors();
					} else {
						logr.error(oe.getMessage(), oe);
					}

					return;
				}

				try {
					fr.close();
					envRUNc = p1.getEnvelope();
				} catch (Exception e) {
					logr.error(e.getMessage(), e);
					System.exit(0);
				}

				logr.info("C done");
			}
		});

		Thread td = new Thread(new Runnable() {
			@Override
			public void run() {
				FileReader fr = null;
				try {
					fr = new FileReader("testFiles/sample.output.840.1");
				} catch (Exception e) {
					logr.error(e.getMessage(), e);
					System.exit(0);
				}

				X12DocumentHandler p1;

				try {
					p1 = new X12DocumentHandler(fr);
				} catch (OBOEException oe) {
					if (oe.getDocumentErrors() != null) {
						oe.getDocumentErrors().logErrors();
					} else {
						logr.error(oe.getMessage(), oe);
					}

					return;
				}

				try {
					fr.close();
					envRUNd = p1.getEnvelope();
				} catch (Exception e) {
					logr.error(e.getMessage(), e);
					System.exit(0);
				}

				logr.info("D done");
			}
		});

		ta.start();
		tb.start();
		tc.start();
		td.start();
		try {
			ta.join();
		} catch (InterruptedException e) {
		}
		try {
			tb.join();
		} catch (InterruptedException e) {
		}
		try {
			tc.join();
		} catch (InterruptedException e) {
		}
		try {
			td.join();
		} catch (InterruptedException e) {
		}

		assertTrue(envRUNa.getFormattedText(Format.XML_FORMAT)
				.compareTo(envRUNb.getFormattedText(Format.XML_FORMAT)) == 0);

	}

}
