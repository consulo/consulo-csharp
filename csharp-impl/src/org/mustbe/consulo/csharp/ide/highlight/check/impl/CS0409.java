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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintList;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetGenericParameter;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CS0409 extends CompilerCheck<CSharpGenericConstraint>
{
	@RequiredReadAction
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpGenericConstraint element)
	{
		PsiElement maybeGenericConstraintList = element.getParent();
		if(!(maybeGenericConstraintList instanceof CSharpGenericConstraintList))
		{
			return null;
		}
		CSharpGenericConstraintList parent = (CSharpGenericConstraintList) maybeGenericConstraintList;

		DotNetGenericParameter resolve = element.resolve();
		if(resolve == null)
		{
			return null;
		}

		boolean selfFind = false;
		CSharpGenericConstraint[] genericConstraints = parent.getGenericConstraints();
		for(CSharpGenericConstraint constraint : genericConstraints)
		{
			if(constraint == element)
			{
				selfFind = true;
				continue;
			}

			if(!selfFind)
			{
				continue;
			}

			if(constraint.resolve() == resolve)
			{
				return newBuilder(constraint, resolve.getName());
			}
		}
		return null;
	}
}
