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

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.CSharpIcons;
import org.mustbe.consulo.csharp.module.extension.CSharpMutableModuleExtension;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.dotnet.DotNetTarget;
import consulo.dotnet.module.extension.DotNetMutableModuleExtension;
import consulo.dotnet.roots.orderEntry.DotNetLibraryOrderEntryImpl;
import consulo.ide.impl.NewModuleBuilder;
import consulo.ide.impl.NewModuleBuilderProcessor;
import consulo.ide.impl.NewModuleContext;
import consulo.ide.impl.UnzipNewModuleBuilderProcessor;
import consulo.roots.ModifiableModuleRootLayer;
import consulo.roots.impl.ModuleRootLayerImpl;

/**
 * @author VISTALL
 * @since 05.06.14
 */
public class CSharpNewModuleBuilder implements NewModuleBuilder
{
	private static final String DEBUG = "Debug";
	private static final String RELEASE = "Release";
	private static final String DEFAULT = "Default";

	public static final Map<String, String[]> ourExtensionMapping = new HashMap<String, String[]>()
	{
		{
			put("MONO_DOTNET_SDK", new String[]{
					"mono-dotnet",
					"mono-csharp"
			});
			put("MICROSOFT_DOTNET_SDK", new String[]{
					"microsoft-dotnet",
					"microsoft-csharp"
			});
		}
	};

	@Override
	public void setupContext(@NotNull NewModuleContext context)
	{
		context.addItem("#CSharp", "C#", CSharpIcons.FileType);
		context.addItem("#CSharpEmpty", "Empty", AllIcons.FileTypes.Unknown);
		context.addItem("#CSharpConsoleApplication", "Console Application", AllIcons.RunConfigurations.Application);

		context.setupItem(new String[]{
				"#CSharp",
				"#CSharpEmpty"
		}, new NewModuleBuilderProcessor<CSharpSdkPanel>()
		{
			@NotNull
			@Override
			public CSharpSdkPanel createConfigurationPanel()
			{
				return new CSharpSdkPanel();
			}

			@Override
			public void setupModule(@NotNull CSharpSdkPanel panel,
					@NotNull ContentEntry contentEntry,
					@NotNull ModifiableRootModel modifiableRootModel)
			{
				defaultSetup(panel, modifiableRootModel);
			}
		});

		context.setupItem(new String[]{
				"#CSharp",
				"#CSharpConsoleApplication"
		}, new UnzipNewModuleBuilderProcessor<CSharpSdkPanel>("/moduleTemplates/#CSharpConsoleApplication.zip")
		{
			@NotNull
			@Override
			public CSharpSdkPanel createConfigurationPanel()
			{
				return new CSharpSdkPanel().disableTargetComboBox(DotNetTarget.EXECUTABLE);
			}

			@Override
			public void setupModule(@NotNull CSharpSdkPanel panel,
					@NotNull final ContentEntry contentEntry,
					@NotNull final ModifiableRootModel modifiableRootModel)
			{
				unzip(modifiableRootModel);

				defaultSetup(panel, modifiableRootModel);

				DumbService.getInstance(modifiableRootModel.getProject()).smartInvokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						VirtualFile dir = contentEntry.getFile();
						if(dir != null)
						{
							VirtualFile mainFile = dir.findFileByRelativePath("Program.cs");
							if(mainFile != null)
							{
								FileEditorManagerEx.getInstanceEx(modifiableRootModel.getProject()).openFile(mainFile, false);
							}
						}
					}
				});
			}
		});
	}

	private static void defaultSetup(@NotNull CSharpSdkPanel panel, @NotNull ModifiableRootModel modifiableRootModel)
	{
		Sdk sdk = panel.getSdk();
		if(sdk == null)
		{
			return;
		}

		String[] pair = ourExtensionMapping.get(sdk.getSdkType().getName());

		for(String layerName : new String[]{
				RELEASE,
				DEBUG
		})
		{
			ModifiableModuleRootLayer layer = modifiableRootModel.addLayer(layerName, DEFAULT, false);

			// first we need enable .NET module extension
			DotNetMutableModuleExtension<?> dotNetMutableModuleExtension = layer.getExtensionWithoutCheck(pair[0]);
			assert dotNetMutableModuleExtension != null;

			dotNetMutableModuleExtension.setEnabled(true);
			boolean debug = layerName.equals(DEBUG);
			if(debug)
			{
				dotNetMutableModuleExtension.setAllowDebugInfo(true);
				dotNetMutableModuleExtension.getVariables().add("DEBUG");
			}
			dotNetMutableModuleExtension.getInheritableSdk().set(null, sdk);
			dotNetMutableModuleExtension.setTarget(panel.getTarget());
			dotNetMutableModuleExtension.getVariables().add("TRACE");

			CSharpMutableModuleExtension<?> cSharpMutableModuleExtension = layer.getExtensionWithoutCheck(pair[1]);
			assert cSharpMutableModuleExtension != null;
			cSharpMutableModuleExtension.setEnabled(true);
			if(!debug)
			{
				cSharpMutableModuleExtension.setOptimizeCode(true);
			}

			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl((ModuleRootLayerImpl) layer, "mscorlib"));
			layer.addOrderEntry(new DotNetLibraryOrderEntryImpl((ModuleRootLayerImpl) layer, "System"));
		}

		modifiableRootModel.setCurrentLayer(DEBUG);
		modifiableRootModel.removeLayer(DEFAULT, false);
	}
}
