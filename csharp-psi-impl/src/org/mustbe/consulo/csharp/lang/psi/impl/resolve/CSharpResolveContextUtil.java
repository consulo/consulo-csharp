package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.Set;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingList;
import org.mustbe.consulo.csharp.lang.psi.impl.partial.CSharpCompositeTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.NotNullFunction;

/**
 * @author VISTALL
 * @since 07.10.14
 */
@Logger
public class CSharpResolveContextUtil
{
	private static final Key<CachedValue<CSharpResolveContext>> RESOLVE_CONTEXT = Key.create("resolve-context");

	@NotNull
	@RequiredReadAction
	public static CSharpResolveContext createContext(@NotNull DotNetGenericExtractor genericExtractor, @NotNull GlobalSearchScope resolveScope, @NotNull PsiElement... elements)
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
	@RequiredReadAction
	public static CSharpResolveContext createContext(@NotNull DotNetGenericExtractor genericExtractor, @NotNull GlobalSearchScope resolveScope, @NotNull PsiElement element)
	{
		return createContext(genericExtractor, resolveScope, element, null);
	}

	@NotNull
	@RequiredReadAction
	public static CSharpResolveContext createContext(@NotNull DotNetGenericExtractor genericExtractor,
			@NotNull GlobalSearchScope resolveScope,
			@NotNull PsiElement element,
			@Nullable Set<PsiElement> recursiveGuardSet)
	{
		if(element instanceof CSharpTypeDeclaration)
		{
			return cacheTypeContext(genericExtractor, resolveScope, (CSharpTypeDeclaration) element, recursiveGuardSet);
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
			return cacheSimple((DotNetGenericParameter) element, new NotNullFunction<DotNetGenericParameter, CSharpResolveContext>()
			{
				@NotNull
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

	@NotNull
	@RequiredReadAction
	private static CSharpResolveContext cacheTypeContext(@NotNull DotNetGenericExtractor genericExtractor,
			GlobalSearchScope resolveScope,
			@NotNull CSharpTypeDeclaration typeDeclaration,
			@Nullable Set<PsiElement> recursiveGuardSet)
	{
		if(typeDeclaration.hasModifier(CSharpModifier.PARTIAL))
		{
			String vmQName = typeDeclaration.getVmQName();
			assert vmQName != null;
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

		return cacheTypeContextImpl(genericExtractor, typeDeclaration, recursiveGuardSet);
	}

	@NotNull
	@RequiredReadAction
	private static CSharpResolveContext cacheTypeContextImpl(@NotNull DotNetGenericExtractor genericExtractor,
			@NotNull final CSharpTypeDeclaration typeDeclaration,
			@Nullable final Set<PsiElement> recursiveGuardSet)
	{
		if(genericExtractor == DotNetGenericExtractor.EMPTY && recursiveGuardSet == null)
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
					return Result.<CSharpResolveContext>create(new CSharpTypeResolveContext(typeDeclaration, DotNetGenericExtractor.EMPTY, null), typeDeclaration);
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

	@NotNull
	private static <T extends PsiElement> CSharpResolveContext cacheSimple(@NotNull final T element, final NotNullFunction<T, CSharpResolveContext> fun)
	{
		CachedValue<CSharpResolveContext> provider = element.getUserData(RESOLVE_CONTEXT);
		if(provider != null)
		{
			return provider.getValue();
		}

		CachedValue<CSharpResolveContext> cachedValue = CachedValuesManager.getManager(element.getProject()).createCachedValue(new CachedValueProvider<CSharpResolveContext>()
		{
			@Nullable
			@Override
			public Result<CSharpResolveContext> compute()
			{
				return Result.create(fun.fun(element), element);
			}
		}, false);
		element.putUserData(RESOLVE_CONTEXT, cachedValue);
		return cachedValue.getValue();
	}
}
