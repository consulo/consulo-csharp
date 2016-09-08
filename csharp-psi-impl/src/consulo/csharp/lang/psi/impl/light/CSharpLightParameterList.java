/*
 * Copyright 2013-2014 must-be.org
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

package consulo.csharp.lang.psi.impl.light;

import org.jetbrains.annotations.NotNull;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpLightParameterList extends CSharpLightElement<PsiElement> implements DotNetParameterList
{
	private DotNetParameter[] myParameters;

	public CSharpLightParameterList(PsiElement original, DotNetParameter[] parameters)
	{
		super(original);
		myParameters = parameters;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitParameterList(this);
	}

	@Override
	public int getParametersCount()
	{
		return myParameters.length;
	}

	@NotNull
	@Override
	public DotNetParameter[] getParameters()
	{
		return myParameters;
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getParameterTypeRefs()
	{
		DotNetParameter[] parameters = getParameters();
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
