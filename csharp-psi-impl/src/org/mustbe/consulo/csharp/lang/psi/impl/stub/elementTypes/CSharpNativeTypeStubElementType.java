package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpNativeType;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpStubNativeTypeImpl;
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
public class CSharpNativeTypeStubElementType extends CSharpAbstractStubElementType<CSharpWithIntValueStub<CSharpNativeType>, CSharpNativeType>
{
	public CSharpNativeTypeStubElementType()
	{
		super("NATIVE_TYPE");
	}

	@NotNull
	@Override
	public PsiElement createElement(@NotNull ASTNode astNode)
	{
		return new CSharpStubNativeTypeImpl(astNode);
	}

	@Override
	public CSharpNativeType createPsi(@NotNull CSharpWithIntValueStub<CSharpNativeType> stub)
	{
		return new CSharpStubNativeTypeImpl(stub, this);
	}

	@Override
	public CSharpWithIntValueStub<CSharpNativeType> createStub(@NotNull CSharpNativeType cSharpNativeType, StubElement stubElement)
	{
		int index = ArrayUtil.indexOf(CSharpTokenSets.NATIVE_TYPES_AS_ARRAY, cSharpNativeType.getTypeElementType());
		assert index != -1;
		return new CSharpWithIntValueStub<CSharpNativeType>(stubElement, this, index);
	}

	@Override
	public void serialize(@NotNull CSharpWithIntValueStub<CSharpNativeType> stub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeVarInt(stub.getValue());
	}

	@NotNull
	@Override
	public CSharpWithIntValueStub<CSharpNativeType> deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		int index = stubInputStream.readVarInt();
		return new CSharpWithIntValueStub<CSharpNativeType>(stubElement, this, index);
	}
}
