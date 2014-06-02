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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.msil.representation.builder.CSharpFileBuilder;
import org.mustbe.consulo.csharp.ide.msil.representation.builder.CSharpToMsiNavigateUtil;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;

/**
 * @author VISTALL
 * @since 02.06.14
 */
public abstract class MsilElementWrapper<T extends PsiElement> extends LightElement
{
	@NotNull
	protected final DotNetQualifiedElement myBuildRoot;
	protected T myMsilElement;

	public MsilElementWrapper(@Nullable DotNetQualifiedElement buildRoot, T msilElement)
	{
		super(PsiManager.getInstance(msilElement.getProject()), CSharpLanguage.INSTANCE);
		myBuildRoot = buildRoot == null ? (DotNetQualifiedElement) this : buildRoot;
		myMsilElement = msilElement;
	}

	@Override
	public boolean canNavigate()
	{
		return true;
	}

	@Override
	public void navigate(boolean requestFocus)
	{
		CSharpFileImpl file = CSharpFileBuilder.buildRoot(myBuildRoot, myMsilElement.getContainingFile().getVirtualFile());

		CSharpToMsiNavigateUtil.navigateToRepresentation(file, myMsilElement);
	}

	@NotNull
	public T getMsilElement()
	{
		return myMsilElement;
	}
}
