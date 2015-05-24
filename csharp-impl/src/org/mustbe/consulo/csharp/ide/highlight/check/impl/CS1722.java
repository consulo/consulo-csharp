/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 20.01.15
 */
public class CS1722 extends CompilerCheck<DotNetTypeList>
{
	public static class MoveToFirstPositionFix extends BaseIntentionAction
	{
		private SmartPsiElementPointer<DotNetType> myTypePointer;

		public MoveToFirstPositionFix(DotNetType baseType)
		{
			myTypePointer = SmartPointerManager.getInstance(baseType.getProject()).createSmartPsiElementPointer(baseType);
			setText("Place base type at first");
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
			return myTypePointer.getElement() != null;
		}

		@Override
		public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
		{
			DotNetType element = myTypePointer.getElement();
			if(element == null)
			{
				return;
			}

			DotNetTypeList parent = (DotNetTypeList) element.getParent();

			DotNetType[] types = parent.getTypes();

			int i = ArrayUtil.indexOf(types, element);
			if(i <= 0)
			{
				return;
			}
			DotNetType elementAtZeroPosition = types[0];

			PsiElement baseElementCopy = element.copy();
			PsiElement elementAtZeroCopy = elementAtZeroPosition.copy();

			elementAtZeroPosition.replace(baseElementCopy);
			element.replace(elementAtZeroCopy);
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetTypeList element)
	{
		if(element.getNode().getElementType() != CSharpStubElements.EXTENDS_LIST)
		{
			return null;
		}

		CSharpTypeDeclaration resolvedElement = null;
		DotNetType baseType = null;
		DotNetType[] types = element.getTypes();

		for(DotNetType type : types)
		{
			DotNetTypeRef typeRef = type.toTypeRef();
			PsiElement temp = typeRef.resolve(element).getElement();
			if(temp instanceof CSharpTypeDeclaration && !((CSharpTypeDeclaration) temp).isInterface())
			{
				resolvedElement = (CSharpTypeDeclaration) temp;
				baseType = type;
				break;
			}
		}

		if(baseType == null)
		{
			return null;
		}
		int i = ArrayUtil.indexOf(types, baseType);
		if(i != 0)
		{
			CSharpTypeDeclaration parent = (CSharpTypeDeclaration) element.getParent();
			return newBuilder(baseType, formatElement(parent), formatElement(resolvedElement)).addQuickFix(new MoveToFirstPositionFix(baseType));
		}
		return null;
	}
}
