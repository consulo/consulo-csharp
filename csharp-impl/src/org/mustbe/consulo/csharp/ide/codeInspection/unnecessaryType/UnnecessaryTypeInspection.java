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

package org.mustbe.consulo.csharp.ide.codeInspection.unnecessaryType;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariableDeclarationStatement;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpCatchStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import lombok.val;

/**
 * @author VISTALL
 * @since 18.05.14
 */
public class UnnecessaryTypeInspection extends LocalInspectionTool
{
	private static class ReplaceByVarFix extends LocalQuickFixOnPsiElement
	{
		ReplaceByVarFix(@NotNull PsiElement element)
		{
			super(element);
		}

		@NotNull
		@Override
		public String getText()
		{
			return "Replace by 'var'";
		}

		@NotNull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}

		@Override
		public void invoke(
				@NotNull final Project project, @NotNull PsiFile psiFile, @NotNull final PsiElement element, @NotNull PsiElement element2)
		{
			new WriteCommandAction.Simple<Object>(project, psiFile)
			{
				@Override
				protected void run() throws Throwable
				{
					val statement = (CSharpLocalVariableDeclarationStatement) CSharpFileFactory.createStatement(project, "var test = 1;");

					val localVariable = statement.getVariables()[0];

					val type = localVariable.getType();

					assert type != null;

					element.replace(type);
				}
			}.execute();
		}
	}

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session)
	{
		CSharpLanguageVersion languageVersion = CSharpModuleUtil.findLanguageVersion(holder.getFile());
		if(!languageVersion.isAtLeast(CSharpLanguageVersion._3_0))
		{
			return CSharpElementVisitor.EMPTY;
		}

		return new CSharpElementVisitor()
		{
			@Override
			public void visitLocalVariable(CSharpLocalVariable variable)
			{
				if(variable.isConstant() || variable.getParent() instanceof CSharpCatchStatementImpl)
				{
					return;
				}

				DotNetExpression initializer = variable.getInitializer();
				if(initializer != null)
				{
					DotNetTypeRef typeRef = initializer.toTypeRef(false);
					if(typeRef instanceof CSharpLambdaTypeRef || typeRef == DotNetTypeRef.NULL_TYPE)
					{
						return;
					}
				}
				else
				{
					return;
				}

				DotNetTypeRef typeRef = variable.toTypeRef(false);
				if(typeRef == DotNetTypeRef.AUTO_TYPE)
				{
					return;
				}
				else if(typeRef == CSharpStaticTypeRef.DYNAMIC)
				{
					return;
				}

				DotNetType type = variable.getType();
				if(type == null)
				{
					return;
				}

				holder.registerProblem(type, "Can replaced by 'var'", ProblemHighlightType.LIKE_UNUSED_SYMBOL, new ReplaceByVarFix(type));
			}
		};
	}
}
