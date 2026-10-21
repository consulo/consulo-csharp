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

package consulo.csharp.lang.impl.psi.source.resolve.sorter;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.RecursionManager;
import consulo.csharp.lang.impl.psi.CSharpContextUtil;
import consulo.csharp.lang.impl.psi.source.CSharpReferenceExpressionImplUtil;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.Comparing;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

/**
 * @author VISTALL
 * @since 28.02.2015
 */
public class StaticVsInstanceComparator implements Comparator<ResolveResult> {
    @RequiredReadAction
    public static StaticVsInstanceComparator create(PsiElement element) {
        CSharpReferenceExpressionEx parent = null;
        if (element instanceof CSharpReferenceExpression) {
            if (((CSharpReferenceExpression) element).getQualifier() == null && element.getParent() instanceof CSharpReferenceExpressionEx) {
                parent = (CSharpReferenceExpressionEx) element.getParent();
            }
        }
        return new StaticVsInstanceComparator(element, parent);
    }

    private final TypeLikeComparator myComparator;
    @Nullable
    private final CSharpReferenceExpressionEx myParent;

    private int myTypeArgumentsSize;

    @RequiredReadAction
    public StaticVsInstanceComparator(PsiElement element, @Nullable CSharpReferenceExpressionEx parent) {
        myComparator = TypeLikeComparator.create(element);
        myTypeArgumentsSize = CSharpReferenceExpressionImplUtil.getTypeArgumentListSize(element);
        myParent = parent;
    }

    @Override
    @RequiredReadAction
    public int compare(ResolveResult o1, ResolveResult o2) {
        if (isNameEqual(o1, o2)) {
            int shadowing = compareShadowing(o1.getElement(), o2.getElement());
            if (shadowing != 0) {
                return shadowing;
            }

            int i = Integer.compare(getWeightByContext(o2), getWeightByContext(o1));
            if (i == 0) {
                return getWeightByTypeArguments(o2) - getWeightByTypeArguments(o1);
            }
            return i;
        }
        return myComparator.compare(o1, o2);
    }

    /**
     * A variable and a type of the same name are the Color Color case only if the variable is of that type - otherwise the
     * variable, found in an inner scope, hides the type.
     */
    @RequiredReadAction
    private int compareShadowing(@Nullable PsiElement e1, @Nullable PsiElement e2) {
        if (myParent == null) {
            return 0;
        }
        if (e1 instanceof DotNetVariable variable && isTypeLike(e2) && !isColorColor(variable, e2)) {
            return -1;
        }
        if (e2 instanceof DotNetVariable variable && isTypeLike(e1) && !isColorColor(variable, e1)) {
            return 1;
        }
        return 0;
    }

    private static boolean isTypeLike(@Nullable PsiElement element) {
        return element instanceof CSharpTypeDeclaration || element instanceof CSharpTypeDefStatement;
    }

    @RequiredReadAction
    private boolean isColorColor(DotNetVariable variable, PsiElement type) {
        // inside its enum declaration an enum constant is of the underlying type
        if (variable instanceof CSharpEnumConstantDeclaration && PsiTreeUtil.isAncestor(variable.getParent(), myParent, false)) {
            return false;
        }
        DotNetTypeDeclaration variableType = resolveTargetElement(variable, myParent);
        DotNetTypeDeclaration typeType = resolveTargetElement(type, myParent);
        return variableType == null || typeType == null || Comparing.equal(variableType.getVmQName(), typeType.getVmQName());
    }

    private static boolean isNameEqual(ResolveResult o1, ResolveResult o2) {
        PsiElement e1 = o1.getElement();
        PsiElement e2 = o2.getElement();
        if (e1 instanceof PsiNamedElement && e2 instanceof PsiNamedElement) {
            return Comparing.equal(((PsiNamedElement) e1).getName(), ((PsiNamedElement) e2).getName());
        }
        return false;
    }

    @RequiredReadAction
    private int getWeightByTypeArguments(ResolveResult o1) {
        PsiElement element = o1.getElement();
        if (element instanceof DotNetGenericParameterListOwner) {
            int genericParametersCount = ((DotNetGenericParameterListOwner) element).getGenericParametersCount();
            int weight = genericParametersCount * 10;
            if (genericParametersCount == myTypeArgumentsSize) {
                weight += 1000;
            }
            return weight;
        }
        return 0;
    }

    @RequiredReadAction
    private int getWeightByContext(ResolveResult resolveResult) {
        final PsiElement element = resolveResult.getElement();
        if (element == null) {
            return -100;
        }

        if (myParent != null) {
            CSharpContextUtil.ContextType parentContext = CSharpContextUtil.ContextType.ANY;
            if (element instanceof CSharpTypeDeclaration || element instanceof CSharpTypeDefStatement) {
                parentContext = CSharpContextUtil.ContextType.STATIC;
            }
            else if (element instanceof DotNetVariable) {
                parentContext = CSharpContextUtil.ContextType.INSTANCE;
            }
            // with nothing to tell them apart the value goes first - Roslyn binds an erroneous member access to it
            int byDefault = parentContext == CSharpContextUtil.ContextType.INSTANCE ? 10 : 5;

            DotNetTypeDeclaration forceTarget = resolveTargetElement(element, myParent);
            if (forceTarget == null) {
                return byDefault;
            }

            ResolveResult[] resolveResults = RecursionManager.doPreventingRecursion(myParent, false, () -> myParent.tryResolveFromQualifier(forceTarget));
            if (resolveResults == null) {
                return byDefault;
            }

            // the member the parent binds to decides (C# spec 12.8.7.2, Color Color) - an instance one makes the name the value,
            // a static one the type
            for (ResolveResult result : resolveResults) {
                PsiElement member = result.getElement();
                if (member == null || !result.isValidResult()) {
                    continue;
                }

                CSharpContextUtil.ContextType contextForResolved = CSharpContextUtil.getContextForResolved(member);
                if (contextForResolved == CSharpContextUtil.ContextType.ANY || parentContext == CSharpContextUtil.ContextType.ANY) {
                    return byDefault;
                }
                boolean matches = parentContext == CSharpContextUtil.ContextType.INSTANCE ? contextForResolved.isAllowInstance() : contextForResolved == CSharpContextUtil.ContextType.STATIC;
                return matches ? 5000 : 0;
            }
            return byDefault;
        }

        // type alias have max priority
        if (element instanceof CSharpTypeDefStatement) {
            return Integer.MAX_VALUE;
        }
        // if expression is single - types and namespaces are in the end of queue
        if (element instanceof CSharpTypeDeclaration || element instanceof DotNetNamespaceAsElement) {
            return -1;
        }
        return 0;
    }

    @Nullable
    @RequiredReadAction
    private static DotNetTypeDeclaration resolveTargetElement(PsiElement element, PsiElement scope) {
        if (element instanceof CSharpTypeDeclaration) {
            return (DotNetTypeDeclaration) element;
        }
        else if (element instanceof CSharpTypeDefStatement typeDef) {
            PsiElement aliased = typeDef.toTypeRef().resolve().getElement();
            return aliased instanceof DotNetTypeDeclaration typeDeclaration ? typeDeclaration : null;
        }
        else if (element instanceof DotNetVariable) {
            DotNetTypeRef typeRef = ((DotNetVariable) element).toTypeRef(true);
            PsiElement resolvedElement = typeRef.resolve().getElement();
            if (resolvedElement == null) {
                return null;
            }
            return resolveTargetElement(resolvedElement, scope);
        }
        return null;
    }
}
