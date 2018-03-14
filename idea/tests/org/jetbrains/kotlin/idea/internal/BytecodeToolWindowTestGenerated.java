/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.internal;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("idea/testData/internal/toolWindow")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class BytecodeToolWindowTestGenerated extends AbstractBytecodeToolWindowTest {
    public void testAllFilesPresentInToolWindow() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("idea/testData/internal/toolWindow"), Pattern.compile("^([^\\.]+)$"), TargetBackend.ANY, false);
    }

    @TestMetadata("componentInlineFun")
    public void testComponentInlineFun() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/internal/toolWindow/componentInlineFun/");
        doTest(fileName);
    }

    @TestMetadata("inlineFunctionBodyResolve")
    public void testInlineFunctionBodyResolve() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/internal/toolWindow/inlineFunctionBodyResolve/");
        doTest(fileName);
    }

    @TestMetadata("inlineFunctionDeep1")
    public void testInlineFunctionDeep1() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/internal/toolWindow/inlineFunctionDeep1/");
        doTest(fileName);
    }

    @TestMetadata("inlineFunctionDeep2")
    public void testInlineFunctionDeep2() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/internal/toolWindow/inlineFunctionDeep2/");
        doTest(fileName);
    }

    @TestMetadata("inlineFunctionInfixCall")
    public void testInlineFunctionInfixCall() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/internal/toolWindow/inlineFunctionInfixCall/");
        doTest(fileName);
    }

    @TestMetadata("inlineFunctionReifiedParam")
    public void testInlineFunctionReifiedParam() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/internal/toolWindow/inlineFunctionReifiedParam/");
        doTest(fileName);
    }

    @TestMetadata("iteratorFun")
    public void testIteratorFun() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/internal/toolWindow/iteratorFun/");
        doTest(fileName);
    }

    @TestMetadata("multipleInlineFunctionCalls")
    public void testMultipleInlineFunctionCalls() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/internal/toolWindow/multipleInlineFunctionCalls/");
        doTest(fileName);
    }

    @TestMetadata("objectInInlineFun")
    public void testObjectInInlineFun() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/internal/toolWindow/objectInInlineFun/");
        doTest(fileName);
    }
}
