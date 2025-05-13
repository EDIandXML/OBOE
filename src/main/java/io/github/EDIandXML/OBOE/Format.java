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
 * Defines supported file and data formats for OBOE (Open Business
 * Objects for EDI). Used in processing and translating EDI and XML data
 * across various standards, such as ANSI X12, UN/EDIFACT, and XML-based
 * formats.
 *
 * @author Joe McVerry
 * @since 2025
 */
public enum Format {

	/**
	 * Indicates an undefined or unknown format.
	 */
	UNDEFINED,

	/**
	 * Represents a custom or proprietary format specific to an OBOE
	 * implementation.
	 */
	PREBUILD_FORMAT,

	/**
	 * Represents generic XML format without specific validation.
	 */
	XML_FORMAT,

	/**
	 * Represents validated XML format, typically conforming to a specific
	 * schema.
	 */
	VALID_XML_FORMAT,

	/**
	 * Represents validated XML format with positional data, such as EDI
	 * segment positions.
	 */
	VALID_XML_FORMAT_WITH_POSITION,

	/**
	 * Represents ANSI X12 EDI format, commonly used in North American
	 * industries.
	 */
	X12_FORMAT,

	/**
	 * Represents UN/EDIFACT EDI format, widely used in international trade.
	 */
	EDIFACT_FORMAT,

	/**
	 * Represents TRADACOMS EDI format, primarily used in UK retail sectors.
	 */
	TRADACOMS_FORMAT,

	/**
	 * Represents Automated Clearing House (ACH) format, used for financial
	 * EDI transactions.
	 */
	ACH_FORMAT,

	/**
	 * Represents comma-separated values (CSV) format.
	 */
	CSV_FORMAT;

	/**
	 * Parses a string to a {@code Format} enum constant.
	 *
	 * @param name the name of the format (case-insensitive)
	 * @return the corresponding {@code Format}, or {@code UNDEFINED} if the
	 *         name is invalid or null
	 */
	public static Format fromString(String name) {
		if (name == null) {
			return UNDEFINED;
		} // as
		String trimmedName = name.trim();
		if (trimmedName.isEmpty()) {
			return UNDEFINED;
		}
		try {
			return valueOf(trimmedName.toUpperCase());
		} catch (IllegalArgumentException e) {
			return UNDEFINED;
		}
	}

	/**
	 * Checks if this format is an EDI standard (X12, EDIFACT, TRADACOMS, or
	 * ACH).
	 *
	 * @return true if the format is an EDI standard, false otherwise
	 */
	public boolean isEDIFormat() {
		return this == X12_FORMAT || this == EDIFACT_FORMAT
				|| this == TRADACOMS_FORMAT || this == ACH_FORMAT;
	}

	/**
	 * Checks if this format is XML-based (XML_FORMAT, VALID_XML_FORMAT, or
	 * VALID_XML_FORMAT_WITH_POSITION).
	 *
	 * @return true if the format is XML-based, false otherwise
	 */
	public boolean isXMLFormat() {
		return this == XML_FORMAT || this == VALID_XML_FORMAT
				|| this == VALID_XML_FORMAT_WITH_POSITION;
	}

	/**
	 * Returns a human-readable name for the format, suitable for logs, UIs,
	 * or error messages.
	 *
	 * @return a display-friendly name for the format
	 */
	public String toDisplayName() {
		return switch (this) {
		case UNDEFINED -> "Undefined";
		case PREBUILD_FORMAT -> "Prebuilt Format";
		case XML_FORMAT -> "XML";
		case VALID_XML_FORMAT -> "Validated XML";
		case VALID_XML_FORMAT_WITH_POSITION -> "Validated XML with Position";
		case X12_FORMAT -> "ANSI X12";
		case EDIFACT_FORMAT -> "UN/EDIFACT";
		case TRADACOMS_FORMAT -> "TRADACOMS";
		case ACH_FORMAT -> "ACH";
		case CSV_FORMAT -> "CSV";
		default -> name();
		};
	}
}