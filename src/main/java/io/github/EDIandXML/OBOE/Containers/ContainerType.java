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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import io.github.EDIandXML.OBOE.Format;

/**
 * Defines the types of containers in OBOE (Open Business Objects for
 * EDI), representing hierarchical structures in EDI documents, such as
 * envelopes, segments, and data elements. Each type has a rank for
 * sorting and specifies which other types it can contain.
 *
 *
 * @since 2025
 * @see ContainerKey
 * @see io.github.EDIandXML.OBOE.IContainedObject
 */
public enum ContainerType {
	/**
	 * Represents an individual data element within a segment or composite
	 * element.
	 */
	DataElement(7),

	/**
	 * Represents a composite element, grouping multiple data elements
	 * within a segment.
	 */
	CompositeElement(6, "DataElement"),

	/**
	 * Represents an EDI segment, containing data elements or composite
	 * elements.
	 */
	Segment(5, "DataElement", "CompositeElement"),

	/**
	 * Represents a loop, grouping segments or other loops
	 * (self-referential).
	 */
	Loop(4, "Segment", "Loop"),

	/**
	 * Represents a table, grouping segments or loops within a transaction
	 * set.
	 */
	Table(3, "Segment", "Loop"),

	/**
	 * Represents a transaction set, containing tables.
	 */
	TransactionSet(2, "Table"),

	/**
	 * Represents a functional group, containing segments or transaction
	 * sets.
	 */
	FunctionalGroup(1, "Segment", "TransactionSet"),

	/**
	 * Represents an envelope, the top-level container for functional groups
	 * or segments.
	 */
	Envelope(0, "Segment", "FunctionalGroup");

	/**
	 * The rank of the container type, used for sorting (lower rank = higher
	 * priority).
	 */
	private int rank;

	/**
	 * The set of container types this type can contain.
	 */
	private Set<ContainerType> containedTypes;

	/**
	 * The names of contained types, used to initialize containedTypes
	 * post-construction.
	 */
	private String[] containedTypeNames;

	/**
	 * Constructs a ContainerType with the specified rank and contained type
	 * names.
	 *
	 * @param rank               the sorting rank (lower is higher priority)
	 * @param containedTypeNames the names of types this container can
	 *                           contain
	 */
	ContainerType(int rank, String... containedTypeNames) {
		this.rank = rank;
		this.containedTypeNames = containedTypeNames;
		this.containedTypes = new HashSet<>();
	}

	// Static initializer to populate containedTypes after all constants are
	// defined
	static {
		for (ContainerType type : values()) {
			for (String typeName : type.containedTypeNames) {
				try {
					ContainerType containedType = valueOf(typeName);
					type.containedTypes.add(containedType);
				} catch (IllegalArgumentException e) {
					throw new IllegalStateException(String.format(
							"Invalid contained type name '%s' for %s", typeName,
							type), e);
				}
			}
			type.containedTypes = Set.copyOf(type.containedTypes); // Immutable
																	// copy
		}
	}

	/**
	 * Returns the rank of this container type, used for sorting in
	 * ContainerKey.
	 *
	 * @return the rank (lower values indicate higher priority)
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * Checks if this container type can contain the specified type.
	 *
	 * @param type the container type to check
	 * @return true if this type can contain the specified type, false
	 *         otherwise
	 * @throws NullPointerException if type is null
	 */
	public boolean containsType(ContainerType type) {
		Objects.requireNonNull(type, "Container type must not be null");
		return containedTypes.contains(type);
	}

	/**
	 * Returns the EDI format associated with this container type. Default
	 * implementation returns UNDEFINED; override for format-specific types.
	 *
	 * @return the associated Format
	 */
	public Format getFormat() {
		return Format.UNDEFINED; // Override in specific constants if needed
	}
}