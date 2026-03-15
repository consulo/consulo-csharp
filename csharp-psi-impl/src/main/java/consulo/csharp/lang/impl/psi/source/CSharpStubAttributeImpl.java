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

package consulo.csharp.lang.impl.psi.source;

import org.jspecify.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpCallArgumentList;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import consulo.csharp.lang.impl.psi.stub.CSharpWithStringValueStub;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.stub.IStubElementType;

/**
 * @author VISTALL
 * @since 19.12.13.
 */
public class CSharpStubAttributeImpl extends CSharpStubElementImpl<CSharpWithStringValueStub<CSharpAttribute>> implements CSharpAttribute
{
	public CSharpStubAttributeImpl(ASTNode node)
	{
		super(node);
	}

	public CSharpStubAttributeImpl(CSharpWithStringValueStub<CSharpAttribute> stub,
			IStubElementType<? extends CSharpWithStringValueStub<CSharpAttribute>, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(CSharpElementVisitor visitor)
	{
		visitor.visitAttribute(this);
	}

	@Nullable
	@Override
	public DotNetTypeDeclaration resolveToType()
	{
		CSharpReferenceExpression ref = getReferenceExpression();
		if(ref == null)
		{
			return null;
		}

		PsiElement psiElement = CSharpReferenceExpressionImplUtil.resolveByTypeKind(ref, true);
		if(psiElement instanceof DotNetTypeDeclaration)
		{
			return (DotNetTypeDeclaration) psiElement;
		}
		return null;
	}

	@Override
	public DotNetTypeRef toTypeRef()
	{
		DotNetTypeDeclaration dotNetTypeDeclaration = resolveToType();
		if(dotNetTypeDeclaration != null)
		{
			return new CSharpTypeRefByTypeDeclaration(dotNetTypeDeclaration);
		}
		return DotNetTypeRef.ERROR_TYPE;
	}

	@Nullable
	@Override
	public CSharpCallArgumentList getParameterList()
	{
		return findChildByClass(CSharpCallArgumentList.class);
	}

	@Override
	public DotNetExpression[] getParameterExpressions()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? DotNetExpression.EMPTY_ARRAY : parameterList.getExpressions();
	}

	@Override
	public CSharpCallArgument[] getCallArguments()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? CSharpCallArgument.EMPTY_ARRAY : parameterList.getArguments();
	}

	@Nullable
	@Override
	public PsiElement resolveToCallable()
	{
		CSharpReferenceExpression ref = getReferenceExpression();
		if(ref == null)
		{
			return null;
		}

		return ref.resolve();
	}

	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		CSharpReferenceExpression ref = getReferenceExpression();
		if(ref == null)
		{
			return ResolveResult.EMPTY_ARRAY;
		}

		return ref.multiResolve(incompleteCode);
	}

	@Override
	@Nullable
	public CSharpReferenceExpression getReferenceExpression()
	{
		return getStubOrPsiChild(CSharpStubElements.REFERENCE_EXPRESSION);
	}
}
