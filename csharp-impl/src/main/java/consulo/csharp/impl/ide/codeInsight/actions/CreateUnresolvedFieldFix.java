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
import consulo.csharp.impl.ide.completion.expected.ExpectedTypeInfo;
import consulo.csharp.impl.ide.completion.expected.ExpectedTypeVisitor;
import consulo.csharp.impl.ide.liveTemplates.expression.TypeRefExpression;
import consulo.csharp.lang.impl.psi.CSharpContextUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.editor.template.Template;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 06.01.15
 */
public class CreateUnresolvedFieldFix extends CreateUnresolvedElementFix
{
	public CreateUnresolvedFieldFix(CSharpReferenceExpression expression)
	{
		super(expression);
	}

	@Nonnull
	@Override
	public PsiElement getElementForAfterAdd(@Nonnull DotNetNamedElement[] elements, @Nonnull CSharpBodyWithBraces targetForGenerate)
	{
		PsiElement last = targetForGenerate.getLeftBrace();
		for(DotNetNamedElement element : elements)
		{
			if(element instanceof DotNetVariable)
			{
				last = element;
			}
		}
		return last;
	}

	@Override
	protected CreateUnresolvedElementFixContext createGenerateContext()
	{
		CSharpReferenceExpression element = myPointer.getElement();
		if(element == null)
		{
			return null;
		}

		CSharpReferenceExpression.ResolveToKind kind = element.kind();
		if(kind == CSharpReferenceExpression.ResolveToKind.ANY_MEMBER)
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
		else if(kind == CSharpReferenceExpression.ResolveToKind.FIELD_OR_PROPERTY)
		{
			CSharpCallArgumentListOwner callArgumentListOwner = PsiTreeUtil.getParentOfType(element, CSharpCallArgumentListOwner.class);

			DotNetTypeRef resolvedTypeRef = DotNetTypeRef.ERROR_TYPE;
			if(callArgumentListOwner instanceof CSharpNewExpression)
			{
				resolvedTypeRef = ((CSharpNewExpression) callArgumentListOwner).toTypeRef(false);
			}
			else if(callArgumentListOwner instanceof DotNetAttribute)
			{
				resolvedTypeRef = ((DotNetAttribute) callArgumentListOwner).toTypeRef();
			}
			else
			{
				throw new IllegalArgumentException(callArgumentListOwner == null ? "null" : callArgumentListOwner.getClass().getName());
			}

			if(resolvedTypeRef == DotNetTypeRef.ERROR_TYPE)
			{
				return null;
			}

			DotNetTypeResolveResult typeResolveResult = resolvedTypeRef.resolve();
			PsiElement typeResolveResultElement = typeResolveResult.getElement();
			if(!(typeResolveResultElement instanceof CSharpTypeDeclaration))
			{
				return null;
			}
			return new CreateUnresolvedElementFixContext(element, (DotNetMemberOwner) typeResolveResultElement);
		}
		return null;
	}

	@Nonnull
	@Override
	public String getText()
	{
		return BundleBase.format("Create field ''{0}''", myReferenceName);
	}

	@RequiredReadAction
	@Override
	public void buildTemplate(@Nonnull CreateUnresolvedElementFixContext context, CSharpContextUtil.ContextType contextType, @Nonnull PsiFile file, @Nonnull Template template)
	{
		template.addTextSegment("public ");

		if(contextType == CSharpContextUtil.ContextType.STATIC)
		{
			template.addTextSegment("static ");
		}

		// get expected from method call expression not reference
		List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(context.getExpression());

		if(!expectedTypeRefs.isEmpty())
		{
			template.addVariable(new TypeRefExpression(expectedTypeRefs, file), true);
		}
		else
		{
			template.addVariable(new TypeRefExpression(new CSharpTypeRefByQName(file, DotNetTypes.System.Object), file), true);
		}

		template.addTextSegment(" ");
		template.addTextSegment(myReferenceName);
		template.addTextSegment(";");
		template.addEndVariable();
	}
}
