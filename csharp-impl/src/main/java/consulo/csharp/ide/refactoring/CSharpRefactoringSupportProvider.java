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

package consulo.csharp.ide.refactoring;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.refactoring.changeSignature.CSharpChangeSignatureHandler;
import consulo.csharp.ide.refactoring.extractMethod.CSharpExtractMethodHandler;
import consulo.csharp.ide.refactoring.introduceVariable.CSharpIntroduceLocalVariableHandler;
import consulo.csharp.lang.psi.CSharpLambdaParameter;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTupleVariable;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.CSharpTupleElementImpl;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBasedPsiElement;
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
		return new CSharpIntroduceLocalVariableHandler();
	}

	@Nullable
	@Override
	public RefactoringActionHandler getExtractMethodHandler()
	{
		return new CSharpExtractMethodHandler();
	}

	@Override
	public boolean isSafeDeleteAvailable(PsiElement element)
	{
		return true;
	}

	@Override
	@RequiredReadAction
	public boolean isInplaceRenameAvailable(PsiElement element, PsiElement context)
	{
		// we not allow default variable inplace renamer
		return false;
	}

	@Override
	@RequiredReadAction
	public boolean isMemberInplaceRenameAvailable(PsiElement element, PsiElement context)
	{
		if(element instanceof DotNetParameter && element instanceof StubBasedPsiElement)
		{
			return true;
		}
		if(element instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) element).hasModifier(CSharpModifier.PARTIAL))
		{
			return false;
		}
		return element instanceof DotNetQualifiedElement && !(element instanceof DotNetNamespaceAsElement);
	}

	@RequiredReadAction
	public static boolean mayRenameInplace(@Nonnull PsiElement elementToRename, @Nullable final PsiElement nameSuggestionContext)
	{
		if(nameSuggestionContext != null && nameSuggestionContext.getContainingFile() != elementToRename.getContainingFile())
		{
			return false;
		}
		if(elementToRename instanceof CSharpTupleVariable || elementToRename instanceof CSharpTupleElementImpl)
		{
			return true;
		}
		if(elementToRename instanceof DotNetNamespaceAsElement)
		{
			return true;
		}
		if(!(elementToRename instanceof CSharpLocalVariable) && !(elementToRename instanceof DotNetParameter) && !(elementToRename instanceof
				CSharpLambdaParameter))
		{
			return false;
		}
		SearchScope useScope = PsiSearchHelper.getInstance(elementToRename.getProject()).getUseScope(elementToRename);
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
