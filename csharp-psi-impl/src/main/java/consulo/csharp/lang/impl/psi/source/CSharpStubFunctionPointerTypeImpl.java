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
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpPointerTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.EmptyStub;
import consulo.language.psi.stub.IStubElementType;

/**
 * C# 9 function pointer {@code delegate* unmanaged[Cdecl]<int, void>}
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CSharpStubFunctionPointerTypeImpl extends CSharpStubTypeElementImpl<EmptyStub<DotNetType>> {
    public CSharpStubFunctionPointerTypeImpl(ASTNode node) {
        super(node);
    }

    public CSharpStubFunctionPointerTypeImpl(EmptyStub<DotNetType> stub, IStubElementType<? extends EmptyStub<DotNetType>, ?> nodeType) {
        super(stub, nodeType);
    }

    @RequiredReadAction
    @Override
    protected DotNetTypeRef toTypeRefImpl() {
        return new CSharpPointerTypeRef(new CSharpTypeRefByQName(this, DotNetTypes.System.Void));
    }

    @Override
    public void accept(CSharpElementVisitor visitor) {
        visitor.visitElement(this);
    }
}
