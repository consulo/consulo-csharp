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

import consulo.annotation.access.RequiredReadAction;
import consulo.component.util.localize.BundleBase;
import consulo.csharp.impl.ide.liveTemplates.expression.ReturnStatementExpression;
import consulo.csharp.impl.ide.liveTemplates.expression.TypeRefExpression;
import consulo.csharp.lang.impl.psi.CSharpContextUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetMemberOwner;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.editor.template.ConstantNode;
import consulo.language.editor.template.Template;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

	@Nonnull
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

			builder.append(CSharpTypeRefPresentationUtil.buildShortText(parameterInfo.getTypeRef()));
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

					DotNetTypeResolveResult typeResolveResult = typeRef.resolve();

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

	@RequiredReadAction
	@Override
	public void buildTemplate(@Nonnull CreateUnresolvedElementFixContext context, CSharpContextUtil.ContextType contextType, @Nonnull PsiFile file, @Nonnull Template template)
	{
		template.addTextSegment(CreateUnresolvedMethodFix.calcModifier(context).getPresentableText());
		template.addTextSegment(" ");
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

	protected void buildParameterList(@Nonnull CreateUnresolvedElementFixContext context, @Nonnull PsiFile file, @Nonnull Template template)
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

			template.addVariable(new ConstantNode(CSharpTypeRefPresentationUtil.buildShortText(parameterInfo.getTypeRef())), true);
			template.addTextSegment(" ");
			template.addVariable(new ConstantNode(parameterInfo.getNotNullName()), true);

		}
		template.addTextSegment(")");
	}
}
