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

package io.github.EDIandXML.OBOE.EDIFACT;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Tokenizers.ITokenizer;
import io.github.EDIandXML.OBOE.Tokenizers.ReaderTokenizer;
import io.github.EDIandXML.OBOE.Tokenizers.Tokenizer;

/**
 * 
 * OBOE - Open Business Objects for EDI
 * 
 * 
 */

public class EDIFACTTokenizer extends Tokenizer implements ITokenizer {

	/** log4j object */
	static Logger logr = LogManager.getLogger(EDIFACTTokenizer.class);

	/**
	 * builds the parsing object for a transaction set
	 *
	 * @param inReader what's to be tokenized
	 * @param inDErr   DocumentError
	 * @throws OBOEException invalid token most likely
	 */
	public EDIFACTTokenizer(Reader inReader, DocumentErrors inDErr)
			throws OBOEException {

		super(inDErr);

		PushbackReader pbr = new PushbackReader(inReader, 10);
		char firstSeg[] = new char[10];
		try {
			if (pbr.read(firstSeg, 0, 10) != 10) {
				throw new OBOEException("expected data not read");
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new OBOEException(ioe.getMessage());
		}

		if ((firstSeg[0] == 'U') && (firstSeg[1] == 'N')
				&& (firstSeg[2] == 'A')) {
			tokenseparatorCharacter = firstSeg[8] + "";
			separators.append(tokenseparatorCharacter);
			escapeCharacters = firstSeg[6] + "";
			transactionSetTokenizer = new ReaderTokenizer(pbr,
					tokenseparatorCharacter, escapeCharacters);
			tokenseparatorCharacter = firstSeg[4] + "";
			separators.append(tokenseparatorCharacter);
			tokenGroups[0] = firstSeg[3];
			separators.append(tokenGroups[0]);
			currentTokenStringToken = new String(firstSeg);
			try {
				if (firstSeg[9] == 'U') {
					pbr.unread(firstSeg[9]);
				} else {
					// otherwise its a control character of a cr/lf or lf/cr
					// combination
					((ReaderTokenizer) transactionSetTokenizer)
							.setSkipChar(firstSeg[9]);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
				throw new OBOEException(ioe.getMessage());
			}
		} else // use defaults
		{

			try {
				pbr.unread(firstSeg);
			} catch (IOException ioe) {
				ioe.printStackTrace();
				throw new OBOEException(ioe.getMessage());
			}
			transactionSetTokenizer = new ReaderTokenizer(pbr,
					Envelope.EDIFACT_SEGMENT_DELIMITER, "?");
			separators.append(Envelope.EDIFACT_SEGMENT_DELIMITER);
			tokenseparatorCharacter = Envelope.EDIFACT_FIELD_DELIMITER;
			separators.append(Envelope.EDIFACT_FIELD_DELIMITER);
			tokenGroups[0] = Envelope.EDIFACT_GROUP_DELIMITER.charAt(0);
			separators.append(tokenGroups[0]);
			this.escapeCharacters = Envelope.EDIFACT_ESCAPE_CHARACTER;
		}
		dataElementReady = false;
	}

}
