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
import consulo.dotnet.psi.DotNetAttributeList;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import org.jspecify.annotations.Nullable;

/**
 * {@code [OnlyMethod] class C} - the attribute is not valid on the declaration by its AttributeUsage
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0592 extends CompilerCheck<CSharpAttribute> {
    private static final String ATTRIBUTE = "System.Attribute";

    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpAttribute element) {
        DotNetTypeDeclaration type = element.resolveToType();
        CSharpReferenceExpression reference = element.getReferenceExpression();
        if (type == null || reference == null || !(element.getParent() instanceof DotNetAttributeList list) || !type.isInheritor(ATTRIBUTE, true)) {
            return null;
        }
        // an invalid location is CS0657 - the attributes are ignored
        if (CS0657.isInvalidLocation(list)) {
            return null;
        }
        int target = AttributeUsageUtil.getTarget(list);
        if (target == 0 || (AttributeUsageUtil.getUsage(type).validOn() & target) != 0) {
            return null;
        }
        return newBuilder(reference, reference.getReferenceName());
    }
}
