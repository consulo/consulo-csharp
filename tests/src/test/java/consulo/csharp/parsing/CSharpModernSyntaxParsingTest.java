/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.csharp.parsing;

import consulo.csharp.module.extension.CSharpLanguageVersion;
import org.junit.jupiter.api.Test;

/**
 * C# 7 - 14 syntax
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CSharpModernSyntaxParsingTest extends CSharpBaseParsingTest {
    public CSharpModernSyntaxParsingTest() throws Exception {
        super("parsing/modern");
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testAliasAnyType(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testAllowsRefStruct(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testAwaitForeachUsing(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testBinaryLiterals(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testCheckedOperator(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testCollectionExpression(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testCompoundAssignmentOperator(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testConditionalRef(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testConstraints(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testCovariantReturn(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testDeconstruction(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testDefaultInterfaceMethod(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testDefaultLiteral(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testDiscards(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testEscapeE(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testExtendedPropertyPattern(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testExtensionBlock(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testFieldKeyword(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testFieldTargetAttribute(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testFileLocalType(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testFileScopedNamespace(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testFunctionPointer(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testGenericAttribute(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testGlobalUsing(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testImplicitIndexInitializer(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testIndexRange(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testInitAccessor(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testInParameters(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testInterpolatedVerbatim(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testIsTypePattern(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testLambdaDefaultParameter(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testLambdaImprovements(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testListPattern(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testLocalFunctionAttributes(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testLocalFunction(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testMixedDeconstruction(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testNameofUnboundGeneric(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testNativeInts(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testNewlineInInterpolation(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testNonTrailingNamedArgs(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testNullableRef(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testNullCoalescingAssignment(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testNullConditionalAssignment(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testOutVar(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testParamsCollections(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testPartialCtorEvent(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testPartialProperty(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testPatternCombinators(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testPositionalPattern(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testPrimaryConstructor(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testPropertyPattern(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testRawInterpolated(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testRawStringLiteral(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testReadonlyMember(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testReadonlyRefStruct(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testRecord(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testRefLocalsReturns(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testRefReadonlyParameter(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testRelationalPattern(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testRequiredMembers(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testScopedRef(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testSealedRecordToString(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testSimpleLambdaModifiers(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testStackallocInit(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testStaticAbstractInterface(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testStaticLambda(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testStructParameterlessCtor(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testSwitchCasePattern(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testSwitchExpression(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testTargetTypedNew(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testThrowExpression(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testTupleLiteral(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testTupleType(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testUnsignedRightShift(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testUsingDeclaration(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testUtf8Literal(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testWithExpression(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testForeachDeconstruction(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testPatternDesignation(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testCasePatternLabel(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._14_0)
    @Test
    public void testInterpolationStringInHole(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._8_0)
    @Test
    public void testSoftModifiersBeforeCSharp11(Context context) throws Exception {
        doTest(context, null);
    }
}
