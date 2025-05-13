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

package io.github.EDIandXML.OBOE.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import io.github.EDIandXML.OBOE.EnvelopeFactory;
import io.github.EDIandXML.OBOE.TransactionSetFactory;
import io.github.EDIandXML.OBOE.Containers.ContainerType;
import io.github.EDIandXML.OBOE.Containers.TransactionSet;
import io.github.EDIandXML.OBOE.Errors.OBOEException;
import io.github.EDIandXML.OBOE.Templates.TemplateCompositeElement;
import io.github.EDIandXML.OBOE.Templates.TemplateDataElement;
import io.github.EDIandXML.OBOE.Templates.TemplateEnvelope;
import io.github.EDIandXML.OBOE.Templates.TemplateLoop;
import io.github.EDIandXML.OBOE.Templates.TemplateSegment;
import io.github.EDIandXML.OBOE.Templates.TemplateTable;
import io.github.EDIandXML.OBOE.Templates.TemplateTransactionSet;

/**
 * OBOE - Open Business Objects for EDI
 *
 * use this application to generate a program that uses a parsed OBOE object or
 * create an OBOE object. The application reads in a message description to generate the
 * code. A lot of code is generated but not enough to be completely useful. You
 * will have to remove code for segments and data elements you don't use and add
 * code for logic that is specific to your system requirements. format: java
 * OBOECodeGenerator p|bJ envelope tsid [-useName] [outputFileName] where p
 * generates a Java program that parses an EDI document or b generates a Java
 * program that builds an EDI document and envelope is the envelope xml file to
 * use and tsid is the TransactionSet id to build and optional -useName
 * indicates that method names should be defined by the objects name attribute
 * and not its id attribute. and optional outputFileName is any valid file name
 * to where results are stored.
 *
 * If the file name ends with .java the program will create the class name from
 * the file name otherwise it will generate the class name from the type of
 * build and the transaction set id.
 *
 *
 * @author Joe McVerry
 * 
 */

public class OBOECodeGenerator

{

	/** pw control output file */
	private PrintWriter pw = null;
	/** what are we building */
	private char ediType; /* x - x12, e - EDIFACT, t - tradacoms, a -ach */
	/** id of transaction set */
	private String tsID = "";

	private String lineFeed = System.getProperty("line.separator");

	private boolean useName = false;

	private Hashtable<String, Integer> methodNameTable = new Hashtable<String, Integer>(2000);

	private String envVersion = "";

	/**
	 * wake up and smell the coffee. This method is used to show the correct format
	 * for calling the application
	 */
	private static void describeStart() {
		System.err.println(
				"format: java OBOECodeGenerator p|b   envelopeRulesFileName tsid [-useName] [outputFileName] [-envelopeVersion=?]");
		System.err.println("    where p generates a Java program that parses an EDI document.");
		System.err.println("       or b generates a Java program that builds an EDI document.");
		System.err.println(
				"    and envelopeRulesFileName is the OBOE Envlope Message Description to use with the transaction set");
		System.err.println("    and tsid is the TransactionSet id to build");
		System.err.println(
				"    and optional -useName is a switch to indicate the Java method names are built with the object's name");
		System.err.println("        otherwise Java method names are buit with the object's id.");
		System.err.println("    and optional outputFileName is any valid file name to where results are stored.");
		System.err.println(
				"    and optional -envelopeVersion is a value to indicate which subdirectory in the XMLPath specification to find the envelope.");
		System.err.println(
				"If the file name ends with .java or .xml the program will create the class name from the file name");
		System.err.println("  otherwise it will generate the class name from the type and tsid.");
		System.err.println("");
		System.exit(0);
	}

	/**
	 * to make this a java application the class needs a main method.
	 * <ul>
	 * accepts 3 or 4 arguments
	 * <li>p or b - indicates if the application is for use with Parsed object or to
	 * Build an object
	 * <li>tsid - transaction set id to use
	 * <li>-useName - optional parameter to indicate that method names are built
	 * with the object's name otherwise use the object's id
	 * <li>outputFilename - optional output file name, if not used output sent to
	 * System.out
	 */
	public static void main(String args[]) {

		String envelope = "";

		String tsid = "";
		String type = "";

		String outputFileName = null;
		boolean useName = false;
		String envVersion = null;

		// if (args.length == 0);
		// else
		if ((args.length < 3) || (args.length > 6)) {
			describeStart();
		} else if (args[0].length() != 1) {
			describeStart();
		} else if (((args[0].charAt(0) != 'p') && (args[0].charAt(0) != 'b'))) {
			describeStart();
		} else {
			type = args[0];
			envelope = args[1];
			tsid = args[2];
			for (int i = 3; i < args.length; i++) {
				if (args[i].startsWith("-")) {
					if (args[i].equals("-useName")) {
						if (useName) {
							describeStart();
							return;
						}
						useName = true;
					} else if (args[i].startsWith("-envelopeVersion=")) {
						if (envVersion != null) {
							describeStart();
							return;
						}
						envVersion = args[i].split("=")[1];

					} else {
						describeStart();
						return;

					}
				} else {
					if (outputFileName != null) {
						describeStart();
						return;
					} else {
						outputFileName = args[i];
					}
				}
			}
		}

		TransactionSet ts = TransactionSetFactory.buildTransactionSet(tsid);

		new OBOECodeGenerator(envelope, ts, type, useName, outputFileName, envVersion);
	}

	/**
	 * constructor accepts 5 arguments
	 *
	 * @param inEnvelope     xml file name
	 * @param inTS           - transaction set id to use
	 * @param type           "p" or "b"; p - Java parsing program; b - Java building
	 *                       program;
	 * @param inUseName      - boolean to indicate if object names are used
	 * @param outputFileName - write resultant code to this filename, if null then
	 *                       send to System.out If filename ends with .java then
	 *                       creates the classname from the filename otherwise uses
	 *                       type and tsid fields.
	 * @param envVersion     - subdirectory in path specified by xmlPath property
	 *                       where envelope file can be found.
	 */
	public OBOECodeGenerator(String inEnvelope, TransactionSet inTS, String type, boolean inUseName,
			String outputFileName, String inEnvVersion) {
		String programname = type + tsID;
		try {
			if (outputFileName == null) {
				pw = new PrintWriter(System.out);
			} else {
				FileOutputStream fos = new FileOutputStream(outputFileName);
				if (outputFileName.toLowerCase().endsWith(".java")) {
					File file = new File(outputFileName);
					String filename = file.getName();
					programname = filename.substring(0, filename.length() - 5);
				}
				pw = new PrintWriter(fos);
			}
			envVersion = inEnvVersion;
			if (envVersion == null) {
				envVersion = "";
			}
			Util.setOBOEProperty(Util.SEARCH_DIRECTIVE, "V");

			TemplateEnvelope env = EnvelopeFactory.buildEnvelope(inEnvelope, envVersion);

			doJava(env, inTS, type, inUseName, programname);

			pw.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.exit(1);
		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(1);
		}
	}

