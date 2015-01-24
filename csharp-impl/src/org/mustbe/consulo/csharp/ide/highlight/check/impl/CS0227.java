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
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUnsafeStatementImpl;
import org.mustbe.consulo.csharp.module.extension.BaseCSharpModuleExtension;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleExtension;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import lombok.val;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS0227 extends CompilerCheck<DotNetElement>
{
	public static class AllowUnsafeCodeFix extends BaseIntentionAction
	{
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
			CSharpModuleExtension extension = ModuleUtilCore.getExtension(file, CSharpModuleExtension.class);
			return extension != null && !extension.isAllowUnsafeCode();
		}

		@Override
		public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
		{
			Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(file);
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
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetElement element)
	{
		PsiElement targetElement = getElement(element);
		if(targetElement == null)
		{
			return null;
		}

		BaseCSharpModuleExtension extension = ModuleUtilCore.getExtension(element, BaseCSharpModuleExtension.class);

		if(extension != null && !extension.isAllowUnsafeCode())
		{
			CompilerCheckBuilder builder = newBuilder(targetElement).addQuickFix(new AllowUnsafeCodeFix());
			if(targetElement.getParent() instanceof CSharpModifierList)
			{
				builder.addQuickFix(new RemoveModifierFix(CSharpModifier.UNSAFE, (DotNetModifierListOwner) element));
			}

			return builder;
		}
		return null;
	}

	@Nullable
	private static PsiElement getElement(DotNetElement element)
	{
		if(element instanceof CSharpUnsafeStatementImpl)
		{
			return ((CSharpUnsafeStatementImpl) element).getUnsafeElement();
		}
		else if(element instanceof DotNetModifierListOwner)
		{
			DotNetModifierList modifierList = ((DotNetModifierListOwner) element).getModifierList();
			if(modifierList == null)
			{
				return null;
			}
			return modifierList.getModifierElement(CSharpModifier.UNSAFE);
		}
		return null;
	}
}
