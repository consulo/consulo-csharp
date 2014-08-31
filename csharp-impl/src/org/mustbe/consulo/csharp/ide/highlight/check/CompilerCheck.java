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

package org.mustbe.consulo.csharp.ide.highlight.check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.CSharpErrorBundle;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.ide.DotNetElementPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 09.03.14
 */
public abstract class CompilerCheck<T extends PsiElement>
{
	public static class CompilerCheckResult
	{
		private String myText;
		private TextRange myTextRange;
		private HighlightInfoType myHighlightInfoType;

		private List<IntentionAction> myQuickFixes = Collections.emptyList();

		public TextRange getTextRange()
		{
			return myTextRange;
		}

		public CompilerCheckResult setTextRange(TextRange textRange)
		{
			myTextRange = textRange;
			return this;
		}

		public String getText()
		{
			return myText;
		}

		public CompilerCheckResult setText(String text)
		{
			myText = text;
			return this;
		}

		public HighlightInfoType getHighlightInfoType()
		{
			return myHighlightInfoType;
		}

		public CompilerCheckResult setHighlightInfoType(HighlightInfoType highlightInfoType)
		{
			myHighlightInfoType = highlightInfoType;
			return this;
		}

		public CompilerCheckResult addQuickFix(IntentionAction a)
		{
			if(myQuickFixes.isEmpty())
			{
				myQuickFixes = new ArrayList<IntentionAction>(3);
			}
			myQuickFixes.add(a);
			return this;
		}

		public List<IntentionAction> getQuickFixes()
		{
			return myQuickFixes;
		}
	}

	@NotNull
	public List<CompilerCheckResult> check(@NotNull CSharpLanguageVersion languageVersion, @NotNull T element)
	{
		CompilerCheckResult check = checkImpl(languageVersion, element);
		if(check == null)
		{
			return Collections.emptyList();
		}
		return Collections.singletonList(check);
	}

	@Nullable
	public CompilerCheckResult checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull T element)
	{
		return null;
	}

	@NotNull
	public CompilerCheckResult result(@NotNull PsiElement range, String... args)
	{
		return resultImpl(getClass(), range, args);
	}

	@NotNull
	public CompilerCheckResult result(@NotNull TextRange range, String... args)
	{
		return resultImpl(getClass(), range, args);
	}

	@NotNull
	public static CompilerCheckResult resultImpl(@NotNull Class<?> clazz, @NotNull PsiElement range, String... args)
	{
		return resultImpl(clazz, range.getTextRange(), args);
	}

	@NotNull
	public static CompilerCheckResult resultImpl(@NotNull Class<?> clazz, @NotNull TextRange range, String... args)
	{
		CompilerCheckResult result = new CompilerCheckResult();
		result.setText(message(clazz, args));
		result.setTextRange(range);
		return result;
	}

	@NotNull
	public static String message(@NotNull Class<?> aClass, String... args)
	{
		String id = aClass.getSimpleName();
		String message = CSharpErrorBundle.message(id, args);
		if(ApplicationManager.getApplication().isInternal())
		{
			message = id + ": " + message;
		}
		return message;
	}

	public static String formatElement(PsiElement e)
	{
		if(e instanceof DotNetParameter)
		{
			return ((DotNetParameter) e).getName();
		}
		else if(e instanceof CSharpLocalVariable)
		{
			return ((CSharpLocalVariable) e).getName();
		}

		String parentName = null;
		PsiElement parent = e.getParent();
		if(parent instanceof DotNetNamespaceDeclaration)
		{
			parentName = ((DotNetNamespaceDeclaration) parent).getPresentableQName();
		}
		else if(parent instanceof DotNetTypeDeclaration)
		{
			parentName = DotNetElementPresentationUtil.formatTypeWithGenericParameters((DotNetTypeDeclaration) parent);
		}

		String currentText = "Unknown element : " + e.getClass().getSimpleName();
		if(e instanceof DotNetLikeMethodDeclaration)
		{
			currentText = DotNetElementPresentationUtil.formatMethod((DotNetLikeMethodDeclaration)e, 0);
		}
		else if(e instanceof DotNetTypeDeclaration)
		{
			currentText = DotNetElementPresentationUtil.formatTypeWithGenericParameters((DotNetTypeDeclaration) e);
		}
		else if(e instanceof DotNetVariable && e instanceof DotNetQualifiedElement)
		{
			currentText = ((DotNetQualifiedElement) e).getName();
		}

		if(StringUtil.isEmpty(parentName))
		{
			return currentText;
		}
		else
		{
			return parentName + "." + currentText;
		}
	}
}
