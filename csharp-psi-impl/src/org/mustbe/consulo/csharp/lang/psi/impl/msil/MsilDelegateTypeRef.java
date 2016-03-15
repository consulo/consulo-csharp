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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.ToNativeElementTransformers;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.transformer.MsilToNativeElementTransformer;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpReferenceTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.SingleNullableStateResolveResult;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetInheritUtil;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.consulo.msil.lang.psi.MsilClassEntry;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import org.mustbe.consulo.msil.lang.psi.impl.type.MsilClassGenericTypeRefImpl;
import org.mustbe.consulo.msil.lang.psi.impl.type.MsilMethodGenericTypeRefImpl;
import org.mustbe.dotnet.msil.decompiler.util.MsilHelper;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilDelegateTypeRef extends DotNetTypeRef.Delegate
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

		@NotNull
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
		@NotNull
		@Override
		@RequiredReadAction
		protected DotNetTypeResolveResult compute()
		{
			final DotNetTypeRef msilTypeRef = getDelegate();
			final DotNetTypeResolveResult resolveResult = msilTypeRef.resolve(myScope);

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
						return new CSharpLambdaTypeRef((CSharpMethodDeclaration) transformedElement).resolve(myScope);
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

						return new CSharpReferenceTypeRef.Result<PsiElement>(genericParameters[methodGenericTypeRef.getIndex()], DotNetGenericExtractor.EMPTY);
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
						final String genericName = msilTypeRef.getPresentableText();

						PsiElement owner = elementByOriginal;
						while(owner instanceof DotNetGenericParameterListOwner)
						{
							DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) owner).getGenericParameters();

							for(DotNetGenericParameter genericParameter : genericParameters)
							{
								if(genericName.equals(genericParameter.getName()))
								{
									return new CSharpReferenceTypeRef.Result<PsiElement>(genericParameter, DotNetGenericExtractor.EMPTY);
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

	@NotNull
	private final PsiElement myScope;

	public MsilDelegateTypeRef(@NotNull PsiElement scope, @NotNull DotNetTypeRef typeRef)
	{
		super(typeRef);
		myScope = scope;
	}

	@NotNull
	@Override
	public String getPresentableText()
	{
		return MsilHelper.cutGenericMarker(super.getPresentableText());
	}

	@NotNull
	@Override
	public String getQualifiedText()
	{
		return MsilHelper.prepareForUser(super.getQualifiedText());
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeResolveResult resolve(@NotNull final PsiElement scope)
	{
		return myResultValue.getValue();
	}
}
