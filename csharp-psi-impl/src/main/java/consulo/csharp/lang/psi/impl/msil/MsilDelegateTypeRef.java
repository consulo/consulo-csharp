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

package consulo.csharp.lang.psi.impl.msil;

import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.ToNativeElementTransformers;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.msil.transformer.MsilToNativeElementTransformer;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpUserTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.SingleNullableStateResolveResult;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetInheritUtil;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.internal.dotnet.msil.decompiler.util.MsilHelper;
import consulo.msil.lang.psi.MsilClassEntry;
import consulo.msil.lang.psi.MsilMethodEntry;
import consulo.msil.lang.psi.impl.type.MsilClassGenericTypeRefImpl;
import consulo.msil.lang.psi.impl.type.MsilMethodGenericTypeRefImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilDelegateTypeRef extends DotNetTypeRefWithCachedResult
{
	private static class MsilResult extends SingleNullableStateResolveResult
	{
		private final PsiElement myElement;
		private final DotNetTypeResolveResult myResolveResult;

		public MsilResult(PsiElement element, DotNetTypeResolveResult resolveResult)
		{
			myElement = element;
			myResolveResult = resolveResult;
		}

		@Nullable
		@Override
		@RequiredReadAction
		public PsiElement getElement()
		{
			return ToNativeElementTransformers.transform(myElement);
		}

		@Nonnull
		@Override
		public DotNetGenericExtractor getGenericExtractor()
		{
			return myResolveResult.getGenericExtractor();
		}

		@Override
		@RequiredReadAction
		public boolean isNullableImpl()
		{
			return CSharpTypeUtil.isNullableElement(getElement());
		}
	}

	private NotNullLazyValue<DotNetTypeResolveResult> myResultValue = new NotNullLazyValue<DotNetTypeResolveResult>()
	{
		@Nonnull
		@Override
		@RequiredReadAction
		protected DotNetTypeResolveResult compute()
		{
			final DotNetTypeRef msilTypeRef = myTypeRef;
			final DotNetTypeResolveResult resolveResult = msilTypeRef.resolve();

			final PsiElement element = resolveResult.getElement();
			if(element == null)
			{
				return DotNetTypeResolveResult.EMPTY;
			}

			if(element instanceof MsilClassEntry)
			{
				MsilClassEntry msilClassEntry = (MsilClassEntry) element;

				if(DotNetInheritUtil.isInheritor(msilClassEntry, DotNetTypes.System.MulticastDelegate, false))
				{
					PsiElement transformedElement = ToNativeElementTransformers.transform(element);
					if(transformedElement instanceof CSharpMethodDeclaration)
					{
						return new CSharpLambdaTypeRef((CSharpMethodDeclaration) transformedElement).resolve();
					}
				}
			}
			else if(element instanceof DotNetGenericParameter)
			{
				if(msilTypeRef instanceof MsilMethodGenericTypeRefImpl)
				{
					MsilMethodGenericTypeRefImpl methodGenericTypeRef = (MsilMethodGenericTypeRefImpl) msilTypeRef;

					MsilMethodEntry msilMethodEntry = methodGenericTypeRef.getParent();

					MsilClassEntry rootClassEntry = MsilToNativeElementTransformer.findRootClassEntry((MsilClassEntry) msilMethodEntry.getParent());

					PsiElement wrappedElement = ToNativeElementTransformers.transform(rootClassEntry);

					if(wrappedElement == rootClassEntry)
					{
						return DotNetTypeResolveResult.EMPTY;
					}

					PsiElement elementByOriginal = MsilToNativeElementTransformer.findElementByOriginal(wrappedElement, msilMethodEntry);
					if(elementByOriginal instanceof DotNetGenericParameterListOwner)
					{
						DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) elementByOriginal).getGenericParameters();

						return new CSharpUserTypeRef.Result<PsiElement>(genericParameters[methodGenericTypeRef.getIndex()], DotNetGenericExtractor.EMPTY);
					}
				}
				else if(msilTypeRef instanceof MsilClassGenericTypeRefImpl)
				{
					MsilClassEntry msilClassEntry = ((MsilClassGenericTypeRefImpl) msilTypeRef).getParent();

					MsilClassEntry rootClassEntry = MsilToNativeElementTransformer.findRootClassEntry(msilClassEntry);

					PsiElement wrappedElement = ToNativeElementTransformers.transform(rootClassEntry);

					if(wrappedElement == rootClassEntry)
					{
						return DotNetTypeResolveResult.EMPTY;
					}

					PsiElement elementByOriginal = MsilToNativeElementTransformer.findElementByOriginal(wrappedElement, msilClassEntry);
					if(elementByOriginal instanceof DotNetGenericParameterListOwner)
					{
						final String genericName = msilTypeRef.toString();

						PsiElement owner = elementByOriginal;
						while(owner instanceof DotNetGenericParameterListOwner)
						{
							DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) owner).getGenericParameters();

							for(DotNetGenericParameter genericParameter : genericParameters)
							{
								if(genericName.equals(genericParameter.getName()))
								{
									return new CSharpUserTypeRef.Result<PsiElement>(genericParameter, DotNetGenericExtractor.EMPTY);
								}
							}

							owner = owner.getParent();
						}
					}
				}

				return DotNetTypeResolveResult.EMPTY;
			}

			return new MsilResult(element, resolveResult);
		}
	};

	private final DotNetTypeRef myTypeRef;

	public MsilDelegateTypeRef(@Nonnull DotNetTypeRef typeRef)
	{
		super(typeRef.getProject(), typeRef.getResolveScope());
		myTypeRef = typeRef;
	}

	@Override
	public boolean isEqualToVmQName(@Nonnull String vmQName)
	{
		return myTypeRef.isEqualToVmQName(vmQName);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return myResultValue.getValue();
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String toString()
	{
		return MsilHelper.prepareForUser(myTypeRef.toString());
	}
}
