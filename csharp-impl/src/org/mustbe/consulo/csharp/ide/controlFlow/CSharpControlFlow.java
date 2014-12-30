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

import org.mustbe.consulo.csharp.ide.controlFlow.instruction.CSharpInstruction;
import org.mustbe.consulo.csharp.ide.controlFlow.instruction.CSharpInstructionFactory;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public class CSharpControlFlow
{
	private final CSharpInstruction[] myInstructions;

	public CSharpControlFlow(CSharpInstructionFactory instructionFactory)
	{
		List<CSharpInstruction> instructions = instructionFactory.getInstructions();
		myInstructions = ContainerUtil.toArray(instructions, CSharpInstruction.ARRAY_FACTORY);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Instructions: \n");
		for(CSharpInstruction instruction : myInstructions)
		{
			builder.append(" - ").append(instruction.toString()).append("\n");
		}
		return builder.toString();
	}
}
