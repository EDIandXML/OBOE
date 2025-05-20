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

import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.Loop;
import io.github.EDIandXML.OBOE.Containers.Segment;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * Manages errors in EDI documents for the OBOE (Open Business Objects
 * for EDI) framework. Stores and reports errors based on WEDI SNIP
 * Testing Types.
 */
public class DocumentErrors {

	private static final Logger logger = LogManager
			.getLogger(DocumentErrors.class);

	/** Error types based on WEDI SNIP Testing Types */
	public enum ERROR_TYPE {
		Integrity(1), Requirement(2), Balancing(3), Situation(4), CodeSet(5),
		ProductTypeService(6), TradingPartnerSpecific(7),
		CAGC_CARC_Processing(8);

		private final int type;

		ERROR_TYPE(int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}
	}

	/** Record to store error details */
	record ErrorRecord(Integer position, String id, String description,
			IContainedObject container, String code, Object errorObject,
			Boolean reportable, ERROR_TYPE level) {
	}

	/** List of error records */
	private final ArrayList<ErrorRecord> errorRecords = new ArrayList<>();

	/** Minimum level of error checking (defaults to Requirement) */
	private ERROR_TYPE errorLevel = ERROR_TYPE.Requirement;

	/**
	 * Constructs a DocumentErrors instance, initializing the error level
	 * from OBOE properties.
	 */
	public DocumentErrors() {
		try {
			String errorLevelProperty = Util
					.getOBOEProperty(Util.ERROR_LEVEL_TO_REPORT);
			if (errorLevelProperty != null) {
				for (ERROR_TYPE level : ERROR_TYPE.values()) {
					if (errorLevelProperty.equals(level.name())) {
						setErrorLevelToReport(level);
						return;
					}
				}
				logger.error("Unknown error level in properties file: {}",
						errorLevelProperty);
			}
		} catch (IOException | OBOEException e) {
			logger.error("Error initializing error level: {}", e.getMessage(),
					e);
		}
	}

	/**
	 * Sets the minimum level of compliance checking.
	 *
	 * @param level The error level to set
	 */
	public void setErrorLevelToReport(ERROR_TYPE level) {
		this.errorLevel = level;
	}

	/**
	 * Gets the minimum level of compliance checking.
	 *
	 * @return The current error level
	 */
	public ERROR_TYPE getErrorLevelToReport() {
		return errorLevel;
	}

	/**
	 * Adds an error to the error records if the error level is within the
	 * reporting threshold.
	 *
	 * @param position    The position of the segment in error
	 * @param id          The ID of the segment in error
	 * @param description The description of the error
	 * @param container   The container owning the erroneous segment or the
	 *                    last valid container
	 * @param code        The X12 or EDIFACT error code
	 * @param errorObject The object causing the error (e.g., transaction
	 *                    set, segment)
	 * @param level       The error level (e.g., HIPAA compliance level)
	 */
	public void addError(int position, String id, String description,
			IContainedObject container, String code, Object errorObject,
			ERROR_TYPE level) {
		if (level.getType() <= errorLevel.getType()) {
			errorRecords.add(new ErrorRecord(position, id, description,
					container, code, errorObject, false, level));
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
	 * Gets the position of the segment in error at the specified index.
	 *
	 * @param index The index in the error records list
	 * @return The segment position
	 */
	public int getErrorPosition(int index) {
		return errorRecords.get(index).position();
	}

	/**
	 * Gets the ID of the segment in error at the specified index.
	 *
	 * @param index The index in the error records list
	 * @return The segment ID
	 */
	public String getErrorID(int index) {
		return errorRecords.get(index).id();
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
	 * Gets the container of the segment in error at the specified index.
	 *
	 * @param index The index in the error records list
	 * @return The container, or null if not available
	 */
	public IContainedObject getContainer(int index) {
		return errorRecords.get(index).container();
	}

	/**
	 * Gets the error code at the specified index.
	 *
	 * @param index The index in the error records list
	 * @return The error code
	 */
	public String getErrorCode(int index) {
		return errorRecords.get(index).code();
	}

	/**
	 * Gets the object causing the error at the specified index.
	 *
	 * @param index The index in the error records list
	 * @return The error object
	 */
	public Object getErrorObject(int index) {
		return errorRecords.get(index).errorObject();
	}

	/**
	 * Checks if the error at the specified index is reportable (e.g., via
	 * 997/CONTRL).
	 *
	 * @param index The index in the error records list
	 * @return True if reportable, false otherwise
	 */
	public boolean isReportable(int index) {
		return errorRecords.get(index).reportable();
	}

	/**
	 * Gets the error level at the specified index.
	 *
	 * @param index The index in the error records list
	 * @return The error level
	 */
	public ERROR_TYPE getLevel(int index) {
		return errorRecords.get(index).level();
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
			message.append("Error at: ").append(getErrorPosition(index))
					.append(" Code: ").append(getErrorCode(index))
					.append(" ID: ").append(getErrorID(index))
					.append(" Description: ")
					.append(getErrorDescription(index));
		} else {
			message.append("Segment position: ").append(getErrorPosition(index))
					.append(" Code: ").append(getErrorCode(index))
					.append(" ID: ").append(getErrorID(index))
					.append(" Description: ").append(getErrorDescription(index))
					.append(" Container ID: ").append(container.getID());
			appendByteOffset(message, index);
		}
		return message.toString();
	}

	/**
	 * Appends byte offset information to the error message if available.
	 *
	 * @param message The StringBuilder to append to
	 * @param index   The index in the error records list
	 */
	private void appendByteOffset(StringBuilder message, int index) {
		Object errorObject = getErrorObject(index);
		if (errorObject instanceof Loop loop && loop.byteOffset > -1) {
			message.append(" near byte offset of [").append(loop.byteOffset)
					.append("]");
		} else if (errorObject instanceof Segment segment
				&& segment.getByteOffset() > -1) {
			message.append(" near byte offset of [")
					.append(segment.getByteOffset()).append("]");
		}
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
		StringBuilder message = new StringBuilder().append(getErrorID(index))
				.append(" ").append(getErrorCode(index)).append(" ")
				.append(getErrorPosition(index)).append(" ")
				.append(getErrorDescription(index)).append(" ");
		IContainedObject container = getContainer(index);
		if (container instanceof Segment segment) {
			message.append("Segment ID:").append(segment.getID())
					.append(" name:").append(segment.getName());
		} else if (container != null) {
			message.append(container);
			appendByteOffset(message, index);
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