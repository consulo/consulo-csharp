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

package org.mustbe.consulo.csharp.ide.actions.generate;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.actions.generate.memberChoose.ConstructorChooseMember;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ConstructorProcessor;
import org.mustbe.consulo.dotnet.psi.DotNetConstructorDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.ide.util.MemberChooser;
import com.intellij.ide.util.MemberChooserBuilder;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiParserFacade;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 25.06.14
 */
public class GenerateConstructorHandler implements CodeInsightActionHandler
{
	@Override
	public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file)
	{
		final CSharpTypeDeclaration typeDeclaration = GenerateConstructorAction.findTypeDeclaration(editor, file);
		if(typeDeclaration == null)
		{
			return;
		}


		final DotNetTypeRef baseType = CSharpTypeDeclarationImplUtil.resolveBaseTypeRef(typeDeclaration, typeDeclaration);

		final PsiElement resolve = baseType.resolve(typeDeclaration);
		if(!(resolve instanceof CSharpTypeDeclaration))
		{
			return;
		}

		final CSharpTypeDeclaration baseTypeDeclaration = (CSharpTypeDeclaration) resolve;

		ConstructorProcessor constructorProcessor = new ConstructorProcessor(null);
		baseTypeDeclaration.processConstructors(constructorProcessor);
		constructorProcessor.executeDefault(baseTypeDeclaration);

		final PsiElement[] psiElements = constructorProcessor.toPsiElements();
		final ConstructorChooseMember[] map = ContainerUtil.map(psiElements, new Function<PsiElement, ConstructorChooseMember>()
		{
			@Override
			public ConstructorChooseMember fun(PsiElement psiElement)
			{
				return new ConstructorChooseMember((DotNetConstructorDeclaration) psiElement);
			}
		}, ConstructorChooseMember.EMPTY_ARRAY);

		if(map.length == 1)
		{
			generateConstructor(typeDeclaration, editor, file, map[0]);
		}
		else
		{
			final MemberChooserBuilder<ConstructorChooseMember> builder = new MemberChooserBuilder<ConstructorChooseMember>(project);
			builder.setTitle("Choose Constructor");
			builder.allowMultiSelection(true);

			final MemberChooser<ConstructorChooseMember> memberChooser = builder.createBuilder(map);

			if(!memberChooser.showAndGet())
			{
				return;
			}

			final List<ConstructorChooseMember> selectedElements = memberChooser.getSelectedElements();
			if(selectedElements == null)
			{
				return;
			}
			for(ConstructorChooseMember selectedElement : selectedElements)
			{
				generateConstructor(typeDeclaration, editor, file, selectedElement);
			}
		}
	}

	private static void generateConstructor(@NotNull final CSharpTypeDeclaration typeDeclaration, @NotNull Editor editor,
			@NotNull final PsiFile file,
			@NotNull ConstructorChooseMember chooseMember)
	{
		String text = chooseMember.getText();
		text = text.replace("$NAME$", typeDeclaration.getName());

		final DotNetLikeMethodDeclaration method = CSharpFileFactory.createMethod(typeDeclaration.getProject(), text);

		final int offset = editor.getCaretModel().getOffset();
		final PsiElement elementAt = file.findElementAt(offset);
		assert elementAt != null;

		new WriteCommandAction.Simple<Object>(file.getProject(), file)
		{
			@Override
			protected void run() throws Throwable
			{
				final PsiElement psiElement = typeDeclaration.addAfter(method, elementAt);
				typeDeclaration.addAfter(PsiParserFacade.SERVICE.getInstance(file.getProject()).createWhiteSpaceFromText("\n"), psiElement);
			}
		}.execute();
	}

	@Override
	public boolean startInWriteAction()
	{
		return false;
	}
}
