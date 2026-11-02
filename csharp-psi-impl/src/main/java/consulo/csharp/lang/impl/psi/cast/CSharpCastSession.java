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

    /**
     * Starts a session, or joins the session of an outer check - closed by the outermost one only
     */
    public static CSharpCastSession start(Map<CSharpImplicitCastKey, CSharpTypeUtil.InheritResult> implicitCast) {
        CSharpCastSession session = get();
        if (session.myDepth++ == 0) {
            session.myServiceImplicitMap = Collections.unmodifiableMap(implicitCast);
        }
        return session;
    }

    public static CSharpCastSession get() {
        return Objects.requireNonNull(ourSession.get());
    }

    private Set<CSharpInheritKey> myVisited = new LinkedHashSet<>();

    private Map<CSharpImplicitCastKey, CSharpTypeUtil.InheritResult> myServiceImplicitMap = Map.of();

    private Map<CSharpImplicitCastKey, CSharpTypeUtil.InheritResult> myNewImplicitMap = new HashMap<>();

    private int myDepth;

    /**
     * @return false if the same check is already in progress - a recursion
     */
    private int myRecursionCount;

    public boolean mark(DotNetTypeRef top, DotNetTypeRef target, @Nullable Pair<CSharpCastType, GlobalSearchScope> castResolvingInfo) {
        CSharpInheritKey key = new CSharpInheritKey(top, target, castResolvingInfo);
        if (myVisited.add(key)) {
            return true;
        }
        myRecursionCount++;
        return false;
    }

    /**
     * How many checks failed as a recursion - a result computed while it grows depends on the checks in progress, so it
     * can be cached by the outermost check only
     */
    public int getRecursionCount() {
        return myRecursionCount;
    }

    public boolean isOutermost() {
        return myDepth <= 1;
    }

    public void unmark(DotNetTypeRef top, DotNetTypeRef target, @Nullable Pair<CSharpCastType, GlobalSearchScope> castResolvingInfo) {
        myVisited.remove(new CSharpInheritKey(top, target, castResolvingInfo));
    }

    public CSharpTypeUtil.InheritResult recordImplicit(CSharpImplicitCastKey key, CSharpTypeUtil.InheritResult result, int recursionCountBefore) {
        if (myRecursionCount == recursionCountBefore) {
            myNewImplicitMap.put(key, result);
        }
        return result;
    }

    public CSharpTypeUtil.InheritResult getImplicitResult(CSharpImplicitCastKey key) {
        CSharpTypeUtil.InheritResult result = myServiceImplicitMap.get(key);
        if (result != null) {
            return result;
        }

        return myNewImplicitMap.get(key);
    }

    public void storeImplicit(Map<CSharpImplicitCastKey, CSharpTypeUtil.InheritResult> serviceImplicitMap) {
        serviceImplicitMap.putAll(myNewImplicitMap);
    }

    private CSharpCastSession() {
    }

    @Override
    public void close() {
        if (--myDepth <= 0) {
            ourSession.remove();
        }
    }
}
