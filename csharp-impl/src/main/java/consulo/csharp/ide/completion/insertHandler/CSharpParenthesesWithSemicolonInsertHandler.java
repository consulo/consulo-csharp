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

package consulo.csharp.ide.completion.insertHandler;

import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.TailType;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.psi.PsiElement;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.csharp.ide.completion.util.CSharpParenthesesInsertHandler;
import consulo.csharp.lang.psi.CSharpCodeFragment;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;

/**
 * @author VISTALL
 * @since 08.03.2016
 */
public class CSharpParenthesesWithSemicolonInsertHandler implements InsertHandler<LookupElement>
{
	private PsiElement myDeclaration;

	public CSharpParenthesesWithSemicolonInsertHandler(PsiElement declaration)
	{
		myDeclaration = declaration;
	}

	@Override
	@RequiredUIAccess
	public void handleInsert(InsertionContext context, LookupElement item)
	{
		boolean isMethodLike = myDeclaration instanceof DotNetLikeMethodDeclaration;
		if(isMethodLike)
		{
			new CSharpParenthesesInsertHandler((DotNetLikeMethodDeclaration) myDeclaration).handleInsert(context, item);
		}

		if(context.getCompletionChar() != '\n' || context.getFile() instanceof CSharpCodeFragment)
		{
			return;
		}

		// for void method we always insert semicolon
		if(isMethodLike && !(myDeclaration instanceof CSharpConstructorDeclaration) && DotNetTypeRefUtil.isVmQNameEqual(((DotNetLikeMethodDeclaration) myDeclaration).getReturnTypeRef(), DotNetTypes.System.Void))
		{
			if(TailType.SEMICOLON.isApplicable(context))
			{
				TailType.SEMICOLON.processTail(context.getEditor(), context.getTailOffset());
			}
		}
		/*else
		{
			context.commitDocument();
			PsiElement elementAt = context.getFile().findElementAt(context.getStartOffset());
			PsiElement parent = PsiTreeUtil.getParentOfType(elementAt, CSharpMethodCallExpressionImpl.class);
			if(parent != null && parent.getNextSibling() instanceof PsiErrorElement)
			{
				TailType.SEMICOLON.processTail(context.getEditor(), context.getTailOffset());
			}

			parent = PsiTreeUtil.getParentOfType(elementAt, CSharpLocalVariable.class);
			if(parent != null && parent.getNextSibling() instanceof PsiErrorElement)
			{
				TailType.SEMICOLON.processTail(context.getEditor(), context.getTailOffset());
			}
		}*/
	}
}