	String javaParseImports = """
			import java.io.File;
			import java.io.FileReader;
			import java.io.IOException;

			import io.github.EDIandXML.OBOE.Containers.ContainerType;
			import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
			import io.github.EDIandXML.OBOE.Containers.Loop;
			import io.github.EDIandXML.OBOE.Containers.Segment;
			import io.github.EDIandXML.OBOE.Containers.Table;
			import io.github.EDIandXML.OBOE.Containers.TransactionSet;
			import io.github.EDIandXML.OBOE.DataElements.CompositeElement;
			import io.github.EDIandXML.OBOE.DataElements.DataElement;
			import io.github.EDIandXML.OBOE.Errors.OBOEException;
			import io.github.EDIandXML.OBOE.x12.X12DocumentHandler;
			import io.github.EDIandXML.OBOE.x12.X12Envelope;
									""";

	String javaBuildImports = """
			import io.github.EDIandXML.OBOE.EnvelopeFactory;
			import io.github.EDIandXML.OBOE.TransactionSetFactory;
			import io.github.EDIandXML.OBOE.Containers.FunctionalGroup;
			import io.github.EDIandXML.OBOE.Containers.Loop;
			import io.github.EDIandXML.OBOE.Containers.Segment;
			import io.github.EDIandXML.OBOE.Containers.Table;
			import io.github.EDIandXML.OBOE.Containers.TransactionSet;
			import io.github.EDIandXML.OBOE.DataElements.CompositeElement;
			import io.github.EDIandXML.OBOE.DataElements.DataElement;
			import io.github.EDIandXML.OBOE.Errors.OBOEException;
			import io.github.EDIandXML.OBOE.x12.X12Envelope;
						""";

	public void doJava(TemplateEnvelope inEnv, TransactionSet inTS, String programType, boolean inUseName,
			String inPgmName) throws Exception {
		useName = inUseName;
		tsID = inTS.getID();
		String programname = inPgmName;

		switch (inEnv.getType()) {
		case "X12":
			ediType = 'x';
			break;
		case "EDIFACT":
			ediType = 'e';
			break;
		case "Tradacoms":
			ediType = 't';
			break;
		case "ACH":
			ediType = 'a';

			break;
		default:
			throw new OBOEException("Unknown type " + inEnv.getType());
		}

		if (programType.equals("b")) {
			pw.write(javaBuildImports.replaceAll("X12", inEnv.getType()));
			pw.write(lineFeed);
			pw.write("/** code template to build" + lineFeed + "*class " + tsID + " " + inTS.getName() + lineFeed + "*"
					+ lineFeed);
		} else if (programType.equals("p")) {
			pw.write(javaParseImports.replaceAll("X12", inEnv.getType()));
			pw.write(lineFeed);
			pw.write("/** code template to parse" + lineFeed + "*class " + tsID + " " + inTS.getName() + lineFeed + "*"
					+ lineFeed);
		}
		int ispace;
		for (int ic = 0; ic < inTS.getShortDescription().length(); ic++) {
			ispace = inTS.getShortDescription().indexOf(' ', ic + 60);
			if (ispace < 0) {
				ispace = inTS.getShortDescription().length();
			}
			pw.write("* " + inTS.getShortDescription().substring(ic, ispace) + lineFeed);
			ic = ispace;
		}
		pw.write("*@author OBOECodeGenerator" + lineFeed + "*/" + lineFeed);
		pw.write("public class " + programname + lineFeed + "{" + lineFeed);
		pw.write("/** constructor for class " + programname + lineFeed);
		if (programType.equals("p")) {
			pw.write("*@param inFileName - String filename to be parsed" + lineFeed);
		}

		pw.write("*@throws OBOEException - most likely TransactionSet not found" + lineFeed);
		if (programType.equals("p")) {
			pw.write("*@throws IOException - most likely input file not found" + lineFeed);
		}

		pw.write("*/" + lineFeed);
		if (programType.equals("b")) {
			pw.write("public " + programname + "()" + lineFeed + "  throws OBOEException" + lineFeed + "{" + lineFeed);
			if (ediType == 'x') {

				pw.write("  X12Envelope env = new X12Envelope(EnvelopeFactory.buildEnvelope(\"x12.envelope\", \""
						+ envVersion + "\"));" + lineFeed);
				pw.write("  /** add code here to work with the headers and other envelope control segments */"
						+ lineFeed);
				pw.write("  Segment interchange_Control_Header = env.createInterchange_Header();" + lineFeed);
				pw.write("  interchange_Control_Header.useDefault();" + lineFeed);
				pw.write("  //  Grade of Service Request not required" + lineFeed);
				pw.write("  Segment grade_of_Service_Request = env.createGrade_of_Service_Request();" + lineFeed);
				pw.write("  grade_of_Service_Request.useDefault();" + lineFeed);
				pw.write("  //  Deferred Delivery Request not required" + lineFeed);
				pw.write("  Segment deferred_Delivery_Request = env.createDeferred_Delivery_Request();" + lineFeed);
				pw.write("  deferred_Delivery_Request.useDefault();" + lineFeed);

			} else if (ediType == 'e') {
				pw.write(
						"  EDIFACTEnvelope env = new EDIFACTEnvelope(EnvelopeFactory.buildEnvelope(\"EDIFACT.envelope\"));"
								+ lineFeed);
				pw.write(
						lineFeed + "  /** add code here to work with the headers and other envelope control segments */"
								+ lineFeed);
				pw.write("  Segment interchange_Control_Header = env.createInterchange_Header();" + lineFeed);
				pw.write("  interchange_Control_Header.useDefault();" + lineFeed);
			} else if (ediType == 't') {
				pw.write(
						"  TradacomsEnvelope env = new TradacomsEnvelope(EnvelopeFactory.buildEnvelope(\"Tradacoms.envelope\"));"
								+ lineFeed);
				pw.write(
						lineFeed + "  /** add code here to work with the headers and other envelope control segments */"
								+ lineFeed);
				pw.write("  Segment interchange_Control_Header = env.createInterchange_Header();" + lineFeed);
				pw.write("  interchange_Control_Header.useDefault();" + lineFeed);
			} else if (ediType == 'a') {
				pw.write("  ACHEnvelope env = new ACHEnvelope(EnvelopeFactory.buildEnvelope(\"ACH.envelope\"));"
						+ lineFeed);
				pw.write(
						lineFeed + "  /** add code here to work with the headers and other envelope control segments */"
								+ lineFeed);
				pw.write("  Segment interchange_Control_Header = env.createInterchange_Header();" + lineFeed);
				pw.write("  interchange_Control_Header.useDefault();" + lineFeed);
			}
			pw.write("  FunctionalGroup fg = env.createFunctionalGroup();" + lineFeed + lineFeed);
			if (ediType == 'x') {
				pw.write(
						lineFeed + "  /** add code here to work with the fg header and trailer segments */" + lineFeed);
				pw.write("  Segment fgHeader = fg.getHeader();" + lineFeed);
				pw.write("  fgHeader.useDefault();" + lineFeed);
				pw.write("  fg.addSegment(fgHeader);" + lineFeed + lineFeed);
				pw.write("  Segment fgTrailer = fg.getTrailer();" + lineFeed);
				pw.write("  fgTrailer.useDefault();" + lineFeed);
				pw.write("  fg.addSegment(fgTrailer);" + lineFeed + lineFeed);
			} else {

			}

			pw.write("  env.addFunctionalGroup(fg);" + lineFeed);
			pw.write("  TransactionSet ts = TransactionSetFactory.buildTransactionSet(\"" + tsID + "\");" + lineFeed);
			pw.write("  fg.addTransactionSet(ts);" + lineFeed + lineFeed);
		} else if (programType.equals("p")) {
			pw.write("public " + programname + "(String inFileName)" + lineFeed + "  throws OBOEException, IOException"
					+ lineFeed + "{" + lineFeed);
			pw.write("  File fileToParse = new File(inFileName);" + lineFeed);
			pw.write("  if (fileToParse.exists() == false)" + lineFeed);
			pw.write("     throw new OBOEException(\"File: \"+ inFileName + \" does not exist.\");" + lineFeed);
			if (ediType == 'x') {
				pw.write("  X12DocumentHandler dh = new X12DocumentHandler();" + lineFeed);
				pw.write("  dh.startParsing(new FileReader(fileToParse));" + lineFeed);
				pw.write("  X12Envelope env = (X12Envelope) dh.getEnvelope();" + lineFeed);
			} else if (ediType == 'e') {
				pw.write("  EDIFACTDocumentHandler dh = new EDIDocumentHandler();" + lineFeed);
				pw.write("  dh.startParsing(new FileReader(fileToParse));" + lineFeed);
				pw.write("  EDIFACTEnvelope env = (EDIFACTEnvelope) parsed.getEnvelope();" + lineFeed);
			} else if (ediType == 't') {
				pw.write("  TradacomsDocumentHandler dh = new EDIDocumentHandler();" + lineFeed);
				pw.write("  dh.startParsing(new FileReader(fileToParse));" + lineFeed);
				pw.write("  TradacomsEnvelope env = (TradacomsEnvelope) parsed.getEnvelope();" + lineFeed);
			}
		}

		pw.write(lineFeed);

		pw.write("  Table table;" + lineFeed);

		if (programType.equals("b")) {
			doBuildForJava(inTS);
		} else if (programType.equals("p")) {
			pw.write("  FunctionalGroup fg;" + lineFeed);
			pw.write("  TransactionSet ts;" + lineFeed);
			doParseForJava(inTS);
		}

		pw.println("}");

	}

