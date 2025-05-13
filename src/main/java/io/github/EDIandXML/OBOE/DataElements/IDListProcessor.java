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

package io.github.EDIandXML.OBOE.DataElements;

import java.util.ArrayList;

/**
 * An interface for classes that process ID lists
 * 
 */

public interface IDListProcessor {

	/**
	 * Add code and description <br>
	 * An optional method.
	 *
	 * @param inCode     String code
	 * @param inDescribe String descriptive value
	 * @exception java.lang.UnsupportedOperationException
	 */
	public void add(String inCode, String inDescribe)
			throws java.lang.UnsupportedOperationException;

	/**
	 * tests if the passed code is valid
	 *
	 * @param inCode String to test
	 * @return boolean
	 */
	public boolean isCodeValid(String inCode);

	/**
	 * returns the descriptive value of the code, if it is not found then
	 * the code is returned
	 *
	 * @param inCode String to test
	 * @return String
	 */
	public String describe(String inCode);

	/**
	 * returns the code value for a descriptive value. if it is not found
	 * the value is returned
	 *
	 * @param inValue String to test
	 * @return String
	 */

	public String getCode(String inValue);

	/**
	 * returns a code at a specific position in ArrayList <br>
	 * An optional method.
	 *
	 * @param pos int String position
	 * @return String
	 * @exception java.lang.UnsupportedOperationException
	 */
	public String getCodeByPos(int pos)
			throws java.lang.UnsupportedOperationException;

	/**
	 * returns the code ArrayList
	 *
	 * @return ArrayList
	 */

	public ArrayList<String> getCodes();

	/**
	 * returns the value ArrayList
	 *
	 * @return ArrayList
	 */

	public ArrayList<String> getValues();

}
