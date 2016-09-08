package consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import consulo.csharp.lang.psi.CSharpGenericConstraintKeywordValue;
import consulo.csharp.lang.psi.impl.source.CSharpGenericConstraintKeywordValueImpl;
import consulo.csharp.lang.psi.impl.stub.CSharpWithIntValueStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 25.10.14
 */
public class CSharpGenericConstraintKeywordValueStubElementType extends
		CSharpAbstractStubElementType<CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue>, CSharpGenericConstraintKeywordValue>
{
	public CSharpGenericConstraintKeywordValueStubElementType()
	{
		super("GENERIC_CONSTRAINT_KEYWORD_VALUE");
	}

	@NotNull
	@Override
	public PsiElement createElement(@NotNull ASTNode astNode)
	{
		return new CSharpGenericConstraintKeywordValueImpl(astNode);
	}

	@Override
	public CSharpGenericConstraintKeywordValue createPsi(@NotNull CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue> stub)
	{
		return new CSharpGenericConstraintKeywordValueImpl(stub, this);
	}

	@Override
	public CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue> createStub(@NotNull CSharpGenericConstraintKeywordValue keywordValue,
			StubElement stubElement)
	{
		int index = ArrayUtil.indexOf(CSharpGenericConstraintKeywordValue.KEYWORDS_AS_ARRAY, keywordValue.getKeywordElementType());
		assert index != -1;
		return new CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue>(stubElement, this, index);
	}

	@Override
	public void serialize(@NotNull CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue> stub,
			@NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeVarInt(stub.getValue());
	}

	@NotNull
	@Override
	public CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue> deserialize(@NotNull StubInputStream stubInputStream,
			StubElement stubElement) throws IOException
	{
		int index = stubInputStream.readVarInt();
		return new CSharpWithIntValueStub<CSharpGenericConstraintKeywordValue>(stubElement, this, index);
	}
}
