package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpReferenceExpressionStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 13.11.14
 */
public class CSharpReferenceExpressionStubElementType extends CSharpAbstractStubElementType<CSharpReferenceExpressionStub, CSharpReferenceExpression>
{
	public CSharpReferenceExpressionStubElementType()
	{
		super("REFERENCE_EXPRESSION");
	}

	@Override
	public boolean shouldCreateStub(ASTNode node)
	{
		return shouldCreateStubImpl(node);
	}

	public static boolean shouldCreateStubImpl(ASTNode node)
	{
		ASTNode parent = node.getTreeParent();
		if(parent != null)
		{
			while(parent != null && parent.getElementType() == CSharpStubElements.REFERENCE_EXPRESSION)
			{
				parent = parent.getTreeParent();
			}

			if(parent == null)
			{
				return false;
			}
			if(CSharpStubElements.TYPE_SET.contains(parent.getElementType()))
			{
				if(CSharpStubTypeUtil.shouldCreateStub(parent))
				{
					return true;
				}
			}
		}
		return false;
	}

	@NotNull
	@Override
	public CSharpReferenceExpression createElement(@NotNull ASTNode astNode)
	{
		return new CSharpReferenceExpressionImpl(astNode);
	}

	@Override
	public CSharpReferenceExpression createPsi(@NotNull CSharpReferenceExpressionStub stub)
	{
		return new CSharpReferenceExpressionImpl(stub, this);
	}

	@Override
	public CSharpReferenceExpressionStub createStub(@NotNull CSharpReferenceExpression psi, StubElement parentStub)
	{
		String referenceName = psi.getReferenceName();
		CSharpReferenceExpression.ResolveToKind kind = psi.kind();
		return new CSharpReferenceExpressionStub(parentStub, this, referenceName, kind.ordinal());
	}

	@Override
	public void serialize(@NotNull CSharpReferenceExpressionStub stub, @NotNull StubOutputStream dataStream) throws IOException
	{
		dataStream.writeName(stub.getReferenceText());
		dataStream.writeVarInt(stub.getKindOrdinal());
	}

	@NotNull
	@Override
	public CSharpReferenceExpressionStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException
	{
		StringRef referenceName = dataStream.readName();
		int kind = dataStream.readVarInt();
		return new CSharpReferenceExpressionStub(parentStub, this, referenceName, kind);
	}
}
