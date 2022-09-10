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

package consulo.csharp.ide.refactoring.changeSignature;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpIdentifier;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.impl.psi.source.CSharpMethodCallExpressionImpl;
import consulo.dataContext.DataContext;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.language.ast.ASTNode;
import consulo.codeEditor.Editor;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.editor.refactoring.changeSignature.ChangeSignatureHandler;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CSharpChangeSignatureHandler implements ChangeSignatureHandler
{
	@Nullable
	@Override
	@RequiredReadAction
	public PsiElement findTargetMember(PsiFile file, Editor editor)
	{
		return findTargetMember(file.findElementAt(editor.getCaretModel().getOffset()));
	}

	@Nullable
	@Override
	@RequiredReadAction
	public PsiElement findTargetMember(PsiElement element)
	{
		if(element == null)
		{
			return null;
		}
		if(element.getLanguage() != CSharpLanguage.INSTANCE)
		{
			return null;
		}
		if(element instanceof DotNetLikeMethodDeclaration)
		{
			return element;
		}
		CSharpMethodCallExpressionImpl callExpression = PsiTreeUtil.getParentOfType(element, CSharpMethodCallExpressionImpl.class);
		if(callExpression != null)
		{
			return findTargetMember(callExpression.resolveToCallable());
		}
		ASTNode node = element.getNode();
		if(node != null && node.getElementType() == CSharpTokens.IDENTIFIER)
		{
			PsiElement parent = element.getParent();
			if(parent instanceof DotNetLikeMethodDeclaration)
			{
				return parent;
			}
			else if(parent instanceof CSharpIdentifier)
			{
				PsiElement maybeMethod = parent.getParent();
				if(maybeMethod instanceof DotNetLikeMethodDeclaration)
				{
					return maybeMethod;
				}
			}
			return parent;
		}
		return null;
	}

	@Override
	public void invoke(
			@Nonnull Project project, Editor editor, PsiFile file, DataContext dataContext)
	{
	}

	@Override
	public void invoke(
			@Nonnull Project project, @Nonnull PsiElement[] elements, @Nullable DataContext dataContext)
	{
		PsiElement element = elements[0];
		if(!(element instanceof DotNetLikeMethodDeclaration))
		{
			return;
		}

		DotNetLikeMethodDeclaration method = (DotNetLikeMethodDeclaration) element;
		CSharpMethodDescriptor methodDescriptor = new CSharpMethodDescriptor(method);
		CSharpChangeSignatureDialog dialog = new CSharpChangeSignatureDialog(project, methodDescriptor, false, element);
		dialog.show();
	}

	@Nullable
	@Override
	public String getTargetNotFoundMessage()
	{
		return "Method or index property not found";
	}
}
