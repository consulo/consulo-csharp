package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 20.10.14
 */
public class CSharpReferenceTypeRef implements DotNetTypeRef
{
	public static class Result implements DotNetTypeResolveResult
	{
		private final CSharpReferenceExpression myReferenceExpression;

		public Result(CSharpReferenceExpression referenceExpression)
		{
			myReferenceExpression = referenceExpression;
		}

		@Nullable
		@Override
		public PsiElement getElement()
		{
			return myReferenceExpression.resolve();
		}

		@NotNull
		@Override
		public DotNetGenericExtractor getGenericExtractor()
		{
			return DotNetGenericExtractor.EMPTY;
		}

		@Override
		public boolean isNullable()
		{
			PsiElement element = getElement();
			return element == null || CSharpTypeUtil.isElementIsNullable(element);
		}
	}

	private final CSharpReferenceExpression myReferenceExpression;

	public CSharpReferenceTypeRef(CSharpReferenceExpression referenceExpression)
	{
		myReferenceExpression = referenceExpression;
	}

	@NotNull
	@Override
	public String getPresentableText()
	{
		return myReferenceExpression.getText();
	}

	@NotNull
	@Override
	public String getQualifiedText()
	{
		return getPresentableText();
	}

	@NotNull
	@Override
	public DotNetTypeResolveResult resolve(@NotNull PsiElement element)
	{
		return new Result(myReferenceExpression);
	}
}
