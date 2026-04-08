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

package io.github.ediandxml.oboe.DataElements;

import java.io.File;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.ediandxml.oboe.Errors.OBOEException;
import io.github.ediandxml.oboe.Parsers.IDListParser;

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
	protected TreeMap<String, String> codesValues;

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
	 * @param inLastDirectoryToLook - name of the highest directory to find idlist
	 *                              file in
	 * @param idListParser          SAX2 parser IDListParser
	 */
	public IDList(String xmlFile, String inLastDirectoryToLook, IDListParser idListParser) {
		this();

		File f = new File(xmlFile);
		shortname = f.getName();

		idListParser.parse(xmlFile, inLastDirectoryToLook, codesValues);
	}

	/**
	 * Construct an id list object with no values
	 */
	public IDList() {
		codesValues = new TreeMap<>();
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
	 * Add code and description
	 * 
	 * @param inCode     String code
	 * @param inDescribe String descriptive value
	 */
	@Override
	public void add(String inCode, String inDescribe) {
		codesValues.put(inCode, inDescribe);
	}

	/**
	 * tests if the passed code is in the code ArrayList
	 * 
	 * @param inCode String to test
	 * @return boolean
	 */
	@Override
	public boolean isCodeValid(String inCode) {
		return codesValues.containsKey(inCode);
	}

	/**
	 * returns the descriptive value of the code, if it is not found then the code
	 * is returned
	 * 
	 * @param inCode String to test
	 * @return String
	 */
	@Override
	public String describe(String inCode) {

		if (codesValues.containsKey(inCode))
			return codesValues.get(inCode).length() == 0 ? inCode : codesValues.get(inCode);

		return null;
	}

	/**
	 * returns the code value for a descriptive value. if it is not found the value
	 * is returned
	 * 
	 * @param inValue String to test
	 * @return String
	 */

	@Override
	public String getCode(String inValue) {
		if (!codesValues.containsValue(inValue))
			return null;

		for (var ke : codesValues.entrySet()) {
			if (ke.getValue().equals(inValue))
				return ke.getKey();
		}
		return null;
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

	public enum IncludeOrExclude {
		EXCLUDE, INCLUDE
	};

	/**
	 * @param IncludeOrExclude include or exclude
	 * @param string           inclusion/exclusion list <br>
	 *                         comma separated and dash range specifier
	 * @return new idlist object
	 */
	public IDList idListWork(IncludeOrExclude iore, String string) {

		IDList idl = new IDList();
		// if excluding let's add 'em first then remove.
		if (iore == IncludeOrExclude.EXCLUDE)
			for (var cve : codesValues.entrySet()) {
				idl.add(cve.getKey(), cve.getValue());
			}
		idl.shortname = this.shortname;
		idl.setFiltered(true);
		var Strings = string.split(",");

		if (string.length() == 0) {
			throw new OBOEException("IDList inclusion/exclusion attribute error; too small or length is zero");
		}
		var keIter = codesValues.entrySet().iterator();
		nextWorkString: for (var workString : Strings) {
			var tkn = workString.trim();

			if (!tkn.contains("-")) {
				if (iore == IncludeOrExclude.EXCLUDE) {
					// include every thing but this
					while (keIter.hasNext()) {
						var ke = keIter.next();
						if (ke.getKey().equals(tkn)) {
							idl.remove(ke.getKey());
							continue nextWorkString;
						}

					}
					continue nextWorkString;
				} else {// exclude everything but this
					while (keIter.hasNext()) {
						var ke = keIter.next();
						if (ke.getKey().equals(tkn)) {
							idl.add(ke.getKey(), ke.getValue());
							continue nextWorkString; // found now add and exit.
						}
					}
					continue nextWorkString;
				}

			} else {
				var tkns = tkn.split(";");
				if (iore == IncludeOrExclude.EXCLUDE) {
					// include every thing up to the first and stop adding at the second
					while (keIter.hasNext()) {
						var ke = keIter.next();
						if (ke.getKey().equals(tkns[0])) {
							idl.remove(tkns[0]);
							break;
						}

					}
					while (keIter.hasNext()) {
						var ke = keIter.next();
						idl.remove(tkns[1]);
						if (ke.getKey().equals(tkns[1]))
							continue nextWorkString;
					}
					continue nextWorkString;
				} else {// exclude everything up to the first and include everything up to the second
					while (keIter.hasNext()) {
						var ke = keIter.next();
						if (ke.getKey().equals(tkns[0])) {
							idl.add(ke.getKey(), ke.getValue());
							break; // found now add and get the rest.
						}
					}
					while (keIter.hasNext()) {
						var ke = keIter.next();
						if (ke.getKey().equals(tkns[1])) {
							idl.add(ke.getKey(), ke.getValue());
							continue nextWorkString;
						}
						idl.add(ke.getKey(), ke.getValue());
					}
					continue nextWorkString;
				}

			}

		}

		return idl;
	}

	private void remove(String key) {
		codesValues.remove(key);

	}

	public int getSize() {

		return codesValues.size();
	}

	public TreeMap<String, String> getCodesValues() {

		return codesValues;
	}

}
