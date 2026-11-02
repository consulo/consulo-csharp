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

package consulo.csharp.lang.impl.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpConditionalExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpParenthesesExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpReturnStatementImpl;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;

/**
 * The type an expression converts to by its position - for the target-typed expressions of C# 9: {@code new()} and
 * {@code c ? a : b}.
 *
 * @author VISTALL
 */
public final class CSharpTargetTypeUtil {
    private CSharpTargetTypeUtil() {
    }

    /**
     * @return the declared type of the variable it initializes, of the left side of the assignment it is the right one of,
     * the return type of the method it is returned from, or {@link DotNetTypeRef#ERROR_TYPE} if the position has no type
     */
    @RequiredReadAction
    public static DotNetTypeRef targetTypeRef(DotNetExpression expression) {
        PsiElement element = expression;
        PsiElement parent = element.getParent();
        while (parent instanceof CSharpParenthesesExpressionImpl) {
            element = parent;
            parent = parent.getParent();
        }

        DotNetTypeRef typeRef = DotNetTypeRef.ERROR_TYPE;
        if (parent instanceof DotNetVariable variable && variable.getInitializer() == element) {
            DotNetType type = variable.getType();
            typeRef = type == null ? DotNetTypeRef.ERROR_TYPE : type.toTypeRef();
        }
        else if (parent instanceof CSharpAssignmentExpressionImpl assignment && assignment.getRightExpression() == element) {
            typeRef = assignment.getLeftExpression().toTypeRef(false);
        }
        else if (parent instanceof CSharpReturnStatementImpl) {
            PsiElement owner = PsiTreeUtil.getParentOfType(parent, DotNetLikeMethodDeclaration.class, CSharpLambdaExpressionImpl.class);
            if (owner instanceof DotNetLikeMethodDeclaration method) {
                typeRef = method.getReturnTypeRef();
            }
        }
        // Point p = c ? new() : new(1)
        else if (parent instanceof CSharpConditionalExpressionImpl conditional && conditional.getConditionExpression() != element) {
            typeRef = targetTypeRef(conditional);
        }
        return typeRef == DotNetTypeRef.AUTO_TYPE ? DotNetTypeRef.ERROR_TYPE : typeRef;
    }
}
