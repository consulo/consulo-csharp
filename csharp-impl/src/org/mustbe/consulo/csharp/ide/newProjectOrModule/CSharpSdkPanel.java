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

package org.mustbe.consulo.csharp.ide.newProjectOrModule;

import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.DotNetTarget;
import org.mustbe.consulo.module.extension.ModuleExtensionProviderEP;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.projectRoots.impl.SdkListCellRenderer;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.ColoredListCellRendererWrapper;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 05.06.14
 */
public class CSharpSdkPanel extends JPanel
{
	private ComboBox myTargetComboBox;
	private ComboBox myComboBox;

	private JComponent myTargetComponent;

	public CSharpSdkPanel()
	{
		super(new VerticalFlowLayout());

		myTargetComboBox = new ComboBox(DotNetTarget.values());
		myTargetComboBox.setRenderer(new ColoredListCellRendererWrapper<DotNetTarget>()
		{
			@Override
			protected void doCustomize(JList list, DotNetTarget value, int index, boolean selected, boolean hasFocus)
			{
				append(value.getDescription());
			}
		});
		add(myTargetComponent = LabeledComponent.left(myTargetComboBox, "Target"));

		SdkTable sdkTable = SdkTable.getInstance();
		myComboBox = new ComboBox();
		myComboBox.setRenderer(new SdkListCellRenderer("<none>"));

		List<String> validSdkTypes = new SmartList<String>();
		for(Map.Entry<String, String[]> entry : CSharpNewModuleBuilder.ourExtensionMapping.entrySet())
		{
			// need check C# extension
			ModuleExtensionProviderEP providerEP = ModuleExtensionProviderEP.findProviderEP(entry.getValue()[1]);
			if(providerEP == null)
			{
				continue;
			}
			validSdkTypes.add(entry.getKey());
		}

		for(Sdk o : sdkTable.getAllSdks())
		{
			SdkTypeId sdkType = o.getSdkType();
			if(validSdkTypes.contains(sdkType.getName()))
			{
				myComboBox.addItem(o);
			}
		}

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
		return (Sdk) myComboBox.getSelectedItem();
	}
}
