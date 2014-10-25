package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayTypeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpWithIntValueStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

/**
 * @author VISTALL
 * @since 19.10.14
 */
public class CSharpArrayTypeStubElementType extends CSharpAbstractStubElementType<CSharpWithIntValueStub<CSharpArrayTypeImpl>, CSharpArrayTypeImpl>
{
	public CSharpArrayTypeStubElementType()
	{
		super("ARRAY_TYPE");
	}

	@Override
	public boolean shouldCreateStub(ASTNode node)
	{
		return CSharpStubTypeUtil.shouldCreateStub(node);
	}

	@NotNull
	@Override
	public PsiElement createElement(@NotNull ASTNode astNode)
	{
		return new CSharpArrayTypeImpl(astNode);
	}

	@Override
	public CSharpArrayTypeImpl createPsi(@NotNull CSharpWithIntValueStub<CSharpArrayTypeImpl> stub)
	{
		return new CSharpArrayTypeImpl(stub, this);
	}

	@Override
	public CSharpWithIntValueStub<CSharpArrayTypeImpl> createStub(@NotNull CSharpArrayTypeImpl cSharpArrayType, StubElement stubElement)
	{
		return new CSharpWithIntValueStub<CSharpArrayTypeImpl>(stubElement, this, cSharpArrayType.getDimensions());
	}

	@Override
	public void serialize(@NotNull CSharpWithIntValueStub stub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeVarInt(stub.getValue());
	}

	@NotNull
	@Override
	public CSharpWithIntValueStub<CSharpArrayTypeImpl> deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		int i = stubInputStream.readVarInt();
		return new CSharpWithIntValueStub<CSharpArrayTypeImpl>(stubElement, this, i);
	}
}
