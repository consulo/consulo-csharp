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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.searches.ReferencesSearch;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.impl.source.CSharpForeachStatementImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CS0168 extends CompilerCheck<CSharpLocalVariable>
{
	public static final class DeleteLocalVariable extends LocalQuickFixAndIntentionActionOnPsiElement
	{
		public DeleteLocalVariable(@NotNull PsiElement element)
		{
			super(element);
		}

		@NotNull
		@Override
		public String getText()
		{
			return "Delete variable";
		}

		@Override
		public void invoke(@NotNull Project project,
				@NotNull PsiFile psiFile,
				@Nullable(value = "is null when called from inspection") Editor editor,
				@NotNull PsiElement psiElement,
				@NotNull PsiElement psiElement1)
		{
			psiElement.delete();
		}

		@NotNull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpLocalVariable element)
	{
		if(element.getInitializer() != null)
		{
			return null;
		}

		if(isUnused(element))
		{
			CompilerCheckBuilder builder = newBuilder(element.getNameIdentifier(), formatElement(element));
			if(!(element.getParent() instanceof CSharpForeachStatementImpl))
			{
				builder.addQuickFix(new DeleteLocalVariable(element));
			}
			return builder;
		}

		return null;
	}

	@RequiredReadAction
	static boolean isUnused(@NotNull CSharpLocalVariable element)
	{
		PsiElement nameIdentifier = element.getNameIdentifier();
		if(nameIdentifier == null)
		{
			return false;
		}

		return ReferencesSearch.search(element).findFirst() == null;
	}
}
