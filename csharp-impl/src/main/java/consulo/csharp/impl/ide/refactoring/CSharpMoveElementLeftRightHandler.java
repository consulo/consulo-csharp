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

package consulo.csharp.impl.ide.refactoring;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpCallArgumentList;
import consulo.csharp.lang.impl.psi.source.CSharpArrayInitializerImpl;
import consulo.csharp.lang.impl.psi.source.CSharpImplicitArrayInitializationExpressionImpl;
import consulo.language.Language;
import consulo.language.editor.moveLeftRight.MoveElementLeftRightHandler;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 05/05/2021
 */
@ExtensionImpl
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

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
