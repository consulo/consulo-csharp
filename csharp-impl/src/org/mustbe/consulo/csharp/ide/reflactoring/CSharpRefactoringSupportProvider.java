/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.ide.reflactoring;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.reflactoring.changeSignature.CSharpChangeSignatureHandler;
import org.mustbe.consulo.csharp.ide.reflactoring.introduceVariable.CSharpIntroduceVariableHandler;
import org.mustbe.consulo.csharp.lang.psi.CSharpLambdaParameter;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpParameterImpl;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.changeSignature.ChangeSignatureHandler;

/**
 * @author VISTALL
 * @since 11.03.14
 */
public class CSharpRefactoringSupportProvider extends RefactoringSupportProvider
{
	@Nullable
	@Override
	public ChangeSignatureHandler getChangeSignatureHandler()
	{
		return new CSharpChangeSignatureHandler();
	}

	@Nullable
	@Override
	public RefactoringActionHandler getIntroduceVariableHandler()
	{
		return new CSharpIntroduceVariableHandler();
	}

	@Override
	public boolean isSafeDeleteAvailable(PsiElement element)
	{
		return true;
	}

	@Override
	public boolean isInplaceRenameAvailable(PsiElement element, PsiElement context)
	{
		return mayRenameInplace(element, context);
	}

	@Override
	public boolean isMemberInplaceRenameAvailable(PsiElement element, PsiElement context)
	{
		return element instanceof DotNetQualifiedElement;
	}

	public static boolean mayRenameInplace(PsiElement elementToRename, final PsiElement nameSuggestionContext)
	{
		if(nameSuggestionContext != null && nameSuggestionContext.getContainingFile() != elementToRename.getContainingFile())
		{
			return false;
		}
		if(!(elementToRename instanceof CSharpLocalVariable) && !(elementToRename instanceof CSharpParameterImpl) && !(elementToRename instanceof
				CSharpLambdaParameter))
		{
			return false;
		}
		SearchScope useScope = PsiSearchHelper.SERVICE.getInstance(elementToRename.getProject()).getUseScope(elementToRename);
		if(!(useScope instanceof LocalSearchScope))
		{
			return false;
		}
		PsiElement[] scopeElements = ((LocalSearchScope) useScope).getScope();
		if(scopeElements.length > 1)
		{
			return false;    // ... and badly scoped resource variables
		}
		PsiFile containingFile = elementToRename.getContainingFile();
		return PsiTreeUtil.isAncestor(containingFile, scopeElements[0], false);
	}

}
