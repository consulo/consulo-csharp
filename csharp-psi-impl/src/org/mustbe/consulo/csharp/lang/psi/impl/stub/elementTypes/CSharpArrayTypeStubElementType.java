package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayType;
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
public class CSharpArrayTypeStubElementType extends CSharpAbstractStubElementType<CSharpWithIntValueStub<CSharpArrayType>, CSharpArrayType>
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
	public CSharpArrayType createPsi(@NotNull CSharpWithIntValueStub<CSharpArrayType> stub)
	{
		return new CSharpArrayTypeImpl(stub, this);
	}

	@Override
	public CSharpWithIntValueStub<CSharpArrayType> createStub(@NotNull CSharpArrayType cSharpArrayType, StubElement stubElement)
	{
		return new CSharpWithIntValueStub<CSharpArrayType>(stubElement, this, cSharpArrayType.getDimensions());
	}

	@Override
	public void serialize(@NotNull CSharpWithIntValueStub stub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeVarInt(stub.getValue());
	}

	@NotNull
	@Override
	public CSharpWithIntValueStub<CSharpArrayType> deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		int i = stubInputStream.readVarInt();
		return new CSharpWithIntValueStub<CSharpArrayType>(stubElement, this, i);
	}
}
