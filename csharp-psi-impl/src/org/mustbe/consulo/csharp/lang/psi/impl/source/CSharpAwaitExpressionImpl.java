package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpImplicitReturnModel;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 09.09.14
 */
public class CSharpAwaitExpressionImpl extends CSharpElementImpl implements DotNetExpression
{
	public static final String System_Threading_Tasks_Task = "System.Threading.Tasks.Task";
	public static final String System_Threading_Tasks_Task$1 = "System.Threading.Tasks.Task`1";

	public CSharpAwaitExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
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
