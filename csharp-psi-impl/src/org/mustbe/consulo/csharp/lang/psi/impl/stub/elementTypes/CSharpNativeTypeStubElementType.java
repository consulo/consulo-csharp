package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpNativeTypeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpWithIntValueStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 19.10.14
 */
public class CSharpNativeTypeStubElementType extends CSharpAbstractStubElementType<CSharpWithIntValueStub<CSharpNativeTypeImpl>, CSharpNativeTypeImpl>
{
	public CSharpNativeTypeStubElementType()
	{
		super("NATIVE_TYPE");
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
		return new CSharpNativeTypeImpl(astNode);
	}

	@Override
	public CSharpNativeTypeImpl createPsi(@NotNull CSharpWithIntValueStub<CSharpNativeTypeImpl> stub)
	{
		return new CSharpNativeTypeImpl(stub, this);
	}

	@Override
	public CSharpWithIntValueStub<CSharpNativeTypeImpl> createStub(@NotNull CSharpNativeTypeImpl cSharpNativeType, StubElement stubElement)
	{
		int index = ArrayUtil.indexOf(CSharpTokenSets.NATIVE_TYPES_AS_ARRAY, cSharpNativeType.getTypeElementType());
		assert index != -1;
		return new CSharpWithIntValueStub<CSharpNativeTypeImpl>(stubElement, this, index);
	}

	@Override
	public void serialize(@NotNull CSharpWithIntValueStub<CSharpNativeTypeImpl> stub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeVarInt(stub.getValue());
	}

	@NotNull
	@Override
	public CSharpWithIntValueStub<CSharpNativeTypeImpl> deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		int index = stubInputStream.readVarInt();
		return new CSharpWithIntValueStub<CSharpNativeTypeImpl>(stubElement, this, index);
	}
}
