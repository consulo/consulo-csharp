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

package consulo.csharp.impl.ide.findUsage;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpPreprocessorLanguage;
import consulo.csharp.lang.psi.CSharpPreprocessorVariable;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.findUsage.FindUsagesProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 21.12.13.
 */
@ExtensionImpl
public class CSharpPreprocessorFindUsagesProvider implements FindUsagesProvider
{
	@Override
	public boolean canFindUsagesFor(@Nonnull PsiElement element)
	{
		return element instanceof CSharpPreprocessorVariable;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public String getType(@Nonnull PsiElement element)
	{
		if(element instanceof CSharpPreprocessorVariable)
		{
			return "preprocessor variable";
		}
		return debugText("getType", element);
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public String getDescriptiveName(@Nonnull PsiElement element)
	{
		if(element instanceof CSharpPreprocessorVariable)
		{
			return ((CSharpPreprocessorVariable) element).getName();
		}
		return debugText("getDescriptiveName", element);
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public String getNodeText(@Nonnull PsiElement element, boolean useFullName)
	{
		if(element instanceof CSharpPreprocessorVariable)
		{
			return ((CSharpPreprocessorVariable) element).getName();
		}

		return debugText("getNodeText", element);
	}

	@Nonnull
	private String debugText(String prefix, @Nonnull PsiElement element)
	{
		IElementType type = PsiUtilCore.getElementType(element);
		String suffix = type == null ? element.getClass().getSimpleName() : type.toString();
		return prefix + " : " + suffix;
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpPreprocessorLanguage.INSTANCE;
	}
}
