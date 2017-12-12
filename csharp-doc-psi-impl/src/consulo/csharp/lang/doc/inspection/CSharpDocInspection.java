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

package consulo.csharp.lang.doc.inspection;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiErrorElement;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.doc.psi.CSharpDocAttribute;
import consulo.csharp.lang.doc.psi.CSharpDocElementVisitor;
import consulo.csharp.lang.doc.psi.CSharpDocTag;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
public class CSharpDocInspection extends LocalInspectionTool
{
	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly)
	{
		return new CSharpDocElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitErrorElement(PsiErrorElement element)
			{
				int textLength = element.getTextLength();
				if(textLength == 0)
				{
					holder.registerProblem(element.getPrevSibling(), element.getErrorDescription(), ProblemHighlightType.WEAK_WARNING);
				}
				else
				{
					holder.registerProblem(element, element.getErrorDescription(), ProblemHighlightType.WEAK_WARNING);
				}
			}

			@Override
			@RequiredReadAction
			public void visitDocTag(CSharpDocTag docTag)
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
}
