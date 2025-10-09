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

package consulo.csharp.impl.ide.codeInspection.unnecessaryType;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.impl.ide.codeInsight.actions.ChangeVariableToTypeRefFix;
import consulo.csharp.impl.ide.codeInspection.CSharpGeneralLocalInspection;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.source.CSharpCatchStatementImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpDynamicTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpNullTypeRef;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpModuleUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.intention.IntentionWrapper;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElementVisitor;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 18.05.14
 */
@ExtensionImpl
public class UnnecessaryTypeInspection extends CSharpGeneralLocalInspection {
    @Nonnull
    @Override
    public PsiElementVisitor buildVisitor(@Nonnull final ProblemsHolder holder, boolean isOnTheFly) {
        CSharpLanguageVersion languageVersion = CSharpModuleUtil.findLanguageVersion(holder.getFile());
        if (!languageVersion.isAtLeast(CSharpLanguageVersion._3_0)) {
            return CSharpElementVisitor.EMPTY;
        }

        return new CSharpElementVisitor() {
            @Override
            @RequiredReadAction
            public void visitLocalVariable(CSharpLocalVariable variable) {
                if (variable.isConstant() || variable.getParent() instanceof CSharpCatchStatementImpl) {
                    return;
                }

                DotNetExpression initializer = variable.getInitializer();
                if (initializer != null) {
                    DotNetTypeRef typeRef = initializer.toTypeRef(false);
                    if (typeRef instanceof CSharpLambdaTypeRef || typeRef instanceof CSharpNullTypeRef) {
                        return;
                    }
                }
                else {
                    return;
                }

                DotNetTypeRef typeRef = variable.toTypeRef(false);
                if (typeRef == DotNetTypeRef.AUTO_TYPE) {
                    return;
                }
                else if (typeRef instanceof CSharpDynamicTypeRef) {
                    return;
                }

                DotNetType type = variable.getType();
                if (type == null) {
                    return;
                }

                holder.registerProblem(type, "Can replaced by 'var'", ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                    new IntentionWrapper(new ChangeVariableToTypeRefFix(variable, DotNetTypeRef.AUTO_TYPE), variable.getContainingFile()));
            }
        };
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("Unnecessary type");
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WEAK_WARNING;
    }
}
