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

package io.github.ediandxml.oboe.x12;

import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.ediandxml.oboe.Containers.Envelope;
import io.github.ediandxml.oboe.Containers.FunctionalGroup;
import io.github.ediandxml.oboe.Containers.Segment;
import io.github.ediandxml.oboe.Containers.TransactionSet;
import io.github.ediandxml.oboe.Errors.DocumentErrors;
import io.github.ediandxml.oboe.Errors.OBOEException;
import io.github.ediandxml.oboe.EDIDocumentHandler;

/**
 * OBOE - Open Business Objects for EDI
 * 
 * Use this if you need to parse extremely large EDI documents.
 *
 * @author Joe McVerry
 * 
 */
public class LargeDocumentHandler implements EDIDocumentHandler {
	static Logger logr = LogManager.getLogger(LargeDocumentHandler.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.ediandxml.oboe.EDIDocumentHandler#endEnvelope(Envelope)
	 */
	@Override
	public void endEnvelope(Envelope inEnv) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.ediandxml.oboe.EDIDocumentHandler#endFunctionalGroup(
	 * FunctionalGroup)
	 */
	@Override
	public void endFunctionalGroup(FunctionalGroup inFG) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.ediandxml.oboe.EDIDocumentHandler#endSegment(Segment)
	 */
	@Override
	public void endSegment(Segment inSeg) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.ediandxml.oboe.EDIDocumentHandler#endTransactionSet(
	 * TransactionSet)
	 */
	@Override
	public void endTransactionSet(TransactionSet inTS) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.ediandxml.oboe.EDIDocumentHandler#getDocumentErrors()
	 */
	@Override
	public DocumentErrors getDocumentErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.ediandxml.oboe.EDIDocumentHandler#startEnvelope(Envelope)
	 */
	@Override
	public void startEnvelope(Envelope inEnv) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.ediandxml.oboe.EDIDocumentHandler#startFunctionalGroup(
	 * FunctionalGroup)
	 */
	@Override
	public void startFunctionalGroup(FunctionalGroup inFG) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.ediandxml.oboe.EDIDocumentHandler#startParsing(java.io.
	 * Reader)
	 */
	@Override
	public void startParsing(Reader inReader) throws OBOEException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.ediandxml.oboe.EDIDocumentHandler#startSegment(Segment)
	 */
	@Override
	public void startSegment(Segment inSeg) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.ediandxml.oboe.EDIDocumentHandler#startTransactionSet(
	 * TransactionSet)
	 */
	@Override
	public void startTransactionSet(TransactionSet inTS) {
		// TODO Auto-generated method stub

	}

}
