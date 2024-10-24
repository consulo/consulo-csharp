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

package consulo.csharp.lang.impl.psi.source.resolve.util;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.impl.psi.source.resolve.AsPsiElementProcessor;
import consulo.csharp.lang.impl.psi.source.resolve.ExecuteTarget;
import consulo.csharp.lang.impl.psi.source.resolve.MemberResolveScopeProcessor;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideProcessor;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.ResolveState;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Collection;

/**
 * @author VISTALL
 * @since 18.05.14
 */
public class CSharpSearchUtil
{
	@Nullable
	@RequiredReadAction
	public static DotNetPropertyDeclaration findPropertyByName(@Nonnull final String name, @Nullable String parentQName, @Nonnull DotNetTypeRef typeRef)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve();
		PsiElement resolvedElement = typeResolveResult.getElement();
		if(resolvedElement == null)
		{
			return null;
		}
		DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();
		return findPropertyByName(name, resolvedElement, parentQName, genericExtractor);
	}

	@Nullable
	@RequiredReadAction
	public static DotNetPropertyDeclaration findPropertyByName(@Nonnull final String name, @Nonnull PsiElement owner, @Nullable String parentQName, @Nonnull DotNetGenericExtractor extractor)
	{
		AsPsiElementProcessor psiElementProcessor = new AsPsiElementProcessor();
		MemberResolveScopeProcessor memberResolveScopeProcessor = new MemberResolveScopeProcessor(owner, psiElementProcessor, new ExecuteTarget[]{ExecuteTarget.PROPERTY},
				OverrideProcessor.ALWAYS_TRUE);

		ResolveState state = ResolveState.initial();
		state = state.put(CSharpResolveUtil.EXTRACTOR, extractor);
		state = state.put(CSharpResolveUtil.SELECTOR, new MemberByNameSelector(name));

		CSharpResolveUtil.walkChildren(memberResolveScopeProcessor, owner, false, true, state);
		for(PsiElement element : psiElementProcessor.getElements())
		{
			if(isMyElement(element, parentQName))
			{
				return (DotNetPropertyDeclaration) element;
			}
		}
		return null;
	}

	@Nullable
	@RequiredReadAction
	public static DotNetMethodDeclaration findMethodByName(@Nonnull final String name, @Nullable String parentQName, @Nonnull DotNetTypeRef typeRef, int parameterSize)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve();
		PsiElement resolvedElement = typeResolveResult.getElement();
		if(resolvedElement == null)
		{
			return null;
		}
		return findMethodByName(name, resolvedElement, parentQName, typeResolveResult.getGenericExtractor(), parameterSize);
	}

	@Nullable
	@RequiredReadAction
	public static DotNetMethodDeclaration findMethodByName(@Nonnull final String name,
			@Nonnull PsiElement owner,
			@Nullable String parentQName,
			@Nonnull DotNetGenericExtractor extractor,
			final int parameterSize)
	{
		//TODO [VISTALL] some hack until we dont make override more powerfull
		if(parentQName != null)
		{
			if(owner instanceof DotNetMemberOwner)
			{
				for(DotNetNamedElement dotNetNamedElement : ((DotNetMemberOwner) owner).getMembers())
				{
					if(dotNetNamedElement instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) dotNetNamedElement).getParameters().length == 0 &&
							name.equals(dotNetNamedElement.getName()))
					{
						DotNetTypeRef typeRefForImplement = ((CSharpMethodDeclaration) dotNetNamedElement).getTypeRefForImplement();
						if(DotNetTypeRefUtil.isVmQNameEqual(typeRefForImplement, parentQName))
						{
							return (DotNetMethodDeclaration) GenericUnwrapTool.extract(dotNetNamedElement, extractor);
						}
					}
				}
			}
		}

		AsPsiElementProcessor psiElementProcessor = new AsPsiElementProcessor();
		MemberResolveScopeProcessor memberResolveScopeProcessor = new MemberResolveScopeProcessor(owner, psiElementProcessor, new ExecuteTarget[]{ExecuteTarget.ELEMENT_GROUP},
				OverrideProcessor.ALWAYS_TRUE);

		ResolveState state = ResolveState.initial();
		state = state.put(CSharpResolveUtil.EXTRACTOR, extractor);
		state = state.put(CSharpResolveUtil.SELECTOR, new MemberByNameSelector(name));

		CSharpResolveUtil.walkChildren(memberResolveScopeProcessor, owner, false, true, state);

		for(PsiElement psiElement : psiElementProcessor.getElements())
		{
			if(psiElement instanceof CSharpElementGroup)
			{
				for(PsiElement element : ((CSharpElementGroup<?>) psiElement).getElements())
				{
					//TODO [VISTALL] parameter handling
					if(element instanceof DotNetMethodDeclaration &&
							((DotNetMethodDeclaration) element).getParameters().length == parameterSize &&
							isMyElement(element, parentQName))
					{
						return (DotNetMethodDeclaration) element;
					}
				}
			}
		}
		return null;
	}

	private static boolean isMyElement(@Nonnull PsiElement element, @Nullable String parentQName)
	{
		if(parentQName == null)
		{
			return true;
		}

		if(isEqualVmQNameOfParent(element, parentQName))
		{
			return true;
		}

		Collection<DotNetVirtualImplementOwner> collection = OverrideUtil.collectOverridingMembers((DotNetVirtualImplementOwner) element);
		for(DotNetVirtualImplementOwner owner : collection)
		{
			if(isEqualVmQNameOfParent(owner, parentQName))
			{
				return true;
			}
		}
		return false;
	}

	@RequiredReadAction
	private static boolean isEqualVmQNameOfParent(PsiElement element, String parentQName)
	{
		PsiElement parent = element.getParent();
		return parent instanceof CSharpTypeDeclaration && parentQName.equals(((CSharpTypeDeclaration) parent).getVmQName());
	}
}
