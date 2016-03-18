/*
 * Copyright 2013-2016 must-be.org
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

package org.mustbe.consulo.csharp.ide.completion.insertHandler;

import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.csharp.ide.completion.util.CSharpParenthesesInsertHandler;
import org.mustbe.consulo.csharp.lang.psi.CSharpCodeFragment;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefUtil;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 08.03.2016
 */
public class CSharpParenthesesWithSemicolonInsertHandler implements InsertHandler<LookupElement>
{
	private DotNetLikeMethodDeclaration myDeclaration;

	public CSharpParenthesesWithSemicolonInsertHandler(DotNetLikeMethodDeclaration declaration)
	{
		myDeclaration = declaration;
	}

	@Override
	@RequiredDispatchThread
	public void handleInsert(InsertionContext context, LookupElement item)
	{
		new CSharpParenthesesInsertHandler(myDeclaration).handleInsert(context, item);

		if(context.getCompletionChar() != '\n' || context.getFile() instanceof CSharpCodeFragment)
		{
			return;
		}

		// for void method we always insert semicolon
		if(DotNetTypeRefUtil.isVmQNameEqual(myDeclaration.getReturnTypeRef(), myDeclaration, DotNetTypes.System.Void))
		{
			if(TailType.SEMICOLON.isApplicable(context))
			{
				TailType.SEMICOLON.processTail(context.getEditor(), context.getTailOffset());
			}
		}
		else
		{
			context.commitDocument();
			PsiElement elementAt = context.getFile().findElementAt(context.getStartOffset());
			CSharpMethodCallExpressionImpl expression = PsiTreeUtil.getParentOfType(elementAt, CSharpMethodCallExpressionImpl.class);
			if(expression != null && expression.getNextSibling() instanceof PsiErrorElement)
			{
				TailType.SEMICOLON.processTail(context.getEditor(), context.getTailOffset());
			}
		}
	}
}
