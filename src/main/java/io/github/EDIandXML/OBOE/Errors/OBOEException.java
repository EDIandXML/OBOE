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
package io.github.EDIandXML.OBOE.Errors;

import java.io.IOException;

/**
 * Custom exception class for the OBOE (Open Business Objects for EDI)
 * framework. Used to handle errors during EDI and XML translation,
 * particularly parsing errors.
 * 
 */
public class OBOEException extends RuntimeException {
	/** Serial version UID for serialization */
	private static final long serialVersionUID = 1L;

	/** DocumentErrors object containing parsing errors, if applicable */
	private final DocumentErrors documentErrors;

	/**
	 * Constructs an OBOEException with no message or cause.
	 */
	public OBOEException() {
		super();
		this.documentErrors = null;
	}

	/**
	 * Constructs an OBOEException with the specified message.
	 *
	 * @param message The detail message
	 */
	public OBOEException(String message) {
		super(message);
		this.documentErrors = null;
	}

	/**
	 * Constructs an OBOEException with parsing errors.
	 *
	 * @param documentErrors The DocumentErrors object containing parsing
	 *                       errors
	 */
	public OBOEException(DocumentErrors documentErrors) {
		super("Parsing Errors.");
		this.documentErrors = documentErrors;
	}

	public OBOEException(String string, IOException e) {
		super(string, e);
		this.documentErrors = null;
	}

	/**
	 * Returns the DocumentErrors object associated with this exception, if
	 * any.
	 *
	 * @return The DocumentErrors object, or null if not set
	 */
	public DocumentErrors getDocumentErrors() {
		return documentErrors;
	}
}