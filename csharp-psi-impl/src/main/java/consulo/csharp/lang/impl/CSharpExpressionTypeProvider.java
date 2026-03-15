/*
 * Copyright 2013-2021 consulo.io
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
package consulo.csharp.lang.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.DotNetExpression;
import consulo.language.Language;
import consulo.language.editor.ExpressionTypeProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.xml.XmlStringUtil;

import java.util.List;

/**
 * @author VISTALL
 * @since 05/05/2021
 */
@ExtensionImpl
public class CSharpExpressionTypeProvider extends ExpressionTypeProvider<DotNetExpression> {
    @Override
    @RequiredReadAction
    public String getInformationHint(DotNetExpression expression) {
        return XmlStringUtil.escapeText(CSharpTypeRefPresentationUtil.buildText(expression.toTypeRef(true)));
    }

    @Override
    public String getErrorHint() {
        return "Expression not found";
    }

    @Override
    public List<DotNetExpression> getExpressionsAt(PsiElement psiElement) {
        DotNetExpression expression = PsiTreeUtil.getParentOfType(psiElement, DotNetExpression.class);
        if (expression instanceof CSharpReferenceExpression && expression.getParent() instanceof CSharpMethodCallExpressionImpl call) {
            expression = call;
        }

        if (expression != null) {
            return List.of(expression);
        }
        return List.of();
    }

    @Override
    public Language getLanguage() {
        return CSharpLanguage.INSTANCE;
    }
}
