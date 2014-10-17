package org.mustbe.consulo.csharp.lang.psi.impl.stub;

import org.mustbe.consulo.dotnet.psi.DotNetAttributeList;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;

/**
 * @author VISTALL
 * @since 17.10.14
 */
public class CSharpAttributeListStub extends StubBase<DotNetAttributeList>
{
	public CSharpAttributeListStub(StubElement parent, IStubElementType elementType)
	{
		super(parent, elementType);
	}
}
