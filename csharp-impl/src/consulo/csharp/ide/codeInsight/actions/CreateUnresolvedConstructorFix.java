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

package consulo.csharp.ide.codeInsight.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpBodyWithBraces;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpContextUtil;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.DotNetMemberOwner;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetQualifiedElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public class CreateUnresolvedConstructorFix extends CreateUnresolvedLikeMethodFix
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
	public CreateUnresolvedElementFixContext createGenerateContext()
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
				return new CreateUnresolvedElementFixContext(element, (DotNetMemberOwner) parent);
			}
		}
		return null;
	}

	@RequiredReadAction
	@Override
	public void buildTemplate(@NotNull CreateUnresolvedElementFixContext context, CSharpContextUtil.ContextType contextType, @NotNull PsiFile file, @NotNull Template template)
	{
		template.addTextSegment("public ");
		template.addTextSegment(myReferenceName);
		buildParameterList(context, file, template);
		template.addTextSegment("{\n");
		template.addEndVariable();
		template.addTextSegment("}");
	}
}
