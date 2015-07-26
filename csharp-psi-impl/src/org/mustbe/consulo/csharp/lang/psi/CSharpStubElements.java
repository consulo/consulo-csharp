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
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpStubNullableTypeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpStubPointerTypeImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingTypeStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpEmptyStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes.*;
import org.mustbe.consulo.dotnet.psi.DotNetPointerType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public interface CSharpStubElements
{
	CSharpFileStubElementType FILE = new CSharpFileStubElementType();
	CSharpMacroStubElementType MACRO_FILE = new CSharpMacroStubElementType();
	CSharpDummyDefElementType DUMMY_DECLARATION = new CSharpDummyDefElementType();
	CSharpNamespaceStubElementType NAMESPACE_DECLARATION = new CSharpNamespaceStubElementType();
	CSharpTypeStubElementType TYPE_DECLARATION = new CSharpTypeStubElementType();
	CSharpMethodStubElementType METHOD_DECLARATION = new CSharpMethodStubElementType();
	CSharpArrayMethodStubElementType ARRAY_METHOD_DECLARATION = new CSharpArrayMethodStubElementType();
	CSharpConstructorStubElementType CONSTRUCTOR_DECLARATION = new CSharpConstructorStubElementType();
	CSharpConversionMethodStubElementType CONVERSION_METHOD_DECLARATION = new CSharpConversionMethodStubElementType();
	CSharpPropertyElementType PROPERTY_DECLARATION = new CSharpPropertyElementType();
	CSharpEventElementType EVENT_DECLARATION = new CSharpEventElementType();
	CSharpFieldStubElementType FIELD_DECLARATION = new CSharpFieldStubElementType();
	CSharpEnumConstantStubElementType ENUM_CONSTANT_DECLARATION = new CSharpEnumConstantStubElementType();
	CSharpTypeListElementType EXTENDS_LIST = new CSharpTypeListElementType("EXTENDS_LIST");
	CSharpParameterListStubElementType PARAMETER_LIST = new CSharpParameterListStubElementType();
	CSharpParameterStubElementType PARAMETER = new CSharpParameterStubElementType();
	CSharpUsingListStubElementType USING_LIST = new CSharpUsingListStubElementType();
	CSharpUsingNamespaceStatementStubElementType USING_NAMESPACE_STATEMENT = new CSharpUsingNamespaceStatementStubElementType();
	CSharpIdentifierStubElementType IDENTIFIER = new CSharpIdentifierStubElementType();
	CSharpEmptyStubElementType<CSharpUsingTypeStatement> USING_TYPE_STATEMENT = new CSharpEmptyStubElementType<CSharpUsingTypeStatement>
			("USING_TYPE_STATEMENT")
	{
		@Override
		public CSharpUsingTypeStatement createPsi(@NotNull CSharpEmptyStub<CSharpUsingTypeStatement> stub)
		{
			return new CSharpUsingTypeStatementImpl(stub, this);
		}

		@NotNull
		@Override
		public PsiElement createElement(@NotNull ASTNode astNode)
		{
			return new CSharpUsingTypeStatementImpl(astNode);
		}
	};
	CSharpTypeDefStubElementType TYPE_DEF_STATEMENT = new CSharpTypeDefStubElementType();
	CSharpGenericParameterListStubElementType GENERIC_PARAMETER_LIST = new CSharpGenericParameterListStubElementType();
	CSharpGenericParameterStubElementType GENERIC_PARAMETER = new CSharpGenericParameterStubElementType();
	CSharpXXXAccessorStubElementType XXX_ACCESSOR = new CSharpXXXAccessorStubElementType();
	CSharpGenericConstraintListStubElementType GENERIC_CONSTRAINT_LIST = new CSharpGenericConstraintListStubElementType();
	CSharpGenericConstraintStubElementType GENERIC_CONSTRAINT = new CSharpGenericConstraintStubElementType();
	CSharpGenericConstraintKeywordValueStubElementType GENERIC_CONSTRAINT_KEYWORD_VALUE = new CSharpGenericConstraintKeywordValueStubElementType();
	CSharpGenericConstraintTypeValueStubElementType GENERIC_CONSTRAINT_TYPE_VALUE = new CSharpGenericConstraintTypeValueStubElementType();
	CSharpModifierListStubElementType MODIFIER_LIST = new CSharpModifierListStubElementType();
	CSharpAttributeListStubElementType ATTRIBUTE_LIST = new CSharpAttributeListStubElementType();
	CSharpAttributeStubElementType ATTRIBUTE = new CSharpAttributeStubElementType();

	TokenSet USING_CHILDREN = TokenSet.create(USING_NAMESPACE_STATEMENT, USING_TYPE_STATEMENT, TYPE_DEF_STATEMENT);

	CSharpEmptyStubElementType<CSharpNullableType> NULLABLE_TYPE = new CSharpEmptyStubElementType<CSharpNullableType>("NULLABLE_TYPE")
	{
		@NotNull
		@Override
		public PsiElement createElement(@NotNull ASTNode astNode)
		{
			return new CSharpStubNullableTypeImpl(astNode);
		}

		@Override
		public CSharpStubNullableTypeImpl createPsi(@NotNull CSharpEmptyStub<CSharpNullableType> stub)
		{
			return new CSharpStubNullableTypeImpl(stub, this);
		}
	};

	CSharpEmptyStubElementType<DotNetPointerType> POINTER_TYPE = new CSharpEmptyStubElementType<DotNetPointerType>("POINTER_TYPE")
	{
		@NotNull
		@Override
		public PsiElement createElement(@NotNull ASTNode astNode)
		{
			return new CSharpStubPointerTypeImpl(astNode);
		}

		@Override
		public CSharpStubPointerTypeImpl createPsi(@NotNull CSharpEmptyStub<DotNetPointerType> stub)
		{
			return new CSharpStubPointerTypeImpl(stub, this);
		}
	};

	CSharpNativeTypeStubElementType NATIVE_TYPE = new CSharpNativeTypeStubElementType();

	CSharpArrayTypeStubElementType ARRAY_TYPE = new CSharpArrayTypeStubElementType();

	CSharpUserTypeStubElementType USER_TYPE = new CSharpUserTypeStubElementType();

	CSharpTypeListElementType TYPE_ARGUMENTS = new CSharpTypeListElementType("TYPE_ARGUMENTS");

	CSharpReferenceExpressionStubElementType REFERENCE_EXPRESSION = new CSharpReferenceExpressionStubElementType();

	TokenSet GENERIC_CONSTRAINT_VALUES = TokenSet.create(GENERIC_CONSTRAINT_KEYWORD_VALUE, GENERIC_CONSTRAINT_TYPE_VALUE);

	TokenSet TYPE_SET = TokenSet.create(NULLABLE_TYPE, POINTER_TYPE, NATIVE_TYPE, ARRAY_TYPE, USER_TYPE);

	TokenSet QUALIFIED_MEMBERS = TokenSet.create(NAMESPACE_DECLARATION, TYPE_DECLARATION, METHOD_DECLARATION, CONSTRUCTOR_DECLARATION,
			PROPERTY_DECLARATION, EVENT_DECLARATION, FIELD_DECLARATION, ENUM_CONSTANT_DECLARATION, CONVERSION_METHOD_DECLARATION,
			ARRAY_METHOD_DECLARATION);
}
