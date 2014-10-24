package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightTypeDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
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
		public LambdaResult(CSharpMethodDeclaration element)
		{
			super(element);
		}

		@Nullable
		@Override
		public PsiElement getElement()
		{
			CSharpLightTypeDeclarationBuilder builder = new CSharpLightTypeDeclarationBuilder(myElement.getProject());
			builder.withParentQName(myElement.getPresentableParentQName());
			builder.withName(myElement.getName());

			builder.addExtendType(new DotNetTypeRefByQName(DotNetTypes.System.MulticastDelegate, CSharpTransform.INSTANCE));

			for(DotNetGenericParameter parameter : myElement.getGenericParameters())
			{
				builder.addGenericParameter(parameter);
			}
			return builder;
		}

		@NotNull
		@Override
		public DotNetTypeRef getReturnTypeRef()
		{
			return myElement.getReturnTypeRef();
		}

		@NotNull
		@Override
		public DotNetTypeRef[] getParameterTypeRefs()
		{
			return myElement.getParameterTypeRefs();
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
			return new LambdaResult((CSharpMethodDeclaration) resolve);
		}
		return new Result<PsiElement>(resolve);
	}
}
