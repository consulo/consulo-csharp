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

package consulo.csharp.ide.newProjectOrModule;

import com.intellij.openapi.projectRoots.SdkTable;
import consulo.bundle.ui.BundleBox;
import consulo.bundle.ui.BundleBoxBuilder;
import consulo.disposer.Disposable;
import consulo.dotnet.DotNetTarget;
import consulo.ide.newProject.ui.UnifiedProjectOrModuleNameStep;
import consulo.localize.LocalizeValue;
import consulo.module.extension.ModuleExtensionProviderEP;
import consulo.module.extension.impl.ModuleExtensionProviders;
import consulo.ui.ComboBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.model.ListModel;
import consulo.ui.util.FormBuilder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 05.06.14
 */
public class CSharpSetupStep extends UnifiedProjectOrModuleNameStep<CSharpNewModuleContext>
{
	private DotNetTarget myForceTarget;

	private Disposable myUiDisposable;

	private ComboBox<DotNetTarget> myTargetComboBox;
	private BundleBox myBundleBox;

	public CSharpSetupStep(CSharpNewModuleContext context)
	{
		super(context);
	}

	@RequiredUIAccess
	@Override
	protected void extend(@Nonnull FormBuilder builder)
	{
		super.extend(builder);

		if(myForceTarget == null)
		{
			myTargetComboBox = ComboBox.create(DotNetTarget.values());
			myTargetComboBox.setValue(DotNetTarget.EXECUTABLE);
			myTargetComboBox.setTextRender(DotNetTarget::getDescription);

			builder.addLabeled(LocalizeValue.localizeTODO("Target:"), myTargetComboBox);
		}

		myUiDisposable = Disposable.newDisposable();

		List<String> validSdkTypes = new ArrayList<>();
		for(Map.Entry<String, String[]> entry : CSharpNewModuleBuilder.ourExtensionMapping.entrySet())
		{
			// need check C# extension
			ModuleExtensionProviderEP providerEP = ModuleExtensionProviders.findProvider(entry.getValue()[1]);
			if(providerEP == null)
			{
				continue;
			}
			validSdkTypes.add(entry.getKey());
		}

		BundleBoxBuilder boxBuilder = BundleBoxBuilder.create(myUiDisposable);
		boxBuilder.withSdkTypeFilter(sdkTypeId -> validSdkTypes.contains(sdkTypeId.getId()));

		myBundleBox = boxBuilder.build();
		ListModel<BundleBox.BundleBoxItem> listModel = myBundleBox.getComponent().getListModel();
		// select first
		if(listModel.getSize() > 0)
		{
			myBundleBox.getComponent().setValue(listModel.get(0));
		}
		builder.addLabeled(LocalizeValue.localizeTODO(".NET SDK:"), myBundleBox.getComponent());
	}

	@Override
	public void onStepLeave(@Nonnull CSharpNewModuleContext context)
	{
		super.onStepLeave(context);

		context.setTarget(myForceTarget != null ? myForceTarget : myTargetComboBox.getValueOrError());

		context.setSdk(SdkTable.getInstance().findSdk(myBundleBox.getSelectedBundleName()));
	}

	@Nonnull
	public CSharpSetupStep disableTargetComboBox(@Nonnull DotNetTarget target)
	{
		myForceTarget = target;
		return this;
	}

	@Override
	public void disposeUIResources()
	{
		super.disposeUIResources();

		if(myUiDisposable != null)
		{
			myUiDisposable.disposeWithTree();
			myUiDisposable = null;
		}
	}
}
