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
package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import org.jspecify.annotations.Nullable;

/**
 * C# 9 {@code r with { X = 1 }} - a copy of r, of the type of r, with the members of the block set
 */
public class CSharpWithExpressionImpl extends CSharpExpressionImpl {
    public CSharpWithExpressionImpl(IElementType elementType) {
        super(elementType);
    }

    @RequiredReadAction
    public @Nullable DotNetExpression getQualifier() {
        return findChildByClass(DotNetExpression.class);
    }

    @RequiredReadAction
    @Override
    public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent) {
        DotNetExpression qualifier = getQualifier();
        return qualifier == null ? DotNetTypeRef.ERROR_TYPE : qualifier.toTypeRef(resolveFromParent);
    }

    @Override
    public void accept(CSharpElementVisitor visitor) {
        visitor.visitElement(this);
    }
}
