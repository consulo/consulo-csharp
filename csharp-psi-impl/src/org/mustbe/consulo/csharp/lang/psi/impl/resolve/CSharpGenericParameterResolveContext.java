package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintUtil;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;

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

	@RequiredReadAction
	@NotNull
	@Override
	protected List<DotNetTypeRef> getExtendTypeRefs()
	{
		return Arrays.asList(CSharpGenericConstraintUtil.getExtendTypes(myElement));
	}
}
