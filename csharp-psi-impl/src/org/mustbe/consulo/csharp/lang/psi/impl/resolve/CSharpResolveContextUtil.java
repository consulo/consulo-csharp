package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingList;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.NotNullFunction;
import com.intellij.util.containers.ContainerUtil;

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
			return cacheTypeContext(genericExtractor, resolveScope, (CSharpTypeDeclaration) element);
		}
		else if(element instanceof DotNetNamespaceAsElement)
		{
			return new CSharpNamespaceResolveContext((DotNetNamespaceAsElement) element, resolveScope);
		}
		else if(element instanceof CSharpUsingList)
		{
			return cacheSimple((CSharpUsingList) element, new NotNullFunction<CSharpUsingList, CSharpResolveContext>()
			{
				@NotNull
				@Override
				public CSharpResolveContext fun(CSharpUsingList usingList)
				{
					return new CSharpUsingListResolveContext(usingList);
				}
			});
		}
		else if(element instanceof DotNetGenericParameter)
		{
			return cacheSimple((DotNetGenericParameter)element, new NotNullFunction<DotNetGenericParameter, CSharpResolveContext>()
			{
				@NotNull
				@Override
				public CSharpResolveContext fun(DotNetGenericParameter element)
				{
					return new CSharpGenericParameterResolveContext(element);
				}
			});
		}
		return CSharpResolveContext.EMPTY;
	}

	@NotNull
	private static CSharpResolveContext cacheTypeContext(@NotNull DotNetGenericExtractor genericExtractor,
			GlobalSearchScope resolveScope,
			@NotNull final CSharpTypeDeclaration typeDeclaration)
	{
		if(typeDeclaration.hasModifier(CSharpModifier.PARTIAL))
		{
			DotNetTypeDeclaration[] types = DotNetPsiSearcher.getInstance(typeDeclaration.getProject()).findTypes(typeDeclaration.getVmQName(),
					resolveScope);

			List<CSharpResolveContext> list = new ArrayList<CSharpResolveContext>(types.length);
			for(DotNetTypeDeclaration element : types)
			{
				if(!(element instanceof CSharpTypeDeclaration))
				{
					continue;
				}
				list.add(cacheTypeContextImpl(genericExtractor, (CSharpTypeDeclaration) element));
			}
			return new CSharpCompositeResolveContext(types[0].getProject(), ContainerUtil.toArray(list, CSharpResolveContext.ARRAY_FACTORY));
		}

		return cacheTypeContextImpl(genericExtractor, typeDeclaration);
	}

	@NotNull
	private static CSharpResolveContext cacheTypeContextImpl(@NotNull DotNetGenericExtractor genericExtractor,
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
	private static <T extends PsiElement> CSharpResolveContext cacheSimple(@NotNull final T element,
			final NotNullFunction<T, CSharpResolveContext> fun)
	{
		CachedValue<CSharpResolveContext> provider = element.getUserData(RESOLVE_CONTEXT);
		if(provider != null)
		{
			return provider.getValue();
		}

		CachedValue<CSharpResolveContext> cachedValue = CachedValuesManager.getManager(element.getProject()).createCachedValue(new
																																	   CachedValueProvider<CSharpResolveContext>()
		{
			@Nullable
			@Override
			public Result<CSharpResolveContext> compute()
			{
				return Result.create(fun.fun(element), element, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
			}
		}, false);
		element.putUserData(RESOLVE_CONTEXT, cachedValue);
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
