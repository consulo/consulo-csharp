package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUserTypeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpTypeWithStringValueStub;
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
public class CSharpUserTypeStubElementType extends CSharpAbstractStubElementType<CSharpTypeWithStringValueStub<CSharpUserTypeImpl>,
		CSharpUserTypeImpl>
{
	public CSharpUserTypeStubElementType()
	{
		super("USER_TYPE");
	}

	@NotNull
	@Override
	public PsiElement createElement(@NotNull ASTNode astNode)
	{
		return new CSharpUserTypeImpl(astNode);
	}

	@Override
	public CSharpUserTypeImpl createPsi(@NotNull CSharpTypeWithStringValueStub<CSharpUserTypeImpl> stub)
	{
		return new CSharpUserTypeImpl(stub, this);
	}

	@Override
	public CSharpTypeWithStringValueStub<CSharpUserTypeImpl> createStub(@NotNull CSharpUserTypeImpl cSharpUserType, StubElement stubElement)
	{

		return new CSharpTypeWithStringValueStub<CSharpUserTypeImpl>(stubElement, this, cSharpUserType.getReferenceText());
	}

	@Override
	public void serialize(@NotNull CSharpTypeWithStringValueStub<CSharpUserTypeImpl> stub,
			@NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(stub.getReferenceText());
	}

	@NotNull
	@Override
	public CSharpTypeWithStringValueStub<CSharpUserTypeImpl> deserialize(@NotNull StubInputStream stubInputStream,
			StubElement stubElement) throws IOException
	{
		StringRef ref = stubInputStream.readName();
		return new CSharpTypeWithStringValueStub<CSharpUserTypeImpl>(stubElement, this, ref);
	}
}
