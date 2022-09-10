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

package consulo.csharp.ide.codeInsight.actions;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import consulo.csharp.ide.completion.expected.ExpectedTypeVisitor;
import consulo.csharp.ide.liveTemplates.expression.ReturnStatementExpression;
import consulo.csharp.ide.liveTemplates.expression.TypeRefExpression;
import consulo.csharp.lang.impl.psi.CSharpContextUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.CSharpAccessModifier;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetMemberOwner;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.editor.template.Template;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

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

	@Nonnull
	@Override
	public String getTemplateText()
	{
		return "Create method ''{0}({1})''";
	}

	@Override
	@Nullable
	@RequiredReadAction
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
				DotNetTypeRef typeRef = ((DotNetExpression) qualifier).toTypeRef(true);

				DotNetTypeResolveResult typeResolveResult = typeRef.resolve();

				PsiElement typeResolveResultElement = typeResolveResult.getElement();
				if(typeResolveResultElement instanceof DotNetMemberOwner && typeResolveResultElement.isWritable())
				{
					return new CreateUnresolvedElementFixContext(element, (DotNetMemberOwner) typeResolveResultElement);
				}
			}
		}
		return null;
	}

	@RequiredReadAction
	@Override
	public void buildTemplate(@Nonnull CreateUnresolvedElementFixContext context, CSharpContextUtil.ContextType contextType, @Nonnull PsiFile file, @Nonnull Template template)
	{
		boolean forInterface = context.getTargetForGenerate() instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) context.getTargetForGenerate()).isInterface();

		if(!forInterface)
		{
			template.addTextSegment(calcModifier(context).getPresentableText());
			template.addTextSegment(" ");
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

	@Nonnull
	static CSharpAccessModifier calcModifier(@Nonnull CreateUnresolvedElementFixContext context)
	{
		final CSharpTypeDeclaration thisType = PsiTreeUtil.getParentOfType(context.getExpression(), CSharpTypeDeclaration.class);
		if(thisType == null)
		{
			return CSharpAccessModifier.PUBLIC;
		}

		if(context.getTargetForGenerate() == thisType)
		{
			return CSharpAccessModifier.PRIVATE;
		}

		return CSharpAccessModifier.PUBLIC;
	}
}
