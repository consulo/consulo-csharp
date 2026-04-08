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
package consulo.csharp.base.module.extension;

import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkModel;
import consulo.content.bundle.SdkType;
import consulo.csharp.compiler.CSharpCompilerProvider;
import consulo.csharp.compiler.CSharpPlatform;
import consulo.csharp.module.CSharpNullableOption;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpModuleExtension;
import consulo.csharp.module.extension.CSharpMutableModuleExtension;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.ide.setting.ProjectStructureSettingsUtil;
import consulo.ide.setting.ShowSettingsUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.module.ui.awt.SdkComboBox;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.SimpleTextAttributes;
import consulo.ui.ex.awt.*;
import consulo.ui.ex.awt.event.DocumentAdapter;
import consulo.util.lang.StringUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2014-07-31
 */
public class CSharpConfigurationPanel extends JPanel {
    private static final String[] COMPILE_LEVELS = new String[]{
        "ISO-1",
        "ISO-2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "7.1",
        "7.2",
        "7.3",
        "8",
        "preview",
        "latest",
        "latestmajor",
        "default"
    };

    @RequiredUIAccess
    public CSharpConfigurationPanel(final CSharpMutableModuleExtension<?> ext) {
        super(new VerticalFlowLayout());
        final ComboBox<Object> levelComboBox = new ComboBox<>();
        levelComboBox.setRenderer(new ColoredListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(JList list, Object value, int i, boolean b, boolean b1) {
                if (value instanceof CSharpLanguageVersion languageLevel) {
                    append(languageLevel.getPresentableName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    append(" ");
                    append(languageLevel.getDescription(), SimpleTextAttributes.GRAY_ATTRIBUTES);
                }
                else if (value instanceof Module module) {
                    setIcon(PlatformIconGroup.nodesModule());
                    append(module.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);

                    CSharpModuleExtension extension = ModuleUtilCore.getExtension(module, CSharpModuleExtension.class);
                    if (extension != null) {
                        CSharpLanguageVersion languageLevel = extension.getLanguageVersion();
                        append("(" + languageLevel.getPresentableName() + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
                    }
                }
                else if (value instanceof String stringValue) {
                    setIcon(PlatformIconGroup.nodesModule());
                    append(stringValue, SimpleTextAttributes.ERROR_BOLD_ATTRIBUTES);
                }
            }
        });

        for (CSharpLanguageVersion languageLevel : CSharpLanguageVersion.values()) {
            levelComboBox.addItem(languageLevel);
        }

        for (Module module : ModuleManager.getInstance(ext.getProject()).getModules()) {
            // don't add self module
            if (module == ext.getModule()) {
                continue;
            }

            CSharpModuleExtension extension = (CSharpModuleExtension) ModuleUtilCore.getExtension(module, ext.getId());
            if (extension != null) {
                levelComboBox.addItem(module);
            }
        }

        final MutableModuleInheritableNamedPointer<CSharpLanguageVersion> languageVersionPointer = ext.getLanguageVersionPointer();
        String moduleName = languageVersionPointer.getModuleName();
        if (moduleName != null) {
            Module module = languageVersionPointer.getModule();
            if (module != null) {
                levelComboBox.setSelectedItem(module);
            }
            else {
                levelComboBox.addItem(moduleName);
            }
        }
        else {
            levelComboBox.setSelectedItem(languageVersionPointer.get());
        }

        levelComboBox.addItemListener(new ItemListener() {
            @Override
            @RequiredUIAccess
            public void itemStateChanged(ItemEvent e) {
                Object selectedItem = levelComboBox.getSelectedItem();
                if (selectedItem instanceof Module module) {
                    languageVersionPointer.set(module.getName(), null);
                }
                else if (selectedItem instanceof CSharpLanguageVersion languageLevel) {
                    languageVersionPointer.set(null, languageLevel.getName());
                }
                else {
                    languageVersionPointer.set(selectedItem.toString(), null);
                }
            }
        });

        add(LabeledComponent.create(levelComboBox, "Language Version: "));

        final TextFieldWithHistory compileLevelField = new TextFieldWithHistory();
        compileLevelField.setHistory(Arrays.asList(COMPILE_LEVELS));
        compileLevelField.setHistorySize(-1);
        String compilerTarget = ext.getCompilerTarget();
        if (compilerTarget != null) {
            compileLevelField.setText(compilerTarget);
        }
        compileLevelField.addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                ext.setCompilerTarget(StringUtil.nullize(compileLevelField.getText(), true));
            }
        });

        add(new TitledSeparator("Compiler Options"));

        List<CSharpCompilerProvider> extensions = CSharpCompilerProvider.EP_NAME.getExtensionList();
        DotNetSimpleModuleExtension netExtension = ext.getModuleRootLayer().getExtension(DotNetSimpleModuleExtension.class);
        Set<SdkType> compilerBundleTypes = new LinkedHashSet<>();
        if (netExtension != null) {
            for (CSharpCompilerProvider typeProvider : extensions) {
                SdkType bundleType = typeProvider.getBundleType(netExtension);
                if (bundleType != null) {
                    compilerBundleTypes.add(bundleType);
                }
            }
        }

        ProjectStructureSettingsUtil settingsUtil = ShowSettingsUtil.getInstance();
        SdkModel model = settingsUtil.getSdksModel();
        SdkComboBox compilerComboBox = new SdkComboBox(
            model,
            compilerBundleTypes::contains,
            null,
            LocalizeValue.localizeTODO("Auto Select"),
            PlatformIconGroup.actionsFind()
        );

        for (CSharpCompilerProvider provider : extensions) {
            provider.insertCustomSdkItems(netExtension, compilerComboBox);
        }

        MutableModuleInheritableNamedPointer<Sdk> customCompilerSdkPointer = ext.getCustomCompilerSdkPointer();
        if (customCompilerSdkPointer.isNull()) {
            compilerComboBox.setSelectedNoneSdk();
        }
        else {
            compilerComboBox.setSelectedSdk(customCompilerSdkPointer.getName());
        }

        compilerComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                customCompilerSdkPointer.set(compilerComboBox.getSelectedModuleName(), compilerComboBox.getSelectedSdkName());
            }
        });

        add(LabeledComponent.create(compilerComboBox, "Compiler"));
        add(LabeledComponent.create(compileLevelField, "Compiler Target (/langversion):"));

        ComboBox<CSharpPlatform> platformComboBox = new ComboBox<>(CSharpPlatform.values());
        platformComboBox.setSelectedItem(ext.getPlatform());
        platformComboBox.setRenderer(new ColoredListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(JList jList, CSharpPlatform platform, int i, boolean b, boolean b1) {
                switch (platform) {
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

        platformComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ext.setPlatform((CSharpPlatform) platformComboBox.getSelectedItem());
            }
        });

        add(LabeledComponent.create(platformComboBox, "Platform (/platform): "));

        JBCheckBox allowUnsafeCode = new JBCheckBox("Allow unsafe code (/unsafe)", ext.isAllowUnsafeCode());
        allowUnsafeCode.addActionListener(e -> ext.setAllowUnsafeCode(allowUnsafeCode.isSelected()));

        add(allowUnsafeCode);

        JBCheckBox optimizeCode = new JBCheckBox("Optimize Code (/optimize+)", ext.isOptimizeCode());
        optimizeCode.addActionListener(e -> ext.setOptimizeCode(optimizeCode.isSelected()));
        add(optimizeCode);

        ComboBox<CSharpNullableOption> nullableBox = new ComboBox<>(CSharpNullableOption.values());
        nullableBox.setRenderer(new ColoredListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(JList jList, CSharpNullableOption option, int i, boolean b, boolean b1) {
                append(option.getDescription().getValue());
            }
        });
        nullableBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ext.setNullableOption((CSharpNullableOption) nullableBox.getSelectedItem());
            }
        });
        nullableBox.setSelectedItem(ext.getNullableOption());
        add(LabeledComponent.create(nullableBox, "Nullable C# 8+ (/nullable)"));
    }
}
