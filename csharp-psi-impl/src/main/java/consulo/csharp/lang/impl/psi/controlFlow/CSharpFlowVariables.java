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

package consulo.csharp.lang.impl.psi.controlFlow;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.csharp.lang.impl.psi.source.*;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * The variables a flow graph reads and writes: locals, parameters, lambda parameters, pattern and out variables of a
 * member body - not fields, nor locals of a struct type other than a built-in one (their fields are assigned one by one,
 * C# spec 9.4.1, not modelled)
 *
 * @author VISTALL
 */
public final class CSharpFlowVariables {
    private CSharpFlowVariables() {
    }

    /**
     * The flow owner of the member the element is in - the outermost one
     */
    @RequiredReadAction
    public static @Nullable CSharpSimpleLikeMethodAsElement topOwner(PsiElement element) {
        CSharpSimpleLikeMethodAsElement top = null;
        for (CSharpSimpleLikeMethodAsElement owner = PsiTreeUtil.getParentOfType(element, CSharpSimpleLikeMethodAsElement.class, false); owner != null;
             owner = PsiTreeUtil.getParentOfType(owner, CSharpSimpleLikeMethodAsElement.class, true)) {
            top = owner;
        }
        return top;
    }

    /**
     * The flow owner a variable belongs to - of the body or the parameter list it is declared in
     */
    @RequiredReadAction
    public static @Nullable CSharpSimpleLikeMethodAsElement ownerOf(PsiElement variable) {
        return PsiTreeUtil.getParentOfType(variable, CSharpSimpleLikeMethodAsElement.class, true);
    }

    /**
     * The names of the variables and local functions of the member - a reference by another name is none of them,
     * and is not resolved by the builder
     */
    @RequiredReadAction
    public static Set<String> names(CSharpSimpleLikeMethodAsElement top) {
        return LanguageCachedValueUtil.getCachedValue(top, () -> CachedValueProvider.Result.create(collectNames(top), PsiModificationTracker.MODIFICATION_COUNT));
    }

    @RequiredReadAction
    private static Set<String> collectNames(PsiElement top) {
        Set<String> names = new HashSet<>();
        for (PsiNamedElement element : PsiTreeUtil.findChildrenOfAnyType(top, false, DotNetVariable.class, CSharpLocalMethodDeclarationImpl.class)) {
            if (element instanceof CSharpLocalMethodDeclarationImpl || isTracked(element)) {
                String name = element.getName();
                if (name != null) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    @RequiredReadAction
    public static boolean isTracked(@Nullable PsiElement element) {
        if (element instanceof CSharpLocalVariable local) {
            // foreach, catch, using variables are assigned; only a declaration without an initializer can be unassigned
            boolean declaration = element.getParent() instanceof CSharpLocalVariableDeclarationStatement;
            return !declaration || local.getInitializer() != null || !isStruct(local);
        }
        if (element instanceof DotNetParameter parameter) {
            return element.isPhysical() && element.getParent() instanceof DotNetParameterList
                && (!parameter.hasModifier(CSharpModifier.OUT) || !isStruct(parameter));
        }
        return element instanceof CSharpLambdaParameter
            || element instanceof CSharpIsVariableImpl
            || element instanceof CSharpCaseVariableImpl
            || element instanceof CSharpOutRefVariableImpl
            || element instanceof CSharpTupleVariable;
    }

    public static boolean isOut(DotNetVariable variable) {
        return variable instanceof DotNetParameter parameter && parameter.hasModifier(CSharpModifier.OUT);
    }

    /**
     * Of a struct type, not a built-in, an enum or a nullable one
     */
    @RequiredReadAction
    private static boolean isStruct(DotNetVariable variable) {
        DotNetType type = variable.getType();
        if (type == null || type instanceof CSharpNativeType) {
            return false;
        }
        if (!(variable.toTypeRef(false).resolve().getElement() instanceof DotNetTypeDeclaration declaration)) {
            return false;
        }
        return declaration.isStruct() && !declaration.isEnum() && !DotNetTypes.System.Nullable$1.equals(declaration.getVmQName());
    }
}
