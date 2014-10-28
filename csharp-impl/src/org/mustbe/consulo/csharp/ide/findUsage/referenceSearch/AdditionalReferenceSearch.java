package org.mustbe.consulo.csharp.ide.findUsage.referenceSearch;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class AdditionalReferenceSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>
{
	@Override
	public void processQuery(@NotNull ReferencesSearch.SearchParameters queryParameters, @NotNull Processor<PsiReference> consumer)
	{
		PsiElement elementToSearch = queryParameters.getElementToSearch();

		PsiElement declaration = elementToSearch.getUserData(CSharpResolveUtil.EXTENSION_METHOD_WRAPPER);
		if(declaration == null)
		{
			declaration = elementToSearch.getUserData(CSharpResolveUtil.ACCESSOR_VALUE_VARIABLE_OWNER);
		}

		if(declaration == null)
		{
			return;
		}

		ReferencesSearch.search(declaration, queryParameters.getEffectiveSearchScope(), queryParameters.isIgnoreAccessScope()).forEach(consumer);
	}
}
