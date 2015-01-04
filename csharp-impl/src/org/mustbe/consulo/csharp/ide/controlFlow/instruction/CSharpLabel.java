/*
 * Copyright 2013-2015 must-be.org
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

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 04.01.15
 */
public class CSharpLabel
{
	private final int myStartPosition;
	private final int myIndex;
	private final PsiElement myDebugElement;
	private final CSharpInstructionFactory myInstructionFactory;
	private int myEndPosition = -1;

	public CSharpLabel(int startPosition, int index, PsiElement debugElement, CSharpInstructionFactory instructionFactory)
	{
		myStartPosition = startPosition;
		myIndex = index;
		myDebugElement = debugElement;
		myInstructionFactory = instructionFactory;
	}

	public int getIndex()
	{
		return myIndex;
	}

	public void finish()
	{
		CSharpInstruction last = myInstructionFactory.last();
		myEndPosition = last == null ? 0 : last.getIndex();
	}

	@NotNull
	public PsiElement getDebugElement()
	{
		return myDebugElement;
	}

	public int getEndPosition()
	{
		return myEndPosition;
	}

	public int getStartPosition()
	{
		return myStartPosition;
	}

	@Override
	public String toString()
	{
		return "L" + myIndex;
	}
}
