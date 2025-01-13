/*
 * Copyright 2013-2017 consulo.io
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
import org.junit.jupiter.api.TestInfo;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpParsingTest extends CSharpBaseParsingTest {
    public CSharpParsingTest() throws Exception {
        super("parsing");
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testVarVar(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testUsingStatic(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._5_0)
    @Test
    public void testSoftKeywords(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._5_0)
    @Test
    public void testGenericParameters(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testNameOf(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue40(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue70(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue89(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue121(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue191(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue233(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue240(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue246(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue248(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue251(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue267(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue469(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue470(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._5_0)
    @Test
    public void testNameOfNotAllowed(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testNamespaceInsideClass(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testEnumParsing(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testRootAttribute(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testAssemblyAttributeBeforeAndAfterMember(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testWhereWhere(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue438(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue441(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue475(TestInfo testInfo) throws Exception {
        doTest(testInfo);
    }
}
