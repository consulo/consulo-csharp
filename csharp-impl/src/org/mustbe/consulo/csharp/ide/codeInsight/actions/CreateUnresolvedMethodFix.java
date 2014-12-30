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
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamedCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetMemberOwner;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 01.07.14
 */
public class CreateUnresolvedMethodFix extends CreateUnresolvedLikeMethodFix<CreateUnresolvedMethodFix.GenerateContext>
{
	public static class GenerateContext extends BaseLikeMethodGenerateContext
	{
		protected boolean myStaticContext;

		public GenerateContext(CSharpReferenceExpression expression, DotNetMemberOwner targetForGenerate, boolean staticContext)
		{
			super(expression, targetForGenerate);
			myStaticContext = staticContext;
		}
	}

	public CreateUnresolvedMethodFix(CSharpReferenceExpression expression)
	{
		super(expression);
	}

	@NotNull
	@Override
	public String getTemplateText()
	{
		return "Create ''{0}({1})''";
	}

	@Override
	@Nullable
	protected GenerateContext createGenerateContext()
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

	@NotNull
	@Override
	public CharSequence buildTemplateForAdd(@NotNull GenerateContext context, @NotNull PsiFile file)
	{
		val builder = new StringBuilder();
		builder.append("public ");

		if(context.myStaticContext)
		{
			builder.append("static ");
		}

		DotNetTypeRef returnTypeRef = new CSharpTypeRefByQName(DotNetTypes.System.Void);

		builder.append(CSharpTypeRefPresentationUtil.buildShortText(returnTypeRef, context.getExpression())).append(" ");
		builder.append(myReferenceName);
		builder.append("(");

		CSharpCallArgumentListOwner parent = (CSharpCallArgumentListOwner) context.getExpression().getParent();

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
				String item = ContainerUtil.getFirstItem(suggestedNames);
				builder.append(item).append(i);
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
		return builder;
	}
}
