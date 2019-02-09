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

import consulo.ui.RequiredUIAccess;
import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefUtil;
import com.intellij.BundleBase;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public class CastExpressionToTypeRef extends BaseIntentionAction
{
	@Nonnull
	protected final SmartPsiElementPointer<DotNetExpression> myExpressionPointer;
	@Nonnull
	protected final DotNetTypeRef myExpectedTypeRef;

	public CastExpressionToTypeRef(@Nonnull DotNetExpression expression, @Nonnull DotNetTypeRef expectedTypeRef)
	{
		myExpressionPointer = SmartPointerManager.getInstance(expression.getProject()).createSmartPsiElementPointer(expression);
		myExpectedTypeRef = expectedTypeRef;
	}

	@Nonnull
	@Override
	@RequiredUIAccess
	public String getText()
	{
		DotNetExpression element = myExpressionPointer.getElement();
		if(element == null)
		{
			return "invalid";
		}
		return BundleBase.format("Cast to ''{0}''", CSharpTypeRefPresentationUtil.buildTextWithKeyword(myExpectedTypeRef, element));
	}

	@Nonnull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}

	@Override
	@RequiredUIAccess
	public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file)
	{
		if(myExpectedTypeRef == DotNetTypeRef.UNKNOWN_TYPE)
		{
			return false;
		}
		DotNetExpression element = myExpressionPointer.getElement();
		if(element == null)
		{
			return false;
		}

		if(DotNetTypeRefUtil.isVmQNameEqual(myExpectedTypeRef, element, DotNetTypes.System.Void))
		{
			return false;
		}
		return true;
	}

	@Override
	@RequiredUIAccess
	public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
	{
		DotNetExpression element = myExpressionPointer.getElement();
		if(element == null)
		{
			return;
		}

		String typeText = CSharpTypeRefPresentationUtil.buildShortText(myExpectedTypeRef, element);

		DotNetExpression expression = CSharpFileFactory.createExpression(project, "(" + typeText + ") " + element.getText());

		element.replace(expression);
	}
}
