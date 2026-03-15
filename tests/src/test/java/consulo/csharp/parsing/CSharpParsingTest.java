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
    public void testVarVar(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testUsingStatic(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._5_0)
    @Test
    public void testSoftKeywords(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._5_0)
    @Test
    public void testGenericParameters(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testNameOf(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue40(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue70(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue89(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue121(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue191(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue233(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue240(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue246(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue248(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue251(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue267(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue469(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue470(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._5_0)
    @Test
    public void testNameOfNotAllowed(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testNamespaceInsideClass(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testEnumParsing(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testRootAttribute(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testAssemblyAttributeBeforeAndAfterMember(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testWhereWhere(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue438(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue441(Context context) throws Exception {
        doTest(context, null);
    }

    @SetLanguageVersion(version = CSharpLanguageVersion._6_0)
    @Test
    public void testIssue475(Context context) throws Exception {
        doTest(context, null);
    }
}
