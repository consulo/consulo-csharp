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
import consulo.application.progress.ProgressManager;
import consulo.content.scope.SearchScope;
import consulo.csharp.impl.ide.codeInspection.UnusedTypeInspection;
import consulo.csharp.impl.ide.findUsage.referenceSearch.CSharpAttributeShortNameReferenceSearch;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.doc.psi.CSharpDocRoot;
import consulo.csharp.lang.impl.psi.CSharpFileUsages;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.*;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetAttributeUtil;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.editor.inspection.reference.RefUtil;
import consulo.language.editor.inspection.scheme.InspectionProjectProfileManager;
import consulo.language.editor.rawHighlight.HighlightDisplayKey;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.scope.LocalSearchScope;
import consulo.language.psi.search.PsiSearchHelper;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Type is never used - a type not of the public API (internal, or in an internal or private type) no code outside it
 * uses: no reference to it or a constructor of it. The references of its file are of {@link CSharpFileUsages}; then
 * the word index decides - no other file has the name: unused, ten or more: used - and only in between a search, to
 * the first use. Not a type the runtime or a framework creates: with Main or extension methods, a Unity object, a
 * test fixture, an implicitly used one. Of {@link UnusedTypeInspection} - off, nothing is searched
 *
 * @author VISTALL
 */
