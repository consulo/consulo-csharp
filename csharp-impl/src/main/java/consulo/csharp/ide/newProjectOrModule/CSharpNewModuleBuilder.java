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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.module.extension.CSharpMutableModuleExtension;
import consulo.dotnet.DotNetTarget;
import consulo.dotnet.module.extension.DotNetMutableModuleExtension;
import consulo.dotnet.roots.orderEntry.DotNetLibraryOrderEntryImpl;
import consulo.ide.impl.UnzipNewModuleBuilderProcessor;
import consulo.ide.newProject.NewModuleBuilder;
import consulo.ide.newProject.NewModuleBuilderProcessor;
import consulo.ide.newProject.NewModuleContext;
import consulo.roots.ModifiableModuleRootLayer;
import consulo.roots.impl.ModuleRootLayerImpl;
import consulo.ui.wizard.WizardStep;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

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
	public void setupContext(@Nonnull NewModuleContext context)
	{
		NewModuleContext.Group group = context.createGroup("csharp", "C#");

		group.add("Empty", AllIcons.FileTypes.Any_type, new NewModuleBuilderProcessor<CSharpNewModuleContext>()
		{
			@Nonnull
			@Override
			public CSharpNewModuleContext createContext(boolean isNewProject)
			{
				return new CSharpNewModuleContext(isNewProject);
			}

			@Override
			public void buildSteps(@Nonnull Consumer<WizardStep<CSharpNewModuleContext>> consumer, @Nonnull CSharpNewModuleContext context)
			{
				consumer.accept(new CSharpSetupStep(context));
			}

			@RequiredReadAction
			@Override
			public void process(@Nonnull CSharpNewModuleContext context, @Nonnull ContentEntry contentEntry, @Nonnull ModifiableRootModel modifiableRootModel)
			{
				defaultSetup(context, modifiableRootModel);
			}
		});

		group.add("Console Application", AllIcons.RunConfigurations.Application, new UnzipNewModuleBuilderProcessor<CSharpNewModuleContext>("/moduleTemplates/#CSharpConsoleApplication.zip")
		{
			@Nonnull
			@Override
			public CSharpNewModuleContext createContext(boolean isNewProject)
			{
				return new CSharpNewModuleContext(isNewProject);
			}

			@Override
			public void buildSteps(@Nonnull Consumer<WizardStep<CSharpNewModuleContext>> consumer, @Nonnull CSharpNewModuleContext context)
			{
				consumer.accept(new CSharpSetupStep(context).disableTargetComboBox(DotNetTarget.EXECUTABLE));
			}

			@RequiredReadAction
			@Override
			public void process(@Nonnull CSharpNewModuleContext context, @Nonnull ContentEntry contentEntry, @Nonnull ModifiableRootModel modifiableRootModel)
			{
				unzip(modifiableRootModel);

				defaultSetup(context, modifiableRootModel);

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

	private static void defaultSetup(@Nonnull CSharpNewModuleContext context, @Nonnull ModifiableRootModel modifiableRootModel)
	{
		Sdk sdk = context.getSdk();
		if(sdk == null)
		{
			return;
		}

		String[] pair = ourExtensionMapping.get(sdk.getSdkType().getId());

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
			dotNetMutableModuleExtension.setTarget(context.getTarget());
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
