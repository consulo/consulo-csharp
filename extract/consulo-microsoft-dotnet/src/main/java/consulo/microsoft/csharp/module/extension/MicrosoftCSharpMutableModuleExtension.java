/*
 * Copyright 2013 must-be.org
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

package consulo.microsoft.csharp.module.extension;

import javax.annotation.Nonnull;
import javax.swing.JComponent;

import javax.annotation.Nullable;
import consulo.csharp.module.extension.CSharpConfigurationPanel;
import consulo.csharp.module.extension.CSharpMutableModuleExtension;
import consulo.annotations.RequiredDispatchThread;
import consulo.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 26.11.13.
 */
public class MicrosoftCSharpMutableModuleExtension extends MicrosoftCSharpModuleExtension implements CSharpMutableModuleExtension<MicrosoftCSharpModuleExtension>
{
	public MicrosoftCSharpMutableModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer module)
	{
		super(id, module);
	}

	@Nullable
	@Override
	@RequiredDispatchThread
	public JComponent createConfigurablePanel(@Nonnull Runnable runnable)
	{
		return new CSharpConfigurationPanel(this);
	}

	@Override
	public void setEnabled(boolean val)
	{
		myIsEnabled = val;
	}

	@Override
	public boolean isModified(@Nonnull MicrosoftCSharpModuleExtension moduleExtension)
	{
		return isModifiedImpl(moduleExtension);
	}
}
