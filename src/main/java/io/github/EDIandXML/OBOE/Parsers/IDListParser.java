
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

package io.github.EDIandXML.OBOE.Parsers;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * SAX2 parser handler for IDList xml files
 * 
 */

public class IDListParser extends DefaultHandler implements LexicalHandler {
	/**
	 * current element count
	 */
	protected int _iElement = 0;
	/**
	 * current line number
	 */
	protected int _iLine = 0;
	/**
	 * ArrayLists of idlist codes and values
	 */
	protected ArrayList<String> codes, values;

	// protected org.xml.sax.Parser parser;
	/**
	 * SAX parser
	 */
	protected SAXParser parser;
	// Buffer for collecting data from
	// the "characters" SAX event.
	private CharArrayWriter contents = new CharArrayWriter();

	/**
	 * directory path for xml file
	 */
	protected String xmlDirectoryPath = "." + File.pathSeparator + ".";

	static Logger logr = LogManager.getLogger(IDListParser.class);

	/**
	 * constructor, sets up SAX parser, turns off validation, turns on
	 * namespaces, sets up content handler and error handler as this object.
	 * sax exceptions go to System.err
	 */
	public IDListParser() throws OBOEException {

		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();

			parser = spf.newSAXParser();
			parser.getXMLReader().setProperty(
					"http://xml.org/sax/properties/lexical-handler", this);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			xmlDirectoryPath = Util.getMessageDescriptionFolder();

		} catch (Exception ex) {
			logr.error("message" + ex.getMessage());
			ex.printStackTrace();
			throw new OBOEException(ex.getMessage());
		}
	}

	private String xmlFile = null;

	/**
	 * makes the SAX2 parser call
	 * 
	 * @param inXMLFile             String of filename to parse
	 * @param inLastDirectoryToLook - name of the top directory where the
	 *                              IDList file could reside
	 * @param vCodes                ArrayList of codes
	 * @param vValues               ArrayList of code descriptive values
	 */
	public void parse(String inXMLFile, String inLastDirectoryToLook,
			ArrayList<String> vCodes, ArrayList<String> vValues) {
		xmlFile = Util.searchForFile(inXMLFile, inLastDirectoryToLook);
		try {
			codes = vCodes;
			values = vValues;

			InputSource is;
			if (Util.findMessageDefinitionFilesInClassPath()) {
				logr.debug("idlistparser using classpath xml file " + xmlFile);
				is = new InputSource(IDListParser.class.getClassLoader()
						.getResourceAsStream(xmlFile));
			} else {
				logr.debug("idlistparser using xml file " + xmlFile);
				is = new InputSource(new FileReader(xmlFile));
			}
			is.setSystemId("");
			parser.parse(is, this);
		} /* end try */
		catch (Exception e1) {
			logr.error("message" + e1.getMessage());
			e1.printStackTrace();

		} /* end catch */

	}

	/**
	 * method called for each xml element found. <br>
	 * process logic
	 * <ul>
	 * <li>based on the name of the element found
	 * <li>for each pull appropriate attributes and construct object
	 * <li>if owned by another class, and all are except for Standard, add
	 * it to its parents object
	 * </ul>
	 * 
	 * @param uri        URI of incoming file
	 * @param localName  String of element's local name
	 * @param rawName    String of element's raw name
	 * @param attributes ArrayList of the elements attributes
	 * @throws SAXException many possible exceptions
	 */
	@Override
	public void startElement(java.lang.String uri, java.lang.String localName,
			java.lang.String rawName, Attributes attributes)
			throws SAXException {

		_iElement++;

		contents.reset();

		// Standard

		if (rawName.compareTo("") == 0) {
			return;
		}

		// MessageDirectory
		if (rawName.equals("idList")) {
			return;
		}

	}

	/**
	 * catches the element's value
	 * 
	 * @param ch     char array of the current element value contents
	 * @param start  int start position within the array
	 * @param length int of characters found so far
	 * @throws SAXException many possible
	 */
	@Override
	public void characters(char ch[], int start, int length)
			throws SAXException {
		contents.write(ch, start, length);
		for (int i = start; i < (start + length); i++) {
			if (ch[i] == '\n') {
				_iLine++;
			}
		}

	}

	/**
	 * Method called by the SAX parser at the </
	 * 
	 * @param uri       URI of incoming file
	 * @param localName String of element's local name
	 * @param rawName   String of element's raw name
	 * @throws SAXException many possible
	 */
	@Override
	public void endElement(java.lang.String uri, java.lang.String localName,
			java.lang.String rawName) throws SAXException {

		if (rawName.equals("idCode")) {
			codes.add(contents.toString());
			values.add(contents.toString());
		}

		if (rawName.equals("idValue")) {
			values.set(codes.size() - 1, contents.toString());
		}

	}

	/**
	 * I use this to keep track of line #s
	 * 
	 * @param ch     char array of found whitespaces
	 * @param start  int start position in array
	 * @param length int length of what's been found
	 */
	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) {
		for (int i = start; i < (start + length); i++) {
			if (ch[i] == '\n') {
				_iLine++;
			}
		}
	}

	/**
	 * catches warning SAXParseExceptions this code sends exception to stdio
	 * and allows public classto continue
	 * 
	 * @param e SaxException object
	 * @throws SAXException exception
	 */
	@Override
	public void warning(SAXParseException e) throws SAXException {
		logr.error("Warning at (file " + xmlFile + ", line " + e.getLineNumber()
				+ ", char " + e.getColumnNumber() + "): " + e.getMessage());
	}

	/**
	 * catches error SAXParseExceptions this code causes exception to
	 * continue
	 * 
	 * @param e SaxException object
	 * @throws SAXException thrown
	 */
	@Override
	public void error(SAXParseException e) throws SAXException {
		throw new SAXException("Error at (file " + xmlFile + ", line "
				+ e.getLineNumber() + ", char " + e.getColumnNumber() + "): "
				+ e.getMessage());
	}

	/**
	 * catches fatal SAXParseExceptions this code causes exception to
	 * continue
	 * 
	 * @param e SAXException object
	 * @throws SAXException thrown
	 */
	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		throw new SAXException("Fatal Error at (file " + xmlFile + ", line "
				+ e.getLineNumber() + ", char " + e.getColumnNumber() + "): "
				+ e.getMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void startDTD(String arg0, String arg1, String arg2)
			throws SAXException {
		//

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ext.LexicalHandler#endDTD()
	 */
	@Override
	public void endDTD() throws SAXException {
		//

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
	 */
	@Override
	public void startEntity(String arg0) throws SAXException {
		//

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
	 */
	@Override
	public void endEntity(String arg0) throws SAXException {
		//

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ext.LexicalHandler#startCDATA()
	 */
	@Override
	public void startCDATA() throws SAXException {
		//

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ext.LexicalHandler#endCDATA()
	 */
	@Override
	public void endCDATA() throws SAXException {
		//

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
	 */
	@Override
	public void comment(char[] chars, int start, int length)
			throws SAXException {
		for (int i = start; i < (start + length); i++) {
			if (chars[i] == '\n') {
				_iLine++;
			}
		}

	}

	//
	/**
	 * get SAX parser
	 * 
	 * @return SAXParser
	 */
	public SAXParser getParser() {
		return parser;
	}

	/**
	 * returns number of code/value pairs
	 * 
	 * @return int
	 */
	public int getCount() {
		return codes.size();
	}

	/**
	 * gets code as specific location
	 * 
	 * @param i int position to look at
	 * @return String
	 */
	public String getCode(int i) {
		return codes.get(i);
	}

	/**
	 * returns descriptive value at position
	 * 
	 * @param i int position to look at
	 * @return String
	 */
	public String getValue(int i) {
		return values.get(i);
	}

}
