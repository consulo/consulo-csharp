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
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.refactoring.util.CSharpNameSuggesterUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamedCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.BundleBase;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.ConstantNode;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public abstract class CreateUnresolvedLikeMethodFix extends CreateUnresolvedElementFix
{
	public CreateUnresolvedLikeMethodFix(CSharpReferenceExpression expression)
	{
		super(expression);
	}

	@NotNull
	@Override
	@RequiredDispatchThread
	public String getText()
	{
		String arguments = buildArgumentTypeRefs();
		if(arguments == null)
		{
			return "invalid";
		}
		return BundleBase.format(getTemplateText(), myReferenceName, arguments);
	}

	@NotNull
	public abstract String getTemplateText();

	@Nullable
	@RequiredReadAction
	public String buildArgumentTypeRefs()
	{
		CSharpReferenceExpression element = myPointer.getElement();
		if(element == null)
		{
			return null;
		}

		StringBuilder builder = new StringBuilder();

		CSharpCallArgumentListOwner parent = PsiTreeUtil.getParentOfType(element, CSharpCallArgumentListOwner.class);

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
				builder.append(CSharpTypeRefPresentationUtil.buildShortText(typeRef, element));
			}
			else
			{
				builder.append("object");
			}
		}
		return builder.toString();
	}

	@RequiredReadAction
	protected void buildParameterList(@NotNull CreateUnresolvedElementFixContext context, @NotNull PsiFile file, @NotNull Template template)
	{
		template.addTextSegment("(");

		CSharpCallArgumentListOwner parent = PsiTreeUtil.getParentOfType(context.getExpression(), CSharpCallArgumentListOwner.class);

		assert parent != null;

		CSharpCallArgument[] callArguments = parent.getCallArguments();

		for(int i = 0; i < callArguments.length; i++)
		{
			if(i != 0)
			{
				template.addTextSegment(", ");
			}

			CSharpCallArgument callArgument = callArguments[i];

			DotNetTypeRef parameterTypeRef = new CSharpTypeRefByQName(callArgument, DotNetTypes.System.Object);
			DotNetExpression argumentExpression = callArgument.getArgumentExpression();
			if(argumentExpression != null)
			{
				parameterTypeRef = argumentExpression.toTypeRef(false);
			}

			template.addVariable(new ConstantNode(CSharpTypeRefPresentationUtil.buildShortText(parameterTypeRef, context.getExpression())), true);

			template.addTextSegment(" ");
			if(callArgument instanceof CSharpNamedCallArgument)
			{
				template.addVariable(new ConstantNode(((CSharpNamedCallArgument) callArgument).getName()), true);
			}
			else
			{
				Collection<String> suggestedNames = CSharpNameSuggesterUtil.getSuggestedNames(argumentExpression);
				String item = ContainerUtil.getFirstItem(suggestedNames);
				template.addVariable(new ConstantNode(item + i), true);
			}
		}
		template.addTextSegment(")");
	}
}
