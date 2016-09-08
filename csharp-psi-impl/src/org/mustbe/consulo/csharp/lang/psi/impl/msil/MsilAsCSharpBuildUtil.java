package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightGenericConstraintList;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightGenericConstraintBuilder;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterList;
import consulo.dotnet.resolve.DotNetPsiSearcher;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.msil.lang.psi.MsilGenericParameter;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 13.12.14
 */
public class MsilAsCSharpBuildUtil
{
	@Nullable
	@RequiredReadAction
	public static CSharpLightGenericConstraintList buildConstraintList(@Nullable DotNetGenericParameterList genericParameterList)
	{
		if(genericParameterList == null)
		{
			return null;
		}

		DotNetGenericParameter[] parameters = genericParameterList.getParameters();
		List<CSharpGenericConstraint> list = new ArrayList<CSharpGenericConstraint>(parameters.length);
		for(DotNetGenericParameter genericParameter : parameters)
		{
			CSharpLightGenericConstraintBuilder builder = new CSharpLightGenericConstraintBuilder(genericParameter);

			assert genericParameter instanceof MsilGenericParameterAsCSharpGenericParameter;

			MsilGenericParameter msilGenericParameter = (MsilGenericParameter) genericParameter.getOriginalElement();

			boolean skipFirst = false;
			DotNetPsiSearcher.TypeResoleKind typeKind = msilGenericParameter.getTypeKind();
			switch(typeKind)
			{
				case CLASS:
					builder.addKeywordConstraint(CSharpTokens.CLASS_KEYWORD);
					if(msilGenericParameter.hasDefaultConstructor())
					{
						builder.addKeywordConstraint(CSharpTokens.NEW_KEYWORD);
					}
					break;
				case STRUCT:
					builder.addKeywordConstraint(CSharpTokens.STRUCT_KEYWORD);
					skipFirst = true;
					break;
			}

			DotNetTypeRef[] extendTypeRefs = msilGenericParameter.getExtendTypeRefs();
			if(skipFirst && extendTypeRefs.length > 0)
			{
				// remove ValueType due STRUCT constraint provide one type ref System.ValueType
				extendTypeRefs = ArrayUtil.remove(extendTypeRefs, 0);
			}

			for(DotNetTypeRef extendTypeRef : extendTypeRefs)
			{
				builder.addTypeConstraint(MsilToCSharpUtil.extractToCSharp(extendTypeRef, msilGenericParameter));
			}

			if(!builder.isEmpty())
			{
				list.add(builder);
			}
		}

		if(list.isEmpty())
		{
			return null;
		}
		return new CSharpLightGenericConstraintList(genericParameterList.getProject(), ContainerUtil.toArray(list,
				CSharpGenericConstraint.ARRAY_FACTORY));
	}
}
