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
import consulo.dotnet.psi.DotNetAttributeList;
import consulo.dotnet.psi.DotNetAttributeTargetType;
import consulo.language.psi.PsiElement;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * {@code [type: A] void M()} - the attribute location is not valid for the declaration, the attributes are ignored
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0657 extends CompilerCheck<DotNetAttributeList> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetAttributeList element) {
        PsiElement target = AttributeUsageUtil.getTargetElement(element);
        if (target == null || !isInvalidLocation(element)) {
            return null;
        }
        return newBuilder(target, target.getText());
    }

    /**
     * @return true for an explicit target not valid on the declaration - {@code [type: A] void M()}
     */
    @RequiredReadAction
    static boolean isInvalidLocation(DotNetAttributeList list) {
        DotNetAttributeTargetType targetType = list.getTargetType();
        PsiElement owner = AttributeUsageUtil.getOwner(list);
        if (targetType == null || owner == null) {
            return false;
        }
        Set<DotNetAttributeTargetType> valid = AttributeUsageUtil.getValidTargets(owner);
        return valid != null && !valid.contains(targetType);
    }
}
