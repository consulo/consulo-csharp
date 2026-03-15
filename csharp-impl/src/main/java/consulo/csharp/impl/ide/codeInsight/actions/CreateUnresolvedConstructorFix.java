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

package consulo.csharp.impl.ide.codeInsight.actions;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.localize.CSharpErrorLocalize;
import consulo.csharp.lang.impl.psi.CSharpContextUtil;
import consulo.csharp.lang.psi.CSharpBodyWithBraces;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.DotNetMemberOwner;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.language.editor.template.Template;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public class CreateUnresolvedConstructorFix extends CreateUnresolvedLikeMethodFix {
    public CreateUnresolvedConstructorFix(CSharpReferenceExpression expression) {
        super(expression);
    }

    @Override
    public PsiElement getElementForAfterAdd(DotNetNamedElement[] elements, CSharpBodyWithBraces targetForGenerate) {
        PsiElement last = targetForGenerate.getLeftBrace();
        for (DotNetNamedElement element : elements) {
            if (element instanceof CSharpConstructorDeclaration) {
                last = element;
            }
        }
        return last;
    }

    @Override
    protected LocalizeValue createText(String referenceName, String arguments) {
        return CSharpErrorLocalize.createConstructor();
    }

    @Override
    @Nullable
    public CreateUnresolvedElementFixContext createGenerateContext() {
        CSharpReferenceExpression element = myPointer.getElement();
        if (element == null) {
            return null;
        }

        if (element.kind() == CSharpReferenceExpression.ResolveToKind.CONSTRUCTOR) {
            final DotNetQualifiedElement qualifiedElement = PsiTreeUtil.getParentOfType(element, DotNetQualifiedElement.class);
            if (qualifiedElement == null) {
                return null;
            }

            PsiElement parent = qualifiedElement.getParent();
            if (parent instanceof DotNetMemberOwner && parent.isWritable()) {
                return new CreateUnresolvedElementFixContext(element, (DotNetMemberOwner) parent);
            }
        }
        return null;
    }

    @RequiredReadAction
    @Override
    public void buildTemplate(CreateUnresolvedElementFixContext context, CSharpContextUtil.ContextType contextType, PsiFile file, Template template) {
        template.addTextSegment("public ");
        template.addTextSegment(myReferenceName);
        buildParameterList(context, file, template);
        template.addTextSegment("{\n");
        template.addEndVariable();
        template.addTextSegment("}");
    }
}
