package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpAttributeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpAttributeStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 17.10.14
 */
public class CSharpAttributeStubElementType extends CSharpAbstractStubElementType<CSharpAttributeStub, CSharpAttribute>
{
	public CSharpAttributeStubElementType()
	{
		super("ATTRIBUTE");
	}

	@NotNull
	@Override
	public PsiElement createElement(@NotNull ASTNode astNode)
	{
		return new CSharpAttributeImpl(astNode);
	}

	@Override
	public CSharpAttribute createPsi(@NotNull CSharpAttributeStub stub)
	{
		return new CSharpAttributeImpl(stub, this);
	}

	@Override
	public CSharpAttributeStub createStub(@NotNull CSharpAttribute attribute, StubElement stubElement)
	{
		CSharpReferenceExpression referenceExpression = attribute.getReferenceExpression();
		String referenceText = referenceExpression == null ? null : referenceExpression.getText();
		return new CSharpAttributeStub(stubElement, this, referenceText);
	}

	@Override
	public void serialize(@NotNull CSharpAttributeStub stub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeName(stub.getReferenceText());
	}

	@NotNull
	@Override
	public CSharpAttributeStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		StringRef referenceText = stubInputStream.readName();
		return new CSharpAttributeStub(stubElement, this, referenceText);
	}
}
