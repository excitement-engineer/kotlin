/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.samWithReceiver;

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
@TestMetadata("plugins/sam-with-receiver/sam-with-receiver-cli/testData/diagnostics")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class SamWithReceiverTestGenerated extends AbstractSamWithReceiverTest {
    public void testAllFilesPresentInDiagnostics() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("plugins/sam-with-receiver/sam-with-receiver-cli/testData/diagnostics"), Pattern.compile("^(.+)\\.kt$"), TargetBackend.ANY, true);
    }

    @TestMetadata("noParameters.kt")
    public void testNoParameters() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/sam-with-receiver/sam-with-receiver-cli/testData/diagnostics/noParameters.kt");
        doTest(fileName);
    }

    @TestMetadata("noReturnType.kt")
    public void testNoReturnType() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/sam-with-receiver/sam-with-receiver-cli/testData/diagnostics/noReturnType.kt");
        doTest(fileName);
    }

    @TestMetadata("samConversionNoParameters.kt")
    public void testSamConversionNoParameters() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/sam-with-receiver/sam-with-receiver-cli/testData/diagnostics/samConversionNoParameters.kt");
        doTest(fileName);
    }

    @TestMetadata("samConversionSimple.kt")
    public void testSamConversionSimple() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/sam-with-receiver/sam-with-receiver-cli/testData/diagnostics/samConversionSimple.kt");
        doTest(fileName);
    }

    @TestMetadata("samConversionSimpleWithoutAnnotation.kt")
    public void testSamConversionSimpleWithoutAnnotation() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/sam-with-receiver/sam-with-receiver-cli/testData/diagnostics/samConversionSimpleWithoutAnnotation.kt");
        doTest(fileName);
    }

    @TestMetadata("samWithAnnotation.kt")
    public void testSamWithAnnotation() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/sam-with-receiver/sam-with-receiver-cli/testData/diagnostics/samWithAnnotation.kt");
        doTest(fileName);
    }

    @TestMetadata("samWithoutAnnotation.kt")
    public void testSamWithoutAnnotation() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/sam-with-receiver/sam-with-receiver-cli/testData/diagnostics/samWithoutAnnotation.kt");
        doTest(fileName);
    }

    @TestMetadata("singleParameter.kt")
    public void testSingleParameter() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/sam-with-receiver/sam-with-receiver-cli/testData/diagnostics/singleParameter.kt");
        doTest(fileName);
    }

    @TestMetadata("singleParameterWithoutAnnotation.kt")
    public void testSingleParameterWithoutAnnotation() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("plugins/sam-with-receiver/sam-with-receiver-cli/testData/diagnostics/singleParameterWithoutAnnotation.kt");
        doTest(fileName);
    }
}
