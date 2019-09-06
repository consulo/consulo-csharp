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
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.util.SmartList;
import consulo.dotnet.DotNetTarget;
import consulo.ide.newProject.ui.ProjectOrModuleNameStep;
import consulo.module.extension.ModuleExtensionProviderEP;
import consulo.module.extension.impl.ModuleExtensionProviders;
import consulo.roots.ui.configuration.SdkComboBox;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 05.06.14
 */
public class CSharpSetupStep extends ProjectOrModuleNameStep<CSharpNewModuleContext>
{
	private ComboBox<DotNetTarget> myTargetComboBox;
	private SdkComboBox myComboBox;

	private JComponent myTargetComponent;

	public CSharpSetupStep(CSharpNewModuleContext context)
	{
		super(context);

		JPanel panel = new JPanel(new VerticalFlowLayout());

		myTargetComboBox = new ComboBox<>(DotNetTarget.values());
		myTargetComboBox.setRenderer(new ColoredListCellRenderer<DotNetTarget>()
		{
			@Override
			protected void customizeCellRenderer(@Nonnull JList<? extends DotNetTarget> jList, DotNetTarget target, int i, boolean b, boolean b1)
			{
				append(target.getDescription());
			}
		});
		myTargetComboBox.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED)
			{
				context.setTarget((DotNetTarget) myTargetComboBox.getSelectedItem());
			}
		});

		panel.add(myTargetComponent = LabeledComponent.create(myTargetComboBox, "Target"));

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

		myComboBox = new SdkComboBox(SdkTable.getInstance(), sdkTypeId -> validSdkTypes.contains(sdkTypeId.getName()), false);
		myComboBox.addItemListener(e -> {
			if(e.getStateChange() == ItemEvent.SELECTED)
			{
				context.setSdk(myComboBox.getSelectedSdk());
			}
		});
		context.setSdk(myComboBox.getSelectedSdk());

		panel.add(LabeledComponent.create(myComboBox, ".NET SDK"));

		myAdditionalContentPanel.add(panel, BorderLayout.NORTH);
	}

	@Nonnull
	public CSharpSetupStep disableTargetComboBox(@Nonnull DotNetTarget target)
	{
		myTargetComboBox.setSelectedItem(target);
		myTargetComponent.setVisible(false);
		return this;
	}

	@Nonnull
	public DotNetTarget getTarget()
	{
		return (DotNetTarget) myTargetComboBox.getSelectedItem();
	}
}
