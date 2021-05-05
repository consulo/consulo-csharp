/*
 * Copyright 2013-2021 consulo.io
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

package consulo.csharp.ide.refactoring;

import com.intellij.codeInsight.editorActions.moveLeftRight.MoveElementLeftRightHandler;
import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCallArgumentList;
import consulo.csharp.lang.psi.impl.source.CSharpArrayInitializerImpl;
import consulo.csharp.lang.psi.impl.source.CSharpImplicitArrayInitializationExpressionImpl;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 05/05/2021
 */
public class CSharpMoveElementLeftRightHandler implements MoveElementLeftRightHandler
{
	@RequiredReadAction
	@Nonnull
	@Override
	public PsiElement[] getMovableSubElements(@Nonnull PsiElement psiElement)
	{
		if(psiElement instanceof CSharpCallArgumentList callArgumentList)
		{
			return callArgumentList.getArguments();
		}
		else if(psiElement instanceof CSharpArrayInitializerImpl initializer)
		{
			return initializer.getValues();
		}
		else if(psiElement instanceof CSharpImplicitArrayInitializationExpressionImpl initializer)
		{
			return initializer.getExpressions();
		}
		return PsiElement.EMPTY_ARRAY;
	}
}
