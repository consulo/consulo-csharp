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

package consulo.csharp.ide.codeInspection.unnecessaryModifier;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.partial.CSharpCompositeTypeDeclaration;
import consulo.dotnet.psi.DotNetModifierList;
import com.intellij.codeInspection.IntentionWrapper;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

/**
 * @author VISTALL
 * @since 17.11.2015
 */
public class UnnecessaryModifierInspection extends LocalInspectionTool
{
	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly)
	{
		return new CSharpElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitTypeDeclaration(CSharpTypeDeclaration declaration)
			{
				DotNetModifierList modifierList = declaration.getModifierList();
				if(modifierList == null)
				{
					return;
				}

				PsiElement modifierElement = modifierList.getModifierElement(CSharpModifier.PARTIAL);
				if(modifierElement != null)
				{
					CSharpCompositeTypeDeclaration compositeType = CSharpCompositeTypeDeclaration.findCompositeType(declaration);
					if(compositeType == null)
					{
						holder.registerProblem(modifierElement, "Unnecessary modifier", ProblemHighlightType.LIKE_UNUSED_SYMBOL, new IntentionWrapper(new RemoveModifierFix(CSharpModifier.PARTIAL,
								declaration), declaration.getContainingFile()));
					}
				}
			}
		};
	}
}
