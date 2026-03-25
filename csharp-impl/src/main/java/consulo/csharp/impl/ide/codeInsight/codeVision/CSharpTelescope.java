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
import consulo.csharp.api.localize.CSharpLocalize;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideUtil;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.dotnet.psi.search.searches.TypeInheritorsSearch;
import consulo.language.psi.PsiFile;
import consulo.language.psi.search.ReferencesSearch;
import consulo.localize.LocalizeValue;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author VISTALL
 * @since 2026-03-25
 */
public class CSharpTelescope {
    static final int TOO_MANY_USAGES = -1;
    private static final int MAX_USAGES = 100;

    record UsagesHint(LocalizeValue hint, int count) {
    }

    static @Nullable UsagesHint usagesHint(DotNetModifierListOwner member, PsiFile psiFile) {
        int totalUsageCount = countUsages(member);
        if (totalUsageCount == TOO_MANY_USAGES) return null;
        if (totalUsageCount == 0) return null;
        return new UsagesHint(CSharpLocalize.usagesTelescope(totalUsageCount), totalUsageCount);
    }

    private static int countUsages(DotNetModifierListOwner member) {
        AtomicInteger count = new AtomicInteger();
        boolean ok = ReferencesSearch.search(member).forEach(ref -> {
            if (count.incrementAndGet() >= MAX_USAGES) return false;
            return true;
        });
        if (!ok && count.get() >= MAX_USAGES) return TOO_MANY_USAGES;
        return count.get();
    }

    @RequiredReadAction
    static int collectInheritingClasses(CSharpTypeDeclaration aClass) {
        if (aClass.hasModifier(CSharpModifier.SEALED)) {
            return 0;
        }
        if (DotNetTypes.System.Object.equals(aClass.getVmQName())) {
            return 0; // It's useless to have overridden markers for Object.
        }

        AtomicInteger count = new AtomicInteger();
        TypeInheritorsSearch.search(aClass, true).forEach(c -> {
            count.incrementAndGet();
            return true;
        });
        return count.get();
    }

    static int collectOverridingMethods(CSharpSimpleLikeMethodAsElement method) {
        return OverrideUtil.collectOverridenMembers((DotNetVirtualImplementOwner) method).size();
    }
}
