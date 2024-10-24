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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.language.psi.stub.StubElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.logging.Logger;
import consulo.language.psi.PsiElement;
import consulo.csharp.lang.impl.psi.stub.CSharpVariableDeclStub;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetVariable;
import consulo.util.collection.ArrayUtil;

/**
 * @author VISTALL
 * @since 10.02.15
 */
public class CSharpStubVariableImplUtil
{
	private static final Logger LOGGER = Logger.getInstance(CSharpStubVariableImplUtil.class);

	public static boolean isMultipleDeclaration(@Nonnull CSharpStubVariableImpl<?> variable)
	{
		CSharpVariableDeclStub<?> stub = variable.getStub();
		if(stub != null)
		{
			return stub.isMultipleDeclaration();
		}
		return variable.getNameIdentifier() != null && variable.getExplicitType() == null;
	}

	@Nullable
	public static DotNetType getType(@Nonnull CSharpStubVariableImpl<?> variable)
	{
		DotNetType type = variable.getExplicitType();
		if(type != null)
		{
			return type;
		}

		DotNetVariable prevVariable = getPrevVariable(variable);
		return prevVariable == null ? null : prevVariable.getType();
	}

	@Nullable
	public static DotNetModifierList getModifierList(@Nonnull CSharpStubVariableImpl<?> variable)
	{
		DotNetModifierList list = variable.getExplicitModifierList();
		if(list != null)
		{
			return list;
		}

		DotNetVariable prevVariable = getPrevVariable(variable);
		return prevVariable == null ? null : prevVariable.getModifierList();
	}

	@Nullable
	public static PsiElement getConstantKeywordElement(@Nonnull CSharpStubVariableImpl<?> variable)
	{
		PsiElement keywordElement = variable.getExplicitConstantKeywordElement();
		if(keywordElement != null)
		{
			return keywordElement;
		}

		DotNetVariable prevVariable = getPrevVariable(variable);
		return prevVariable == null ? null : prevVariable.getConstantKeywordElement();
	}

	@Nullable
	private static DotNetVariable getPrevVariable(@Nonnull CSharpStubVariableImpl<?> variable)
	{
		if(isMultipleDeclaration(variable))
		{
			CSharpVariableDeclStub<?> stub = variable.getStub();
			if(stub != null)
			{
				StubElement<?> parentStub = stub.getParentStub();
				PsiElement[] stubVariables = parentStub.getChildrenByType(variable.getElementType(), PsiElement.ARRAY_FACTORY);

				int i = ArrayUtil.find(stubVariables, variable);
				if(i <= 0)
				{
					LOGGER.error("Variable dont have type but dont second");
					return null;
				}

				return (DotNetVariable) stubVariables[i - 1];
			}
			else
			{
				CSharpStubVariableImpl<?> prevVariable = PsiTreeUtil.getPrevSiblingOfType(variable, variable.getClass());
				if(prevVariable == null)
				{
					LOGGER.error("Variable dont have type but dont second");
					return null;
				}
				return prevVariable;
			}
		}
		return null;
	}
}
