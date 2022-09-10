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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.content.scope.SearchScope;
import consulo.csharp.ide.refactoring.changeSignature.CSharpChangeSignatureHandler;
import consulo.csharp.ide.refactoring.extractMethod.CSharpExtractMethodHandler;
import consulo.csharp.ide.refactoring.introduceVariable.CSharpIntroduceLocalVariableHandler;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.source.CSharpTupleElementImpl;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.language.Language;
import consulo.language.editor.refactoring.RefactoringSupportProvider;
import consulo.language.editor.refactoring.action.RefactoringActionHandler;
import consulo.language.editor.refactoring.changeSignature.ChangeSignatureHandler;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.StubBasedPsiElement;
import consulo.language.psi.scope.LocalSearchScope;
import consulo.language.psi.search.PsiSearchHelper;
import consulo.language.psi.util.PsiTreeUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 11.03.14
 */
@ExtensionImpl
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

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
