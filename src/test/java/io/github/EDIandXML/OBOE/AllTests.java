package io.github.EDIandXML.OBOE;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({ BadDocumentTestCase.class, BinDETestCase.class,
		CharDETestCase.class, CompositeElementTestCase.class,
		DataElementTestCase.class, DateDETestCase.class,
		DocumentHandlerTestCase.class, DocumentHandlerTestCaseIII.class,
		DocumentHandlerTestCaseIIIWithClasspath.class,
		DocumentHandlerTestCaseWithClasspath.class,
		DocumentRegisterHandlerTestCase.class, EnvelopeFactoryTestCase.class,
		EnvelopeTestCase.class, EnvelopeVersioning.class,
		EnvelopeVersioningWithClasspath.class, GetSetDEValueTestCase.class,
		IDDETestCase.class, IDListParserTestCase.class,
		MissingStuffTestCases.class, NumericDETestCase.class,
		OBOEValidateTestCase.class, ParserBugTestCase.class,
		RealDETestCase.class, SegmentFetchTestCase.class, SegmentTestCase.class,
		TestIDList103.class, TestLargeDocGenerator.class,
		TestMultipleElements.class, TestMultipleLikeSegmentsByPrimaryID.class,
		TestSegmentsByPrimaryID.class, TimeDETestCase.class, TrimTest.class,
		ValidCharSetTest.class })
public class AllTests {

}
