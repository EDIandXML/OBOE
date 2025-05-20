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

/**
 * interface for DataTokenizers
 *
 * <P>
 * OBOE - Open Business Objects for EDI
 * 
 *
 */

public interface IDataTokenizer {
	/**
	 * gets the next token in tokenized string
	 *
	 * @return String
	 */
	public String nextToken();

	/**
	 * gets the token in tokenized string at a specifiec position
	 *
	 * @param pos int position, if < 0 or > the total returns null
	 * @return String
	 */
	public String getTokenAt(int pos);

	/**
	 * tests if more tokens are available
	 *
	 * @return true or false
	 */
	public boolean hasMoreElements();

	/**
	 * returns the number of tokens left to process
	 *
	 * @return int
	 */
	public int countTokens();

	/**
	 * returns the byte offset position of the token in the overall input
	 * stream
	 *
	 * @return int
	 */
	public int getPositionInStream();

	/**
	 * sets the byte offset position of the token in the input stream
	 *
	 * @param positioninStream int
	 */
	public void setPositionInStream(int positionInStream);

}
