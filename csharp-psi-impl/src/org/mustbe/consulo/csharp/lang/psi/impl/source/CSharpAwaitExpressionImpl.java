package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpImplicitReturnModel;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 09.09.14
 */
public class CSharpAwaitExpressionImpl extends CSharpElementImpl implements DotNetExpression
{
	public CSharpAwaitExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@NotNull
	public PsiElement getAwaitKeywordElement()
	{
		return findNotNullChildByType(CSharpSoftTokens.AWAIT_KEYWORD);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitAwaitExpression(this);
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean b)
	{
		DotNetExpression innerExpression = getInnerExpression();
		if(innerExpression == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		return CSharpImplicitReturnModel.Async.extractTypeRef(innerExpression.toTypeRef(true), this);
	}

	@Nullable
	public DotNetExpression getInnerExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}
}
