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

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.function.CommonProcessors;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.util.PsiTreeUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 24.01.15
 */
public class CSharpArrayInitializerSingleValueImpl extends CSharpElementImpl implements CSharpArrayInitializerValue, CSharpCallArgument, CSharpCallArgumentListOwner
{
	public CSharpArrayInitializerSingleValueImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitArrayInitializerSingleValue(this);
	}

	@Nullable
	@Override
	public DotNetExpression getArgumentExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@Override
	public boolean canResolve()
	{
		CSharpArrayInitializerOwner arrayInitializerOwner = PsiTreeUtil.getParentOfType(this, CSharpArrayInitializerOwner.class);
		if(arrayInitializerOwner instanceof CSharpNewExpressionImpl)
		{
			DotNetTypeRef typeRef = ((CSharpNewExpressionImpl) arrayInitializerOwner).toTypeRef(false);
			return typeRef != DotNetTypeRef.ERROR_TYPE;
		}
		return false;
	}

	@Nonnull
	@Override
	public DotNetExpression[] getParameterExpressions()
	{
		return new DotNetExpression[0];
	}

	@Nullable
	@Override
	public PsiElement resolveToCallable()
	{
		ResolveResult[] resolveResults = multiResolve(false);
		if(resolveResults.length == 0)
		{
			return null;
		}
		return CSharpResolveUtil.findFirstValidElement(resolveResults);
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public ResolveResult[] multiResolve(boolean b)
	{
		CSharpArrayInitializerOwner arrayInitializerOwner = PsiTreeUtil.getParentOfType(this, CSharpArrayInitializerOwner.class);
		if(arrayInitializerOwner instanceof CSharpNewExpressionImpl)
		{
			DotNetTypeRef typeRef = ((CSharpNewExpressionImpl) arrayInitializerOwner).toTypeRef(false);
			if(typeRef == DotNetTypeRef.ERROR_TYPE)
			{
				return ResolveResult.EMPTY_ARRAY;
			}

			DotNetTypeResolveResult typeResolveResult = typeRef.resolve();
			PsiElement resolvedElement = typeResolveResult.getElement();
			if(resolvedElement == null)
			{
				return ResolveResult.EMPTY_ARRAY;
			}

			CSharpResolveOptions options = new CSharpResolveOptions(CSharpReferenceExpression.ResolveToKind.METHOD,
					new MemberByNameSelector("Add"), this, this, false, true);

			CommonProcessors.CollectProcessor<ResolveResult> processor = new CommonProcessors.CollectProcessor<ResolveResult>();
			CSharpReferenceExpressionImplUtil.collectResults(options, typeResolveResult.getGenericExtractor(), resolvedElement, processor);
			return processor.toArray(ResolveResult.ARRAY_FACTORY);
		}
		return ResolveResult.EMPTY_ARRAY;
	}

	@Nonnull
	@Override
	public CSharpCallArgument[] getCallArguments()
	{
		return new CSharpCallArgument[] {this};
	}

	@Nullable
	@Override
	public CSharpCallArgumentList getParameterList()
	{
		return null;
	}
}
