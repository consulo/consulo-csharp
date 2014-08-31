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

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImpl;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.BundleBase;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 01.07.14
 */
public abstract class CreateMethodBaseFix extends PsiElementBaseIntentionAction
{
	private String myReferenceName;

	public CreateMethodBaseFix(String referenceName)
	{
		myReferenceName = referenceName;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull final PsiElement element) throws IncorrectOperationException
	{
		final DotNetQualifiedElement qualifiedElement = PsiTreeUtil.getParentOfType(element, DotNetQualifiedElement.class);
		if(qualifiedElement == null)
		{
			return;
		}

		StringBuilder builder = new StringBuilder();
		builder.append("public ");

		if(qualifiedElement instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) qualifiedElement).hasModifier(DotNetModifier.STATIC))
		{
			builder.append("static ");
		}

		DotNetTypeRef returnTypeRef = new DotNetTypeRefByQName(DotNetTypes.System.Object, CSharpTransform.INSTANCE); //TODO [VISTALL]

		builder.append(returnTypeRef.getPresentableText()).append(" ");
		builder.append(myReferenceName);
		builder.append("(");

		CSharpMethodCallExpressionImpl parent = (CSharpMethodCallExpressionImpl) element.getParent().getParent();

		DotNetExpression[] parameterExpressions = parent.getParameterExpressions();

		for(int i = 0; i < parameterExpressions.length; i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}
			DotNetExpression parameterExpression = parameterExpressions[i];
			builder.append(parameterExpression.toTypeRef(false).getPresentableText());
			builder.append(" p").append(i);
		}
		builder.append(")\n");
		builder.append("{");

		String defaultValueForType = MethodGenerateUtil.getDefaultValueForType(returnTypeRef, element);
		if(defaultValueForType != null)
		{
			builder.append("return ");
			builder.append(defaultValueForType);
			builder.append(";\n");
		}
		builder.append("}");

		final DotNetLikeMethodDeclaration method = CSharpFileFactory.createMethod(project, builder);


		new WriteCommandAction.Simple<Object>(project, element.getContainingFile())
		{

			@Override
			protected void run() throws Throwable
			{
				PsiElement parent1 = qualifiedElement.getParent();
				parent1.addAfter(method, qualifiedElement);

			}
		}.execute();
	}

	public abstract String getTemplateText();

	@NotNull
	@Override
	public String getText()
	{
		return BundleBase.format(getTemplateText(), myReferenceName);
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element)
	{
		ASTNode node = element.getNode();
		if(node != null && node.getElementType() == CSharpTokens.IDENTIFIER)
		{
			PsiElement parent = element.getParent();
			if(parent instanceof CSharpReferenceExpressionImpl && ((CSharpReferenceExpressionImpl) parent).kind() == CSharpReferenceExpression
					.ResolveToKind.METHOD)
			{
				return true;
			}
		}
		return false;
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}
}
