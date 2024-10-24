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

package consulo.csharp.impl.ide.refactoring.changeSignature;

import consulo.usage.UsageViewDescriptor;
import consulo.usage.UsageViewUtil;
import consulo.util.lang.StringUtil;
import consulo.language.psi.PsiElement;
import consulo.language.editor.refactoring.RefactoringBundle;
import consulo.usage.UsageViewBundle;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 12.06.14
 *
 * by
 * @author Jeka via Apache 2
 *
 * maybe move to core?
 */
public class ChangeSignatureViewDescriptor implements UsageViewDescriptor
{
	private final PsiElement myMethod;
	private final String myProcessedElementsHeader;

	public ChangeSignatureViewDescriptor(PsiElement method)
	{
		myMethod = method;
		myProcessedElementsHeader = StringUtil.capitalize(RefactoringBundle.message("0.to.change.signature", UsageViewUtil.getType(method)));
	}

	@Override
	@Nonnull
	public PsiElement[] getElements()
	{
		return new PsiElement[]{myMethod};
	}

	@Override
	public String getProcessedElementsHeader()
	{
		return myProcessedElementsHeader;
	}

	@Override
	public String getCodeReferencesText(int usagesCount, int filesCount)
	{
		return RefactoringBundle.message("references.to.be.changed", UsageViewBundle.getReferencesString(usagesCount, filesCount));
	}

	@Override
	public String getCommentReferencesText(int usagesCount, int filesCount)
	{
		return null;
	}
}
