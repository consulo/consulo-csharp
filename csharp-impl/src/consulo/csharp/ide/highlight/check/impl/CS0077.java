/*
 * Copyright 2013-2015 must-be.org
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
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.impl.source.CSharpAsExpressionImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 27.02.2015
 */
public class CS0077 extends CompilerCheck<CSharpAsExpressionImpl>
{
	public static class AddQuestMarkQuickFix extends BaseIntentionAction
	{
		private SmartPsiElementPointer<DotNetType> myPointer;

		public AddQuestMarkQuickFix(DotNetType type)
		{
			myPointer = SmartPointerManager.getInstance(type.getProject()).createSmartPsiElementPointer(type);
			setText("Add '?'");
		}

		@NotNull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}

		@Override
		public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
		{
			return myPointer.getElement() != null;
		}

		@Override
		public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
		{
			DotNetType element = myPointer.getElement();
			if(element == null)
			{
				return;
			}

			DotNetType type = CSharpFileFactory.createMaybeStubType(project, element.getText() + "?", element);
			element.replace(type);
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpAsExpressionImpl element)
	{
		DotNetTypeRef typeRef = element.toTypeRef(false);
		if(typeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return null;
		}

		if(!typeRef.resolve().isNullable())
		{
			DotNetType type = element.getType();
			assert type != null;
			return newBuilder(element.getAsKeyword(), "as", formatTypeRef(typeRef, element)).addQuickFix(new AddQuestMarkQuickFix(type));
		}
		return super.checkImpl(languageVersion, highlightContext, element);
	}
}
