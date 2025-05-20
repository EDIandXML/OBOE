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

package io.github.EDIandXML.OBOE.x12;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.DataElements.IDList;
import io.github.EDIandXML.OBOE.DataElements.IDListProcessor;
import io.github.EDIandXML.OBOE.Parsers.IDListParser;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * Id List Processor to be used with the X12 data element id = 103. aka
 * Packing Code
 *
 * OBOE - Open Business Objects for EDI
 * <p>
 * 
 * An EDI and XML Translator Written In Java <br>
 *
 *
 * 
 */
public class IDList103Processor implements IDListProcessor {

	IDListParser idListParser = null;
	static Hashtable<String, String> code1 = null;
	static Hashtable<String, String> code2 = null;
	static Logger logr = LogManager
			.getLogger(IDList103Processor.class.getName());

	/**
	 *
	 */
	public IDList103Processor() throws IOException {
		if (idListParser != null) {
			return; // done this already;
		}
		ArrayList<String> v, c;
		try {
			idListParser = new IDListParser();
		} catch (Exception e) {
			logr.error(e.getMessage(), e);
			return;
		}
		String msgDescriptionFolder = Util.getMessageDescriptionFolder();

		IDList currentIDList = new IDList(
				Util.getMessageDescriptionFolder() + "IDList103Part1.xml",
				msgDescriptionFolder, idListParser);
		v = currentIDList.getValues();
		c = currentIDList.getCodes();
		code1 = new Hashtable<String, String>(v.size());
		int i;
		for (i = 0; i < v.size(); i++) {
			code1.put(c.get(i), v.get(i));
		}
		currentIDList = new IDList(
				Util.getMessageDescriptionFolder() + "IDList103Part2.xml",
				msgDescriptionFolder, idListParser);

		v = currentIDList.getValues();
		c = currentIDList.getCodes();
		code2 = new Hashtable<String, String>(v.size());
		for (i = 0; i < v.size(); i++) {
			code2.put(c.get(i), v.get(i));
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * java.lang.String)
	 */
	@Override
	public void add(String inCode, String inDescribe)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException("don't do this now");

	}

	@Override
	public boolean isCodeValid(String inCode) {
		String c1;

		if (inCode.length() == 3) {
			c1 = inCode;
		} else if (inCode.length() != 5) {
			return false;
		} else {
			c1 = inCode.substring(0, 3);
		}

		if (code1.get(c1) == null) {
			return false;
		}
		if (inCode.length() == 3) {
			return true;
		}

		String c2 = inCode.substring(3, 5);
		if (code2.get(c2) == null) {
			return false;
		}
		return true;
	}

	@Override
	public String describe(String inCode) {
		//
		String c1;

		if (inCode.length() == 3) {
			c1 = inCode;
		} else if (inCode.length() != 5) {
			return inCode;
		} else {
			c1 = inCode.substring(0, 3);
		}

		if (code1.get(c1) == null) {
			return inCode;
		}

		if (inCode.length() == 3) {
			return code1.get(c1);
		}

		String c2 = inCode.substring(3, 5);

		if (code2.get(c2) == null) {
			return inCode;
		}

		return code1.get(c1) + " " + code2.get(c2);

	}

	/*
	 * (non-Javadoc)
	 *
	 */
	@Override
	public String getCode(String inValue) {
		//
		return inValue;
	}

	/*
	 * (non-Javadoc)
	 *
	 */
	@Override
	public String getCodeByPos(int pos) throws UnsupportedOperationException {
		//
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 */
	@Override
	public ArrayList<String> getCodes() {
		//
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 */
	@Override
	public ArrayList<String> getValues() {
		//
		return null;
	}

}
