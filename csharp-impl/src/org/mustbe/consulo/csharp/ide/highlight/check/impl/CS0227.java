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
import org.mustbe.consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifierList;
import org.mustbe.consulo.csharp.module.extension.BaseCSharpModuleExtension;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;
import lombok.val;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS0227 extends CompilerCheck<CSharpModifierList>
{
	public static class AllowUnsafeCodeFix extends BaseIntentionAction
	{
		private SmartPsiElementPointer<PsiElement> myPointer;

		public AllowUnsafeCodeFix(PsiElement element)
		{
			myPointer = SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);
		}

		@NotNull
		@Override
		public String getText()
		{
			return "Allow unsafe code";
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
			PsiElement element = myPointer.getElement();
			if(element == null)
			{
				return;
			}

			Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(element);
			if(moduleForPsiElement == null)
			{
				return;
			}

			ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(moduleForPsiElement);

			val modifiableModel = moduleRootManager.getModifiableModel();
			BaseCSharpModuleExtension cSharpModuleExtension = modifiableModel.getExtension(BaseCSharpModuleExtension.class);
			if(cSharpModuleExtension != null)
			{
				cSharpModuleExtension.setAllowUnsafeCode(true);
			}

			modifiableModel.commit();
		}
	}

	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpModifierList element)
	{
		PsiElement modifier = element.getModifierElement(CSharpModifier.UNSAFE);
		if(modifier == null)
		{
			return null;
		}

		BaseCSharpModuleExtension extension = ModuleUtilCore.getExtension(element, BaseCSharpModuleExtension.class);

		if(extension != null && !extension.isAllowUnsafeCode())
		{
			return newBuilder(modifier).addQuickFix(new AllowUnsafeCodeFix(element)).addQuickFix(new RemoveModifierFix(CSharpModifier.UNSAFE,
					(DotNetModifierListOwner) element.getParent()));
		}
		return null;
	}
}
