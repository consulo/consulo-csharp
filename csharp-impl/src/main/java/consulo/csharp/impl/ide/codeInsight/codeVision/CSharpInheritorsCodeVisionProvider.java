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

package consulo.csharp.impl.ide.codeInsight.codeVision;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.codeEditor.Editor;
import consulo.csharp.api.localize.CSharpLocalize;
import consulo.csharp.impl.ide.lineMarkerProvider.HidedOrOverridedElementCollector;
import consulo.csharp.impl.ide.lineMarkerProvider.LineMarkerCollector;
import consulo.csharp.impl.ide.lineMarkerProvider.OverrideTypeCollector;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.language.editor.codeVision.CodeVisionRelativeOrdering;
import consulo.language.editor.impl.codeVision.CodeVisionProviderBase;
import consulo.language.editor.impl.codeVision.InheritorsCodeVisionProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiModificationTracker;
import consulo.localize.LocalizeValue;
import org.jspecify.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-03-25
 */
@ExtensionImpl
public class CSharpInheritorsCodeVisionProvider extends InheritorsCodeVisionProvider {
    public static final String ID = "csharp.inheritors";

    @Override
    public boolean acceptsFile(PsiFile file) {
        return file.getLanguage() == CSharpLanguage.INSTANCE;
    }

    @Override
    public boolean acceptsElement(PsiElement element) {
        return element instanceof DotNetModifierListOwner;
    }

    @Override
    @RequiredReadAction
    public CodeVisionProviderBase.@Nullable CodeVisionInfo getVisionInfo(PsiElement element, PsiFile file) {
        if (element instanceof CSharpTypeDeclaration typeDeclaration) {
            int inheritors = computeClassInheritors(typeDeclaration);
            if (inheritors > 0) {
                boolean isInterface = typeDeclaration.isInterface();
                return new CodeVisionProviderBase.CodeVisionInfo(
                    (isInterface
                        ? CSharpLocalize.codeVisionImplementationsHint(inheritors)
                        : CSharpLocalize.codeVisionInheritorsHint(inheritors)).get(),
                    inheritors
                );
            }
        }
        else if (element instanceof DotNetVirtualImplementOwner method) {
            int overrides = computeMethodInheritors((CSharpSimpleLikeMethodAsElement) method);
            if (overrides > 0) {
                boolean isAbstract = ((DotNetModifierListOwner) method).hasModifier(DotNetModifier.ABSTRACT);
                return new CodeVisionProviderBase.CodeVisionInfo(
                    (isAbstract
                        ? CSharpLocalize.codeVisionImplementationsHint(overrides)
                        : CSharpLocalize.codeVisionOverridesHint(overrides)).get(),
                    overrides
                );
            }
        }
        return null;
    }

    @Override
    public @Nullable String getHint(PsiElement element, PsiFile file) {
        CodeVisionProviderBase.CodeVisionInfo info = getVisionInfo(element, file);
        return info != null ? info.text() : null;
    }

    private int computeMethodInheritors(CSharpSimpleLikeMethodAsElement method) {
        return CachedValuesManager.getManager(method.getProject()).getCachedValue(method, () ->
            CachedValueProvider.Result.create(
                CSharpTelescope.collectOverridingMethods(method),
                PsiModificationTracker.MODIFICATION_COUNT
            )
        );
    }

    private int computeClassInheritors(CSharpTypeDeclaration aClass) {
        return CachedValuesManager.getManager(aClass.getProject()).getCachedValue(aClass, () ->
            CachedValueProvider.Result.create(
                CSharpTelescope.collectInheritingClasses(aClass),
                PsiModificationTracker.MODIFICATION_COUNT
            )
        );
    }

    @Override
    public void handleClick(Editor editor, PsiElement element, @Nullable MouseEvent event) {
        LineMarkerCollector collector;
        if (element instanceof CSharpTypeDeclaration typeDeclaration) {
            collector = OverrideTypeCollector.INSTANCE;
        }
        else {
            collector = HidedOrOverridedElementCollector.INSTANCE;
        }

        collector.navigate(event, element);
    }

    @Override
    public List<CodeVisionRelativeOrdering> getRelativeOrderings() {
        return Collections.emptyList();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public LocalizeValue getName() {
        return CSharpLocalize.settingsInlayInheritors();
    }
}
