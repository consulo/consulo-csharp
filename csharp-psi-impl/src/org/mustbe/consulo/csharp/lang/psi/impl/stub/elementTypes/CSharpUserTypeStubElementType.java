package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpUserType;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpStubUserTypeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpWithStringValueStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 19.10.14
 */
public class CSharpUserTypeStubElementType extends CSharpAbstractStubElementType<CSharpWithStringValueStub<CSharpUserType>,
		CSharpUserType>
{
	public CSharpUserTypeStubElementType()
	{
		super("USER_TYPE");
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
		return new CSharpStubUserTypeImpl(astNode);
	}

	@Override
	public CSharpUserType createPsi(@NotNull CSharpWithStringValueStub<CSharpUserType> stub)
	{
		return new CSharpStubUserTypeImpl(stub, this);
	}

	@Override
	public CSharpWithStringValueStub<CSharpUserType> createStub(@NotNull CSharpUserType cSharpUserType, StubElement stubElement)
	{

		return new CSharpWithStringValueStub<CSharpUserType>(stubElement, this, cSharpUserType.getReferenceText());
	}

	@Override
	public void serialize(@NotNull CSharpWithStringValueStub<CSharpUserType> stub,
			@NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(stub.getReferenceText());
	}

	@NotNull
	@Override
	public CSharpWithStringValueStub<CSharpUserType> deserialize(@NotNull StubInputStream stubInputStream,
			StubElement stubElement) throws IOException
	{
		StringRef ref = stubInputStream.readName();
		return new CSharpWithStringValueStub<CSharpUserType>(stubElement, this, ref);
	}
}
