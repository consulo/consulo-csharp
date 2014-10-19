package org.mustbe.consulo.csharp.lang.psi.impl.stub;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;

/**
 * @author VISTALL
 * @since 19.10.14
 */
public class CSharpEmptyStub<T extends PsiElement> extends StubBase<T>
{
	public CSharpEmptyStub(StubElement parent, IStubElementType elementType)
	{
		super(parent, elementType);
	}
}
