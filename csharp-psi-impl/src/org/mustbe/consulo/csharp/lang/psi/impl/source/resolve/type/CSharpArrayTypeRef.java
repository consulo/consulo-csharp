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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetArrayTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.NullableLazyValue;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpArrayTypeRef extends DotNetTypeRef.Adapter implements DotNetArrayTypeRef
{
	public static class Result implements DotNetTypeResolveResult
	{
		private final PsiElement myScope;
		private final int myDimensions;
		private final DotNetTypeRef myInnerType;

		private NullableLazyValue<PsiElement> myValue = new NullableLazyValue<PsiElement>()
		{
			@Nullable
			@Override
			protected PsiElement compute()
			{
				Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(myScope);
				if(moduleForPsiElement == null)
				{
					return null;
				}
				return CSharpModuleTypeHelper.getInstance(moduleForPsiElement).getArrayType(myDimensions);
			}
		};

		public Result(PsiElement scope, int dimensions, DotNetTypeRef innerType)
		{
			myScope = scope;
			myDimensions = dimensions;
			myInnerType = innerType;
		}

		@Nullable
		@Override
		public PsiElement getElement()
		{
			return myValue.getValue();
		}

		@NotNull
		@Override
		public DotNetGenericExtractor getGenericExtractor()
		{
			PsiElement element = getElement();

			if(!(element instanceof DotNetGenericParameterListOwner))
			{
				return DotNetGenericExtractor.EMPTY;
			}
			return new CSharpGenericExtractor(((DotNetGenericParameterListOwner) element).getGenericParameters(),
					new DotNetTypeRef[]{myInnerType});
		}

		@Override
		public boolean isNullable()
		{
			return true;
		}
	}

	private final DotNetTypeRef myInnerType;
	private final int myDimensions;

	public CSharpArrayTypeRef(DotNetTypeRef innerType, int dimensions)
	{
		myInnerType = innerType;
		myDimensions = dimensions;
	}

	@NotNull
	@Override
	public String getPresentableText()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(myInnerType.getPresentableText());
		builder.append("[");
		for(int i = 0; i < myDimensions; i++)
		{
			builder.append(",");
		}
		builder.append("]");
		return builder.toString();
	}

	@NotNull
	@Override
	public String getQualifiedText()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(myInnerType.getQualifiedText());
		builder.append("[");
		for(int i = 0; i < myDimensions; i++)
		{
			builder.append(",");
		}
		builder.append("]");
		return builder.toString();
	}

	@NotNull
	@Override
	public DotNetTypeResolveResult resolve(@NotNull PsiElement scope)
	{
		return new Result(scope, myDimensions, myInnerType);
	}

	@Override
	@NotNull
	public DotNetTypeRef getInnerTypeRef()
	{
		return myInnerType;
	}

	public int getDimensions()
	{
		return myDimensions;
	}
}
