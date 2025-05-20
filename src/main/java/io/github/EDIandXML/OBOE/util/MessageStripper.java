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
package io.github.EDIandXML.OBOE.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.Containers.Envelope;
import io.github.EDIandXML.OBOE.EDIFACT.EDIFACTDocumentHandler;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;

/**
 * @author Joe McVerry
 * 
 *         Twas X12 specific, pulls multiple ISA/ISE groups out of a
 *         reader object Presently works with X12 and EDIFACT so now it
 *         can pull (UNA)UNB/UNZ groups out of a reader object
 */
public class MessageStripper {

	private Reader currentReader;

	static final char lastSegmentTest = '$';

	static Logger logr = LogManager.getLogger(MessageStripper.class);
	Format format;

	/**
	 * constructor is passed a reader object containing the multiple
	 * messages
	 *
	 * @param inReader
	 *
	 */
	public MessageStripper(Reader inReader, Format inFormat) {

		currentReader = inReader;
		if ((inFormat == Format.X12_FORMAT)
				|| (inFormat == Format.EDIFACT_FORMAT)) {
			format = inFormat;
		} else {
			throw new OBOEException("Format passed is unknown to process.");
		}

	}

	/**
	 * parsers out the next message into a writer object
	 *
	 * @param inWriter - message is written to this writer object.
	 * @return int - number of characters read; -1 - no message
	 * @throws OBOEException - several, such as testing if first three
	 *                       characters are ISA or message ends before last
	 *                       ISE and segment delimiter is hit
	 * @throws IOException   , see exceptions java.io.StringWriter,
	 *                       StringReader
	 */
	public int getNextMessage(Writer inWriter)
			throws OBOEException, IOException {

		if (format == Format.X12_FORMAT) {
			return getNextX12Message(inWriter);
		} else {
			return getNextEDIFACTMessage(inWriter);
		}

	}

	DocumentErrors de;

	/**
	 *
	 * @return an Envelope object you will have to cast to the correct type.
	 * @throws OBOEException
	 * @throws IOException
	 */

	public Envelope getNextEnvelope() throws OBOEException, IOException {
		StringWriter sw = new StringWriter();
		if (getNextMessage(sw) < 1) {
			return null;
		}
		String senv = sw.toString();
		if (format == Format.X12_FORMAT) {
			X12DocumentHandler xdh = new X12DocumentHandler();
			xdh.getParser().parseDocument(senv);
			de = xdh.getDocumentErrors();
			return xdh.getEnvelope();
		} else {
			EDIFACTDocumentHandler edh = new EDIFACTDocumentHandler();
			edh.getParser().parseDocument(senv);
			de = edh.getDocumentErrors();
			return edh.getEnvelope();
		}

	}

	/**
	 * returns the document errors from the last message stripped and parsed
	 *
	 * @return de - DocumentErrors object
	 */
	public DocumentErrors getDocumentErrors() {
		return de;
	}

	/**
	 * @param inWriter
	 * @return int number of bytes read
	 */
	private int getNextEDIFACTMessage(Writer inWriter)
			throws OBOEException, IOException {
		char array[] = new char[9];
		int readcnt, count;
		while (true) {

			int ri = currentReader.read();
			if (ri == -1) {
				return -1;
			}
			if (ri == 'U') {
				array[0] = 'U';
				break;
			}

		}
		readcnt = currentReader.read(array, 1, 8);
		if (readcnt == -1) {
			return -1;
		}

		if (readcnt < 3) {
			throw new OBOEException("inReader data content too small");
		}

		if ((array[0] != 'U') || (array[1] != 'N')
				|| ((array[2] != 'A') && (array[2] != 'B'))) {
			throw new OBOEException(
					"unknown incoming data " + new String(array));
		}

		inWriter.write(array);

		char fldDelimiter = array[3];
		char segDelimiter = Envelope.EDIFACT_SEGMENT_DELIMITER.charAt(0);

		if (array[2] == 'A') {
			segDelimiter = array[8];
		}

		int state = 0;

		loop: do {

			// while (currentReader.ready() == false)
			// System.out.print("not ready ");
			count = currentReader.read();
			if (count == -1) {
				throw new OBOEException("inReader data content too small");
			}
			readcnt++;
			inWriter.write(count);

			switch (state) {

			case -1:
				if (count == segDelimiter) {
					state = 0;
				}
				break;

			case 0:
				if (count == 'U') {
					state = 'U';
				}
				break;

			case 'U':
				if (count == 'N') {
					state = 'N';
				} else {
					state = -1;
				}
				break;

			case 'N':
				if (count == 'Z') {
					state = 'Z';
				} else {
					state = -1;
				}
				break;

			case 'Z':
				if (count == fldDelimiter) {
					state = lastSegmentTest;
				} else {
					state = -1;
				}
				break;
			case lastSegmentTest:
				if (count == segDelimiter) {
					break loop;
				}
				break;
			default:
				if (count == segDelimiter) {
					state = 0;
				} else {
					state = -1;
				}
			}

		} while (true);

		return readcnt;
	}

