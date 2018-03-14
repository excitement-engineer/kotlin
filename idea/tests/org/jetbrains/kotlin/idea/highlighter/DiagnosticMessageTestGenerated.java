/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.highlighter;

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
@TestMetadata("idea/testData/diagnosticMessage")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class DiagnosticMessageTestGenerated extends AbstractDiagnosticMessageTest {
    @TestMetadata("abstractBaseClassMemberNotImplemented.kt")
    public void testAbstractBaseClassMemberNotImplemented() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/abstractBaseClassMemberNotImplemented.kt");
        doTest(fileName);
    }

    public void testAllFilesPresentInDiagnosticMessage() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("idea/testData/diagnosticMessage"), Pattern.compile("^(.+)\\.kt$"), TargetBackend.ANY, false);
    }

    @TestMetadata("annotationsForResolve.kt")
    public void testAnnotationsForResolve() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/annotationsForResolve.kt");
        doTest(fileName);
    }

    @TestMetadata("assignedButNeverAccessedVariable.kt")
    public void testAssignedButNeverAccessedVariable() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/assignedButNeverAccessedVariable.kt");
        doTest(fileName);
    }

    @TestMetadata("cannotInferVisibility.kt")
    public void testCannotInferVisibility() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/cannotInferVisibility.kt");
        doTest(fileName);
    }

    @TestMetadata("cannotOverrideInvisibleMember.kt")
    public void testCannotOverrideInvisibleMember() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/cannotOverrideInvisibleMember.kt");
        doTest(fileName);
    }

    @TestMetadata("complexTypeMismatchWithTypeParameters.kt")
    public void testComplexTypeMismatchWithTypeParameters() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/complexTypeMismatchWithTypeParameters.kt");
        doTest(fileName);
    }

    @TestMetadata("complexTypeMismatchWithTypeParametersAndTypeAlias.kt")
    public void testComplexTypeMismatchWithTypeParametersAndTypeAlias() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/complexTypeMismatchWithTypeParametersAndTypeAlias.kt");
        doTest(fileName);
    }

    @TestMetadata("conflictingOverloadsClass.kt")
    public void testConflictingOverloadsClass() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/conflictingOverloadsClass.kt");
        doTest(fileName);
    }

    @TestMetadata("conflictingOverloadsDefaultPackage.kt")
    public void testConflictingOverloadsDefaultPackage() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/conflictingOverloadsDefaultPackage.kt");
        doTest(fileName);
    }

    @TestMetadata("conflictingSubstitutions.kt")
    public void testConflictingSubstitutions() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/conflictingSubstitutions.kt");
        doTest(fileName);
    }

    @TestMetadata("constructorsRedeclaration.kt")
    public void testConstructorsRedeclaration() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/constructorsRedeclaration.kt");
        doTest(fileName);
    }

    @TestMetadata("constructorsRedeclarationTopLevel.kt")
    public void testConstructorsRedeclarationTopLevel() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/constructorsRedeclarationTopLevel.kt");
        doTest(fileName);
    }

    @TestMetadata("differentNamesForSameParameter.kt")
    public void testDifferentNamesForSameParameter() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/differentNamesForSameParameter.kt");
        doTest(fileName);
    }

    @TestMetadata("expectedNothingDueToProjections.kt")
    public void testExpectedNothingDueToProjections() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/expectedNothingDueToProjections.kt");
        doTest(fileName);
    }

    @TestMetadata("extensionInClassReference.kt")
    public void testExtensionInClassReference() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/extensionInClassReference.kt");
        doTest(fileName);
    }

    @TestMetadata("functionPlaceholder.kt")
    public void testFunctionPlaceholder() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/functionPlaceholder.kt");
        doTest(fileName);
    }

    @TestMetadata("illegalSuspendCall.kt")
    public void testIllegalSuspendCall() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/illegalSuspendCall.kt");
        doTest(fileName);
    }

    @TestMetadata("invisibleMember.kt")
    public void testInvisibleMember() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/invisibleMember.kt");
        doTest(fileName);
    }

    @TestMetadata("multipleDefaultsFromSupertypes.kt")
    public void testMultipleDefaultsFromSupertypes() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/multipleDefaultsFromSupertypes.kt");
        doTest(fileName);
    }

    @TestMetadata("nameInConstraintIsNotATypeParameter.kt")
    public void testNameInConstraintIsNotATypeParameter() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/nameInConstraintIsNotATypeParameter.kt");
        doTest(fileName);
    }

    @TestMetadata("nestedClassAcessedViaInstanceReference.kt")
    public void testNestedClassAcessedViaInstanceReference() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/nestedClassAcessedViaInstanceReference.kt");
        doTest(fileName);
    }

    @TestMetadata("nestedClassShouldBeQualified.kt")
    public void testNestedClassShouldBeQualified() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/nestedClassShouldBeQualified.kt");
        doTest(fileName);
    }

    @TestMetadata("noneApplicable.kt")
    public void testNoneApplicable() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/noneApplicable.kt");
        doTest(fileName);
    }

    @TestMetadata("noneApplicableCallableReference.kt")
    public void testNoneApplicableCallableReference() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/noneApplicableCallableReference.kt");
        doTest(fileName);
    }

    @TestMetadata("noneApplicableConstructor.kt")
    public void testNoneApplicableConstructor() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/noneApplicableConstructor.kt");
        doTest(fileName);
    }

    @TestMetadata("noneApplicableGeneric.kt")
    public void testNoneApplicableGeneric() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/noneApplicableGeneric.kt");
        doTest(fileName);
    }

    @TestMetadata("noneApplicableHtml.kt")
    public void testNoneApplicableHtml() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/noneApplicableHtml.kt");
        doTest(fileName);
    }

    @TestMetadata("noneApplicableTxt.kt")
    public void testNoneApplicableTxt() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/noneApplicableTxt.kt");
        doTest(fileName);
    }

    @TestMetadata("numberValueTypes.kt")
    public void testNumberValueTypes() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/numberValueTypes.kt");
        doTest(fileName);
    }

    @TestMetadata("overloadResolutionAmbiguityHtml.kt")
    public void testOverloadResolutionAmbiguityHtml() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/overloadResolutionAmbiguityHtml.kt");
        doTest(fileName);
    }

    @TestMetadata("overloadResolutionAmbiguityTxt.kt")
    public void testOverloadResolutionAmbiguityTxt() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/overloadResolutionAmbiguityTxt.kt");
        doTest(fileName);
    }

    @TestMetadata("renderCollectionOfTypes.kt")
    public void testRenderCollectionOfTypes() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/renderCollectionOfTypes.kt");
        doTest(fileName);
    }

    @TestMetadata("returnTypeMismatchOnOverride.kt")
    public void testReturnTypeMismatchOnOverride() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/returnTypeMismatchOnOverride.kt");
        doTest(fileName);
    }

    @TestMetadata("typeInferenceCannotCaptureTypes.kt")
    public void testTypeInferenceCannotCaptureTypes() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/typeInferenceCannotCaptureTypes.kt");
        doTest(fileName);
    }

    @TestMetadata("typeInferenceExpectedTypeMismatch.kt")
    public void testTypeInferenceExpectedTypeMismatch() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/typeInferenceExpectedTypeMismatch.kt");
        doTest(fileName);
    }

    @TestMetadata("typeMismatchDueToProjections.kt")
    public void testTypeMismatchDueToProjections() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/typeMismatchDueToProjections.kt");
        doTest(fileName);
    }

    @TestMetadata("typeMismatchDueToProjectionsIn.kt")
    public void testTypeMismatchDueToProjectionsIn() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/typeMismatchDueToProjectionsIn.kt");
        doTest(fileName);
    }

    @TestMetadata("typeMismatchDueToProjectionsTxt.kt")
    public void testTypeMismatchDueToProjectionsTxt() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/typeMismatchDueToProjectionsTxt.kt");
        doTest(fileName);
    }

    @TestMetadata("typeMismatchWithFunctionalType.kt")
    public void testTypeMismatchWithFunctionalType() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/typeMismatchWithFunctionalType.kt");
        doTest(fileName);
    }

    @TestMetadata("typeMismatchWithNothing.kt")
    public void testTypeMismatchWithNothing() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/typeMismatchWithNothing.kt");
        doTest(fileName);
    }

    @TestMetadata("typeMismatchWithTypeParameters.kt")
    public void testTypeMismatchWithTypeParameters() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/typeMismatchWithTypeParameters.kt");
        doTest(fileName);
    }

    @TestMetadata("typeParameterAsReified.kt")
    public void testTypeParameterAsReified() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/typeParameterAsReified.kt");
        doTest(fileName);
    }

    @TestMetadata("typeVarianceConflictInTypeAliasExpansion.kt")
    public void testTypeVarianceConflictInTypeAliasExpansion() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/typeVarianceConflictInTypeAliasExpansion.kt");
        doTest(fileName);
    }

    @TestMetadata("unsupportedFeature.kt")
    public void testUnsupportedFeature() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/unsupportedFeature.kt");
        doTest(fileName);
    }

    @TestMetadata("unusedParameter.kt")
    public void testUnusedParameter() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/unusedParameter.kt");
        doTest(fileName);
    }

    @TestMetadata("unusedValue.kt")
    public void testUnusedValue() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/unusedValue.kt");
        doTest(fileName);
    }

    @TestMetadata("unusedVariable.kt")
    public void testUnusedVariable() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/unusedVariable.kt");
        doTest(fileName);
    }

    @TestMetadata("upperBoundViolated.kt")
    public void testUpperBoundViolated() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/upperBoundViolated.kt");
        doTest(fileName);
    }

    @TestMetadata("upperBoundViolatedInTypeAliasConstructorCall.kt")
    public void testUpperBoundViolatedInTypeAliasConstructorCall() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/upperBoundViolatedInTypeAliasConstructorCall.kt");
        doTest(fileName);
    }

    @TestMetadata("urlRender.kt")
    public void testUrlRender() throws Exception {
        String fileName = KotlinTestUtils.navigationMetadata("idea/testData/diagnosticMessage/urlRender.kt");
        doTest(fileName);
    }
}
