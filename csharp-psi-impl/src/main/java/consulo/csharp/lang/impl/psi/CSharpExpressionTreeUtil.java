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

package consulo.csharp.lang.impl.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;

/**
 * Expression trees: a lambda converts to {@code System.Linq.Expressions.Expression<TDelegate>} the way it converts to
 * {@code TDelegate} - {@code Expression<Func<T, object>> e = o => o.Name;} types {@code o} by the {@code Func}.
 *
 * @author VISTALL
 */
public final class CSharpExpressionTreeUtil {
    public static final String EXPRESSION_VM_QNAME = "System.Linq.Expressions.Expression`1";

    private CSharpExpressionTreeUtil() {
    }

    /**
     * @return {@code TDelegate} for {@code Expression<TDelegate>}, the type itself otherwise
     */
    @RequiredReadAction
    public static DotNetTypeRef unwrap(DotNetTypeRef typeRef) {
        DotNetTypeResolveResult result = typeRef.resolve();
        if (result.getElement() instanceof DotNetTypeDeclaration type && EXPRESSION_VM_QNAME.equals(type.getVmQName())) {
            DotNetGenericParameter[] genericParameters = type.getGenericParameters();
            if (genericParameters.length == 1) {
                DotNetTypeRef delegateTypeRef = result.getGenericExtractor().extract(genericParameters[0]);
                if (delegateTypeRef != null) {
                    return delegateTypeRef;
                }
            }
        }
        return typeRef;
    }
}
