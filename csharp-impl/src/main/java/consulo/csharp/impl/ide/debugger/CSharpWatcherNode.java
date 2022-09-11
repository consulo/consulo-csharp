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

package consulo.csharp.impl.ide.debugger;

import consulo.application.AllIcons;
import consulo.dotnet.debugger.DotNetDebugContext;
import consulo.dotnet.debugger.impl.nodes.DotNetAbstractVariableValueNode;
import consulo.dotnet.debugger.proxy.DotNetStackFrameProxy;
import consulo.dotnet.debugger.proxy.DotNetTypeProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;
import consulo.execution.debug.frame.XValueModifier;
import consulo.ui.image.Image;
import consulo.util.lang.ref.SimpleReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class CSharpWatcherNode extends DotNetAbstractVariableValueNode
{
	private DotNetValueProxy myValue;

	public CSharpWatcherNode(@Nonnull DotNetDebugContext debuggerContext, @Nonnull String name, @Nonnull DotNetStackFrameProxy frameProxy, @Nonnull DotNetValueProxy value)
	{
		super(debuggerContext, name, frameProxy);
		myValue = value;
	}

	@Nonnull
	@Override
	public Image getIconForVariable(@Nullable SimpleReference<DotNetValueProxy> alreadyCalledValue)
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
	public void setValueForVariableImpl(@Nonnull DotNetValueProxy value)
	{

	}

	@Nullable
	@Override
	public DotNetTypeProxy getTypeOfVariableImpl()
	{
		return null;
	}
}
