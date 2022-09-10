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

package consulo.csharp.ide.codeInspection.languageKeywordUsage;

import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.WriteCommandAction;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.ast.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.lang.impl.ide.codeStyle.CSharpCodeGenerationSettings;
import consulo.csharp.ide.completion.CSharpCompletionUtil;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.impl.psi.source.CSharpNativeTypeImplUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiUtilCore;
import consulo.project.Project;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author VISTALL
 * @since 16.04.2016
 */
public abstract class LanguageKeywordUsageInspection extends LocalInspectionTool
{
	public static class ReplaceByKeywordFix extends LocalQuickFixOnPsiElement
	{
		private IElementType myKeywordElementType;

		public ReplaceByKeywordFix(@Nonnull PsiElement element, IElementType keywordElementType)
		{
			super(element);
			myKeywordElementType = keywordElementType;
		}

		@Nonnull
		@Override
		public String getText()
		{
			return "Replace by '" + CSharpCompletionUtil.textOfKeyword(myKeywordElementType) + "' keyword";
		}

		@Override
		public void invoke(@Nonnull final Project project, @Nonnull PsiFile file, @Nonnull final PsiElement startElement, @Nonnull PsiElement endElement)
		{
			new WriteCommandAction.Simple<Object>(project, file)
			{
				@Override
				@RequiredWriteAction
				protected void run() throws Throwable
				{
					CSharpReferenceExpression refExpression = (CSharpReferenceExpression) startElement.getParent();

					PsiElement parent = refExpression.getParent();
					String text = CSharpCompletionUtil.textOfKeyword(myKeywordElementType);

					if(parent instanceof CSharpUserType)
					{
						DotNetType stubType = CSharpFileFactory.createMaybeStubType(project, text, (DotNetType) parent);
						parent.replace(stubType);
					}
					else
					{
						// hack due simple keyword parsed not as expression
						CSharpReferenceExpression expression = (CSharpReferenceExpression) CSharpFileFactory.createExpression(project, text + ".call");

						DotNetExpression qualifier = expression.getQualifier();
						assert qualifier != null;

						refExpression.replace(qualifier);
					}
				}
			}.execute();
		}

		@Nonnull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}
	}

	private static class Visitor extends CSharpElementVisitor
	{
		private ProblemsHolder myHolder;

		public Visitor(ProblemsHolder holder)
		{
			myHolder = holder;
		}

		@Override
		@RequiredReadAction
		public void visitReferenceExpression(CSharpReferenceExpression expression)
		{
			DotNetExpression qualifier = expression.getQualifier();
			CSharpReferenceExpression.ResolveToKind kind = expression.kind();
			if(qualifier != null || kind == CSharpReferenceExpression.ResolveToKind.BASE || kind == CSharpReferenceExpression.ResolveToKind.THIS)
			{
				return;
			}

			PsiElement referenceElement = expression.getReferenceElement();
			if(referenceElement == null)
			{
				return;
			}

			PsiElement resolved = expression.resolve();
			if(!(resolved instanceof CSharpTypeDeclaration))
			{
				return;
			}

			String vmQName = ((CSharpTypeDeclaration) resolved).getVmQName();
			assert vmQName != null;
			for(Map.Entry<IElementType, String> entry : CSharpNativeTypeImplUtil.ourElementToQTypes.entrySet())
			{
				if(vmQName.equals(entry.getValue()) && PsiUtilCore.getElementType(referenceElement) != entry.getKey())
				{
					myHolder.registerProblem(referenceElement, "Reference does not match the current code style, use '" + CSharpCompletionUtil.textOfKeyword(entry.getKey()) + "'", new ReplaceByKeywordFix(referenceElement, entry.getKey()));
					break;
				}
			}
		}
	}

	@Nonnull
	@Override
	public PsiElementVisitor buildVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly)
	{
		CSharpCodeGenerationSettings settings = CSharpCodeGenerationSettings.getInstance(holder.getProject());
		if(!settings.USE_LANGUAGE_DATA_TYPES)
		{
			return new PsiElementVisitor()
			{
			};
		}
		return new Visitor(holder);
	}
}
