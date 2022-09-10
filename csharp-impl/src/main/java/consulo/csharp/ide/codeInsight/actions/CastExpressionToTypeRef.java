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

import consulo.codeEditor.Editor;
import consulo.language.editor.intention.BaseIntentionAction;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.project.Project;
import consulo.language.psi.PsiFile;
import consulo.component.util.localize.BundleBase;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.language.util.IncorrectOperationException;

import javax.annotation.Nonnull;

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
		return BundleBase.format("Cast to ''{0}''", CSharpTypeRefPresentationUtil.buildTextWithKeyword(myExpectedTypeRef));
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

		if(DotNetTypeRefUtil.isVmQNameEqual(myExpectedTypeRef, DotNetTypes.System.Void))
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

		String typeText = CSharpTypeRefPresentationUtil.buildShortText(myExpectedTypeRef);

		DotNetExpression expression = CSharpFileFactory.createExpression(project, "(" + typeText + ") " + element.getText());

		element.replace(expression);
	}
}
