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

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

import io.github.EDIandXML.OBOE.Errors.OBOEException;

/**
 * class to assist in tokenizing input transaction sets
 * 
 * OBOE - Open Business Objects for EDI
 */

public class ReaderTokenizer implements IDataTokenizer {
	/**
	 * current position within tokenized string
	 */
	protected int currentPos;
	/**
	 * current tokenized string
	 */
	protected PushbackReader tokenReader;
	/**
	 * what breaks up fields
	 */
	protected String tokens;

	/**
	 * cr/lf used as a segment delimiter can also be lf/cr we want to skip
	 * the second character
	 */

	private char skipChar = (char) -1;

	/**
	 * escape characters to allow tokens in the text
	 */
	protected String escapeCharacters = "";

	/**
	 * constructor
	 * 
	 * @param inReader           - Reader object containing tokenized data
	 * @param inTokens           - list of tokens
	 * @param inEscapeCharacters - list of characters to escape and allow
	 *                           tokens in the text pass an empty string or
	 *                           null if no escape characters used
	 */
	public ReaderTokenizer(Reader inReader, String inTokens,
			String inEscapeCharacters) {
		currentPos = -1;
		tokenReader = new PushbackReader(inReader, 1);
		tokens = inTokens;

		if (inEscapeCharacters != null) {
			escapeCharacters = inEscapeCharacters;
		}

	}

	/**
	 * to get next token
	 * 
	 * @return String - the next token
	 *
	 */
	@Override
	public String nextToken() {

		StringBuilder StringBuilder = new StringBuilder(80);

		int currentValue;
		char currentChar;

		try {
			while (tokenReader.ready()) {
				currentValue = tokenReader.read();
				if (currentValue == -1) {
					break;
				}

				currentChar = (char) currentValue;
				if (escapeCharacters.indexOf(currentChar) > -1) {
					char tempChar = currentChar;
					int tempValue = tokenReader.read();
					if (tempValue == -1) {
						break;
					}
					currentChar = (char) tempValue;
					if ((tokens.indexOf(currentChar) > -1)
							&& (escapeCharacters.indexOf(currentChar) > -1)) {
						// escaping a delimiter so keep it
						StringBuilder.append(currentChar);
					} else {
						StringBuilder.append(tempChar);
						StringBuilder.append(currentChar);
					}
					continue;
				}

				if (currentChar == skipChar) {
					// ignore the carriage return and catch the linefeed
					continue;
				}
				if (tokens.indexOf(currentChar) > -1) {
					break;
				}
				StringBuilder.append(currentChar);
			}
		} catch (java.io.IOException ioe) {
			return null;
		}

		return StringBuilder.toString(); // trim for one last cleanup
	}

	/**
	 * pulls more data out of reader stream for binarytokenizer
	 * 
	 * @param inLength int length
	 * @return byte[]
	 * @exception OBOEException - not enough data read
	 */
	public byte[] getMoreData(int inLength) throws OBOEException {

		byte readArray[] = new byte[inLength];

		int pos = 0;
		for (pos = 0; pos < tokens.length(); pos++) {
			readArray[pos] = (byte) tokens.charAt(pos);
			// assume the end marker not read
		}

		int currentValue;
		char currentChar = ' ';

		try {
			while (tokenReader.ready() && (pos < inLength)) {
				currentValue = tokenReader.read();
				if (currentValue == -1) {
					break;
				}
				readArray[pos] = (byte) currentValue;
				pos++;
			}
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
			throw new OBOEException("IO error: " + ioe.getMessage());
		}

		if (pos != inLength) {
			throw new OBOEException("Binary read length error.  Asked for "
					+ inLength + " read only " + pos);
		}

		try {
			currentValue = tokenReader.read();
			if (currentValue != -1) {
				currentChar = (char) currentValue;
			}
		} catch (java.io.IOException ioe) {
			ioe.printStackTrace();
			throw new OBOEException("IO error: " + ioe.getMessage());
		}

		if (tokens.indexOf(currentChar) < 0) {
			throw new OBOEException(
					"Binary read error.  Last character not end of segment delimiter found "
							+ currentChar);
		}

		return readArray; // trim for one last cleanup
	}

	/**
	 * gets the token in tokenized string at a specifiec position
	 * 
	 * @param pos int position, if < 0 or > the total returns null
	 * @return String
	 */
	@Override
	public String getTokenAt(int pos) {
		return null;

	}

	public void setSkipChar(char inChar) {
		skipChar = inChar;
	}

	/**
	 * indicates if there are more data elements to parse
	 * 
	 * @return boolean more to parse?
	 *
	 */
	@Override
	public boolean hasMoreElements() {
		try {
			int c = tokenReader.read();
			tokenReader.unread(c);
			if (c == -1) {
				return false;
			}
			return true; // moreTokens;
		} catch (IOException ioe) {
			return false;
		}

	}

	/**
	 * how many tokens in list
	 * 
	 * @return int - a count
	 *
	 */
	@Override
	public int countTokens() {

		return -1;
	}

	int positionInStream = -1;

	@Override
	public int getPositionInStream() {
		return positionInStream;
	}

	@Override
	public void setPositionInStream(int positionInStream) {
		this.positionInStream = positionInStream;

	}
}
