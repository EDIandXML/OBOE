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

import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Containers.ContainerType;
import io.github.EDIandXML.OBOE.Containers.MetaTemplateContainer;

/**
 * class for Template FunctionalGroups
 * <p>
 * OBOE - Open Business Objects for EDI
 * <p>
 * 
 * An EDI and XML Translator Written In Java <br>
 * 
 * 
 */

public class TemplateFunctionalGroup extends MetaTemplateContainer
		implements ITemplate, IContainedObject {

	/**
	 * creates a FunctionalGroup object
	 */
	public TemplateFunctionalGroup() {
		super();

	}

	/**
	 * creates a FunctionalGroup object
	 * 
	 * @param inParent owning Object
	 */
	public TemplateFunctionalGroup(IContainedObject inParent) {
		super();
		setParent(inParent);

	}

	protected IContainedObject parent = null;

	/**
	 * sets parent attribute
	 * 
	 * @param inParent TemplateSegmentContainer
	 */
	@Override
	public void setParent(IContainedObject inParent) {
		parent = inParent;
	}

	/**
	 * gets parent attribute
	 * 
	 * @return TemplateSegmentContainer
	 */
	@Override
	public IContainedObject getParent() {
		return parent;
	}

	@Override
	public String getID() {
		return "template fg";
	}

	@Override
	public String getShortName() {
		return "<templateFG>";
	}

	@Override
	public ContainerType getContainerType() {
		return ContainerType.FunctionalGroup;
	}

}
