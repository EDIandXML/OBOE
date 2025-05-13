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

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 */

public class X12Tokenizer extends Tokenizer implements ITokenizer {

	/** log4j object */
	static Logger logr = LogManager.getLogger(X12Tokenizer.class);

	/**
	 * builds the parsing object for a transaction set
	 * 
	 * @param inReader what's to be tokenized
	 * @param inDErr   DocumentError
	 * @throws OBOEException invalid token most likely
	 */
	public X12Tokenizer(Reader inReader, DocumentErrors inDErr)
			throws OBOEException {

		super(inDErr);

		int i;

		PushbackReader pbr = new PushbackReader(inReader, 107);
		char firstSeg[] = new char[107];
		try {
			if (pbr.read(firstSeg, 0, 107) != 107) {
				throw new OBOEException("expected data not read");
			}
			pbr.unread(firstSeg);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new OBOEException(ioe.getMessage());
		}

		int tokencnt = 0;
		boolean saveVersion = false;

		for (i = 0; i < 107; i++) {
			if (firstSeg[i] == firstSeg[3]) {
				tokencnt++;
				if (tokencnt == 11) {
					if (firstSeg[i + 1] != 'U') // hmmmm.... maybe it indicates
												// the repeater separator for
												// post 4010
					{
						repeatChar = firstSeg[i + 1];
						logr.debug("Found repeating character " + repeatChar);
					}
				}
				if (tokencnt == 12) {
					saveVersion = true;
					continue;
				}

				if (tokencnt == 13) {
					saveVersion = false;
				}
				if (tokencnt == 16) {
					if (i > 103) {
						if (inDErr != null) {
							inDErr.addError(0, "Envelope",
									"ISA segment too long, unable to continue",
									null, "E", null,
									DocumentErrors.ERROR_TYPE.Integrity);
						}
						logr.error(
								"ISA segment too long, unable to to continue");
						throw new OBOEException(
								"ISA segment too long, unable to to continue");
					}
					if (i < 103) {
						if (inDErr != null) {
							inDErr.addError(0, "Envelope",
									"ISA segment too short, attempting to continue",
									null, "E", null,
									DocumentErrors.ERROR_TYPE.Integrity);
						}
						logr.error(
								"ISA segment too short, attempting to continue");
					}

					if ((firstSeg[i + 1] == 10) || (firstSeg[i + 1] == 13)
							|| (firstSeg[i + 1] == '\n')) {
						if (inDErr != null) {
							inDErr.addError(0, "Envelope",
									"ISA segment in error, missing component element separator",
									null, "E", null,
									DocumentErrors.ERROR_TYPE.Integrity);
						} else {
							throw new OBOEException(
									"ISA segment in error, missing component element separator");
						}
					}

					tokenGroups[0] = firstSeg[i + 1];
					tokenseparatorCharacter = firstSeg[i + 2] + "";

					if (((firstSeg[i + 2] == 10) && (firstSeg[i + 3] == 13))
							|| ((firstSeg[i + 2] == 13)
									&& (firstSeg[i + 3] == 10))) {
						tokenseparatorCharacter = "\n";
					}

					separators.append(tokenseparatorCharacter);
					separators.append(firstSeg[3]);
					separators.append(tokenGroups[0]);
					if (repeatChar != 0) {
						separators.append(repeatChar);
					}

					break;
				}

			}
			if (saveVersion == true) {
				versionBuffer.append(firstSeg[i]);
			}
		}

		if (tokencnt != 16) {
			throw new OBOEException(
					"ISA Segment incorrect format, cannot continue");
		}

		/*
		 * tokenGroups[0] = firstSeg[104]; tokenseparatorCharacter =
		 * firstSeg[105]+"";
		 */

		// TransactionSetTokenizer = new DataTokenizer(str,
		// tokenseparatorCharacter, escapeCharacters);
		transactionSetTokenizer = new ReaderTokenizer(pbr,
				tokenseparatorCharacter, escapeCharacters);

		tokenseparatorCharacter = firstSeg[3] + "";

		if (((firstSeg[i + 2] == 10) && (firstSeg[i + 3] == 13))
				|| ((firstSeg[i + 2] == 13) && (firstSeg[i + 3] == 10))) {
			((ReaderTokenizer) transactionSetTokenizer)
					.setSkipChar(firstSeg[i + 2]);
		}

		dataElementReady = false;
	}
}
