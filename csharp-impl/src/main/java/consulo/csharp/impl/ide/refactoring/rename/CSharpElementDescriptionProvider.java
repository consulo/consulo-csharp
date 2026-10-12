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
package consulo.csharp.impl.ide.refactoring.rename;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.psi.CSharpNamedElement;
import consulo.language.psi.ElementDescriptionLocation;
import consulo.language.psi.ElementDescriptionProvider;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.usage.UsageViewShortNameLocation;

/**
 * @author VISTALL
 * @since 2017-11-14
 */
@ExtensionImpl
public class CSharpElementDescriptionProvider implements ElementDescriptionProvider {
    @Override
    @RequiredReadAction
    public LocalizeValue getElementDescription(PsiElement element, ElementDescriptionLocation location) {
        if (location == UsageViewShortNameLocation.INSTANCE && element instanceof CSharpNamedElement named) {
            return LocalizeValue.ofNullable(named.getNameWithAt());
        }
        return LocalizeValue.empty();
    }
}
