/*
 * Copyright 2013-2023 consulo.io
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

import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.util.lang.Pair;
import org.jspecify.annotations.Nullable;

import java.io.Closeable;
import java.util.*;

/**
 * @author VISTALL
 * @since 2023-12-31
 */
public class CSharpCastSession implements Closeable {
    private static final ThreadLocal<CSharpCastSession> ourSession = ThreadLocal.withInitial(CSharpCastSession::new);

    public static CSharpCastSession start(Map<CSharpImpicitCastKey, CSharpTypeUtil.InheritResult> implicitCast) {
        CSharpCastSession session = get();
        session.myServiceImplicitMap = Collections.unmodifiableMap(implicitCast);
        return session;
    }

    public static CSharpCastSession get() {
        return Objects.requireNonNull(ourSession.get());
    }

    private Set<CSharpInheritKey> myVisited = new LinkedHashSet<>();

    private Map<CSharpImpicitCastKey, CSharpTypeUtil.InheritResult> myServiceImplicitMap = Map.of();

    private Map<CSharpImpicitCastKey, CSharpTypeUtil.InheritResult> myNewImplicitMap = new HashMap<>();

    public boolean mark(DotNetTypeRef top, DotNetTypeRef target, @Nullable Pair<CSharpCastType, GlobalSearchScope> castResolvingInfo) {
        CSharpInheritKey key = new CSharpInheritKey(top, target, castResolvingInfo);
        return myVisited.add(key);
    }

    public CSharpTypeUtil.InheritResult recordImpicit(CSharpImpicitCastKey key, CSharpTypeUtil.InheritResult result) {
        myNewImplicitMap.put(key, result);
        return result;
    }

    public CSharpTypeUtil.InheritResult getImplicitResult(CSharpImpicitCastKey key) {
        CSharpTypeUtil.InheritResult result = myServiceImplicitMap.get(key);
        if (result != null) {
            return result;
        }

        return myNewImplicitMap.get(key);
    }

    public void storeImplicit(Map<CSharpImpicitCastKey, CSharpTypeUtil.InheritResult> serviceImplicitMap) {
        serviceImplicitMap.putAll(myNewImplicitMap);
    }

    private CSharpCastSession() {
    }

    @Override
    public void close() {
        ourSession.remove();
    }
}
