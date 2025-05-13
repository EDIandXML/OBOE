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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.Containers.MetaContainer;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors.ERROR_TYPE;
import io.github.EDIandXML.OBOE.Errors.OBOEException;

/**
 * class to assist in tokenizing input transaction sets <br>
 * x12 field separator uses 3rd byte of input string <br>
 * x12 segment separator uses 16th field + 1 byte field, if it sees a cr
 * character then checks for a lf character and then assumes a \\n
 * character <br>
 * EDIFACT uses different control positions. see header segment.
 * 
 */

public abstract class Tokenizer implements ITokenizer {

	/**
	 * StringTokenizer data contents
	 */
	// protected StringTokenizer transactionSetTokenizer;
	protected IDataTokenizer transactionSetTokenizer;
	/**
	 * String current token
	 */
	protected String currentTokenStringToken = "";
	/**
	 * String current tokenizer
	 */
	protected IDataTokenizer currentTokenizer;
	/**
	 * String current data element value
	 */
	protected String currentDataElement;
	/**
	 * boolean has it been processed yet?
	 */
	protected boolean dataElementReady;
	/**
	 * String position within
	 */
	protected int segmentPos = 0;
	/**
	 * String position within
	 */
	protected int dataelementPos = -1;
	/**
	 * Character that breaks up fields
	 */
	protected String tokenseparatorCharacter;

	/**
	 * String simple array for helper routine
	 */

	protected char tokenGroups[] = { ' ' };

	/** allow token characters in text */
	protected String escapeCharacters = "";

	/** all the tokens separator */
	protected StringBuilder separators = new StringBuilder(4);

	protected MetaContainer requestingContainer = null;

	protected DocumentErrors dErr = null;

	protected String currentSegID = "";

	/** token the breaks up repeating elements */
	protected char repeatChar = 0;
	/** log4j object */
	static Logger logr = LogManager.getLogger(Tokenizer.class);

	/** version id in header works only for X12 I11 */
	protected StringBuilder versionBuffer = new StringBuilder();

	/** track the number of characters read */
	private int inputByteCount = 0;

	/**
	 * builds the parsing object for a transaction set
	 * 
	 * @param inDErr DocumentError
	 * @throws OBOEException invalid token most likely
	 */
	public Tokenizer(DocumentErrors inDErr) throws OBOEException {
		super();
		dErr = inDErr;
	}

	/**
	 * return the separator characters that were used to parse the document
	 * 
	 * @return String
	 */

	public String getSeparators() {
		return new String(separators);
	}

	/**
	 * returns the version id in the header segments presently only works
	 * for X12 field I11.
	 * 
	 * @return String
	 */

	public String getVersion() {
		return versionBuffer.toString();
	}

	/**
	 * returns the next segment to be parsed
	 * 
	 * @return IDataTokenizer the next segment
	 *
	 */
	@Override
	public IDataTokenizer getNextSegment(MetaContainer inSegContainer) {
		if (currentTokenStringToken.startsWith("UNA")) {
			String temp = currentTokenStringToken;
			currentTokenStringToken = "";
			currentTokenizer = new DataTokenizer(temp, tokenseparatorCharacter,
					escapeCharacters);
			currentSegID = "UNA";
			return currentTokenizer;
		}
		requestingContainer = inSegContainer;
		segmentPos++;
		dataelementPos = -1;

		currentTokenStringToken = transactionSetTokenizer.nextToken();
		int currentTokenLegth = 0;
		if (currentTokenStringToken != null) {
			currentTokenLegth = currentTokenStringToken.length();
		}

		if (transactionSetTokenizer.hasMoreElements()
				&& (currentTokenStringToken.length() == 0)) {
			dErr.addError(segmentPos, currentSegID, "Invalid token length",
					inSegContainer, "4", inSegContainer,
					DocumentErrors.ERROR_TYPE.Integrity);
			return new DataTokenizer("", "", "");
		}

		if (currentTokenStringToken.startsWith("BIN")) {
			currentTokenizer = new BinaryTokenizer(currentTokenStringToken,
					tokenseparatorCharacter,
					(ReaderTokenizer) transactionSetTokenizer);
		} else {
			currentTokenizer = new DataTokenizer(currentTokenStringToken,
					tokenseparatorCharacter, escapeCharacters);
		}

		currentTokenizer.setPositionInStream(inputByteCount);
		dataElementReady = false;

		currentSegID = currentTokenizer.getTokenAt(0);

		inputByteCount += currentTokenLegth;

		return currentTokenizer;
	}

	/**
	 * helper routine for EDIFACT UNA segment
	 *
	 * @return String
	 */
	public String getRestOfSegment() {

		return ((DataTokenizer) currentTokenizer).getTokenString().substring(3,
				8);
	}

	/**
	 * indicates if there are more segments to parse
	 * 
	 * @return boolean more to parse?
	 *
	 */
	@Override
	public boolean hasMoreSegments() {
		return transactionSetTokenizer.hasMoreElements();
	}

	/**
	 * resets the current segment to the beginning - an inverse look-ahead
	 * method
	 * 
	 * @return IDataTokenizer the next segment
	 *
	 */
	@Override
	public IDataTokenizer resetSegment() {
		dataelementPos = -1;

		currentTokenizer = new DataTokenizer(currentTokenStringToken,
				tokenseparatorCharacter, escapeCharacters);
		getNextDataElement();
		return currentTokenizer;
	}

