package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Pair;

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

		DotNetTypeRef typeRef = innerExpression.toTypeRef(b);
		Pair<DotNetTypeDeclaration, DotNetGenericExtractor> typeInSuper = CSharpTypeUtil.findTypeInSuper(typeRef, "System.Threading.Tasks.Task", this);
		if(typeInSuper != null)
		{
			return new DotNetTypeRefByQName(DotNetTypes.System.Void, CSharpTransform.INSTANCE, false);
		}

		typeInSuper = CSharpTypeUtil.findTypeInSuper(typeRef, "System.Threading.Tasks.Task`1", this);
		if(typeInSuper != null)
		{
			DotNetTypeRef extract = typeInSuper.getSecond().extract(typeInSuper.getFirst().getGenericParameters()[0]);
			assert extract != null;
			return extract;
		}
		return DotNetTypeRef.ERROR_TYPE;
	}

	@Nullable
	public DotNetExpression getInnerExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}
}
