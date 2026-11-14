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
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpXAccessorOwner;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * {@code abstract void M();} of a class which is not abstract
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0513 extends CompilerCheck<DotNetModifierListOwner> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetModifierListOwner element) {
        // a method, or an accessor of an abstract property - the property itself is no member of the metadata
        DotNetModifierListOwner member = element instanceof DotNetXAccessor accessor && accessor.getParent() instanceof DotNetModifierListOwner owner ? owner : element;
        if (member instanceof CSharpXAccessorOwner && !(element instanceof DotNetXAccessor) || !(member instanceof DotNetLikeMethodDeclaration || member instanceof CSharpXAccessorOwner)) {
            return null;
        }
        if (!(member.getParent() instanceof DotNetTypeDeclaration type) || type.isInterface() || type.hasModifier(CSharpModifier.ABSTRACT)) {
            return null;
        }
        DotNetModifierList modifierList = member.getModifierList();
        if (modifierList == null || modifierList.getModifierElement(CSharpModifier.ABSTRACT) == null) {
            return null;
        }
        // private abstract is CS0621
        if (member.hasModifier(CSharpModifier.PRIVATE) && modifierList.getModifierElement(CSharpModifier.PROTECTED) == null) {
            return null;
        }
        PsiElement range = element instanceof DotNetXAccessor accessor ? getNameIdentifier(accessor) : getNameIdentifier(element);
        return newBuilder(range == null ? element : range, formatElement(element), formatElement(type));
    }
}
