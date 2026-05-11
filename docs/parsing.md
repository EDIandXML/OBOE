# Parsing EDI Documents

OBOE uses a streaming SAX-style parser for excellent performance and low memory usage.

## Basic Parsing Example

```java
EdiParser parser = new EdiParser();
EdiDocument document = parser.parse(ediString, "rules/x12/850.rules.xml");
```

## Working with Segments and Loops

- `document.getSegment("BEG")`
- `document.getLoop("PO1")`

Full details coming soon.