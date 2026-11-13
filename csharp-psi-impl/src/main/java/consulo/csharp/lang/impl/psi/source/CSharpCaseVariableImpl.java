/*
 * Copyright 2013-2019 consulo.io
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

import consulo.language.ast.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;

import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2019-10-05
 */
public class CSharpCaseVariableImpl extends CSharpVariableImpl {
    public CSharpCaseVariableImpl(IElementType elementType) {
        super(elementType);
    }

    @Override
    public void accept(CSharpElementVisitor visitor) {
        visitor.visitCaseVariable(this);
    }

    @RequiredReadAction
    @Nullable
    @Override
    public DotNetType getType() {
        return findChildByClass(DotNetType.class);
    }

    /**
     * {@code var v} is of the type of the value matched - the switch value, an element of a positional or a list pattern,
     * a property of a property pattern. The designation of {@code (1, var y) t} has no type of its own
     */
    @RequiredReadAction
    @Override
    public DotNetTypeRef toTypeRefImpl(boolean resolveFromInitializer) {
        DotNetType type = getType();
        if (type == null) {
            CSharpPatternImpl pattern = findChildByClass(CSharpPatternImpl.class);
            return pattern == null ? DotNetTypeRef.ERROR_TYPE : CSharpPatternTypeUtil.getMatchedTypeRef(pattern);
        }

        DotNetTypeRef typeRef = type.toTypeRef();
        if (typeRef != DotNetTypeRef.AUTO_TYPE) {
            return typeRef;
        }
        return CSharpPatternTypeUtil.getInputTypeRef(this);
    }
}
