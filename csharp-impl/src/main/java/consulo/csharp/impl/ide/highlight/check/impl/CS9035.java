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
import consulo.csharp.lang.impl.psi.DotNetTypes2;
import consulo.csharp.lang.psi.CSharpCallArgumentList;
import consulo.csharp.lang.psi.CSharpFieldOrPropertySet;
import consulo.csharp.lang.psi.CSharpFieldOrPropertySetBlock;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpNewExpression;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetAttributeUtil;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;

import java.util.*;

/**
 * Required member must be set in the object initializer or attribute constructor - an object created without
 * setting a required field or property of its type or a base one, by a constructor without [SetsRequiredMembers];
 * at the type created, or at new for new()
 *
 * @author VISTALL
 */
public class CS9035 extends CompilerCheck<CSharpNewExpression> {
    @RequiredReadAction
    @Override
    public List<? extends HighlightInfoFactory> check(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpNewExpression element) {
        if (!(element.toTypeRef(false).resolve().getElement() instanceof DotNetTypeDeclaration type)) {
            return List.of();
        }
        List<DotNetVariable> required = requiredMembers(type);
        if (required.isEmpty()
            || element.resolveToCallable() instanceof PsiElement constructor
            && DotNetAttributeUtil.hasAttribute(constructor, DotNetTypes2.System.Diagnostics.CodeAnalysis.SetsRequiredMembersAttribute)) {
            return List.of();
        }

        Set<String> set = new HashSet<>();
        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            // the argument list is a set block too - of the named arguments of an attribute
            if (child instanceof CSharpFieldOrPropertySetBlock block && !(child instanceof CSharpCallArgumentList)) {
                for (CSharpFieldOrPropertySet fieldOrPropertySet : block.getSets()) {
                    set.add(fieldOrPropertySet.getName());
                }
            }
        }

        DotNetType newType = element.getNewType();
        PsiElement range = newType != null ? newType : element.getFirstChild();
        List<HighlightInfoFactory> results = new ArrayList<>();
        for (DotNetVariable member : required) {
            if (!set.contains(member.getName())) {
                results.add(newBuilder(range, formatElement(member)));
            }
        }
        return results;
    }

    /**
     * The required fields and properties of the type and its base types - an overriding one in place of the overridden
     */
    @RequiredReadAction
    private static List<DotNetVariable> requiredMembers(DotNetTypeDeclaration type) {
        List<List<DotNetVariable>> levels = new ArrayList<>();
        Set<String> names = new HashSet<>();
        Set<DotNetTypeDeclaration> visited = new HashSet<>();
        for (DotNetTypeDeclaration current = type; current != null && visited.add(current); current = baseClass(current)) {
            List<DotNetVariable> level = new ArrayList<>();
            for (PsiElement member : current.getMembers()) {
                if (member instanceof DotNetVariable variable && variable.hasModifier(CSharpModifier.REQUIRED) && names.add(variable.getName())) {
                    level.add(variable);
                }
            }
            levels.add(level);
        }
        // the base ones first, as Roslyn
        List<DotNetVariable> result = new ArrayList<>();
        for (int i = levels.size() - 1; i >= 0; i--) {
            result.addAll(levels.get(i));
        }
        return result;
    }

    @RequiredReadAction
    private static DotNetTypeDeclaration baseClass(DotNetTypeDeclaration type) {
        for (DotNetTypeRef typeRef : type.getExtendTypeRefs()) {
            if (typeRef.resolve().getElement() instanceof DotNetTypeDeclaration base && !base.isInterface()) {
                return base;
            }
        }
        return null;
    }
}
