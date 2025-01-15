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

package consulo.csharp;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpFileType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.test.junit.impl.extension.ConsuloProjectLoader;
import consulo.test.junit.impl.extension.NoParamDisplayNameGenerator;
import consulo.test.junit.impl.language.SimpleParsingTest;
import consulo.util.io.StreamUtil;
import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author VISTALL
 * @since 08.04.2016
 */
@ExtendWith(ConsuloProjectLoader.class)
@DisplayNameGeneration(NoParamDisplayNameGenerator.class)
public class VmQNameTest {
    private static final String ourTarget = "<target>";

    @RequiredReadAction
    @Test
    public void testNormalType(TestInfo testInfo, PsiFileFactory psiFileFactory) throws Exception {
        assertEqualVmQName(testInfo, psiFileFactory, "System.Media.Maybe", "Maybe");
    }

    @RequiredReadAction
    @Test
    public void testGenericType(TestInfo testInfo, PsiFileFactory psiFileFactory) throws Exception {
        assertEqualVmQName(testInfo, psiFileFactory, "System.Test`4", "Test`4");
    }

    @RequiredReadAction
    @Test
    public void testNestedGenericType(TestInfo testInfo, PsiFileFactory psiFileFactory) throws Exception {
        assertEqualVmQName(testInfo, psiFileFactory, "System.Test`4/AA`1", "AA`1");
    }

    @RequiredReadAction
    @Test
    private void assertEqualVmQName(@Nonnull TestInfo testInfo,
                                    @Nonnull PsiFileFactory psiFileFactory,
                                    @Nonnull String vmQName,
                                    @Nonnull String vmName) throws Exception {
        String testResName = NoParamDisplayNameGenerator.getName(testInfo.getTestMethod().get().getName(), false);

        InputStream stream = getClass().getResourceAsStream("/vmqname/" + testResName + CSharpFileType.DOT_EXTENSION);

        String text = StreamUtil.readText(stream, StandardCharsets.UTF_8);
        int index = text.indexOf(ourTarget);
        if (index == -1) {
            throw new IllegalArgumentException("no target set");
        }
        text = text.replace(ourTarget, "");

        PsiFile psiFile = psiFileFactory.createFileFromText("test.cs", CSharpFileType.INSTANCE, text);

        SimpleParsingTest.ensureParsed(psiFile);

        PsiElement elementAt = psiFile.findElementAt(index);
        DotNetTypeDeclaration declaration = PsiTreeUtil.getParentOfType(elementAt, DotNetTypeDeclaration.class);
        if (declaration == null) {
            throw new IllegalArgumentException("no type");
        }

        Assertions.assertEquals(vmQName, declaration.getVmQName(), "VmQName is not equal");
        Assertions.assertEquals(vmName, declaration.getVmName(), "VmName is not equal");
    }
}
