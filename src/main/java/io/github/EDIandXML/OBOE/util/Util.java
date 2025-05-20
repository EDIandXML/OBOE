/*
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
package io.github.EDIandXML.OBOE.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.Errors.OBOEException;

/**
 * Utility class for OBOE (Open Business Objects for EDI) providing
 * methods for property management, file handling, string manipulation,
 * and EDI processing.
 *
 * @author Joe McVerry
 */
public final class Util {
	private static final Logger LOGGER = LogManager.getLogger(Util.class);

	// Property keys
	public static final String MESSAGE_DESCRIPTION_FOLDER = "xmlPath";
	public static final String ERROR_LEVEL_TO_REPORT = "errorLevelToReport";
	public static final String SEARCH_DIRECTIVE = "searchDirective";
	public static final String THROW_PARSING_EXCEPTION = "THROW_PARSING_EXCEPTION";
	public static final String DO_PREVALIDATE = "doPrevalidate";
	public static final String REAL_NUMBERS_RETAIN_PRECISION = "realNumbersRetainPrecision";
	public static final String CHECK_PROPERTY_FILE_FOR_CHANGES = "checkPropertyFileForChanges";
	public static final String VALID_CHARACTER_SET = "validCharacterSet";
	public static final String SEARCH_CLASSPATH = "searchClassPathForMessageFiles";

	private static final String PROPERTIES_FILE_NAME = "OBOE.properties";
	private static final String LINE_SEPARATOR = System.lineSeparator();

	private static final Map<String, String> PROPERTIES = new ConcurrentHashMap<>();
	private static volatile Path propertyFile;
	private static volatile long lastUpdate = -1;
	private static volatile boolean recheckProperties;
	private static volatile String validCharacters;

	// if you are using the rules and code builder
	// store your transaction message files here that you don't want changed
	// the application will look in this directory to get the original file
	// contents

	public static String lineFeed = System.getProperty("line.separator");

	static {
		try {
			String test = getOBOEProperty(CHECK_PROPERTY_FILE_FOR_CHANGES);
			recheckProperties = test != null && ("true".equalsIgnoreCase(test)
					|| "yes".equalsIgnoreCase(test));
			LOGGER.debug("recheckProperties is {}", recheckProperties);
		} catch (IOException e) {
			LOGGER.error("Failed to initialize recheckProperties");
		}
	}

	private Util() {
		// Prevent instantiation
	}

	/**
	 * Checks if files should be searched in the classpath.
	 *
	 * @return boolean
	 */
	public static boolean findMessageDefinitionFilesInClassPath() {
		try {
			String useClassPath = getOBOEProperty(SEARCH_CLASSPATH);
			if (useClassPath == null) {
				return false;
			}
			return useClassPath.toLowerCase().equals("true");
		} catch (IOException e) {
			LOGGER.debug("Failed to determine message description location");
			return false;
		}
	}

	/**
	 * Returns the current date in CCYYMMDD format.
	 *
	 * @return formatted date string
	 */
	public static String currentDate() {
		Calendar calendar = Calendar.getInstance();
		DecimalFormat df = new DecimalFormat("00000000");
		return df.format((calendar.get(Calendar.YEAR) * 10000L)
				+ ((calendar.get(Calendar.MONTH) + 1) * 100)
				+ calendar.get(Calendar.DAY_OF_MONTH));
	}

	/**
	 * Returns the current time in HHMM (24-hour) format.
	 *
	 * @return formatted time string
	 */
	public static String currentTime() {
		Calendar calendar = Calendar.getInstance();
		DecimalFormat df = new DecimalFormat("0000");
		return df.format((calendar.get(Calendar.HOUR_OF_DAY) * 100)
				+ calendar.get(Calendar.MINUTE));
	}

