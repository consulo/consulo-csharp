package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class CSharpResolveContextUtil
{
	private static final Key<CachedValue<CSharpResolveContext>> TYPE_RESOLVE_CONTEXT = Key.create("type-resolve-context");

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
			return getOrCreateResolveContext(genericExtractor, (CSharpTypeDeclaration) element);
		}
		else if(element instanceof DotNetNamespaceAsElement)
		{
			return new CSharpNamespaceResolveContext((DotNetNamespaceAsElement) element, resolveScope);
		}
		return CSharpResolveContext.EMPTY;
	}

	@NotNull
	private static CSharpResolveContext getOrCreateResolveContext(@NotNull DotNetGenericExtractor genericExtractor,
			@NotNull final CSharpTypeDeclaration typeDeclaration)
	{
		if(genericExtractor == DotNetGenericExtractor.EMPTY)
		{
			//TODO [VISTALL] caching
			/*CachedValue<CSharpResolveContext> provider = typeDeclaration.getUserData(TYPE_RESOLVE_CONTEXT);
			if(provider != null)
			{
				return provider.getValue();
			}

			CachedValuesManager.getManager(typeDeclaration.getProject()).createCachedValue(new CachedValueProvider<CSharpResolveContext>()
			{
				@Nullable
				@Override
				public Result<CSharpResolveContext> compute()
				{
					return Result.<CSharpResolveContext>create(new CSharpTypeResolveContextImpl(typeDeclaration),
							PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);

				}
			}); */
			return new CSharpTypeResolveContext(typeDeclaration, genericExtractor);
		}
		else
		{
			return new CSharpTypeResolveContext(typeDeclaration, genericExtractor);
		}
	}

}
