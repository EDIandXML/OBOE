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

import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.EDIDocumentHandler;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;

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
	 * @see
	 * io.github.EDIandXML.OBOE.EDIDocumentHandler#endEnvelope(Envelope)
	 */
	@Override
	public void endEnvelope(Envelope inEnv) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.EDIandXML.OBOE.EDIDocumentHandler#endFunctionalGroup(
	 * FunctionalGroup)
	 */
	@Override
	public void endFunctionalGroup(FunctionalGroup inFG) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.EDIandXML.OBOE.EDIDocumentHandler#endSegment(Segment)
	 */
	@Override
	public void endSegment(Segment inSeg) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.EDIandXML.OBOE.EDIDocumentHandler#endTransactionSet(
	 * TransactionSet)
	 */
	@Override
	public void endTransactionSet(TransactionSet inTS) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.EDIandXML.OBOE.EDIDocumentHandler#getDocumentErrors()
	 */
	@Override
	public DocumentErrors getDocumentErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.github.EDIandXML.OBOE.EDIDocumentHandler#startEnvelope(Envelope)
	 */
	@Override
	public void startEnvelope(Envelope inEnv) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.github.EDIandXML.OBOE.EDIDocumentHandler#startFunctionalGroup(
	 * FunctionalGroup)
	 */
	@Override
	public void startFunctionalGroup(FunctionalGroup inFG) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.github.EDIandXML.OBOE.EDIDocumentHandler#startParsing(java.io.
	 * Reader)
	 */
	@Override
	public void startParsing(Reader inReader) throws OBOEException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.github.EDIandXML.OBOE.EDIDocumentHandler#startSegment(Segment)
	 */
	@Override
	public void startSegment(Segment inSeg) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see io.github.EDIandXML.OBOE.EDIDocumentHandler#startTransactionSet(
	 * TransactionSet)
	 */
	@Override
	public void startTransactionSet(TransactionSet inTS) {
		// TODO Auto-generated method stub

	}

}
