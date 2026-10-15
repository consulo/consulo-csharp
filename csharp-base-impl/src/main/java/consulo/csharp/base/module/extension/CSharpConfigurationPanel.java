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
import consulo.content.bundle.SdkType;
import consulo.csharp.api.localize.CSharpLocalize;
import consulo.csharp.compiler.CSharpCompilerProvider;
import consulo.csharp.compiler.CSharpPlatform;
import consulo.csharp.module.CSharpNullableOption;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpModuleExtension;
import consulo.csharp.module.extension.CSharpMutableModuleExtension;
import consulo.disposer.Disposable;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.CheckBox;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.TextAttribute;
import consulo.ui.TextBoxWithHistory;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.LabeledLayout;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2014-07-31
 */
public final class CSharpConfigurationPanel {
    @SuppressWarnings("SpellCheckingInspection")
    private static final List<String> COMPILE_LEVELS = List.of(
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
    );

    private CSharpConfigurationPanel() {
    }

    @RequiredUIAccess
    public static Component create(CSharpMutableModuleExtension<?> ext, Disposable uiDisposable) {
        FormBuilder languageForm = FormBuilder.create();
        languageForm.addLabeled(CSharpLocalize.configurationLanguageVersionLabel(), createLanguageVersionBox(ext));

        BundleBox compilerBox = createCompilerBox(ext, uiDisposable);

        TextBoxWithHistory compileLevelBox = TextBoxWithHistory.create(StringUtil.notNullize(ext.getCompilerTarget()));
        compileLevelBox.setHistory(COMPILE_LEVELS);
        compileLevelBox.addValueListener(event -> ext.setCompilerTarget(StringUtil.nullize(event.getValue(), true)));

        ComboBox<CSharpPlatform> platformBox = ComboBox.create(CSharpPlatform.values());
        platformBox.setRenderer((presentation, item) -> {
            CSharpPlatform platform = item.getValue();
            if (platform == null) {
                return;
            }

            switch (platform) {
                case ANY_CPU:
                    presentation.append(CSharpLocalize.configurationPlatformAnyCpu());
                    break;
                case ANY_CPU_32BIT_PREFERRED:
                    presentation.append(CSharpLocalize.configurationPlatformAnyCpu());
                    presentation.append(" ");
                    presentation.append(CSharpLocalize.configurationPlatformAnyCpuPreferred(), TextAttribute.GRAYED);
                    break;
                case ARM:
                    presentation.append("ARM");
                    break;
                case X86:
                    presentation.append("x86");
                    break;
                case X64:
                    presentation.append("x64");
                    break;
                case ITANIUM:
                    presentation.append("Itanium");
                    break;
            }
        });
        platformBox.setValue(ext.getPlatform());
        platformBox.addValueListener(event -> {
            CSharpPlatform platform = event.getValue();
            if (platform != null) {
                ext.setPlatform(platform);
            }
        });

        ComboBox<CSharpNullableOption> nullableBox = ComboBox.create(CSharpNullableOption.values());
        nullableBox.setTextRenderer(option -> option == null ? LocalizeValue.empty() : option.getDescription());
        nullableBox.setValue(ext.getNullableOption());
        nullableBox.addValueListener(event -> {
            CSharpNullableOption option = event.getValue();
            if (option != null) {
                ext.setNullableOption(option);
            }
        });

        CheckBox allowUnsafeCodeBox = CheckBox.create(CSharpLocalize.configurationAllowUnsafeCode(), ext.isAllowUnsafeCode());
        allowUnsafeCodeBox.addValueListener(event -> ext.setAllowUnsafeCode(Boolean.TRUE.equals(event.getValue())));

        CheckBox optimizeCodeBox = CheckBox.create(CSharpLocalize.configurationOptimizeCode(), ext.isOptimizeCode());
        optimizeCodeBox.addValueListener(event -> ext.setOptimizeCode(Boolean.TRUE.equals(event.getValue())));

        FormBuilder compilerForm = FormBuilder.create();
        compilerForm.addLabeled(CSharpLocalize.configurationCompilerLabel(), compilerBox.getComponent());
        compilerForm.addLabeled(CSharpLocalize.configurationCompilerTargetLabel(), compileLevelBox);
        compilerForm.addLabeled(CSharpLocalize.configurationPlatformLabel(), platformBox);
        compilerForm.addLabeled(CSharpLocalize.configurationNullableLabel(), nullableBox);

        VerticalLayout compilerLayout = VerticalLayout.create();
        compilerLayout.add(allowUnsafeCodeBox);
        compilerLayout.add(optimizeCodeBox);
        compilerLayout.add(compilerForm.build());

        VerticalLayout layout = VerticalLayout.create();
        layout.add(languageForm.build());
        layout.add(LabeledLayout.create(CSharpLocalize.configurationCompilerOptionsGroup(), compilerLayout));
        return layout;
    }

