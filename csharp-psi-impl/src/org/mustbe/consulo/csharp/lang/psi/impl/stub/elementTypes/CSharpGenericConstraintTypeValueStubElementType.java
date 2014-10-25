package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintTypeValue;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpGenericConstraintTypeValueImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpEmptyStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 25.10.14
 */
public class CSharpGenericConstraintTypeValueStubElementType extends CSharpEmptyStubElementType<CSharpGenericConstraintTypeValue>
{
	public CSharpGenericConstraintTypeValueStubElementType()
	{
		super("GENERIC_CONSTRAINT_TYPE_VALUE");
	}

	@NotNull
	@Override
	public PsiElement createElement(@NotNull ASTNode astNode)
	{
		return new CSharpGenericConstraintTypeValueImpl(astNode);
	}

	@Override
	public CSharpGenericConstraintTypeValue createPsi(@NotNull CSharpEmptyStub<CSharpGenericConstraintTypeValue> stub)
	{
		return new CSharpGenericConstraintTypeValueImpl(stub, this);
	}
}
