package org.mustbe.consulo.csharp.lang.psi.impl.stub;

import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 17.10.14
 */
public class CSharpTypeWithStringValueStub<T extends PsiElement> extends StubBase<T>
{
	private StringRef myReferenceText;

	public CSharpTypeWithStringValueStub(StubElement parent, IStubElementType elementType, StringRef referenceText)
	{
		super(parent, elementType);
		myReferenceText = referenceText;
	}

	public CSharpTypeWithStringValueStub(StubElement parent, IStubElementType elementType, String referenceText)
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
