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
import org.mustbe.consulo.csharp.ide.refactoring.util.CSharpNameSuggesterUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpBodyWithBraces;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamedCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetMemberOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public class CreateUnresolvedConstructorFix extends CreateUnresolvedLikeMethodFix<BaseLikeMethodGenerateContext>
{
	public CreateUnresolvedConstructorFix(CSharpReferenceExpression expression)
	{
		super(expression);
	}

	@NotNull
	@Override
	public PsiElement getElementForAfterAdd(@NotNull DotNetNamedElement[] elements, @NotNull CSharpBodyWithBraces targetForGenerate)
	{
		PsiElement last = targetForGenerate.getLeftBrace();
		for(DotNetNamedElement element : elements)
		{
			if(element instanceof CSharpConstructorDeclaration)
			{
				last = element;
			}
		}
		return last;
	}

	@NotNull
	@Override
	public String getTemplateText()
	{
		return "Create constructor";
	}

	@Override
	@Nullable
	public BaseLikeMethodGenerateContext createGenerateContext()
	{
		CSharpReferenceExpression element = myPointer.getElement();
		if(element == null)
		{
			return null;
		}

		if(element.kind() == CSharpReferenceExpression.ResolveToKind.CONSTRUCTOR)
		{
			final DotNetQualifiedElement qualifiedElement = PsiTreeUtil.getParentOfType(element, DotNetQualifiedElement.class);
			if(qualifiedElement == null)
			{
				return null;
			}

			PsiElement parent = qualifiedElement.getParent();
			if(parent instanceof DotNetMemberOwner && parent.isWritable())
			{
				return new BaseLikeMethodGenerateContext(element, (DotNetMemberOwner) parent);
			}
		}
		return null;
	}

	@NotNull
	@Override
	public CharSequence buildTemplateForAdd(@NotNull BaseLikeMethodGenerateContext context, @NotNull PsiFile file)
	{
		val builder = new StringBuilder();
		builder.append("public ");

		builder.append(myReferenceName);
		builder.append("(");

		CSharpCallArgumentListOwner parent = PsiTreeUtil.getParentOfType(context.getExpression(), CSharpCallArgumentListOwner.class);

		assert parent != null;

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
				builder.append(CSharpTypeRefPresentationUtil.buildShortText(typeRef, context.getExpression()));
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
		builder.append("}");
		return builder;
	}
}
