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
package io.github.EDIandXML.OBOE.Containers;

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.DataElements.DataElement;
import io.github.EDIandXML.OBOE.DataElements.Element;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;

/**
 * Defines a contract for containers in OBOE (Open Business Objects for
 * EDI) that manage data elements and composite elements, such as
 * segments or composite elements. Typically implemented by classes
 * representing EDI structures, this interface provides methods for
 * accessing, building, and validating elements within the container.
 *
 * 
 * @since 2025
 * @see Element
 * @see DataElement
 * @see IContainer
 * @see ContainerType
 * @see Format
 */
public interface IElementContainer {

	/**
	 * Retrieves an element by its ID. If multiple elements share the same
	 * ID, the first matching element is returned, making this method less
	 * effective for unique lookups.
	 *
	 * @param id the ID of the element to retrieve
	 * @return the matching element
	 * @throws OBOEException if the ID is unknown
	 */
	Element getElement(String id) throws OBOEException;

	/**
	 * Retrieves a data element by its name, starting at the specified
	 * offset.
	 *
	 * @param name   the name of the data element
	 * @param offset the starting index (0-based) for the search
	 * @return the matching data element
	 * @throws OBOEException if the data element is not found
	 */
	DataElement getDataElementByName(String name, int offset)
			throws OBOEException;

	/**
	 * Retrieves an element by its ID and offset, useful when multiple
	 * elements share the same ID.
	 *
	 * @param id     the ID of the element to retrieve
	 * @param offset the starting index (0-based) for the search
	 * @return the matching element
	 * @throws OBOEException if the ID is unknown
	 */
	Element getElement(String id, int offset) throws OBOEException;

	/**
	 * Retrieves an element by its 1-based position within the container.
	 *
	 * @param position the 1-based position of the element
	 * @return the element at the specified position
	 * @throws OBOEException if the position is invalid or out of range
	 */
	Element getElement(int position) throws OBOEException;

	/**
	 * Returns the number of defined elements (data elements and composite
	 * elements) in the container.
	 *
	 * @return the count of elements
	 */
	int getContainerSize();

	/**
	 * Checks if the element at the specified 1-based position is a data
	 * element.
	 *
	 * @param position the 1-based position to check
	 * @return true if the element is a data element, false otherwise
	 */
	boolean isDataElement(int position);

	/**
	 * Checks if the element at the specified 1-based position is a
	 * composite element.
	 *
	 * @param position the 1-based position to check
	 * @return true if the element is a composite element, false otherwise
	 */
	boolean isCompositeElement(int position);

	/**
	 * Builds a data element at the specified 1-based position using the
	 * container's template.
	 *
	 * @param position the 1-based position of the element to build
	 * @return the constructed data element
	 * @throws OBOEException if the position is invalid
	 */
	Element buildElement(int position) throws OBOEException;

	/**
	 * Builds a data element with the specified ID using the container's
	 * template.
	 *
	 * @param id the ID of the element to build
	 * @return the constructed data element
	 * @throws OBOEException if the ID is unknown
	 */
	Element buildElement(String id) throws OBOEException;

	/**
	 * Builds a data element with the specified ID and offset using the
	 * container's template.
	 *
	 * @param id     the ID of the element to build
	 * @param offset the starting index (0-based) for the search
	 * @return the constructed data element
	 * @throws OBOEException if the ID is unknown
	 */
	Element buildElement(String id, int offset) throws OBOEException;

	/**
	 * Retrieves the value of a data element by its ID. Returns an empty
	 * string if the element is not built or does not exist.
	 *
	 * @param id the ID of the data element
	 * @return the value of the data element, or an empty string if not set
	 * @throws OBOEException if the ID is unknown or the element is a
	 *                       composite element
	 */
	String getDataElementValue(String id) throws OBOEException;

	/**
	 * Retrieves the value of a data element by its 1-based position.
	 * Returns an empty string if the element is not built or does not
	 * exist.
	 *
	 * @param position the 1-based position of the data element
	 * @return the value of the data element, or an empty string if not set
	 * @throws OBOEException if the position is invalid or the element is a
	 *                       composite element
	 */
	String getDataElementValue(int position) throws OBOEException;

	/**
	 * Sets the value of a data element by its ID, building the element if
	 * it does not exist.
	 *
	 * @param id    the ID of the data element
	 * @param value the value to set
	 * @return the data element that was set
	 * @throws OBOEException if the ID is unknown or the element is a
	 *                       composite element
	 */
	Element setDataElementValue(String id, String value) throws OBOEException;

	/**
	 * Sets the value of a data element by its ID and offset, building the
	 * element if it does not exist.
	 *
	 * @param id     the ID of the data element
	 * @param offset the starting index (0-based) for the search
	 * @param value  the value to set
	 * @return the data element that was set
	 * @throws OBOEException if the ID is unknown or the element is a
	 *                       composite element
	 */
	Element setDataElementValue(String id, int offset, String value)
			throws OBOEException;

	/**
	 * Sets the value of a data element by its 1-based position, building
	 * the element if it does not exist.
	 *
	 * @param position the 1-based position of the data element
	 * @param value    the value to set
	 * @return the data element that was set
	 * @throws OBOEException if the position is invalid or the element is a
	 *                       composite element
	 */
	Element setDataElementValue(int position, String value)
			throws OBOEException;

	/**
	 * Validates the container and its elements against their template
	 * rules.
	 *
	 * @return true if the container is valid, false otherwise
	 */
	boolean validate();

	/**
	 * Validates the container and its elements, collecting errors in the
	 * provided DocumentErrors.
	 *
	 * @param errors the DocumentErrors object to collect validation errors
	 * @return true if the container is valid, false otherwise
	 */
	boolean validate(DocumentErrors errors);

	/**
	 * Returns the formatted text representation of the container in the
	 * specified format.
	 *
	 * @param format the format for the output (e.g., X12, XML)
	 * @return the formatted text
	 */
	String getFormattedText(Format format);

	/**
	 * Returns the total number of elements (data elements and composite
	 * elements) in the container.
	 *
	 * @return the element count
	 */
	int getElementCount();

	/**
	 * Removes trailing empty elements from the container, adjusting its
	 * size.
	 *
	 * @return the number of elements removed
	 */
	int trim();

	/**
	 * Finds the index of the first element with the specified XML tag,
	 * starting at the given position.
	 *
	 * @param tag     the XML tag to search for
	 * @param startAt the 0-based starting index for the search
	 * @return the 0-based index of the matching element, or -1 if not found
	 */
	int doIUseThisXMLElement(String tag, int startAt);

	/**
	 * Applies default values to the container's elements based on their
	 * template definitions.
	 */
	void useDefault();

	/**
	 * Retrieves the primary identifying data element for the container, if
	 * defined.
	 *
	 * @return the primary data element
	 * @throws OBOEException if no primary data element is defined
	 */
	Element getPrimaryIDDE() throws OBOEException;
}