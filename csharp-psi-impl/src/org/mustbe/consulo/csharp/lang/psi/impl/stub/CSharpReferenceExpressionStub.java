package org.mustbe.consulo.csharp.lang.psi.impl.stub;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 13.11.14
 */
public class CSharpReferenceExpressionStub extends CSharpWithStringValueStub<CSharpReferenceExpression>
{
	private final int myKindOrdinal;

	public CSharpReferenceExpressionStub(StubElement parent, IStubElementType elementType, StringRef referenceText, int kindOrdinal)
	{
		super(parent, elementType, referenceText);
		myKindOrdinal = kindOrdinal;
	}

	public CSharpReferenceExpressionStub(StubElement parent, IStubElementType elementType, String referenceText, int kindOrdinal)
	{
		super(parent, elementType, referenceText);
		myKindOrdinal = kindOrdinal;
	}

	public int getKindOrdinal()
	{
		return myKindOrdinal;
	}

	@NotNull
	public CSharpReferenceExpression.ResolveToKind getKind()
	{
		CSharpReferenceExpression.ResolveToKind resolveToKind = ArrayUtil2.safeGet(CSharpReferenceExpression.ResolveToKind.VALUES, myKindOrdinal);
		return resolveToKind == null ? CSharpReferenceExpression.ResolveToKind.ANY_MEMBER : resolveToKind;
	}
}