	/**
	 * @param inWriter
	 * @return int number of bytes read
	 */
	private int getNextX12Message(Writer inWriter)
			throws OBOEException, IOException {
		char array[] = new char[107];
		int readcnt, count;

		while (true) {

			int ri = currentReader.read();
			if (ri == -1) {
				return -1;
			}
			if (ri == 'I') {
				array[0] = 'I';
				break;
			}

		}
		readcnt = currentReader.read(array, 1, 106);
		if (readcnt == -1) {
			return -1;
		}

		if (readcnt < 106) {
			throw new OBOEException("inReader data content too small");
		}

		if ((array[0] != 'I') || (array[1] != 'S') || (array[2] != 'A')) {
			throw new OBOEException(
					"unknown incoming data " + new String(array));
		}

		int fieldCnt = 0;
		inWriter.write(array);

		char fldDelimiter = array[3];
		char segDelimiter = array[105];
		for (int i = 0; i < readcnt; i++) {
			if (array[i] == fldDelimiter) {
				fieldCnt++;
			}
			if (fieldCnt == 16) {
				if (i != 103) {
					System.err.println("ISA segmet is too short " + i
							+ " attempting to continue.");
					segDelimiter = array[i + 2];
				}
				break;
			}
		}

		if (fieldCnt != 16) {
			throw new OBOEException(
					"ISA not in correct format, fieldCnt not 16, count is "
							+ fieldCnt);
		}

		int state = 0;

		loop: do {

			// while (currentReader.ready() == false)
			// System.out.print("not ready ");
			count = currentReader.read();
			if ((count == -1) && (readcnt > 0)) {
				throw new OBOEException("inReader data content too small "
						+ readcnt + " in state " + (char) state);
			}
			readcnt++;

			// System.out.print((char) count);
			inWriter.write(count);

			switch (state) {
			case -1:
				if (count == segDelimiter) {
					state = 0;
				}
				break;

			case 0:
				if (count == 'I') {
					state = 'I';
				}
				break;

			case 'I':
				if (count == 'E') {
					state = 'E';
				} else {
					state = -1;
				}
				break;

			case 'E':
				if (count == 'A') {
					state = 'A';
				} else {
					state = -1;
				}
				break;

			case 'A':
				if (count == fldDelimiter) {
					state = lastSegmentTest;
				} else {
					state = -1;
				}
				break;
			case lastSegmentTest:
				if (count == segDelimiter) {
					// System.out.println();
					// System.out.flush();
					break loop;
				}
				break;
			default:
				if (count == segDelimiter) {
					state = 0;
				} else {
					state = -1;
				}
			}

		} while (true);

		return readcnt;

	}

	public static void main(String[] args) throws Exception {

		Util.setOBOEProperty("THROW_PARSING_EXCEPTION", "false");

		InputStreamReader isr = new InputStreamReader(
				new FileInputStream(args[0]));
		MessageStripper ms;

		if (args.length == 0) {
			ms = new MessageStripper(Util.removeCRLFFromReader(isr),
					Format.X12_FORMAT);
		} else {
			// anything in args[1]
			ms = new MessageStripper(Util.removeCRLFFromReader(isr),
					Format.X12_FORMAT);
		}

		do {
			if (args.length > 0) {
				StringWriter sw = new StringWriter();
				int c = ms.getNextMessage(sw);
				if (c == -1) {
					return;
				}
				X12DocumentHandler xdh = new X12DocumentHandler(
						new StringReader(sw.toString()));
				System.out.println(xdh.getEnvelope()
						.getFormattedText(Format.VALID_XML_FORMAT));
			}

			else {
				Envelope env = ms.getNextEnvelope();
				if (env == null) {
					break;
				}
				System.out
						.println(env.getFormattedText(Format.VALID_XML_FORMAT));
			}
			ms.getDocumentErrors().logErrors();
		} while (true);

	}
}
