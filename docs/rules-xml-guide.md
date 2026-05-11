# Rules XML Guide

OBOE uses a powerful XML-based rules system to define how EDI segments, loops, and elements should be parsed and validated.

## ediRules.xsd

The rules files must conform to the `ediRules.xsd` schema located in the project.

## Basic Structure

```xml
<ediRules xmlns="http://ediandxml.com/oboe/rules">
  <transactionSet id="850">
    <segment id="ST" required="true">
      <element pos="1" type="ID" />
      <!-- more elements -->
    </segment>
    <!-- loops and more segments -->
  </transactionSet>
</ediRules>
```

More details coming soon...