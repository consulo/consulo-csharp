package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.consulo.dotnet.resolve.SimpleTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 23.10.14
 */
public class CSharpNullTypeRef implements DotNetTypeRef
{
	public static final CSharpNullTypeRef INSTANCE = new CSharpNullTypeRef();

	@NotNull
	@Override
	public String getPresentableText()
	{
		return "null";
	}

	@NotNull
	@Override
	public String getQualifiedText()
	{
		return getPresentableText();
	}

	@NotNull
	@Override
	@RequiredReadAction
	public DotNetTypeResolveResult resolve(@NotNull PsiElement element)
	{
		DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(element.getProject()).findType(DotNetTypes.System.Object, element.getResolveScope(), CSharpTransform.INSTANCE);
		if(type == null)
		{
			return DotNetTypeResolveResult.EMPTY;
		}
		return new SimpleTypeResolveResult(type, DotNetGenericExtractor.EMPTY);
	}
}
