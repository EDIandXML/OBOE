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

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Identifier;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.ITemplate;
import io.github.EDIandXML.OBOE.Templates.TemplateFunctionalGroup;
import io.github.EDIandXML.OBOE.Templates.TemplateLoop;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;

/**
 * An abstract base class for template containers in OBOE (Open Business
 * Objects for EDI), managing contained objects such as segments, loops,
 * tables, transaction sets, functional groups, or envelopes. Implements
 * ITemplate for identification, IContainer for containment, and
 * Identifier for ID and XML tag access. Subclasses (e.g.,
 * TemplateEnvelope, TemplateFunctionalGroup) define specific container
 * types and behaviors.
 *
 *
 * @since 2025
 * @see ITemplate
 * @see IContainer
 * @see Identifier
 * @see TemplateSegment
 * @see TemplateLoop
 * @see TemplateFunctionalGroup
 */
public abstract class MetaTemplateContainer
		implements ITemplate, IContainer, Identifier {

	/**
	 * A map of container keys to indices in theContainer, ensuring ordered
	 * access.
	 */
	protected final TreeMap<ContainerKey, Integer> keyContainer;

	/**
	 * A list of contained objects (e.g., segments, loops, tables).
	 */
	protected final ArrayList<IContainedObject> theContainer;

	/**
	 * Constructs a MetaTemplateContainer with empty key and container
	 * collections.
	 */
	public MetaTemplateContainer() {
		keyContainer = new TreeMap<>();
		theContainer = new ArrayList<>();
	}

	/**
	 * Returns the list of contained objects.
	 *
	 * @return the list of contained objects, never null
	 */
	public ArrayList<IContainedObject> getContainer() {
		return new ArrayList<>(theContainer); // Defensive copy
	}

	/**
	 * Adds a contained object to the container, mapping its key to its
	 * index.
	 *
	 * @param container the contained object to add
	 * @throws OBOEException if the container type is not allowed in this
	 *                       container
	 */
	public void addContainer(IContainedObject container) throws OBOEException {
		if (container == null) {
			throw new OBOEException("Container cannot be null");
		}
		if (!getContainerType().containsType(container.getContainerType())) {
			throw new OBOEException(String.format("Cannot contain %s in %s",
					container.getContainerType().name(),
					getContainerType().name()));
		}
		ContainerKey key = new ContainerKey(container.getContainerType(),
				container.getID());
		theContainer.add(container);
		keyContainer.put(key, theContainer.size() - 1);
	}

	/**
	 * Retrieves a contained object by its type and ID.
	 *
	 * @param type the container type
	 * @param id   the container ID
	 * @return the contained object
	 * @throws OBOEException if the container is not found
	 */
	public IContainedObject getContainer(ContainerType type, String id)
			throws OBOEException {
		if (type == null || id == null) {
			throw new OBOEException("Container type or ID cannot be null");
		}
		ContainerKey key = new ContainerKey(type, id);
		Integer index = keyContainer.get(key);
		if (index == null) {
			throw new OBOEException(String.format(
					"Unknown container %s with id %s", type.name(), id));
		}
		return theContainer.get(index);
	}

	/**
	 * Returns the ID of the container (to be implemented by subclasses).
	 *
	 * @return the container ID, or null if not applicable
	 */
	@Override
	public String getID() {
		return null; // Subclasses should override
	}

	/**
	 * Sets the parent container (to be implemented by subclasses).
	 *
	 * @param parent the parent container
	 */
	@Override
	public void setParent(IContainedObject parent) {
		// Subclasses should override
	}

	/**
	 * Returns the parent container (to be implemented by subclasses).
	 *
	 * @return the parent container, or null if not set
	 */
	@Override
	public IContainedObject getParent() {
		return null; // Subclasses should override
	}

	/**
	 * Trims unused containers and returns the number removed (to be
	 * implemented by subclasses).
	 *
	 * @return the number of containers removed
	 */
	public int trim() {
		return 0; // Subclasses should override
	}

	/**
	 * Returns the container type (must be implemented by subclasses).
	 *
	 * @return the container type
	 */
	@Override
	public abstract ContainerType getContainerType();

	/**
	 * Returns the template functional group, if present.
	 *
	 * @return the TemplateFunctionalGroup, or null if not found
	 */
	public TemplateFunctionalGroup getTemplateFunctionalGroup() {
		for (IContainedObject entry : theContainer) {
			if (entry.getContainerType() == ContainerType.FunctionalGroup) {
				return (TemplateFunctionalGroup) entry;
			}
		}
		return null;
	}

	/**
	 * Returns the template segment with the specified ID.
	 *
	 * @param id the segment ID
	 * @return the TemplateSegment, or null if not found
	 */
	public TemplateSegment getTemplateSegment(String id) {
		for (IContainedObject entry : theContainer) {
			if (entry.getContainerType() == ContainerType.Segment
					&& entry.getID().equals(id)) {
				return (TemplateSegment) entry;
			}
		}
		return null;
	}

	/**
	 * Returns the template segment with the specified ID and primary ID
	 * value for prevalidation.
	 *
	 * @param id        the segment ID
	 * @param primaryID the primary ID value for prevalidation
	 * @return the TemplateSegment, or null if not found or does not match
	 */
	public TemplateSegment getTemplateSegment(String id, String primaryID) {
		for (IContainedObject entry : theContainer) {
			if (entry.getContainerType() == ContainerType.Segment
					&& entry.getID().equals(id)) {
				TemplateSegment segment = (TemplateSegment) entry;
				if (segment.canYouPrevalidate()
						&& segment.isThisYou(primaryID)) {
					return segment;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the template loop with the specified ID.
	 *
	 * @param id the loop ID
	 * @return the TemplateLoop, or null if not found
	 */
	public TemplateLoop getTemplateLoop(String id) {
		for (IContainedObject entry : theContainer) {
			if (entry.getContainerType() == ContainerType.Loop
					&& entry.getID().equals(id)) {
				return (TemplateLoop) entry;
			}
		}
		return null;
	}

	/**
	 * Returns the template segment with the specified ID and name.
	 *
	 * @param id   the segment ID
	 * @param name the segment name
	 * @return the TemplateSegment, or null if not found
	 */
	public TemplateSegment getTemplateSegmentByIDAndName(String id,
			String name) {
		for (IContainedObject entry : theContainer) {
			if (entry.getContainerType() == ContainerType.Segment
					&& entry.getID().equals(id)) {
				TemplateSegment segment = (TemplateSegment) entry;
				if (segment.getName().equals(name)) {
					return segment;
				}
			}
		}
		return null;
	}

	/**
	 * Checks if the container or its loops contain a segment with the
	 * specified ID.
	 *
	 * @param id the segment ID to check
	 * @return true if the segment ID is contained, false otherwise
	 * @throws OBOEException if an error occurs during validation
	 */
	public boolean doYouWantThisSegment(String id) throws OBOEException {
		if (id == null) {
			return false;
		}
		for (IContainedObject entry : theContainer) {
			if (entry.getContainerType() == ContainerType.Loop) {
				TemplateLoop loop = (TemplateLoop) entry;
				if (!loop.theContainer.isEmpty()
						&& loop.theContainer.get(0).getID().equals(id)) {
					return true;
				}
			} else if (entry.getContainerType() == ContainerType.Segment
					&& entry.getID().equals(id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the map of container keys to indices.
	 *
	 * @return the key container map, never null
	 */
	public Map<ContainerKey, Integer> getKeyContainer() {
		return new TreeMap<>(keyContainer); // Defensive copy
	}

	/**
	 * Returns a string representation of the container.
	 *
	 * @return a string describing the container type and number of
	 *         contained objects
	 */
	@Override
	public String toString() {
		return String.format("MetaTemplateContainer[type=%s, containers=%d]",
				getContainerType().name(), theContainer.size());
	}

	/**
	 * Returns the XML tag for the container (to be implemented by
	 * subclasses).
	 *
	 * @return the XML tag
	 */
	@Override
	public String getShortName() {
		return null; // Subclasses should override
	}
}