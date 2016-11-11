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

package consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpModifierList;
import consulo.csharp.lang.psi.impl.source.CSharpUnsafeStatementImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpSimpleModuleExtension;
import consulo.csharp.module.extension.CSharpSimpleMutableModuleExtension;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

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
		@RequiredDispatchThread
		public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
		{
			CSharpSimpleModuleExtension extension = ModuleUtilCore.getExtension(file, CSharpSimpleModuleExtension.class);
			return extension != null && !extension.isAllowUnsafeCode();
		}

		@Override
		@RequiredWriteAction
		public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
		{
			Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(file);
			if(moduleForPsiElement == null)
			{
				return;
			}

			ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(moduleForPsiElement);

			final ModifiableRootModel modifiableModel = moduleRootManager.getModifiableModel();
			CSharpSimpleMutableModuleExtension cSharpModuleExtension = modifiableModel.getExtension(CSharpSimpleMutableModuleExtension.class);
			if(cSharpModuleExtension != null)
			{
				cSharpModuleExtension.setAllowUnsafeCode(true);
			}

			modifiableModel.commit();
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetElement element)
	{
		PsiElement targetElement = getElement(element);
		if(targetElement == null)
		{
			return null;
		}

		CSharpSimpleModuleExtension extension = highlightContext.getCSharpModuleExtension();

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