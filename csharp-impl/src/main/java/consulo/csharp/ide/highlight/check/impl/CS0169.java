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

package consulo.csharp.ide.highlight.check.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetTypeDeclaration;

/**
 * @author VISTALL
 * @since 06-Nov-17
 */
public class CS0169 extends CompilerCheck<CSharpFieldDeclaration>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpFieldDeclaration element)
	{
		if(!element.hasModifier(CSharpModifier.PRIVATE))
		{
			return null;
		}

		PsiElement parent = element.getParent();
		if(!(parent instanceof CSharpTypeDeclaration))
		{
			return null;
		}

		PsiElement nameIdentifier = element.getNameIdentifier();
		if(nameIdentifier == null)
		{
			return null;
		}

		Query<PsiReference> search = ReferencesSearch.search(element, CSharpCompositeTypeDeclaration.createLocalScope((DotNetTypeDeclaration) parent));
		if(search.findFirst() == null)
		{
			return newBuilder(nameIdentifier, formatElement(element));
		}

		return null;
	}
}
