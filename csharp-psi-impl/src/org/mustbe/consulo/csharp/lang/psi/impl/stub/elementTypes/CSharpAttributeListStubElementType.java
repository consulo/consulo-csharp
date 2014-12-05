package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttributeList;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpStubAttributeListImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpAttributeListStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

/**
 * @author VISTALL
 * @since 17.10.14
 */
public class CSharpAttributeListStubElementType extends CSharpAbstractStubElementType<CSharpAttributeListStub, CSharpAttributeList>
{
	public CSharpAttributeListStubElementType()
	{
		super("ATTRIBUTE_LIST");
	}

	@NotNull
	@Override
	public PsiElement createElement(@NotNull ASTNode astNode)
	{
		return new CSharpStubAttributeListImpl(astNode);
	}

	@Override
	public CSharpAttributeList createPsi(@NotNull CSharpAttributeListStub stub)
	{
		return new CSharpStubAttributeListImpl(stub, this);
	}

	@Override
	public CSharpAttributeListStub createStub(@NotNull CSharpAttributeList attributeList, StubElement stubElement)
	{
		return new CSharpAttributeListStub(stubElement, this);
	}

	@Override
	public void serialize(@NotNull CSharpAttributeListStub cSharpAttributeListStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{

	}

	@NotNull
	@Override
	public CSharpAttributeListStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		return new CSharpAttributeListStub(stubElement, this);
	}
}
