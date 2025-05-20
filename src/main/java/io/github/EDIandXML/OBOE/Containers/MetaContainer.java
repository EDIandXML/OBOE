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

import java.io.IOException;
import java.io.PushbackInputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.github.EDIandXML.OBOE.Format;
import io.github.EDIandXML.OBOE.IContainedObject;
import io.github.EDIandXML.OBOE.Identifier;
import io.github.EDIandXML.OBOE.DataElements.IDDE;
import io.github.EDIandXML.OBOE.Errors.DocumentErrors;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Parsers.SegmentParser;
import io.github.EDIandXML.OBOE.Templates.TemplateLoop;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;
import io.github.EDIandXML.OBOE.Tokenizers.ITokenizer;
import io.github.EDIandXML.OBOE.Tokenizers.Tokenizer;

/**
 * An abstract base class for runtime EDI containers in OBOE (Open
 * Business Objects for EDI), such as segments, loops, or transaction
 * sets, which hold actual data and reference templates from
 * MetaTemplateContainer. Implements IContainedObject, ITemplatedObject,
 * and Identifier to provide hierarchy, template association, and
 * identification functionality. Subclasses define specific container
 * behaviors and parsing logic.
 *
 *
 * @since 2025
 * @see IContainedObject
 * @see ITemplatedObject
 * @see Identifier
 * @see MetaTemplateContainer
 * @see ContainerType
 * @see Format
 */
