package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetShortNameSearcher;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;
import com.intellij.util.indexing.IdFilter;

/**
 * @author VISTALL
 * @since 08.07.2015
 */
public class CSharpShortNameSearcher extends DotNetShortNameSearcher
{
	public CSharpShortNameSearcher(Project project)
	{
		super(project);
	}

	@Override
	public void collectTypeNames(@NotNull Processor<String> processor, @NotNull GlobalSearchScope searchScope, @Nullable IdFilter filter)
	{
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.TYPE_INDEX, processor, searchScope, filter);
	}

	@Override
	public void collectTypes(@NotNull String s,
			@NotNull GlobalSearchScope searchScope,
			@Nullable IdFilter filter,
			@NotNull Processor<DotNetTypeDeclaration> processor)
	{
		StubIndex.getInstance().processElements(CSharpIndexKeys.TYPE_INDEX, s, myProject, searchScope, filter, CSharpTypeDeclaration.class,
				processor);
	}
}
