/*
 * Copyright 2013-2026 consulo.io
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

package consulo.csharp.lang.impl.psi.source.resolve.methodResolving;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpContextUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpArrayInitializerValue;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * Candidates removed from overload resolution before the better one is looked for (C# 7.3, "improved overload
 * candidates"): a static method called through an instance and an instance method called through a type or from a
 * static context, and a generic method whose type arguments do not satisfy its constraints - or are ref structs.
 *
 * @author VISTALL
 */
public final class CandidateRules
{
	private CandidateRules()
	{
	}

	/**
	 * @param reference             the reference which names the method, as in {@code p.M} of {@code p.M(null)}
	 * @param method                the candidate as it is declared
	 * @param extractor             the type arguments inferred or given for it
	 * @param forceQualifierElement what the member is looked up in instead of the qualifier of the reference - when the
	 *                              qualifier itself is being chosen, as
	 *                              {@link consulo.csharp.lang.impl.psi.source.resolve.sorter.StaticVsInstanceComparator} does
	 *                              for {@code Color.M()} with a property Color of the type Color
	 */
	@RequiredReadAction
	public static boolean isCandidate(PsiElement reference, DotNetLikeMethodDeclaration method, DotNetGenericExtractor extractor, @Nullable PsiElement forceQualifierElement)
	{
		return matchesReceiver(reference, method, forceQualifierElement) && satisfiesConstraints(method, extractor);
	}

	@RequiredReadAction
	private static boolean matchesReceiver(PsiElement reference, DotNetLikeMethodDeclaration method, @Nullable PsiElement forceQualifierElement)
	{
		// the qualifier is not the receiver - which one it is, the lookup decides
		if(forceQualifierElement != null && reference instanceof CSharpReferenceExpression)
		{
			return true;
		}

		if(!(method instanceof CSharpMethodDeclaration methodDeclaration) || methodDeclaration.isDelegate() || methodDeclaration.isExtension())
		{
			return true;
		}

		CSharpContextUtil.ContextType methodContext = CSharpContextUtil.getContextForResolved(method);
		return switch(receiver(reference))
		{
			case STATIC -> methodContext == CSharpContextUtil.ContextType.STATIC || methodContext == CSharpContextUtil.ContextType.ANY;
			case INSTANCE -> methodContext.isAllowInstance() || methodContext == CSharpContextUtil.ContextType.ANY;
			case INSTANCE_WITH_STATIC, ANY -> true;
		};
	}

	/**
	 * What the call is made through - like {@link CSharpContextUtil#getParentContextTypeForReference}, without resolving
	 * the reference which is being resolved.
	 */
	@RequiredReadAction
	private static CSharpContextUtil.ContextType receiver(PsiElement reference)
	{
		// new C { a } calls Add on the new instance
		if(reference instanceof CSharpArrayInitializerValue)
		{
			return CSharpContextUtil.ContextType.INSTANCE;
		}
		if(!(reference instanceof CSharpReferenceExpression referenceExpression))
		{
			return CSharpContextUtil.ContextType.ANY;
		}

		DotNetExpression qualifier = referenceExpression.getQualifier();
		if(qualifier == null)
		{
			return CSharpContextUtil.getUnqualifiedContextType(reference);
		}
		if(qualifier instanceof CSharpReferenceExpression qualifierReference)
		{
			// an unresolved qualifier tells nothing
			if(qualifierReference.kind() != CSharpReferenceExpression.ResolveToKind.THIS
					&& qualifierReference.kind() != CSharpReferenceExpression.ResolveToKind.BASE && qualifierReference.resolve() == null)
			{
				return CSharpContextUtil.ContextType.ANY;
			}
			return CSharpContextUtil.getQualifierContextType(qualifierReference);
		}
		// new Q().M(), (a).M()
		return CSharpContextUtil.ContextType.INSTANCE;
	}

	@RequiredReadAction
	private static boolean satisfiesConstraints(DotNetLikeMethodDeclaration method, DotNetGenericExtractor extractor)
	{
		if(!(method instanceof CSharpGenericConstraintOwner owner) || extractor == DotNetGenericExtractor.EMPTY)
		{
			return true;
		}

		// a ref struct is no type argument (C# 13 "allows ref struct" is not looked at)
		for(DotNetGenericParameter parameter : owner.getGenericParameters())
		{
			DotNetTypeRef argument = extractor.extract(parameter);
			if(argument != null && argument.resolve().getElement() instanceof CSharpTypeDeclaration type && type.isStruct() && type.hasModifier(CSharpModifier.REF))
			{
				return false;
			}
		}

		for(CSharpGenericConstraint constraint : owner.getGenericConstraints())
		{
			DotNetGenericParameter parameter = constraint.resolve();
			DotNetTypeRef argument = parameter == null ? null : extractor.extract(parameter);
			if(argument == null || argument == DotNetTypeRef.ERROR_TYPE || argument.resolve().getElement() == null)
			{
				continue;
			}

			for(CSharpGenericConstraintValue value : constraint.getGenericConstraintValues())
			{
				if(!satisfies(value, argument, extractor))
				{
					return false;
				}
			}
		}
		return true;
	}

	@RequiredReadAction
	private static boolean satisfies(CSharpGenericConstraintValue value, DotNetTypeRef argument, DotNetGenericExtractor extractor)
	{
		PsiElement element = argument.resolve().getElement();
		if(element instanceof DotNetGenericParameter)
		{
			// the argument is a type parameter of the caller - its own constraints are not looked at here
			return true;
		}

		if(value instanceof CSharpGenericConstraintKeywordValue keywordValue)
		{
			IElementType keyword = keywordValue.getKeywordElementType();
			boolean valueType = element instanceof DotNetTypeDeclaration type && (type.isStruct() || type.isEnum());
			boolean nullable = element instanceof DotNetTypeDeclaration type && DotNetTypes.System.Nullable$1.equals(type.getVmQName());
			if(keyword == CSharpTokens.CLASS_KEYWORD)
			{
				return !valueType;
			}
			if(keyword == CSharpTokens.STRUCT_KEYWORD)
			{
				return valueType && !nullable;
			}
			return true;
		}

		if(value instanceof CSharpGenericConstraintTypeValue typeValue)
		{
			// identity, reference or boxing conversion - no user defined one
			DotNetTypeRef constraintTypeRef = GenericUnwrapTool.exchangeTypeRef(typeValue.toTypeRef(), extractor);
			return CSharpTypeUtil.isInheritable(constraintTypeRef, argument);
		}
		return true;
	}
}
