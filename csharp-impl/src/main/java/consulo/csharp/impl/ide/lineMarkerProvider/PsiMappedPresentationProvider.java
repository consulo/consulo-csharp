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
package consulo.csharp.impl.ide.lineMarkerProvider;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.Application;
import consulo.language.editor.ui.navigation.PsiTargetPresentationFactory;
import consulo.language.editor.ui.navigation.TargetPresentationProvider;
import consulo.language.psi.PsiElement;
import consulo.navigation.TargetPresentation;

import java.util.function.Function;

/**
 * What {@code PsiMappedElementListCellRender} used to draw, as data built once under a read lock - an
 * element shown as whatever the map turns it into, itself when the map has no answer.
 *
 * @author VISTALL
 * @since 2026-08-27
 */
public class PsiMappedPresentationProvider implements TargetPresentationProvider<PsiElement> {
    private final Function<PsiElement, PsiElement> myMap;

    public PsiMappedPresentationProvider(Function<PsiElement, PsiElement> map) {
        myMap = map;
    }

    @Override
    @RequiredReadAction
    public TargetPresentation getPresentation(PsiElement element) {
        PsiElement target = myMap.apply(element);
        if (target == null) {
            target = element;
        }

        return Application.get().getInstance(PsiTargetPresentationFactory.class).presentation(target);
    }
}
