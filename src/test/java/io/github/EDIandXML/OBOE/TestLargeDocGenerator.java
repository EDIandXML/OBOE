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
import java.io.FileReader;
import java.io.StringWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.EDIandXML.OBOE.Containers.ContainerKey;
import io.github.EDIandXML.OBOE.Containers.ContainerType;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Loop;
import io.github.EDIandXML.OBOE.Containers.MetaContainer;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.DataElements.CompositeElement;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.x12.LargeDocumentGenerator;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;
import io.github.EDIandXML.OBOE.x12.X12Envelope;

/**
 * @author joe mcverry
 *
 */
public class TestLargeDocGenerator {

	static Logger logr = LogManager.getLogger(TestLargeDocGenerator.class);

	X12Envelope env;
	String envString;

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	@BeforeEach
	protected void setUp() throws Exception {

		EnvelopeFactory.reloadbuiltTable();
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

		envString = sb.toString();
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
			}

			return;
		}

		fr.close();

		env = (X12Envelope) p1.getEnvelope();
	}

	@Test
	public void testLargeDocumentGenerator() throws Exception {
		StringWriter sw = new StringWriter();
		LargeDocumentGenerator ldg = new LargeDocumentGenerator(sw);
		Segment newseg = ldg.getISA();
		Segment oldseg = env.getInterchange_Header();
		moveSegment(oldseg, newseg);
		newseg = ldg.startFunctionalGroup();
		oldseg = env.getFunctionalGroup(0).getHeader();
		moveSegment(oldseg, newseg);
		TransactionSet oldts = env.getFunctionalGroup(0).getTransactionSet(0);
		TransactionSet ts = ldg.startTransactionSet(oldts.getID());
		if (oldts.getHeaderTable() != null) {
			moveLoopAndSegments(oldts.getHeaderTable(), ts.getHeaderTable());
		}
		if (oldts.getDetailTable() != null) {
			moveLoopAndSegments(oldts.getDetailTable(), ts.getDetailTable());
		}
		if (oldts.getSummaryTable() != null) {
			moveLoopAndSegments(oldts.getSummaryTable(), ts.getSummaryTable());
		}
		ldg.endTransationSet();
		ldg.endFunctionalGroup();
		ldg.endEnvelope();
		logr.debug(sw.toString());
		assertEquals(this.envString, sw.toString());
		assertEquals(0, ldg.getDocumentErrors().getErrorCount());
	}

	@Test
	public void test2TSLargeDocumentGenerator() throws Exception {
		StringWriter sw = new StringWriter();
		LargeDocumentGenerator ldg = new LargeDocumentGenerator(sw);
		Segment newseg = ldg.getISA();
		Segment oldseg = env.getInterchange_Header();
		moveSegment(oldseg, newseg);
		newseg = ldg.startFunctionalGroup();
		oldseg = env.getFunctionalGroup(0).getHeader();
		moveSegment(oldseg, newseg);
		TransactionSet oldts = env.getFunctionalGroup(0).getTransactionSet(0);
		TransactionSet ts = ldg.startTransactionSet(oldts.getID());
		if (oldts.getHeaderTable() != null) {
			moveLoopAndSegments(oldts.getHeaderTable(), ts.getHeaderTable());
		}
		if (oldts.getDetailTable() != null) {
			moveLoopAndSegments(oldts.getDetailTable(), ts.getDetailTable());
		}
		if (oldts.getSummaryTable() != null) {
			moveLoopAndSegments(oldts.getSummaryTable(), ts.getSummaryTable());
		}
		ldg.endTransationSet();
		// second transaction set
		ts = ldg.startTransactionSet(oldts.getID());
		if (oldts.getHeaderTable() != null) {
			moveLoopAndSegments(oldts.getHeaderTable(), ts.getHeaderTable());
		}
		if (oldts.getDetailTable() != null) {
			moveLoopAndSegments(oldts.getDetailTable(), ts.getDetailTable());
		}
		if (oldts.getSummaryTable() != null) {
			moveLoopAndSegments(oldts.getSummaryTable(), ts.getSummaryTable());
		}
		ldg.endTransationSet();
		FunctionalGroup fg = ldg.endFunctionalGroup();
		Envelope env = ldg.endEnvelope();
		logr.debug(sw.toString());

		assertEquals("" + 2, fg.getTrailer().getDataElementValue(1));
		assertEquals("" + 1, env.getSegment("IEA").getDataElementValue(1));
		assertEquals(0, ldg.getDocumentErrors().getErrorCount());
	}

	@Test
	public void test2FGLargeDocumentGenerator() throws Exception {
		StringWriter sw = new StringWriter();
		LargeDocumentGenerator ldg = new LargeDocumentGenerator(sw);
		Segment newseg = ldg.getISA();
		Segment oldseg = env.getInterchange_Header();
		moveSegment(oldseg, newseg);
		newseg = ldg.startFunctionalGroup();
		oldseg = env.getFunctionalGroup(0).getHeader();
		moveSegment(oldseg, newseg);
		TransactionSet oldts = env.getFunctionalGroup(0).getTransactionSet(0);
		TransactionSet ts = ldg.startTransactionSet(oldts.getID());
		if (oldts.getHeaderTable() != null) {
			moveLoopAndSegments(oldts.getHeaderTable(), ts.getHeaderTable());
		}
		if (oldts.getDetailTable() != null) {
			moveLoopAndSegments(oldts.getDetailTable(), ts.getDetailTable());
		}
		if (oldts.getSummaryTable() != null) {
			moveLoopAndSegments(oldts.getSummaryTable(), ts.getSummaryTable());
		}
		ldg.endTransationSet();
		// second functional group
		FunctionalGroup fg = ldg.endFunctionalGroup();
		assertEquals("" + 1, fg.getTrailer().getDataElementValue(1));
		newseg = ldg.startFunctionalGroup();
		oldseg = env.getFunctionalGroup(0).getHeader();
		moveSegment(oldseg, newseg);
		ts = ldg.startTransactionSet(oldts.getID());
		if (oldts.getHeaderTable() != null) {
			moveLoopAndSegments(oldts.getHeaderTable(), ts.getHeaderTable());
		}
		if (oldts.getDetailTable() != null) {
			moveLoopAndSegments(oldts.getDetailTable(), ts.getDetailTable());
		}
		if (oldts.getSummaryTable() != null) {
			moveLoopAndSegments(oldts.getSummaryTable(), ts.getSummaryTable());
		}
		ldg.endTransationSet();
		fg = ldg.endFunctionalGroup();
		assertEquals("" + 1, fg.getTrailer().getDataElementValue(1));
		Envelope env = ldg.endEnvelope();
		logr.debug(sw.toString());
		assertEquals("" + 2, env.getSegment("IEA").getDataElementValue(1));
		assertEquals(0, ldg.getDocumentErrors().getErrorCount());
	}

	public void moveLoopAndSegments(MetaContainer oldls, MetaContainer newls) {

		var oldContainer = oldls.getTheContainer();
		for (var firstList : oldContainer) {
			for (var secondList : firstList) {
				ContainerKey ck = new ContainerKey(
						secondList.getContainerType(), secondList.getID());
				if (secondList.getContainerType() == ContainerType.Loop) {
					Loop oldLoop = (Loop) secondList;
					Loop newLoop = newls.createAndAddLoop(secondList.getID());
					moveLoopAndSegments(oldLoop, newLoop);
				} else {
					Segment oldSegment = (Segment) secondList;
					Segment newSegment = newls
							.createAndAddSegment(secondList.getID());
					moveSegment(oldSegment, newSegment);

				}
			}
		}

	}

	public void moveSegment(Segment oldseg, Segment newseg) {

		for (var elm : oldseg.myElementContainer.getElementList().entrySet()) {
			if (elm.getValue() instanceof CompositeElement) {
				CompositeElement oldelm = (CompositeElement) elm.getValue();
				CompositeElement newelm = (CompositeElement) newseg
						.buildElement(elm.getKey());
				moveComposite(oldelm, newelm);

			} else {
				DataElement oldelm = (DataElement) elm.getValue();
				DataElement newelm = (DataElement) newseg
						.buildElement(elm.getKey());
				newelm.set(oldelm.get());
			}

		}

	}

	/**
	 * @param old CompositeElement
	 * @param new composite
	 */
	private void moveComposite(CompositeElement oldcomp,
			CompositeElement newcomp) {
		for (var elmcontainer : oldcomp.getGroupOfMyElements()) {
			for (var elm : elmcontainer.getElementList().entrySet()) {
				DataElement oldelm = (DataElement) elm.getValue();
				DataElement newelm = (DataElement) newcomp
						.buildElement(elm.getKey());
				newelm.set(oldelm.get());
			}

		}
	}
}
