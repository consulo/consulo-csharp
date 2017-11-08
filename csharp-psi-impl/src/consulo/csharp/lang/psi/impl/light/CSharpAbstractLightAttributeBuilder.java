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

package consulo.csharp.lang.psi.impl.light;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpCallArgumentList;
import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpNamedCallArgument;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.DotNetExpression;

/**
 * @author VISTALL
 * @since 02.09.14
 */
public abstract class CSharpAbstractLightAttributeBuilder extends LightElement implements CSharpAttribute
{
	private List<NotNullLazyValue<CSharpCallArgument>> myCallArguments = Collections.emptyList();

	public CSharpAbstractLightAttributeBuilder(Project project)
	{
		super(PsiManager.getInstance(project), CSharpLanguage.INSTANCE);
	}

	public void addParameterExpression(Object o)
	{
		if(myCallArguments.isEmpty())
		{
			myCallArguments = new SmartList<>();
		}

		myCallArguments.add(NotNullLazyValue.createValue(() ->
		{
			Object value = o;

			if(value instanceof String)
			{
				value = StringUtil.QUOTER.fun((String) value);
			}

			return new CSharpLightCallArgument(CSharpFileFactory.createExpression(getProject(), String.valueOf(value)));
		}));
	}

	@Nullable
	@Override
	public CSharpReferenceExpression getReferenceExpression()
	{
		return null;
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
		List<DotNetExpression> list = new ArrayList<>(arguments.length);
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
		if(myCallArguments.isEmpty())
		{
			return CSharpCallArgument.EMPTY_ARRAY;
		}
		List<CSharpCallArgument> arguments = new ArrayList<>(myCallArguments.size());
		for(NotNullLazyValue<CSharpCallArgument> callArgument : myCallArguments)
		{
			arguments.add(callArgument.getValue());
		}
		return ContainerUtil.toArray(arguments, CSharpCallArgument.ARRAY_FACTORY);
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
		return ResolveResult.EMPTY_ARRAY;
	}
}
