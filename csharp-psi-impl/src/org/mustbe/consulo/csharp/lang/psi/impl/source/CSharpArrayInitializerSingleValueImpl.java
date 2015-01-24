/*
 * Copyright 2013-2015 must-be.org
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 24.01.15
 */
public class CSharpArrayInitializerSingleValueImpl extends CSharpElementImpl implements CSharpArrayInitializerValue, CSharpCallArgument, CSharpCallArgumentListOwner
{
	public CSharpArrayInitializerSingleValueImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
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

	@NotNull
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

	@NotNull
	@Override
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

			DotNetTypeResolveResult typeResolveResult = typeRef.resolve(this);
			PsiElement resolvedElement = typeResolveResult.getElement();
			if(resolvedElement == null)
			{
				return ResolveResult.EMPTY_ARRAY;
			}

			CSharpResolveOptions options = new CSharpResolveOptions(CSharpReferenceExpression.ResolveToKind.METHOD,
					new MemberByNameSelector("Add"), this, this, false, true);

			return CSharpReferenceExpressionImplUtil.collectResults(options, typeResolveResult.getGenericExtractor(), resolvedElement);
		}
		return ResolveResult.EMPTY_ARRAY;
	}

	@NotNull
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
