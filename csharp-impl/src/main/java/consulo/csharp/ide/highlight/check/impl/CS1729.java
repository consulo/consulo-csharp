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

package consulo.csharp.ide.highlight.check.impl;

import java.util.Arrays;
import java.util.Optional;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.actions.generate.GenerateConstructorHandler;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImplUtil;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetGenericExtractor;

/**
 * @author VISTALL
 * @since 01-Nov-17
 */
public class CS1729 extends CompilerCheck<DotNetQualifiedElement>
{
	public static class GenerateConstructorFix extends LocalQuickFixAndIntentionActionOnPsiElement
	{
		private GenerateConstructorHandler myHandler = new GenerateConstructorHandler();

		protected GenerateConstructorFix(@Nullable PsiElement element)
		{
			super(element);
		}

		@Override
		@RequiredDispatchThread
		public void invoke(@NotNull Project project,
				@NotNull PsiFile psiFile,
				@Nullable(value = "is null when called from inspection") Editor editor,
				@NotNull PsiElement psiElement,
				@NotNull PsiElement psiElement1)
		{
			myHandler.invoke(project, editor, psiFile);
		}

		@Override
		public boolean startInWriteAction()
		{
			return myHandler.startInWriteAction();
		}

		@NotNull
		@Override
		public String getText()
		{
			return "Generate constructor";
		}

		@Nls
		@NotNull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetQualifiedElement t)
	{
		if(t instanceof CSharpConstructorDeclaration)
		{
			CSharpConstructorDeclaration element = (CSharpConstructorDeclaration) t;

			if(element.isDeConstructor())
			{
				return null;
			}

			if(element.getConstructorSuperCall() != null)
			{
				return null;
			}

			PsiElement parent = element.getParent();
			if(!(parent instanceof CSharpTypeDeclaration))
			{
				return null;
			}

			Pair<Boolean, DotNetTypeDeclaration> result = findEmptyConstructor((DotNetTypeDeclaration) parent, element);

			if(!result.getFirst())
			{
				return newBuilder(getNameIdentifier(t), formatElement(result.getSecond()), "0");
			}
		}
		else if(t instanceof CSharpTypeDeclaration)
		{
			DotNetTypeDeclaration declaration = CSharpCompositeTypeDeclaration.selectCompositeOrSelfType((CSharpTypeDeclaration) t);
			if(declaration.isEnum() || declaration.isInterface())
			{
				return null;
			}

			Optional<DotNetNamedElement> constructor = Arrays.stream(declaration.getMembers()).filter(it -> it instanceof CSharpConstructorDeclaration && !((CSharpConstructorDeclaration) it)
					.isDeConstructor()).findAny();

			if(constructor.isPresent())
			{
				return null;
			}

			Pair<Boolean, DotNetTypeDeclaration> result = findEmptyConstructor((DotNetTypeDeclaration) t, t);

			if(!result.getFirst())
			{
				return newBuilder(getNameIdentifier(t), formatElement(result.getSecond()), "0").addQuickFix(new GenerateConstructorFix(t));
			}
		}

		return null;
	}

	@RequiredReadAction
	private static Pair<Boolean, DotNetTypeDeclaration> findEmptyConstructor(DotNetTypeDeclaration parent, PsiElement scope)
	{
		Pair<DotNetTypeDeclaration, DotNetGenericExtractor> type = CSharpTypeDeclarationImplUtil.resolveBaseType((DotNetTypeDeclaration) parent, scope);
		if(type == null)
		{
			// if not base - return true for skip
			return Pair.create(Boolean.TRUE, null);
		}

		CSharpResolveContext context = CSharpResolveContextUtil.createContext(type.getSecond(), scope.getResolveScope(), type.getFirst());

		boolean emptyConstructorFind = false;
		CSharpElementGroup<CSharpConstructorDeclaration> group = context.constructorGroup();
		if(group != null)
		{
			for(CSharpConstructorDeclaration declaration : group.getElements())
			{
				if(declaration.getParameters().length == 0)
				{
					emptyConstructorFind = true;
					break;
				}
			}
		}
		return Pair.create(emptyConstructorFind, type.getFirst());
	}
}