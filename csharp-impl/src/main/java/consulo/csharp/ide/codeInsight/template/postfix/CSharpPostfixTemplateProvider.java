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

package consulo.csharp.ide.codeInsight.template.postfix;

import java.util.Set;

import javax.annotation.Nonnull;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.language.Language;
import consulo.language.editor.postfixTemplate.PostfixTemplate;
import consulo.language.editor.postfixTemplate.PostfixTemplateProvider;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiFile;
import consulo.ide.impl.idea.util.containers.ArrayListSet;

import static consulo.ui.ex.content.event.ContentManagerEvent.ContentOperation.add;

/**
 * @author VISTALL
 * @since 17.05.14
 */
@ExtensionImpl
public class CSharpPostfixTemplateProvider extends PostfixTemplateProvider
{
	@Override
	protected Set<PostfixTemplate> buildTemplates()
	{
		return Set.of(new CSharpParenthesesPostfixTemplate());
	}

	@Override
	public boolean isTerminalSymbol(char currentChar)
	{
		return currentChar == '.' || currentChar == '!';
	}

	@Override
	public void preExpand(@Nonnull PsiFile file, @Nonnull Editor editor)
	{

	}

	@Override
	public void afterExpand(@Nonnull PsiFile file, @Nonnull Editor editor)
	{

	}

	@Nonnull
	@Override
	public PsiFile preCheck(@Nonnull final PsiFile copyFile, @Nonnull Editor realEditor, int currentOffset)
	{
		return copyFile;
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
