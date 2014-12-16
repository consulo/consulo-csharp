package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import gnu.trove.THashSet;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public class CSharpTypeResolveContext extends CSharpBaseResolveContext<CSharpTypeDeclaration>
{
	public CSharpTypeResolveContext(@NotNull CSharpTypeDeclaration element, @NotNull DotNetGenericExtractor genericExtractor)
	{
		super(element, genericExtractor);
	}

	@NotNull
	@Override
	protected List<DotNetTypeRef> getExtendTypeRefs()
	{
		List<DotNetTypeRef> extendTypeRefs = new SmartList<DotNetTypeRef>();

		if(myElement.hasModifier(CSharpModifier.PARTIAL))
		{
			DotNetTypeDeclaration[] types = DotNetPsiSearcher.getInstance(myElement.getProject()).findTypes(myElement.getVmQName(),
					myElement.getResolveScope());

			for(DotNetTypeDeclaration type : types)
			{
				DotNetTypeList extendList = type.getExtendList();
				if(extendList != null)
				{
					DotNetTypeRef[] typeRefs = extendList.getTypeRefs();
					for(DotNetTypeRef typeRef : typeRefs)
					{
						extendTypeRefs.add(GenericUnwrapTool.exchangeTypeRef(typeRef, myExtractor, type));
					}
				}
			}

			if(extendTypeRefs.isEmpty())
			{
				Set<String> set = new THashSet<String>();
				for(DotNetTypeDeclaration type : types)
				{
					set.add(CSharpTypeDeclarationImplUtil.getDefaultSuperType(type));
				}

				if(set.contains(DotNetTypes.System.ValueType))
				{
					extendTypeRefs.add(new CSharpTypeRefByQName(DotNetTypes.System.ValueType));
				}
				else
				{
					extendTypeRefs.add(new CSharpTypeRefByQName(DotNetTypes.System.Object));
				}
			}
		}
		else
		{
			for(DotNetTypeRef typeRef : myElement.getExtendTypeRefs())
			{
				extendTypeRefs.add(GenericUnwrapTool.exchangeTypeRef(typeRef, myExtractor, myElement));
			}
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
