package org.mustbe.consulo.csharp.lang.psi.impl.stub;

import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpNativeTypeImpl;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;

/**
 * @author VISTALL
 * @since 19.10.14
 */
public class CSharpTypeWithIntValueStub extends StubBase<CSharpNativeTypeImpl>
{
	private final int myValue;

	public CSharpTypeWithIntValueStub(StubElement parent, IStubElementType elementType, int value)
	{
		super(parent, elementType);
		myValue = value;
	}

	public int getValue()
	{
		return myValue;
	}
}
