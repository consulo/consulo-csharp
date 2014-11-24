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

package org.mustbe.consulo.csharp.ide.highlight.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.highlight.quickFix.RenameQuickFix;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 12.11.14
 */
public class ConstructorHighlightUtil
{
	@Nullable
	public static HighlightInfo checkConstructorDeclaration(@NotNull CSharpConstructorDeclaration declaration)
	{
		PsiElement nameIdentifier = declaration.getNameIdentifier();

		if(nameIdentifier == null)
		{
			return null;
		}

		PsiElement parent = declaration.getParent();
		if(!(parent instanceof CSharpTypeDeclaration))
		{
			return null;
		}

		String expectedTypeName = ((CSharpTypeDeclaration) parent).getName();
		if(expectedTypeName == null)
		{
			return null;
		}
		if(!Comparing.equal(expectedTypeName, nameIdentifier.getText()))
		{
			HighlightInfo.Builder builder = HighlightInfo.newHighlightInfo(HighlightInfoType.ERROR);
			builder = builder.descriptionAndTooltip("Expected method name");
			builder = builder.range(nameIdentifier);
			HighlightInfo highlightInfo = builder.create();
			QuickFixAction.registerQuickFixAction(highlightInfo, new RenameQuickFix(expectedTypeName, declaration));
			return highlightInfo;
		}
		return null;
	}
}
