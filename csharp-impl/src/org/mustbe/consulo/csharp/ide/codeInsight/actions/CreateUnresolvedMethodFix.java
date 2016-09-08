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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeVisitor;
import org.mustbe.consulo.csharp.ide.liveTemplates.expression.ReturnStatementExpression;
import org.mustbe.consulo.csharp.ide.liveTemplates.expression.TypeRefExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpContextUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetMemberOwner;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.codeInsight.template.Template;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 01.07.14
 */
public class CreateUnresolvedMethodFix extends CreateUnresolvedLikeMethodFix
{
	public CreateUnresolvedMethodFix(CSharpReferenceExpression expression)
	{
		super(expression);
	}

	@NotNull
	@Override
	public String getTemplateText()
	{
		return "Create method ''{0}({1})''";
	}

	@Override
	@Nullable
	protected CreateUnresolvedElementFixContext createGenerateContext()
	{
		CSharpReferenceExpression element = myPointer.getElement();
		if(element == null)
		{
			return null;
		}

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
	public void buildTemplate(@NotNull CreateUnresolvedElementFixContext context, CSharpContextUtil.ContextType contextType, @NotNull PsiFile file, @NotNull Template template)
	{
		boolean forInterface = context.getTargetForGenerate() instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) context.getTargetForGenerate()).isInterface();

		if(!forInterface)
		{
			template.addTextSegment("public ");
			if(contextType == CSharpContextUtil.ContextType.STATIC)
			{
				template.addTextSegment("static ");
			}
		}

		// get expected from method call expression not reference
		List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(context.getExpression().getParent());

		if(!expectedTypeRefs.isEmpty())
		{
			template.addVariable(new TypeRefExpression(expectedTypeRefs, file), true);
		}
		else
		{
			template.addVariable(new TypeRefExpression(new CSharpTypeRefByQName(file, DotNetTypes.System.Void), file), true);
		}

		template.addTextSegment(" ");
		template.addTextSegment(myReferenceName);

		buildParameterList(context, file, template);

		if(forInterface)
		{
			template.addTextSegment(";");
		}
		else
		{
			template.addTextSegment("{\n");

			template.addVariable("$RETURN_STATEMENT$", new ReturnStatementExpression(), false);
			template.addEndVariable();

			template.addTextSegment("}");
		}
	}
}
