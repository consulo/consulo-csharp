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

package org.mustbe.consulo.csharp.ide.codeInsight.actions;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.reflactoring.util.CSharpNameSuggesterUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamedCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetMemberOwner;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.BundleBase;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.codeStyle.CodeEditUtil;
import com.intellij.psi.impl.source.codeStyle.IndentHelper;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 01.07.14
 */
public class CreateUnresolvedMethodFix extends BaseIntentionAction
{
	public static class GenerateContext
	{
		CSharpReferenceExpression myExpression;
		DotNetMemberOwner myTargetForGenerate;
		boolean myStaticContext;

		public GenerateContext(CSharpReferenceExpression expression, DotNetMemberOwner targetForGenerate, boolean staticContext)
		{
			myExpression = expression;
			myTargetForGenerate = targetForGenerate;
			myStaticContext = staticContext;
		}
	}

	private final SmartPsiElementPointer<CSharpReferenceExpression> myPointer;
	private final String myReferenceName;

	public CreateUnresolvedMethodFix(CSharpReferenceExpression expression)
	{
		myPointer = SmartPointerManager.getInstance(expression.getProject()).createSmartPsiElementPointer(expression);
		myReferenceName = expression.getReferenceName();
	}

	@NotNull
	@Override
	public String getText()
	{
		return BundleBase.format("Create ''{0}'' method", myReferenceName);
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
	{
		return resolveTargets() != null;
	}

	@Nullable
	public GenerateContext resolveTargets()
	{
		CSharpReferenceExpression element = myPointer.getElement();
		if(element == null)
		{
			return null;
		}

		//TODO [VISTALL] creating for ANY_MEMBER
		if(element.kind() == CSharpReferenceExpression.ResolveToKind.METHOD)
		{
			PsiElement qualifier = element.getQualifier();
			if(qualifier == null)
			{
				final DotNetQualifiedElement qualifiedElement = PsiTreeUtil.getParentOfType(element, DotNetQualifiedElement.class);
				if(qualifiedElement == null)
				{
					return null;
				}

				PsiElement parent = qualifiedElement.getParent();
				if(parent instanceof DotNetMemberOwner && parent.isWritable())
				{
					boolean staticModifier = qualifiedElement instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) qualifiedElement)
							.hasModifier(DotNetModifier.STATIC);
					return new GenerateContext(element, (DotNetMemberOwner) parent, staticModifier);
				}
			}
			else
			{
				if(qualifier instanceof DotNetExpression)
				{
					DotNetTypeRef typeRef = ((DotNetExpression) qualifier).toTypeRef(true);

					DotNetTypeResolveResult typeResolveResult = typeRef.resolve(element);

					PsiElement typeResolveResultElement = typeResolveResult.getElement();
					if(typeResolveResultElement instanceof DotNetMemberOwner && typeResolveResultElement.isWritable())
					{
						boolean staticModifier = false;
						if(qualifier instanceof CSharpReferenceExpression)
						{
							PsiElement resolved = ((CSharpReferenceExpression) qualifier).resolve();
							if(resolved instanceof DotNetTypeDeclaration)
							{
								staticModifier = true;
							}
						}
						return new GenerateContext(element, (DotNetMemberOwner) typeResolveResultElement, staticModifier);
					}
				}
			}
		}
		return null;
	}

	@Override
	public void invoke(@NotNull Project project, final Editor editor, PsiFile file) throws IncorrectOperationException
	{
		val generateContext = resolveTargets();
		if(generateContext == null)
		{
			return;
		}
		PsiDocumentManager.getInstance(project).commitAllDocuments();

		val builder = new StringBuilder();
		builder.append("public ");

		if(generateContext.myStaticContext)
		{
			builder.append("static ");
		}

		DotNetTypeRef returnTypeRef = new DotNetTypeRefByQName(DotNetTypes.System.Void, CSharpTransform.INSTANCE); //TODO [VISTALL]

		builder.append(CSharpTypeRefPresentationUtil.buildShortText(returnTypeRef, generateContext.myExpression)).append(" ");
		builder.append(myReferenceName);
		builder.append("(");

		CSharpCallArgumentListOwner parent = (CSharpCallArgumentListOwner) generateContext.myExpression.getParent();

		CSharpCallArgument[] callArguments = parent.getCallArguments();

		for(int i = 0; i < callArguments.length; i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}

			CSharpCallArgument callArgument = callArguments[i];

			DotNetExpression argumentExpression = callArgument.getArgumentExpression();
			if(argumentExpression != null)
			{
				DotNetTypeRef typeRef = argumentExpression.toTypeRef(false);
				builder.append(CSharpTypeRefPresentationUtil.buildShortText(typeRef, generateContext.myExpression));
			}
			else
			{
				builder.append("object");
			}

			builder.append(" ");
			if(callArgument instanceof CSharpNamedCallArgument)
			{
				builder.append(((CSharpNamedCallArgument) callArgument).getName());
			}
			else
			{
				Collection<String> suggestedNames = CSharpNameSuggesterUtil.getSuggestedNames(argumentExpression);
				builder.append(ContainerUtil.getFirstItem(suggestedNames)).append(i);
			}
		}
		builder.append(")");
		builder.append("{");

		String defaultValueForType = MethodGenerateUtil.getDefaultValueForType(returnTypeRef, file);
		if(defaultValueForType != null)
		{
			builder.append("return ");
			builder.append(defaultValueForType);
			builder.append(";\n");
		}
		builder.append("}");


		new WriteCommandAction.Simple<Object>(project, file)
		{
			@Override
			protected void run() throws Throwable
			{
				final DotNetLikeMethodDeclaration newMethod = CSharpFileFactory.createMethod(getProject(), builder);

				DotNetMemberOwner targetForGenerate = generateContext.myTargetForGenerate;

				DotNetNamedElement[] members = targetForGenerate.getMembers();
				if(members.length == 0)
				{

				}
				else
				{
					DotNetNamedElement lastElement = ArrayUtil.getLastElement(members);
					assert lastElement != null;

					ASTNode node = lastElement.getNode();
					ASTNode treeNext = node.getTreeNext();

					targetForGenerate.getNode().addLeaf(CSharpTokens.WHITE_SPACE, "\n", treeNext);

					int indent = IndentHelper.getInstance().getIndent(lastElement.getProject(), lastElement.getContainingFile().getFileType(),
							lastElement.getNode());
					CodeEditUtil.setOldIndentation((TreeElement) newMethod.getNode(), indent);
					targetForGenerate.getNode().addChild(newMethod.getNode(), treeNext);

					PsiDocumentManager.getInstance(getProject()).doPostponedOperationsAndUnblockDocument(editor.getDocument());
					CodeStyleManager.getInstance(getProject()).reformat(newMethod);
				}
			}
		}.execute();
	}
}
