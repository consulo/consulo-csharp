package org.mustbe.consulo.csharp.ide.findUsage.referenceSearch;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
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
public class ExtensionMethodReferenceSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>
{
	@Override
	public void processQuery(@NotNull ReferencesSearch.SearchParameters queryParameters, @NotNull Processor<PsiReference> consumer)
	{
		PsiElement elementToSearch = queryParameters.getElementToSearch();

		CSharpMethodDeclaration declaration = elementToSearch.getUserData(CSharpResolveUtil.EXTENSION_METHOD_WRAPPER);
		if(declaration != null)
		{
			ReferencesSearch.search(declaration, queryParameters.getEffectiveSearchScope(), queryParameters.isIgnoreAccessScope()).forEach(consumer);
		}
	}
}
