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

package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpNullableTypeUtil;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightIndexMethodDeclarationBuilder;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightParameterBuilder;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpSearchUtil;
import consulo.csharp.lang.psi.*;
import consulo.document.util.TextRange;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetMethodDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.dotnet.psi.resolve.DotNetPointerTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiPolyVariantReference;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveCache;
import consulo.language.util.IncorrectOperationException;

import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpIndexAccessExpressionImpl extends CSharpExpressionImpl implements DotNetExpression, CSharpCallArgumentListOwner, CSharpQualifiedNonReference, PsiPolyVariantReference
{
	public static class OurResolver implements ResolveCache.PolyVariantResolver<CSharpIndexAccessExpressionImpl>
	{
		public static final OurResolver INSTANCE = new OurResolver();

		@RequiredReadAction
		@Override
		public ResolveResult[] resolve(CSharpIndexAccessExpressionImpl expression, boolean incompleteCode)
		{
			DotNetExpression qualifier = expression.getQualifier();
			DotNetTypeRef typeRef = qualifier.toTypeRef(true);
			if(typeRef instanceof DotNetPointerTypeRef)
			{
				DotNetTypeRef innerTypeRef = ((DotNetPointerTypeRef) typeRef).getInnerTypeRef();

				CSharpLightIndexMethodDeclarationBuilder builder = new CSharpLightIndexMethodDeclarationBuilder(expression.getProject(), 0);
				builder.withReturnType(innerTypeRef);
				builder.addParameter(new CSharpLightParameterBuilder(expression.getProject()).withName("p").withTypeRef(new CSharpTypeRefByQName(expression, DotNetTypes.System.Int32)));
				return new ResolveResult[]{new CSharpResolveResult(builder)};
			}

			ResolveResult[] resolveResults = CSharpReferenceExpressionImplUtil.multiResolveImpl(CSharpReferenceExpression.ResolveToKind.ARRAY_METHOD, expression, expression, true);
			if(CSharpResolveUtil.findFirstValidResult(resolveResults) == null)
			{
				ResolveResult implicitIndexer = resolveImplicitIndexOrRange(expression, typeRef, resolveResults);
				if(implicitIndexer != null)
				{
					return new ResolveResult[]{implicitIndexer};
				}
			}
			return incompleteCode ? resolveResults : CSharpResolveUtil.filterValidResults(resolveResults);
		}

		/**
		 * The implicit Index and Range support of C# 8 - for a type without such indexers: x[^1] by its int indexer, when it has Length
		 * or Count; x[1..3] by its Slice(int, int), of a string by Substring
		 */
		@Nullable
		@RequiredReadAction
		private static ResolveResult resolveImplicitIndexOrRange(CSharpIndexAccessExpressionImpl expression, DotNetTypeRef typeRef, ResolveResult[] candidates)
		{
			CSharpCallArgument[] arguments = expression.getCallArguments();
			DotNetExpression argument = arguments.length == 1 ? arguments[0].getArgumentExpression() : null;
			if(argument == null || !(argument.toTypeRef(true).resolve().getElement() instanceof DotNetTypeDeclaration argumentType))
			{
				return null;
			}

			String argumentVmQName = argumentType.getVmQName();
			boolean index = CSharpRangeExpressionImpl.INDEX.equals(argumentVmQName);
			if(!index && !CSharpRangeExpressionImpl.RANGE.equals(argumentVmQName))
			{
				return null;
			}

			// countable - with Length or Count
			if(CSharpSearchUtil.findPropertyByName("Length", null, typeRef) == null && CSharpSearchUtil.findPropertyByName("Count", null, typeRef) == null)
			{
				return null;
			}

			DotNetTypeRef returnTypeRef = null;
			if(index)
			{
				for(ResolveResult candidate : candidates)
				{
					if(candidate.getElement() instanceof CSharpIndexMethodDeclaration indexer && indexer.getParameterTypeRefs().length == 1
							&& DotNetTypeRefUtil.isVmQNameEqual(indexer.getParameterTypeRefs()[0], DotNetTypes.System.Int32))
					{
						returnTypeRef = indexer.getReturnTypeRef();
						break;
					}
				}
			}
			else if(DotNetTypeRefUtil.isVmQNameEqual(typeRef, DotNetTypes.System.String))
			{
				returnTypeRef = typeRef;
			}
			else
			{
				DotNetMethodDeclaration slice = CSharpSearchUtil.findMethodByName("Slice", null, typeRef, 2);
				returnTypeRef = slice == null ? null : slice.getReturnTypeRef();
			}

			if(returnTypeRef == null)
			{
				return null;
			}

			CSharpLightIndexMethodDeclarationBuilder builder = new CSharpLightIndexMethodDeclarationBuilder(expression.getProject(), 0);
			builder.withReturnType(returnTypeRef);
			builder.addParameter(new CSharpLightParameterBuilder(expression.getProject()).withName(index ? "index" : "range").withTypeRef(argument.toTypeRef(true)));
			return new CSharpResolveResult(builder);
		}
	}

	public CSharpIndexAccessExpressionImpl(IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(CSharpElementVisitor visitor)
	{
		visitor.visitIndexAccessExpression(this);
	}

	@Override
	@RequiredReadAction
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		PsiElement resolve = resolveToCallable();
		if(resolve instanceof CSharpIndexMethodDeclaration)
		{
			DotNetTypeRef returnTypeRef = ((CSharpIndexMethodDeclaration) resolve).getReturnTypeRef();
			if(CSharpNullableTypeUtil.containsNullableCalls(this))
			{
				return CSharpNullableTypeUtil.boxIfNeed(returnTypeRef);
			}
			return returnTypeRef;
		}
		return DotNetTypeRef.ERROR_TYPE;
	}

	@Override
	@RequiredReadAction
	public DotNetExpression getQualifier()
	{
		return (DotNetExpression) getFirstChild();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getReferenceName()
	{
		throw new UnsupportedOperationException();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getReferenceNameWithAt()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public DotNetExpression[] getParameterExpressions()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? DotNetExpression.EMPTY_ARRAY : parameterList.getExpressions();
	}

	@Override
	public CSharpCallArgument[] getCallArguments()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? CSharpCallArgument.EMPTY_ARRAY : parameterList.getArguments();
	}

	@Nullable
	@Override
	public CSharpCallArgumentList getParameterList()
	{
		return findChildByClass(CSharpCallArgumentList.class);
	}

	@Nullable
	@Override
	public PsiElement resolveToCallable()
	{
		ResolveResult[] resolveResults = multiResolve(false);
		if(resolveResults.length == 0)
		{
			return null;
		}
		return CSharpResolveUtil.findFirstValidElement(resolveResults);
	}

	@Override
	@RequiredReadAction
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		if(CSharpReferenceExpressionImplUtil.isCacheDisabled(this))
		{
			return OurResolver.INSTANCE.resolve(this, incompleteCode);
		}
		return ResolveCache.getInstance(getProject()).resolveWithCaching(this, OurResolver.INSTANCE, true, incompleteCode);
	}

	@RequiredReadAction
	public boolean isNullable()
	{
		return findChildByType(CSharpTokens.QUEST) != null;
	}

	@RequiredReadAction
	@Override
	public PsiElement getElement()
	{
		return this;
	}

	@RequiredReadAction
	@Override
	public TextRange getRangeInElement()
	{
		return new TextRange(0, getTextLength());
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement resolve()
	{
		return CSharpResolveUtil.findFirstValidElement(multiResolve(false));
	}

	@RequiredReadAction
	@Override
	public String getCanonicalText()
	{
		return getText();
	}

	@RequiredWriteAction
	@Override
	public PsiElement handleElementRename(String s) throws IncorrectOperationException
	{
		return null;
	}

	@RequiredWriteAction
	@Override
	public PsiElement bindToElement(PsiElement psiElement) throws IncorrectOperationException
	{
		return null;
	}

	@RequiredReadAction
	@Override
	public boolean isReferenceTo(PsiElement element)
	{
		return CSharpReferenceExpressionImplUtil.isReferenceTo(this, element);
	}

	@RequiredReadAction
	@Override
	public boolean isSoft()
	{
		return false;
	}
}
