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

package consulo.csharp.ide.completion.weigher;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionWeigher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.NotNullLazyKey;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.ProcessingContext;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.completion.CSharpCompletionSorting;
import consulo.csharp.ide.completion.CSharpCompletionUtil;
import consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import consulo.csharp.ide.completion.expected.ExpectedTypeVisitor;
import consulo.csharp.ide.completion.item.CSharpTypeLikeLookupElement;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpReferenceExpressionEx;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImplUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class CSharpInheritCompletionWeighter extends CompletionWeigher
{
	public enum Position
	{
		DOWN,
		UP_REF,
		UP_KEYWORD,
		HIGH
	}

	private static final NotNullLazyKey<List<ExpectedTypeInfo>, CompletionLocation> ourExpectedInfoTypes = NotNullLazyKey.create("ourExpectedInfoTypes", it -> getExpectedTypeInfosForExpression(it
			.getCompletionParameters().getPosition(), null));


	@NotNull
	@RequiredReadAction
	public static List<ExpectedTypeInfo> getExpectedTypeInfosForExpression(CompletionParameters parameters, @Nullable ProcessingContext context)
	{
		return getExpectedTypeInfosForExpression(parameters.getPosition(), context);
	}

	@NotNull
	@RequiredReadAction
	public static List<ExpectedTypeInfo> getExpectedTypeInfosForExpression(PsiElement position, @Nullable ProcessingContext context)
	{
		if(PsiUtilBase.getElementType(position) != CSharpTokens.IDENTIFIER)
		{
			return Collections.emptyList();
		}

		PsiElement parent = position.getParent();
		if(!(parent instanceof CSharpReferenceExpressionEx))
		{
			return Collections.emptyList();
		}

		List<ExpectedTypeInfo> expectedTypeInfos = context == null ? null : context.get(ExpectedTypeVisitor.EXPECTED_TYPE_INFOS);
		if(expectedTypeInfos != null)
		{
			return expectedTypeInfos;
		}

		expectedTypeInfos = ExpectedTypeVisitor.findExpectedTypeRefs(parent);
		if(context != null)
		{
			context.put(ExpectedTypeVisitor.EXPECTED_TYPE_INFOS, expectedTypeInfos);
		}
		return expectedTypeInfos;
	}

	@Override
	@RequiredReadAction
	public Comparable weigh(@NotNull LookupElement element, @NotNull CompletionLocation completionLocation)
	{
		if(element.getPsiElement() instanceof CSharpConstructorDeclaration)
		{
			return null;
		}

		CSharpCompletionSorting.KindSorter.Type sort = CSharpCompletionSorting.getSort(element);
		if(sort == CSharpCompletionSorting.KindSorter.Type.top1)
		{
			return Position.HIGH;
		}

		List<ExpectedTypeInfo> expectedTypeInfoList = ourExpectedInfoTypes.getValue(completionLocation);

		if(expectedTypeInfoList.isEmpty())
		{
			return null;
		}

		CSharpReferenceExpressionEx referenceExpressionEx = (CSharpReferenceExpressionEx) completionLocation.getCompletionParameters().getPosition().getParent();

		DotNetGenericExtractor extractor = DotNetGenericExtractor.EMPTY;
		if(element instanceof CSharpTypeLikeLookupElement)
		{
			extractor = ((CSharpTypeLikeLookupElement) element).getExtractor();
		}
		PsiElement psiElement = element.getPsiElement();
		if(psiElement == null)
		{
			Object object = element.getObject();
			if(object instanceof IElementType)
			{
				DotNetTypeRef typeRef = typeRefFromTokeType((IElementType) object, referenceExpressionEx);
				if(typeRef == null)
				{
					return null;
				}
				PsiElement resolvedElement = typeRef.resolve().getElement();
				if(resolvedElement == null)
				{
					return null;
				}
				return weighElement(resolvedElement, extractor, referenceExpressionEx, expectedTypeInfoList, Position.UP_KEYWORD);
			}
			return null;
		}
		else
		{
			return weighElement(psiElement, extractor, referenceExpressionEx, expectedTypeInfoList, Position.UP_REF);
		}
	}

	@RequiredReadAction
	public Comparable weighElement(@NotNull PsiElement psiElement,
			DotNetGenericExtractor extractor,
			@NotNull CSharpReferenceExpressionEx referenceExpressionEx,
			@NotNull List<ExpectedTypeInfo> expectedTypeRefs,
			@NotNull Position upPosition)
	{
		// if we have not type declaration, make types lower, dont allow int i = Int32 completion more high
		if(referenceExpressionEx.kind() != CSharpReferenceExpression.ResolveToKind.TYPE_LIKE && CSharpCompletionUtil.isTypeLikeElementWithNamespace(psiElement) && upPosition == Position.UP_REF)
		{
			return Position.DOWN;
		}

		DotNetTypeRef typeOfElement;
		if(psiElement instanceof CSharpMethodDeclaration)
		{
			CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) psiElement;
			typeOfElement = GenericUnwrapTool.exchangeTypeRef(methodDeclaration.getReturnTypeRef(), extractor, psiElement);

			for(ExpectedTypeInfo expectedTypeInfo : expectedTypeRefs)
			{
				if(expectedTypeInfo.getTypeProvider() == psiElement)
				{
					continue;
				}

				if(CSharpTypeUtil.isInheritable(expectedTypeInfo.getTypeRef(), typeOfElement, referenceExpressionEx))
				{
					return upPosition;
				}
				/*else
				{
					DotNetTypeResolveResult typeResolveResult = expectedTypeInfo.getTypeRef().resolve(position);
					if(typeResolveResult instanceof CSharpLambdaResolveResult)
					{
						if(CSharpTypeUtil.isInheritable(expectedTypeInfo.getTypeRef(), new CSharpLambdaTypeRef(methodDeclaration),
								position))
						{
							next = buildForMethodReference(methodDeclaration);
							iterator.set(PrioritizedLookupElement.withPriority(next, CSharpCompletionUtil.EXPR_REF_PRIORITY));
						}
					}
				} */
			}
		}
		else
		{
			typeOfElement = CSharpReferenceExpressionImplUtil.toTypeRef(psiElement, extractor);

			for(ExpectedTypeInfo expectedTypeInfo : expectedTypeRefs)
			{
				if(expectedTypeInfo.getTypeProvider() == psiElement)
				{
					return Position.DOWN;
				}

				if(CSharpTypeUtil.isInheritable(expectedTypeInfo.getTypeRef(), typeOfElement, referenceExpressionEx))
				{
					return upPosition;
				}
			}
		}

		return null;
	}

	@Nullable
	@RequiredReadAction
	private static DotNetTypeRef typeRefFromTokeType(@NotNull IElementType e, CSharpReferenceExpressionEx parent)
	{
		if(e == CSharpTokens.TRUE_KEYWORD || e == CSharpTokens.FALSE_KEYWORD)
		{
			return new CSharpTypeRefByQName(parent, DotNetTypes.System.Boolean);
		}
		else if(e == CSharpTokens.TYPEOF_KEYWORD)
		{
			return new CSharpTypeRefByQName(parent, DotNetTypes.System.Type);
		}
		else if(e == CSharpSoftTokens.NAMEOF_KEYWORD)
		{
			return new CSharpTypeRefByQName(parent, DotNetTypes.System.String);
		}
		else if(e == CSharpTokens.SIZEOF_KEYWORD)
		{
			return new CSharpTypeRefByQName(parent, DotNetTypes.System.Int32);
		}
		else if(e == CSharpTokens.__MAKEREF_KEYWORD)
		{
			return new CSharpTypeRefByQName(parent, DotNetTypes.System.TypedReference);
		}
		else if(e == CSharpTokens.__REFTYPE_KEYWORD)
		{
			return new CSharpTypeRefByQName(parent, DotNetTypes.System.Type);
		}
		else if(e == CSharpTokens.THIS_KEYWORD)
		{
			DotNetTypeDeclaration thisTypeDeclaration = PsiTreeUtil.getParentOfType(parent, DotNetTypeDeclaration.class);
			if(thisTypeDeclaration != null)
			{
				return new CSharpTypeRefByTypeDeclaration(thisTypeDeclaration);
			}
		}
		else if(e == CSharpTokens.BASE_KEYWORD)
		{
			DotNetTypeDeclaration thisTypeDeclaration = PsiTreeUtil.getParentOfType(parent, DotNetTypeDeclaration.class);
			if(thisTypeDeclaration != null)
			{
				Pair<DotNetTypeDeclaration, DotNetGenericExtractor> pair = CSharpTypeDeclarationImplUtil.resolveBaseType(thisTypeDeclaration, parent);
				if(pair != null)
				{
					return new CSharpTypeRefByTypeDeclaration(pair.getFirst(), pair.getSecond());
				}
			}
		}
		return null;
	}
}
