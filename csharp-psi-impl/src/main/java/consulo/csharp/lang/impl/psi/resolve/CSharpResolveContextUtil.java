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

package consulo.csharp.lang.impl.psi.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetPsiSearcher;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.DumbService;
import consulo.util.dataholder.Key;
import consulo.util.dataholder.UserDataHolderEx;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Set;
import java.util.function.Function;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class CSharpResolveContextUtil
{
	private static final Key<CachedValue<CSharpResolveContext>> RESOLVE_CONTEXT = Key.create("resolve-context");

	@Nonnull
	@RequiredReadAction
	public static CSharpResolveContext createContext(@Nonnull DotNetGenericExtractor genericExtractor, @Nonnull GlobalSearchScope resolveScope, @Nonnull PsiElement element)
	{
		return createContext(genericExtractor, resolveScope, element, null);
	}

	@Nonnull
	@RequiredReadAction
	public static CSharpResolveContext createContext(@Nonnull DotNetGenericExtractor genericExtractor,
													 @Nonnull GlobalSearchScope resolveScope,
													 @Nonnull PsiElement element,
													 @Nullable Set<PsiElement> recursiveGuardSet)
	{
		if(element instanceof CSharpTypeDeclaration typeDeclaration)
		{
			return cacheTypeContext(genericExtractor, resolveScope, typeDeclaration, recursiveGuardSet);
		}
		else if(element instanceof DotNetNamespaceAsElement namespaceAsElement)
		{
			if(DumbService.isDumb(element.getProject()))
			{
				return CSharpResolveContext.EMPTY;
			}
			return cacheSimple(namespaceAsElement, it -> new CSharpNamespaceResolveContext(it, resolveScope));
		}
		else if(element instanceof CSharpUsingNamespaceStatement || element instanceof CSharpUsingTypeStatement)
		{
			return cacheSimple((CSharpUsingListChild) element, CSharpUsingNamespaceOrTypeResolveContext::new);
		}
		else if(element instanceof CSharpTypeDefStatement typeDefStatement)
		{
			return cacheSimple(typeDefStatement, CSharpTypeDefResolveContext::new);
		}
		else if(element instanceof DotNetGenericParameter genericParameter)
		{
			return cacheSimple(genericParameter, CSharpGenericParameterResolveContext::new);
		}
		return CSharpResolveContext.EMPTY;
	}

	@Nonnull
	@RequiredReadAction
	private static CSharpResolveContext cacheTypeContext(@Nonnull DotNetGenericExtractor genericExtractor,
														 GlobalSearchScope resolveScope,
														 @Nonnull CSharpTypeDeclaration typeDeclaration,
														 @Nullable Set<PsiElement> recursiveGuardSet)
	{
		if(typeDeclaration.hasModifier(CSharpModifier.PARTIAL))
		{
			String vmQName = typeDeclaration.getVmQName();
			if(vmQName != null)
			{
				DotNetTypeDeclaration[] types = DotNetPsiSearcher.getInstance(typeDeclaration.getProject()).findTypes(vmQName, resolveScope);

				for(DotNetTypeDeclaration type : types)
				{
					if(type instanceof CSharpCompositeTypeDeclaration)
					{
						typeDeclaration = (CSharpTypeDeclaration) type;
						break;
					}
				}
			}
		}

		return cacheTypeContextImpl(genericExtractor, typeDeclaration, recursiveGuardSet);
	}

	@Nonnull
	@RequiredReadAction
	private static CSharpResolveContext cacheTypeContextImpl(@Nonnull DotNetGenericExtractor genericExtractor,
															 @Nonnull CSharpTypeDeclaration typeDeclaration,
															 @Nullable Set<PsiElement> recursiveGuardSet)
	{
		if(genericExtractor == DotNetGenericExtractor.EMPTY)
		{
			CachedValue<CSharpResolveContext> provider = typeDeclaration.getUserData(RESOLVE_CONTEXT);
			if(provider != null)
			{
				return provider.getValue();
			}

			CachedValuesManager manager = CachedValuesManager.getManager(typeDeclaration.getProject());
			CachedValue<CSharpResolveContext> cachedValue = manager.createCachedValue(() -> CachedValueProvider.Result
					.<CSharpResolveContext>create(new CSharpTypeResolveContext(typeDeclaration, DotNetGenericExtractor.EMPTY, null), PsiModificationTracker.MODIFICATION_COUNT), false);

			CachedValue<CSharpResolveContext> result = ((UserDataHolderEx) typeDeclaration).putUserDataIfAbsent(RESOLVE_CONTEXT, cachedValue);
			return result.getValue();
		}
		else
		{
			return new CSharpTypeResolveContext(typeDeclaration, genericExtractor, recursiveGuardSet);
		}
	}

	@Nonnull
	private static <T extends PsiElement> CSharpResolveContext cacheSimple(@Nonnull T element, @RequiredReadAction Function<T, CSharpResolveContext> fun)
	{
		CachedValue<CSharpResolveContext> provider = element.getUserData(RESOLVE_CONTEXT);
		if(provider != null)
		{
			return provider.getValue();
		}

		CachedValuesManager manager = CachedValuesManager.getManager(element.getProject());
		CachedValue<CSharpResolveContext> cachedValue = manager.createCachedValue(() -> CachedValueProvider.Result.create(fun.apply(element),
				PsiModificationTracker.MODIFICATION_COUNT), false);

		CachedValue<CSharpResolveContext> result = ((UserDataHolderEx) element).putUserDataIfAbsent(RESOLVE_CONTEXT, cachedValue);
		return result.getValue();
	}
}
