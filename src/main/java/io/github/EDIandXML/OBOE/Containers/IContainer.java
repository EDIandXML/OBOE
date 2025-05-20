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

import io.github.EDIandXML.OBOE.IContainedObject;

/**
 * A marker interface for container objects in OBOE (Open Business
 * Objects for EDI), representing hierarchical structures such as
 * envelopes, segments, or functional groups. Extends IContainedObject
 * to inherit identification and hierarchy methods (e.g., getID,
 * getShortName, getParent, getContainerType). Implementations typically
 * manage contained objects in an EDI document structure.
 *
 *
 * @since 2025
 * @see IContainedObject
 * @see ContainerType
 * @see ContainerKey
 */
public interface IContainer extends IContainedObject {
}