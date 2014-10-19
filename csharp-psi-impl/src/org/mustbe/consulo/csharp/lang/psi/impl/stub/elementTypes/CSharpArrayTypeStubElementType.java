package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayTypeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpTypeWithIntValueStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

/**
 * @author VISTALL
 * @since 19.10.14
 */
public class CSharpArrayTypeStubElementType extends CSharpAbstractStubElementType<CSharpTypeWithIntValueStub, CSharpArrayTypeImpl>
{
	public CSharpArrayTypeStubElementType()
	{
		super("ARRAY_TYPE");
	}

	@NotNull
	@Override
	public PsiElement createElement(@NotNull ASTNode astNode)
	{
		return new CSharpArrayTypeImpl(astNode);
	}

	@Override
	public CSharpArrayTypeImpl createPsi(@NotNull CSharpTypeWithIntValueStub stub)
	{
		return new CSharpArrayTypeImpl(stub, this);
	}

	@Override
	public CSharpTypeWithIntValueStub createStub(@NotNull CSharpArrayTypeImpl cSharpArrayType, StubElement stubElement)
	{
		return new CSharpTypeWithIntValueStub(stubElement, this, cSharpArrayType.getDimensions());
	}

	@Override
	public void serialize(@NotNull CSharpTypeWithIntValueStub stub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeVarInt(stub.getValue());
	}

	@NotNull
	@Override
	public CSharpTypeWithIntValueStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		int i = stubInputStream.readVarInt();
		return new CSharpTypeWithIntValueStub(stubElement, this, i);
	}
}