	/**
	 * Normalizes a string by escaping XML characters and handling control
	 * codes.
	 *
	 * @param input the input string to normalize
	 * @return normalized string
	 */
	public static String normalize(String input) {
		if (input == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder(input.length());
		for (char c : input.toCharArray()) {
			switch (c) {
			case '&':
				sb.append("&amp;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '"':
				sb.append("&quot;");
				break;
			case '\'':
				sb.append("&apos;");
				break;
			default:
				if (c < ' ') {
					sb.append((char) (c + '\uee00'));
				} else {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Converts a string to a byte-based representation.
	 *
	 * @param input the input string
	 * @return byte-based string representation
	 */
	public static String normalizeNonUnicode(String input) {
		if (input == null) {
			return "";
		}
		byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
		StringBuilder sb = new StringBuilder(bytes.length * 4); // Estimate
																// capacity
		for (int i = 0; i < bytes.length; i++) {
			sb.append(bytes[i]).append('#');
		}
		return sb.toString();
	}

	/**
	 * Converts a byte-based string back to its original form.
	 *
	 * @param input the byte-based string
	 * @return original string
	 */
	public static String denormalizeNonUnicode(String input) {
		if (input == null || input.isEmpty()) {
			return "";
		}
		String[] tokens = input.split("#");
		byte[] bytes = new byte[tokens.length];
		for (int i = 0; i < tokens.length; i++) {
			bytes[i] = Byte.parseByte(tokens[i]);
		}
		return new String(bytes, StandardCharsets.UTF_8);
	}

	/**
	 * Removes escape characters from a string.
	 *
	 * @param input     the input string
	 * @param escString the escape characters to remove
	 * @return unescaped string
	 */
	public static String unEscape(String input, String escString) {
		if (input == null) {
			return "";
		}
		if (escString == null || escString.isEmpty()) {
			return input;
		}
		StringBuilder sb = new StringBuilder(input.length());
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (escString.indexOf(c) != -1 && i + 1 < input.length()) {
				i++;
				sb.append(input.charAt(i));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Searches for a file in the specified path or classpath.
	 *
	 * @param filename  the file to search for
	 * @param stopAtDir the directory to stop searching at
	 * @return the found file path
	 * @throws OBOEException if the file is not found
	 */
	public static String searchForFile(String inFilename, String inStopAtDir) {
		String theName = inFilename;
		String endDir = " ";
		// we go through the loop at least once, so set this at the end of loop
		String name;
		String stopDir = (inStopAtDir == null ? "" : inStopAtDir);
		// if no stopdir sent set to zero-length string.
		File f;
		int pos, nextpos;
		if (Util.findMessageDefinitionFilesInClassPath()) {
			ArrayList<String> paths = new ArrayList<String>();
			ArrayList<String> seps = new ArrayList<String>();
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < theName.length(); i++) {
				if ((theName.charAt(i) == '/') || (theName.charAt(i) == '\\')) {
					paths.add(sb.toString());
					sb = new StringBuilder();
					seps.add("" + theName.charAt(i));
					continue;
				}
				sb.append(theName.charAt(i));
			}
			String filename = sb.toString();

			for (int i = paths.size() - 1; i >= 0; i--) {
				sb = new StringBuilder();
				for (int j = 0; j <= i; j++) {
					sb.append(paths.get(j));
					sb.append("/");
				}

				String newfilename = sb.toString() + filename;
				InputStream is = Util.class.getClassLoader()
						.getResourceAsStream(newfilename);
				if (is != null) {
					return newfilename;
				}
				if (sb.toString().compareTo(inStopAtDir) == 0) {
					break;
				}

			}

			throw new OBOEException(
					"File " + inFilename + " not found in classpath.");
		}

		while (true) {

			f = new File(theName);
			name = f.getName();
			if (f.exists()) {
				if (f.isDirectory() == false) {
					return theName;
				}
			}
			if (stopDir.compareTo(endDir) == 0) {
				break;
			}
			pos = f.getAbsolutePath().lastIndexOf(File.separator);
			if (pos < 0) {
				break;
			}
			nextpos = f.getAbsolutePath().substring(0, pos)
					.lastIndexOf(File.separator);
			if (nextpos < 0) {
				break;
			}
			endDir = f.getAbsolutePath().substring(0, nextpos) + File.separator;
			theName = endDir + name;
		}
		throw new OBOEException("File " + inFilename + " not found.");
	}

	/**
	 * Checks if parsers should throw exceptions based on the
	 * OBOE.properties file.
	 *
	 * @return true if exceptions should be thrown, false otherwise
	 */
	public static boolean propertyFileIndicatesTHROW_PARSING_EXCEPTION() {
		try {
			String value = getOBOEProperty(THROW_PARSING_EXCEPTION);
			return value == null || !value.equalsIgnoreCase("false");
		} catch (IOException e) {
			LOGGER.debug("Failed to read THROW_PARSING_EXCEPTION property");
			return true;
		}
	}

	/**
	 * Checks if prevalidation should be performed based on the
	 * OBOE.properties file.
	 *
	 * @return true if prevalidation is enabled, false otherwise
	 */
	public static boolean propertyFileIndicatesDoPrevalidate() {
		try {
			String value = getOBOEProperty(DO_PREVALIDATE);
			return value != null && value.equalsIgnoreCase("true");
		} catch (IOException e) {
			LOGGER.debug("Failed to read doPrevalidate property");
			return false;
		}
	}

	/**
	 * Checks if real numbers should retain precision based on the
	 * OBOE.properties file.
	 *
	 * @return true if precision should be retained, false otherwise
	 */
	public static boolean propertyFileIndicatesRealNumbersRetainPrecision() {
		try {
			String value = getOBOEProperty(REAL_NUMBERS_RETAIN_PRECISION);
			return value != null && value.equalsIgnoreCase("true");
		} catch (IOException e) {
			LOGGER.debug("Failed to read realNumbersRetainPrecision property");
			return false;
		}
	}

	/**
	 * Retrieves the message description folder from OBOE.properties.
	 *
	 * @return the folder path
	 * @throws IOException if an I/O error occurs
	 */
	public static String getMessageDescriptionFolder() throws IOException {
		String value = getOBOEProperty(MESSAGE_DESCRIPTION_FOLDER);
		if (value == null) {
			throw new IOException("propertery: " + MESSAGE_DESCRIPTION_FOLDER
					+ " not defined in OBOE.properties");
		}
		return value;
	}

	/**
	 * Removes trailing spaces from a string.
	 *
	 * @param input the input string
	 * @return trimmed string
	 */
	public static String rightTrim(String input) {
		if (input == null || input.isEmpty()) {
			return "";
		}
		int end = input.length();
		while (end > 0 && input.charAt(end - 1) == ' ') {
			end--;
		}
		return input.substring(0, end);
	}

	/**
	 * Sets a property in OBOE.properties.
	 *
	 * @param key   the property key
	 * @param value the property value
	 */
	public static void setOBOEProperty(String key, String value) {
		Objects.requireNonNull(key, "Property key must not be null");
		Objects.requireNonNull(value, "Property value must not be null");
		if (PROPERTIES.isEmpty()) {
			try {
				loadProperties();
			} catch (IOException e) {
				throw new OBOEException(
						"Something wrong with oboe.properties file", e);

			}
		}
		PROPERTIES.put(key, value);
		LOGGER.debug("Set property {} = {}", key, value);
	}

	/**
	 * Retrieves a property from OBOE.properties.
	 *
	 * @param key the property key
	 * @return the property value, or null if not found
	 * @throws IOException if an I/O error occurs
	 */
	public static String getOBOEProperty(String key) throws IOException {
		Objects.requireNonNull(key, "Property key must not be null");
		if (PROPERTIES.isEmpty()) {
			loadProperties();
		}
		if (propertyFile != null && recheckProperties && Files
				.getLastModifiedTime(propertyFile).toMillis() > lastUpdate) {
			loadProperties();
		}
		String value = PROPERTIES.get(key);
		return value != null ? value.trim() : null;
	}

	/**
	 * Closes and resets the OBOE.properties file.
	 */
	public static void closeOBOEProperty() {
		PROPERTIES.clear();
		propertyFile = null;
		lastUpdate = -1;
		LOGGER.debug("Closed and reset OBOE properties");
	}

	/**
	 * Loads the OBOE.properties file from various locations.
	 *
	 * @throws IOException if the file cannot be loaded
	 */
	private static synchronized void loadProperties() throws IOException {
		if (!PROPERTIES.isEmpty()
				&& !(propertyFile != null && recheckProperties)) {
			return;
		}
		try (InputStream is = getPropertiesFile()) {
			Properties props = new Properties();
			props.load(is);
			PROPERTIES.clear();
			props.forEach((k, v) -> PROPERTIES.put(k.toString(), v.toString()));
			if (propertyFile != null) {
				lastUpdate = Files.getLastModifiedTime(propertyFile).toMillis();
				LOGGER.debug("Loaded properties from {}", propertyFile);
			} else {
				LOGGER.debug("Loaded properties from classpath");
			}
		}
	}

	/**
	 * Locates and returns an InputStream for OBOE.properties.
	 *
	 * @return the InputStream
	 * @throws OBOEException if the file is not found
	 */
	private static InputStream getPropertiesFile() throws OBOEException {
		String propFileName = System.getProperty(PROPERTIES_FILE_NAME,
				PROPERTIES_FILE_NAME);
		Path path = Paths.get(propFileName);
		if (Files.exists(path)) {
			propertyFile = path;
			LOGGER.info("Properties file loaded from {}",
					path.toAbsolutePath());
			try {
				return Files.newInputStream(path);
			} catch (IOException e) {
				throw new OBOEException(
						"Failed to open properties file: " + path);
			}
		}

		path = Paths.get(System.getProperty("user.home"), PROPERTIES_FILE_NAME);
		if (Files.exists(path)) {
			propertyFile = path;
			LOGGER.info("Properties file loaded from {}",
					path.toAbsolutePath());
			try {
				return Files.newInputStream(path);
			} catch (IOException e) {
				throw new OBOEException(
						"Failed to open properties file: " + path);
			}
		}

		path = Paths.get(System.getProperty("java.home"), PROPERTIES_FILE_NAME);
		if (Files.exists(path)) {
			propertyFile = path;
			LOGGER.info("Properties file loaded from {}",
					path.toAbsolutePath());
			try {
				return Files.newInputStream(path);
			} catch (IOException e) {
				throw new OBOEException(
						"Failed to open properties file: " + path);
			}
		}

		InputStream is = Util.class
				.getResourceAsStream("/" + PROPERTIES_FILE_NAME);
		if (is != null) {
			LOGGER.info("Properties file loaded from classpath");
			return is;
		}

		throw new OBOEException(
				"OBOE.properties file not found in any location");
	}

	/**
	 * Returns the X12 composite element separator character.
	 *
	 * @return the separator character
	 */
	public static String getCES() {
		return Envelope.X12_GROUP_DELIMITER;
	}

	/**
	 * Removes CRLF from an InputStream, preserving it if it’s a segment
	 * delimiter.
	 *
	 * @param input the input stream
	 * @return a new InputStream without CRLF
	 * @throws IOException if an I/O error occurs
	 */
	public static InputStream removeCRLFFromStream(InputStream input)
			throws IOException {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(input, StandardCharsets.UTF_8));
				ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			String line = reader.readLine();
			if (line == null) {
				LOGGER.error("Failed to read input stream during CRLF removal");
				return new ByteArrayInputStream(new byte[0]);
			}
			boolean preserveCRLF = line.length() == 105
					&& line.charAt(3) == line.charAt(103);
			if (preserveCRLF) {
				LOGGER.info(
						"Preserving CRLF as it appears to be a segment delimiter");
			}
			do {
				baos.write(line.getBytes(StandardCharsets.UTF_8));
				if (preserveCRLF) {
					baos.write(LINE_SEPARATOR.getBytes(StandardCharsets.UTF_8));
				}
				line = reader.readLine();
			} while (line != null);
			return new ByteArrayInputStream(baos.toByteArray());
		}
	}

	/**
	 * Removes CRLF from a Reader, preserving it if it’s a segment
	 * delimiter.
	 *
	 * @param input the input reader
	 * @return a new Reader without CRLF
	 * @throws IOException if an I/O error occurs
	 */
	public static Reader removeCRLFFromReader(Reader input) throws IOException {
		Path tempFile = Files.createTempFile("oboe", ".tmp");
		try (BufferedReader reader = new BufferedReader(input);
				BufferedWriter writer = Files.newBufferedWriter(tempFile,
						StandardCharsets.UTF_8)) {
			String line = reader.readLine();
			if (line == null) {
				LOGGER.error("Failed to read input during CRLF removal");
				return new StringReader("");
			}
			boolean preserveCRLF = line.length() == 105
					&& line.charAt(3) == line.charAt(103);
			if (preserveCRLF) {
				LOGGER.info(
						"Preserving CRLF as it appears to be a segment delimiter");
			}
			do {
				writer.write(line);
				if (preserveCRLF) {
					writer.write(LINE_SEPARATOR);
				}
				line = reader.readLine();
			} while (line != null);
		}
		return Files.newBufferedReader(tempFile, StandardCharsets.UTF_8);
	}

	/**
	 * Creates a deep copy of an Externalizable object.
	 *
	 * @param fromObj the source object
	 * @param toObj   the target object
	 * @throws OBOEException if the copy fails
	 */
	public static void deepCopy(Externalizable fromObj, Externalizable toObj) {
		if (fromObj == toObj) {
			throw new OBOEException("Source and target objects are the same");
		}
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				ByteArrayInputStream bis = new ByteArrayInputStream(
						bos.toByteArray());
				ObjectInputStream ois = new ObjectInputStream(bis)) {
			fromObj.writeExternal(oos);
			oos.flush();
			toObj.readExternal(ois);
		} catch (IOException | ClassNotFoundException e) {
			throw new OBOEException("Failed to perform deep copy");
		}
	}

	/**
	 * Checks if a string contains only Roman digits (0-9).
	 *
	 * @param input the input string
	 * @return true if the string is a valid integer, false otherwise
	 */
	public static boolean isInteger(String input) {
		if (input == null || input.isEmpty()) {
			return false;
		}
		for (char c : input.toCharArray()) {
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}

	/**
	 * Resets the valid character set for JUnit tests.
	 */
	public static void resetValids() {
		validCharacters = null;
		PROPERTIES.remove(VALID_CHARACTER_SET);
		LOGGER.debug("Reset valid character set");
	}

	/**
	 * Validates a string against the character set defined in
	 * OBOE.properties.
	 *
	 * @param input the input string
	 * @return the position of the first invalid character, or -1 if all are
	 *         valid
	 */
	public static int isValidForCharacterSet(String input) {
		if (input == null || input.isEmpty()) {
			return -1;
		}
		try {
			if (validCharacters == null) {
				String charSet = getOBOEProperty(VALID_CHARACTER_SET);
				if (charSet == null) {
					return -1;
				}
				validCharacters = expandCharacterSet(charSet);
				LOGGER.debug("Expanded valid character set: {}",
						validCharacters);
			}
			for (int i = 0; i < input.length(); i++) {
				if (validCharacters.indexOf(input.charAt(i)) < 0) {
					return i;
				}
			}
		} catch (IOException e) {
			LOGGER.error("Failed to validate character set");
		}
		return -1;
	}

	/**
	 * Expands a character set definition (e.g., "a...z") into a full
	 * string.
	 *
	 * @param charSet the character set definition
	 * @return the expanded character set
	 */
	private static String expandCharacterSet(String charSet) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < charSet.length(); i++) {
			if (i + 4 < charSet.length()) {
				String range = charSet.substring(i, i + 5);
				if (range.equals("a...z")) {
					for (char c = 'a'; c <= 'z'; c++) {
						sb.append(c);
					}
					i += 4;
					continue;
				} else if (range.equals("A...Z")) {
					for (char c = 'A'; c <= 'Z'; c++) {
						sb.append(c);
					}
					i += 4;
					continue;
				} else if (range.equals("0...9")) {
					for (char c = '0'; c <= '9'; c++) {
						sb.append(c);
					}
					i += 4;
					continue;
				}
			}
			sb.append(charSet.charAt(i));
		}
		return sb.toString();
	}

}