	/**
	 * when parsing an OBOE object this code generates the code to work with
	 * Envelopes, Functional Groups and Transaction Sets
	 *
	 * @param inTS TransactionSet
	 */
	public void doParseForJava(TransactionSet inTS) {

		TemplateTransactionSet ts = (TemplateTransactionSet) inTS.getMyTemplate();

		pw.write("  int fgCount = env.getFunctionalGroupCount();" + lineFeed);
		pw.write("  int tsCount = -1;" + lineFeed);
		pw.write("  for (int fgPosition = 0; fgPosition < fgCount; fgPosition++)" + lineFeed);
		pw.write("     {" + lineFeed);
		pw.write("        fg = env.getFunctionalGroup(fgPosition);" + lineFeed);
		pw.write("        tsCount = fg.getTransactionSetCount();" + lineFeed);
		pw.write("        for (int tsPosition = 0; tsPosition < tsCount; tsPosition++)" + lineFeed);
		pw.write("           {" + lineFeed);
		pw.write("             ts = fg.getTransactionSet(tsPosition);" + lineFeed);

		TemplateTable table = ts.getHeaderTemplateTable();

		StringBuilder sb = new StringBuilder();

		if (table != null) {
			pw.write("             table = ts.getHeaderTable();" + lineFeed);
			pw.write("             if (table != null)" + lineFeed + "               {" + lineFeed);
			for (var elms : table.getContainer()) {
				if (elms.getContainerType() == ContainerType.Loop) {
					TemplateLoop loop = (TemplateLoop) elms;
					if (loop.isUsed()) {
						if (useName) {
							pw.write("                 extractLoop" + loop.getShortName() + "fromTableHeader(table);"
									+ lineFeed);
							sb.append(parseLoopCodeForJava(table, "TableHeader", loop, loop.getShortName()));
						} else {
							pw.write("                 extractLoop" + loop.getID() + "fromTableHeader(table);"
									+ lineFeed);
							sb.append(parseLoopCodeForJava(table, "TableHeader", loop, loop.getID()));
						}

					}

				} else if (elms.getContainerType() == ContainerType.Segment) {

					TemplateSegment segment = (TemplateSegment) elms;
					if (segment.isUsed()) {
						if (useName) {
							pw.write("                 extractSegment" + segment.getShortName() + "fromTableHeader(table);"
									+ lineFeed);
							sb.append(parseSegmentCodeForJava(table, "TableHeader", segment, segment.getShortName()));
						} else {
							pw.write("                 extractSegment" + segment.getID() + "fromTableHeader(table);"
									+ lineFeed);
							sb.append(parseSegmentCodeForJava(table, "TableHeader", segment, segment.getID()));
						}

					}

				}
			}

			pw.write(lineFeed + "              }" + lineFeed);
		}

		table = ts.getDetailTemplateTable();
		if (table != null) {
			pw.write("             table = ts.getDetailTable();" + lineFeed);
			pw.write("             if (table != null)" + lineFeed + "               {" + lineFeed);
			for (var elms : table.getContainer()) {
				if (elms.getContainerType() == ContainerType.Loop) {
					TemplateLoop loop = (TemplateLoop) elms;
					if (loop.isUsed()) {
						if (useName) {
							pw.write("                 extractLoop" + loop.getShortName() + "fromTableDetail(table);"
									+ lineFeed);
							sb.append(parseLoopCodeForJava(table, "TableDetail", loop, loop.getShortName()));
						} else {
							pw.write("                 extractLoop" + loop.getID() + "fromTableDetail(table);"
									+ lineFeed);
							sb.append(parseLoopCodeForJava(table, "TableDetail", loop, loop.getID()));
						}

					}

				} else if (elms.getContainerType() == ContainerType.Segment) {

					TemplateSegment segment = (TemplateSegment) elms;
					if (segment.isUsed()) {
						if (useName) {
							pw.write("                 extractSegment" + segment.getShortName() + "fromTableDetail(table);"
									+ lineFeed);
							sb.append(parseSegmentCodeForJava(table, "TableDetail", segment, segment.getShortName()));
						} else {
							pw.write("                 extractSegment" + segment.getID() + "fromTableDetail(table);"
									+ lineFeed);
							sb.append(parseSegmentCodeForJava(table, "TableDetail", segment, segment.getID()));
						}

					}

				}
			}
			pw.write(lineFeed + "              }" + lineFeed);
		}

		table = ts.getSummaryTemplateTable();
		if (table != null) {
			pw.write("             table = ts.getSummaryTable();" + lineFeed);
			pw.write("             if (table != null)" + lineFeed + "               {" + lineFeed);
			for (var elms : table.getContainer()) {
				if (elms.getContainerType() == ContainerType.Loop) {
					TemplateLoop loop = (TemplateLoop) elms;
					if (loop.isUsed()) {
						if (useName) {
							pw.write("                 extractLoop" + loop.getShortName() + "fromTableSummary(table);"
									+ lineFeed);
							sb.append(parseLoopCodeForJava(table, "TableSummary", loop, loop.getShortName()));
						} else {
							pw.write("                 extractLoop" + loop.getID() + "fromTableSummary(table);"
									+ lineFeed);
							sb.append(parseLoopCodeForJava(table, "TableSummary", loop, loop.getID()));
						}

					}

				} else if (elms.getContainerType() == ContainerType.Segment) {

					TemplateSegment segment = (TemplateSegment) elms;
					if (segment.isUsed()) {
						if (useName) {
							pw.write("                 extractSegment" + segment.getShortName()
									+ "fromTableSummary(table);" + lineFeed);
							sb.append(parseSegmentCodeForJava(table, "TableSummary", segment, segment.getShortName()));
						} else {
							pw.write("                 extractSegment" + segment.getID() + "fromTableSummary(table);"
									+ lineFeed);
							sb.append(parseSegmentCodeForJava(table, "TableSummary", segment, segment.getID()));
						}

					}

				}
			}
			pw.write(lineFeed + "              }" + lineFeed);
		}

		pw.write("           }" + lineFeed);
		pw.write("     }" + lineFeed);

		pw.write(lineFeed + "}" + lineFeed);

		pw.write(sb.toString() + lineFeed);

	}

