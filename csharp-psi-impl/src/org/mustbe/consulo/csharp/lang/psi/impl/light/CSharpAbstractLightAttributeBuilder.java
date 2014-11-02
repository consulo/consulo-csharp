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

package org.mustbe.consulo.csharp.lang.psi.impl.light;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamedCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 02.09.14
 */
public abstract class CSharpAbstractLightAttributeBuilder extends LightElement implements CSharpAttribute
{
	private final List<CSharpCallArgument> myCallArguments = new SmartList<CSharpCallArgument>();

	public CSharpAbstractLightAttributeBuilder(Project project)
	{
		super(PsiManager.getInstance(project), CSharpLanguage.INSTANCE);
	}

	public void addParameterExpression(Object o)
	{
		if(o instanceof String)
		{
			o = StringUtil.QUOTER.fun((String)o);
		}

		myCallArguments.add(new CSharpLightCallArgument(CSharpFileFactory.createExpression(getProject(), String.valueOf(o))));
	}

	@Nullable
	@Override
	public CSharpReferenceExpression getReferenceExpression()
	{
		return null;
	}

	@Override
	public boolean canResolve()
	{
		return true;
	}

	@Nullable
	@Override
	public CSharpCallArgumentList getParameterList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetExpression[] getParameterExpressions()
	{
		CSharpCallArgument[] arguments = getCallArguments();
		List<DotNetExpression> list = new ArrayList<DotNetExpression>(arguments.length);
		for(CSharpCallArgument callArgument : arguments)
		{
			if(!(callArgument instanceof CSharpNamedCallArgument))
			{
				DotNetExpression argumentExpression = callArgument.getArgumentExpression();
				assert argumentExpression != null;
				list.add(argumentExpression);
			}
		}
		return ContainerUtil.toArray(list, DotNetExpression.ARRAY_FACTORY);
	}

	@NotNull
	@Override
	public CSharpCallArgument[] getCallArguments()
	{
		return ContainerUtil.toArray(myCallArguments, CSharpCallArgument.ARRAY_FACTORY);
	}

	@Nullable
	@Override
	public DotNetTypeList getTypeArgumentList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getTypeArgumentListRefs()
	{
		return new DotNetTypeRef[0];
	}

	@Nullable
	@Override
	public PsiElement resolveToCallable()
	{
		return null;
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		return new ResolveResult[0];
	}
}
