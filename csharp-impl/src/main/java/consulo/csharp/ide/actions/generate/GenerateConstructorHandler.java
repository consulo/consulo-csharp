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

package consulo.csharp.ide.actions.generate;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.function.Processor;
import consulo.codeEditor.Editor;
import consulo.csharp.ide.actions.generate.memberChoose.CSharpVariableChooseObject;
import consulo.csharp.ide.actions.generate.memberChoose.ConstructorChooseMember;
import consulo.csharp.ide.completion.expected.ExpectedUsingInfo;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.resolve.CSharpResolveContextUtil;
import consulo.csharp.lang.impl.psi.source.CSharpTypeDeclarationImplUtil;
import consulo.csharp.lang.impl.psi.source.resolve.AsPsiElementProcessor;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.impl.psi.source.resolve.ExecuteTarget;
import consulo.csharp.lang.impl.psi.source.resolve.MemberResolveScopeProcessor;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.csharp.lang.psi.resolve.StaticResolveSelectors;
import consulo.dotnet.psi.DotNetConstructorDeclaration;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.action.CodeInsightActionHandler;
import consulo.language.editor.generation.ClassMember;
import consulo.language.editor.generation.MemberChooserBuilder;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiParserFacade;
import consulo.language.psi.resolve.ResolveState;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 25.06.14
 */
public class GenerateConstructorHandler implements CodeInsightActionHandler
{
	@RequiredUIAccess
	@Override
	public void invoke(@Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiFile file)
	{
		PsiDocumentManager.getInstance(project).commitAllDocuments();

		final CSharpTypeDeclaration typeDeclaration = CSharpGenerateAction.findTypeDeclaration(editor, file);
		if(typeDeclaration == null)
		{
			return;
		}

		Pair<DotNetTypeDeclaration, DotNetGenericExtractor> pair = CSharpTypeDeclarationImplUtil.resolveBaseType(typeDeclaration, typeDeclaration);
		if(pair == null)
		{
			return;
		}
		final DotNetTypeDeclaration baseType = pair.getFirst();

		if(!(baseType instanceof CSharpTypeDeclaration))
		{
			return;
		}

		AsPsiElementProcessor psiElementProcessor = new AsPsiElementProcessor();
		MemberResolveScopeProcessor memberResolveScopeProcessor = new MemberResolveScopeProcessor(CSharpResolveOptions.build().element(typeDeclaration), psiElementProcessor, new
				ExecuteTarget[]{ExecuteTarget.ELEMENT_GROUP});

		ResolveState resolveState = ResolveState.initial();
		resolveState = resolveState.put(CSharpResolveUtil.SELECTOR, StaticResolveSelectors.CONSTRUCTOR_GROUP);
		resolveState = resolveState.put(CSharpResolveUtil.EXTRACTOR, pair.getSecond());

		CSharpResolveUtil.walkChildren(memberResolveScopeProcessor, baseType, false, false, resolveState);

		List<ConstructorChooseMember> members = new ArrayList<>();
		for(PsiElement psiElement : psiElementProcessor.getElements())
		{
			if(psiElement instanceof CSharpElementGroup)
			{
				for(PsiElement element : ((CSharpElementGroup<?>) psiElement).getElements())
				{
					members.add(new ConstructorChooseMember((DotNetConstructorDeclaration) element));
				}
			}
		}

		final ConstructorChooseMember[] map = ContainerUtil.toArray(members, ConstructorChooseMember.ARRAY_FACTORY);

		if(map.length == 1)
		{
			generateConstructor(typeDeclaration, editor, file, map[0]);
		}
		else
		{
			MemberChooserBuilder<ConstructorChooseMember> builder = MemberChooserBuilder.create(map);
			builder.withTitle(LocalizeValue.localizeTODO("Choose Constructor"));
			builder.withMultipleSelection();

			builder.showAsync(project, dataHolder ->
			{
				List<ClassMember> selectedElements = dataHolder.getUserData(ClassMember.KEY_OF_LIST);

				if(selectedElements.isEmpty())
				{
					return;
				}

				for(ClassMember selectedElement : selectedElements)
				{
					generateConstructor(typeDeclaration, editor, file, (ConstructorChooseMember) selectedElement);
				}
			});
		}
	}

