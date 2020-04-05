/*
 * Copyright 2013-2020 consulo.io
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

package consulo.csharp.ide.codeInsight;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import consulo.annotation.access.RequiredReadAction;
import consulo.codeInsight.TargetElementUtil;
import consulo.codeInsight.TargetElementUtilEx;
import consulo.csharp.lang.psi.CSharpReferenceExpression;

/**
 * @author VISTALL
 * @since 2020-04-05
 */
public class CSharpTargetElementUtilEx extends TargetElementUtilEx.Adapter
{
	@Nullable
	@Override
	@RequiredReadAction
	public PsiElement modifyReferenceOrReferencedElement(@Nullable PsiElement refElement, @Nonnull PsiFile file, @Nonnull Editor editor, @Nonnull Set<String> flags, int offset)
	{
		PsiElement target = refElement;
		CSharpReferenceExpression referenceExpression = refElement instanceof CSharpReferenceExpression ? (CSharpReferenceExpression) refElement : null;

		if(refElement == null)
		{
			PsiReference reference = TargetElementUtil.findReference(editor, offset);
			if(reference instanceof CSharpReferenceExpression)
			{
				referenceExpression = (CSharpReferenceExpression) reference;
			}
		}

		if(referenceExpression != null)
		{
			target = referenceExpression.resolve();
		}
		return target;
	}
}
