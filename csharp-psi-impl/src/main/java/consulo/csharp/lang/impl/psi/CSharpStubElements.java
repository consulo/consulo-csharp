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

package consulo.csharp.lang.impl.psi;

import consulo.csharp.lang.impl.psi.source.CSharpStubNullableTypeImpl;
import consulo.csharp.lang.impl.psi.source.CSharpStubPointerTypeImpl;
import consulo.csharp.lang.impl.psi.source.CSharpUsingTypeStatementImpl;
import consulo.csharp.lang.impl.psi.stub.elementTypes.*;
import consulo.csharp.lang.psi.CSharpNullableType;
import consulo.csharp.lang.psi.CSharpUsingTypeStatement;
import consulo.dotnet.psi.DotNetPointerType;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.EmptyStub;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public interface CSharpStubElements
{
	CSharpFileStubElementType FILE = new CSharpFileStubElementType();
	CSharpDummyDefElementType DUMMY_DECLARATION = new CSharpDummyDefElementType();
	CSharpNamespaceDeclarationStubElementType NAMESPACE_DECLARATION = new CSharpNamespaceDeclarationStubElementType();
	CSNamespaceStatementStubElementType NAMESPACE_STATEMENT = new CSNamespaceStatementStubElementType();
	CSharpTypeStubElementType TYPE_DECLARATION = new CSharpTypeStubElementType();
	CSharpMethodStubElementType METHOD_DECLARATION = new CSharpMethodStubElementType();
	CSharpIndexMethodStubElementType INDEX_METHOD_DECLARATION = new CSharpIndexMethodStubElementType();
	CSharpConstructorStubElementType CONSTRUCTOR_DECLARATION = new CSharpConstructorStubElementType();
	CSharpConversionMethodStubElementType CONVERSION_METHOD_DECLARATION = new CSharpConversionMethodStubElementType();
	CSharpPropertyElementType PROPERTY_DECLARATION = new CSharpPropertyElementType();
	CSharpEventElementType EVENT_DECLARATION = new CSharpEventElementType();
	CSharpFieldStubElementType FIELD_DECLARATION = new CSharpFieldStubElementType();
	CSharpEnumConstantStubElementType ENUM_CONSTANT_DECLARATION = new CSharpEnumConstantStubElementType();
	CSharpTypeListElementType EXTENDS_LIST = new CSharpTypeListElementType("EXTENDS_LIST");
	CSharpParameterListStubElementType PARAMETER_LIST = new CSharpParameterListStubElementType();
	CSharpParameterStubElementType PARAMETER = new CSharpParameterStubElementType();
	CSharpUsingNamespaceStatementStubElementType USING_NAMESPACE_STATEMENT = new CSharpUsingNamespaceStatementStubElementType();
	CSharpIdentifierStubElementType IDENTIFIER = new CSharpIdentifierStubElementType();
	CSharpEmptyStubElementType<CSharpUsingTypeStatement> USING_TYPE_STATEMENT = new CSharpEmptyStubElementType<>("USING_TYPE_STATEMENT")
	{
		@Override
		public CSharpUsingTypeStatement createPsi(@Nonnull EmptyStub<CSharpUsingTypeStatement> stub)
		{
			return new CSharpUsingTypeStatementImpl(stub, this);
		}

		@Nonnull
		@Override
		public PsiElement createElement(@Nonnull ASTNode astNode)
		{
			return new CSharpUsingTypeStatementImpl(astNode);
		}
	};
	CSharpTypeDefStubElementType TYPE_DEF_STATEMENT = new CSharpTypeDefStubElementType();
	CSharpGenericParameterListStubElementType GENERIC_PARAMETER_LIST = new CSharpGenericParameterListStubElementType();
	CSharpGenericParameterStubElementType GENERIC_PARAMETER = new CSharpGenericParameterStubElementType();
	CSharpXAccessorStubElementType XACCESSOR = new CSharpXAccessorStubElementType();
	CSharpGenericConstraintListStubElementType GENERIC_CONSTRAINT_LIST = new CSharpGenericConstraintListStubElementType();
	CSharpGenericConstraintStubElementType GENERIC_CONSTRAINT = new CSharpGenericConstraintStubElementType();
	CSharpGenericConstraintKeywordValueStubElementType GENERIC_CONSTRAINT_KEYWORD_VALUE = new CSharpGenericConstraintKeywordValueStubElementType();
	CSharpGenericConstraintTypeValueStubElementType GENERIC_CONSTRAINT_TYPE_VALUE = new CSharpGenericConstraintTypeValueStubElementType();
	CSharpModifierListStubElementType MODIFIER_LIST = new CSharpModifierListStubElementType();
	CSharpAttributeListStubElementType ATTRIBUTE_LIST = new CSharpAttributeListStubElementType();
	CSharpAttributeStubElementType ATTRIBUTE = new CSharpAttributeStubElementType();

	CSharpEmptyStubElementType<CSharpNullableType> NULLABLE_TYPE = new CSharpEmptyStubElementType<>("NULLABLE_TYPE")
	{
		@Nonnull
		@Override
		public PsiElement createElement(@Nonnull ASTNode astNode)
		{
			return new CSharpStubNullableTypeImpl(astNode);
		}

		@Override
		public CSharpStubNullableTypeImpl createPsi(@Nonnull EmptyStub<CSharpNullableType> stub)
		{
			return new CSharpStubNullableTypeImpl(stub, this);
		}
	};

	CSharpEmptyStubElementType<DotNetPointerType> POINTER_TYPE = new CSharpEmptyStubElementType<>("POINTER_TYPE")
	{
		@Nonnull
		@Override
		public PsiElement createElement(@Nonnull ASTNode astNode)
		{
			return new CSharpStubPointerTypeImpl(astNode);
		}

		@Override
		public CSharpStubPointerTypeImpl createPsi(@Nonnull EmptyStub<DotNetPointerType> stub)
		{
			return new CSharpStubPointerTypeImpl(stub, this);
		}
	};

	CSharpTupleTypeStubElementType TUPLE_TYPE = new CSharpTupleTypeStubElementType();

	CSharpTupleVariableStubElementType TUPLE_VARIABLE = new CSharpTupleVariableStubElementType();

	CSharpNativeTypeStubElementType NATIVE_TYPE = new CSharpNativeTypeStubElementType();

	CSharpArrayTypeStubElementType ARRAY_TYPE = new CSharpArrayTypeStubElementType();

	CSharpUserTypeStubElementType USER_TYPE = new CSharpUserTypeStubElementType();

	CSharpTypeListElementType TYPE_ARGUMENTS = new CSharpTypeListElementType("TYPE_ARGUMENTS");

	CSharpReferenceExpressionStubElementType REFERENCE_EXPRESSION = new CSharpReferenceExpressionStubElementType();
}
