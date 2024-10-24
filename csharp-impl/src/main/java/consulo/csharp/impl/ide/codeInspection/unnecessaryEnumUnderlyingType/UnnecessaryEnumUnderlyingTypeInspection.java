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

package consulo.csharp.impl.ide.codeInspection.unnecessaryEnumUnderlyingType;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.impl.ide.codeInspection.CSharpGeneralLocalInspection;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import org.jetbrains.annotations.Nls;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 01-Nov-17
 */
@ExtensionImpl
public class UnnecessaryEnumUnderlyingTypeInspection extends CSharpGeneralLocalInspection
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
					if(CSharpTypeUtil.isTypeEqual(typeRef, new CSharpTypeRefByQName(declaration, DotNetTypes.System.Int32)))
					{
						holder.registerProblem(type, "Unnecessary enum underlying type", ProblemHighlightType.LIKE_UNUSED_SYMBOL, new RemoveFix(extendList));
					}
				}
			}
		};
	}

	@Nonnull
	@Override
	public String getDisplayName()
	{
		return "Unnecessary enum underlying type";
	}

	@Nonnull
	@Override
	public HighlightDisplayLevel getDefaultLevel()
	{
		return HighlightDisplayLevel.WARNING;
	}
}
