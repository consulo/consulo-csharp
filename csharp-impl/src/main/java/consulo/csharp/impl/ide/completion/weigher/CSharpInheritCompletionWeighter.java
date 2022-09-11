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

package consulo.csharp.impl.ide.completion.weigher;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.completion.CompletionWeigher;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.dataholder.NotNullLazyKey;
import consulo.util.lang.Pair;
import consulo.language.psi.PsiElement;
import consulo.language.ast.IElementType;
import consulo.language.editor.util.PsiUtilBase;
import consulo.language.util.ProcessingContext;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.completion.CSharpCompletionSorting;
import consulo.csharp.impl.ide.completion.CSharpCompletionUtil;
import consulo.csharp.impl.ide.completion.expected.ExpectedTypeInfo;
import consulo.csharp.impl.ide.completion.expected.ExpectedTypeVisitor;
import consulo.csharp.impl.ide.completion.item.CSharpTypeLikeLookupElement;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpReferenceExpressionImplUtil;
import consulo.csharp.lang.impl.psi.source.CSharpTypeDeclarationImplUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.completion.CompletionLocation;
import consulo.language.editor.completion.CompletionParameters;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
@ExtensionImpl(order = "before stats, before csharpKindSorter")
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


	@Nonnull
	@RequiredReadAction
	public static List<ExpectedTypeInfo> getExpectedTypeInfosForExpression(CompletionParameters parameters, @Nullable ProcessingContext context)
	{
		return getExpectedTypeInfosForExpression(parameters.getPosition(), context);
	}

	@Nonnull
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

		List<ExpectedTypeInfo> expectedTypeInfos = context == null ? null : context.getSharedContext().get(ExpectedTypeVisitor.EXPECTED_TYPE_INFOS);
		if(expectedTypeInfos != null)
		{
			return expectedTypeInfos;
		}

		expectedTypeInfos = ExpectedTypeVisitor.findExpectedTypeRefs(parent);
		if(context != null)
		{
			context.getSharedContext().put(ExpectedTypeVisitor.EXPECTED_TYPE_INFOS, expectedTypeInfos);
		}
		return expectedTypeInfos;
	}

	@Override
	@RequiredReadAction
	public Comparable weigh(@Nonnull LookupElement element, @Nonnull CompletionLocation completionLocation)
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
	public Comparable weighElement(@Nonnull PsiElement psiElement,
			DotNetGenericExtractor extractor,
			@Nonnull CSharpReferenceExpressionEx referenceExpressionEx,
			@Nonnull List<ExpectedTypeInfo> expectedTypeRefs,
			@Nonnull Position upPosition)
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
			typeOfElement = GenericUnwrapTool.exchangeTypeRef(methodDeclaration.getReturnTypeRef(), extractor);

			for(ExpectedTypeInfo expectedTypeInfo : expectedTypeRefs)
			{
				if(expectedTypeInfo.getTypeProvider() == psiElement)
				{
					continue;
				}

				if(CSharpTypeUtil.isInheritable(expectedTypeInfo.getTypeRef(), typeOfElement))
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
			typeOfElement = CSharpReferenceExpressionImplUtil.toTypeRef(psiElement.getResolveScope(), psiElement, extractor);

			for(ExpectedTypeInfo expectedTypeInfo : expectedTypeRefs)
			{
				if(expectedTypeInfo.getTypeProvider() == psiElement)
				{
					return Position.DOWN;
				}

				if(CSharpTypeUtil.isInheritable(expectedTypeInfo.getTypeRef(), typeOfElement))
				{
					return upPosition;
				}
			}
		}

		return null;
	}

	@Nullable
	@RequiredReadAction
	private static DotNetTypeRef typeRefFromTokeType(@Nonnull IElementType e, CSharpReferenceExpressionEx parent)
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
