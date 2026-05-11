# Architecture

## Core Components

- **SAX-style Streaming Parser**: Low memory footprint, suitable for large files.
- **Rules Engine**: XML-driven validation and structure definition.
- **Envelope Handler**: ISA, GS, ST, SE, GE, IEA handling.
- **Document Model**: EdiDocument, Segment, Loop abstractions.

OBOE is designed to be thread-safe and highly performant.