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

package consulo.csharp.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.impl.PsiTreeChangeEventImpl;
import com.intellij.psi.impl.PsiTreeChangePreprocessorBase;
import com.intellij.psi.util.PsiTreeUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetStatement;

/**
 * @author VISTALL
 * @since 19.12.13.
 */
public class CSharpPsiTreeChangePreprocessor extends PsiTreeChangePreprocessorBase
{
	public CSharpPsiTreeChangePreprocessor(@NotNull PsiManager psiManager)
	{
		super(psiManager);
	}

	@Override
	protected boolean acceptsEvent(@NotNull PsiTreeChangeEventImpl event)
	{
		return event.getFile() instanceof CSharpFile;
	}

	@Override
	@RequiredReadAction
	protected boolean isOutOfCodeBlock(@NotNull PsiElement element)
	{
		return !isInsideCodeBlock(element);
	}

	@RequiredReadAction
	private boolean isInsideCodeBlock(@Nullable PsiElement element)
	{
		if(PsiTreeUtil.getParentOfType(element, DotNetStatement.class, false) != null)
		{
			return true;
		}
		DotNetQualifiedElement qualifiedElement = PsiTreeUtil.getParentOfType(element, DotNetQualifiedElement.class);
		if(!(qualifiedElement instanceof PsiNameIdentifierOwner))
		{
			return false;
		}
		return CSharpPsiUtilImpl.isNullOrEmpty((PsiNameIdentifierOwner) qualifiedElement);
	}
}
