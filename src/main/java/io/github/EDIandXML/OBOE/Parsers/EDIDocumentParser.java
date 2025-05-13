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

package io.github.EDIandXML.OBOE.Parsers;

/**
 * OBOE - Open Business Objects for EDI
 */

import java.util.ArrayList;

import io.github.EDIandXML.OBOE.EDIDocumentHandler;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors.ERROR_TYPE;
import io.github.EDIandXML.OBOE.Errors.OBOEException;

/**
 * abstract class defining methods for parsing EDI Documents <br>
 * Document handlers will register with this class to be notified when
 * specific edi objects are created or finished <br>
 * Unlike the old parser these parsers will not contain the objects, the
 * process of adding objects to owning parents (such as adding
 * functional groups to an envelope) is left up to the document handler.
 */

public abstract class EDIDocumentParser {

	/** list of registered document handlers */

	private ArrayList<EDIDocumentHandler> handlers = new ArrayList<EDIDocumentHandler>();

	protected DocumentErrors dErr = new DocumentErrors();

	/**
	 * method to reset the error level processing
	 *
	 * @param i int
	 */

	public void setErrorLevelToReport(ERROR_TYPE type) {
		dErr.setErrorLevelToReport(type);
	}

	/**
	 * method for handlers to register with the parser
	 *
	 * @param edh EDIDocumentHandler
	 */
	public void registerHandler(EDIDocumentHandler edh) {
		synchronized (handlers) {
			handlers.add(edh);
		}
	}

	/**
	 * abstract method all document parsers must implement
	 *
	 * @param s String edi document
	 * @return boolean - true - continue or false - halted
	 * @throws OBOEException
	 */

	public abstract boolean parseDocument(String s) throws OBOEException;

	/**
	 * abstract method all document parsers must implement
	 *
	 * @param r java.io.Reader object containing edi document
	 * @param b boolean if true run validation routine after parsing.
	 * @return boolean - true - continue or false - halted
	 * @throws OBOEException
	 */

	public abstract boolean parseDocument(java.io.Reader r, boolean b)
			throws OBOEException;

	/**
	 * method to notifiy handlers when an envelope was just created
	 *
	 * @param inEnv Envelope
	 */
	public void notifyStartEnvelope(Envelope inEnv) {
		synchronized (handlers) {
			for (int i = 0; i < handlers.size(); i++) {
				EDIDocumentHandler edh = handlers.get(i);
				edh.startEnvelope(inEnv);
			}
		}
	}

	/**
	 * method to notifiy handlers when an FunctionalGroup was just created
	 *
	 * @param inFG FunctionalGroup
	 */
	public void notifyStartFunctionalGroup(FunctionalGroup inFG) {
		synchronized (handlers) {
			for (int i = 0; i < handlers.size(); i++) {
				EDIDocumentHandler edh = handlers.get(i);
				edh.startFunctionalGroup(inFG);
			}
		}
	}

	/**
	 * method to notifiy handlers when an TransactionSet was just created
	 *
	 * @param inTS TransactionSet
	 */
	public void notifyStartTransactionSet(TransactionSet inTS) {
		synchronized (handlers) {
			for (int i = 0; i < handlers.size(); i++) {
				EDIDocumentHandler edh = handlers.get(i);
				edh.startTransactionSet(inTS);
			}
		}
	}

	/**
	 * method to notifiy handlers when an Segment was just created
	 *
	 * @param inSeg Segment
	 */
	public void notifyStartSegment(Segment inSeg) {
		synchronized (handlers) {
			for (int i = 0; i < handlers.size(); i++) {
				EDIDocumentHandler edh = handlers.get(i);
				edh.startSegment(inSeg);
			}
		}
	}

	/**
	 * method to notifiy handlers when processing of an envelope is complete
	 *
	 * @param inEnv Envelope
	 */
	public void notifyEndEnvelope(Envelope inEnv) {
		synchronized (handlers) {
			for (int i = 0; i < handlers.size(); i++) {
				EDIDocumentHandler edh = handlers.get(i);
				edh.endEnvelope(inEnv);
			}
		}
	}

	/**
	 * method to notifiy handlers when processing of a functional group is
	 * complete
	 *
	 * @param inFG FunctionalGroup
	 */
	public void notifyEndFunctionalGroup(FunctionalGroup inFG) {
		synchronized (handlers) {
			for (int i = 0; i < handlers.size(); i++) {
				EDIDocumentHandler edh = handlers.get(i);
				edh.endFunctionalGroup(inFG);
			}
		}
	}

	/**
	 * method to notifiy handlers when processing of an TransactionSet is
	 * complete
	 *
	 * @param inTS TransactionSet
	 */
	public void notifyEndTransactionSet(TransactionSet inTS) {
		synchronized (handlers) {
			for (int i = 0; i < handlers.size(); i++) {
				EDIDocumentHandler edh = handlers.get(i);
				edh.endTransactionSet(inTS);
			}
		}
	}

	/**
	 * method to notifiy handlers when processing of an Segment is complete
	 *
	 * @param inSeg Segment
	 */
	public void notifyEndSegment(Segment inSeg) {
		synchronized (handlers) {
			for (int i = 0; i < handlers.size(); i++) {
				EDIDocumentHandler edh = handlers.get(i);
				edh.endSegment(inSeg);
			}
		}
	}

	// public abstract CompositeElement notifyEndCompositeElement();
	// public abstract DataElement notifyEndDataElement();

	/**
	 * method for handlers to deregister from the parser
	 *
	 * @param edh EDIDocumentHandler
	 */
	public void deregisterHandler(EDIDocumentHandler edh) {
		synchronized (handlers) {
			handlers.remove(edh);
		}
	}

	/**
	 * gets the DocumentErrors object
	 *
	 * @return DocumentErrors object
	 */

	public DocumentErrors getDocumentErrors() {
		return dErr;
	}

	/**
	 * used to tell the parser to exit
	 */

	public String whyHaltParser = "";

	/**
	 * reset halt indicator
	 *
	 * @param inWhy
	 */
	public void resetWhyHaltParser() {
		whyHaltParser = "";
	}

	/**
	 * inform parser to halt and why
	 *
	 * @param inWhy
	 */
	public void setWhyHaltParser(String inWhy) {
		whyHaltParser = inWhy;
	}

	public boolean halted() {
		return (whyHaltParser.length() > 0);
	}

}
