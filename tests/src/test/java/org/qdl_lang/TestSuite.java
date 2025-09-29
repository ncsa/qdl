package org.qdl_lang;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Top-level test suite for this module.
 * <p>Created by Jeff Gaynor<br>
 * on 7/23/18 at  11:44 AM
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SystemTest.class,
        AnaphorTest.class,
        QDLModuleTest.class,
        QDLVariableTest.class,
        ModuleTests.class,
        TestMonadicOperations.class,
        TestDyadicOperations.class,
        SetTest.class,
        ExpressionTest.class,
        StatementTest.class,
        IOFunctionTest.class,
        StringFunctionTests.class,
        MathFunctionsTest.class,
        StemTest.class,
        ParserTest.class,
        OldModuleTests.class,
        GlomTest.class,
        SerializationTest.class,
        CryptoTest.class,
        // Without the VFS tests, all other tests (569 of them, often with multiple parts, 1/1/2025) takes 6.559 s.
        // (av. 11.6 ms per test)
        // Running this next test adds up to several seconds for the initial database latency,
        // Point is that QDL is actually quite fast since pretty much every test creates a parser and executes it.
        VFSTest.class

})
public class TestSuite extends junit.framework.TestSuite {
}
