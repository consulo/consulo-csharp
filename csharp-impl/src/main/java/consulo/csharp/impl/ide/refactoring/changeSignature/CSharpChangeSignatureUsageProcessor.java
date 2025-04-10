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

package consulo.csharp.impl.ide.refactoring.changeSignature;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.ReadActionProcessor;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.psi.*;
import consulo.document.util.TextRange;
import consulo.dotnet.psi.*;
import consulo.internal.dotnet.msil.decompiler.textBuilder.util.StubBlockUtil;
import consulo.language.editor.refactoring.ResolveSnapshotProvider;
import consulo.language.editor.refactoring.changeSignature.ChangeInfo;
import consulo.language.editor.refactoring.changeSignature.ChangeSignatureUsageProcessor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.usage.UsageInfo;
import consulo.util.collection.MultiMap;
import consulo.util.lang.StringUtil;
import consulo.util.lang.function.PairFunction;
import consulo.util.lang.ref.SimpleReference;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2014-06-12
 */
@ExtensionImpl
public class CSharpChangeSignatureUsageProcessor implements ChangeSignatureUsageProcessor {
    @Nonnull
    @Override
    public UsageInfo[] findUsages(@Nonnull final ChangeInfo info) {
        if (!(info instanceof CSharpChangeInfo)) {
            return UsageInfo.EMPTY_ARRAY;
        }
        final List<UsageInfo> list = new ArrayList<>();

        final ReadActionProcessor<PsiReference> refProcessor = new ReadActionProcessor<>() {
            @RequiredReadAction
            @Override
            public boolean processInReadAction(final PsiReference ref) {
                final PsiElement resolve = ref.resolve();
                if (resolve != info.getMethod()) {
                    return true;
                }
                TextRange rangeInElement = ref.getRangeInElement();
                list.add(new UsageInfo(ref.getElement(), rangeInElement.getStartOffset(), rangeInElement.getEndOffset(), false));
                return true;
            }
        };

        ReferencesSearch.search(new ReferencesSearch.SearchParameters(info.getMethod(), info.getMethod().getResolveScope(), false))
            .forEach(refProcessor);
        return list.toArray(UsageInfo.EMPTY_ARRAY);
    }

    @Nonnull
    @Override
    public MultiMap<PsiElement, String> findConflicts(@Nonnull ChangeInfo info, SimpleReference<UsageInfo[]> refUsages) {
        return MultiMap.empty();
    }

    @Override
    @RequiredWriteAction
    public boolean processUsage(
        @Nonnull ChangeInfo changeInfo,
        @Nonnull UsageInfo usageInfo,
        boolean beforeMethodChange,
        @Nonnull UsageInfo[] usages
    ) {
        if (!(changeInfo instanceof CSharpChangeInfo)) {
            return false;
        }
        PsiElement element = usageInfo.getElement();
        if (!(element instanceof DotNetReferenceExpression)) {
            return false;
        }

        if (!beforeMethodChange) {
            return true;
        }

        if (changeInfo.isNameChanged()) {
            ((DotNetReferenceExpression)element).handleElementRename(changeInfo.getNewName());
        }

        if (((CSharpChangeInfo)changeInfo).isParametersChanged()) {
            PsiElement parent = element.getParent();
            if (parent instanceof CSharpCallArgumentListOwner) {
                CSharpCallArgumentList parameterList = ((CSharpCallArgumentListOwner)parent).getParameterList();
                if (parameterList == null) {
                    return true;
                }

                CSharpParameterInfo[] newParameters = ((CSharpChangeInfo)changeInfo).getNewParameters();

                DotNetExpression[] expressions = parameterList.getExpressions();
                String[] newArguments = new String[newParameters.length];

                for (CSharpParameterInfo newParameter : newParameters) {
                    if (newParameter.getOldIndex() != -1) {
                        newArguments[newParameter.getNewIndex()] = expressions[newParameter.getOldIndex()].getText();
                    }
                    else {
                        newArguments[newParameter.getNewIndex()] = newParameter.getDefaultValue();
                    }
                }

                StringBuilder builder = new StringBuilder("test(");
                builder.append(StringUtil.join(newArguments, ", "));
                builder.append(");");

                DotNetStatement statement = CSharpFileFactory.createStatement(usageInfo.getProject(), builder);
                CSharpCallArgumentListOwner call = PsiTreeUtil.getChildOfType(statement, CSharpCallArgumentListOwner.class);
                parameterList.replace(call.getParameterList());
            }
            return true;
        }
        return false;
    }

