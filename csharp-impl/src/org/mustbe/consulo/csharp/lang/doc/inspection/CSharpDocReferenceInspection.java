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

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.ide.highlight.check.impl.CC0001;
import org.mustbe.consulo.csharp.lang.doc.CSharpDocUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

/**
 * @author VISTALL
 * @since 24.07.2015
 */
public class CSharpDocReferenceInspection extends LocalInspectionTool
{
	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly)
	{
		return new CSharpElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitReferenceExpression(CSharpReferenceExpression expression)
			{
				PsiElement referenceElement = expression.getReferenceElement();
				if(referenceElement == null || expression.isSoft() || !CSharpDocUtil.isInsideDoc(expression))
				{
					return;
				}

				List<CompilerCheck.HighlightInfoFactory> factories = CC0001.checkReference(expression, Arrays.asList(referenceElement));
				if(factories.isEmpty())
				{
					return;
				}

				for(CompilerCheck.HighlightInfoFactory factory : factories)
				{
					HighlightInfo highlightInfo = factory.create();
					if(highlightInfo == null)
					{
						continue;
					}
					holder.registerProblem(expression, highlightInfo.getDescription());
				}
			}
		};
	}
}
