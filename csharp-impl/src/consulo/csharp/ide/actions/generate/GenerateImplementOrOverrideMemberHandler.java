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

package consulo.csharp.ide.actions.generate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.actions.generate.memberChoose.CSharpMemberChooseObject;
import consulo.csharp.ide.actions.generate.memberChoose.MethodChooseMember;
import consulo.csharp.ide.actions.generate.memberChoose.XXXAccessorOwnerChooseMember;
import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.CSharpXXXAccessorOwner;
import consulo.dotnet.psi.DotNetNamedElement;
import com.intellij.ide.util.MemberChooser;
import com.intellij.ide.util.MemberChooserBuilder;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiParserFacade;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.PairConsumer;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 16.12.14
 */
public abstract class GenerateImplementOrOverrideMemberHandler implements LanguageCodeInsightActionHandler
{
	@RequiredDispatchThread
	@Override
	public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file)
	{
		final CSharpTypeDeclaration typeDeclaration = CSharpGenerateAction.findTypeDeclaration(editor, file);
		if(typeDeclaration == null)
		{
			return;
		}

		Collection<? extends PsiElement> psiElements = getItems(typeDeclaration);
		if(psiElements.isEmpty())
		{
			return;
		}

		boolean canGenerateBlock = !typeDeclaration.isInterface();
		List<CSharpMemberChooseObject> memberChooseObjects = new ArrayList<CSharpMemberChooseObject>(psiElements.size());
		PairConsumer<PsiElement, StringBuilder> returnAppender = new PairConsumer<PsiElement, StringBuilder>()
		{
			@Override
			@RequiredDispatchThread
			public void consume(PsiElement element, StringBuilder builder)
			{
				GenerateImplementOrOverrideMemberHandler.this.appendReturnStatement(builder, element);
			}
		};
		PairConsumer<PsiElement, StringBuilder> additionalModifiersAppender = new PairConsumer<PsiElement, StringBuilder>()
		{
			@Override
			@RequiredDispatchThread
			public void consume(PsiElement element, StringBuilder builder)
			{
				GenerateImplementOrOverrideMemberHandler.this.appendAdditionalModifiers(builder, element);
			}
		};

		for(final PsiElement psiElement : psiElements)
		{
			if(psiElement instanceof CSharpMethodDeclaration)
			{
				memberChooseObjects.add(new MethodChooseMember((CSharpMethodDeclaration) psiElement, additionalModifiersAppender, returnAppender, canGenerateBlock));
			}
			else if(psiElement instanceof CSharpPropertyDeclaration || psiElement instanceof CSharpIndexMethodDeclaration)
			{
				memberChooseObjects.add(new XXXAccessorOwnerChooseMember((CSharpXXXAccessorOwner) psiElement, additionalModifiersAppender, returnAppender, canGenerateBlock));
			}
		}

		final MemberChooserBuilder<CSharpMemberChooseObject<?>> builder = new MemberChooserBuilder<CSharpMemberChooseObject<?>>(project);
		builder.setTitle(getTitle());
		builder.allowMultiSelection(true);

		final MemberChooser<CSharpMemberChooseObject<?>> memberChooser = builder.createBuilder(ContainerUtil.toArray(memberChooseObjects, CSharpMemberChooseObject.ARRAY_FACTORY));

		if(!memberChooser.showAndGet())
		{
			return;
		}

		final List<CSharpMemberChooseObject<?>> selectedElements = memberChooser.getSelectedElements();
		if(selectedElements == null)
		{
			return;
		}

		for(CSharpMemberChooseObject<?> selectedElement : selectedElements)
		{
			generateMember(typeDeclaration, editor, file, selectedElement);
		}
	}

	@NotNull
	public abstract String getTitle();

	@RequiredReadAction
	public abstract void appendAdditionalModifiers(@NotNull StringBuilder builder, @NotNull PsiElement item);

	@RequiredReadAction
	public abstract void appendReturnStatement(@NotNull StringBuilder builder, @NotNull PsiElement item);

	@NotNull
	@RequiredReadAction
	public abstract Collection<? extends PsiElement> getItems(@NotNull CSharpTypeDeclaration typeDeclaration);

	@RequiredReadAction
	private static void generateMember(@NotNull final CSharpTypeDeclaration typeDeclaration,
			@NotNull final Editor editor,
			@NotNull final PsiFile file,
			@NotNull CSharpMemberChooseObject<?> chooseMember)
	{
		String text = chooseMember.getText();

		final DotNetNamedElement namedElement = CSharpFileFactory.createMember(typeDeclaration.getProject(), text);

		final int offset = editor.getCaretModel().getOffset();
		PsiElement elementAt = file.findElementAt(offset);
		assert elementAt != null;

		PsiElement brace = typeDeclaration.getLeftBrace();
		if(brace != null && brace.getTextOffset() > offset)
		{
			elementAt = file.findElementAt(brace.getTextOffset() + 1);
		}

		final PsiElement temp = elementAt;
		new WriteCommandAction.Simple<Object>(file.getProject(), file)
		{
			@Override
			protected void run() throws Throwable
			{
				final PsiElement psiElement = typeDeclaration.addAfter(namedElement, temp);
				typeDeclaration.addAfter(PsiParserFacade.SERVICE.getInstance(file.getProject()).createWhiteSpaceFromText("\n"), psiElement);

				PsiDocumentManager documentManager = PsiDocumentManager.getInstance(getProject());

				documentManager.doPostponedOperationsAndUnblockDocument(editor.getDocument());

				documentManager.commitDocument(editor.getDocument());

				CodeStyleManager.getInstance(getProject()).reformat(psiElement);
			}
		}.execute();
	}

	@Override
	public boolean startInWriteAction()
	{
		return false;
	}

	@Override
	public boolean isValidFor(Editor editor, PsiFile file)
	{
		CSharpTypeDeclaration typeDeclaration = CSharpGenerateAction.findTypeDeclaration(editor, file);
		if(typeDeclaration == null)
		{
			return false;
		}
		return !getItems(typeDeclaration).isEmpty();
	}
}
