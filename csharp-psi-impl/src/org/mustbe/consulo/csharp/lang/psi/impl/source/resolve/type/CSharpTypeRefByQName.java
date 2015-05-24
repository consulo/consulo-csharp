package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.lang.psi.impl.stub.MsilHelper;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class CSharpTypeRefByQName extends DotNetTypeRef.Adapter
{
	@NotNull
	private final String myQualifiedName;

	public CSharpTypeRefByQName(@NotNull String qualifiedName)
	{
		myQualifiedName = qualifiedName;
	}

	@NotNull
	@Override
	public String getQualifiedText()
	{
		return MsilHelper.cutGenericMarker(myQualifiedName);
	}

	@NotNull
	@Override
	public String getPresentableText()
	{
		return StringUtil.getShortName(getQualifiedText());
	}

	@NotNull
	@Override
	public DotNetTypeResolveResult resolve(@NotNull PsiElement scope)
	{
		DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(scope.getProject()).findType(myQualifiedName, scope.getResolveScope(),
				DotNetPsiSearcher.TypeResoleKind.UNKNOWN, CSharpTransform.INSTANCE);

		if(type == null)
		{
			return DotNetTypeResolveResult.EMPTY;
		}

		return new CSharpReferenceTypeRef.Result<DotNetTypeDeclaration>(type, DotNetGenericExtractor.EMPTY);
	}
}
