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

package consulo.csharp.microsoft.module.extension;

import consulo.csharp.base.module.extension.CSharpConfigurationPanel;
import consulo.csharp.module.extension.CSharpMutableModuleExtension;
import consulo.disposer.Disposable;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;

import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 26.11.13.
 */
public class MicrosoftCSharpMutableModuleExtension extends MicrosoftCSharpModuleExtension implements CSharpMutableModuleExtension<MicrosoftCSharpModuleExtension>
{
	public MicrosoftCSharpMutableModuleExtension(String id, ModuleRootLayer module)
	{
		super(id, module);
	}

	@RequiredUIAccess
	@Nullable
	@Override
	public Component createConfigurationComponent(Disposable disposable, Runnable runnable)
	{
		return CSharpConfigurationPanel.create(this, disposable);
	}

	@Override
	public void setEnabled(boolean val)
	{
		myIsEnabled = val;
	}

	@Override
	public boolean isModified(MicrosoftCSharpModuleExtension moduleExtension)
	{
		return isModifiedImpl(moduleExtension);
	}
}
