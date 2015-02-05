/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpQualifiedNonReference;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightArrayMethodDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightParameterBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.cache.CSharpResolveCache;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpArrayAccessExpressionImpl extends CSharpElementImpl implements DotNetExpression, CSharpCallArgumentListOwner,
		CSharpQualifiedNonReference
{
	public static class OurResolver implements CSharpResolveCache.PolyVariantResolver<CSharpArrayAccessExpressionImpl>
	{
		public static final OurResolver INSTANCE = new OurResolver();

		@NotNull
		@Override
		public ResolveResult[] resolve(@NotNull CSharpArrayAccessExpressionImpl expression, boolean incompleteCode, boolean resolveFromParent)
		{
			DotNetExpression qualifier = expression.getQualifier();
			DotNetTypeRef typeRef = qualifier.toTypeRef(true);
			if(typeRef instanceof DotNetPointerTypeRef)
			{
				DotNetTypeRef innerTypeRef = ((DotNetPointerTypeRef) typeRef).getInnerTypeRef();

				CSharpLightArrayMethodDeclarationBuilder builder = new CSharpLightArrayMethodDeclarationBuilder(expression.getProject());
				builder.withReturnType(innerTypeRef);
				builder.addParameter(new CSharpLightParameterBuilder(expression.getProject()).withName("p").withTypeRef(new CSharpTypeRefByQName
						(DotNetTypes.System.Int32)));
				return new ResolveResult[]{new PsiElementResolveResult(builder)};
			}

			ResolveResult[] resolveResults = CSharpReferenceExpressionImplUtil.multiResolveImpl(CSharpReferenceExpression.ResolveToKind.ARRAY_METHOD,
					expression, expression, true);
			if(!incompleteCode)
			{
				return resolveResults;
			}
			List<ResolveResult> filter = new ArrayList<ResolveResult>();
			for(ResolveResult resolveResult : resolveResults)
			{
				if(resolveResult.isValidResult())
				{
					filter.add(resolveResult);
				}
			}
			return ContainerUtil.toArray(filter, ResolveResult.EMPTY_ARRAY);
		}
	}

	public CSharpArrayAccessExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitArrayAccessExpression(this);
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		PsiElement resolve = resolveToCallable();
		if(resolve instanceof CSharpArrayMethodDeclaration)
		{
			return ((CSharpArrayMethodDeclaration) resolve).getReturnTypeRef();
		}
		return DotNetTypeRef.ERROR_TYPE;
	}

	@Override
	@NotNull
	public DotNetExpression getQualifier()
	{
		return (DotNetExpression) getFirstChild();
	}

	@Nullable
	@Override
	public String getReferenceName()
	{
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public String getReferenceNameWithAt()
	{
		throw new UnsupportedOperationException();
	}

	@NotNull
	@Override
	public DotNetExpression[] getParameterExpressions()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? DotNetExpression.EMPTY_ARRAY : parameterList.getExpressions();
	}

	@Override
	public boolean canResolve()
	{
		return true;
	}

	@NotNull
	@Override
	public CSharpCallArgument[] getCallArguments()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? CSharpCallArgument.EMPTY_ARRAY : parameterList.getArguments();
	}

	@Nullable
	@Override
	public CSharpCallArgumentList getParameterList()
	{
		return findChildByClass(CSharpCallArgumentList.class);
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

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		return CSharpResolveCache.getInstance(getProject()).resolveWithCaching(this, OurResolver.INSTANCE, true, incompleteCode, true);
	}
}
