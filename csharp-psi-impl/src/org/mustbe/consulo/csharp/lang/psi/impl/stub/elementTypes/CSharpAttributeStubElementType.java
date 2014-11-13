package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpAttributeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpEmptyStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 17.10.14
 */
public class CSharpAttributeStubElementType extends CSharpEmptyStubElementType<CSharpAttribute>
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
	public CSharpAttribute createPsi(@NotNull CSharpEmptyStub<CSharpAttribute> stub)
	{
		return new CSharpAttributeImpl(stub, this);
	}
}
