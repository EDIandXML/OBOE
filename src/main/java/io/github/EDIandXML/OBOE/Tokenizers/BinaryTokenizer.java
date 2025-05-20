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

package io.github.EDIandXML.OBOE.Tokenizers;

import java.util.ArrayList;

import io.github.EDIandXML.OBOE.Errors.OBOEException;

/**
 * class to assist in tokenizing input transaction sets OBOE - Open
 * Business Objects for EDI
 */

public class BinaryTokenizer implements IDataTokenizer {
	/**
	 * current position within tokenized string
	 */
	protected int currentPos;
	/**
	 * length of field
	 */
	protected int stringLength;
	/**
	 * current tokenized string
	 */
	protected String tokenString;
	/**
	 * what breaks up fields
	 */
	protected String tokens;

	/**
	 * where all the substrings are stored
	 */
	protected ArrayList<String> allTokens = new ArrayList<String>();

	/**
	 * constructor for Binary Tokenizer (X12 BIN Segments) <br>
	 * first field contains length of data <br>
	 * second field contains binary data (including token separators) <br>
	 * so if the incoming string is too short, we must go back to the main
	 * tokeninzer and ask for the rest of the string
	 * 
	 * @param inString      incoming string to tokenize
	 * @param inTokens      token separators
	 * @param mainTokenizer tokenizer that does that bulk of the work
	 * @exception OBOEException - most likely length error
	 */
	public BinaryTokenizer(String inString, String inTokens,
			ReaderTokenizer mainTokenizer) throws OBOEException {
		int startPos = 0;
		tokenString = inString;
		stringLength = tokenString.length();
		tokens = inTokens;

		currentPos = 0;
		while (currentPos < stringLength) {
			if (tokens.indexOf(tokenString.charAt(currentPos)) > -1) {
				allTokens.add(tokenString.substring(startPos, currentPos));
				currentPos++;
				startPos = currentPos;
				continue;
			}
			currentPos++;
		}
		if (currentPos != startPos) {
			allTokens.add(tokenString.substring(startPos, currentPos));
		}

		int len;
		try {
			len = Integer.parseInt(allTokens.get(1));
		} catch (java.lang.NumberFormatException nfe) {
			nfe.printStackTrace();
			throw new OBOEException(
					"BIN01 field not numeric: " + nfe.getMessage());
		}
		int dif = currentPos - startPos;
		if (len < dif) {
			throw new OBOEException(
					"BIN01 field specifies incorrect length, field value ="
							+ len + " length calculated as =" + dif);
		}
		if (len > dif) {
			byte b[] = allTokens.get(2).getBytes();
			byte needMoreData[] = mainTokenizer.getMoreData(len - dif);
			byte c[] = new byte[len];
			java.lang.System.arraycopy(c, 0, b, 0, b.length);
			java.lang.System.arraycopy(c, currentPos, needMoreData, 0, dif);

		}

		currentPos = -1;
	}

	/**
	 * to get next token
	 * 
	 * @return String - the next token
	 *
	 */
	@Override
	public String nextToken() {

		currentPos++;
		if (currentPos < allTokens.size()) {
			return allTokens.get(currentPos);
		}

		return null;

	}

	/**
	 * gets the token in tokenized string at a specifiec position
	 * 
	 * @param pos int position, if < 0 or > the total returns null
	 * @return String
	 */
	@Override
	public String getTokenAt(int pos) {
		if (pos < 0) {
			return null;
		}
		if (pos < allTokens.size()) {
			return allTokens.get(pos);
		}
		return null;

	}

	/**
	 * indicates if there are more data elements to parse
	 * 
	 * @return boolean more to parse?
	 *
	 */
	@Override
	public boolean hasMoreElements() {
		return (currentPos < allTokens.size());
	}

	/**
	 * how many tokens in list
	 * 
	 * @return int - a count
	 *
	 */
	@Override
	public int countTokens() {

		return allTokens.size();
	}

	/**
	 */
	public String getTokenString() {
		return tokenString;
	}

	int positionInStream = -1;

	@Override
	public int getPositionInStream() {
		return positionInStream;
	}

	@Override
	public void setPositionInStream(int positioninStream) {
		this.positionInStream = positioninStream;
	}

}
