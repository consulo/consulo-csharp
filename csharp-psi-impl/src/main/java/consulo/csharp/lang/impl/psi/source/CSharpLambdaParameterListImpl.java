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

package consulo.csharp.lang.impl.psi.source;

import consulo.language.ast.IElementType;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpLambdaParameter;
import consulo.csharp.lang.psi.CSharpLambdaParameterList;
import consulo.dotnet.psi.resolve.DotNetTypeRef;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 19.01.14
 */
public class CSharpLambdaParameterListImpl extends CSharpElementImpl implements CSharpLambdaParameterList
{
	public CSharpLambdaParameterListImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitLambdaParameterList(this);
	}

	@Override
	@Nonnull
	public CSharpLambdaParameter[] getParameters()
	{
		return findChildrenByClass(CSharpLambdaParameter.class);
	}

	@Override
	@Nonnull
	public DotNetTypeRef[] getParameterTypeRefs()
	{
		CSharpLambdaParameter[] parameters = getParameters();
		if(parameters.length == 0)
		{
			return DotNetTypeRef.EMPTY_ARRAY;
		}
		DotNetTypeRef[] dotNetTypeRefs = new DotNetTypeRef[parameters.length];
		for(int i = 0; i < dotNetTypeRefs.length; i++)
		{
			dotNetTypeRefs[i] = parameters[i].toTypeRef(true);
		}
		return dotNetTypeRefs;
	}
}
