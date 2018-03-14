/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.test.semantics;

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
@TestMetadata("compiler/testData/codegen/boxInline/property")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class PropertyAccessorsInlineTestsGenerated extends AbstractPropertyAccessorsInlineTests {
    public void testAllFilesPresentInProperty() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("compiler/testData/codegen/boxInline/property"), Pattern.compile("^(.+)\\.kt$"), TargetBackend.JS, true);
    }

    @TestMetadata("augAssignmentAndInc.kt")
    public void testAugAssignmentAndInc() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/boxInline/property/augAssignmentAndInc.kt");
        doTest(fileName);
    }

    @TestMetadata("augAssignmentAndIncInClass.kt")
    public void testAugAssignmentAndIncInClass() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/boxInline/property/augAssignmentAndIncInClass.kt");
        doTest(fileName);
    }

    @TestMetadata("augAssignmentAndIncInClassViaConvention.kt")
    public void testAugAssignmentAndIncInClassViaConvention() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/boxInline/property/augAssignmentAndIncInClassViaConvention.kt");
        doTest(fileName);
    }

    @TestMetadata("augAssignmentAndIncOnExtension.kt")
    public void testAugAssignmentAndIncOnExtension() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/boxInline/property/augAssignmentAndIncOnExtension.kt");
        doTest(fileName);
    }

    @TestMetadata("augAssignmentAndIncOnExtensionInClass.kt")
    public void testAugAssignmentAndIncOnExtensionInClass() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/boxInline/property/augAssignmentAndIncOnExtensionInClass.kt");
        doTest(fileName);
    }

    @TestMetadata("augAssignmentAndIncViaConvention.kt")
    public void testAugAssignmentAndIncViaConvention() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/boxInline/property/augAssignmentAndIncViaConvention.kt");
        doTest(fileName);
    }

    @TestMetadata("property.kt")
    public void testProperty() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/boxInline/property/property.kt");
        doTest(fileName);
    }

    @TestMetadata("reifiedVal.kt")
    public void testReifiedVal() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/boxInline/property/reifiedVal.kt");
        doTest(fileName);
    }

    @TestMetadata("reifiedVar.kt")
    public void testReifiedVar() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/boxInline/property/reifiedVar.kt");
        doTest(fileName);
    }

    @TestMetadata("simple.kt")
    public void testSimple() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/boxInline/property/simple.kt");
        doTest(fileName);
    }

    @TestMetadata("simpleExtension.kt")
    public void testSimpleExtension() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("compiler/testData/codegen/boxInline/property/simpleExtension.kt");
        doTest(fileName);
    }
}
