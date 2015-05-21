package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public class CSharpTypeResolveContext extends CSharpBaseResolveContext<CSharpTypeDeclaration>
{
	@RequiredReadAction
	public CSharpTypeResolveContext(@NotNull CSharpTypeDeclaration element, @NotNull DotNetGenericExtractor genericExtractor)
	{
		super(element, genericExtractor);
	}

	@NotNull
	@Override
	protected List<DotNetTypeRef> getExtendTypeRefs()
	{
		DotNetTypeRef[] typeRefs = myElement.getExtendTypeRefs();
		List<DotNetTypeRef> extendTypeRefs = new ArrayList<DotNetTypeRef>(typeRefs.length);

		for(DotNetTypeRef typeRef : typeRefs)
		{
			extendTypeRefs.add(GenericUnwrapTool.exchangeTypeRef(typeRef, myExtractor, myElement));
		}
		return extendTypeRefs;
	}

	@Override
	public void processMembers(CSharpTypeDeclaration element, Collector collector)
	{
		DotNetNamedElement[] members = element.getMembers();
		for(DotNetNamedElement member : members)
		{
			member.accept(collector);
		}
	}
}