public abstract class MetaContainer
		implements IContainedObject, ITemplatedObject, Identifier {

	private static final Logger logr = LogManager
			.getLogger(MetaContainer.class);

	/**
	 * The type of this container (e.g., Segment, Loop).
	 */
	protected final ContainerType myContainerType;

	/**
	 * The template associated with this container.
	 */
	protected final MetaTemplateContainer myTemplate;

	/**
	 * Maps container keys to their indices in the container list.
	 */
	private final SequencedMap<ContainerKey, Integer> keyContainer;

	/**
	 * Stores lists of contained objects, grouped by type and ID.
	 */
	private final ArrayList<ArrayList<IContainedObject>> theContainer;

	/**
	 * Indicates if a duplicate segment was detected during parsing.
	 */
	protected boolean dupSegment = false;

	/**
	 * The byte offset of the current container in the input stream.
	 */
	public int byteOffset = -1;

	/**
	 * Constructs a MetaContainer with the specified template.
	 *
	 * @param template the template for this container
	 * @throws IllegalArgumentException if template is null
	 */
	public MetaContainer(MetaTemplateContainer template) {
		Objects.requireNonNull(template, "Template cannot be null");
		this.myContainerType = template.getContainerType();
		this.myTemplate = template;
		this.keyContainer = new TreeMap<>();
		this.theContainer = new ArrayList<>();
	}

	/**
	 * Returns the map of container keys to their indices.
	 *
	 * @return the key container map
	 */
	public SequencedMap<ContainerKey, Integer> getKeyContainer() {
		return keyContainer;
	}

	/**
	 * Returns the list of contained object lists.
	 *
	 * @return the container lists
	 */
	public ArrayList<ArrayList<IContainedObject>> getTheContainer() {
		return theContainer;
	}

	/**
	 * Returns container keys filtered by the specified type.
	 *
	 * @param type the container type to filter
	 * @return a list of matching container keys
	 */
	public List<ContainerKey> getKeysByContainerType(ContainerType type) {
		return keyContainer.keySet().stream().filter(a -> a.type() == type)
				.toList();
	}

	/**
	 * Initializes a container space for the specified type with an empty
	 * ID.
	 *
	 * @param ct the container type
	 */
	public void buildContainerSpace(ContainerType ct) {
		var key = new ContainerKey(ct, "");
		var containedObjects = new ArrayList<IContainedObject>();
		theContainer.add(containedObjects);
		keyContainer.put(key, theContainer.indexOf(containedObjects));
	}

	/**
	 * Adds a contained object, validating its type and occurrence limits.
	 *
	 * @param inContainer the contained object to add
	 * @throws OBOEException if the type is invalid or occurrence limit is
	 *                       exceeded
	 */
	public void addContainer(IContainedObject inContainer)
			throws OBOEException {
		if (inContainer == null) {
			throw new OBOEException("Container cannot be null");
		}
		if (!getContainerType().containsType(inContainer.getContainerType())) {
			throw new OBOEException(
					"Can't contain a " + inContainer.getContainerType().name()
							+ " in a " + myContainerType.name());
		}

		var key = new ContainerKey(inContainer.getContainerType(),
				inContainer.getID());
		if (keyContainer.containsKey(key)) {
			var containedObjects = theContainer.get(keyContainer.get(key));
			if (inContainer.getContainerType() == ContainerType.Loop) {
				TemplateLoop tl = myTemplate
						.getTemplateLoop(inContainer.getID());
				if (tl.getOccurs() != 1) {
					if (tl.getOccurs() != -1
							&& containedObjects.size() >= tl.getOccurs()) {
						throw new OBOEException(
								"Can't add loop, occurs size will be exceeded");
					}
					containedObjects.add(inContainer);
					return;
				}
			}
			if (inContainer.getContainerType() == ContainerType.Segment) {
				TemplateSegment ts = myTemplate
						.getTemplateSegment(inContainer.getID());
				if (ts.getOccurs() != 1) {
					if (ts.getOccurs() != -1
							&& containedObjects.size() >= ts.getOccurs()) {
						throw new OBOEException(
								"Can't add segment, occurs size will be exceeded");
					}
					containedObjects.add(inContainer);
					return;
				}
			}
		}

		var containedObjects = new ArrayList<IContainedObject>();
		containedObjects.add(inContainer);
		theContainer.add(containedObjects);
		keyContainer.put(key, theContainer.indexOf(containedObjects));
	}

	/**
	 * Adds a contained object without using its ID, grouping by type only.
	 *
	 * @param inContainer the contained object to add
	 * @throws OBOEException if the type is invalid
	 */
	public void addContainerDontUseID(IContainedObject inContainer)
			throws OBOEException {
		if (inContainer == null) {
			throw new OBOEException("Container cannot be null");
		}
		if (!getContainerType().containsType(inContainer.getContainerType())) {
			throw new OBOEException(
					"Can't contain a " + inContainer.getContainerType().name()
							+ " in a " + myContainerType.name());
		}

		var key = new ContainerKey(inContainer.getContainerType(), "");
		if (keyContainer.containsKey(key)) {
			var containedObjects = theContainer.get(keyContainer.get(key));
			containedObjects.add(inContainer);
			return;
		}
		var containedObjects = new ArrayList<IContainedObject>();
		containedObjects.add(inContainer);
		theContainer.add(containedObjects);
		keyContainer.put(key, theContainer.indexOf(containedObjects));
	}

	/**
	 * Adds a segment to the container.
	 *
	 * @param inSegment the segment to add
	 * @throws OBOEException if the segment is invalid
	 */
	public void addSegment(Segment inSegment) throws OBOEException {
		addContainer(inSegment);
	}

	/**
	 * Adds a loop to the container.
	 *
	 * @param inLoop the loop to add
	 * @throws OBOEException if the loop is invalid or not allowed
	 */
	public void addLoop(Loop inLoop) throws OBOEException {
		if (!getContainerType().containsType(ContainerType.Loop)) {
			throw new OBOEException("Can't contain a " + ContainerType.Loop
					+ " in a " + myContainerType.name());
		}
		addContainer(inLoop);
	}

	/**
	 * Retrieves all contained objects of the specified type and ID.
	 *
	 * @param type the container type
	 * @param ID   the ID of the containers
	 * @return the list of matching contained objects
	 * @throws OBOEException if no containers are found
	 */
	public ArrayList<IContainedObject> getAllContainers(ContainerType type,
			String ID) throws OBOEException {
		var key = new ContainerKey(type, ID);
		var gotWhere = keyContainer.get(key);
		if (gotWhere == null) {
			throw new OBOEException(
					"Unknown container " + type.name() + " with id " + ID);
		}
		return theContainer.get(gotWhere);
	}

	/**
	 * Retrieves the first contained object of the specified type and ID.
	 *
	 * @param type the container type
	 * @param ID   the ID of the container
	 * @return the first matching contained object
	 * @throws OBOEException if no container is found or the container is
	 *                       empty
	 */
	public IContainedObject getFirstContainer(ContainerType type, String ID)
			throws OBOEException {
		var key = new ContainerKey(type, ID);
		var gotWhere = keyContainer.get(key);
		if (gotWhere == null) {
			throw new OBOEException(
					"Unknown container " + type.name() + " with id " + ID);
		}
		var got = theContainer.get(gotWhere);
		if (got.isEmpty()) {
			throw new OBOEException("Container " + type.name() + " with id "
					+ ID + " is empty.");
		}
		return got.get(0);
	}

	/**
	 * Retrieves a contained object by type, ID, and position.
	 *
	 * @param type the container type
	 * @param ID   the ID of the container (empty string for type-only
	 *             lookup)
	 * @param pos  the 0-based position in the container list
	 * @return the matching contained object
	 * @throws OBOEException if the container is not found, empty, or
	 *                       position is invalid
	 */
	public IContainedObject getContainer(ContainerType type, String ID, int pos)
			throws OBOEException {
		if (pos < 0) {
			throw new OBOEException("Container " + type.name() + " with id "
					+ ID + " can't have negative holders.");
		}
		ArrayList<IContainedObject> got;
		if (ID.length() == 0) {
			for (var entry : keyContainer.entrySet()) {
				if (entry.getKey().type() == type) {
					got = theContainer.get(entry.getValue());
					if (got.isEmpty()) {
						throw new OBOEException(
								"Container " + type.name() + " is empty.");
					}
					if (pos > got.size()) {
						throw new OBOEException(
								"Request for position " + pos + " inContainer "
										+ type.name() + " is too high.");
					}
					return got.get(pos);
				}
			}
			throw new OBOEException("Unknown container " + type.name()
					+ " with no id specified ");
		}
		var key = new ContainerKey(type, ID);
		var gotWhere = keyContainer.get(key);
		if (gotWhere == null) {
			throw new OBOEException(
					"Unknown container " + type.name() + " with id " + ID);
		}
		got = theContainer.get(gotWhere);
		if (got.isEmpty()) {
			throw new OBOEException("Container " + type.name() + " with id "
					+ ID + " is empty.");
		}
		if (pos > got.size()) {
			throw new OBOEException("Request for position " + pos
					+ " inContainer " + type.name() + " with id " + ID
					+ " is too high. ");
		}
		return got.get(pos);
	}

	/**
	 * Returns the number of contained objects of the specified type and ID.
	 *
	 * @param type the container type
	 * @param ID   the ID of the containers
	 * @return the count of matching containers
	 * @throws OBOEException if no containers are found
	 */
	public int getContainerSize(ContainerType type, String ID)
			throws OBOEException {
		var key = new ContainerKey(type, ID);
		var gotWhere = keyContainer.get(key);
		if (gotWhere == null) {
			throw new OBOEException(
					"Unknown container " + type.name() + " with id " + ID);
		}
		var got = theContainer.get(gotWhere);
		return got.size();
	}

	/**
	 * Returns the XML tag for this container, delegated to the template.
	 *
	 * @return the XML tag
	 */
	@Override
	public String getShortName() {
		return myTemplate.getShortName();
	}

	/**
	 * Returns the ID of this container. Subclasses must implement this
	 * method.
	 *
	 * @return the container's ID
	 */
	@Override
	public abstract String getID();

	/**
	 * Sets the parent container. Subclasses must implement this method.
	 *
	 * @param inParent the parent container
	 */
	@Override
	public abstract void setParent(IContainedObject inParent);

	/**
	 * Returns the parent container. Subclasses must implement this method.
	 *
	 * @return the parent container
	 */
	@Override
	public abstract IContainedObject getParent();

	/**
	 * Removes trailing empty containers, adjusting the container size.
	 * Subclasses must implement this method.
	 *
	 * @return the number of containers removed
	 */
	public abstract int trim();

	/**
	 * Returns the container type (e.g., Segment, Loop). Subclasses must
	 * implement this method.
	 *
	 * @return the container type
	 */
	@Override
	public abstract ContainerType getContainerType();

	/**
	 * Parses an EDI document from a tokenized input, adding segments or
	 * loops as appropriate.
	 *
	 * @param TransactionTokenizedString the tokenized EDI input
	 * @return true if the container used the current token, false otherwise
	 * @throws OBOEException if parsing fails or an invalid token is
	 *                       encountered
	 */
	public boolean parse(ITokenizer TransactionTokenizedString)
			throws OBOEException {
		boolean isUsed = false;
		byteOffset = TransactionTokenizedString.getInputByteCount();
		String foundId = "";
		boolean lookback = false;
		boolean foundButNotQuiteRight = false;
		ArrayList<IContainedObject> elmsArray = myTemplate.getContainer();
		int i = 0;

		nextElms: while (i < elmsArray.size()) {
			var elms = elmsArray.get(i);
			String nextToken = TransactionTokenizedString
					.getCurrentDataElement();
			foundId = nextToken;
			if (nextToken == null) {
				return false;
			}

			if (elms.getContainerType() == ContainerType.Loop) {
				TemplateLoop templateLoop = (TemplateLoop) elms;
				if (nextToken
						.equals(templateLoop.getContainer().get(0).getID())) {
					foundButNotQuiteRight = true;
					if (templateLoop.canYouPrevalidate() && !templateLoop
							.isThisYou(TransactionTokenizedString)) {
						i++;
						isUsed = false;
						continue nextElms;
					}
					var loopCnt = getLoopCnt(foundId);
					if (templateLoop.getOccurs() == loopCnt
							&& templateLoop.getOccurs() != -1) {
						TransactionTokenizedString.reportError(
								"Duplicate loop or segment at byte offset("
										+ TransactionTokenizedString
												.getInputByteCount()
										+ ")",
								this, "2", this,
								DocumentErrors.ERROR_TYPE.Integrity);
						return true;
					}
					foundButNotQuiteRight = false;
					var currentLoop = new Loop(templateLoop, this);
					addLoop(currentLoop);
					currentLoop.parse(TransactionTokenizedString);
					isUsed = true;
					if (templateLoop.getOccurs() == 1) {
						i++;
					} else if (equivalentLoops(i, elmsArray)) {
						i = resetToFirstEquivalentLoop(i, elmsArray);
					}
				} else {
					i++;
				}
			} else if (elms.getContainerType() == ContainerType.Segment) {
				TemplateSegment TemplateSegment = (TemplateSegment) elms;
				if (nextToken.equals(elms.getID())) {
					foundButNotQuiteRight = true;
					if (TemplateSegment.canYouPrevalidate() && !TemplateSegment
							.isThisYou(TransactionTokenizedString)) {
						i++;
						isUsed = false;
						continue;
					}
					foundButNotQuiteRight = false;
					var currentSegment = new Segment(TemplateSegment, this);
					currentSegment.setByteOffset(
							TransactionTokenizedString.getInputByteCount());
					addSegment(currentSegment);
					SegmentParser.parse(currentSegment,
							TransactionTokenizedString);
					if (TemplateSegment.getOccurs() == 1) {
						if (equivalentSegments(i, elmsArray)) {
							i = resetToFirstEquivalentSegment(i, elmsArray);
						} else {
							i++;
							continue;
						}
					} else if (equivalentSegments(i, elmsArray)) {
						i = resetToFirstEquivalentSegment(i, elmsArray);
					}
					isUsed = true;
				} else {
					i++;
				}
			}

			if (i == myTemplate.getContainer().size() && lookback) {
				lookback = false;
				i = 0;
			}
		}

		if (foundButNotQuiteRight) {
			logr.info(
					"Segment with id {} not used here. If using equivalent segments check the primary id value field for correctness. Near byte offset [{}]",
					foundId, byteOffset);
		}

		return isUsed;
	}

	private int getLoopCnt(String id) {
		var key = new ContainerKey(ContainerType.Loop, id);
		var got = keyContainer.get(key);
		if (got == null) {
			return 0;
		}
		var gotArray = theContainer.get(got);
		return gotArray.size();
	}

	protected boolean equivalentLoops(int i,
			ArrayList<IContainedObject> elmsArray) {
		if (i == 0) {
			return false;
		}
		var iTemplateLoop = elmsArray.get(i);
		TemplateLoop loopAtI = (TemplateLoop) iTemplateLoop;
		if (elmsArray.get(i - 1).getContainerType() != ContainerType.Loop) {
			return false;
		}
		var iOtherTemplateLoop = elmsArray.get(i - 1);
		TemplateLoop otherLoop = (TemplateLoop) iOtherTemplateLoop;
		TemplateSegment iTemplateSegment = (TemplateSegment) loopAtI
				.getContainer().get(0);
		TemplateSegment otherTemplateSegment = (TemplateSegment) otherLoop
				.getContainer().get(0);
		return loopAtI.getContainer().get(0).getID()
				.equals(otherLoop.getContainer().get(0).getID())
				& iTemplateSegment.getPosition() == otherTemplateSegment
						.getPosition();
	}

	protected int resetToFirstEquivalentLoop(int i,
			ArrayList<IContainedObject> elmsArray) {
		if (i == 0) {
			return 0;
		}
		int j = i;
		var iTemplateLoop = elmsArray.get(i);
		TemplateLoop loopAtI = (TemplateLoop) iTemplateLoop;
		while (j > -1) {
			if (elmsArray.get(j).getContainerType() != ContainerType.Loop) {
				return j;
			}
			if (!elmsArray.get(j).getID().equals(elmsArray.get(i).getID())) {
				return j;
			}
			var iOtherTemplateLoop = elmsArray.get(j);
			TemplateLoop otherLoop = (TemplateLoop) iOtherTemplateLoop;
			TemplateSegment iTemplateSegment = (TemplateSegment) loopAtI
					.getContainer().get(0);
			TemplateSegment otherTemplateSegment = (TemplateSegment) otherLoop
					.getContainer().get(0);
			if (iTemplateSegment.getPosition() != otherTemplateSegment
					.getPosition()) {
				return j;
			}
			j--;
		}
		return i;
	}

	protected boolean equivalentSegments(int i,
			ArrayList<IContainedObject> elmsArray) {
		if (i == 0) {
			return false;
		}
		TemplateSegment templateSegment = (TemplateSegment) elmsArray.get(i);
		if (elmsArray.get(i - 1).getContainerType() != ContainerType.Segment) {
			return false;
		}
		TemplateSegment otherTemplateSegment = (TemplateSegment) elmsArray
				.get(i - 1);
		return templateSegment.getID().equals(otherTemplateSegment.getID())
				& templateSegment.getPosition() == otherTemplateSegment
						.getPosition();
	}

	protected int resetToFirstEquivalentSegment(int i,
			ArrayList<IContainedObject> elmsArray) {
		if (i == 0) {
			return 0;
		}
		int j = i;
		var iTemplateSegment = elmsArray.get(i);
		TemplateSegment segmentAtI = (TemplateSegment) iTemplateSegment;
		while (j > -1) {
			if (elmsArray.get(j).getContainerType() != ContainerType.Segment) {
				return j;
			}
			if (!elmsArray.get(j).getID().equals(elmsArray.get(i).getID())) {
				return j;
			}
			var iOtherTemplateSegment = elmsArray.get(j);
			TemplateSegment otherSegment = (TemplateSegment) iOtherTemplateSegment;
			if (!segmentAtI.getID().equals(otherSegment.getID())
					|| segmentAtI.getPosition() != otherSegment.getPosition()) {
				return j;
			}
			j--;
		}
		return i;
	}

	/**
	 * Continues parsing an EDI document after an error, restarting from the
	 * specified container.
	 *
	 * @param inContainer             the last container used
	 * @param TransactionSetTokenizer the tokenizer holding transaction
	 *                                strings
	 * @return true if reparsing started successfully, false otherwise
	 * @throws OBOEException if parsing fails
	 */
	public boolean continueParse(MetaContainer inContainer,
			ITokenizer TransactionSetTokenizer) throws OBOEException {
		boolean restartSuccessful = false;
		if (inContainer == this) {
			if (parse(TransactionSetTokenizer)) {
				return true;
			}
		}
		for (var elms : theContainer) {
			for (var elmsofelms : elms) {
				if (elmsofelms.getContainerType() == ContainerType.Loop) {
					restartSuccessful = ((MetaContainer) elmsofelms)
							.continueParse(inContainer,
									TransactionSetTokenizer);
					if (restartSuccessful) {
						parse(TransactionSetTokenizer);
						return true;
					}
				}
			}
		}
		if (inContainer != this && restartSuccessful) {
			return parse(TransactionSetTokenizer);
		}
		return restartSuccessful;
	}

	/**
	 * Parses an XML EDI document, adding segments or loops based on the DOM
	 * node.
	 *
	 * @param node the DOM node to parse
	 * @throws OBOEException if the node is invalid or unknown
	 */
	public void parse(Node node) throws OBOEException {
		NodeList nl = node.getChildNodes();
		Node currentNode = node;
		ArrayList<IContainedObject> elmsArray = myTemplate.getContainer();

		nodeLoop: for (int n = 0; n < nl.getLength(); n++) {
			currentNode = nl.item(n);
			if (currentNode.getNodeType() != Node.ELEMENT_NODE) {
				continue nodeLoop;
			}
			for (int i = 0; i < elmsArray.size(); i++) {
				if (elmsArray.get(i).getShortName()
						.equals(currentNode.getNodeName())) {
					if (elmsArray.get(i)
							.getContainerType() == ContainerType.Loop) {
						var iTemplateLoop = elmsArray.get(i);
						TemplateLoop templateLoop = (TemplateLoop) iTemplateLoop;
						var currentLoop = new Loop(templateLoop, this);
						addContainer(currentLoop);
						currentLoop.parse(currentNode);
						continue nodeLoop;
					} else if (elmsArray.get(i)
							.getContainerType() == ContainerType.Segment) {
						TemplateSegment templateSegment = (TemplateSegment) elmsArray
								.get(i);
						var currentSegment = new Segment(templateSegment, this);
						addContainer(currentSegment);
						SegmentParser.parse(currentSegment, currentNode);
						continue nodeLoop;
					}
				}
			}
			throw new OBOEException("Unknown node found "
					+ currentNode.getNodeName() + " in container " + getID());
		}
	}

	/**
	 * Parses a fixed-length EDI document from an input stream.
	 *
	 * @param pis the pushback input stream
	 * @throws OBOEException if parsing fails
	 * @throws IOException   if an I/O error occurs
	 */
	public void parse(PushbackInputStream pis)
			throws OBOEException, IOException {
		for (var elms : myTemplate.getContainer()) {
			int idLen = elms.getID().length();
			byte[] me = new byte[idLen];
			if (pis.read(me) != idLen) {
				throw new OBOEException("expected data not read");
			}
			pis.unread(me);
			if (elms.getContainerType() == ContainerType.Loop) {
				var iTemplateLoop = elms;
				TemplateLoop templateLoop = (TemplateLoop) iTemplateLoop;
				var currentLoop = new Loop(templateLoop, this);
				addContainer(currentLoop);
			} else if (elms.getContainerType() == ContainerType.Segment) {
				TemplateSegment templateSegment = (TemplateSegment) elms;
				var currentSegment = new Segment(templateSegment, this);
				addContainer(currentSegment);
			} else {
				throw new OBOEException(
						"Logic error not expecting this container "
								+ elms.getContainerType());
			}
		}
	}

	/**
	 * Creates a segment with the specified ID.
	 *
	 * @param id the segment ID
	 * @return the created segment
	 */
	public Segment createSegment(String id) {
		return new Segment(myTemplate.getTemplateSegment(id), this);
	}

	/**
	 * Creates a segment with the specified ID and name.
	 *
	 * @param id     the segment ID
	 * @param sgname the segment name
	 * @return the created segment
	 */
	public Segment createSegmentByIDAndName(String id, String sgname) {
		return new Segment(myTemplate.getTemplateSegmentByIDAndName(id, sgname),
				this);
	}

	/**
	 * Creates and adds a segment with the specified ID and primary ID
	 * value, setting the primary ID.
	 *
	 * @param id             the segment ID
	 * @param primaryIDValue the primary ID value
	 * @return the created segment
	 * @throws OBOEException if the segment is unknown
	 */
	public Segment createAndAddSegmentWithPrimaryIDValue(String id,
			String primaryIDValue) throws OBOEException {
		TemplateSegment tseg = myTemplate.getTemplateSegment(id,
				primaryIDValue);
		if (tseg == null) {
			throw new OBOEException("Segment " + id + " with primaryIDValue of "
					+ primaryIDValue + " unknown to container.");
		}
		Segment seg = new Segment(tseg, this);
		addContainer(seg);
		if (seg != null) {
			seg.useDefault();
			IDDE de = (IDDE) seg.getPrimaryIDDE();
			de.set(primaryIDValue);
		}
		return seg;
	}

	/**
	 * Creates and adds a segment with the specified ID.
	 *
	 * @param id the segment ID
	 * @return the created segment
	 */
	public Segment createAndAddSegment(String id) {
		Segment seg = new Segment(myTemplate.getTemplateSegment(id), this);
		addContainer(seg);
		return seg;
	}

	/**
	 * Creates and adds a segment with the specified ID and primary ID.
	 *
	 * @param id        the segment ID
	 * @param primaryID the primary ID value
	 * @return the created segment, or null if the segment is unknown
	 */
	public Segment createAndAddSegment(String id, String primaryID) {
		var templateSegment = myTemplate.getTemplateSegment(id, primaryID);
		if (templateSegment == null) {
			return null;
		}
		Segment seg = new Segment(templateSegment, this);
		addContainer(seg);
		return seg;
	}

	/**
	 * Creates and adds a loop with the specified ID.
	 *
	 * @param id the loop ID
	 * @return the created loop
	 */
	public Loop createAndAddLoop(String id) {
		Loop loop = new Loop(myTemplate.getTemplateLoop(id), this);
		addContainer(loop);
		return loop;
	}

	/**
	 * Retrieves the first segment with the specified ID.
	 *
	 * @param id the segment ID
	 * @return the first matching segment, or null if not found
	 */
	public Segment getSegment(String id) {
		for (var key : keyContainer.keySet()) {
			if (key.type() == ContainerType.Segment) {
				if (key.ID().equals(id)) {
					return (Segment) theContainer.get(keyContainer.get(key))
							.get(0);
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves a segment by ID and position.
	 *
	 * @param id  the segment ID
	 * @param pos the 0-based position
	 * @return the matching segment, or null if not found
	 */
	public Segment getSegment(String id, int pos) {
		for (var key : keyContainer.keySet()) {
			if (key.type() == ContainerType.Segment) {
				if (key.ID().equals(id)) {
					return (Segment) theContainer.get(keyContainer.get(key))
							.get(pos);
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves a segment by ID and primary ID value, validating against
	 * prevalidation rules.
	 *
	 * @param ID             the segment ID
	 * @param primaryIDValue the primary ID value
	 * @return the matching segment, or null if not found
	 * @throws OBOEException if prevalidation fails
	 */
	public Segment getSegment(String ID, String primaryIDValue)
			throws OBOEException {
		Segment segment;
		for (var key : keyContainer.keySet()) {
			if (key.type() == ContainerType.Segment) {
				if (key.ID().equals(ID)) {
					segment = (Segment) theContainer.get(keyContainer.get(key))
							.get(0);
					var template = segment.getTemplate();
					if (template.canYouPrevalidate()) {
						if (template.isThisYou(primaryIDValue)) {
							return segment;
						}
					} else {
						throw new OBOEException(
								"Method does not work with this type of segment, this segment needs a primaryIDField");
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the total number of segments in this container, including
	 * those in loops.
	 *
	 * @return the segment count
	 */
	public int getSegmentCount() {
		int count = 0;
		for (var arrayOfContainers : theContainer) {
			for (var container : arrayOfContainers) {
				if (container.getContainerType() == ContainerType.Loop) {
					count += ((MetaContainer) container).getSegmentCount();
				} else if (container
						.getContainerType() == ContainerType.Segment) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * Returns the number of segments with the specified ID.
	 *
	 * @param id the segment ID
	 * @return the count of matching segments
	 */
	public int getSegmentCount(String id) {
		int count = 0;
		for (var key : keyContainer.keySet()) {
			if (key.type() == ContainerType.Segment) {
				if (key.ID().equals(id)) {
					count += theContainer.get(keyContainer.get(key)).size();
				}
			}
		}
		return count;
	}

	/**
	 * Retrieves a loop by ID and position.
	 *
	 * @param id  the loop ID
	 * @param pos the 0-based position
	 * @return the matching loop
	 */
	public Loop getLoop(String id, int pos) {
		ContainerKey ck = new ContainerKey(ContainerType.Loop, id);
		var ky = keyContainer.get(ck);
		var loops = theContainer.get(ky);
		return (Loop) loops.get(pos);
	}

	/**
	 * Returns the total number of loops and segments in this container,
	 * including those in nested loops.
	 *
	 * @return the total count
	 */
	public int getLoopAndSegmentCount() {
		int count = 0;
		for (var key : keyContainer.keySet()) {
			if (key.type() == ContainerType.Loop) {
				for (var loop : theContainer.get(keyContainer.get(key))) {
					count += ((MetaContainer) loop).getLoopAndSegmentCount();
				}
			}
			if (key.type() == ContainerType.Segment) {
				count += theContainer.get(keyContainer.get(key)).size();
			}
		}
		return count;
	}

	/**
	 * Retrieves all functional groups in this container.
	 *
	 * @return the list of functional groups, or null if none exist
	 */
	public ArrayList<IContainedObject> getFunctionalGroups() {
		for (var key : keyContainer.entrySet()) {
			if (key.getKey().type() == ContainerType.FunctionalGroup) {
				return theContainer.get(key.getValue());
			}
		}
		return null;
	}

	/**
	 * Returns the number of transaction sets in this container.
	 *
	 * @return the transaction set count
	 */
	public int getTransactionSetCount() {
		int cnt = 0;
		for (var key : keyContainer.keySet()) {
			if (key.type() == ContainerType.TransactionSet) {
				cnt += theContainer.get(keyContainer.get(key)).size();
			}
		}
		return cnt;
	}

	/**
	 * Appends the formatted text representation of this container to a
	 * StringBuilder.
	 *
	 * @param sbFormattedText the StringBuilder to append to
	 * @param format          the output format (e.g., X12, XML)
	 * @throws OBOEException if an invalid container type is encountered
	 */
	public void getContainerFormattedText(StringBuilder sbFormattedText,
			Format format) throws OBOEException {
		for (var elms : theContainer) {
			for (var elmsofelms : elms) {
				if (elmsofelms.getContainerType() == ContainerType.Loop) {
					Loop l = (Loop) elmsofelms;
					sbFormattedText.append(l.getFormattedText(format));
				} else if (elmsofelms
						.getContainerType() == ContainerType.Segment) {
					Segment s = (Segment) elmsofelms;
					sbFormattedText.append(s.getFormattedText(format));
				} else {
					throw new OBOEException("didn't expect this "
							+ elmsofelms.getContainerType());
				}
			}
		}
	}

	/**
	 * Writes the formatted text representation of this container to a
	 * Writer.
	 *
	 * @param inWriter the Writer to write to
	 * @param format   the output format (e.g., X12, XML)
	 * @throws IOException   if an I/O error occurs
	 * @throws OBOEException if an invalid container type is encountered
	 */
	public void writeContainerFormattedText(Writer inWriter, Format format)
			throws IOException, OBOEException {
		for (var elms : theContainer) {
			for (var elmsofelms : elms) {
				if (elmsofelms.getContainerType() == ContainerType.Loop) {
					Loop l = (Loop) elmsofelms;
					inWriter.write(l.getFormattedText(format));
				} else if (elmsofelms
						.getContainerType() == ContainerType.Segment) {
					Segment s = (Segment) elmsofelms;
					inWriter.write(s.getFormattedText(format));
				} else {
					throw new OBOEException("didn't expect this "
							+ elmsofelms.getContainerType());
				}
			}
		}
	}

	/**
	 * Trims trailing empty containers and updates the container size.
	 *
	 * @return the number of segments with non-empty elements
	 */
	public int trimTheContainer() {
		int cnt = 0;
		for (var elms : theContainer) {
			for (var elm : elms) {
				if (elm.getContainerType() == ContainerType.Segment) {
					Segment seg = (Segment) elm;
					if (seg.trim() > 0) {
						cnt++;
					} else {
						elms.remove(elm);
					}
				} else if (elm.getContainerType() == ContainerType.Loop) {
					Loop loop = (Loop) elm;
					cnt += loop.trim();
				}
			}
		}
		return cnt;
	}

	/**
	 * Validates the container and its elements, collecting errors.
	 *
	 * @param inDErr the DocumentErrors object to collect validation errors
	 */
	public void validateTheContainer(DocumentErrors inDErr) {
		for (var elms : theContainer) {
			for (var elm : elms) {
				if (elm.getContainerType() == ContainerType.Segment) {
					((Segment) elm).validate(inDErr);
				} else {
					((MetaContainer) elm).validate(inDErr);
				}
			}
		}
	}

	/**
	 * Validates the container and its elements.
	 *
	 * @return true if valid, false otherwise
	 */
	public boolean validate() {
		boolean validateResponse = true;
		for (var key : keyContainer.keySet()) {
			if (key.type() == ContainerType.Loop) {
				for (var loop : theContainer.get(keyContainer.get(key))) {
					validateResponse &= ((MetaContainer) loop).validate();
				}
			}
			if (key.type() == ContainerType.Segment) {
				for (var segment : theContainer.get(keyContainer.get(key))) {
					validateResponse &= ((Segment) segment).validate();
				}
			}
		}
		return validateResponse;
	}

	/**
	 * Validates the container, collecting errors. Subclasses must implement
	 * this method.
	 *
	 * @param inDErr the DocumentErrors object to collect validation errors
	 */
	public abstract void validate(DocumentErrors inDErr);

	/**
	 * Checks for missing required loops or segments, adding errors to
	 * DocumentErrors.
	 *
	 * @param inDErr the DocumentErrors object to collect errors
	 */
	public void testMissing(DocumentErrors inDErr) {
		for (var key : myTemplate.getKeyContainer().keySet()) {
			String id = key.ID();
			if (key.type() == ContainerType.Loop) {
				var tempLoop = myTemplate.getTemplateLoop(id);
				if (tempLoop.isRequired()) {
					var kc = new ContainerKey(ContainerType.Loop, id);
					if (!keyContainer.containsKey(kc)) {
						inDErr.addError(0, getID(),
								"Required loop (" + tempLoop.getID()
										+ ") missing",
								this, "3", this,
								DocumentErrors.ERROR_TYPE.Integrity);
						continue;
					}
					var loop = theContainer.get(keyContainer.get(kc));
					if (loop.size() == 0) {
						inDErr.addError(0, getID(),
								"Required loop (" + tempLoop.getID()
										+ ") missing",
								this, "3", this,
								DocumentErrors.ERROR_TYPE.Integrity);
					}
				}
			}
			if (key.type() == ContainerType.Segment) {
				var tempSegment = myTemplate.getTemplateSegment(id);
				if (tempSegment.isRequired()) {
					var kc = new ContainerKey(ContainerType.Segment, id);
					if (!keyContainer.containsKey(kc)) {
						inDErr.addError(0, getID(),
								"Required Segment (" + tempSegment.getID()
										+ ") missing",
								this, "3", this,
								DocumentErrors.ERROR_TYPE.Integrity);
						continue;
					}
					var segment = theContainer.get(keyContainer.get(kc));
					if (segment.size() == 0) {
						inDErr.addError(0, getID(),
								"Required Segment (" + tempSegment.getID()
										+ ") missing",
								this, "3", this,
								DocumentErrors.ERROR_TYPE.Integrity);
					}
				}
			}
		}
	}

	/**
	 * Returns the template associated with this container.
	 *
	 * @return the MetaTemplateContainer template
	 */
	@Override
	public MetaTemplateContainer getMyTemplate() {
		return myTemplate;
	}

	/**
	 * Reports why the current token was not used by this container.
	 *
	 * @param et the tokenizer with the current token
	 */
	public void whyNotUsed(Tokenizer et) {
		if (dupSegment) {
			et.reportError("Duplicate Segment) at byte offset ("
					+ et.getInputByteCount() + ")", "2");
			return;
		}
		for (var elms : getMyTemplate().getContainer()) {
			if (!et.getCurrentDataElement().equals(elms.getID())) {
				continue;
			}
			if (elms.getContainerType() == ContainerType.Loop) {
				TemplateLoop tloop = (TemplateLoop) elms;
				if (tloop.canYouPrevalidate() && !tloop.isThisYou(et)) {
					continue;
				}
				tloop.whyNotYou(et);
				return;
			} else if (elms.getContainerType() == ContainerType.Segment) {
				TemplateSegment tseg = (TemplateSegment) elms;
				if (tseg.canYouPrevalidate()) {
					tseg.whyNotYou(et, this);
					return;
				}
			}
		}
		et.reportError(
				"Unknown or out of place segment (" + et.getCurrentDataElement()
						+ ") at byte offset (" + et.getInputByteCount() + ")",
				"2");
	}
}