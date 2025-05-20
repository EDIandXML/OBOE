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
package io.github.EDIandXML.OBOE;

/**
 * 
 * Defines a contract for identifying fields in OBOE (Open Business
 * Objects for EDI). Used by {@code Composite} and {@code DataElement}
 * classes in an EDI and XML translator. Implementations should provide
 * unique identifiers and corresponding XML tags for elements in the
 * translation process.
 *
 * @author Joe McVerry
 * @since 2025
 */
public interface Identifier {
	/**
	 * Returns the unique identifier for the element. Implementations should
	 * ensure the ID is non-null and non-empty.
	 *
	 * @return the unique identifier as a non-null string
	 */
	String getID();

	/**
	 * Returns the XML tag associated with the element. Implementations
	 * should ensure the tag is a valid, non-null XML tag name adhering to
	 * XML naming conventions (e.g., no spaces, valid characters).
	 *
	 * @return the XML tag as a non-null string
	 */
	String getShortName();
}