	@RequiredUIAccess
	private static void generateConstructor(@Nonnull final CSharpTypeDeclaration typeDeclaration,
											@Nonnull final Editor editor,
											@Nonnull final PsiFile file,
											@Nonnull ConstructorChooseMember chooseMember)
	{
		CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, typeDeclaration.getResolveScope(), typeDeclaration);
		final List<DotNetVariable> fieldOrProperties = new ArrayList<>();
		context.processElements(new Processor<PsiElement>()
		{
			@Override
			public boolean process(PsiElement psiElement)
			{
				if(psiElement instanceof CSharpElementGroup)
				{
					((CSharpElementGroup) psiElement).process(this);
				}

				if(psiElement instanceof CSharpFieldDeclaration || psiElement instanceof CSharpPropertyDeclaration)
				{
					fieldOrProperties.add((DotNetVariable) psiElement);
				}
				return true;
			}
		}, false);

		if(!fieldOrProperties.isEmpty())
		{
			List<CSharpVariableChooseObject> map = ContainerUtil.map(fieldOrProperties, CSharpVariableChooseObject::new);
			MemberChooserBuilder<CSharpVariableChooseObject> builder = MemberChooserBuilder.create(ContainerUtil.toArray(map, CSharpVariableChooseObject.ARRAY_FACTORY));
			builder.withTitle(LocalizeValue.localizeTODO("Choose Fields or Properties"));
			builder.withMultipleSelection();
			builder.withMultipleSelection();

			builder.showAsync(file.getProject(), dataHolder ->
			{
				List<ClassMember> result = dataHolder.getUserData(ClassMember.KEY_OF_LIST);

				generateConstructorInner(typeDeclaration, editor, file, chooseMember, (List) result);
			});
		}
		else
		{
			generateConstructorInner(typeDeclaration, editor, file, chooseMember, List.of());
		}

	}

	@RequiredReadAction
	private static void generateConstructorInner(@Nonnull final CSharpTypeDeclaration typeDeclaration,
										  @Nonnull final Editor editor,
										  @Nonnull final PsiFile file,
										  @Nonnull ConstructorChooseMember chooseMember,
										  @Nonnull List<CSharpVariableChooseObject> additionalParameters)
	{
		String text = chooseMember.getText(additionalParameters);
		text = text.replace("$NAME$", typeDeclaration.getName());

		final DotNetLikeMethodDeclaration method = CSharpFileFactory.createMethod(typeDeclaration.getProject(), text);

		final PsiElement elementAt = getTargetForInsert(file, editor);

		ExpectedUsingInfo expectedUsingInfo = chooseMember.getExpectedUsingInfo();
		for(CSharpVariableChooseObject object : additionalParameters)
		{
			expectedUsingInfo = ExpectedUsingInfo.merge(expectedUsingInfo, object.getExpectedUsingInfo());
		}

		final ExpectedUsingInfo finalExpectedUsingInfo = expectedUsingInfo;
		new WriteCommandAction.Simple<Object>(file.getProject(), file)
		{
			@Override
			protected void run() throws Throwable
			{
				final PsiElement psiElement = typeDeclaration.addAfter(method, elementAt);

				typeDeclaration.addAfter(PsiParserFacade.getInstance(file.getProject()).createWhiteSpaceFromText("\n"), psiElement);

				if(finalExpectedUsingInfo != null)
				{
					finalExpectedUsingInfo.insertUsingBefore(typeDeclaration);
				}

				PsiDocumentManager.getInstance(getProject()).commitDocument(editor.getDocument());

				CodeStyleManager.getInstance(getProject()).reformat(psiElement);
			}
		}.execute();
	}

	@RequiredReadAction
	@Nonnull
	private static PsiElement getTargetForInsert(PsiFile file, Editor editor)
	{
		int offset = editor.getCaretModel().getOffset();

		PsiElement elementAt = file.findElementAt(offset);
		assert elementAt != null;

		CSharpTypeDeclaration declaration = CSharpGenerateAction.findTypeDeclaration(editor, file);
		if(declaration == null)
		{
			return elementAt;
		}

		PsiElement leftBrace = declaration.getLeftBrace();
		if(leftBrace == null)
		{
			return elementAt;
		}
		if(offset <= leftBrace.getTextOffset())
		{
			return leftBrace;
		}
		return elementAt;
	}

	@Override
	public boolean startInWriteAction()
	{
		return false;
	}
}
