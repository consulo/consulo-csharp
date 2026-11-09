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
package consulo.csharp.impl.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.impl.psi.stub.index.TypeByVmQNameIndex;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.assembly.AssemblyModuleService;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.PsiElement;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Missing partial modifier on declaration of type - another declaration of the type of the assembly is partial; and the
 * parts of a partial type the other checks of them look at (CS0261 - CS0265)
 *
 * @author VISTALL
 */
public class CS0260 extends CompilerCheck<CSharpTypeDeclaration> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpTypeDeclaration element) {
        String vmQName = element.getVmQName();
        if (element.hasModifier(CSharpModifier.PARTIAL) || vmQName == null) {
            return null;
        }
        AssemblyModuleService assemblyModuleService = AssemblyModuleService.getInstance(element.getProject());
        for (DotNetTypeDeclaration type : TypeByVmQNameIndex.getInstance().get(vmQName.hashCode(), element.getProject(), element.getResolveScope())) {
            if (type != element && type.hasModifier(CSharpModifier.PARTIAL) && vmQName.equals(type.getVmQName())
                && assemblyModuleService.resolve(type).equals(assemblyModuleService.resolve(element))) {
                return newBuilder(getNameIdentifier(element), formatElement(element));
            }
        }
        return null;
    }

    /**
     * The declarations of a partial type in the order of the sources, or null if the type is of one
     */
    @RequiredReadAction
    static @Nullable List<CSharpTypeDeclaration> parts(CSharpTypeDeclaration element) {
        if (!element.hasModifier(CSharpModifier.PARTIAL)) {
            return null;
        }
        CSharpCompositeTypeDeclaration compositeType = CSharpCompositeTypeDeclaration.findCompositeType(element);
        if (compositeType == null) {
            return null;
        }
        CSharpTypeDeclaration[] parts = compositeType.getTypeDeclarations().clone();
        Arrays.sort(parts, Comparator.comparing(CS0260::path).thenComparingInt(PsiElement::getTextOffset));
        return List.of(parts);
    }

    @RequiredReadAction
    private static String path(PsiElement element) {
        VirtualFile file = element.getContainingFile().getVirtualFile();
        return file == null ? "" : file.getPath();
    }

    /**
     * The first declaration - where a conflict of the parts is reported
     */
    @RequiredReadAction
    static boolean isFirst(CSharpTypeDeclaration element, @Nullable List<CSharpTypeDeclaration> parts) {
        return parts != null && Objects.equals(parts.get(0), element);
    }
}
