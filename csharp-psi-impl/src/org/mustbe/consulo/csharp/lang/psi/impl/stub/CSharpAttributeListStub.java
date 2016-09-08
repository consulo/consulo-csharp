package org.mustbe.consulo.csharp.lang.psi.impl.stub;

import org.jetbrains.annotations.NotNull;
import consulo.dotnet.psi.DotNetAttributeList;
import consulo.dotnet.psi.DotNetAttributeTargetType;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;

/**
 * @author VISTALL
 * @since 17.10.14
 */
public class CSharpAttributeListStub extends StubBase<DotNetAttributeList>
{
	private int myTargetIndex;

	public CSharpAttributeListStub(StubElement parent, IStubElementType elementType, int targetIndex)
	{
		super(parent, elementType);
		myTargetIndex = targetIndex;
	}

	@NotNull
	public DotNetAttributeTargetType getTarget()
	{
		return DotNetAttributeTargetType.values()[getTargetIndex()];
	}

	public int getTargetIndex()
	{
		return myTargetIndex;
	}
}