	/**
	 * when building an OBOE object this code generates the code to work with
	 * Envelopes, Functional Groups and Transaction Sets
	 *
	 * @param inTS TransactionSet
	 */
	private void doBuildForJava(TransactionSet inTS) {

		TemplateTransactionSet ts = (TemplateTransactionSet) inTS.getMyTemplate();
		TemplateTable table = ts.getHeaderTemplateTable();

		StringBuilder sb = new StringBuilder();

		if (table != null) {
			pw.write("  table = ts.getHeaderTable();" + lineFeed);
			for (var elms : table.getContainer()) {

				if (elms.getContainerType() == ContainerType.Loop) {
					TemplateLoop loop = (TemplateLoop) elms;
					if (loop.isUsed()) {
						if (useName) {
							pw.write("		buildLoop" + loop.getShortName() + "forTableHeader(table);" + lineFeed);
						} else {
							pw.write("		buildLoop" + loop.getID() + "forTableHeader(table);" + lineFeed);
						}
						sb.append(buildLoopCodeForJava(table, "TableHeader", loop, "Header"));
					}

				} else if (elms.getContainerType() == ContainerType.Segment) {
					TemplateSegment segment = (TemplateSegment) elms;
					if (segment.isUsed()) {
						if (useName) {
							pw.write("		buildSegment" + segment.getShortName() + "forTableHeader(table);" + lineFeed);
						} else {
							pw.write("		buildSegment" + segment.getID() + "forTableHeader(table);" + lineFeed);
						}
						sb.append(buildSegmentCodeForJava(table, "TableHeader", segment, "Header"));
					}

				}
			}

		}

		table = ts.getDetailTemplateTable();
		if (table != null) {
			pw.write("  table = ts.getDetailTable();" + lineFeed);
			for (var elms : table.getContainer()) {

				if (elms.getContainerType() == ContainerType.Loop) {
					TemplateLoop loop = (TemplateLoop) elms;
					if (loop.isUsed()) {
						if (useName) {
							pw.write("		buildLoop" + loop.getShortName() + "forTableDetail(table);" + lineFeed);
						} else {
							pw.write("		buildLoop" + loop.getID() + "forTableDetail(table);" + lineFeed);
						}
						sb.append(buildLoopCodeForJava(table, "TableDetail", loop, "Detail"));
					}

				} else if (elms.getContainerType() == ContainerType.Segment) {
					TemplateSegment segment = (TemplateSegment) elms;
					if (segment.isUsed()) {
						if (useName) {
							pw.write("		buildSegment" + segment.getShortName() + "forTableDetail(table);" + lineFeed);
						} else {
							pw.write("		buildSegment" + segment.getID() + "forTableDetail(table);" + lineFeed);
						}
						sb.append(buildSegmentCodeForJava(table, "TableDetail", segment, "Detail"));
					}

				}
			}

		}

		table = ts.getSummaryTemplateTable();
		if (table != null) {
			pw.write("  table = ts.getSummaryTable();" + lineFeed);
			for (var elms : table.getContainer()) {

				if (elms.getContainerType() == ContainerType.Loop) {
					TemplateLoop loop = (TemplateLoop) elms;
					if (loop.isUsed()) {
						if (useName) {
							pw.write("		buildLoop" + loop.getShortName() + "forTableSummary(table);" + lineFeed);
						} else {
							pw.write("		buildLoop" + loop.getID() + "forTableSummary(table);" + lineFeed);
						}
						sb.append(buildLoopCodeForJava(table, "TableSummary", loop, "Summary"));
					}

				} else if (elms.getContainerType() == ContainerType.Segment) {
					TemplateSegment segment = (TemplateSegment) elms;
					if (segment.isUsed()) {
						if (useName) {
							pw.write("		buildSegment" + segment.getShortName() + "forTableSummary(table);" + lineFeed);
						} else {
							pw.write("		buildSegment" + segment.getID() + "forTableSummary(table);" + lineFeed);
						}
						sb.append(buildSegmentCodeForJava(table, "TableSummary", segment, "Summary"));
					}

				}
			}

		}
		pw.write(lineFeed);
		if (ediType == 'x') {
			pw.write("  Segment interchange_Control_Trailer = env.createInterchange_Trailer();" + lineFeed);
			pw.write("  interchange_Control_Trailer.useDefault();" + lineFeed);
		} else if (ediType == 'e') {
			pw.write("  Segment interchange_Control_Trailer = env.createInterchange_Trailer();" + lineFeed);
			pw.write("  interchange_Control_Trailer.useDefault();" + lineFeed);
		} else if (ediType == 't') {
			pw.write("  Segment interchange_Control_Trailer = env.createInterchange_Trailer();" + lineFeed);
			pw.write("  interchange_Control_Trailer.useDefault();" + lineFeed);
		} else if (ediType == 'a') {
			pw.write("  Segment interchange_Control_Trailer = env.createInterchange_Trailer();" + lineFeed);
			pw.write("  interchange_Control_Trailer.useDefault();" + lineFeed);
		}
		pw.write(lineFeed + " for (int i = 0; i < env.getFunctionalGroupCount(); i++)" + lineFeed);
		pw.write("    {" + lineFeed);
		pw.write("     env.getFunctionalGroup(i).setCountInTrailer();" + lineFeed);
		pw.write("     for (int j = 0; j < env.getFunctionalGroup(i).getTransactionSetCount(); j++) {" + lineFeed);
		pw.write("       env.getFunctionalGroup(i).getTransactionSet(j).trim();" + lineFeed);
		pw.write("       env.getFunctionalGroup(i).getTransactionSet(j).setTrailerFields(); }" + lineFeed);
		pw.write("    }" + lineFeed);

		pw.write(lineFeed + " env.setFGCountInTrailer();");

		pw.write(lineFeed + "}" + lineFeed);

		pw.write(sb.toString());

	}

