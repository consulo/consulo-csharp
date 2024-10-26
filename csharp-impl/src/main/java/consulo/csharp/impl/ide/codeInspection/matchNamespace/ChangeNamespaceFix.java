/*
 * Copyright 2013-2019 consulo.io
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

package consulo.csharp.impl.ide.codeInspection.matchNamespace;

import consulo.csharp.lang.psi.CSharpNamespaceProvider;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import org.jetbrains.annotations.Nls;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2019-07-24
 */
public class ChangeNamespaceFix extends LocalQuickFixOnPsiElement
{
	private final String myExpectedNamespace;

	public ChangeNamespaceFix(@Nonnull CSharpNamespaceProvider element, @Nonnull String expectedNamespace)
	{
		super(element);
		myExpectedNamespace = expectedNamespace;
	}

	@Override
	public void invoke(@Nonnull Project project, @Nonnull PsiFile psiFile, @Nonnull PsiElement element1, @Nonnull PsiElement element2)
	{
		CSharpNamespaceProvider declaration = (CSharpNamespaceProvider) element1;

		new ChangeNamespaceProcessor(project, declaration, myExpectedNamespace).run();
	}

	@Override
	public boolean startInWriteAction()
	{
		return false;
	}

	@Nonnull
	@Override
	public String getText()
	{
		return "Change declaration to '" + myExpectedNamespace + "'";
	}

	@Nls
	@Nonnull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}
}
