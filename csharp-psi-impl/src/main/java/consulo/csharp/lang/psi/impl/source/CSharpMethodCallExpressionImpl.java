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

package consulo.csharp.lang.psi.impl.source;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import consulo.annotations.DeprecationInfo;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodResolvePriorityInfo;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.NCallArgumentBuilder;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResultUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 16.12.13.
 */
public class CSharpMethodCallExpressionImpl extends CSharpExpressionImpl implements DotNetExpression, CSharpCallArgumentListOwner
{
	public CSharpMethodCallExpressionImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	@Override
	@Nullable
	public CSharpCallArgumentList getParameterList()
	{
		return findChildByClass(CSharpCallArgumentList.class);
	}

	@Override
	@Nonnull
	@Deprecated
	@DeprecationInfo("Use #getCallArguments() due we can have named arguments")
	public DotNetExpression[] getParameterExpressions()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? DotNetExpression.EMPTY_ARRAY : parameterList.getExpressions();
	}

	@Nonnull
	@Override
	public CSharpCallArgument[] getCallArguments()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? CSharpCallArgument.EMPTY_ARRAY : parameterList.getArguments();
	}

	@Nonnull
	public DotNetExpression getCallExpression()
	{
		return findNotNullChildByClass(DotNetExpression.class);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitMethodCallExpression(this);
	}

	@Override
	@Nullable
	@RequiredReadAction
	public PsiElement resolveToCallable()
	{
		DotNetExpression callExpression = getCallExpression();

		if(callExpression instanceof CSharpReferenceExpression)
		{
			PsiElement resolvedElement = ((CSharpReferenceExpression) callExpression).resolve();
			if(resolvedElement != null)
			{
				return resolvedElement;
			}

			ResolveResult[] resolveResults = multiResolve(false);
			if(resolveResults.length > 0)
			{
				return resolveResults[0].getElement();
			}
		}
		else
		{
			return CSharpResolveUtil.findFirstValidElement(multiResolve(false));
		}
		return null;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		DotNetExpression callExpression = getCallExpression();

		if(callExpression instanceof CSharpReferenceExpression)
		{
			return ((CSharpReferenceExpression) callExpression).multiResolve(incompleteCode);
		}
		else
		{
			DotNetTypeRef typeRef = callExpression.toTypeRef(true);

			DotNetTypeResolveResult typeResolveResult = typeRef.resolve();

			PsiElement element = typeResolveResult.getElement();
			CSharpMethodDeclaration declaration = CSharpLambdaResolveResultUtil.getDelegateMethodTypeWrapper(element);
			if(declaration != null)
			{
				declaration = GenericUnwrapTool.extract(declaration, typeResolveResult.getGenericExtractor());

				MethodResolvePriorityInfo calcResult = NCallArgumentBuilder.calc(this, declaration, this);

				return new ResolveResult[]{MethodResolveResult.createResult(calcResult, declaration, null)};
			}
			return ResolveResult.EMPTY_ARRAY;
		}
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		PsiElement resolvedElement = resolveToCallable();
		if(resolvedElement instanceof DotNetVariable)
		{
			DotNetTypeRef dotNetTypeRef = ((DotNetVariable) resolvedElement).toTypeRef(false);
			DotNetTypeResolveResult typeResolveResult = dotNetTypeRef.resolve();
			if(typeResolveResult instanceof CSharpLambdaResolveResult)
			{
				return ((CSharpLambdaResolveResult) typeResolveResult).getReturnTypeRef();
			}
			return DotNetTypeRef.ERROR_TYPE;
		}
		if(resolvedElement instanceof CSharpSimpleLikeMethodAsElement)
		{
			return ((CSharpSimpleLikeMethodAsElement) resolvedElement).getReturnTypeRef();
		}
		return CSharpReferenceExpressionImplUtil.toTypeRef(resolvedElement);
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		CSharpCallArgument[] callArguments = getCallArguments();
		for(CSharpCallArgument callArgument : callArguments)
		{
			if(!callArgument.processDeclarations(processor, state, lastParent, place))
			{
				return false;
			}
		}

		return super.processDeclarations(processor, state, lastParent, place);
	}
}
