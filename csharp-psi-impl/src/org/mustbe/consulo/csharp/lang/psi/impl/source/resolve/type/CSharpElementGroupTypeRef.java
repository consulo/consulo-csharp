package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class CSharpElementGroupTypeRef extends DotNetTypeRef.Adapter
{
	private final CSharpElementGroup<?> myElementGroup;

	public CSharpElementGroupTypeRef(CSharpElementGroup<?> elementGroup)
	{
		myElementGroup = elementGroup;
	}

	@NotNull
	@Override
	public String getPresentableText()
	{
		return myElementGroup.getName();
	}

	@NotNull
	@Override
	public String getQualifiedText()
	{
		return myElementGroup.getName();
	}
}
