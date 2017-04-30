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

import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.util.SmartList;
import consulo.dotnet.DotNetTarget;
import consulo.module.extension.ModuleExtensionProviderEP;
import consulo.module.extension.impl.ModuleExtensionProviders;
import consulo.roots.ui.configuration.SdkComboBox;

/**
 * @author VISTALL
 * @since 05.06.14
 */
public class CSharpSdkPanel extends JPanel
{
	private ComboBox<DotNetTarget> myTargetComboBox;
	private SdkComboBox myComboBox;

	private JComponent myTargetComponent;

	public CSharpSdkPanel()
	{
		super(new VerticalFlowLayout());

		myTargetComboBox = new ComboBox<>(DotNetTarget.values());
		myTargetComboBox.setRenderer(new ColoredListCellRenderer<DotNetTarget>()
		{
			@Override
			protected void customizeCellRenderer(@NotNull JList<? extends DotNetTarget> jList, DotNetTarget target, int i, boolean b, boolean b1)
			{
				append(target.getDescription());
			}
		});
		add(myTargetComponent = LabeledComponent.left(myTargetComboBox, "Target"));

		List<String> validSdkTypes = new SmartList<>();
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

		ProjectSdksModel model = new ProjectSdksModel();
		model.reset();

		myComboBox = new SdkComboBox(model, sdkTypeId -> validSdkTypes.contains(sdkTypeId.getName()), false);

		add(LabeledComponent.left(myComboBox, ".NET SDK"));
	}

	@NotNull
	public CSharpSdkPanel disableTargetComboBox(@NotNull DotNetTarget target)
	{
		myTargetComboBox.setSelectedItem(target);
		myTargetComponent.setVisible(false);
		return this;
	}

	@NotNull
	public DotNetTarget getTarget()
	{
		return (DotNetTarget) myTargetComboBox.getSelectedItem();
	}

	@Nullable
	public Sdk getSdk()
	{
		return myComboBox.getSelectedSdk();
	}
}
