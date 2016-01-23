/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 31.12.14
 */
public class CSharpContextUtil
{
	public enum ContextType
	{
		ANY,
		STATIC,
		// without qualifier we can access to static members
		INSTANCE_WITH_STATIC
				{
					@Override
					public boolean isAllowInstance()
					{
						return true;
					}
				},
		INSTANCE
				{
					@Override
					public boolean isAllowInstance()
					{
						return true;
					}
				};

		public boolean isAllowInstance()
		{
			return false;
		}
	}


	@NotNull
	@RequiredReadAction
	public static CSharpContextUtil.ContextType getContextForResolved(@NotNull PsiElement element)
	{
		if(!(element instanceof DotNetModifierListOwner))
		{
			return ContextType.ANY;
		}

		if(CSharpPsiUtilImpl.isTypeLikeElement(element))
		{
			return ContextType.STATIC;
		}

		if(element instanceof CSharpConstructorDeclaration)
		{
			return CSharpContextUtil.ContextType.ANY;
		}

		if(element instanceof DotNetGenericParameter)
		{
			return ContextType.ANY;
		}

		if(CSharpMethodImplUtil.isExtensionWrapper(element))
		{
			return CSharpContextUtil.ContextType.INSTANCE;
		}

		if(((DotNetModifierListOwner) element).hasModifier(DotNetModifier.STATIC))
		{
			return CSharpContextUtil.ContextType.STATIC;
		}
		return CSharpContextUtil.ContextType.INSTANCE;
	}

	@NotNull
	@RequiredReadAction
	public static ContextType getParentContextTypeForReference(@NotNull CSharpReferenceExpression referenceExpression)
	{
		PsiElement qualifier = referenceExpression.getQualifier();
		if(qualifier == null)
		{
			// object initializer can only initialize instance variables
			PsiElement parent = referenceExpression.getParent();
			if(parent instanceof CSharpFieldOrPropertySet && ((CSharpFieldOrPropertySet) parent).getNameElement() == referenceExpression)
			{
				return ContextType.INSTANCE;
			}
			PsiElement resolvedElement = referenceExpression.resolve();
			if(resolvedElement instanceof CSharpTypeDeclaration ||
					resolvedElement instanceof DotNetNamespaceAsElement ||
					resolvedElement instanceof CSharpLocalVariable ||
					resolvedElement instanceof CSharpLinqVariable ||
					resolvedElement instanceof DotNetParameter ||
					resolvedElement instanceof CSharpLambdaParameter ||
					resolvedElement instanceof CSharpConstructorDeclaration)
			{
				return ContextType.ANY;
			}
			DotNetModifierListOwner qualifiedElement = (DotNetModifierListOwner) PsiTreeUtil.getParentOfType(referenceExpression, DotNetQualifiedElement.class);

			if(qualifiedElement == null)
			{
				return ContextType.ANY;
			}

			if(qualifiedElement.hasModifier(DotNetModifier.STATIC) || CSharpPsiUtilImpl.isTypeLikeElement(qualifiedElement))
			{
				return ContextType.STATIC;
			}

			// if member is static we can use it inside instance elements
			if(resolvedElement instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) resolvedElement).hasModifier(DotNetModifier.STATIC))
			{
				return ContextType.ANY;
			}
			return ContextType.INSTANCE_WITH_STATIC;
		}
		else if(qualifier instanceof CSharpReferenceExpression)
		{
			CSharpReferenceExpression.ResolveToKind kind = ((CSharpReferenceExpression) qualifier).kind();
			// this.<caret> and base.<caret> accept only instance elements
			if(kind == CSharpReferenceExpression.ResolveToKind.BASE || kind == CSharpReferenceExpression.ResolveToKind.THIS)
			{
				return ContextType.INSTANCE;
			}

			PsiElement qualifiedResolvedElement = ((CSharpReferenceExpression) qualifier).resolve();
			// Console.<caret>
			if(isLikeType(qualifiedResolvedElement))
			{
				return ContextType.STATIC;
			}

			return ContextType.INSTANCE;
		}
		return ContextType.ANY;
	}

	private static boolean isLikeType(PsiElement element)
	{
		return CSharpPsiUtilImpl.isTypeLikeElement(element)||
				element instanceof DotNetNamespaceAsElement ||
				element instanceof CSharpTypeDefStatement;
	}
}