public class CC0009 extends CompilerCheck<CSharpTypeDeclaration> {
    private static final List<String> TEST_ATTRIBUTES = List.of(
        "NUnit.Framework.TestFixtureAttribute",
        "NUnit.Framework.TestAttribute",
        "NUnit.Framework.TestCaseAttribute",
        "Microsoft.VisualStudio.TestTools.UnitTesting.TestClassAttribute",
        "Microsoft.VisualStudio.TestTools.UnitTesting.TestMethodAttribute",
        "Xunit.FactAttribute",
        "Xunit.TheoryAttribute"
    );

    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpTypeDeclaration element) {
        PsiElement nameIdentifier = element.getNameIdentifier();
        if (nameIdentifier == null || !isEnabled(element) || !isChecked(element) || isUsed(element)) {
            return null;
        }
        return newBuilder(nameIdentifier, element.getName());
    }

    @RequiredReadAction
    private static boolean isEnabled(CSharpTypeDeclaration element) {
        HighlightDisplayKey key = HighlightDisplayKey.find(UnusedTypeInspection.ID);
        return key != null && InspectionProjectProfileManager.getInstance(element.getProject()).getCurrentProfile().isToolEnabled(key, element);
    }

    /**
     * Not of the public API, the first part of a partial one, not created by the runtime or a framework
     */
    @RequiredReadAction
    private static boolean isChecked(CSharpTypeDeclaration type) {
        if (!isHidden(type) || type.getName() == null) {
            return false;
        }
        List<CSharpTypeDeclaration> parts = CS0260.parts(type);
        if (parts != null && !CS0260.isFirst(type, parts)) {
            return false;
        }
        for (CSharpTypeDeclaration part : parts == null ? List.of(type) : parts) {
            if (isCreatedImplicitly(part)) {
                return false;
            }
        }
        return !type.isInheritor("UnityEngine.Object", true);
    }

    /**
     * Internal, private or private protected - itself or a type it is in
     */
    @RequiredReadAction
    private static boolean isHidden(CSharpTypeDeclaration type) {
        for (PsiElement element = type; element instanceof CSharpTypeDeclaration declaration; element = element.getParent()) {
            CSharpAccessModifier access = CSharpAccessModifier.findModifier(CSharpCompositeTypeDeclaration.selectCompositeOrSelfType(declaration));
            if (access == CSharpAccessModifier.INTERNAL || access == CSharpAccessModifier.PRIVATE || access == CSharpAccessModifier.PRIVATE_PROTECTED) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    private static boolean isCreatedImplicitly(CSharpTypeDeclaration part) {
        if (RefUtil.isImplicitUsage(part) || TEST_ATTRIBUTES.stream().anyMatch(attribute -> DotNetAttributeUtil.hasAttribute(part, attribute))) {
            return true;
        }
        for (DotNetNamedElement member : part.getMembers()) {
            if (member instanceof CSharpMethodDeclaration method) {
                // an entry point; extension methods are called without the type
                if (method.hasModifier(CSharpModifier.STATIC) && "Main".equals(method.getName()) || method.isExtension()) {
                    return true;
                }
                if (TEST_ATTRIBUTES.stream().anyMatch(attribute -> DotNetAttributeUtil.hasAttribute(method, attribute))) {
                    return true;
                }
            }
        }
        return false;
    }

    @RequiredReadAction
    private static boolean isUsed(CSharpTypeDeclaration type) {
        DotNetTypeDeclaration composite = CSharpCompositeTypeDeclaration.selectCompositeOrSelfType(type);
        List<PsiElement> parts = composite instanceof CSharpCompositeTypeDeclaration compositeType ? List.of(compositeType.getTypeDeclarations()) : List.of(type);

        // the references of the file - to the type or a constructor of it
        List<PsiElement> targets = new ArrayList<>();
        targets.add(type);
        targets.add(composite);
        for (PsiElement part : parts) {
            for (DotNetNamedElement member : ((CSharpTypeDeclaration) part).getMembers()) {
                if (member instanceof CSharpConstructorDeclaration) {
                    targets.add(member);
                }
            }
        }
        for (PsiElement target : targets) {
            for (PsiElement reference : CSharpFileUsages.getReferences(target)) {
                if (!isInside(reference, parts)) {
                    return true;
                }
            }
        }

        PsiFile file = type.getContainingFile();
        SearchScope useScope = type.getUseScope();
        if (useScope instanceof GlobalSearchScope globalScope) {
            PsiSearchHelper.SearchCostResult cost = cost(type, globalScope, file);
            if (cost == PsiSearchHelper.SearchCostResult.ZERO_OCCURRENCES) {
                return false;
            }
            if (cost == PsiSearchHelper.SearchCostResult.TOO_MANY_OCCURRENCES) {
                return true;
            }
        }
        else if (!(useScope instanceof LocalSearchScope)) {
            return true;
        }

        // to the first use outside the type and the file, not of a doc comment
        return ReferencesSearch.search(type, useScope).anyMatch(reference -> {
            PsiElement element = reference.getElement();
            return element.getContainingFile() != file && !isInside(element, parts) && PsiTreeUtil.getParentOfType(element, CSharpDocRoot.class) == null;
        });
    }

    /**
     * The files with the name - and with the short name of an attribute - other than the file of the type
     */
    @RequiredReadAction
    private static PsiSearchHelper.SearchCostResult cost(CSharpTypeDeclaration type, GlobalSearchScope scope, PsiFile file) {
        PsiSearchHelper helper = PsiSearchHelper.getInstance(type.getProject());
        PsiSearchHelper.SearchCostResult cost = helper.isCheapEnoughToSearch(type.getName(), scope, file, ProgressManager.getInstance().getProgressIndicator());
        String shortName = CSharpAttributeShortNameReferenceSearch.shortName(type);
        if (shortName == null || cost == PsiSearchHelper.SearchCostResult.TOO_MANY_OCCURRENCES) {
            return cost;
        }
        PsiSearchHelper.SearchCostResult shortCost = helper.isCheapEnoughToSearch(shortName, scope, file, ProgressManager.getInstance().getProgressIndicator());
        if (shortCost == PsiSearchHelper.SearchCostResult.ZERO_OCCURRENCES) {
            return cost;
        }
        return shortCost == PsiSearchHelper.SearchCostResult.TOO_MANY_OCCURRENCES ? shortCost : PsiSearchHelper.SearchCostResult.FEW_OCCURRENCES;
    }

    @RequiredReadAction
    private static boolean isInside(PsiElement element, List<PsiElement> parts) {
        for (PsiElement part : parts) {
            if (PsiTreeUtil.isAncestor(part, element, false)) {
                return true;
            }
        }
        return false;
    }
}
