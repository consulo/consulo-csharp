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

package consulo.csharp.lang.impl.psi.source.resolve;

import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import jakarta.annotation.Nonnull;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class AsPsiElementProcessor implements Predicate<ResolveResult> {
    private Set<PsiElement> myElements = new LinkedHashSet<>();

    @Override
    public boolean test(ResolveResult resolveResult) {
        PsiElement element = resolveResult.getElement();
        myElements.add(element);
        return true;
    }

    @Nonnull
    public Set<PsiElement> getElements() {
        return myElements;
    }
}
