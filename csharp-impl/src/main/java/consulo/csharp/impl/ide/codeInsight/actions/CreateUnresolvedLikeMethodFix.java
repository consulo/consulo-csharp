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

package consulo.csharp.impl.ide.codeInsight.actions;

import consulo.language.editor.template.Template;
import consulo.language.editor.template.ConstantNode;
import consulo.annotation.access.RequiredReadAction;
import consulo.component.util.localize.BundleBase;
import consulo.csharp.impl.ide.refactoring.util.CSharpNameSuggesterUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.impl.DebugUtil;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.logging.Logger;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.collection.ContainerUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public abstract class CreateUnresolvedLikeMethodFix extends CreateUnresolvedElementFix
{
	private static final Logger LOG = Logger.getInstance(CreateUnresolvedLikeMethodFix.class);

	public CreateUnresolvedLikeMethodFix(CSharpReferenceExpression expression)
	{
		super(expression);
	}

	@Nonnull
	@Override
	@RequiredUIAccess
	public String getText()
	{
		String arguments = buildArgumentTypeRefs();
		if(arguments == null)
		{
			return "invalid";
		}
		return BundleBase.format(getTemplateText(), myReferenceName, arguments);
	}

	@Nonnull
	public abstract String getTemplateText();

	@Nullable
	@RequiredReadAction
	public String buildArgumentTypeRefs()
	{
		CSharpReferenceExpression element = myPointer.getElement();
		if(element == null || !element.isValid())
		{
			return null;
		}

		StringBuilder builder = new StringBuilder();

		CSharpCallArgumentListOwner parent = PsiTreeUtil.getParentOfType(element, CSharpCallArgumentListOwner.class);

		if(parent == null)
		{
			LOG.error("Can't find parent by 'CSharpCallArgumentListOwner'. Element: " + DebugUtil.psiToString(element, true));
			return null;
		}

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
				builder.append(CSharpTypeRefPresentationUtil.buildShortText(typeRef));
			}
			else
			{
				builder.append("object");
			}
		}
		return builder.toString();
	}

	@RequiredReadAction
	protected void buildParameterList(@Nonnull CreateUnresolvedElementFixContext context, @Nonnull PsiFile file, @Nonnull Template template)
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

			template.addVariable(new ConstantNode(CSharpTypeRefPresentationUtil.buildShortText(parameterTypeRef)), true);

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
