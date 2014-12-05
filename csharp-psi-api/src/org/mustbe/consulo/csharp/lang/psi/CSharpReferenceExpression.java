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

import org.consulo.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public interface CSharpReferenceExpression extends DotNetReferenceExpression, PsiPolyVariantReference,
		CSharpQualifiedNonReference
{
	public static enum ResolveToKind
	{
		GENERIC_PARAMETER_FROM_PARENT, // return generic parameter from parent
		QUALIFIED_NAMESPACE,  // namespace by fully qualified like 'System.Reflection' system is not searching from context
		SOFT_QUALIFIED_NAMESPACE, // same as QUALIFIED_NAMESPACE but - soft ref
		METHOD,
		ATTRIBUTE,  // return type declaration but ref can find without Attribute sufix
		NATIVE_TYPE_WRAPPER, // return type declaration of native type
		ARRAY_METHOD,
		TYPE_LIKE, // return generic parameter or delegated method or type declaration
		CONSTRUCTOR,
		ANY_MEMBER,
		FIELD_OR_PROPERTY,
		PARAMETER,
		THIS, // return type declaration of parent
		BASE,  // return type declaration super class of parent
		LABEL;

		@NotNull
		@Immutable
		public static final ResolveToKind[] VALUES = values();
	}

	@Nullable
	PsiElement getReferenceElement();

	@NotNull
	ResolveToKind kind();

	@NotNull
	DotNetTypeRef toTypeRefWithoutCaching(ResolveToKind kind, boolean resolveFromParent);
}
