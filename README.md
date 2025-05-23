# OBOE - Open Business Objects for EDI

The previous version of this code used the package name americancoders.com. Someone (gd) stole the URL before I could renew it, so I changed the package name.

- Joe McVerry usacoder@gmail.   2025-05-20

## oboe.properties file

Use the oboe.properties file to provide direction for the program.  

-xmlPath - property points to the EDI message definition file folder.

-searchClassPathForMessageFiles (true or false). Directs the application to search the classpath for the message folder.

-searchDirective property provides a mechanism for the package to process incoming documents and use subfolders to find specific message definition files.

-errorLevelToReport—is from the old HIPAA package. It is used by the error logging routines to show what SNAP level (1-5) to report errors; it is generally not used.

-THROW_PARSING_EXCEPTION - OBOE Exceptions will not be thrown if set to TRUE.

-doPrevalidate - from the old HIPAA package to test field values with predefined classes, defaults to FALSE.

-realNumbersRetainPrecision - maintain numeric accuracy during conversion, defaults to FALSE.

-checkPropertyFileForChanges - constantly reloads the property file, defaults to FALSE.

-validCharacterSet - used for EDIFACT and TRADACOMS processing.

### Notes
In the oboe.properties file, you may find lines such as: <br/>
 HealthCareClaim=837
The well-formed XML parser uses this property. When an incoming document has a tag with "HealthCareClaim,"  this property informs the package to use the 837 message definition file.  


## Helpful Programs.

There is a GUI program that uses a stand-alone program that you can use to build programs to parse and create Java programs.

- The GUI is Util.TransactionSetMessageEditor will edit the message files and create Java programs
- The stand-alone program is Util.OBOECodeGenerator, called by TransactionSetMessageEditor to create Java programs.

## Creating Message Description Files (aka Rules Files) 

I create X12 transaction files using the X12 definitions purchased from X12.org many years ago. 
For TRADACOMS and EDIFACT files, a friend wrote a Perl script to parse the respective PDF files. The script's output is downloaded into a SQL database. If you're interested in the program, let me know - THERE IS NO DOCUMENTATION AND I HAVEN'T USED THE SCRIPT IN 20 YEARS.

Luckily, in the 25 years since then, A.I. has gotten much better. You can create the message files using Grok, OpenAI, or other tools. For instance, I told Grok to download the ediRules.xsd file, read the PDF files at Burlington Northern Santa Fe (https://www.bnsf.com/ship-with-bnsf/support-services/pdf/BNSF_322v4010.pdf), and generate a 322 Transaction Set message file. The file produced was usable.

## XML Files.

When I started writing the OBOE package, 25+ years ago, XML was coming into its own.  
I defined the EDI message in XML format and wrote code for the package to read and write EDI messages in XML format. EDI in XML format never took off. But these file types became useful for mock testing, so I have left that code in place.  

