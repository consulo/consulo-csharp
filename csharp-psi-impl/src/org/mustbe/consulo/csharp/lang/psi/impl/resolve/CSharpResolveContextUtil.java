package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingList;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class CSharpResolveContextUtil
{
	private static final Key<CachedValue<CSharpResolveContext>> RESOLVE_CONTEXT = Key.create("resolve-context");

	@NotNull
	public static CSharpResolveContext createContext(@NotNull DotNetGenericExtractor genericExtractor,
			@NotNull GlobalSearchScope resolveScope,
			@NotNull PsiElement... elements)
	{
		if(elements.length == 0)
		{
			throw new IllegalArgumentException();
		}

		CSharpResolveContext[] array = new CSharpResolveContext[elements.length];
		for(int i = 0; i < elements.length; i++)
		{
			PsiElement element = elements[i];
			array[i] = createContext(genericExtractor, resolveScope, element);
		}
		return new CSharpCompositeResolveContext(elements[0].getProject(), array);
	}

	@NotNull
	public static CSharpResolveContext createContext(@NotNull DotNetGenericExtractor genericExtractor,
			@NotNull GlobalSearchScope resolveScope,
			@NotNull PsiElement element)
	{
		if(element instanceof CSharpTypeDeclaration)
		{
			return cacheTypeContext(genericExtractor, (CSharpTypeDeclaration) element);
		}
		else if(element instanceof DotNetNamespaceAsElement)
		{
			return new CSharpNamespaceResolveContext((DotNetNamespaceAsElement) element, resolveScope);
		}
		else if(element instanceof CSharpUsingList)
		{
			return cacheUsingContext((CSharpUsingList) element);
		}
		return CSharpResolveContext.EMPTY;
	}

	@NotNull
	private static CSharpResolveContext cacheTypeContext(@NotNull DotNetGenericExtractor genericExtractor,
			@NotNull final CSharpTypeDeclaration typeDeclaration)
	{
		if(genericExtractor == DotNetGenericExtractor.EMPTY)
		{
			CachedValue<CSharpResolveContext> provider = typeDeclaration.getUserData(RESOLVE_CONTEXT);
			if(provider != null)
			{
				return provider.getValue();
			}

			CachedValue<CSharpResolveContext> cachedValue = CachedValuesManager.getManager(typeDeclaration.getProject()).createCachedValue(new
																																				   CachedValueProvider<CSharpResolveContext>()
			{
				@Nullable
				@Override
				public Result<CSharpResolveContext> compute()
				{
					return Result.<CSharpResolveContext>create(new CSharpTypeResolveContext(typeDeclaration, DotNetGenericExtractor.EMPTY),
							typeDeclaration, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
				}
			}, false);
			typeDeclaration.putUserData(RESOLVE_CONTEXT, cachedValue);
			return cachedValue.getValue();
		}
		else
		{
			return new CSharpTypeResolveContext(typeDeclaration, genericExtractor);
		}
	}

	@NotNull
	private static CSharpResolveContext cacheUsingContext(@NotNull final CSharpUsingList usingList)
	{
		CachedValue<CSharpResolveContext> provider = usingList.getUserData(RESOLVE_CONTEXT);
		if(provider != null)
		{
			return provider.getValue();
		}

		CachedValue<CSharpResolveContext> cachedValue = CachedValuesManager.getManager(usingList.getProject()).createCachedValue(new
																																		 CachedValueProvider<CSharpResolveContext>()
		{
			@Nullable
			@Override
			public Result<CSharpResolveContext> compute()
			{
				return Result.<CSharpResolveContext>create(new CSharpUsingListResolveContext(usingList), usingList,
						PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
			}
		}, false);
		usingList.putUserData(RESOLVE_CONTEXT, cachedValue);
		return cachedValue.getValue();
	}

	@Nullable
	public static PsiElement findValidWithGeneric(UserDataHolder holder, PsiElement[] elements)
	{
		Integer expectedGenericCount = holder.getUserData(CSharpResolveContext.GENERIC_COUNT);
		if(expectedGenericCount != null)
		{
			for(PsiElement element : elements)
			{
				if(element instanceof DotNetGenericParameterListOwner)
				{
					int genericParametersCount = ((DotNetGenericParameterListOwner) element).getGenericParametersCount();
					if(genericParametersCount == expectedGenericCount)
					{
						return element;
					}
				}
			}
		}
		return null;
	}
}