	/**
	 * when building an OBOE object this code generates the segment logic
	 *
	 * @param parent     object either a Table or TemplateSegment
	 * @param name       parent name path
	 * @param loop       TemplateLoop currently working with
	 * @param methodName - part of method name to use
	 */
	private String parseLoopCodeForJava(Object parent, String name, TemplateLoop loop, String methodName) {
		StringWriter pw = new StringWriter();

		String indent = "";

		StringBuilder sb = new StringBuilder();
		pw.write("/** extract data from loop " + loop.getID() + " that is part of " + name + lineFeed);
		pw.write("*" + loop.getName() + " used " + lineFeed);

		if (parent instanceof TemplateTable) {
			pw.write("* @param inTable table containing this loop" + lineFeed);
		} else {
			pw.write("* @param inLoop loop containing this loop" + lineFeed);
		}

		pw.write("*@throws OBOEException - most likely loop not found" + lineFeed);
		pw.write("*/" + lineFeed);
		if (parent instanceof TemplateTable) {
			if (useName) {
				pw.write("public void extractLoop" + methodName + "from" + name + "(Table inTable)" + lineFeed
						+ "  throws OBOEException" + lineFeed + "{" + lineFeed);
			} else {
				pw.write("public void extractLoop" + methodName + "from" + name + "(Table inTable)" + lineFeed
						+ "  throws OBOEException" + lineFeed + "{" + lineFeed);
			}

			pw.write("  for (var loop : inTable.getAllContainers(ContainerType.Loop, \"" + loop.getID() + "\")) {"
					+ lineFeed);

		} else // templateloop
		{
			if (useName) {
				pw.write("public void extractLoop" + methodName + "from" + name + "(Loop inLoop)  throws OBOEException"
						+ lineFeed + "{" + lineFeed);
			} else {
				pw.write("public void extractLoop" + methodName + "from" + name + "(Loop inLoop)  throws OBOEException"
						+ lineFeed + "{" + lineFeed);
			}
			pw.write("  for (var loop : inLoop.getAllContainers(ContainerType.Loop, \"" + loop.getID() + "\")) {"
					+ lineFeed);
		}

		for (var elm : loop.getContainer()) {
			if (elm.getContainerType() == ContainerType.Loop) {
				TemplateLoop insideloop = (TemplateLoop) elm;
				if (!insideloop.isUsed()) {
					continue;
				}
				if (useName) {
					String pmName = this.whatMethodName(loop.getShortName(), insideloop.getShortName());

					pw.write(indent + "  extractLoop" + pmName + "fromLoop" + loop.getShortName() + "((Loop) loop);"
							+ lineFeed);
					sb.append(parseLoopCodeForJava(loop, "Loop" + loop.getShortName(), insideloop, pmName));
				} else {
					String pmName = this.whatMethodName(name, insideloop.getID());
					pw.write(indent + "  extractLoop" + pmName + "from" + name + "Loop" + loop.getID()
							+ "((Loop) loop);" + lineFeed);
					sb.append(parseLoopCodeForJava(loop, name + "Loop" + loop.getID(), insideloop, pmName));

				}
			} else if (elm.getContainerType() == ContainerType.Segment) {
				TemplateSegment insideSegment = (TemplateSegment) elm;
				if (!insideSegment.isUsed()) {
					continue;
				}
				if (useName) {
					String pmName = this.whatMethodName(loop.getShortName(), insideSegment.getShortName());

					pw.write(indent + "  extractSegment" + pmName + "fromLoop" + loop.getShortName() + "((Loop) loop);"
							+ lineFeed);
					sb.append(parseSegmentCodeForJava(loop, "Loop" + loop.getShortName(), insideSegment, pmName));
				} else {
					String pmName = this.whatMethodName(name, insideSegment.getID());
					pw.write(indent + "  extractSegment" + pmName + "from" + name + "Loop" + loop.getID()
							+ "((Loop) loop);" + lineFeed);
					sb.append(parseSegmentCodeForJava(loop, name + "Loop" + loop.getID(), insideSegment, pmName));
				}
			}

		}

		pw.write("    }" + lineFeed);

		pw.write("  }" + lineFeed);

		pw.write(sb.toString() + lineFeed);

		return pw.toString();
	}

