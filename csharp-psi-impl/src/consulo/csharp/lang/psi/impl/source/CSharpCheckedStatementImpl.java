package consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.lang.ASTNode;
import consulo.dotnet.psi.DotNetStatement;

/**
 * @author VISTALL
 * @since 11.02.14
 */
public class CSharpCheckedStatementImpl extends CSharpElementImpl implements DotNetStatement
{
	public CSharpCheckedStatementImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public boolean isUnchecked()
	{
		return findChildByType(CSharpTokens.UNCHECKED_KEYWORD) != null;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitCheckedStatement(this);
	}
}
