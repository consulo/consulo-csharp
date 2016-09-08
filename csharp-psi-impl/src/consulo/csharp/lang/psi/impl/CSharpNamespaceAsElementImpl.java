package consulo.csharp.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import consulo.csharp.lang.CSharpLanguage;
import consulo.dotnet.lang.psi.impl.IndexBasedDotNetNamespaceAsElement;
import consulo.dotnet.resolve.impl.IndexBasedDotNetPsiSearcher;
import com.intellij.openapi.project.Project;

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
}
