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

package consulo.csharp.lang.doc.impl.inspection;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.language.Language;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.util.collection.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.doc.CSharpDocUtil;
import consulo.csharp.lang.doc.impl.psi.CSharpDocAttribute;
import consulo.csharp.lang.doc.impl.psi.CSharpDocElementVisitor;
import consulo.csharp.lang.doc.impl.psi.CSharpDocTagImpl;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.psi.PsiElementVisitor;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
@ExtensionImpl
public class CSharpDocInspection extends LocalInspectionTool
{
	@Nonnull
	@Override
	public PsiElementVisitor buildVisitor(@Nonnull final ProblemsHolder holder, boolean isOnTheFly)
	{
		return new CSharpDocElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitErrorElement(PsiErrorElement element)
			{
				if(!CSharpDocUtil.isInsideDoc(element))
				{
					return;
				}

				int textLength = element.getTextLength();
				if(textLength == 0)
				{
					PsiElement prevSibling = element.getPrevSibling();
					if(prevSibling == null)
					{
						return;
					}
					holder.registerProblem(prevSibling, element.getErrorDescription(), ProblemHighlightType.WEAK_WARNING);
				}
				else
				{
					holder.registerProblem(element, element.getErrorDescription(), ProblemHighlightType.WEAK_WARNING);
				}
			}

			@Override
			@RequiredReadAction
			public void visitDocTag(CSharpDocTagImpl docTag)
			{
				if(docTag.getTagInfo() == null)
				{
					List<PsiElement> nameElements = docTag.getNameElements();
					PsiElement firstItem = ContainerUtil.getFirstItem(nameElements);
					if(firstItem == null)
					{
						return;
					}
					holder.registerProblem(firstItem, "Unknown tag name '" + firstItem.getText() + "'", ProblemHighlightType.WEAK_WARNING);
				}
			}

			@Override
			@RequiredReadAction
			public void visitDocAttribute(CSharpDocAttribute docAttribute)
			{
				if(docAttribute.getAttributeInfo() == null)
				{
					PsiElement psiElement = docAttribute.getNameIdentifier();
					assert psiElement != null;
					holder.registerProblem(psiElement, "Unknown attribute name '" + psiElement.getText() + "'", ProblemHighlightType.WEAK_WARNING);
				}
			}
		};
	}

	@Nullable
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}

	@Nonnull
	@Override
	public String getGroupDisplayName()
	{
		return "Documentation";
	}

	@Nonnull
	@Override
	public String getDisplayName()
	{
		return "Documentation problems";
	}

	@Nonnull
	@Override
	public HighlightDisplayLevel getDefaultLevel()
	{
		return HighlightDisplayLevel.WEAK_WARNING;
	}
}
