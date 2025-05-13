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

import io.github.EDIandXML.OBOE.Containers.MetaContainer;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors.ERROR_TYPE;

/**
 * interface for Tokenizers
 *
 * <P>
 * OBOE - Open Business Objects for EDI
 * 
 */

public interface ITokenizer {
	/**
	 * number of dataelements in token string
	 * 
	 * @return int
	 */
	public int countDataElements();

	/**
	 * get the contents for the current data element
	 * 
	 * @return String
	 */
	public String getCurrentDataElement();

	/**
	 * gets the position within segment or composite of the current data
	 * element
	 * 
	 * @return int
	 */
	public int getDataElementPos();

	/**
	 * look ahead to the next data element
	 * 
	 * @return String
	 */
	public String getNextDataElement();

	/**
	 * returns data element at preparsed position
	 * 
	 * @param pos int position
	 * @return String may return null
	 */
	public String getDataElementAt(int pos);

	/**
	 * gets the next DataTokenizer object
	 * 
	 * @param inSegContainer SegmentContainer requesting the next segment.
	 *                       Used for error handling and recovery.
	 * @return IDataTokenizer
	 */
	public IDataTokenizer getNextSegment(MetaContainer inSegContainer);

	/**
	 * gets the current segment position within tokenized object
	 * 
	 * @return int
	 */
	public int getSegmentPos();

	/**
	 * determine if there are more data elements to parse in token
	 * 
	 * @return true or false
	 */
	public boolean hasMoreDataElements();

	/**
	 * determine if there are more segments to parse
	 * 
	 * @return true or false
	 */
	public boolean hasMoreSegments();

	/**
	 * is there a data element to parse or a segment
	 * 
	 * @return true - data element false - segment
	 */
	public boolean isThereADataElement();

	/**
	 * reset for look ahead
	 * 
	 * @return IDataTokenizer
	 */
	public IDataTokenizer resetSegment();

	/**
	 * turn the current tokenizer into a subfield tokenizer
	 * 
	 * @return IDataTokenizer
	 */

	public IDataTokenizer makeSubfieldTokenizer();

	/**
	 * adds an error description to the OBOEParserErrors object
	 * 
	 * @param inDescription description of the error,
	 */

	public void reportError(String inDescription);

	/**
	 * adds an error description to the OBOEParserErrors object
	 * 
	 * @param inDescription         description of the error,
	 * @param inRequestingContainer segment, table...
	 * @param inCode                720 code
	 * @param inObject              object reporting erro
	 * @param ERROR_TYPE
	 */

	public void reportError(String inDescription,
			MetaContainer inRequestingContainer, String inCode, Object inObject,
			ERROR_TYPE type);

	/**
	 * returns the escape character string
	 * 
	 * @return String
	 */
	public String getEscapeCharacters();

	/**
	 * returns the repeater delimiter
	 * 
	 * @return String
	 */
	public String getRepeaterCharacter();

	/**
	 * returns the element delimiter
	 * 
	 * @return String
	 */
	public String getElementSeparator();

	@Override
	public String toString();

	/**
	 * returns the byte position in the input stream of the current token
	 * 
	 * @return int
	 */
	public int getInputByteCount();

	/**
	 * returns the relative segment position
	 * 
	 * @return int
	 */
	public int getSegmentPosition();
}
