package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDefStatement;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
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
	public static class Result<T extends PsiElement> extends SingleNullableStateResolveResult
	{
		protected final T myElement;
		protected final DotNetGenericExtractor myExtractor;

		public Result(T element, DotNetGenericExtractor extractor)
		{
			myElement = element;
			myExtractor = extractor;
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
			return myExtractor;
		}

		@Override
		public boolean isNullableImpl()
		{
			PsiElement element = getElement();
			return element == null || CSharpTypeUtil.isNullableElement(element);
		}
	}

	public static class LambdaResult extends Result<CSharpMethodDeclaration> implements CSharpLambdaResolveResult
	{
		private final PsiElement myScope;

		public LambdaResult(@NotNull PsiElement scope, @NotNull CSharpMethodDeclaration element, @NotNull DotNetGenericExtractor extractor)
		{
			super(element, extractor);
			myScope = scope;
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
		public boolean isNullableImpl()
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

	protected final CSharpReferenceExpression myReferenceExpression;

	public CSharpReferenceTypeRef(CSharpReferenceExpression referenceExpression)
	{
		myReferenceExpression = referenceExpression;
	}

	@NotNull
	@Override
	public String getPresentableText()
	{
		return getReferenceText();
	}

	@NotNull
	@Override
	public String getQualifiedText()
	{
		DotNetTypeRef[] argumentTypeRefs = myReferenceExpression.getTypeArgumentListRefs();

		StringBuilder builder = new StringBuilder();
		builder.append(myReferenceExpression.getReferenceName());

		if(argumentTypeRefs.length > 0)
		{
			builder.append("<");
			for(int i = 0; i < argumentTypeRefs.length; i++)
			{
				if(i != 0)
				{
					builder.append(", ");
				}
				DotNetTypeRef argument = argumentTypeRefs[i];
				builder.append(argument.getQualifiedText());
			}
			builder.append(">");
		}
		return builder.toString();
	}

	@NotNull
	@Override
	@RequiredReadAction
	public DotNetTypeResolveResult resolve(@NotNull PsiElement scope)
	{
		PsiElement resolve = myReferenceExpression.resolve();
		if(resolve instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) resolve).isDelegate())
		{
			return new LambdaResult(scope, (CSharpMethodDeclaration) resolve, createExtractor(resolve));
		}
		else if(resolve instanceof CSharpTypeDefStatement)
		{
			DotNetTypeRef typeRef = ((CSharpTypeDefStatement) resolve).toTypeRef();
			return typeRef.resolve(resolve);
		}
		return new Result<PsiElement>(resolve, createExtractor(resolve));
	}

	@NotNull
	public CSharpReferenceExpression getReferenceExpression()
	{
		return myReferenceExpression;
	}

	@NotNull
	private DotNetGenericExtractor createExtractor(PsiElement resolved)
	{
		DotNetTypeRef[] typeArgumentListRefs = myReferenceExpression.getTypeArgumentListRefs();
		if(typeArgumentListRefs.length == 0)
		{
			return DotNetGenericExtractor.EMPTY;
		}
		if(resolved instanceof DotNetGenericParameterListOwner)
		{
			DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) resolved).getGenericParameters();
			if(genericParameters.length == typeArgumentListRefs.length)
			{
				return CSharpGenericExtractor.create(genericParameters, typeArgumentListRefs);
			}
		}
		return DotNetGenericExtractor.EMPTY;
	}

	@NotNull
	public String getReferenceText()
	{
		DotNetTypeRef[] argumentTypeRefs = myReferenceExpression.getTypeArgumentListRefs();

		StringBuilder builder = new StringBuilder();
		builder.append(myReferenceExpression.getReferenceName());

		if(argumentTypeRefs.length > 0)
		{
			builder.append("<");
			for(int i = 0; i < argumentTypeRefs.length; i++)
			{
				if(i != 0)
				{
					builder.append(", ");
				}
				DotNetTypeRef argument = argumentTypeRefs[i];
				builder.append(argument.getPresentableText());
			}
			builder.append(">");
		}
		return builder.toString();
	}
}