	/**
	 * when building an OBOE object this code generates the segment logic
	 *
	 * @param parent     object either a Table or TemplateSegment
	 * @param name       parent name path
	 * @param seg        TemplateSegment currently working with
	 * @param methodName - part of method name to use
	 */
	private String parseSegmentCodeForJava(Object parent, String name, TemplateSegment seg, String methodName) {

		StringWriter pw = new StringWriter();
		String indent = "  ";

		pw.write("/** extract data from segment " + seg.getID() + " that is part of the " + name + lineFeed);
		pw.write("*" + seg.getName() + " used " + lineFeed);
		pw.write("*" + seg.getDescription() + lineFeed);
		if (parent instanceof TemplateTable) {
			pw.write("* @param inTable Table containing this segment" + lineFeed);
		} else {
			pw.write("* @param inLoop Loop containing this segment" + lineFeed);
		}

		pw.write("*@throws OBOEException - most likely segment not found" + lineFeed);
		pw.write("*/" + lineFeed);
		if (parent instanceof TemplateTable) {
			if (useName) {
				pw.write("public void extractSegment" + methodName + "from" + name + "(Table inTable)" + lineFeed
						+ "  throws OBOEException" + lineFeed + "{" + lineFeed);
			} else {
				pw.write("public void extractSegment" + methodName + "from" + name + "(Table inTable)" + lineFeed
						+ "  throws OBOEException" + lineFeed + "{" + lineFeed);
			}
			pw.write("  Segment segment");
			if ((seg.getOccurs() > 1) || (seg.getOccurs() == -1)) {
				pw.write(";" + lineFeed);
				pw.write("  int numberOfSegmentsInArrayList = inTable.getSegmentCount(\"" + seg.getID() + "\");"
						+ lineFeed);
				pw.write("  for (int i = 0; i <  numberOfSegmentsInArrayList; i++)" + lineFeed);
				pw.write("   {" + lineFeed);

				pw.write("     segment = inTable.getSegment(\"" + seg.getID() + "\", i);" + lineFeed);

				indent = "     ";
			} else {

				pw.write(" = inTable.getSegment(\"" + seg.getID() + "\");" + lineFeed);

			}
		} else // loop
		{
			if (useName) {
				pw.write("public void extractSegment" + methodName + "from" + name
						+ "(Loop inLoop)  throws OBOEException" + lineFeed + "{" + lineFeed);
			} else {
				pw.write("public void extractSegment" + methodName + "from" + name
						+ "(Loop inLoop)  throws OBOEException" + lineFeed + "{" + lineFeed);
			}
			pw.write("  Segment segment");
			if ((seg.getOccurs() > 1) || (seg.getOccurs() == -1)) {
				pw.write(";" + lineFeed);
				pw.write("  int numberOfSegmentsInArrayList = inLoop.getSegmentCount(\"" + seg.getID() + "\");"
						+ lineFeed);
				pw.write("  for (int i = 0; i <  numberOfSegmentsInArrayList; i++)" + lineFeed);
				pw.write("    {" + lineFeed);

				pw.write("       segment = inLoop.getSegment(\"" + seg.getID() + "\", i);" + lineFeed);

				indent = "       ";
			} else {

				pw.write(" = inLoop.getSegment(\"" + seg.getID() + "\");" + lineFeed);

			}
		}

		pw.write(indent + "if (segment == null)" + lineFeed + indent + "  return;" + lineFeed);

		pw.write(indent + "DataElement de;" + lineFeed);
		boolean compositeDone = false;
		int pos = -1;
		for (var elm : seg.myElementContainer.getAllTemplateElementsValues()) {
			pos++;
			if (elm.isUsed()) {
				if (elm.IAmATemplateComposite()) {
					if (compositeDone == false) {
						pw.write(indent + "  CompositeElement");
						compositeDone = true;
					}
					pw.write(parseCompositeCodeForJava((TemplateCompositeElement) elm, pos));
				} else if (elm.IAmATemplateDE()) {
					pw.write(indent + "de = (DataElement) segment.getElement(" + pos + ");");
					pw.write(indent + "// " + elm.getID() + " " + elm.getName() + lineFeed);
					pw.write(indent + "if (de != null)" + lineFeed);
					pw.write(indent + "  de.get();" + lineFeed);

				}

			}

		}

		if ((seg.getOccurs() > 1) || (seg.getOccurs() == -1)) {
			pw.write("    }" + lineFeed);
		}

		pw.write("  }" + lineFeed);

		return pw.toString();

	}

	/**
	 * when building an OBOE object this code generates the composite logic
	 *
	 * @param tce TemplateComposite currently working with
	 * @param inI position of Composite within parent object, used for output code
	 */
	private String parseCompositeCodeForJava(TemplateCompositeElement tce, int inI) {

		StringWriter pw = new StringWriter();
		pw.write("  composite = (CompositeElement) segment.getElement(" + (inI + 1) + ");");
		pw.write("  // " + tce.getID() + " " + tce.getName() + lineFeed);
		pw.write("  if (composite == null)" + lineFeed + "    return;" + lineFeed);
		int i;
		for (i = 0; i < tce.getContainerSize(); i++) {
			if (tce.isTemplateDE(i + 1) && tce.getTemplateElement(i + 1).isUsed()) {
				pw.write("  de = (DataElement) composite.getElement(" + (i + 1) + ");");
				pw.write("  // composite element " + tce.getTemplateElement(i + 1).getID() + " "
						+ tce.getTemplateElement(i + 1).getName() + lineFeed);
				pw.write("  if (de != null)" + lineFeed);
				pw.write("    de.get();" + lineFeed);
			}
		}
		return pw.toString();
	}

