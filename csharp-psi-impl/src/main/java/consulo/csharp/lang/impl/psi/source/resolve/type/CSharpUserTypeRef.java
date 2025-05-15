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

package consulo.csharp.lang.impl.psi.source.resolve.type;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.NotNullLazyValue;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.csharp.lang.psi.CSharpTypeDefStatement;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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

		@Nonnull
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
		private final NotNullLazyValue<CSharpSimpleParameterInfo[]> myParameterInfosValue;
		private final NotNullLazyValue<PsiElement> myElementValue;
		private final NotNullLazyValue<DotNetTypeRef> myReturnTypRefValue;


		@RequiredReadAction
		public LambdaResult(@Nonnull Project project, @Nonnull GlobalSearchScope resolveScope, @Nonnull CSharpMethodDeclaration element, @Nonnull DotNetGenericExtractor extractor)
		{
			super(element, extractor);
			myParameterInfosValue = NotNullLazyValue.createValue(() ->
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
					DotNetTypeRef typeRef = GenericUnwrapTool.exchangeTypeRef(parameterInfo.getTypeRef(), getGenericExtractor());
					temp[i] = new CSharpSimpleParameterInfo(parameterInfo.getIndex(), parameterInfo.getName(), parameterInfo.getElement(), typeRef);
				}
				return temp;
			});
			myElementValue = NotNullLazyValue.createValue(() -> CSharpLambdaResolveResultUtil.createTypeFromDelegate(myElement, myExtractor));
			myReturnTypRefValue = NotNullLazyValue.createValue(() -> GenericUnwrapTool.exchangeTypeRef(myElement.getReturnTypeRef(), getGenericExtractor()));
		}

		@Nonnull
		@Override
		@RequiredReadAction
		public CSharpSimpleParameterInfo[] getParameterInfos()
		{
			return myParameterInfosValue.getValue();
		}

		@Nullable
		@Override
		public PsiElement getElement()
		{
			return myElementValue.getValue();
		}

		@RequiredReadAction
		@Nonnull
		@Override
		public DotNetTypeRef getReturnTypeRef()
		{
			return myReturnTypRefValue.getValue();
		}

		@RequiredReadAction
		@Override
		public boolean isInheritParameters()
		{
			return false;
		}

		@RequiredReadAction
		@Override
		public boolean isNullableImpl()
		{
			return true;
		}

		@Nonnull
		@Override
		public CSharpMethodDeclaration getTarget()
		{
			return GenericUnwrapTool.extract(myElement, getGenericExtractor());
		}
	}

	protected final CSharpReferenceExpression myReferenceExpression;

	public CSharpUserTypeRef(@Nonnull CSharpReferenceExpression referenceExpression)
	{
		super(referenceExpression.getProject(), referenceExpression.getResolveScope());
		myReferenceExpression = referenceExpression;
	}

	@RequiredReadAction
	@Nonnull
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

	@Nonnull
	@Override
	public String getVmQName()
	{
		return CSharpTypeRefPresentationUtil.buildVmQName(this);
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public DotNetTypeResolveResult resolveResult()
	{
		PsiElement resolve = myReferenceExpression.resolve();
		if(resolve instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) resolve).isDelegate())
		{
			return new LambdaResult(myReferenceExpression.getProject(), myReferenceExpression.getResolveScope(), (CSharpMethodDeclaration) resolve, createExtractor(resolve));
		}
		else if(resolve instanceof CSharpTypeDefStatement)
		{
			DotNetTypeRef typeRef = ((CSharpTypeDefStatement) resolve).toTypeRef();
			return typeRef.resolve();
		}
		return new Result<>(resolve, createExtractor(resolve));
	}

	@Nonnull
	public CSharpReferenceExpression getReferenceExpression()
	{
		return myReferenceExpression;
	}

	@Nonnull
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

	@Nonnull
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

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}

		CSharpUserTypeRef that = (CSharpUserTypeRef) o;

		if(myReferenceExpression != null ? !myReferenceExpression.equals(that.myReferenceExpression) : that.myReferenceExpression != null)
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		return myReferenceExpression != null ? myReferenceExpression.hashCode() : 0;
	}
}
