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

package consulo.csharp.ide.codeInspection.unnecessaryEnumUnderlyingType;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.Nls;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 01-Nov-17
 */
public class UnnecessaryEnumUnderlyingTypeInspection extends LocalInspectionTool
{
	private static class RemoveFix extends LocalQuickFixOnPsiElement
	{
		protected RemoveFix(@Nonnull PsiElement element)
		{
			super(element);
		}

		@Nonnull
		@Override
		public String getText()
		{
			return "Remove underlying type";
		}

		@Override
		public void invoke(@Nonnull Project project, @Nonnull PsiFile psiFile, @Nonnull PsiElement psiElement, @Nonnull PsiElement psiElement1)
		{
			psiElement.delete();
		}

		@Nls
		@Nonnull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}
	}

	@Nonnull
	@Override
	public PsiElementVisitor buildVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly)
	{
		return new CSharpElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitTypeDeclaration(CSharpTypeDeclaration declaration)
			{
				if(!declaration.isEnum())
				{
					return;
				}

				DotNetTypeList extendList = declaration.getExtendList();
				if(extendList == null)
				{
					return;
				}
				DotNetType[] types = extendList.getTypes();
				if(types.length == 1)
				{
					DotNetType type = types[0];

					DotNetTypeRef typeRef = type.toTypeRef();
					if(CSharpTypeUtil.isTypeEqual(typeRef, new CSharpTypeRefByQName(declaration, DotNetTypes.System.Int32), declaration))
					{
						holder.registerProblem(type, "Unnecessary enum underlying type", ProblemHighlightType.LIKE_UNUSED_SYMBOL, new RemoveFix(extendList));
					}
				}
			}
		};
	}
}
