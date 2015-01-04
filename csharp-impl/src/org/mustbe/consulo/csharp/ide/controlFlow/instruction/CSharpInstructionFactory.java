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

package org.mustbe.consulo.csharp.ide.controlFlow.instruction;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public class CSharpInstructionFactory
{
	private final List<CSharpInstruction> myInstructions = new ArrayList<CSharpInstruction>();
	private final List<CSharpLabel> myLabels = new ArrayList<CSharpLabel>();

	@NotNull
	public CSharpPutConstantValueInstruction putConstantValue(CSharpConstantExpressionImpl constantExpression)
	{
		return add(new CSharpPutConstantValueInstruction(constantExpression));
	}

	@NotNull
	public CSharpCreateVariableInstruction createVariable(@NotNull DotNetVariable variable)
	{
		return add(new CSharpCreateVariableInstruction(variable));
	}

	@NotNull
	public CSharpWriteValueInstruction writeValue(@NotNull PsiElement element)
	{
		return add(new CSharpWriteValueInstruction(element));
	}

	@NotNull
	public CSharpReturnInstruction returnValue(@Nullable PsiElement element)
	{
		return add(new CSharpReturnInstruction(element));
	}

	@NotNull
	public CSharpReadValueInstruction readValue(@NotNull PsiElement element)
	{
		return add(new CSharpReadValueInstruction(element));
	}

	@NotNull
	public CSharpBoolJumpInstruction boolJump(int elsePosition)
	{
		return add(new CSharpBoolJumpInstruction(elsePosition));
	}

	@NotNull
	public CSharpPutErrorValueInstruction errorValue()
	{
		return add(new CSharpPutErrorValueInstruction());
	}

	@NotNull
	public CSharpBindInstruction bind()
	{
		return add(new CSharpBindInstruction());
	}

	@NotNull
	public CSharpJumpInstruction jump(int position)
	{
		return add(new CSharpJumpInstruction(position));
	}

	@NotNull
	public CSharpPopInstruction pop()
	{
		return add(new CSharpPopInstruction());
	}

	@NotNull
	public CSharpLabel label(@NotNull PsiElement debugElement)
	{
		CSharpLabel label = new CSharpLabel(myInstructions.size(), myLabels.size(), debugElement, this);
		myLabels.add(label);
		return label;
	}

	@Nullable
	public CSharpInstruction last()
	{
		return ContainerUtil.getLastItem(myInstructions);
	}

	@NotNull
	public CSharpInstructionFactory replace(@NotNull final CSharpBindInstruction bindInstruction)
	{
		final CSharpInstructionFactory thisInstance = this;

		return new CSharpInstructionFactory()
		{
			@NotNull
			@Override
			protected <T extends CSharpInstruction> T add(T i)
			{
				int index = thisInstance.myInstructions.indexOf(bindInstruction);
				if(index == -1)
				{
					throw new IllegalArgumentException("Bind not inside instructions");
				}

				i.setIndex(bindInstruction.getIndex());
				thisInstance.myInstructions.set(index, i);
				return i;
			}
		};
	}

	public int position()
	{
		return myInstructions.size();
	}

	@NotNull
	protected <T extends CSharpInstruction> T add(T i)
	{
		i.setIndex(myInstructions.size());
		myInstructions.add(i);
		return i;
	}

	public List<CSharpLabel> getLabels()
	{
		return myLabels;
	}

	public List<CSharpInstruction> getInstructions()
	{
		return myInstructions;
	}
}
