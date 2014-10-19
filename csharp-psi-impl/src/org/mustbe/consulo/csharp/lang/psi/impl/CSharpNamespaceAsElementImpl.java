package org.mustbe.consulo.csharp.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingListImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import org.mustbe.consulo.dotnet.lang.psi.impl.IndexBasedDotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceUtil;
import org.mustbe.consulo.dotnet.resolve.impl.IndexBasedDotNetPsiSearcher;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 23.09.14
 */
public class CSharpNamespaceAsElementImpl extends IndexBasedDotNetNamespaceAsElement
{
	public CSharpNamespaceAsElementImpl(@NotNull Project project,
			@NotNull String indexKey,
			@NotNull String qName,
			@NotNull IndexBasedDotNetPsiSearcher searcher)
	{
		super(project, CSharpLanguage.INSTANCE, indexKey, qName, searcher);
	}

	@Override
	public boolean processDeclarations(@NotNull final PsiScopeProcessor processor,
			@NotNull final ResolveState state,
			final PsiElement lastParent,
			@NotNull final PsiElement place)
	{
		if(!super.processDeclarations(processor, state, lastParent, place))
		{
			return false;
		}

		if(processor.getHint(CSharpResolveUtil.NO_USING_LIST) == Boolean.TRUE)
		{
			return true;
		}

		GlobalSearchScope searchScope = state.get(RESOLVE_SCOPE);
		return StubIndex.getInstance().processElements(CSharpIndexKeys.USING_LIST_INDEX, DotNetNamespaceUtil.getIndexableNamespace(myQName),
				myProject, searchScope, CSharpUsingListImpl.class, new Processor<CSharpUsingListImpl>()
		{
			@Override
			public boolean process(CSharpUsingListImpl usingList)
			{
				return usingList.processDeclarations(processor, state, lastParent, place);
			}
		});
	}
}
