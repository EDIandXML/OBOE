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

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Parsers.IDListParser;
import io.github.EDIandXML.OBOE.util.Util;

/**
 * An class for processing IDLists
 */

public class IDList implements IDListProcessor {

	/**
	 * IDList name
	 */
	protected String name = null;
	/**
	 * codes and descriptive values associated with IDList
	 */
	protected ArrayList<String> codes, values;

	/**
	 * IDList short filename
	 */
	protected String shortname = "";

	/**
	 * IDList boolean filtered - filtered data from filename
	 */
	protected boolean filtered = false;
	protected String filterList = "";

	/** log4j object */
	static Logger logr = LogManager.getLogger(IDList.class);

	/**
	 * Build an IDList structure from an XML file
	 * 
	 * @param xmlFile               xml file containing id list structure
	 * @param inLastDirectoryToLook - name of the highest directory to find
	 *                              idlist file in
	 * @param idListParser          SAX2 parser IDListParser
	 */
	public IDList(String xmlFile, String inLastDirectoryToLook,
			IDListParser idListParser) {
		this();
		name = xmlFile;

		File f = new File(name);
		shortname = f.getName();

		idListParser.parse(xmlFile, inLastDirectoryToLook, codes, values);
	}

	/**
	 * Construct an id list object with no values
	 */
	public IDList() {
		codes = new ArrayList<String>();
		values = new ArrayList<String>();
	}

	/**
	 * Construct an id list object with no values but a name
	 * 
	 * @param inShortName String name of idList
	 */
	public IDList(String inShortName) {
		this();
		shortname = inShortName;
	}

	/**
	 * sets the name of id list file
	 *
	 */
	public void setName(String inName) {
		name = inName;
		if (name != null) {
			File f = new File(name);
			shortname = f.getName();
		} else {
			shortname = null;
		}
	}

	/**
	 * return name of id list file
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * return short name of id list
	 *
	 * @return String
	 */
	public String getShortName() {
		return shortname;
	}

	/**
	 * Add code and description to the ArrayLists
	 * 
	 * @param inCode     String code
	 * @param inDescribe String descriptive value
	 */
	@Override
	public void add(String inCode, String inDescribe) {
		codes.add(inCode);
		values.add(inDescribe);
	}

