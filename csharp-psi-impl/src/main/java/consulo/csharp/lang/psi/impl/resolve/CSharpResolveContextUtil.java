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

package consulo.csharp.lang.psi.impl.resolve;

import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.NotNullFunction;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.resolve.DotNetPsiSearcher;
import consulo.util.dataholder.Key;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

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
		if(element instanceof CSharpTypeDeclaration)
		{
			return cacheTypeContext(genericExtractor, resolveScope, (CSharpTypeDeclaration) element, recursiveGuardSet);
		}
		else if(element instanceof DotNetNamespaceAsElement)
		{
			if(DumbService.isDumb(element.getProject()))
			{
				return CSharpResolveContext.EMPTY;
			}
			return cacheSimple(element, it -> new CSharpNamespaceResolveContext((DotNetNamespaceAsElement) it, resolveScope));
		}
		else if(element instanceof CSharpUsingNamespaceStatement || element instanceof CSharpUsingTypeStatement)
		{
			return cacheSimple((CSharpUsingListChild) element, CSharpUsingNamespaceOrTypeResolveContext::new);
		}
		else if(element instanceof CSharpTypeDefStatement)
		{
			return cacheSimple((CSharpTypeDefStatement) element, CSharpTypeDefResolveContext::new);
		}
		else if(element instanceof DotNetGenericParameter)
		{
			return cacheSimple((DotNetGenericParameter) element, new NotNullFunction<DotNetGenericParameter, CSharpResolveContext>()
			{
				@Nonnull
				@Override
				@RequiredReadAction
				public CSharpResolveContext fun(DotNetGenericParameter element)
				{
					return new CSharpGenericParameterResolveContext(element);
				}
			});
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
															 @Nonnull final CSharpTypeDeclaration typeDeclaration,
															 @Nullable final Set<PsiElement> recursiveGuardSet)
	{
		if(genericExtractor == DotNetGenericExtractor.EMPTY && (recursiveGuardSet == null || recursiveGuardSet.size() == 1 && recursiveGuardSet.contains(typeDeclaration)))
		{
			CachedValue<CSharpResolveContext> provider = typeDeclaration.getUserData(RESOLVE_CONTEXT);
			if(provider != null)
			{
				return provider.getValue();
			}

			CachedValue<CSharpResolveContext> cachedValue = CachedValuesManager.getManager(typeDeclaration.getProject()).createCachedValue(new CachedValueProvider<CSharpResolveContext>()
			{
				@Nullable
				@Override
				@RequiredReadAction
				public Result<CSharpResolveContext> compute()
				{
					return Result.<CSharpResolveContext>create(new CSharpTypeResolveContext(typeDeclaration, DotNetGenericExtractor.EMPTY, null), PsiModificationTracker.MODIFICATION_COUNT);
				}
			}, false);
			typeDeclaration.putUserData(RESOLVE_CONTEXT, cachedValue);
			return cachedValue.getValue();
		}
		else
		{
			return new CSharpTypeResolveContext(typeDeclaration, genericExtractor, recursiveGuardSet);
		}
	}

	@Nonnull
	private static <T extends PsiElement> CSharpResolveContext cacheSimple(@Nonnull final T element, final NotNullFunction<T, CSharpResolveContext> fun)
	{
		CachedValue<CSharpResolveContext> provider = element.getUserData(RESOLVE_CONTEXT);
		if(provider != null)
		{
			return provider.getValue();
		}

		CachedValue<CSharpResolveContext> cachedValue = CachedValuesManager.getManager(element.getProject()).createCachedValue(() -> CachedValueProvider.Result.create(fun.fun(element),
				PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT), false);
		element.putUserData(RESOLVE_CONTEXT, cachedValue);
		return cachedValue.getValue();
	}
}
