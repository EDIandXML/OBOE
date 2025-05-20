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
package io.github.EDIandXML.OBOE;

import io.github.EDIandXML.OBOE.Containers.ContainerType;

/**
 * Defines a contract for objects within a hierarchy in OBOE (Open
 * Business Objects for EDI). Extends {@code Identifier} to provide ID
 * and XML tag functionality. Used for managing hierarchical
 * relationships between EDI elements, such as segments or loops.
 *
 * @author Joe McVerry
 * @since 2025
 */
public interface IContainedObject extends Identifier {

	/**
	 * Sets the parent object in the containment hierarchy. Implementations
	 * should validate the parent to avoid circular references.
	 *
	 *
	 * @param parent the parent {@code IContainedObject}, or null to clear
	 *               the parent
	 */
	void setParent(IContainedObject parent);

	/**
	 * Retrieves the parent object in the containment hierarchy.
	 *
	 * @return the parent {@code IContainedObject}, or null if this is a
	 *         root object
	 */
	IContainedObject getParent();

	/**
	 * Retrieves the type of container this object represents.
	 * Implementations should ensure the returned type is non-null.
	 *
	 * @return the {@code ContainerType} of this object
	 */
	ContainerType getContainerType();
}