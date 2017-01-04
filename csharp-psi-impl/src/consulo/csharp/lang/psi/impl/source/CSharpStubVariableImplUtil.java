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

package consulo.csharp.lang.psi.impl.source;

import consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.impl.stub.CSharpVariableDeclStub;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetVariable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 10.02.15
 */
@Logger
public class CSharpStubVariableImplUtil
{
	public static boolean isMultipleDeclaration(@NotNull CSharpStubVariableImpl<?> variable)
	{
		CSharpVariableDeclStub<?> stub = variable.getStub();
		if(stub != null)
		{
			return stub.isMultipleDeclaration();
		}
		return variable.getNameIdentifier() != null && variable.getExplicitType() == null;
	}

	@Nullable
	public static DotNetType getType(@NotNull CSharpStubVariableImpl<?> variable)
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
	public static DotNetModifierList getModifierList(@NotNull CSharpStubVariableImpl<?> variable)
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
	public static PsiElement getConstantKeywordElement(@NotNull CSharpStubVariableImpl<?> variable)
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
	private static DotNetVariable getPrevVariable(@NotNull CSharpStubVariableImpl<?> variable)
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
					CSharpStubVariableImplUtil.LOGGER.error("Variable dont have type but dont second");
					return null;
				}

				return (DotNetVariable) stubVariables[i - 1];
			}
			else
			{
				CSharpStubVariableImpl<?> prevVariable = PsiTreeUtil.getPrevSiblingOfType(variable, variable.getClass());
				if(prevVariable == null)
				{
					CSharpStubVariableImplUtil.LOGGER.error("Variable dont have type but dont second");
					return null;
				}
				return prevVariable;
			}
		}
		return null;
	}
}
