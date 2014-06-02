package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 11.02.14
 */
public class CSharpOutRefWrapExpressionImpl extends CSharpElementImpl implements DotNetExpression
{
	private static final TokenSet ourStartTypes = TokenSet.create(CSharpTokens.OUT_KEYWORD, CSharpTokens.REF_KEYWORD);

	public CSharpOutRefWrapExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitOurRefWrapExpression(this);
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		DotNetExpression innerExpression = getInnerExpression();
		if(innerExpression == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		DotNetTypeRef typeRef = innerExpression.toTypeRef(resolveFromParent);
		PsiElement startElement = getStartElement();
		CSharpRefTypeRef.Type type = CSharpRefTypeRef.Type.ref;
		if(startElement.getNode().getElementType() == CSharpTokens.OUT_KEYWORD)
		{
			type = CSharpRefTypeRef.Type.out;
		}
		return new CSharpRefTypeRef(type, typeRef);
	}

	@NotNull
	public PsiElement getStartElement()
	{
		return findNotNullChildByFilter(ourStartTypes);
	}

	@Nullable
	public DotNetExpression getInnerExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}
}
