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

package org.mustbe.consulo.csharp.lang.doc.inspection;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocElementVisitor;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocTag;
import org.mustbe.consulo.csharp.lang.doc.validation.CSharpDocTagInfo;
import org.mustbe.consulo.csharp.lang.doc.validation.CSharpDocTagManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

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
			public void visitDocTag(CSharpDocTag docTag)
			{
				CSharpDocTagManager docTagManager = CSharpDocTagManager.getInstance();
				List<PsiElement> nameElements = docTag.getNameElements();
				for(PsiElement nameElement : nameElements)
				{
					CSharpDocTagInfo tagInfo = docTagManager.getTagInfo(nameElement.getText());
					if(tagInfo == null)
					{
						holder.registerProblem(nameElement, "Unknown tag name '" + nameElement.getText() + "'", ProblemHighlightType.ERROR);
					}
				}
			}
		};
	}
}
