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

package consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.csharp.lang.psi.CSharpTypeDefStatement;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.lombok.annotations.Lazy;

/**
 * @author VISTALL
 * @since 20.10.14
 */
public class CSharpUserTypeRef extends DotNetTypeRefWithCachedResult
{
	public static class Result<T extends PsiElement> extends SingleNullableStateResolveResult
	{
		protected final T myElement;
		protected final DotNetGenericExtractor myExtractor;

		public Result(T element, DotNetGenericExtractor extractor)
		{
			myElement = element;
			myExtractor = extractor;
		}

		@Nullable
		@Override
		public PsiElement getElement()
		{
			return myElement;
		}

		@NotNull
		@Override
		public DotNetGenericExtractor getGenericExtractor()
		{
			return myExtractor;
		}

		@RequiredReadAction
		@Override
		public boolean isNullableImpl()
		{
			PsiElement element = getElement();
			return element == null || CSharpTypeUtil.isNullableElement(element);
		}
	}

	public static class LambdaResult extends Result<CSharpMethodDeclaration> implements CSharpLambdaResolveResult
	{
		private final PsiElement myScope;

		public LambdaResult(@NotNull PsiElement scope, @NotNull CSharpMethodDeclaration element, @NotNull DotNetGenericExtractor extractor)
		{
			super(element, extractor);
			myScope = scope;
		}

		@NotNull
		@Override
		@RequiredReadAction
		@Lazy
		public CSharpSimpleParameterInfo[] getParameterInfos()
		{
			CSharpSimpleParameterInfo[] parameterInfos = myElement.getParameterInfos();
			if(myExtractor == DotNetGenericExtractor.EMPTY)
			{
				return parameterInfos;
			}
			CSharpSimpleParameterInfo[] temp = new CSharpSimpleParameterInfo[parameterInfos.length];
			for(int i = 0; i < parameterInfos.length; i++)
			{
				CSharpSimpleParameterInfo parameterInfo = parameterInfos[i];
				DotNetTypeRef typeRef = GenericUnwrapTool.exchangeTypeRef(parameterInfo.getTypeRef(), getGenericExtractor(), myScope);
				temp[i] = new CSharpSimpleParameterInfo(parameterInfo.getIndex(), parameterInfo.getName(), parameterInfo.getElement(), typeRef);
			}
			return temp;
		}

		@Nullable
		@Override
		@Lazy
		public PsiElement getElement()
		{
			return CSharpLambdaResolveResultUtil.createTypeFromDelegate(myElement);
		}

		@RequiredReadAction
		@NotNull
		@Override
		@Lazy
		public DotNetTypeRef getReturnTypeRef()
		{
			return GenericUnwrapTool.exchangeTypeRef(myElement.getReturnTypeRef(), getGenericExtractor(), myScope);
		}

		@RequiredReadAction
		@Override
		public boolean isInheritParameters()
		{
			return false;
		}

		@RequiredReadAction
		@NotNull
		@Override
		@Lazy
		public DotNetTypeRef[] getParameterTypeRefs()
		{
			return GenericUnwrapTool.exchangeTypeRefs(myElement.getParameterTypeRefs(), getGenericExtractor(), myScope);
		}

		@RequiredReadAction
		@Override
		public boolean isNullableImpl()
		{
			return true;
		}

		@NotNull
		@Override
		public CSharpMethodDeclaration getTarget()
		{
			return myElement;
		}
	}

	protected final CSharpReferenceExpression myReferenceExpression;

	public CSharpUserTypeRef(@NotNull CSharpReferenceExpression referenceExpression)
	{
		myReferenceExpression = referenceExpression;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String toString()
	{
		DotNetTypeRef[] argumentTypeRefs = myReferenceExpression.getTypeArgumentListRefs();

		StringBuilder builder = new StringBuilder();
		builder.append(myReferenceExpression.getReferenceName());

		if(argumentTypeRefs.length > 0)
		{
			builder.append("<");
			for(int i = 0; i < argumentTypeRefs.length; i++)
			{
				if(i != 0)
				{
					builder.append(", ");
				}
				DotNetTypeRef argument = argumentTypeRefs[i];
				builder.append(argument.toString());
			}
			builder.append(">");
		}
		return builder.toString();
	}

	@NotNull
	@Override
	@RequiredReadAction
	public DotNetTypeResolveResult resolveResult()
	{
		PsiElement resolve = myReferenceExpression.resolve();
		if(resolve instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) resolve).isDelegate())
		{
			return new LambdaResult(myReferenceExpression, (CSharpMethodDeclaration) resolve, createExtractor(resolve));
		}
		else if(resolve instanceof CSharpTypeDefStatement)
		{
			DotNetTypeRef typeRef = ((CSharpTypeDefStatement) resolve).toTypeRef();
			return typeRef.resolve();
		}
		return new Result<>(resolve, createExtractor(resolve));
	}

	@NotNull
	public CSharpReferenceExpression getReferenceExpression()
	{
		return myReferenceExpression;
	}

	@NotNull
	@RequiredReadAction
	private DotNetGenericExtractor createExtractor(PsiElement resolved)
	{
		DotNetTypeRef[] typeArgumentListRefs = myReferenceExpression.getTypeArgumentListRefs();
		if(typeArgumentListRefs.length == 0)
		{
			return DotNetGenericExtractor.EMPTY;
		}
		if(resolved instanceof DotNetGenericParameterListOwner)
		{
			DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) resolved).getGenericParameters();
			if(genericParameters.length == typeArgumentListRefs.length)
			{
				return CSharpGenericExtractor.create(genericParameters, typeArgumentListRefs);
			}
		}
		return DotNetGenericExtractor.EMPTY;
	}

	@NotNull
	@RequiredReadAction
	public String getReferenceText()
	{
		DotNetTypeRef[] argumentTypeRefs = myReferenceExpression.getTypeArgumentListRefs();

		StringBuilder builder = new StringBuilder();
		builder.append(myReferenceExpression.getReferenceName());

		if(argumentTypeRefs.length > 0)
		{
			builder.append("<");
			for(int i = 0; i < argumentTypeRefs.length; i++)
			{
				if(i != 0)
				{
					builder.append(", ");
				}
				DotNetTypeRef argument = argumentTypeRefs[i];
				builder.append(argument.toString());
			}
			builder.append(">");
		}
		return builder.toString();
	}
}