    @Override
    @RequiredWriteAction
    public boolean processPrimaryMethod(@Nonnull ChangeInfo changeInfo) {
        if (!(changeInfo instanceof CSharpChangeInfo)) {
            return false;
        }
        CSharpChangeInfo sharpChangeInfo = (CSharpChangeInfo)changeInfo;

        DotNetLikeMethodDeclaration method = sharpChangeInfo.getMethod();

        if (sharpChangeInfo.isNameChanged()) {
            assert method instanceof CSharpMethodDeclaration;

            method.setName(sharpChangeInfo.getNewName());
        }

        StringBuilder builder = new StringBuilder();
        CSharpAccessModifier newVisibility = sharpChangeInfo.getNewVisibility();
        if (newVisibility != null) {
            builder.append(newVisibility.getPresentableText()).append(" ");
        }
        if (method instanceof CSharpMethodDeclaration) {
            if (changeInfo.isReturnTypeChanged()) {
                builder.append(((CSharpChangeInfo)changeInfo).getNewReturnType()).append(" ");
            }
            else {
                builder.append(CSharpTypeRefPresentationUtil.buildShortText(method.getReturnTypeRef())).append(" ");
            }
        }
        builder.append(method.getName());
        builder.append("(");

        StubBlockUtil.join(builder, sharpChangeInfo.getNewParameters(), new PairFunction<>() {
            @Nullable
            @Override
            public Void fun(StringBuilder stringBuilder, CSharpParameterInfo parameterInfo) {
                CSharpModifier modifier = parameterInfo.getModifier();
                if (modifier != null) {
                    stringBuilder.append(modifier.getPresentableText()).append(" ");
                }
                stringBuilder.append(parameterInfo.getTypeText());
                stringBuilder.append(" ");
                stringBuilder.append(parameterInfo.getName());
                return null;
            }
        }, ", ");

        builder.append(");");

        DotNetLikeMethodDeclaration newMethod = CSharpFileFactory.createMethod(method.getProject(), builder);

        if (sharpChangeInfo.isReturnTypeChanged()) {
            method.getReturnType().replace(newMethod.getReturnType());
        }

        if (newVisibility != null) {
            DotNetModifierList modifierList = method.getModifierList();
            assert modifierList != null;

            for (CSharpAccessModifier value : CSharpAccessModifier.VALUES) {
                for (CSharpModifier modifier : value.getModifiers()) {
                    modifierList.removeModifier(modifier);
                }
            }

            for (CSharpModifier modifier : newVisibility.getModifiers()) {
                modifierList.addModifier(modifier);
            }
        }

        if (sharpChangeInfo.isParametersChanged()) {
            CSharpParameterInfo[] newParameters = sharpChangeInfo.getNewParameters();

            for (final CSharpParameterInfo newParameter : newParameters) {
                DotNetParameter originalParameter = newParameter.getParameter();
                if (originalParameter != null) {
                    ReferencesSearch.SearchParameters searchParameters =
                        new ReferencesSearch.SearchParameters(originalParameter, originalParameter.getUseScope(), false);
                    ReferencesSearch.search(searchParameters).forEach(reference -> {
                        reference.handleElementRename(newParameter.getName());
                        return true;
                    });

                    originalParameter.setName(newParameter.getName());
                }
            }

            DotNetParameterList parameterList = method.getParameterList();
            if (parameterList != null) {
                parameterList.replace(newMethod.getParameterList());
            }
        }
        return true;
    }

    @Override
    public boolean shouldPreviewUsages(@Nonnull ChangeInfo changeInfo, @Nonnull UsageInfo[] usages) {
        return false;
    }

    @Override
    public void registerConflictResolvers(
        @Nonnull List<ResolveSnapshotProvider.ResolveSnapshot> snapshots,
        @Nonnull ResolveSnapshotProvider resolveSnapshotProvider,
        @Nonnull UsageInfo[] usages,
        @Nonnull ChangeInfo changeInfo
    ) {
    }
}
