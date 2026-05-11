# Generating EDI Documents

OBOE makes it easy to build EDI documents programmatically.

## Example

```java
EdiDocument doc = new EdiDocument("X12");
doc.addSegment("BEG").setElements("00", "SA", "PO12345");
```

See `examples/GenerateX12_850.java` for a complete example.