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
package io.github.EDIandXML.OBOE.Errors;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * Manages errors in XML documents for the OBOE (Open Business Objects
 * for EDI) framework. Tracks and reports errors related to XML parsing
 * and validation.
 */
public class XMLErrors {
	/** Logger for this class */
	private static final Logger logger = LogManager.getLogger(XMLErrors.class);

	/** Record to store XML error details */
	private record ErrorRecord(String description, IContainedObject container,
			Node node, Integer lineNumber, Integer level, Boolean reportable) {
	}

	/** List of error records */
	private final ArrayList<ErrorRecord> errorRecords = new ArrayList<>();

	/** Minimum level of error checking (defaults to 2) */
	private int errorLevel = 2;

	/**
	 * Constructs an XMLErrors instance, initializing the error level from
	 * OBOE properties.
	 */
	public XMLErrors() {
		try {
			String errorLevelProperty = Util
					.getOBOEProperty(Util.ERROR_LEVEL_TO_REPORT);
			if (errorLevelProperty != null) {
				int level = Integer.parseInt(errorLevelProperty);
				setErrorLevel(level);
			}
		} catch (NumberFormatException | IOException | OBOEException e) {
			logger.error("Error initializing error level: {}", e.getMessage(),
					e);
		}
	}

	/**
	 * Sets the minimum level of compliance checking (1 to 10).
	 *
	 * @param level The error level to set
	 * @throws IllegalArgumentException If the level is out of range (1-10)
	 */
	public void setErrorLevel(int level) {
		if (level < 1 || level > 10) {
			logger.error("Error level out of range (1-10). Value passed: {}",
					level);
			throw new IllegalArgumentException(
					"Error level must be between 1 and 10");
		}
		this.errorLevel = level;
	}

	/**
	 * Gets the minimum level of compliance checking.
	 *
	 * @return The current error level
	 */
	public int getErrorLevel() {
		return errorLevel;
	}

	/**
	 * Adds an XML error with a description, container, and DOM node.
	 *
	 * @param description The error description
	 * @param container   The container associated with the error, or null
	 * @param node        The DOM node associated with the error, or null
	 * @param level       The error level
	 */
	public void addError(String description, IContainedObject container,
			Node node, int level) {
		if (level <= errorLevel) {
			errorRecords.add(new ErrorRecord(description, container, node, null,
					level, false));
		}
	}

	/**
	 * Adds an XML error with a line number, description, and container.
	 *
	 * @param lineNumber  The line number associated with the error
	 * @param description The error description
	 * @param container   The container associated with the error, or null
	 * @param level       The error level
	 */
	public void addError(int lineNumber, String description,
			IContainedObject container, int level) {
		if (level <= errorLevel) {
			errorRecords.add(new ErrorRecord(description, container, null,
					lineNumber, level, false));
		}
	}

	/**
	 * Returns the number of recorded errors.
	 *
	 * @return The error count
	 */
	public int getErrorCount() {
		return errorRecords.size();
	}

	/**
	 * Gets the description of the error at the specified index.
	 *
	 * @param index The index in the error records list
	 * @return The error description
	 */
	public String getErrorDescription(int index) {
		return errorRecords.get(index).description();
	}

	/**
	 * Gets the container associated with the error at the specified index.
	 *
	 * @param index The index in the error records list
	 * @return The container, or null if not available
	 */
	public IContainedObject getContainer(int index) {
		return errorRecords.get(index).container();
	}

	/**
	 * Checks if the error at the specified index is reportable (e.g., via
	 * 997/CONTRL).
	 *
	 * @param index The index in the error records list
	 * @return True if reportable, false otherwise
	 */
	public boolean isReportable(int index) {
		Boolean reportable = errorRecords.get(index).reportable();
		return reportable != null && reportable;
	}

	/**
	 * Gets the error level at the specified index.
	 *
	 * @param index The index in the error records list
	 * @return The error level, or 0 if not set
	 */
	public int getLevel(int index) {
		Integer level = errorRecords.get(index).level();
		return level != null ? level : 0;
	}

	/**
	 * Gets the line number associated with the error at the specified
	 * index.
	 *
	 * @param index The index in the error records list
	 * @return The line number, or 0 if not set
	 */
	public int getLineNumber(int index) {
		Integer lineNumber = errorRecords.get(index).lineNumber();
		return lineNumber != null ? lineNumber : 0;
	}

	/**
	 * Gets the DOM node associated with the error at the specified index.
	 *
	 * @param index The index in the error records list
	 * @return The DOM node, or null if not set
	 */
	public Node getNode(int index) {
		return errorRecords.get(index).node();
	}

	/**
	 * Returns an array of formatted error messages, or null if no errors
	 * exist.
	 *
	 * @return An array of error messages, or null if empty
	 */
	public String[] getError() {
		if (getErrorCount() == 0) {
			return null;
		}
		String[] errors = new String[getErrorCount()];
		for (int i = 0; i < getErrorCount(); i++) {
			errors[i] = formatErrorMessage(i);
		}
		return errors;
	}

	/**
	 * Formats an error message for the specified index.
	 *
	 * @param index The index in the error records list
	 * @return The formatted error message
	 */
	private String formatErrorMessage(int index) {
		StringBuilder message = new StringBuilder();
		IContainedObject container = getContainer(index);
		if (container == null) {
			message.append("Error at: ").append(getLineNumber(index))
					.append(" Description: ")
					.append(getErrorDescription(index));
		} else {
			message.append("Segment position: ").append(getLineNumber(index))
					.append(" Description: ").append(getErrorDescription(index))
					.append(" Container ID: ").append(container.getID());
		}
		return message.toString();
	}

	/**
	 * Logs all error messages to the log file.
	 */
	public void logErrors() {
		for (int i = 0; i < getErrorCount(); i++) {
			logger.error(formatLogMessage(i));
		}
	}

	/**
	 * Formats an error message for logging.
	 *
	 * @param index The index in the error records list
	 * @return The formatted log message
	 */
	private String formatLogMessage(int index) {
		StringBuilder message = new StringBuilder(getErrorDescription(index))
				.append(" ");
		IContainedObject container = getContainer(index);
		if (container instanceof Segment segment) {
			message.append("Segment ID:").append(segment.getID())
					.append(" name:").append(segment.getName());
		} else if (container != null) {
			message.append(container);
		}
		return message.toString();
	}

	/**
	 * Writes all error messages to the provided writer.
	 *
	 * @param writer The writer to output the errors
	 * @throws IOException If an I/O error occurs during writing
	 */
	public void writeErrors(Writer writer) throws IOException {
		for (int i = 0; i < getErrorCount(); i++) {
			writer.write(formatLogMessage(i));
			writer.write("\n");
		}
	}
}