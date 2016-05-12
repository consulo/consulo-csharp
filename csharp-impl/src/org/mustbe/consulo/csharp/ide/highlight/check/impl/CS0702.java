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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import gnu.trove.THashMap;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintTypeValue;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 04.12.14
 */
public class CS0702 extends CompilerCheck<CSharpGenericConstraintTypeValue>
{
	public static class ReplaceConstraintFix extends BaseIntentionAction
	{
		private final String myKeywordForReplace;
		private final SmartPsiElementPointer<CSharpGenericConstraintTypeValue> myPointer;

		public ReplaceConstraintFix(CSharpGenericConstraintTypeValue declaration, String keywordForReplace)
		{
			myKeywordForReplace = keywordForReplace;
			myPointer = SmartPointerManager.getInstance(declaration.getProject()).createSmartPsiElementPointer(declaration);
		}

		@NotNull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}

		@NotNull
		@Override
		public String getText()
		{
			return "Replace by '" + myKeywordForReplace + "' constraint";
		}

		@Override
		public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
		{
			return myPointer.getElement() != null;
		}

		@Override
		public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
		{
			CSharpGenericConstraintTypeValue element = myPointer.getElement();
			if(element == null)
			{
				return;
			}
			PsiDocumentManager.getInstance(project).commitAllDocuments();

			CSharpMethodDeclaration method = (CSharpMethodDeclaration) CSharpFileFactory.createMethod(project, "void test<T> where T : " +
					myKeywordForReplace + " {}");

			CSharpGenericConstraint newGenericConstraint = method.getGenericConstraints()[0];

			element.replace(newGenericConstraint.getGenericConstraintValues()[0]);
		}
	}

	private static final Map<String, String> ourErrorMap = new THashMap<String, String>()
	{
		{
			put(DotNetTypes.System.Object, "class");
			put(DotNetTypes.System.ValueType, "struct");
		}
	};

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpGenericConstraintTypeValue element)
	{
		DotNetTypeRef typeRef = element.toTypeRef();
		Pair<String, DotNetTypeDeclaration> pair = CSharpTypeUtil.resolveTypeElement(typeRef);
		if(pair == null)
		{
			return null;
		}
		String keywordForReplace = ourErrorMap.get(pair.getFirst());
		if(keywordForReplace != null)
		{
			DotNetType type = element.getType();
			assert type != null;
			return newBuilder(type, pair.getFirst()).addQuickFix(new ReplaceConstraintFix(element, keywordForReplace));
		}
		return null;
	}
}