	/**
	 * when parsing an OBOE object this code generates the loop logic
	 *
	 * @param parent object either a Table or TemplateLoop
	 * @param name   parent name path
	 * @param loop   TemplateLoop currently working with
	 *
	 */
	private String buildLoopCodeForJava(Object parent, String name, TemplateLoop loop, String methodName) {

		StringWriter pw = new StringWriter();

		StringBuilder sb = new StringBuilder();

		pw.write("/** builds loop " + loop.getID() + " that is part of the " + name + lineFeed);
		pw.write("*" + loop.getName() + " used " + lineFeed);

		if (parent instanceof TemplateTable) {
			pw.write("* @param inTable table containing this segment" + lineFeed);
		} else {
			pw.write("* @param inLoop loop" + lineFeed);
		}
		pw.write("* @return loop object " + loop.getID() + lineFeed);
		pw.write("* @throws OBOEException - most likely segment not found" + lineFeed);
		pw.write("*/" + lineFeed);
		if (parent instanceof TemplateTable) {
			if (useName) {
				pw.write("public Loop buildLoop" + loop.getShortName() + "for" + name + "(Table inTable)" + lineFeed
						+ "  throws OBOEException" + lineFeed + "{" + lineFeed);
			} else {
				pw.write("public Loop buildLoop" + loop.getID() + "for" + name + "(Table inTable)" + lineFeed
						+ "  throws OBOEException" + lineFeed + "{" + lineFeed);
			}
			pw.write("  Loop loop = inTable.createAndAddLoop(\"" + loop.getID() + "\");" + lineFeed);

		} else {
			if (useName) {
				pw.write("public Loop buildLoop" + methodName + "for" + name + "(Loop inLoop)  throws OBOEException"
						+ lineFeed + "{" + lineFeed);
			} else {
				pw.write("public Loop buildLoop" + methodName + "for" + name + "(Loop inLoop)  throws OBOEException"
						+ lineFeed + "{" + lineFeed);
			}
			pw.write("  Loop loop = inLoop.createAndAddLoop(\"" + loop.getID() + "\");" + lineFeed);

		}

		for (var elms : loop.getContainer()) {
			if (elms.getContainerType() == ContainerType.Loop) {
				TemplateLoop insideloop = (TemplateLoop) elms;
				if (!insideloop.isUsed()) {
					continue;
				}
				if (useName) {
					String pmName = this.whatMethodName(loop.getShortName(), insideloop.getShortName());
					pw.write("  buildLoop" + pmName + "forLoop" + loop.getID() + "(loop);" + lineFeed);
					sb.append(buildLoopCodeForJava(loop, "Loop" + loop.getID(), insideloop, pmName));
				} else {
					String pmName = this.whatMethodName(name + loop.getID(), insideloop.getID());
					pw.write("  buildLoop" + pmName + "for" + name + loop.getID() + "(loop);" + lineFeed);
					sb.append(buildLoopCodeForJava(loop, name + loop.getID(), insideloop, pmName));
				}

			} else if (elms.getContainerType() == ContainerType.Segment) {
				TemplateSegment insidesegment = (TemplateSegment) elms;
				if (!insidesegment.isUsed()) {
					continue;
				}
				if (useName) {
					String pmName = this.whatMethodName(loop.getShortName(), insidesegment.getShortName());
					pw.write("  buildSegment" + pmName + "forLoop" + loop.getID() + "(loop);" + lineFeed);
					sb.append(buildSegmentCodeForJava(loop, "Loop" + loop.getID(), insidesegment, pmName));
				} else {
					String pmName = this.whatMethodName(name + loop.getID(), insidesegment.getID());
					pw.write("  buildSegment" + pmName + "for" + name + loop.getID() + "(loop);" + lineFeed);
					sb.append(buildSegmentCodeForJava(loop, name + loop.getID(), insidesegment, pmName));
				}
			}

		}

		pw.write("  return loop;" + lineFeed);

		pw.write("  }" + lineFeed);

		pw.write(sb.toString());
		return pw.toString();

	}

