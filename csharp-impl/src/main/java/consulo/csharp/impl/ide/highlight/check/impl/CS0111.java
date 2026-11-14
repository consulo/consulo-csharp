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
import consulo.csharp.lang.impl.psi.CSharpElementCompareUtil;
import consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Type already defines a member with the same parameter types - the later of two methods, constructors or indexers of
 * a name and the same parameters
 *
 * @author VISTALL
 */
public class CS0111 extends CompilerCheck<CSharpTypeDeclaration> {
    @RequiredReadAction
    @Override
    public List<? extends HighlightInfoFactory> check(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpTypeDeclaration element) {
        List<HighlightInfoFactory> results = new ArrayList<>();
        DotNetNamedElement[] members = element.getMembers();
        for (int j = 0; j < members.length; j++) {
            if (!isChecked(members[j])) {
                continue;
            }
            for (int i = 0; i < j; i++) {
                if (isChecked(members[i]) && sameName(members[i], members[j]) && CSharpElementCompareUtil.isEqualWithVirtualImpl(members[i], members[j])
                    && !isPartialPair(members[i], members[j])) {
                    results.add(newBuilder(range(members[j]), element.getName(), name(members[j])));
                    break;
                }
            }
        }
        return results;
    }

    /**
     * A method, a constructor or an indexer - not an operator, a conversion, a delegate
     */
    @RequiredReadAction
    private static boolean isChecked(PsiElement member) {
        if (member instanceof CSharpMethodDeclaration method) {
            return !method.isOperator() && !method.isDelegate();
        }
        return member instanceof DotNetLikeMethodDeclaration && !(member instanceof CSharpConversionMethodDeclaration);
    }

    /**
     * The defining and the implementing declarations of a partial method - not duplicates
     */
    @RequiredReadAction
    private static boolean isPartialPair(PsiElement first, PsiElement second) {
        return first instanceof CSharpMethodDeclaration a && second instanceof CSharpMethodDeclaration b && PartialMethodUtil.isPartial(a) && PartialMethodUtil.isPartial(b)
            && PartialMethodUtil.isImplementing(a) != PartialMethodUtil.isImplementing(b);
    }

    @RequiredReadAction
    private static boolean sameName(DotNetNamedElement first, DotNetNamedElement second) {
        return first.getClass() == second.getClass() && Objects.equals(name(first), name(second));
    }

    @RequiredReadAction
    private static String name(DotNetNamedElement member) {
        return member instanceof CSharpIndexMethodDeclaration ? "this" : member.getName();
    }

    @RequiredReadAction
    static PsiElement range(DotNetNamedElement member) {
        if (member instanceof CSharpIndexMethodDeclaration) {
            for (PsiElement child = member.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (PsiUtilCore.getElementType(child) == CSharpTokens.THIS_KEYWORD) {
                    return child;
                }
            }
        }
        return getNameIdentifier(member);
    }
}
