package consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.lang.ASTNode;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 11.02.14
 */
public class CSharpCheckedExpressionImpl extends CSharpExpressionImpl implements DotNetExpression
{
	public CSharpCheckedExpressionImpl(@NotNull ASTNode node)
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
		visitor.visitCheckedExpression(this);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		DotNetExpression innerExpression = getInnerExpression();
		return innerExpression == null ? DotNetTypeRef.ERROR_TYPE : innerExpression.toTypeRef(true);
	}

	@Nullable
	public DotNetExpression getInnerExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}
}
