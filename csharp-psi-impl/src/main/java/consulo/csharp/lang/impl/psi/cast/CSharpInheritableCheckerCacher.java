/*
 * Copyright 2013-2020 consulo.io
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

package consulo.csharp.lang.impl.psi.cast;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.impl.psi.CSharpInheritableChecker;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.disposer.Disposable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiModificationTrackerListener;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.util.lang.Pair;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author VISTALL
 * @since 2020-10-24
 */
@Singleton
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class CSharpInheritableCheckerCacher implements Disposable {
    public static CSharpInheritableCheckerCacher getInstance(@Nonnull Project project) {
        return project.getInstance(CSharpInheritableCheckerCacher.class);
    }

    private final Map<CSharpInheritKey, CSharpTypeUtil.InheritResult> myCache = new ConcurrentHashMap<>();
    private final Map<CSharpImpicitCastKey, CSharpTypeUtil.InheritResult> myImplicitCasts = new ConcurrentSkipListMap<>(Comparator.comparing(Object::toString));

    @Inject
    public CSharpInheritableCheckerCacher(Project project) {
        project.getMessageBus().connect(this).subscribe(PsiModificationTrackerListener.class, () -> {
            myCache.clear();
            myImplicitCasts.clear();
        });
    }

    @Nonnull
    @RequiredReadAction
    public CSharpTypeUtil.InheritResult getOrCheck(DotNetTypeRef top,
                                                   DotNetTypeRef target,
                                                   @Nullable Pair<CSharpCastType, GlobalSearchScope> castTypeResolver) {
        CSharpInheritKey key = new CSharpInheritKey(top, target, castTypeResolver);
        CSharpTypeUtil.InheritResult result = myCache.get(key);
        if (result != null) {
            return result;
        }

        try (CSharpCastSession session = CSharpCastSession.start(myImplicitCasts)) {
            result = CSharpInheritableChecker.isInheritable(key.top(), key.target(), key.castResolvingInfo());

            session.storeImplicit(myImplicitCasts);
        }

        // we can't use computeIfAbsent since we can call store data in recursive mode, which ConcurrentHashMap not allow
        myCache.putIfAbsent(key, result);
        return result;
    }

    @Override
    public void dispose() {
        myCache.clear();
    }
}