	/**
	 * returns the position (line number?) of the current segment being
	 * tokenized. value is relative to zero
	 * 
	 * @return int the current segment position (line number)
	 *
	 */
	@Override
	public int getSegmentPos() {
		return segmentPos;
	}

	/**
	 * returns a count of dataelements in segment
	 * 
	 * @return int the number of data elements
	 *
	 */
	@Override
	public int countDataElements() {
		return currentTokenizer.countTokens();
	}

	/**
	 * returns the currently parsed data element. It does not return a data
	 * element object but a value.
	 * 
	 * @return String current value of data element
	 *
	 */
	@Override
	public String getCurrentDataElement() {
		return currentDataElement;
	}

	/**
	 * returns the next data element to be parsed
	 * 
	 * @return String current value of data element
	 *
	 */
	@Override
	public String getNextDataElement() {

		if (currentTokenizer.hasMoreElements()) {
			currentDataElement = currentTokenizer.nextToken();
			dataelementPos++;
			dataElementReady = true;
		} else {
			currentDataElement = "";
			dataElementReady = false;
		}

		return currentDataElement;

	}

	/**
	 * returns the data element parsed at a particular postion
	 * 
	 * @param pos int
	 * @return String current value of data element
	 *
	 */
	@Override
	public String getDataElementAt(int pos) {
		return currentTokenizer.getTokenAt(pos);
	}

	/**
	 * indicates if there are more data elements to parse
	 * 
	 * @return boolean more to parse?
	 *
	 */
	@Override
	public boolean hasMoreDataElements() {
		return currentTokenizer.hasMoreElements();
	}

	/**
	 * indicates if there is data element to parse
	 * 
	 * @return boolean data element ready?
	 *
	 */
	@Override
	public boolean isThereADataElement() {
		return dataElementReady;
	}

	/**
	 * returns the position of the current dataelement being tokenized.
	 * Value is relative to zero.
	 * 
	 * @return int the current datalement position
	 *
	 */
	@Override
	public int getDataElementPos() {
		return dataelementPos;
	}

	/**
	 * returns a subfield tokeinzer
	 * 
	 * @return IDataTokenizer
	 *
	 */
	@Override
	public IDataTokenizer makeSubfieldTokenizer() {
		return new DataTokenizer(currentDataElement, new String(tokenGroups),
				escapeCharacters);
	}

	/**
	 * returns the segment string // used by EDIFACT envelope parser
	 * 
	 * @return String
	 *
	 */
	public String getCurrentTokenStringToken() {
		return currentTokenStringToken;
	}

	/**
	 * adds an error description to the OBOEParserErrors object
	 * 
	 * @param inDescription description of the error,
	 */

	@Override
	public void reportError(String inDescription) {
		dErr.addError(getSegmentPos(), getSegmentID(), inDescription,
				requestingContainer, "?", null,
				DocumentErrors.ERROR_TYPE.Integrity);
	}

	/**
	 * adds an error description to the OBOEParserErrors object
	 * 
	 * @param inDescription         description of the error,
	 * @param inRequestingContainer segment, table...
	 * @param inCode                err code
	 * @param inObject              object reporting the error
	 * @param inLevel               int error level
	 */

	@Override
	public void reportError(String inDescription,
			MetaContainer inRequestingContainer, String inCode, Object inObject,
			ERROR_TYPE inType) {
		dErr.addError(getSegmentPos(), getSegmentID(), inDescription,
				inRequestingContainer, inCode, inObject, inType);
	}

	/**
	 * adds an error description to the OBOEParserErrors object
	 * 
	 * @param inDescription description of the error,
	 * @param inCode        err code
	 */

	public void reportError(String inDescription, String inCode) {
		dErr.addError(getSegmentPos(), getSegmentID(), inDescription,
				requestingContainer, inCode, null,
				DocumentErrors.ERROR_TYPE.Integrity);
	}

	/**
	 * gets the DocumentErrors object
	 * 
	 * @return DocumentErrors object
	 */

	public DocumentErrors getReportError() {
		return dErr;
	}

	/**
	 * sets the last container that made a getNextSegment request
	 * 
	 * @param inMC a Container
	 */

	public void setLastSegmentContainer(MetaContainer inMC) {
		requestingContainer = inMC;
	}

	/**
	 * returns the last container that made a getNextSegment request
	 * 
	 * @return MetaContainer, maybe null
	 */

	public MetaContainer getLastMetaContainer() {
		return requestingContainer;
	}

	/**
	 * returns the escape character string
	 * 
	 * @return String
	 */
	@Override
	public String getEscapeCharacters() {
		return escapeCharacters;
	}

	/**
	 * returns what is suppose to be the id of the segment being parsed.
	 * 
	 * @return String
	 */
	public String getSegmentID() {
		return currentSegID;
	}

	/**
	 */
	@Override
	public String getRepeaterCharacter() {
		return repeatChar + "";
	}

	/**
	 */
	@Override
	public String getElementSeparator() {
		return new String(tokenGroups);
	}

	/**
	 * returns the byte positin in the input stream of the current token
	 *
	 * @return int
	 */
	@Override
	public int getInputByteCount() {
		return inputByteCount;
	}

	/**
	 * returns the relative segment position
	 *
	 * @return int
	 */
	@Override
	public int getSegmentPosition() {
		return segmentPos;
	}

	/**
	 * returns the current token string.
	 */
	@Override
	public String toString() {
		return currentTokenStringToken;
	}
}
