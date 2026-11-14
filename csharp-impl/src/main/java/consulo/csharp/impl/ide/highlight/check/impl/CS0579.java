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
import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetAttributeList;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.csharp.lang.psi.CSharpModifierList;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * {@code [A, A]} - a duplicate attribute not allowing multiple
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0579 extends CompilerCheck<CSharpAttribute> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpAttribute element) {
        DotNetTypeDeclaration type = element.resolveToType();
        CSharpReferenceExpression reference = element.getReferenceExpression();
        if (type == null || reference == null || !(element.getParent() instanceof DotNetAttributeList list) || !(list.getParent() instanceof CSharpModifierList modifierList)) {
            return null;
        }
        if (CS0657.isInvalidLocation(list) || AttributeUsageUtil.getUsage(type).allowMultiple()) {
            return null;
        }
        int target = AttributeUsageUtil.getTarget(list);
        // an attribute of the same class before it, on the same target - [A, A], [A] [A]
        for (DotNetAttributeList otherList : modifierList.getAttributeLists()) {
            if (CS0657.isInvalidLocation(otherList) || AttributeUsageUtil.getTarget(otherList) != target) {
                continue;
            }
            for (DotNetAttribute other : otherList.getAttributes()) {
                if (other == element) {
                    return null;
                }
                DotNetTypeDeclaration otherType = other.resolveToType();
                if (otherType != null && Objects.equals(otherType.getVmQName(), type.getVmQName())) {
                    return newBuilder(reference, reference.getReferenceName());
                }
            }
        }
        return null;
    }
}
