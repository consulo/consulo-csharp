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
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpNullableType;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 15.09.14
 */
public class CS0453 extends CompilerCheck<CSharpNullableType>
{
	public static class DeleteQuestMarkQuickFix extends BaseIntentionAction
	{
		private SmartPsiElementPointer<CSharpNullableType> myPointer;
		private String myText;

		public DeleteQuestMarkQuickFix(CSharpNullableType nullableType, String text)
		{
			myText = text;
			myPointer = SmartPointerManager.getInstance(nullableType.getProject()).createSmartPsiElementPointer(nullableType);
		}

		@NotNull
		@Override
		public String getText()
		{
			return "Remove '" + myText + "'";
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
		public boolean startInWriteAction()
		{
			return true;
		}

		@Override
		public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
		{
			CSharpNullableType element = myPointer.getElement();
			if(element == null)
			{
				return;
			}

			DotNetType innerType = element.getInnerType();
			if(innerType == null)
			{
				return;
			}

			DotNetType type = CSharpFileFactory.createType(project, innerType.getText());
			element.replace(type);
		}
	}

	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpNullableType element)
	{
		DotNetType innerType = element.getInnerType();
		if(innerType == null)
		{
			return null;
		}
		DotNetTypeRef dotNetTypeRef = innerType.toTypeRef();

		DotNetTypeResolveResult typeResolveResult = dotNetTypeRef.resolve(element);

		if(!typeResolveResult.isNullable())
		{
			return null;
		}
		PsiElement questElement = element.getQuestElement();
		return newBuilder(questElement, CSharpTypeRefPresentationUtil.buildTextWithKeyword(dotNetTypeRef,
				element)).addQuickFix(new DeleteQuestMarkQuickFix(element, "?"));
	}
}