    @RequiredUIAccess
    private static ComboBox<Object> createLanguageVersionBox(CSharpMutableModuleExtension<?> ext) {
        List<Object> items = new ArrayList<>(List.of(CSharpLanguageVersion.values()));
        Map<Module, String> inheritedVersions = new HashMap<>();

        for (Module module : ModuleManager.getInstance(ext.getProject()).getModules()) {
            if (module == ext.getModule()) {
                continue;
            }

            CSharpModuleExtension<?> extension = (CSharpModuleExtension<?>) ModuleUtilCore.getExtension(module, ext.getId());
            if (extension != null) {
                items.add(module);

                CSharpModuleExtension<?> csharpExtension = ModuleUtilCore.getExtension(module, CSharpModuleExtension.class);
                if (csharpExtension != null) {
                    inheritedVersions.put(module, csharpExtension.getLanguageVersion().getPresentableName());
                }
            }
        }

        MutableModuleInheritableNamedPointer<CSharpLanguageVersion> languageVersionPointer = ext.getLanguageVersionPointer();

        Object selected = languageVersionPointer.get();
        String moduleName = languageVersionPointer.getModuleName();
        if (moduleName != null) {
            Module module = languageVersionPointer.getModule();
            if (module != null) {
                selected = module;
            }
            else {
                items.add(moduleName);
                selected = moduleName;
            }
        }

        ComboBox<Object> versionBox = ComboBox.create(items);
        versionBox.setRenderer((presentation, item) -> {
            Object value = item.getValue();
            if (value instanceof CSharpLanguageVersion languageVersion) {
                presentation.append(languageVersion.getPresentableName());
                presentation.append(" ");
                presentation.append(languageVersion.getDescription(), TextAttribute.GRAYED);
            }
            else if (value instanceof Module module) {
                presentation.withIcon(PlatformIconGroup.nodesModule());
                presentation.append(module.getName(), TextAttribute.REGULAR_BOLD);

                String inheritedVersion = inheritedVersions.get(module);
                if (inheritedVersion != null) {
                    presentation.append("(" + inheritedVersion + ")", TextAttribute.GRAYED);
                }
            }
            else if (value instanceof String name) {
                presentation.withIcon(PlatformIconGroup.nodesModule());
                presentation.append(name, TextAttribute.ERROR_BOLD);
            }
        });
        versionBox.setValue(selected);
        versionBox.addValueListener(event -> {
            Object value = event.getValue();
            if (value instanceof Module module) {
                languageVersionPointer.set(module.getName(), null);
            }
            else if (value instanceof CSharpLanguageVersion languageVersion) {
                languageVersionPointer.set(null, languageVersion.getName());
            }
            else if (value != null) {
                languageVersionPointer.set(value.toString(), null);
            }
        });
        return versionBox;
    }

    @RequiredUIAccess
    private static BundleBox createCompilerBox(CSharpMutableModuleExtension<?> ext, Disposable uiDisposable) {
        List<CSharpCompilerProvider> providers = CSharpCompilerProvider.EP_NAME.getExtensionList();
        DotNetSimpleModuleExtension netExtension = ext.getModuleRootLayer().getExtension(DotNetSimpleModuleExtension.class);
        Set<SdkType> compilerBundleTypes = new LinkedHashSet<>();
        if (netExtension != null) {
            for (CSharpCompilerProvider provider : providers) {
                SdkType bundleType = provider.getBundleType(netExtension);
                if (bundleType != null) {
                    compilerBundleTypes.add(bundleType);
                }
            }
        }

        BundleBox compilerBox = BundleBoxBuilder.create(uiDisposable)
            .withSdkTypeFilter(compilerBundleTypes::contains)
            .withNoneItem(CSharpLocalize.configurationCompilerAutoSelect(), PlatformIconGroup.actionsFind())
            .build();

        for (CSharpCompilerProvider provider : providers) {
            provider.insertCustomSdkItems(netExtension, compilerBox);
        }

        MutableModuleInheritableNamedPointer<Sdk> customCompilerSdkPointer = ext.getCustomCompilerSdkPointer();
        if (customCompilerSdkPointer.isNull()) {
            compilerBox.setSelectedNoneBundle();
        }
        else {
            compilerBox.setSelectedBundle(customCompilerSdkPointer.getName());
        }

        compilerBox.getComponent().addValueListener(
            event -> customCompilerSdkPointer.set(compilerBox.getSelectedModuleName(), compilerBox.getSelectedBundleName())
        );
        return compilerBox;
    }
}
