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

package org.mustbe.consulo.csharp.ide.debugger;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.debugger.DotNetDebugContext;
import org.mustbe.consulo.dotnet.debugger.nodes.DotNetAbstractVariableMirrorNode;
import com.intellij.icons.AllIcons;
import mono.debugger.InvalidFieldIdException;
import mono.debugger.InvalidStackFrameException;
import mono.debugger.ThreadMirror;
import mono.debugger.ThrowValueException;
import mono.debugger.TypeMirror;
import mono.debugger.VMDisconnectedException;
import mono.debugger.Value;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class CSharpWatcherNode extends DotNetAbstractVariableMirrorNode
{
	private Value<?> myValue;

	public CSharpWatcherNode(@NotNull DotNetDebugContext debuggerContext, @NotNull String name, @NotNull ThreadMirror threadMirror, @NotNull Value<?> value)
	{
		super(debuggerContext, name, threadMirror);
		myValue = value;
	}

	@NotNull
	@Override
	public Icon getIconForVariable()
	{
		return AllIcons.Debugger.Watch;
	}

	@Nullable
	@Override
	public Value<?> getValueOfVariableImpl() throws ThrowValueException, InvalidFieldIdException, VMDisconnectedException, InvalidStackFrameException
	{
		return myValue;
	}

	@Override
	public void setValueForVariableImpl(@NotNull Value<?> value) throws ThrowValueException, InvalidFieldIdException, VMDisconnectedException, InvalidStackFrameException
	{

	}

	@Nullable
	@Override
	public TypeMirror getTypeOfVariable()
	{
		return null;
	}
}
