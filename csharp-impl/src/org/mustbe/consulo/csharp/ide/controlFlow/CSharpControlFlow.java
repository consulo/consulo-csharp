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

package org.mustbe.consulo.csharp.ide.controlFlow;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.controlFlow.instruction.CSharpInstruction;
import org.mustbe.consulo.csharp.ide.controlFlow.instruction.CSharpInstructionFactory;
import org.mustbe.consulo.csharp.ide.controlFlow.instruction.CSharpLabel;
import com.intellij.openapi.util.Condition;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public class CSharpControlFlow
{
	private final List<CSharpInstruction> myInstructions;
	private final List<CSharpLabel> myLabels;

	public CSharpControlFlow(CSharpInstructionFactory instructionFactory)
	{
		myInstructions = instructionFactory.getInstructions();
		myLabels = instructionFactory.getLabels();
		for(CSharpLabel label : myLabels)
		{
			if(label.getEndPosition() == -1)
			{
				throw new IllegalArgumentException("Label " + label + " is not finished at element: " + label.getDebugElement());
			}
		}
	}

	@NotNull
	public String toDebugString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Instructions: \n");
		for(int i = 0; i < myInstructions.size(); i++)
		{
			val temp = i;
			CSharpLabel startLabel = ContainerUtil.find(myLabels, new Condition<CSharpLabel>()
			{
				@Override
				public boolean value(CSharpLabel cSharpLabel)
				{
					return cSharpLabel.getStartPosition() == temp;
				}
			});

			if(startLabel != null)
			{
				builder.append("+").append(startLabel.toString()).append("\n");
			}
			CSharpInstruction instruction = myInstructions.get(i);
			builder.append(" - ").append(instruction.toString()).append("\n");

			CSharpLabel endLabel = ContainerUtil.find(myLabels, new Condition<CSharpLabel>()
			{
				@Override
				public boolean value(CSharpLabel cSharpLabel)
				{
					return cSharpLabel.getEndPosition() == temp;
				}
			});

			if(endLabel != null)
			{
				builder.append("-").append(endLabel.toString()).append("\n");
			}
		}
		return builder.toString();
	}
}
