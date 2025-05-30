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

package consulo.csharp.impl.ide.highlight.check.impl;

import jakarta.annotation.Nullable;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintList;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CS0409 extends CompilerCheck<CSharpGenericConstraint>
{
	@RequiredReadAction
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpGenericConstraint element)
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
