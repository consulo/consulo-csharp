/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.ide.completion.weighter;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.completion.CSharpCompletionUtil;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFile;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpressionEx;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.Weigher;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class CSharpInheritProximityWeigher extends Weigher<LookupElement, CompletionLocation>
{
	public enum Position
	{
		DOWN,
		NONE,
		UP_KEYWORD,
		UP_REF,
	}

	@Nullable
	@Override
	public Comparable weigh(@NotNull LookupElement element, @NotNull CompletionLocation completionLocation)
	{
		PsiElement position = completionLocation.getCompletionParameters().getPosition();
		if(!(position.getContainingFile() instanceof CSharpFile))
		{
			return Position.NONE;
		}

		PsiElement parent = position.getParent();
		if(!(parent instanceof CSharpReferenceExpressionEx))
		{
			return Position.NONE;
		}

		CSharpReferenceExpressionEx referenceExpressionEx = (CSharpReferenceExpressionEx) parent;

		List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(referenceExpressionEx);
		if(expectedTypeRefs.isEmpty())
		{
			return Position.NONE;
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
					return Position.NONE;
				}
				PsiElement resolvedElement = typeRef.resolve(parent).getElement();
				if(resolvedElement == null)
				{
					return Position.NONE;
				}
				return weighElement(resolvedElement, referenceExpressionEx, expectedTypeRefs, Position.UP_KEYWORD);
			}
			return Position.NONE;
		}
		else
		{
			return weighElement(psiElement, referenceExpressionEx, expectedTypeRefs, Position.UP_REF);
		}
	}

	public Comparable weighElement(@NotNull PsiElement psiElement,
			@NotNull CSharpReferenceExpressionEx referenceExpressionEx,
			@NotNull List<ExpectedTypeInfo> expectedTypeRefs,
			@NotNull Position upPosition)
	{
		// if we have not type declaration, make types lower, dont allow int i = Int32 completion more high
		if(referenceExpressionEx.kind() != CSharpReferenceExpression.ResolveToKind.TYPE_LIKE && CSharpCompletionUtil.isTypeLikeElement(psiElement)
				&& upPosition == Position.UP_REF)
		{
			return Position.DOWN;
		}

		DotNetTypeRef typeOfElement;
		if(psiElement instanceof CSharpMethodDeclaration)
		{
			CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) psiElement;
			typeOfElement = methodDeclaration.getReturnTypeRef();

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
			typeOfElement = CSharpReferenceExpressionImplUtil.toTypeRef(psiElement);

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

		return Position.NONE;
	}

	@Nullable
	private static DotNetTypeRef typeRefFromTokeType(@NotNull IElementType e, CSharpReferenceExpressionEx parent)
	{
		if(e == CSharpTokens.TRUE_KEYWORD || e == CSharpTokens.FALSE_KEYWORD)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Boolean);
		}
		else if(e == CSharpTokens.TYPEOF_KEYWORD)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Type);
		}
		else if(e == CSharpSoftTokens.NAMEOF_KEYWORD)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.String);
		}
		else if(e == CSharpTokens.SIZEOF_KEYWORD)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Int32);
		}
		else if(e == CSharpTokens.__MAKEREF_KEYWORD)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.TypedReference);
		}
		else if(e == CSharpTokens.__REFTYPE_KEYWORD)
		{
			return new CSharpTypeRefByQName(DotNetTypes.System.Type);
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
				val pair = CSharpTypeDeclarationImplUtil.resolveBaseType(thisTypeDeclaration, parent);
				if(pair != null)
				{
					return new CSharpTypeRefByTypeDeclaration(pair.getFirst(), pair.getSecond());
				}
			}
		}
		return null;
	}
}
