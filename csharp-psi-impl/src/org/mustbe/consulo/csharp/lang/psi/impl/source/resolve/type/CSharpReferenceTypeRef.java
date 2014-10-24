package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
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
		protected final PsiElement myElement;

		public Result(PsiElement element)
		{
			myElement = element;
		}

		@Nullable
		@Override
		public PsiElement getElement()
		{
			return myElement;
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

	public static class LambdaResult extends Result implements CSharpLambdaResolveResult
	{
		private final PsiElement myScope;

		public LambdaResult(PsiElement scope, PsiElement element)
		{
			super(element);
			myScope = scope;
		}

		@Nullable
		@Override
		public PsiElement getElement()
		{
			return DotNetPsiSearcher.getInstance(myScope.getProject()).findType(DotNetTypes.System.MulticastDelegate,
					myScope.getResolveScope(), DotNetPsiSearcher.TypeResoleKind.UNKNOWN, CSharpTransform.INSTANCE);
		}

		@NotNull
		@Override
		public DotNetTypeRef getReturnTypeRef()
		{
			return ((CSharpMethodDeclaration)myElement).getReturnTypeRef();
		}

		@NotNull
		@Override
		public DotNetTypeRef[] getParameterTypeRefs()
		{
			return ((CSharpMethodDeclaration)myElement).getParameterTypeRefs();
		}

		@NotNull
		@Override
		public PsiElement getTarget()
		{
			return myElement;
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
		return myReferenceExpression.getReferenceName();
	}

	@NotNull
	@Override
	public String getQualifiedText()
	{
		return myReferenceExpression.getText();
	}

	@NotNull
	@Override
	public DotNetTypeResolveResult resolve(@NotNull PsiElement scope)
	{
		PsiElement resolve = myReferenceExpression.resolve();
		if(resolve instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) resolve).isDelegate())
		{
			return new LambdaResult(scope, resolve);
		}
		return new Result(resolve);
	}
}
