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
import consulo.application.util.CachedValueProvider;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.CSharpFileUsages;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.impl.psi.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpNameOfExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpOutRefWrapExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpParenthesesExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpTupleExpressionImpl;
import consulo.csharp.lang.psi.CSharpAccessModifier;
import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpNamedFieldOrPropertySet;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetAttributeUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.PsiReference;
import consulo.language.psi.scope.LocalSearchScope;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The field is assigned but its value is never used - a private field never read, all its assignments (the
 * initializer included) constants, as CS0219 for a local
 *
 * @author VISTALL
 */
public class CS0414 extends CompilerCheck<CSharpFieldDeclaration> {
    /**
     * How the references of a field use it - a read, a write, all the values written constants
     */
    record Usage(boolean referenced, boolean read, boolean written, boolean constantWrites) {
    }

    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpFieldDeclaration element) {
        Usage usage = usage(element);
        if (usage == null || usage.read() || !usage.written() || !usage.constantWrites()) {
            return null;
        }
        return newBuilder(element.getNameIdentifier(), formatElement(element));
    }

    /**
     * The usage of a private field of a type without [StructLayout] - null for another field; once for CS0414 and CS0649
     */
    @RequiredReadAction
    static @Nullable Usage usage(CSharpFieldDeclaration field) {
        return LanguageCachedValueUtil.getCachedValue(field, () -> CachedValueProvider.Result.create(computeUsage(field), PsiModificationTracker.MODIFICATION_COUNT));
    }

    @RequiredReadAction
    private static @Nullable Usage computeUsage(CSharpFieldDeclaration field) {
        // private protected is not private - it is used by the derived types
        if (CSharpAccessModifier.findModifier(field) != CSharpAccessModifier.PRIVATE || field.isConstant() || field.getNameIdentifier() == null
            || !(field.getParent() instanceof CSharpTypeDeclaration type)
            || DotNetAttributeUtil.hasAttribute(type, "System.Runtime.InteropServices.StructLayoutAttribute")) {
            return null;
        }
        DotNetExpression initializer = field.getInitializer();
        boolean read = false;
        boolean referenced = false;
        boolean written = initializer != null;
        boolean constantWrites = initializer == null || CS0219.isConstantValue(field, initializer);
        for (PsiElement element : references(field, type)) {
            referenced = true;
            if (PsiTreeUtil.getParentOfType(element, CSharpNameOfExpressionImpl.class) != null) {
                read = true;
                continue;
            }
            DotNetExpression value = assignedValue(element);
            if (value != null) {
                written = true;
                constantWrites &= CS0219.isConstantValue(field, value);
            }
            else {
                // a read; x += 1, x++, ref x, out x read and write it
                read = true;
                written |= isWrite(element);
                constantWrites = false;
            }
        }
        return new Usage(referenced, read, written, constantWrites);
    }

    /**
     * The references to the field - of its file, collected by the highlighting; searched if the type has parts in other
     * files
     */
    @RequiredReadAction
    private static List<PsiElement> references(CSharpFieldDeclaration field, CSharpTypeDeclaration type) {
        LocalSearchScope scope = CSharpCompositeTypeDeclaration.createLocalScope(type);
        for (PsiElement part : scope.getScope()) {
            if (part.getContainingFile() != field.getContainingFile()) {
                List<PsiElement> references = new ArrayList<>();
                for (PsiReference reference : ReferencesSearch.search(field, scope)) {
                    references.add(reference.getElement());
                }
                return references;
            }
        }
        return CSharpFileUsages.getReferences(field);
    }

    /**
     * The value of f = value, new C { f = value } with the reference as the target - null for any other use
     */
    @RequiredReadAction
    private static @Nullable DotNetExpression assignedValue(PsiElement reference) {
        PsiElement target = reference;
        while (target.getParent() instanceof CSharpParenthesesExpressionImpl parentheses) {
            target = parentheses;
        }
        PsiElement parent = target.getParent();
        if (parent instanceof CSharpAssignmentExpressionImpl assignment && assignment.getLeftExpression() == target
            && assignment.getOperatorElement().getOperatorElementType() == CSharpTokens.EQ) {
            return assignment.getRightExpression();
        }
        if (parent instanceof CSharpNamedFieldOrPropertySet set && set.getNameElement() == target) {
            return set.getValueExpression();
        }
        return null;
    }

    /**
     * Written by a compound assignment, ++, --, ref or out, or as a target of a tuple
     */
    @RequiredReadAction
    private static boolean isWrite(PsiElement reference) {
        PsiElement parent = reference.getParent();
        return parent instanceof CSharpAssignmentExpressionImpl assignment && assignment.getLeftExpression() == reference
            || parent instanceof DotNetExpression && CS0191.target((DotNetExpression) parent) == reference
            || parent instanceof CSharpOutRefWrapExpressionImpl
            || PsiTreeUtil.getParentOfType(reference, CSharpTupleExpressionImpl.class) != null;
    }

    /**
     * [SerializeField], [SerializeReference] of Unity - its serializer sets the field
     */
    @RequiredReadAction
    static boolean isSerialized(CSharpFieldDeclaration field) {
        DotNetModifierList modifierList = field.getModifierList();
        if (modifierList == null) {
            return false;
        }
        for (DotNetAttribute attribute : modifierList.getAttributes()) {
            if (attribute instanceof CSharpAttribute csharpAttribute && csharpAttribute.getReferenceExpression() != null) {
                String name = csharpAttribute.getReferenceExpression().getReferenceName();
                if ("SerializeField".equals(name) || "SerializeReference".equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }
}
