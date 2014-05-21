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

package org.mustbe.consulo.csharp.lang.psi.impl.light.builder;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 06.02.14
 */
public abstract class CSharpLightNamedElementBuilder<T extends CSharpLightNamedElementBuilder<T>> extends CSharpLightElementBuilder<T> implements
		DotNetNamedElement
{
	private String myName;

	public CSharpLightNamedElementBuilder(Project project)
	{
		super(project);
	}

	public CSharpLightNamedElementBuilder(PsiElement element)
	{
		super(element);
		setNavigationElement(element);
	}

	@SuppressWarnings("unchecked")
	public T withName(@NotNull String name)
	{
		myName = name;
		return (T)this;
	}

	@Override
	public String getName()
	{
		return myName;
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return null;
	}
}
