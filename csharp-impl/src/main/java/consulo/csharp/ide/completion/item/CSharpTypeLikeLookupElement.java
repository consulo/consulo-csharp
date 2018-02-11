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

package consulo.csharp.ide.completion.item;

import javax.annotation.Nonnull;

import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.csharp.ide.completion.CSharpCompletionSorting;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpMethodUtil;
import consulo.csharp.lang.psi.CSharpNewExpression;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.CSharpUserType;
import consulo.csharp.lang.psi.impl.msil.MsilMethodAsCSharpMethodDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementDecorator;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 28.07.2015
 */
public class CSharpTypeLikeLookupElement extends LookupElementDecorator<LookupElement>
{
	@Nonnull
	public static CSharpTypeLikeLookupElement create(LookupElement delegate, DotNetGenericExtractor extractor, PsiElement expression)
	{
		CSharpNewExpression newExpression = null;
		PsiElement parent = expression == null ? null : expression.getParent();
		if(parent instanceof CSharpUserType)
		{
			PsiElement typeParent = parent.getParent();
			if(typeParent instanceof CSharpNewExpression)
			{
				newExpression = (CSharpNewExpression) typeParent;
			}
		}

		return new CSharpTypeLikeLookupElement(delegate, extractor, newExpression != null);
	}

	private DotNetGenericExtractor myExtractor;
	private boolean myAfterNew;

	public CSharpTypeLikeLookupElement(LookupElement delegate, DotNetGenericExtractor extractor, boolean afterNew)
	{
		super(delegate);
		myExtractor = extractor;
		myAfterNew = afterNew;

		PsiElement psiElement = delegate.getPsiElement();
		assert psiElement instanceof DotNetQualifiedElement;

		CSharpCompletionSorting.copyForce(delegate, this);
	}

	@Override
	@RequiredWriteAction
	public void handleInsert(InsertionContext context)
	{
		super.handleInsert(context);

		if(myAfterNew)
		{
			context.commitDocument();

			PsiDocumentManager.getInstance(context.getProject()).doPostponedOperationsAndUnblockDocument(context.getDocument());

			boolean hasParameters = false;
			if(myExtractor != DotNetGenericExtractor.EMPTY)
			{
				PsiElement psiElement = getPsiElement();
				if(psiElement instanceof CSharpTypeDeclaration)
				{
					DotNetNamedElement[] members = ((CSharpTypeDeclaration) psiElement).getMembers();
					for(DotNetNamedElement member : members)
					{
						if(member instanceof CSharpConstructorDeclaration)
						{
							int length = ((CSharpConstructorDeclaration) member).getParameters().length;
							if(length > 0)
							{
								hasParameters = true;
								break;
							}
						}
					}
				}
				else if(CSharpMethodUtil.isDelegate(psiElement))
				{
					hasParameters = true;
				}
			}

			CaretModel caretModel = context.getEditor().getCaretModel();
			int oldCaretOffset = caretModel.getOffset();

			ParenthesesInsertHandler.getInstance(true).handleInsert(context, this);

			if(!hasParameters)
			{
				caretModel.moveToOffset(oldCaretOffset + 2);
			}
		}
	}

	@Override
	@RequiredReadAction
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}

		return Comparing.equal(getOriginal().getPresentableQName(), ((CSharpTypeLikeLookupElement) o).getOriginal().getPresentableQName());
	}

	@Override
	public int hashCode()
	{
		return getOriginal().hashCode();
	}

	@Nonnull
	public DotNetGenericExtractor getExtractor()
	{
		return myExtractor;
	}

	@Nonnull
	private DotNetQualifiedElement getOriginal()
	{
		LookupElement delegate = getDelegate();
		PsiElement psiElement = delegate.getPsiElement();
		assert psiElement != null;
		if(psiElement instanceof MsilMethodAsCSharpMethodDeclaration)
		{
			return ((MsilMethodAsCSharpMethodDeclaration) psiElement).getDelegate();
		}
		return (DotNetQualifiedElement) psiElement.getOriginalElement();
	}
}
