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

package io.github.EDIandXML.OBOE.ach;

import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Tokenizers.ITokenizer;
import io.github.EDIandXML.OBOE.Tokenizers.Tokenizer;

/**
 * 
 * OBOE - Open Business Objects for EDI
 * 
 */

public class ACHTokenizer extends Tokenizer implements ITokenizer {

	/** log4j object */
	static Logger logr = LogManager.getLogger(ACHTokenizer.class);

	/**
	 * builds the parsing object for a transaction set
	 *
	 * @param inReader what's to be tokenized
	 * @param inType   String (X12 | EDIFACT)
	 * @param inDErr   DocumentError
	 * @throws OBOEException invalid token most likely
	 */
	public ACHTokenizer(Reader inReader, String inType, DocumentErrors inDErr)
			throws OBOEException {

		super(inDErr);

	}

}
