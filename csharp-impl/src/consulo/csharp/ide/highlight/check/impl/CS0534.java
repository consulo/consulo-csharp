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

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.codeInsight.generation.ImplementMethodsHandler;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.resolve.DotNetGenericExtractor;

/**
 * @author VISTALL
 * @since 16.12.14
 */
public class CS0534 extends CompilerCheck<CSharpTypeDeclaration>
{
	public static class ImplementMembersQuickFix extends BaseIntentionAction
	{
		@NotNull
		@Override
		public String getText()
		{
			return "Implement members";
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
			return true;
		}

		@Override
		public boolean startInWriteAction()
		{
			return false;
		}

		@Override
		@RequiredDispatchThread
		public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
		{
			new ImplementMethodsHandler().invoke(project, editor, file);
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpTypeDeclaration element)
	{
		if(element.isInterface())
		{
			return null;
		}
		PsiElement nameIdentifier = element.getNameIdentifier();
		if(nameIdentifier == null)
		{
			return null;
		}

		Collection<DotNetModifierListOwner> abstractElements = OverrideUtil.collectMembersWithModifier(element, DotNetGenericExtractor.EMPTY, CSharpModifier.ABSTRACT);
		if(!abstractElements.isEmpty())
		{
			List<DotNetModifierListOwner> targets = new SmartList<>();

			boolean isAbstract = CSharpCompositeTypeDeclaration.selectCompositeOrSelfType(element).hasModifier(CSharpModifier.ABSTRACT);

			for(DotNetModifierListOwner abstractElement : abstractElements)
			{
				if(abstractElement.hasModifier(CSharpModifier.INTERFACE_ABSTRACT))
				{
					targets.add(abstractElement);
				}
				else if(!isAbstract)
				{
					targets.add(abstractElement);
				}
			}

			if(targets.isEmpty())
			{
				return null;
			}

			DotNetModifierListOwner firstItem = targets.get(0);

			CompilerCheckBuilder compilerCheckBuilder;

			if(firstItem.hasModifier(CSharpModifier.INTERFACE_ABSTRACT))
			{
				compilerCheckBuilder = newBuilderImpl(CS0535.class, nameIdentifier, formatElement(element), formatElement(firstItem));
			}
			else
			{
				compilerCheckBuilder = newBuilder(nameIdentifier, formatElement(element), formatElement(firstItem));
			}

			compilerCheckBuilder.addQuickFix(new ImplementMembersQuickFix());
			return compilerCheckBuilder;
		}
		return null;
	}
}
