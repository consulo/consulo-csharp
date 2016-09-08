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

package consulo.csharp.lang.psi.impl.source.resolve.util;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.resolve.AsPsiElementProcessor;
import consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideProcessor;
import consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import consulo.dotnet.psi.DotNetMemberOwner;
import consulo.dotnet.psi.DotNetMethodDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetPropertyDeclaration;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefUtil;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;

/**
 * @author VISTALL
 * @since 18.05.14
 */
public class CSharpSearchUtil
{
	@Nullable
	@RequiredReadAction
	public static DotNetPropertyDeclaration findPropertyByName(@NotNull final String name, @Nullable String parentQName, @NotNull DotNetTypeRef typeRef)
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
	public static DotNetPropertyDeclaration findPropertyByName(@NotNull final String name, @NotNull PsiElement owner, @Nullable String parentQName, @NotNull DotNetGenericExtractor extractor)
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
	public static DotNetMethodDeclaration findMethodByName(@NotNull final String name, @Nullable String parentQName, @NotNull DotNetTypeRef typeRef, int parameterSize)
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
	public static DotNetMethodDeclaration findMethodByName(@NotNull final String name,
			@NotNull PsiElement owner,
			@Nullable String parentQName,
			@NotNull DotNetGenericExtractor extractor,
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
						if(DotNetTypeRefUtil.isVmQNameEqual(typeRefForImplement, owner, parentQName))
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

	private static boolean isMyElement(@NotNull PsiElement element, @Nullable String parentQName)
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
