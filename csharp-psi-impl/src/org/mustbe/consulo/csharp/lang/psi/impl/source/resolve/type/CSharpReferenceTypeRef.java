package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
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
	public static class Result<T extends PsiElement> implements DotNetTypeResolveResult
	{
		protected final T myElement;

		public Result(T element)
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

	public static class LambdaResult extends Result<CSharpMethodDeclaration> implements CSharpLambdaResolveResult
	{
		private final PsiElement myScope;
		private final DotNetGenericExtractor myExtractor;

		public LambdaResult(@NotNull PsiElement scope, @NotNull CSharpMethodDeclaration element, @NotNull DotNetGenericExtractor extractor)
		{
			super(element);
			myScope = scope;
			myExtractor = extractor;
		}

		@NotNull
		@Override
		public CSharpSimpleParameterInfo[] getParameterInfos()
		{
			CSharpSimpleParameterInfo[] parameterInfos = myElement.getParameterInfos();
			if(myExtractor == DotNetGenericExtractor.EMPTY)
			{
				return parameterInfos;
			}
			CSharpSimpleParameterInfo[] temp = new CSharpSimpleParameterInfo[parameterInfos.length];
			for(int i = 0; i < parameterInfos.length; i++)
			{
				CSharpSimpleParameterInfo parameterInfo = parameterInfos[i];
				DotNetTypeRef typeRef = GenericUnwrapTool.exchangeTypeRef(parameterInfo.getTypeRef(), getGenericExtractor(), myScope);
				temp[i] = new CSharpSimpleParameterInfo(parameterInfo.getIndex(), parameterInfo.getName(), parameterInfo.getElement(), typeRef);
			}
			return temp;
		}

		@NotNull
		@Override
		public DotNetGenericExtractor getGenericExtractor()
		{
			return myExtractor;
		}

		@Nullable
		@Override
		public PsiElement getElement()
		{
			return CSharpLambdaResolveResultUtil.createTypeFromDelegate(myElement);
		}

		@NotNull
		@Override
		@LazyInstance
		public DotNetTypeRef getReturnTypeRef()
		{
			return GenericUnwrapTool.exchangeTypeRef(myElement.getReturnTypeRef(), getGenericExtractor(), myScope);
		}

		@Override
		public boolean isInheritParameters()
		{
			return false;
		}

		@NotNull
		@Override
		@LazyInstance
		public DotNetTypeRef[] getParameterTypeRefs()
		{
			return GenericUnwrapTool.exchangeTypeRefs(myElement.getParameterTypeRefs(), getGenericExtractor(), myScope);
		}

		@Override
		public boolean isNullable()
		{
			return true;
		}

		@NotNull
		@Override
		public CSharpMethodDeclaration getTarget()
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
			return new LambdaResult(scope, (CSharpMethodDeclaration) resolve, DotNetGenericExtractor.EMPTY);
		}
		return new Result<PsiElement>(resolve);
	}
}
