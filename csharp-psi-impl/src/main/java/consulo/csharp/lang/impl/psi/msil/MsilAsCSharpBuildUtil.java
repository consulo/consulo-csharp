/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.csharp.lang.impl.psi.msil;

import consulo.util.collection.ArrayUtil;
import consulo.util.collection.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.impl.psi.light.CSharpLightGenericConstraintList;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightGenericConstraintBuilder;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterList;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.msil.lang.psi.MsilGenericParameter;
import consulo.msil.lang.psi.MsilUserType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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
		List<CSharpGenericConstraint> list = new ArrayList<>(parameters.length);
		for(DotNetGenericParameter genericParameter : parameters)
		{
			CSharpLightGenericConstraintBuilder builder = new CSharpLightGenericConstraintBuilder(genericParameter);

			assert genericParameter instanceof MsilGenericParameterAsCSharpGenericParameter;

			MsilGenericParameter msilGenericParameter = (MsilGenericParameter) genericParameter.getOriginalElement();

			boolean skipFirst = false;
			MsilUserType.Target target = msilGenericParameter.getTarget();
			if(target != null)
			{
				switch(target)
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
			}

			DotNetTypeRef[] extendTypeRefs = msilGenericParameter.getExtendTypeRefs();
			if(skipFirst && extendTypeRefs.length > 0)
			{
				// remove ValueType due STRUCT constraint provide one type ref System.ValueType
				extendTypeRefs = ArrayUtil.remove(extendTypeRefs, 0);
			}

			for(DotNetTypeRef extendTypeRef : extendTypeRefs)
			{
				builder.addTypeConstraint(MsilToCSharpUtil.extractToCSharp(extendTypeRef));
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
		return new CSharpLightGenericConstraintList(genericParameterList.getProject(), ContainerUtil.toArray(list, CSharpGenericConstraint.ARRAY_FACTORY));
	}
}