	/**
	 * tests if the passed code is in the code ArrayList
	 * 
	 * @param inCode String to test
	 * @return boolean
	 */
	@Override
	public boolean isCodeValid(String inCode) {
		String test;
		for (int i = 0; i < codes.size(); i++) {
			test = codes.get(i);
			if (test == null) {
				continue;
			}
			if (test.compareTo(inCode) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * returns the descriptive value of the code, if it is not found then
	 * the code is returned
	 * 
	 * @param inCode String to test
	 * @return String
	 */
	@Override
	public String describe(String inCode) {
		String test;
		for (int i = 0; i < codes.size(); i++) {
			test = codes.get(i);
			if (test.compareTo(inCode) == 0) {
				if (values.get(i) == null) {
					return inCode;
				} else {
					return values.get(i);
				}
			}
		}
		return inCode;
	}

	/**
	 * returns the code value for a descriptive value. if it is not found
	 * the value is returned
	 * 
	 * @param inValue String to test
	 * @return String
	 */

	@Override
	public String getCode(String inValue) {
		String test;
		for (int i = 0; i < values.size(); i++) {
			test = values.get(i);
			if (test.compareTo(inValue) == 0) {
				return codes.get(i);
			}
		}
		return inValue;
	}

	/**
	 * returns a code at a specific position in ArrayList
	 * 
	 * @param pos int String position
	 * @return String
	 */
	@Override
	public String getCodeByPos(int pos) {
		return codes.get(pos);
	}

	/**
	 * returns the code ArrayList
	 *
	 * @return ArrayList
	 */

	@Override
	public ArrayList<String> getCodes() {
		return codes;
	}

	/**
	 * returns the value ArrayList
	 *
	 * @return ArrayList
	 */

	@Override
	public ArrayList<String> getValues() {
		return values;
	}

	public boolean isFiltered() {
		return filtered;
	}

	public void setFiltered(boolean filtered) {
		this.filtered = filtered;
	}

	public String getFilterList() {
		return filterList;
	}

	public void setFilterList(String filterList) {
		this.filterList = filterList;
		setFiltered(true);
	}

	/**
	 * @param c      'i' or 'x', include or exclude
	 * @param string inclusion/exclusion list <br>
	 *               comma separated and dash range specifier
	 * @return new idlist object
	 */
	public IDList idListWork(char c, String string) {

		IDList idl = new IDList();
		idl.shortname = this.shortname;
		idl.setFiltered(true);
		StringTokenizer st = new StringTokenizer(string, ",");
		String tkn, strt, stp;
		String lststrt = "";
		int i;
		int currentpos = 0;
		int currentlength = this.getCodes().size();
		if (string.length() == 0) {
			throw new OBOEException(
					"IDList inclusion/exclusion attribute error; too small or length is zero");
		}
		while (st.hasMoreTokens()) {
			tkn = st.nextToken().trim();

			if (tkn.indexOf('-') < 0) {
				strt = tkn;
				if (strt.length() == 0) {
					throw new OBOEException(
							"IDList inclusion/exclusion attribute error; start value too small or length is zero "
									+ tkn);
				}
				stp = tkn;
			} else {
				strt = "";
				stp = "";
				for (i = 0; i < tkn.indexOf('-'); i++) {
					strt += tkn.charAt(i);
				}
				strt = strt.trim();
				if (strt.length() == 0) {
					throw new OBOEException(
							"IDList inclusion/exclusion attribute error; start value too small or length is zero "
									+ tkn);
				}
				for (i++; i < tkn.length(); i++) {
					if (tkn.charAt(i) == '-') {
						throw new OBOEException(
								"IDList inclusion/exclusion attribute error; too many dashes in "
										+ tkn + " within " + string);
					}
					stp += tkn.charAt(i);
				}
				stp = stp.trim();
				if (stp.length() == 0) {
					throw new OBOEException(
							"IDList inclusion/exclusion attribute error; stop value too small or length is zero "
									+ tkn);
				}
			}

			boolean numCheck = (Util.isInteger(strt) && Util.isInteger(stp));
			if (numCheck) {
				int s = Integer.parseInt(strt);
				int p = Integer.parseInt(stp);
				if (s > p) {
					throw new OBOEException(
							"IDList inclusion/exclusion attribute error; invalid range of values in "
									+ tkn + " within " + string);
				}
			} else if (strt.compareTo(stp) > 0) {
				throw new OBOEException(
						"IDList inclusion/exclusion attribute error; invalid range of values in "
								+ tkn + " within " + string);
			}

			if (Util.isInteger(strt) && Util.isInteger(lststrt)) {
				int s = Integer.parseInt(strt);
				int p = Integer.parseInt(lststrt);
				if (s < p) {
					throw new OBOEException(
							"IDList inclusion/exclusion attribute error; new start value is less than old start value "
									+ tkn + " within " + string);
				}
			} else if (strt.compareTo(lststrt) <= 0) {
				throw new OBOEException(
						"IDList inclusion/exclusion attribute error; new start value is less than old start value "
								+ tkn + " within " + string);
			}

			lststrt = strt;

			logr.debug("working with " + c + " token is " + tkn + " start is "
					+ strt + " stop is " + stp);
			for (; currentpos < currentlength; currentpos++) {

				String cd = this.getCodeByPos(currentpos);
				if (c == 'i') {
					if (numCheck && Util.isInteger(cd) && (Integer
							.parseInt(cd) < Integer.parseInt(strt))) {
						continue;
					}
					if (!numCheck && (cd.compareTo(strt) < 0)) {
						continue; // inner while loop;
					}
					if (numCheck && Util.isInteger(cd)
							&& (Integer.parseInt(cd) > Integer.parseInt(stp))) {
						break;
					}
					if (!numCheck && (cd.compareTo(stp) > 0)) {
						break; // inner while loop;
					}

					idl.add(cd, this.describe(cd));

				} else // c is 'x'
				{
					if (numCheck && Util.isInteger(cd) && (Integer
							.parseInt(cd) < Integer.parseInt(strt))) {
						idl.add(cd, this.describe(cd));
						continue; // inner while loop;
					}
					if (!numCheck && (cd.compareTo(strt) < 0)) // not there yet
					{
						idl.add(cd, this.describe(cd));
						continue; // inner while loop;
					}
					// are we done, if so break inner while loop
					if (numCheck && Util.isInteger(cd)
							&& (Integer.parseInt(cd) > Integer.parseInt(stp))) {
						break;
					}
					if (!numCheck && (cd.compareTo(stp) > 0)) {
						break;
					}

				}

			}

		}

		for (; currentpos < currentlength; currentpos++) {

			String cd = this.getCodeByPos(currentpos);
			if (c == 'x') {
				idl.add(cd, this.describe(cd));
			}

		}

		return idl;
	}

}
