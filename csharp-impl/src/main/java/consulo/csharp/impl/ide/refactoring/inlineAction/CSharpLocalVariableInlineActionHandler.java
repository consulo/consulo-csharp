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

package consulo.csharp.impl.ide.refactoring.inlineAction;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.function.Processor;
import consulo.codeEditor.Editor;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.source.CSharpForStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLocalVariableUtil;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.Language;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.refactoring.inline.InlineActionHandler;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.search.ReferencesSearch;
import consulo.project.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 12.06.14
 */
@ExtensionImpl
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
