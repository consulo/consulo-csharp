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
package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElements;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpSearchUtil;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetPropertyDeclaration;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * The type of the value a pattern is matched against - {@code var y} of {@code p is (1, var y)} is of the second element
 * of p, {@code var n} of {@code o is { Name: var n }} of the Name property
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CSharpPatternTypeUtil {
    /**
     * @param pattern a {@link CSharpPatternImpl} or a {@link CSharpCaseVariableImpl}
     */
    @RequiredReadAction
    public static DotNetTypeRef getInputTypeRef(PsiElement pattern) {
        PsiElement parent = pattern.getParent();
        if (parent instanceof CSharpPatternImpl parentPattern) {
            IElementType elementType = parentPattern.getNode().getElementType();
            if (elementType == CSharpElements.PROPERTY_SUBPATTERN) {
                return getSubpatternTypeRef(parentPattern);
            }
            if (elementType == CSharpElements.LIST_PATTERN) {
                return CSharpResolveUtil.resolveIterableType(getInputTypeRef(parentPattern));
            }
            if (elementType == CSharpElements.SLICE_PATTERN) {
                // .. var rest - a slice of the list
                PsiElement list = parentPattern.getParent();
                return list == null ? DotNetTypeRef.ERROR_TYPE : getInputTypeRef(list);
            }
            // not p, p and p, (p)
            return getInputTypeRef(parentPattern);
        }
        // the designation around a pattern - (1, var y) t
        if (parent instanceof CSharpCaseVariableImpl variable) {
            return getInputTypeRef(variable);
        }

        DotNetExpression value = null;
        if (parent instanceof CSharpIsExpressionImpl isExpression) {
            value = isExpression.getExpression();
        }
        else if (parent instanceof CSharpSwitchExpressionArmImpl arm && arm.getParent() instanceof CSharpSwitchExpressionImpl switchExpression) {
            value = switchExpression.getExpression();
        }
        // case var v: - the labels are in the block of the switch statement
        else if (parent instanceof CSharpCasePatternStatementImpl label
            && label.getParent() instanceof CSharpBlockStatementImpl block && block.getParent() instanceof CSharpSwitchStatementImpl switchStatement) {
            value = switchStatement.getExpression();
        }
        return value == null ? DotNetTypeRef.ERROR_TYPE : value.toTypeRef(true);
    }

    /**
     * @return the type a pattern with a type checks - {@code Point} of {@code Point(var x, var y)}, or the input type
     */
    @RequiredReadAction
    public static DotNetTypeRef getMatchedTypeRef(PsiElement pattern) {
        for (PsiElement child = pattern.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof DotNetType type) {
                return type.toTypeRef();
            }
        }
        return getInputTypeRef(pattern);
    }

    @RequiredReadAction
    private static DotNetTypeRef getSubpatternTypeRef(CSharpPatternImpl subpattern) {
        PsiElement owner = subpattern.getParent();
        if (owner == null) {
            return DotNetTypeRef.ERROR_TYPE;
        }

        // positional (a, b) or property { A: a } - the bracket before decides, the nested ones are inside the siblings
        boolean positional = false;
        int index = 0;
        for (PsiElement prev = subpattern.getPrevSibling(); prev != null; prev = prev.getPrevSibling()) {
            IElementType type = prev.getNode().getElementType();
            if (type == CSharpTokens.LPAR) {
                positional = true;
                break;
            }
            if (type == CSharpTokens.LBRACE) {
                break;
            }
            if (type == CSharpElements.PROPERTY_SUBPATTERN) {
                index++;
            }
        }

        DotNetTypeRef ownerTypeRef = getMatchedTypeRef(owner);
        if (positional) {
            int count = index;
            for (PsiElement next = subpattern.getNextSibling(); next != null; next = next.getNextSibling()) {
                IElementType type = next.getNode().getElementType();
                if (type == CSharpTokens.RPAR) {
                    break;
                }
                if (type == CSharpElements.PROPERTY_SUBPATTERN) {
                    count++;
                }
            }
            return CSharpDeconstructionUtil.getElementTypeRef(ownerTypeRef, index, count + 1);
        }

        // Name: p, Name.Inner: p
        DotNetTypeRef typeRef = ownerTypeRef;
        for (PsiElement child = subpattern.getFirstChild(); child != null; child = child.getNextSibling()) {
            IElementType type = child.getNode().getElementType();
            if (type == CSharpTokens.COLON) {
                return typeRef;
            }
            if (type == CSharpTokens.IDENTIFIER) {
                typeRef = getPropertyTypeRef(typeRef, child.getText());
            }
        }
        return DotNetTypeRef.ERROR_TYPE;
    }

    @RequiredReadAction
    private static DotNetTypeRef getPropertyTypeRef(DotNetTypeRef typeRef, String name) {
        if (typeRef == DotNetTypeRef.ERROR_TYPE) {
            return typeRef;
        }
        @Nullable DotNetPropertyDeclaration property = CSharpSearchUtil.findPropertyByName(name, null, typeRef);
        return property == null ? DotNetTypeRef.ERROR_TYPE : property.toTypeRef(true);
    }
}