	/**
	 * when parsing an OBOE object this code generates the segment logic
	 *
	 * @param parent     object either a Table or TemplateSegment
	 * @param name       parent name path
	 * @param seg        TemplateSegment currently working with
	 * @param methodName - part of method name
	 */
	private String buildSegmentCodeForJava(Object parent, String name, TemplateSegment seg, String methodName) {

		StringWriter pw = new StringWriter();

		pw.write("/** builds segment " + seg.getID() + " that is part of the " + name + lineFeed);
		pw.write("*" + seg.getName() + " used " + lineFeed);
		pw.write("*" + seg.getDescription() + lineFeed);
		if (parent instanceof TemplateTable) {
			pw.write("* @param inTable table containing this segment" + lineFeed);
		} else {
			pw.write("* @param inLoop loop containing this segment" + lineFeed);
		}
		pw.write("* @return segment object " + seg.getID() + lineFeed);
		pw.write("* @throws OBOEException - most likely segment not found" + lineFeed);
		pw.write("*/" + lineFeed);
		if (parent instanceof TemplateTable) {
			if (useName) {
				pw.write("public Segment buildSegment" + seg.getShortName() + "for" + name + "(Table inTable)" + lineFeed
						+ "  throws OBOEException" + lineFeed + "{" + lineFeed);
			} else {
				pw.write("public Segment buildSegment" + seg.getID() + "for" + name + "(Table inTable)" + lineFeed
						+ "  throws OBOEException" + lineFeed + "{" + lineFeed);
			}
			if (seg.canYouPrevalidate()) {
				ArrayList<String> v = getIDList(seg);
				if (v.size() == 1) {
					pw.write("  Segment segment = inTable.createSegment(\"" + seg.getID() + "\", \"" + v.get(0) + "\");"
							+ lineFeed);
				} else if (v.size() < 5) {
					pw.write("  Segment segment = inTable.createSegment(\"" + seg.getID() + "\", StringReplace);"
							+ lineFeed);
					pw.write(
							"// remove the StringReplace argument or replace StringReplace with one of the following..."
									+ lineFeed);
					pw.write("//");
					for (int vi = 0; vi < v.size(); vi++) {
						pw.write("\"" + v.get(vi) + "\" ");
					}
					pw.write(lineFeed);
				} else {
					pw.write("  Segment segment = inTable.createSegment(\"" + seg.getID() + "\");" + lineFeed);
					pw.write("// but you may want to try createSegment(String, String);" + lineFeed);
				}
			} else {
				pw.write("  Segment segment = inTable.createSegment(\"" + seg.getID() + "\");" + lineFeed);
			}
			pw.write("  inTable.addSegment(segment);" + lineFeed);
		} else {
			if (useName) {
				pw.write("public Segment buildSegment" + methodName + "for" + name
						+ "(Loop inLoop)  throws OBOEException" + lineFeed + "{" + lineFeed);
			} else {
				pw.write("public Segment buildSegment" + methodName + "for" + name
						+ "(Loop inLoop)  throws OBOEException" + lineFeed + "{" + lineFeed);
			}
			if (seg.canYouPrevalidate()) {
				ArrayList<String> v = getIDList(seg);
				if (v.size() == 1) {
					pw.write("  Segment segment = inLoop.createSegment(\"" + seg.getID() + "\", \"" + v.get(0) + "\");"
							+ lineFeed);
				} else if (v.size() < 5) {
					pw.write("  Segment segment = inLoop.createSegment(\"" + seg.getID() + "\", StringReplace);"
							+ lineFeed);
					pw.write(
							"// remove the StringReplace argument or replace StringReplace with one of the following..."
									+ lineFeed);
					pw.write("//");
					for (int vi = 0; vi < v.size(); vi++) {
						pw.write("\"" + v.get(vi) + "\" ");
					}
					pw.write(lineFeed);
				} else {
					pw.write("  Segment segment = inLoop.createSegment(\"" + seg.getID() + "\");" + lineFeed);
					pw.write("// but you may want to try createSegment(String, String);" + lineFeed);
				}
			} else {
				pw.write("  Segment segment = inLoop.createSegment(\"" + seg.getID() + "\");" + lineFeed);
			}
			pw.write("  inLoop.addSegment(segment);" + lineFeed);
		}

		pw.write("  DataElement de;" + lineFeed);
		boolean compositeDone = false;
		var pos = -1;
		for (var elm : seg.myElementContainer.getAllTemplateElementsValues()) {
			pos++;
			if (elm.isUsed()) {
				if (elm.IAmATemplateComposite()) {
					if (compositeDone == false) {
						pw.write("  CompositeElement");
						compositeDone = true;
					}
					pw.write(buildCompositeCodeForJava((TemplateCompositeElement) elm, pos));
				} else {
					pw.write("  de = (DataElement) segment.buildElement(" + pos + ");");
					pw.write("  // " + elm.getID() + " " + elm.getName() + lineFeed);
					if ((ediType == 'x') && (seg.getID().equals("ST")) && (elm.getPosition() == 10)) {
						pw.write("  de.set(\"" + tsID + "\");" + lineFeed);
					} else if (elm.isRequired()) {
						pw.write("  //de.set(\"\");" + lineFeed);
					} else {
						pw.write("  //de.set(\"\");//not required" + lineFeed);
					}

				}
			}
		}

		pw.write("  segment.useDefault(); " + lineFeed);

		pw.write("  return segment;" + lineFeed);

		pw.write("  }" + lineFeed);

		return pw.toString();
	}

	/**
	 * when parsing an OBOE object that code generates the composite logic
	 *
	 * @param tce TemplateComposite currently working with
	 * @param inI position of Composite within parent object, used for output code
	 */

	private String buildCompositeCodeForJava(TemplateCompositeElement tce, int inI) {

		StringWriter pw = new StringWriter();

		pw.write("  composite = (CompositeElement) segment.buildElement(" + (inI + 1) + ");");
		pw.write("  // " + tce.getID() + " " + tce.getName() + lineFeed);
		var pos = -1;
		for (var elm : tce.myElementContainer.getAllTemplateElementsValues()) {
			pos++;
			if (elm.isUsed()) {
				pw.write("  de = (DataElement) composite.buildElement(" + pos + ");");
				pw.write("  // composite element " + elm.getID() + " " + elm.getName() + lineFeed);
				pw.write("  de.set(\"\");" + lineFeed);
			}
		}

		return pw.toString();
	}

	private ArrayList<String> getIDList(TemplateSegment seg) {
		for (int i = 0; i < seg.getContainerSize(); i++) {
			if (seg.isTemplateComposite(i + 1) && (seg.getTemplateElement(i + 1).isRequired())) {
				TemplateCompositeElement tce = (TemplateCompositeElement) seg.getTemplateElement(i + 1);
				TemplateDataElement tde = (TemplateDataElement) tce.getTemplateElement(1);
				if ((tde.isRequired()) && (tde.getType().equals("ID"))) {
					if (tde.getIDList() == null) {
						break;
					}

					return tde.getIDList().getCodes();
				}
				continue;
			}
			if (seg.isTemplateDE(i + 1) == false) {
				continue;
			}
			if (seg.getTemplateElement(i + 1).getRequired() != 'M') {
				continue;
			}
			if (seg.getTemplateElement(i + 1).getType().equals("ID")) {
				if (((TemplateDataElement) seg.getTemplateElement(i + 1)).getIDList() == null) {
					continue;
				}
				return ((TemplateDataElement) seg.getTemplateElement(i + 1)).getIDList().getCodes();
			}
		}
		return null;
	}

	public String whatMethodName(String parent, String mine) {
		String pm = parent + mine;
		Integer intgr = this.methodNameTable.get(pm);
		if (intgr == null) {
			this.methodNameTable.put(pm, 1);
			return mine;// +"_"+1;
		}

		int i = intgr.intValue() + 1;
		this.methodNameTable.remove(pm);
		this.methodNameTable.put(pm, i);
		return mine + "_" + i;

	}

	public String addDepth(int count) {
		String s = "";
		for (int i = 0; i < count; i++) {
			s += '\t';
		}
		return s;
	}

}
