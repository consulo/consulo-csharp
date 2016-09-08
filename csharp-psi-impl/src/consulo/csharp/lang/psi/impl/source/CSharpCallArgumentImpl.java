package consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import com.intellij.lang.ASTNode;
import consulo.dotnet.psi.DotNetExpression;

/**
 * @author VISTALL
 * @since 15.09.14
 */
public class CSharpCallArgumentImpl extends CSharpElementImpl implements CSharpCallArgument
{
	public CSharpCallArgumentImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitCallArgument(this);
	}

	@Nullable
	@Override
	public DotNetExpression getArgumentExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}
}
