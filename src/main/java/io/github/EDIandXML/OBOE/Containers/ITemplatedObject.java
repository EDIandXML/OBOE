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
package io.github.EDIandXML.OBOE.Containers;

import io.github.EDIandXML.OBOE.Templates.ITemplate;

/**
 * Defines a contract for objects in OBOE (Open Business Objects for
 * EDI) that are associated with a template, such as segments,
 * envelopes, or functional groups. Implementations provide access to
 * their corresponding template, which defines metadata or rules for the
 * EDI structure.
 *
 *
 * @since 2025
 * @see ITemplate
 * @see io.github.EDIandXML.OBOE.IContainedObject
 * @see IContainer
 */
public interface ITemplatedObject {

	/**
	 * Retrieves the template associated with this object.
	 *
	 * @return the associated ITemplate, or null if no template is set
	 */

	ITemplate getMyTemplate();
}