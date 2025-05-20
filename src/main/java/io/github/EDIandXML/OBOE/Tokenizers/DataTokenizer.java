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

/**
 * class to assist in tokenizing input transaction sets <br>
 * similar to the java.lang.StringTokenizer. It seemed that
 * StringTokenizer couldn't return a zero-length element between 2
 * tokens - such as token(*) and string in is (***) it would not find 2
 * zero-length tokens. <br>
 * <p>
 * OBOE - Open Business Objects for EDI
 */

public class DataTokenizer implements IDataTokenizer {
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
	 * escape characters to allow tokens in the text
	 */
	protected String escapeCharacters = "";

	/**
	 * where all the substrings are stored
	 */
	protected ArrayList<String> allTokens = new ArrayList<String>();

	protected int positionInStream;

	/**
	 * constructor
	 *
	 * @param inString           - string to be tokenized
	 * @param inTokens           - list of tokens
	 * @param inEscapeCharacters - list of characters to escape and allow
	 *                           tokens in the text. pass an empty string or
	 *                           null if no escape characters used
	 */
	public DataTokenizer(String inString, String inTokens,
			String inEscapeCharacters) {
		int startPos = 0;
		tokenString = inString;
		stringLength = tokenString.length();
		tokens = inTokens;
		if (inEscapeCharacters != null) {
			escapeCharacters = inEscapeCharacters;
		}

		StringBuilder sb = new StringBuilder();

		currentPos = 0;

		while (currentPos < stringLength) {
			if (escapeCharacters.indexOf(tokenString.charAt(currentPos)) > -1) {
				// if ((tokens.indexOf(tokenString.charAt(currentPos + 1)) > -1)
				// || (escapeCharacters
				// .indexOf(tokenString.charAt(currentPos + 1))
				// > -1)) {
				sb.append(tokenString.charAt(currentPos));
				currentPos++;
				sb.append(tokenString.charAt(currentPos));
				currentPos++;
				continue;
				// }
			}
			if (tokens.indexOf(tokenString.charAt(currentPos)) > -1) {
				allTokens.add(sb.toString());
				sb = new StringBuilder();
				currentPos++;
				startPos = currentPos;
				continue;
			}
			sb.append(tokenString.charAt(currentPos));
			currentPos++;
		}
		if (currentPos != startPos) {
			allTokens.add(sb.toString());
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

	@Override
	public int getPositionInStream() {
		return positionInStream;
	}

	@Override
	public void setPositionInStream(int positionInStream) {
		this.positionInStream = positionInStream;
	}

}
