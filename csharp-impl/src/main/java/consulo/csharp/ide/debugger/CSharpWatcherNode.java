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

package consulo.csharp.ide.debugger;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.Ref;
import com.intellij.xdebugger.frame.XValueModifier;
import consulo.dotnet.debugger.DotNetDebugContext;
import consulo.dotnet.debugger.nodes.DotNetAbstractVariableValueNode;
import consulo.dotnet.debugger.proxy.DotNetStackFrameProxy;
import consulo.dotnet.debugger.proxy.DotNetTypeProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class CSharpWatcherNode extends DotNetAbstractVariableValueNode
{
	private DotNetValueProxy myValue;

	public CSharpWatcherNode(@NotNull DotNetDebugContext debuggerContext, @NotNull String name, @NotNull DotNetStackFrameProxy frameProxy, @NotNull DotNetValueProxy value)
	{
		super(debuggerContext, name, frameProxy);
		myValue = value;
	}

	@NotNull
	@Override
	public Icon getIconForVariable(@Nullable Ref<DotNetValueProxy> alreadyCalledValue)
	{
		return AllIcons.Debugger.Watch;
	}

	@Nullable
	@Override
	public XValueModifier getModifier()
	{
		return null;
	}

	@Nullable
	@Override
	public DotNetValueProxy getValueOfVariableImpl()
	{
		return myValue;
	}

	@Override
	public void setValueForVariableImpl(@NotNull DotNetValueProxy value)
	{

	}

	@Nullable
	@Override
	public DotNetTypeProxy getTypeOfVariableImpl()
	{
		return null;
	}
}