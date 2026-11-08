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
import consulo.csharp.lang.impl.psi.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpParenthesesExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpPostfixExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpPrefixExpressionImpl;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

/**
 * A readonly field cannot be assigned to (except in a constructor or a variable initializer) - an assignment, ++ or --
 * of it out of a constructor of its type, or of another instance than this; a static one out of the static
 * constructor - CS0198
 *
 * @author VISTALL
 */
public class CS0191 extends CompilerCheck<DotNetExpression> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, DotNetExpression element) {
        CSharpFieldDeclaration field = readonlyFieldAssigned(element);
        return field != null && !field.hasModifier(CSharpModifier.STATIC) ? newBuilder(target(element)) : null;
    }

    /**
     * The readonly field the assignment, ++ or -- writes where it may not
     */
    @RequiredReadAction
    static @Nullable CSharpFieldDeclaration readonlyFieldAssigned(DotNetExpression element) {
        DotNetExpression target = target(element);
        while (target instanceof CSharpParenthesesExpressionImpl parentheses) {
            target = parentheses.getInnerExpression();
        }
        if (!(target instanceof CSharpReferenceExpression reference) || !(reference.resolve() instanceof CSharpFieldDeclaration field)
            || !field.hasModifier(CSharpModifier.READONLY) || field.isConstant()) {
            return null;
        }
        boolean isStatic = field.hasModifier(CSharpModifier.STATIC);
        // a constructor of the type of the field - not a lambda or a local function in it
        CSharpSimpleLikeMethodAsElement owner = PsiTreeUtil.getParentOfType(element, CSharpSimpleLikeMethodAsElement.class);
        boolean inConstructor = owner instanceof CSharpConstructorDeclaration constructor && !constructor.isDeConstructor()
            && constructor.hasModifier(CSharpModifier.STATIC) == isStatic && constructor.getParent() == field.getParent();
        DotNetExpression qualifier = reference.getQualifier();
        boolean onThis = qualifier == null || isStatic || qualifier instanceof CSharpReferenceExpression thisReference
            && thisReference.getQualifier() == null && PsiUtilCore.getElementType(thisReference.getReferenceElement()) == CSharpTokens.THIS_KEYWORD;
        return inConstructor && onThis ? null : field;
    }

    /**
     * The expression written - the left side of an assignment, the operand of ++ or --; null for another expression
     */
    @RequiredReadAction
    static @Nullable DotNetExpression target(DotNetExpression element) {
        if (element instanceof CSharpAssignmentExpressionImpl assignment) {
            return assignment.getLeftExpression();
        }
        if (element instanceof CSharpPostfixExpressionImpl postfix) {
            return postfix.getExpression();
        }
        if (element instanceof CSharpPrefixExpressionImpl prefix) {
            IElementType operator = prefix.getOperatorElement().getOperatorElementType();
            return operator == CSharpTokens.PLUSPLUS || operator == CSharpTokens.MINUSMINUS ? prefix.getExpression() : null;
        }
        return null;
    }
}
