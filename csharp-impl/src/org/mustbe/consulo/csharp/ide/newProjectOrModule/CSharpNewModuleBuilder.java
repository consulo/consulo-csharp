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
import org.mustbe.consulo.dotnet.module.extension.DotNetMutableModuleExtension;
import org.mustbe.consulo.dotnet.module.roots.DotNetLibraryOrderEntryImpl;
import org.mustbe.consulo.ide.impl.NewModuleBuilder;
import org.mustbe.consulo.ide.impl.NewModuleContext;
import org.mustbe.consulo.ide.impl.UnzipNewModuleBuilderProcessor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableModuleRootLayer;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.impl.ModuleRootLayerImpl;

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
		context.addItem("#CSharpHelloWorld", "Hello World", AllIcons.RunConfigurations.Application);

		context.setupItem(new String[]{
				"#CSharp",
				"#CSharpHelloWorld"
		}, new UnzipNewModuleBuilderProcessor<CSharpNewModuleBuilderPanel>("/moduleTemplates/#CSharpHelloWorld.zip")
		{
			@NotNull
			@Override
			public CSharpNewModuleBuilderPanel createConfigurationPanel()
			{
				return new CSharpNewModuleBuilderPanel();
			}

			@Override
			public void setupModule(@NotNull CSharpNewModuleBuilderPanel panel, @NotNull ContentEntry contentEntry,
					@NotNull ModifiableRootModel modifiableRootModel)
			{
				unzip(modifiableRootModel);

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

					assert layer != null;

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

					CSharpMutableModuleExtension<?> cSharpMutableModuleExtension = layer.getExtensionWithoutCheck(pair[1]);
					assert cSharpMutableModuleExtension != null;
					cSharpMutableModuleExtension.setEnabled(true);

					layer.addOrderEntry(new DotNetLibraryOrderEntryImpl((ModuleRootLayerImpl) layer, "mscorlib.dll"));
					layer.addOrderEntry(new DotNetLibraryOrderEntryImpl((ModuleRootLayerImpl) layer, "System.dll"));
				}

				modifiableRootModel.setCurrentLayer(DEBUG);
				modifiableRootModel.removeLayer(DEFAULT, false);
			}
		});
	}
}
