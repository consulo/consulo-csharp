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

package consulo.csharp.impl.ide.codeInsight.problems;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.language.editor.ProblemHighlightFilter;
import consulo.language.psi.PsiCodeFragment;
import consulo.language.psi.PsiFile;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 13.04.2015
 */
@ExtensionImpl
public class CSharpProblemHighlightFilter extends ProblemHighlightFilter
{
	@Override
	public boolean shouldHighlight(@Nonnull PsiFile psiFile)
	{
		if(psiFile instanceof CSharpFile)
		{
			if(psiFile instanceof PsiCodeFragment)
			{
				return true;
			}
			return CSharpLocationUtil.isValidLocation(psiFile.getProject(), psiFile.getVirtualFile());
		}
		return true;
	}
}
