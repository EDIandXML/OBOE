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

package io.github.EDIandXML.OBOE;

import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;

/**
 * OBOE - Open Business Objects for EDI
 * 
 * this is an interface to handle high level edi objects. once the
 * handler is registered with the EDIDocumentParser the Parser will call
 * make individual method calls when the objects are created or finished
 *
 */

public interface EDIDocumentHandler {

	/**
	 * starts the parser with the passed Reader object
	 *
	 * @param inReader the edi document in a java io Reader object
	 * @exception OBOEException - unknown transaction set, this transaction
	 *                          set is undefined to OBOE - parsing erros
	 */

	public void startParsing(java.io.Reader inReader) throws OBOEException;

	/**
	 * called when an Envelope object is created
	 * 
	 * @param inEnv Envelope found
	 */
	public void startEnvelope(Envelope inEnv);

	/**
	 * called when an FunctionalGroup object is created
	 * 
	 * @param inFG FunctionalGroup found
	 */
	public void startFunctionalGroup(FunctionalGroup inFG);

	/**
	 * called when an TransactionSet object is created
	 * 
	 * @param inTS TransactionSet found
	 */
	public void startTransactionSet(TransactionSet inTS);

	/**
	 * called when an Segment object is created <br>
	 * only called for segments at the Envelope and functionalGroup level
	 * does not get called for segments within TransactionSet
	 * 
	 * @param inSeg Segment found
	 */
	public void startSegment(Segment inSeg);

	/**
	 * called when an Evelope is finished
	 * 
	 * @param inEnv envelope found
	 */
	public void endEnvelope(Envelope inEnv);

	/**
	 * called when an FunctionalGroup object is finished
	 * 
	 * @param inFG FunctionalGroup found
	 */
	public void endFunctionalGroup(FunctionalGroup inFG);

	/**
	 * called when an TransactionSet object is finished
	 * 
	 * @param inTS TransactionSet found
	 */
	public void endTransactionSet(TransactionSet inTS);

	/**
	 * called when an Segment object is finished <br>
	 * only called for segments at the Envelope and functionalGroup level
	 * does not get called for segments within TransactionSet
	 * 
	 * @param inSeg Segment found
	 */
	public void endSegment(Segment inSeg);

	// public CompositeElement endCompositeElement();
	// public DataElement endDataElement();

	public DocumentErrors getDocumentErrors();
}
