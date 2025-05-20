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

import java.util.Objects;

/**
 * A record representing a key for identifying containers in OBOE (Open
 * Business Objects for EDI). Combines a ContainerType and an ID to
 * uniquely identify containers, such as envelopes, segments, or
 * functional groups. Implements Comparable to enable sorting by
 * ContainerType rank and then ID.
 *
 *
 * @param type the type of container (e.g., envelope, segment)
 * @param ID   the unique identifier for the container
 * @since 2025
 * @see ContainerType
 */
public record ContainerKey(ContainerType type, String ID)
		implements Comparable<ContainerKey> {

	/**
	 * Constructs a ContainerKey with the specified type and ID.
	 *
	 * @param type the container type, must not be null
	 * @param ID   the container ID, must not be null
	 * @throws NullPointerException if type or ID is null
	 */
	public ContainerKey {
		Objects.requireNonNull(type, "Container type must not be null");
		Objects.requireNonNull(ID, "Container ID must not be null");
	}

	/**
	 * Compares this ContainerKey to another based on ContainerType rank and
	 * ID. Containers are first sorted by the rank of their type
	 * (ascending), and if ranks are equal, by their ID (lexicographically).
	 *
	 * @param other the other ContainerKey to compare to
	 * @return a negative integer, zero, or a positive integer if this key
	 *         is less than, equal to, or greater than the specified key
	 * @throws NullPointerException if other or its fields are null
	 */
	@Override
	public int compareTo(ContainerKey other) {
		Objects.requireNonNull(other, "Comparison target must not be null");
		int rankComparison = Integer.compare(type.getRank(),
				other.type.getRank());
		return rankComparison != 0 ? rankComparison : ID.compareTo(other.ID);
	}
}