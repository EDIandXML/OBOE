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

package io.github.EDIandXML.OBOE.TRADACOMS;

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
 * class to assist in tokenizing input transaction sets x12 field
 * separator uses 3rd byte of input string x12 segment separator uses
 * 16th field + 1 byte field, if it sees a cr character then checks for
 * a lf character and then assumes a \\n character EDIFACT uses
 * different control positions. see header segment.
 * 
 * OBOE - Open Business Objects for EDI
 * 
 * 
 * @author Joe McVerry
 * 
 */

public class TradacomsTokenizer extends Tokenizer implements ITokenizer {

	/** log4j object */
	static Logger logr = LogManager.getLogger(TradacomsTokenizer.class);

	/**
	 * builds the parsing object for a transaction set
	 * 
	 * @param inReader what's to be tokenized
	 * @param inDErr   DocumentError
	 * @throws OBOEException invalid token most likely
	 */
	public TradacomsTokenizer(Reader inReader, DocumentErrors inDErr)
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
		try {
			pbr.unread(firstSeg);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new OBOEException(ioe.getMessage());
		}
		transactionSetTokenizer = new ReaderTokenizer(pbr,
				Envelope.TRADACOMS_SEGMENT_DELIMITER, "?");
		separators.append(Envelope.TRADACOMS_SEGMENT_DELIMITER);
		tokenseparatorCharacter = Envelope.TRADACOMS_FIELD_DELIMITER
				+ Envelope.TRADACOMS_SEGID_DELIMITER;
		separators.append(Envelope.TRADACOMS_FIELD_DELIMITER);
		tokenGroups[0] = Envelope.TRADACOMS_GROUP_DELIMITER.charAt(0);
		separators.append(tokenGroups[0]);
		this.escapeCharacters = Envelope.TRADACOMS_ESCAPE_CHARACTER;
		dataElementReady = false;
	}

}
