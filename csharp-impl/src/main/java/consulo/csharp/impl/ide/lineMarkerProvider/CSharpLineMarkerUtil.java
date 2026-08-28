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

package consulo.csharp.impl.ide.lineMarkerProvider;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.Application;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideUtil;
import consulo.csharp.lang.psi.CSharpIdentifier;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.language.ast.IElementType;
import consulo.language.editor.ui.navigation.PsiTargetNavigationService;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.event.ComponentEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 26.07.2015
 */
public class CSharpLineMarkerUtil {
    public static final Function<PsiElement, PsiElement> BY_PARENT = element -> element.getParent();

    /**
     * The targets are collected by the supplier inside a single read action off the ui thread, so a handler
     * hands over the search itself rather than a list it resolved on the event dispatch thread. A single
     * target is opened directly, an empty collection shows nothing.
     */
    @RequiredUIAccess
    public static void openTargets(ComponentEvent<?> event,
                                   Project project,
                                   String title,
                                   Function<PsiElement, PsiElement> map,
                                   Supplier<Collection<? extends PsiElement>> targets) {
        PsiMappedPresentationProvider provider = new PsiMappedPresentationProvider(map);

        Application.get().getInstance(PsiTargetNavigationService.class)
            .<PsiElement>newNavigator(() -> {
                List<PsiElement> elements = new ArrayList<>(targets.get());
                elements.sort(provider.comparator());
                return elements;
            })
            .presentationProvider(provider)
            .title(LocalizeValue.localizeTODO(title))
            .findUsagesTitle(LocalizeValue.localizeTODO(title))
            .navigate(event, project);
    }

    @Nullable
    public static DotNetVirtualImplementOwner findElementForLineMarker(PsiElement element) {
        PsiElement superParent = null;
        IElementType elementType = PsiUtilCore.getElementType(element);
        if (elementType == CSharpTokens.THIS_KEYWORD) {
            superParent = element.getParent();
        }
        else if (elementType == CSharpTokens.IDENTIFIER) {
            superParent = getParentIfIsIdentifier(element);
        }
        if (superParent == null) {
            return null;
        }

        return OverrideUtil.isAllowForOverride(superParent) ? (DotNetVirtualImplementOwner) superParent : null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T getNameIdentifierAs(@Nullable PsiElement element, Class<T> clazz) {
        if (element == null) {
            return null;
        }

        PsiElement parentIfIsIdentifier = getParentIfIsIdentifier(element);
        if (parentIfIsIdentifier != null) {
            return clazz.isAssignableFrom(parentIfIsIdentifier.getClass()) ? (T) parentIfIsIdentifier : null;
        }
        return null;
    }

    @Nullable
    public static PsiElement getParentIfIsIdentifier(PsiElement element) {
        IElementType elementType = PsiUtilCore.getElementType(element);
        if (elementType == CSharpTokens.IDENTIFIER && element.getParent() instanceof CSharpIdentifier) {
            return element.getParent().getParent();
        }
        return null;
    }
}
