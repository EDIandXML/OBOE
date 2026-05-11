# OBOE — Open Business Objects for EDI

**Modern, lightweight, high-performance Java library for parsing, validating, and generating EDI documents.**

Supports:
- **ANSI X12** (including all HIPAA variants)
- **EDIFACT**
- **TRADACOMS**

OBOE is an open-source, actively maintained Java EDI engine with Maven Central support, clean API, and production-grade performance.

[![Maven Central](https://img.shields.io/maven-central/v/com.ediandxml/oboe)](https://central.sonatype.com/artifact/com.ediandxml/oboe)
[![Java](https://img.shields.io/badge/Java-8%2B-blue.svg)](https://www.java.com/)

**📖 [Full Documentation](https://EDIandXML.github.io/OBOE/)**

---

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>com.ediandxml</groupId>
    <artifactId>oboe</artifactId>
    <version>2026.04.08</version>
</dependency>
```

### Basic Usage Example
See the `examples/` folder for complete working samples.

## Features
- Streaming / SAX-style parsing (memory efficient)
- Full support for X12 envelopes and segments
- Strong HIPAA transaction support
- XML rules-based engine (`ediRules.xsd`)
- Built-in code generation capabilities
- Easy integration with Spring Boot and modern Java

## Examples
Full runnable examples are located in the [`examples/`](examples/) directory:

- `ParseX12_850.java` – Reading a Purchase Order
- `GenerateX12_850.java` – Creating an 850 from code
- More examples coming soon (EDIFACT, 837, JSON conversion, etc.)

## Documentation
- [📖 Full Documentation Site](https://EDIandXML.github.io/OBOE/)
- Full tutorial: [Parsing X12 850 with OBOE](https://dev.to/stock_trendswithjoe_712/parsing-x12-850-purchase-orders-in-java-with-oboe-1nnp)

## Why Choose OBOE?
Commercial EDI solutions are often expensive and complex. OBOE gives you full control, no vendor lock-in, and clean modern Java integration.

## Contributing
Contributions, issues, and feature requests are welcome!

## License
See the [LICENSE](LICENSE) file for details.