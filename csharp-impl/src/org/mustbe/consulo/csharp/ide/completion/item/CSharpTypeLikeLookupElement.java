package org.mustbe.consulo.csharp.ide.completion.item;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.RequiredWriteAction;
import org.mustbe.consulo.csharp.ide.completion.CSharpCompletionSorting;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpNewExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpUserType;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.MsilMethodAsCSharpMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
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
	@NotNull
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
				caretModel.moveToOffset(oldCaretOffset);
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

	@NotNull
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
