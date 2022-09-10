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

import consulo.codeEditor.Editor;
import consulo.language.util.ModuleUtilCore;
import consulo.project.Project;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.content.ModuleRootManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpModifierList;
import consulo.csharp.lang.impl.psi.source.CSharpUnsafeStatementImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpSimpleModuleExtension;
import consulo.csharp.module.extension.CSharpSimpleMutableModuleExtension;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.language.editor.intention.BaseIntentionAction;
import consulo.module.Module;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS0227 extends CompilerCheck<DotNetElement>
{
	public static class AllowUnsafeCodeFix extends BaseIntentionAction
	{
		@Nonnull
		@Override
		public String getText()
		{
			return "Allow unsafe code";
		}

		@Nonnull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}

		@Override
		@RequiredUIAccess
		public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file)
		{
			CSharpSimpleModuleExtension extension = ModuleUtilCore.getExtension(file, CSharpSimpleModuleExtension.class);
			return extension != null && !extension.isAllowUnsafeCode();
		}

		@Override
		@RequiredWriteAction
		public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
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
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetElement element)
	{
		PsiElement targetElement = getElement(element);
		if(targetElement == null)
		{
			return null;
		}

		CSharpSimpleModuleExtension extension = highlightContext.getCSharpModuleExtension();

		if(extension != null && !extension.isAllowUnsafeCode())
		{
			CompilerCheckBuilder builder = newBuilder(targetElement).withQuickFix(new AllowUnsafeCodeFix());
			if(targetElement.getParent() instanceof CSharpModifierList)
			{
				builder.withQuickFix(new RemoveModifierFix(CSharpModifier.UNSAFE, (DotNetModifierListOwner) element));
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
