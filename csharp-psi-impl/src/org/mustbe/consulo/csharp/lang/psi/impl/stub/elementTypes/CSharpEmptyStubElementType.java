package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpEmptyStub;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

/**
 * @author VISTALL
 * @since 19.10.14
 */
public abstract class CSharpEmptyStubElementType<T extends PsiElement> extends CSharpAbstractStubElementType<CSharpEmptyStub<T>, T>
{
	public CSharpEmptyStubElementType(@NotNull @NonNls String debugName)
	{
		super(debugName);
	}

	@Override
	public CSharpEmptyStub<T> createStub(@NotNull T type, StubElement stubElement)
	{
		return new CSharpEmptyStub<T>(stubElement, this);
	}

	@Override
	public void serialize(@NotNull CSharpEmptyStub cSharpEmptyStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{

	}

	@NotNull
	@Override
	public CSharpEmptyStub<T> deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		return new CSharpEmptyStub<T>(stubElement, this);
	}
}
