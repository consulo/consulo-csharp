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

package org.mustbe.consulo.csharp.ide.reflactoring.changeSignature;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.changeSignature.ChangeSignatureHandler;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CSharpChangeSignatureHandler implements ChangeSignatureHandler
{
	@Nullable
	@Override
	public PsiElement findTargetMember(PsiFile file, Editor editor)
	{
		return findTargetMember(file.findElementAt(editor.getCaretModel().getOffset()));
	}

	@Nullable
	@Override
	public PsiElement findTargetMember(PsiElement element)
	{
		ASTNode node = element.getNode();
		if(node != null && node.getElementType() == CSharpTokens.IDENTIFIER && element.getParent() instanceof DotNetLikeMethodDeclaration)
		{
			return element.getParent();
		}
		return null;
	}

	@Override
	public void invoke(
			@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext)
	{
		System.out.println("invoke");
	}

	@Override
	public void invoke(
			@NotNull Project project, @NotNull PsiElement[] elements, @Nullable DataContext dataContext)
	{
		PsiElement element = elements[0];

		DotNetLikeMethodDeclaration method = (DotNetLikeMethodDeclaration) element;
		CSharpMethodDescriptor methodDescriptor = new CSharpMethodDescriptor(method);
		CSharpChangeSignatureDialog dialog = new CSharpChangeSignatureDialog(project, methodDescriptor, false, element);
		dialog.show();
	}

	@Nullable
	@Override
	public String getTargetNotFoundMessage()
	{
		return "Method not found";
	}
}
