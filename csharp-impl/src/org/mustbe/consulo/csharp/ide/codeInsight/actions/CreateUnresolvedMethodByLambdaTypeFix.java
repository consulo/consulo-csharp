/*
 * Copyright 2013-2015 must-be.org
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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.liveTemplates.expression.ReturnStatementExpression;
import org.mustbe.consulo.csharp.ide.liveTemplates.expression.TypeRefExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpContextUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetMemberOwner;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.BundleBase;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.ConstantNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 06.01.15
 */
public class CreateUnresolvedMethodByLambdaTypeFix extends CreateUnresolvedElementFix
{
	private final CSharpLambdaResolveResult myLikeMethod;

	public CreateUnresolvedMethodByLambdaTypeFix(CSharpReferenceExpression expression, CSharpLambdaResolveResult likeMethod)
	{
		super(expression);
		myLikeMethod = likeMethod;
	}

	@NotNull
	@Override
	@RequiredReadAction
	public String getText()
	{
		String arguments = buildArgumentTypeRefs();
		if(arguments == null)
		{
			return "invalid";
		}
		return BundleBase.format("Create method ''{0}({1})''", myReferenceName, arguments);
	}

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

		CSharpSimpleParameterInfo[] parameterInfos = myLikeMethod.getParameterInfos();

		for(int i = 0; i < parameterInfos.length; i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}

			CSharpSimpleParameterInfo parameterInfo = parameterInfos[i];

			builder.append(CSharpTypeRefPresentationUtil.buildShortText(parameterInfo.getTypeRef(), element));
		}
		return builder.toString();
	}

	@Override
	protected CreateUnresolvedElementFixContext createGenerateContext()
	{
		CSharpReferenceExpression element = myPointer.getElement();
		if(element == null)
		{
			return null;
		}

		if(element.kind() == CSharpReferenceExpression.ResolveToKind.ANY_MEMBER)
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
					return new CreateUnresolvedElementFixContext(element, (DotNetMemberOwner) parent);
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
						return new CreateUnresolvedElementFixContext(element, (DotNetMemberOwner) typeResolveResultElement);
					}
				}
			}
		}
		return null;
	}

	@Override
	public void buildTemplate(@NotNull CreateUnresolvedElementFixContext context, CSharpContextUtil.ContextType contextType, @NotNull PsiFile file, @NotNull Template template)
	{
		template.addTextSegment("public ");
		if(contextType == CSharpContextUtil.ContextType.STATIC)
		{
			template.addTextSegment("static ");
		}
		template.addVariable(new TypeRefExpression(myLikeMethod.getReturnTypeRef(), file), true);
		template.addTextSegment(" ");
		template.addTextSegment(myReferenceName);

		buildParameterList(context, file, template);

		template.addTextSegment("{\n");

		template.addVariable("$RETURN_STATEMENT$", new ReturnStatementExpression(), false);
		template.addEndVariable();

		template.addTextSegment("}");
	}

	protected void buildParameterList(@NotNull CreateUnresolvedElementFixContext context, @NotNull PsiFile file, @NotNull Template template)
	{
		template.addTextSegment("(");

		CSharpSimpleParameterInfo[] parameterInfos = myLikeMethod.getParameterInfos();

		for(int i = 0; i < parameterInfos.length; i++)
		{
			if(i != 0)
			{
				template.addTextSegment(", ");
			}

			CSharpSimpleParameterInfo parameterInfo = parameterInfos[i];

			template.addVariable(new ConstantNode(CSharpTypeRefPresentationUtil.buildShortText(parameterInfo.getTypeRef(), context.getExpression())), true);
			template.addTextSegment(" ");
			template.addVariable(new ConstantNode(parameterInfo.getNotNullName()), true);

		}
		template.addTextSegment(")");
	}
}
