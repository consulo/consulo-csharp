package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 01.07.2015
 */
public class CSharpEmptyGenericWrapperTypeRef extends CSharpGenericWrapperTypeRef
{
	public CSharpEmptyGenericWrapperTypeRef(DotNetTypeRef innerTypeRef)
	{
		super(innerTypeRef, EMPTY_ARRAY);
	}
}
