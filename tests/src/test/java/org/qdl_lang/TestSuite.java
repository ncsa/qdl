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
        // Without the VFS tests, all other tests (156 of them) take 1.532 s. (av. 9.82 ms per test)
        // Running this next test adds a full 3 seconds for the initial database connection
        // and unzipping.
        // Point is that this is quite fast since pretty much every test creates a parser and executes it.
        VFSTest.class

})
public class TestSuite extends junit.framework.TestSuite {
}
