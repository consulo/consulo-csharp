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

package org.mustbe.consulo.csharp.ide.codeInsight.actions;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.BundleBase;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;
import lombok.val;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public class ChangeReturnToTypeRefFix extends BaseIntentionAction
{
	private final SmartPsiElementPointer<DotNetLikeMethodDeclaration> myMethodPointer;
	@NotNull
	private final DotNetTypeRef myToTypeRef;

	public ChangeReturnToTypeRefFix(@NotNull DotNetLikeMethodDeclaration element, @NotNull DotNetTypeRef toTypeRef)
	{
		myMethodPointer = SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);
		myToTypeRef = toTypeRef;
	}

	@NotNull
	@Override
	public String getText()
	{
		DotNetLikeMethodDeclaration element = myMethodPointer.getElement();
		if(element == null)
		{
			return "invalid";
		}
		return BundleBase.format("Make ''{0}'' return to ''{1}''", element.getName(), CSharpTypeRefPresentationUtil.buildTextWithKeyword
				(myToTypeRef, element));
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
	{
		return myMethodPointer.getElement() != null;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
	{
		PsiDocumentManager.getInstance(project).commitAllDocuments();

		DotNetLikeMethodDeclaration element = myMethodPointer.getElement();
		if(element == null)
		{
			return;
		}

		DotNetType typeOfVariable = element.getReturnType();
		if(typeOfVariable == null)
		{
			return;
		}
		String typeText = CSharpTypeRefPresentationUtil.buildShortText(myToTypeRef, element);

		val type = CSharpFileFactory.createStubType(project, typeText, typeOfVariable);

		typeOfVariable.replace(type);
	}
}