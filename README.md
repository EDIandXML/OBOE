# OBOE - Open Business Objects for EDI

[![Java](https://img.shields.io/badge/Java-8+-blue.svg)](https://www.java.com)
[![Maven](https://img.shields.io/badge/Maven-Ready-brightgreen)](pom.xml)
[![License](https://img.shields.io/github/license/EDIandXML/OBOE)](LICENSE)

**OBOE** is a lightweight, mature, and flexible Java library for parsing, validating, and generating Electronic Data Interchange (EDI) documents.

It fully supports:
- **ANSI X12** (4010, 5010, and many others)
- **UN/EDIFACT**
- **TRADACOMS**
- **HIPAA** transactions (837, 835, 834, 270/271, etc.)

OBOE uses **simple XML-based message definition files** (rules files) to define the structure of each transaction set. This makes it extremely extensible without hard-coding formats.

---

## ✨ Features

- Pure Java — no heavy commercial EDI engines required
- XML-driven rules engine (`ediRules.xsd`)
- Built-in code generator that creates strongly-typed Java classes
- Graphical Message Editor (`Util.TransactionSetMessageEditor`)
- Support for envelopes (ISA/GS/ST, UNB/UNH, etc.)
- Robust error handling and validation
- Lightweight with minimal dependencies
- 25+ years of real-world EDI battle-testing

## 🚀 Recent Updates (2025–2026)

- Package name migrated from `americancoders.com` → `io.github.EDIandXML`
- Repository cleaned up and modernized on GitHub
- Improved build process (Maven-ready)
- Better documentation and community readiness

## 📦 Installation

### Maven (Recommended)

Add this to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.ediandxml</groupId>
    <artifactId>OBOE</artifactId>
    <version>2026.04.08</version>
</dependency>