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

package io.github.EDIandXML.OBOE.x12;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import io.github.EDIandXML.OBOE.EnvelopeFactory;
import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.TransactionSetFactory;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;

/**
 * OBOE - Open Business Objects for EDI
 * 
 * Use this if you need to create extremly large EDI documents
 *
 * @author Joe McVerry
 * 
 */
public class LargeDocumentGenerator {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		PrintWriter pw = new PrintWriter(System.out);
		LargeDocumentGenerator ldg = new LargeDocumentGenerator(pw);
		ldg.endEnvelope();

	}

	private X12Envelope env = null;
	private Segment isa = null;
	private Segment iea = null;
	private DocumentErrors derr = null;
	private File tempFile;
	private Writer w;
	private FileWriter fw;

	public LargeDocumentGenerator(Writer w) throws IOException {
		this.w = w;
		derr = new DocumentErrors();
		env = new X12Envelope(
				EnvelopeFactory.buildEnvelope("x12.envelope", ""));
		isa = env.createInterchange_Header();

		iea = env.createInterchange_Trailer();

		tempFile = File.createTempFile("large", "oboe");
		fw = new FileWriter(tempFile);
	}

	public DocumentErrors getDocumentErrors() {
		return derr;
	}

	public Segment getISA() {
		return isa;
	};

	private boolean testedEnvSegs = false;

	public void addEnvelopeSegment(Segment seg) throws OBOEException {
		if (seg.getID().equals(X12Envelope.idInterchangeHeader)) {
			throw new OBOEException("ISA segment already added");
		}
		if (seg.getID().equals(X12Envelope.idInterchangeTrailer)) {
			throw new OBOEException("IEA segment already added");
		}

	}

	public Segment createDeferred_Delivery_Request() {
		Segment seg = env.createDeferred_Delivery_Request();

		return seg;
	}

	public Segment createGrade_of_Service_Request() {
		Segment seg = env.createGrade_of_Service_Request();

		return seg;
	}

	public Segment createInterchange_Acknowledgment() {
		Segment seg = env.createInterchange_Acknowledgment();

		return seg;
	}

	int fgcnt = 0;
	X12FunctionalGroup fg = null;
	Segment gs = null;
	Segment ge = null;
	boolean testedGS = false;
	int tscnt = 0;

	public Segment startFunctionalGroup() throws IOException {
		if (testedEnvSegs == false) {
			isa.validate(derr);
			testedEnvSegs = true;
		}
		fg = (X12FunctionalGroup) env.createFunctionalGroup();
		fw.write(isa.getFormattedText(Format.X12_FORMAT));
		gs = fg.createAndAddSegment("GS");

		ge = fg.createAndAddSegment("GE");

		tscnt = 0;
		testedGS = false;
		return gs;

	}

	TransactionSet ts = null;

	public TransactionSet startTransactionSet(String id) throws IOException {
		if (testedGS == false) {
			gs.validate(derr);
			testedGS = true;
		}
		fw.write(gs.getFormattedText(Format.X12_FORMAT));
		ts = TransactionSetFactory.buildTransactionSet(id);
		return ts;
	}

	public TransactionSet endTransationSet() throws IOException {
		ts.validate(derr);
		ts.writeFormattedText(fw, Format.X12_FORMAT);
		tscnt++;
		return ts;
	}

	public FunctionalGroup endFunctionalGroup() throws IOException {
		ge.setDataElementValue(1, "" + tscnt);
		ge.setDataElementValue(2, gs.getDataElementValue(6));
		ge.validate(derr);
		fw.write(ge.getFormattedText(Format.X12_FORMAT));
		fgcnt++;
		return fg;
	}

	public Envelope endEnvelope() throws IOException {
		iea.setDataElementValue(1, "" + fgcnt);
		iea.setDataElementValue(2, isa.getDataElementValue(13));
		iea.validate(derr);
		fw.write(iea.getFormattedText(Format.X12_FORMAT));
		fw.close();
		FileReader fr = new FileReader(tempFile);
		int ch;
		while ((ch = fr.read()) > 0) {
			w.write(ch);
		}
		fr.close();
		return env;
	}
}
