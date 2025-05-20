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

package io.github.EDIandXML.OBOE.Templates;

/**
 * OBOE - Open Business Objects for EDI
 */

import java.util.Collection;
import java.util.TreeMap;

import io.github.EDIandXML.OBOE.Containers.MetaContainer;
import io.github.EDIandXML.OBOE.DataElements.IDListProcessor;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Tokenizers.ITokenizer;
import io.github.EDIandXML.OBOE.Tokenizers.Tokenizer;

public interface ITemplateElementContainer {

	public int getContainerSize();

	public void addElement(TemplateElement inTemplateElement)
			throws OBOEException;

	public TemplateElement getTemplateElement(int pos) throws OBOEException;

	public TemplateElement getTemplateElement(String id) throws OBOEException;

	public boolean isTemplateDE(int at) throws OBOEException;

	public boolean isTemplateComposite(int at) throws OBOEException;

	public int doYouUseThisElement(String inID, int startAt);

	public int doYouUseThisXMLElement(String inXML, int startAt);

	public boolean canYouPrevalidate();

	public boolean isThisYou(ITokenizer inToken);

	public boolean isThisYou(String primaryIDValue);

	public void whyNotYou(Tokenizer et, MetaContainer errContainer);

	public IDListProcessor getIDListThatPrevalidates();

	public String getID();

	TreeMap<Integer, TemplateElement> getAllTemplateElements();

	Collection<TemplateElement> getAllTemplateElementsValues();

}
