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

package org.mustbe.consulo.csharp.module;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleExtension;
import org.mustbe.consulo.module.extension.ConfigurationLayer;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.ColoredListCellRendererWrapper;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBCheckBox;
import lombok.val;

/**
 * @author VISTALL
 * @since 01.02.14
 */
public class CSharpConfigurationLayer implements ConfigurationLayer
{
	private final Project myProject;
	private final String myId;
	private boolean myAllowUnsafeCode;
	private CSharpLanguageVersionPointer myLanguageVersionPointer;

	public CSharpConfigurationLayer(Project project, String id)
	{
		myProject = project;
		myId = id;
		myLanguageVersionPointer = new CSharpLanguageVersionPointer(project, id);
	}

	@Override
	public void loadState(Element element)
	{
		myAllowUnsafeCode = Boolean.valueOf(element.getAttributeValue("unsafe-code", "false"));
		myLanguageVersionPointer.fromXml(element);
	}

	@Override
	public void getState(Element element)
	{
		element.setAttribute("unsafe-code", Boolean.toString(myAllowUnsafeCode));
		myLanguageVersionPointer.toXml(element);
	}

	@Nullable
	@Override
	public JComponent createConfigurablePanel(@NotNull ModifiableRootModel modifiableRootModel, @Nullable Runnable runnable)
	{
		val panel = new JPanel(new VerticalFlowLayout());

		final ComboBox myLanguageLevelComboBox = new ComboBox();
		myLanguageLevelComboBox.setRenderer(new ColoredListCellRendererWrapper<Object>()
		{
			@Override
			protected void doCustomize(JList list, Object value, int index, boolean selected, boolean hasFocus)
			{
				if(value instanceof CSharpLanguageVersion)
				{
					final CSharpLanguageVersion languageLevel = (CSharpLanguageVersion) value;
					append(languageLevel.getPresentableName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
					append(" ");
					append(languageLevel.getDescription(), SimpleTextAttributes.GRAY_ATTRIBUTES);
				}
				else if(value instanceof Module)
				{
					setIcon(AllIcons.Nodes.Module);
					append(((Module) value).getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);

					final CSharpModuleExtension extension = ModuleUtilCore.getExtension((Module) value, CSharpModuleExtension.class);
					if(extension != null)
					{
						final CSharpLanguageVersion languageLevel = extension.getLanguageVersion();
						append("(" + languageLevel.getPresentableName() + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
					}
				}
				else if(value instanceof String)
				{
					setIcon(AllIcons.Nodes.Module);
					append((String) value, SimpleTextAttributes.ERROR_BOLD_ATTRIBUTES);
				}
			}
		});

		for(CSharpLanguageVersion languageLevel : CSharpLanguageVersion.values())
		{
			myLanguageLevelComboBox.addItem(languageLevel);
		}

		for(Module module : ModuleManager.getInstance(modifiableRootModel.getProject()).getModules())
		{
			// dont add self module
			if(module == modifiableRootModel.getModule())
			{
				continue;
			}

			final CSharpModuleExtension extension = (CSharpModuleExtension) ModuleUtilCore.getExtension(module, myId);
			if(extension != null)
			{
				myLanguageLevelComboBox.addItem(module);
			}
		}

		final String moduleName = myLanguageVersionPointer.getModuleName();
		if(moduleName != null)
		{
			final Module module = myLanguageVersionPointer.getModule();
			if(module != null)
			{
				myLanguageLevelComboBox.setSelectedItem(module);
			}
			else
			{
				myLanguageLevelComboBox.addItem(moduleName);
			}
		}
		else
		{
			myLanguageLevelComboBox.setSelectedItem(myLanguageVersionPointer.get());
		}

		myLanguageLevelComboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				final Object selectedItem = myLanguageLevelComboBox.getSelectedItem();
				if(selectedItem instanceof Module)
				{
					myLanguageVersionPointer.set(((Module) selectedItem).getName(), null);
				}
				else if(selectedItem instanceof CSharpLanguageVersion)
				{
					myLanguageVersionPointer.set(null, ((CSharpLanguageVersion) selectedItem).getName());
				}
				else
				{
					myLanguageVersionPointer.set(selectedItem.toString(), null);
				}
			}
		});

		val comp = new JBCheckBox("Allow unsafe code?", myAllowUnsafeCode);
		comp.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				myAllowUnsafeCode = comp.isSelected();
			}
		});

		LabeledComponent<ComboBox> versionLabel = LabeledComponent.create(myLanguageLevelComboBox, "Language Version: ");
		versionLabel.setLabelLocation(BorderLayout.WEST);
		panel.add(versionLabel);
		panel.add(comp);
		return panel;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof CSharpConfigurationLayer)
		{
			CSharpConfigurationLayer other = (CSharpConfigurationLayer) obj;
			return myAllowUnsafeCode == other.isAllowUnsafeCode() && myLanguageVersionPointer.equals(other.myLanguageVersionPointer);
		}
		return false;
	}

	@NotNull
	@Override
	public ConfigurationLayer clone()
	{
		CSharpConfigurationLayer profileEx = new CSharpConfigurationLayer(myProject, myId);
		profileEx.setAllowUnsafeCode(myAllowUnsafeCode);
		profileEx.myLanguageVersionPointer.set(myLanguageVersionPointer);
		return profileEx;
	}

	public boolean isAllowUnsafeCode()
	{
		return myAllowUnsafeCode;
	}

	@NotNull
	public CSharpLanguageVersion getLanguageVersion()
	{
		//noinspection ConstantConditions
		return myLanguageVersionPointer.get();
	}

	public void setAllowUnsafeCode(boolean allowUnsafeCode)
	{
		myAllowUnsafeCode = allowUnsafeCode;
	}

	public void setLanguageVersion(CSharpLanguageVersion languageVersion)
	{
		myLanguageVersionPointer.set(null, languageVersion);
	}
}
