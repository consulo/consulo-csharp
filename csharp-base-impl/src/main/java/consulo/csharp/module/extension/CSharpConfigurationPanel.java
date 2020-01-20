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

package consulo.csharp.module.extension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.DocumentEvent;


import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColoredListCellRendererWrapper;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import consulo.csharp.compiler.CSharpCompilerProvider;
import consulo.csharp.compiler.CSharpPlatform;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.roots.ui.configuration.SdkComboBox;
import consulo.ui.annotation.RequiredUIAccess;

/**
 * @author VISTALL
 * @since 31.07.14
 */
public class CSharpConfigurationPanel extends JPanel
{
	private static final String[] ourCompileLevels = new String[]{
			"ISO-1",
			"ISO-2",
			"3",
			"4",
			"5",
			"6",
			"default"
	};

	@RequiredUIAccess
	public CSharpConfigurationPanel(final CSharpMutableModuleExtension<?> ext)
	{
		super(new VerticalFlowLayout());
		final ComboBox levelComboBox = new ComboBox();
		levelComboBox.setRenderer(new ColoredListCellRendererWrapper<Object>()
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
			levelComboBox.addItem(languageLevel);
		}

		for(Module module : ModuleManager.getInstance(ext.getProject()).getModules())
		{
			// dont add self module
			if(module == ext.getModule())
			{
				continue;
			}

			final CSharpModuleExtension extension = (CSharpModuleExtension) ModuleUtilCore.getExtension(module, ext.getId());
			if(extension != null)
			{
				levelComboBox.addItem(module);
			}
		}

		final MutableModuleInheritableNamedPointer<CSharpLanguageVersion> languageVersionPointer = ext.getLanguageVersionPointer();
		final String moduleName = languageVersionPointer.getModuleName();
		if(moduleName != null)
		{
			final Module module = languageVersionPointer.getModule();
			if(module != null)
			{
				levelComboBox.setSelectedItem(module);
			}
			else
			{
				levelComboBox.addItem(moduleName);
			}
		}
		else
		{
			levelComboBox.setSelectedItem(languageVersionPointer.get());
		}

		levelComboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				final Object selectedItem = levelComboBox.getSelectedItem();
				if(selectedItem instanceof Module)
				{
					languageVersionPointer.set(((Module) selectedItem).getName(), null);
				}
				else if(selectedItem instanceof CSharpLanguageVersion)
				{
					languageVersionPointer.set(null, ((CSharpLanguageVersion) selectedItem).getName());
				}
				else
				{
					languageVersionPointer.set(selectedItem.toString(), null);
				}
			}
		});

		add(LabeledComponent.left(levelComboBox, "Language Version: "));

		final TextFieldWithHistory compileLevelField = new TextFieldWithHistory();
		compileLevelField.setHistory(Arrays.asList(ourCompileLevels));
		compileLevelField.setHistorySize(-1);
		String compilerTarget = ext.getCompilerTarget();
		if(compilerTarget != null)
		{
			compileLevelField.setText(compilerTarget);
		}
		compileLevelField.addDocumentListener(new DocumentAdapter()
		{
			@Override
			protected void textChanged(DocumentEvent e)
			{
				ext.setCompilerTarget(StringUtil.nullize(compileLevelField.getText(), true));
			}
		});

		add(new TitledSeparator("Compiler Options"));

		List<CSharpCompilerProvider> extensions = CSharpCompilerProvider.EP_NAME.getExtensionList();
		DotNetSimpleModuleExtension netExtension = ext.getModuleRootLayer().getExtension(DotNetSimpleModuleExtension.class);
		final Set<SdkType> compilerBundleTypes = new LinkedHashSet<SdkType>();
		if(netExtension != null)
		{
			for(CSharpCompilerProvider typeProvider : extensions)
			{
				SdkType bundleType = typeProvider.getBundleType(netExtension);
				if(bundleType != null)
				{
					compilerBundleTypes.add(bundleType);
				}
			}
		}

		final SdkModel projectSdksModel = ProjectStructureConfigurable.getInstance(ext.getProject()).getProjectSdksModel();

		final SdkComboBox compilerComboBox = new SdkComboBox(projectSdksModel, compilerBundleTypes::contains, null, "Auto Select", AllIcons.Actions.FindPlain);

		for(CSharpCompilerProvider provider : extensions)
		{
			provider.insertCustomSdkItems(netExtension, compilerComboBox);
		}

		final MutableModuleInheritableNamedPointer<Sdk> customCompilerSdkPointer = ext.getCustomCompilerSdkPointer();
		if(customCompilerSdkPointer.isNull())
		{
			compilerComboBox.setSelectedNoneSdk();
		}
		else
		{
			compilerComboBox.setSelectedSdk(customCompilerSdkPointer.getName());
		}

		compilerComboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					customCompilerSdkPointer.set(compilerComboBox.getSelectedModuleName(), compilerComboBox.getSelectedSdkName());
				}
			}
		});

		add(LabeledComponent.left(compilerComboBox, "Compiler"));
		add(LabeledComponent.left(compileLevelField, "Compiler Target (/langversion):"));

		final ComboBox platformComboBox = new ComboBox(CSharpPlatform.values());
		platformComboBox.setSelectedItem(ext.getPlatform());
		platformComboBox.setRenderer(new ColoredListCellRendererWrapper<CSharpPlatform>()
		{
			@Override
			protected void doCustomize(JList jList, CSharpPlatform cSharpPlatform, int i, boolean b, boolean b2)
			{
				switch(cSharpPlatform)
				{
					case ANY_CPU:
						append("Any CPU");
						break;
					case ANY_CPU_32BIT_PREFERRED:
						append("Any CPU");
						append(" (32 bit preferred)", SimpleTextAttributes.GRAY_ATTRIBUTES);
						break;
					case ARM:
						append("ARM");
						break;
					case X86:
						append("x86");
						break;
					case X64:
						append("x64");
						break;
					case ITANIUM:
						append("Itanium");
						break;
				}
			}
		});

		platformComboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					ext.setPlatform((CSharpPlatform) platformComboBox.getSelectedItem());
				}
			}
		});

		add(LabeledComponent.left(platformComboBox, "Platform (/platform): "));

		final JBCheckBox allowUnsafeCode = new JBCheckBox("Allow unsafe code (/unsafe)", ext.isAllowUnsafeCode());
		allowUnsafeCode.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ext.setAllowUnsafeCode(allowUnsafeCode.isSelected());
			}
		});

		add(allowUnsafeCode);

		final JBCheckBox optimizeCode = new JBCheckBox("Optimize Code (/optimize+)", ext.isOptimizeCode());
		optimizeCode.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ext.setOptimizeCode(optimizeCode.isSelected());
			}
		});
		add(optimizeCode);
	}
}
