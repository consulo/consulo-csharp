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

package consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import consulo.annotations.Immutable;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.resolve.DotNetTypeRef;

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
		ROOT_NAMESPACE,  // root namespace - global keyword
		LABEL,
		BASE_CONSTRUCTOR,
		THIS_CONSTRUCTOR,
		PARAMETER_FROM_PARENT,
		NAMEOF,
		EXPRESSION_OR_TYPE_LIKE,
		TUPLE_PROPERTY; // tuple property (name: exp)

		@NotNull
		@Immutable
		public static final ResolveToKind[] VALUES = values();
	}

	public static enum AccessType
	{
		NONE,
		DOT,
		ARROW,
		COLONCOLON,
		NULLABLE_CALL;

		@NotNull
		@Immutable
		public static final AccessType[] VALUES = values();
	}

	@Nullable
	@Override
	@RequiredReadAction
	DotNetExpression getQualifier();

	@Nullable
	@RequiredReadAction
	PsiElement getReferenceElement();

	@NotNull
	@RequiredReadAction
	ResolveToKind kind();

	@Nullable
	@RequiredReadAction
	DotNetTypeList getTypeArgumentList();

	@NotNull
	@RequiredReadAction
	DotNetTypeRef[] getTypeArgumentListRefs();

	@RequiredReadAction
	boolean isGlobalElement();

	@Nullable
	@RequiredReadAction
	PsiElement getMemberAccessElement();

	@NotNull
	@RequiredReadAction
	AccessType getMemberAccessType();
}
