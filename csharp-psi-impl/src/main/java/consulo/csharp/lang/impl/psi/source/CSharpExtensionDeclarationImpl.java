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
import consulo.csharp.lang.impl.psi.CSharpStubElementSets;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.impl.psi.source.resolve.ExecuteTarget;
import consulo.csharp.lang.impl.psi.source.resolve.ExecuteTargetUtil;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterList;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.stub.EmptyStub;
import consulo.language.psi.stub.IStubElementType;
import org.jspecify.annotations.Nullable;

/**
 * C# 14 extension block of a static class - {@code extension<T>(List<T> list) where T : class { members }}, the receiver
 * parameter and the type parameters are visible in the members
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CSharpExtensionDeclarationImpl extends CSharpStubElementImpl<EmptyStub<CSharpExtensionDeclarationImpl>> implements DotNetGenericParameterListOwner {
    public CSharpExtensionDeclarationImpl(ASTNode node) {
        super(node);
    }

    public CSharpExtensionDeclarationImpl(EmptyStub<CSharpExtensionDeclarationImpl> stub,
                                          IStubElementType<? extends EmptyStub<CSharpExtensionDeclarationImpl>, ?> nodeType) {
        super(stub, nodeType);
    }

    @Override
    public void accept(CSharpElementVisitor visitor) {
        visitor.visitElement(this);
    }

    @Nullable
    @Override
    public DotNetGenericParameterList getGenericParameterList() {
        return getStubOrPsiChild(CSharpStubElements.GENERIC_PARAMETER_LIST);
    }

    @Override
    public DotNetGenericParameter[] getGenericParameters() {
        DotNetGenericParameterList genericParameterList = getGenericParameterList();
        return genericParameterList == null ? DotNetGenericParameter.EMPTY_ARRAY : genericParameterList.getParameters();
    }

    @Override
    public int getGenericParametersCount() {
        DotNetGenericParameterList genericParameterList = getGenericParameterList();
        return genericParameterList == null ? 0 : genericParameterList.getGenericParametersCount();
    }

    @Nullable
    public DotNetParameterList getParameterList() {
        return getStubOrPsiChild(CSharpStubElements.PARAMETER_LIST);
    }

    /**
     * @return the receiver - unnamed in {@code extension(int)}, an extension of static members only
     */
    @Nullable
    public DotNetParameter getReceiverParameter() {
        DotNetParameterList parameterList = getParameterList();
        DotNetParameter[] parameters = parameterList == null ? DotNetParameter.EMPTY_ARRAY : parameterList.getParameters();
        return parameters.length == 0 ? null : parameters[0];
    }

    public DotNetQualifiedElement[] getMembers() {
        return getStubOrPsiChildren(CSharpStubElementSets.QUALIFIED_MEMBERS, DotNetQualifiedElement.ARRAY_FACTORY);
    }

    @RequiredReadAction
    @Override
    public boolean processDeclarations(PsiScopeProcessor processor, ResolveState state, @Nullable PsiElement lastParent, PsiElement place) {
        if (ExecuteTargetUtil.canProcess(processor, ExecuteTarget.GENERIC_PARAMETER)) {
            for (DotNetGenericParameter genericParameter : getGenericParameters()) {
                if (!processor.execute(genericParameter, state)) {
                    return false;
                }
            }
        }

        if (ExecuteTargetUtil.canProcess(processor, ExecuteTarget.LOCAL_VARIABLE_OR_PARAMETER_OR_LOCAL_METHOD)) {
            DotNetParameter receiver = getReceiverParameter();
            if (receiver != null && receiver.getName() != null && !processor.execute(receiver, state)) {
                return false;
            }
        }
        return true;
    }
}
