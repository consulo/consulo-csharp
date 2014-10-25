package org.mustbe.consulo.csharp.lang.psi.impl.stub;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;

/**
 * @author VISTALL
 * @since 19.10.14
 */
public class CSharpWithIntValueStub<T extends PsiElement> extends StubBase<T>
{
	private final int myValue;

	public CSharpWithIntValueStub(StubElement parent, IStubElementType elementType, int value)
	{
		super(parent, elementType);
		myValue = value;
	}

	public int getValue()
	{
		return myValue;
	}
}
