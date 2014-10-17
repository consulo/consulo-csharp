package org.mustbe.consulo.csharp.lang.psi.impl.stub;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 17.10.14
 */
public class CSharpAttributeStub extends StubBase<CSharpAttribute>
{
	private StringRef myReferenceText;

	public CSharpAttributeStub(StubElement parent, IStubElementType elementType, StringRef referenceText)
	{
		super(parent, elementType);
		myReferenceText = referenceText;
	}

	public CSharpAttributeStub(StubElement parent, IStubElementType elementType, String referenceText)
	{
		super(parent, elementType);
		myReferenceText = StringRef.fromNullableString(referenceText);
	}

	@Nullable
	public String getReferenceText()
	{
		return StringRef.toString(myReferenceText);
	}
}
