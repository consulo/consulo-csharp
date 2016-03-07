package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintUtil;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class CSharpGenericParameterResolveContext extends CSharpBaseResolveContext<DotNetGenericParameter>
{
	@RequiredReadAction
	public CSharpGenericParameterResolveContext(@NotNull DotNetGenericParameter element)
	{
		super(element, DotNetGenericExtractor.EMPTY, null);
	}

	@Override
	public void acceptChildren(CSharpElementVisitor visitor)
	{

	}

	@NotNull
	@Override
	protected List<DotNetTypeRef> getExtendTypeRefs()
	{
		return CSharpGenericConstraintUtil.getExtendTypes(myElement);
	}
}
