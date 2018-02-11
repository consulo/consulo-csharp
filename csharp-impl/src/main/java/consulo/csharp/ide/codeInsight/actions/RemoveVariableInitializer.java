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

package consulo.csharp.ide.codeInsight.actions;

import javax.annotation.Nonnull;

import consulo.csharp.lang.psi.CSharpTokenSets;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;
import consulo.annotations.RequiredWriteAction;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetVariable;

/**
 * @author VISTALL
 * @since 24.05.2015
 */
public class RemoveVariableInitializer extends BaseIntentionAction
{
	private SmartPsiElementPointer<DotNetVariable> myPointer;

	public RemoveVariableInitializer(DotNetVariable variable)
	{
		myPointer = SmartPointerManager.getInstance(variable.getProject()).createSmartPsiElementPointer(variable);
		setText(variable instanceof DotNetParameter ? "Remove initializer" : "Remove default value");
	}

	@Nonnull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}

	@Override
	public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file)
	{
		DotNetVariable element = myPointer.getElement();
		return element != null && element.getInitializer() != null;
	}

	@Override
	@RequiredWriteAction
	public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
	{
		DotNetVariable element = myPointer.getElement();
		if(element == null)
		{
			return;
		}
		PsiDocumentManager.getInstance(project).commitAllDocuments();

		DotNetExpression initializer = element.getInitializer();
		if(initializer == null)
		{
			return;
		}

		initializer.delete();

		ASTNode node = element.getNode();
		ASTNode childByType = node.findChildByType(CSharpTokenSets.EQ);
		if(childByType != null)
		{
			node.removeChild(childByType);
		}
	}
}
