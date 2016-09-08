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

package org.mustbe.consulo.csharp.ide.refactoring.inlineAction;

import java.util.ArrayList;
import java.util.List;

import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpForStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLocalVariableUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import com.intellij.lang.Language;
import com.intellij.lang.refactoring.InlineActionHandler;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 12.06.14
 */
public class CSharpLocalVariableInlineActionHandler extends InlineActionHandler
{
	@Override
	public boolean isEnabledForLanguage(Language l)
	{
		return l == CSharpLanguage.INSTANCE;
	}

	@Override
	public boolean canInlineElement(PsiElement element)
	{
		if(element instanceof CSharpLocalVariable)
		{
			if(CSharpLocalVariableUtil.isForeachVariable((DotNetVariable) element) || element.getParent() instanceof CSharpForStatementImpl)
			{
				return false;
			}
			return ((CSharpLocalVariable) element).getInitializer() != null;
		}
		return false;
	}

	@Override
	public void inlineElement(final Project project, Editor editor, PsiElement element)
	{
		final CSharpLocalVariable variable = (CSharpLocalVariable) element;
		final DotNetExpression initializer = variable.getInitializer();
		assert initializer != null;

		final List<PsiElement> elementsToReplace = new ArrayList<PsiElement>();

		ReferencesSearch.search(new ReferencesSearch.SearchParameters(variable, variable.getUseScope(), false)).forEach(new Processor<PsiReference>()
		{
			@Override
			public boolean process(final PsiReference reference)
			{
				final PsiElement referenceElement = reference.getElement();

				elementsToReplace.add(referenceElement);
				return true;
			}
		});

		new WriteCommandAction.Simple<Object>(project, variable.getContainingFile())
		{
			@Override
			protected void run() throws Throwable
			{
				for(PsiElement referenceElement : elementsToReplace)
				{
					DotNetExpression expression = CSharpFileFactory.createExpression(project, initializer.getText());

					referenceElement.replace(expression);
				}
				variable.delete();
			}
		}.execute();
	}
}
