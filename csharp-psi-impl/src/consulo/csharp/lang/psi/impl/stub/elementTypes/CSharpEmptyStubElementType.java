package consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.EmptyStub;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

/**
 * @author VISTALL
 * @since 19.10.14
 */
public abstract class CSharpEmptyStubElementType<T extends PsiElement> extends CSharpAbstractStubElementType<EmptyStub<T>, T>
{
	public CSharpEmptyStubElementType(@NotNull @NonNls String debugName)
	{
		super(debugName);
	}

	@RequiredReadAction
	@Override
	public EmptyStub<T> createStub(@NotNull T type, StubElement stubElement)
	{
		return new EmptyStub<T>(stubElement, this);
	}

	@Override
	public void serialize(@NotNull EmptyStub cSharpEmptyStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{

	}

	@NotNull
	@Override
	public EmptyStub<T> deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		return new EmptyStub<T>(stubElement, this);
	}
}
